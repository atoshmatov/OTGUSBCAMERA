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
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera1
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

class StreamService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var rtmpUSB: RtmpUSB? = null
    private var rtmpCamera1: RtmpCamera1? = null
    private var uvcCamera: UVCCamera? = null
    private var usbMonitor: USBMonitor? = null
    private var endpoint: String? = null
    private var isServiceDestroying = false
    private var useUSB = false

    var cameraWidth = 1280
    var cameraHeight = 720

    private val _isStreamingFlow = MutableStateFlow(false)
    val isStreamingFlow = _isStreamingFlow.asStateFlow()

    val isStreaming: Boolean
        get() = (if (useUSB) rtmpUSB?.isStreaming else rtmpCamera1?.isStreaming) == true && !isServiceDestroying

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
        initializeUSBMonitor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Ilova yopilganda ham backgroundda ishlashni davom ettirish uchun 
        // stopSelf() chaqirilmaydi.
        // Ayrim qurilmalarda bildirishnomani yangilash xizmatni tirik saqlashga yordam beradi
        showNotification(if (isStreaming) "Stream davom etmoqda..." else "Service backgroundda ishlayapti")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        isServiceDestroying = true
        cleanupResources()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "RTP Stream",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_video_replay)
            .setContentTitle("RTP Stream Service")
            .setContentText("Service is running in background")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)

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
        serviceScope.launch {
            delay(2000)
            if (uvcCamera == null && !useUSB) {
                Log.d(TAG, "USB kamera topilmadi, telefon kamerasidan foydalaniladi")
                prepareNormalCamera()
            }
        }
    }

    private fun prepareNormalCamera() {
        useUSB = false
        if (rtmpCamera1 == null) {
            rtmpCamera1 = if (openGlView == null) {
                RtmpCamera1(this, connectCheckerRtmp)
            } else {
                RtmpCamera1(openGlView, connectCheckerRtmp)
            }
        }
        cameraWidth = 1280
        cameraHeight = 720
        startPreview()
    }

    private val onDeviceConnectListener = object : USBMonitor.OnDeviceConnectListener {
        override fun onConnect(
            device: UsbDevice?,
            ctrlBlock: USBMonitor.UsbControlBlock?,
            createNew: Boolean
        ) {
            cleanupCamera()
            uvcCamera = UVCCamera().apply {
                runCatching {
                    open(ctrlBlock)
                    setPreviewSize(cameraWidth, cameraHeight, UVCCamera.FRAME_FORMAT_MJPEG)
                }.onFailure {
                    Log.e(TAG, "USB kamerani ochib bo'lmadi: ${it.message}")
                    uvcCamera = null
                    return
                }
            }
            useUSB = true
            rtmpCamera1?.stopPreview()
            rtmpCamera1 = null
            startPreview()
        }

        override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            cleanupCamera()
            serviceScope.launch {
                delay(500)
                prepareNormalCamera()
            }
        }

        override fun onAttach(device: UsbDevice?) {
            usbMonitor?.requestPermission(device)
        }

        override fun onCancel(device: UsbDevice?) {}
        override fun onDettach(device: UsbDevice?) {
            cleanupCamera()
        }
    }

    private fun cleanupCamera() {
        try {
            uvcCamera?.apply {
                stopPreview()
                destroy()
            }
        } catch (_: Exception) {}
        uvcCamera = null
    }

    private fun prepareRtmpUSB() {
        if (rtmpUSB == null) {
            rtmpUSB = if (openGlView == null) RtmpUSB(this, connectCheckerRtmp)
            else RtmpUSB(openGlView!!, connectCheckerRtmp)
        }
    }

    fun startStreamRtp(endpoint: String): Boolean {
        if (isStreaming) {
            Log.w(TAG, "Stream allaqachon boshlangan")
            return true
        }
        this.endpoint = endpoint
        return try {
            if (useUSB) {
                prepareRtmpUSB()
                rtmpUSB?.let { r ->
                    val ok = r.prepareVideo(cameraWidth, cameraHeight, 30, 4000 * 1024, 0, uvcCamera) && r.prepareAudio()
                    if (ok) {
                        r.startStream(uvcCamera, endpoint)
                        serviceScope.launch { _isStreamingFlow.emit(true) }
                        showNotification("USB Stream boshlandi")
                        true
                    } else false
                } ?: false
            } else {
                rtmpCamera1?.let { r ->
                    val ok = r.prepareVideo(cameraWidth, cameraHeight, 30, 4000 * 1024, 2, 0) && r.prepareAudio()
                    if (ok) {
                        r.startStream(endpoint)
                        serviceScope.launch { _isStreamingFlow.emit(true) }
                        showNotification("Kamera Stream boshlandi")
                        true
                    } else false
                } ?: false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Streamni boshlashda xatolik: ${e.message}")
            serviceScope.launch { _isStreamingFlow.emit(false) }
            false
        }
    }

    fun stopStream(force: Boolean = false) {
        if (force) endpoint = null
        try {
            if (useUSB) rtmpUSB?.stopStream(uvcCamera)
            else rtmpCamera1?.stopStream()
        } catch (_: Exception) {}
        serviceScope.launch { _isStreamingFlow.emit(false) }
        if (!isServiceDestroying) showNotification("Stream to'xtatildi")
    }

    fun startPreview() {
        if (useUSB) {
            uvcCamera?.let { cam ->
                runCatching { rtmpUSB?.startPreview(cam, cameraWidth, cameraHeight) }
            }
        } else {
            runCatching { rtmpCamera1?.startPreview() }
        }
    }

    fun stopPreview() {
        if (useUSB) runCatching { rtmpUSB?.stopPreview(uvcCamera) }
        else runCatching { rtmpCamera1?.stopPreview() }
    }

    fun setView(view: OpenGlView) {
        if (openGlView != view) {
            openGlView = view
            if (useUSB) {
                rtmpUSB?.replaceView(view, uvcCamera)
            } else {
                rtmpCamera1?.replaceView(view) ?: run {
                    rtmpCamera1 = RtmpCamera1(view, connectCheckerRtmp)
                }
            }
        }
        startPreview()
    }

    fun clearView() {
        if (openGlView == null) return
        openGlView = null
        if (useUSB) {
            rtmpUSB?.replaceView(applicationContext, uvcCamera)
        } else {
            rtmpCamera1?.replaceView(applicationContext) ?: run {
                rtmpCamera1 = RtmpCamera1(applicationContext, connectCheckerRtmp)
            }
        }
    }

    private val connectCheckerRtmp = object : ConnectCheckerRtmp {
        override fun onConnectionSuccessRtmp() {
            showNotification("Stream ulangan")
            serviceScope.launch { _isStreamingFlow.emit(true) }
        }

        override fun onConnectionFailedRtmp(reason: String) {
            showNotification("Xatolik: $reason")
            stopStream(true)
            serviceScope.launch { _isStreamingFlow.emit(false) }
        }

        override fun onConnectionStartedRtmp(rtmpUrl: String) = showNotification("Ulanmoqda…")
        override fun onNewBitrateRtmp(bitrate: Long) {}
        override fun onDisconnectRtmp() {
            showNotification("Ulanish uzildi")
            stopStream(true)
            serviceScope.launch { _isStreamingFlow.emit(false) }
        }

        override fun onAuthErrorRtmp() {
            showNotification("Auth xatosi")
            stopStream(true)
            serviceScope.launch { _isStreamingFlow.emit(false) }
        }

        override fun onAuthSuccessRtmp() = showNotification("Auth muvaffaqiyatli")
    }

    private fun cleanupResources() {
        stopStream(true)
        stopPreview()
        cleanupCamera()
        runCatching { usbMonitor?.unregister() }
        usbMonitor = null
        rtmpUSB = null
        rtmpCamera1 = null
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
