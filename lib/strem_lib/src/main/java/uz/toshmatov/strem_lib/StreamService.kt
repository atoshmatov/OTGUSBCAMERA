package uz.toshmatov.strem_lib

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.view.OpenGlView
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CameraState {
    Disconnected, USB, Phone
}

class StreamService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var rtmpUSB: RtmpUSB? = null
    private var uvcCamera: UVCCamera? = null
    private var usbMonitor: USBMonitor? = null
    private var endpoint: String? = null
    private var isServiceDestroying = false

    // Phone camera streaming
    private var phoneCameraStreamer: Any? = null // PhoneCameraStreamer type
    private var isUsingPhoneCamera = false

    var cameraWidth = 1280
    var cameraHeight = 960

    private val _isStreamingFlow = MutableStateFlow(false)
    val isStreamingFlow = _isStreamingFlow.asStateFlow()

    private val _cameraStateFlow = MutableStateFlow<CameraState>(CameraState.Disconnected)
    val cameraStateFlow = _cameraStateFlow.asStateFlow()

    val isStreaming: Boolean
        get() {
            return if (isUsingPhoneCamera) {
                _isStreamingFlow.value
            } else {
                rtmpUSB?.isStreaming == true && !isServiceDestroying
            }
        }

    private val notificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): StreamService = this@StreamService
        fun streamingFlow() = isStreamingFlow
        fun cameraStateFlow() = cameraStateFlow
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceNotification()
        initializeUSBMonitor()

        val savedEndpoint = getSharedPreferences("stream_prefs", MODE_PRIVATE)
            .getString("endpoint", null)
        if (savedEndpoint != null) {
            endpoint = savedEndpoint
            serviceScope.launch {
                delay(1000)
                startStreamRtp(savedEndpoint)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("endpoint")
        if (url != null) {
            endpoint = url
            getSharedPreferences("stream_prefs", MODE_PRIVATE).edit {
                putString("endpoint", url)
            }

            serviceScope.launch {
                delay(500)
                startStreamRtp(url)
            }
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {}

    override fun onDestroy() {
        isServiceDestroying = true
        cleanupResources()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "RTP Stream",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "RTP Streaming Service"
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForegroundServiceNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_video_replay)
            .setContentTitle("RTP Stream")
            .setContentText("Streaming service running…")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }
        startForeground(NOTIFY_ID, builder.build())
    }

    private fun showNotification(text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_video_replay)
            .setContentTitle("RTP Stream")
            .setContentText(text)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(NOTIFY_ID, notification)
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
                    camera.setPreviewSize(cameraWidth, cameraHeight, UVCCamera.FRAME_FORMAT_MJPEG)
                }.onFailure {
                    camera.setPreviewSize(cameraWidth, cameraHeight, UVCCamera.DEFAULT_PREVIEW_MODE)
                }

                uvcCamera = camera
                serviceScope.launch { _cameraStateFlow.emit(CameraState.USB) }

                // Coroutine bilan ishlash
                serviceScope.launch {
                    delay(300)
                    prepareRtmpUSB()
                    runCatching { rtmpUSB?.startPreview(camera, cameraWidth, cameraHeight) }
                    endpoint?.let { url ->
                        delay(350)
                        startStreamRtp(url)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Camera connect error: ${e.message}")
                camera.destroy()
            }
        }

        override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            Log.w(TAG, "USB onDisconnect")
            stopStream(false)
            cleanupCamera()
            serviceScope.launch {
                _isStreamingFlow.emit(false)
                _cameraStateFlow.emit(CameraState.Phone)
            }
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
            stopStream(false)
            cleanupCamera()
            serviceScope.launch {
                _isStreamingFlow.emit(false)
                _cameraStateFlow.emit(CameraState.Phone)
            }
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
        this.endpoint = endpoint

        // Try USB camera first, fall back to phone camera
        return if (uvcCamera != null) {
            startUSBStream(endpoint)
        } else {
            startPhoneCameraStream(endpoint)
        }
    }

    private fun startUSBStream(endpoint: String): Boolean {
        if (uvcCamera == null) {
            Log.e(TAG, "Camera not ready for streaming")
            return false
        }

        return try {
            prepareRtmpUSB()
            rtmpUSB?.let { r ->
                val ok = r.prepareVideo(
                    cameraWidth, cameraHeight, 30,
                    4000 * 1024, 0, uvcCamera
                ) && r.prepareAudio()
                if (ok) {
                    r.startStream(uvcCamera, endpoint)
                    isUsingPhoneCamera = false
                    serviceScope.launch { _isStreamingFlow.emit(true) }
                    showNotification("USB Camera Streaming")
                    true
                } else false
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error starting USB stream: ${e.message}")
            serviceScope.launch { _isStreamingFlow.emit(false) }
            false
        }
    }

    private fun startPhoneCameraStream(endpoint: String): Boolean {
        return try {
            Log.d(TAG, "Starting phone camera stream")
            isUsingPhoneCamera = true
            serviceScope.launch { _isStreamingFlow.emit(true) }
            showNotification("Phone Camera Streaming")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting phone camera stream: ${e.message}")
            isUsingPhoneCamera = false
            serviceScope.launch { _isStreamingFlow.emit(false) }
            false
        }
    }

    fun stopStream(force: Boolean = false) {
        if (force) endpoint = null
        try {
            if (isUsingPhoneCamera) {
                // Phone camera streaming cleanup
                isUsingPhoneCamera = false
                Log.d(TAG, "Phone camera stream stopped")
            } else {
                rtmpUSB?.stopStream(uvcCamera)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping stream: ${e.message}")
        }
        serviceScope.launch { _isStreamingFlow.emit(false) }
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
            stopPreview()
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

    fun notifyPhoneCameraReady() {
        serviceScope.launch {
            _cameraStateFlow.emit(CameraState.Phone)
        }
    }

    private val connectCheckerRtmp = object : ConnectCheckerRtmp {
        override fun onConnectionSuccessRtmp() {
            showNotification("Stream connected")
            serviceScope.launch { _isStreamingFlow.emit(true) }
        }

        override fun onConnectionFailedRtmp(reason: String) {
            showNotification("Failed: $reason")
            stopStream(true)
            serviceScope.launch { _isStreamingFlow.emit(false) }
        }

        override fun onConnectionStartedRtmp(rtmpUrl: String) = showNotification("Connecting…")
        override fun onNewBitrateRtmp(bitrate: Long) {}
        override fun onDisconnectRtmp() {
            showNotification("Disconnected")
            stopStream(true)
            serviceScope.launch { _isStreamingFlow.emit(false) }
        }

        override fun onAuthErrorRtmp() {
            showNotification("Auth error")
            stopStream(true)
            serviceScope.launch { _isStreamingFlow.emit(false) }
        }

        override fun onAuthSuccessRtmp() = showNotification("Auth success")
    }

    private fun cleanupResources() {
        stopStream(true)
        stopPreview()
        cleanupCamera()

        isUsingPhoneCamera = false
        phoneCameraStreamer = null

        runCatching { usbMonitor?.unregister() }
        usbMonitor = null

        rtmpUSB = null
        openGlView = null
        endpoint = null

        notificationManager.cancel(NOTIFY_ID)
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
    }

    companion object {
        private const val TAG = "StreamService -->"
        private const val CHANNEL_ID = "rtpStreamChannel"
        private const val NOTIFY_ID = 123456
        var openGlView: OpenGlView? = null
    }
}
