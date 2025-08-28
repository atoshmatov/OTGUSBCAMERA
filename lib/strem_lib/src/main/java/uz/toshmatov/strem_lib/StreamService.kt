package uz.toshmatov.strem_lib

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.view.OpenGlView
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StreamService : Service() {

    companion object {
        private const val TAG = "StreamService -->"
        private const val CHANNEL_ID = "rtpStreamChannel"
        private const val NOTIFY_ID = 123456
        var openGlView: OpenGlView? = null
    }

    private var rtmpUSB: RtmpUSB? = null
    private var uvcCamera: UVCCamera? = null
    private var usbMonitor: USBMonitor? = null
    private var endpoint: String? = null
    private var isServiceDestroying = false

    private var previewHandler: Handler? = null
    private var streamHandler: Handler? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var cameraWidth = 1280
    var cameraHeight = 960

    /** Public holat: UI/ViewModel shu bilan kuzatadi */
    private val _isStreamingFlow = MutableStateFlow(false)
    val isStreamingFlow = _isStreamingFlow.asStateFlow()

    val isStreaming: Boolean
        get() = rtmpUSB?.isStreaming == true && !isServiceDestroying

    private val notificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): StreamService = this@StreamService
        fun streamingFlow() = isStreamingFlow
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceNotification()
        initializeHandlers()
        initializeUSBMonitor()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        try {
            Log.w(TAG, "onTaskRemoved -> cleaning and stopping self")
            stopStream(true)
            stopPreview()
            cleanupCamera()
            usbMonitor?.unregister()
            usbMonitor = null
            _isStreamingFlow.value = false
            notificationManager.cancel(NOTIFY_ID)
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy()")
        isServiceDestroying = true
        cleanupResources()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "RTP Stream", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "RTP Streaming Service"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("RTP Stream")
            .setContentText("Service running…")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
        }
        startForeground(NOTIFY_ID, builder.build())
    }

    private fun showNotification(text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("RTP Stream")
            .setContentText(text)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        notificationManager.notify(NOTIFY_ID, notification)
    }

    private fun initializeHandlers() {
        previewHandler = Handler(Looper.getMainLooper())
        streamHandler = Handler(Looper.getMainLooper())
    }

    private fun initializeUSBMonitor() {
        if (usbMonitor == null) {
            usbMonitor = USBMonitor(this, onDeviceConnectListener).apply { register() }
        }
    }

    private val onDeviceConnectListener = object : USBMonitor.OnDeviceConnectListener {
        override fun onConnect(
            device: UsbDevice?,
            ctrlBlock: USBMonitor.UsbControlBlock?,
            createNew: Boolean
        ) {
            if (isServiceDestroying) return
            Log.d(TAG, "USB onConnect")
            cleanupCamera()

            val camera = UVCCamera()
            try {
                camera.open(ctrlBlock)
                val sizes = runCatching { camera.supportedSizeList }.getOrNull()
                val maxSize = sizes?.maxByOrNull { it.width * it.height }
                cameraWidth = maxSize?.width ?: 1280
                cameraHeight = maxSize?.height ?: 960

                runCatching {
                    camera.setPreviewSize(
                        cameraWidth,
                        cameraHeight,
                        UVCCamera.FRAME_FORMAT_MJPEG
                    )
                }.onFailure {
                    camera.setPreviewSize(
                        cameraWidth,
                        cameraHeight,
                        UVCCamera.DEFAULT_PREVIEW_MODE
                    )
                }

                uvcCamera = camera

                previewHandler?.postDelayed({
                    prepareRtmpUSB()
                    runCatching { rtmpUSB?.startPreview(camera, cameraWidth, cameraHeight) }
                    endpoint?.let { url ->
                        streamHandler?.postDelayed({ startStreamRtp(url) }, 350)
                    }
                }, 300)
            } catch (e: Exception) {
                Log.e(TAG, "Camera connect error: ${e.message}")
                camera.destroy()
            }
        }

        override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            Log.w(TAG, "USB onDisconnect")
            stopStream(true)          // streamni to‘xtat
            _isStreamingFlow.value = false
            cleanupCamera()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        override fun onAttach(device: UsbDevice?) {
            Log.d(TAG, "USB onAttach -> requestPermission")
            usbMonitor?.requestPermission(device)
        }

        override fun onCancel(device: UsbDevice?) {
            Log.d(TAG, "USB permission cancelled")
        }

        override fun onDettach(device: UsbDevice?) {
            Log.w(TAG, "USB onDettach")
            stopStream(true)
            _isStreamingFlow.value = false
            cleanupCamera()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun cleanupCamera() {
        uvcCamera?.apply {
            runCatching { stopPreview() }
            runCatching { close() }
            runCatching { destroy() }
        }
        uvcCamera = null
    }

    private fun prepareRtmpUSB() {
        stopStream()
        stopPreview()
        rtmpUSB = if (openGlView == null) {
            RtmpUSB(this, connectCheckerRtmp)
        } else {
            RtmpUSB(openGlView!!, connectCheckerRtmp)
        }
    }

    fun startStreamRtp(endpoint: String): Boolean {
        if (isServiceDestroying) return false
        if (uvcCamera == null) {
            Log.e(TAG, "Camera not ready for streaming")
            return false
        }
        this.endpoint = endpoint
        return try {
            prepareRtmpUSB()
            rtmpUSB?.let { r ->
                val ok = r.prepareVideo(
                    cameraWidth,
                    cameraHeight,
                    30,
                    4000 * 1024,
                    0,
                    uvcCamera
                ) && r.prepareAudio()
                if (ok) {
                    r.startStream(uvcCamera, endpoint)
                    _isStreamingFlow.value = true
                    showNotification("Streaming started")
                    true
                } else false
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error starting stream: ${e.message}")
            _isStreamingFlow.value = false
            false
        }
    }

    fun stopStream(force: Boolean = false) {
        if (force) endpoint = null
        try {
            rtmpUSB?.stopStream(uvcCamera)
        } catch (_: Exception) {
        }
        _isStreamingFlow.value = false
        if (!isServiceDestroying) showNotification("Streaming stopped")
    }

    fun startPreview() {
        uvcCamera?.let { cam ->
            runCatching { rtmpUSB?.startPreview(cam, cameraWidth, cameraHeight) }
        }
    }

    fun stopPreview() {
        runCatching { rtmpUSB?.stopPreview(uvcCamera) }
    }

    fun setView2(view: OpenGlView) {
        openGlView = view
        runCatching { rtmpUSB?.replaceView(view, uvcCamera) }
    }

    fun setView(context: Context) {
        openGlView = null
        runCatching { rtmpUSB?.replaceView(context, uvcCamera) }
    }

    fun setView(view: OpenGlView) {
        if (openGlView != view) {
            stopPreview() // eski preview to‘xtatiladi
            openGlView = view
            rtmpUSB?.replaceView(view, uvcCamera)
            startPreview()
        }
    }

    fun clearView() {
        openGlView = null
        rtmpUSB?.replaceView(applicationContext, uvcCamera)
        stopPreview()
    }

    // ---- RTMP callbacks ----
    private val connectCheckerRtmp = object : ConnectCheckerRtmp {
        override fun onConnectionSuccessRtmp() {
            showNotification("Stream connected")
            _isStreamingFlow.value = true
        }

        override fun onConnectionFailedRtmp(reason: String) {
            showNotification("Failed: $reason")
            stopStream(true)
            _isStreamingFlow.value = false
        }

        override fun onConnectionStartedRtmp(rtmpUrl: String) = showNotification("Connecting…")
        override fun onNewBitrateRtmp(bitrate: Long) {}
        override fun onDisconnectRtmp() {
            showNotification("Disconnected")
            stopStream(true)
            _isStreamingFlow.value = false
        }

        override fun onAuthErrorRtmp() {
            showNotification("Auth error")
            stopStream(true)
            _isStreamingFlow.value = false
        }

        override fun onAuthSuccessRtmp() = showNotification("Auth success")
    }

    private fun cleanupResources() {
        previewHandler?.removeCallbacksAndMessages(null)
        streamHandler?.removeCallbacksAndMessages(null)
        previewHandler = null
        streamHandler = null

        stopStream(true)
        stopPreview()
        cleanupCamera()

        runCatching { usbMonitor?.unregister() }
        usbMonitor = null

        rtmpUSB = null
        openGlView = null
        endpoint = null

        notificationManager.cancel(NOTIFY_ID)
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
    }
}
