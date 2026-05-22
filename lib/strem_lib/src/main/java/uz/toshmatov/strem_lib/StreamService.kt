package uz.toshmatov.strem_lib

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.usb.UsbDevice
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera2
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

    // OpenGlView stored as instance field — avoids Context leak from static companion object
    private var openGlView: OpenGlView? = null

    // Phone camera streaming via RtmpCamera2
    private var rtmpCamera2: RtmpCamera2? = null
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
                rtmpCamera2?.isStreaming == true && !isServiceDestroying
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

        val notification = builder.build()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFY_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(NOTIFY_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed: ${e.message}")
            stopSelf()
        }
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
                delay(300)
                startPreview() // phone camera preview'ga qaytish
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
                delay(300)
                startPreview() // phone camera preview'ga qaytish
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
        // Phone camera renderer ni to'xtatish — ikki renderer bir OpenGlView'da bo'lmasin
        cleanupPhoneCamera()
        rtmpUSB = if (openGlView == null) {
            RtmpUSB(this, connectCheckerRtmp)
        } else {
            RtmpUSB(openGlView!!, connectCheckerRtmp)
        }
    }

    private fun cleanupPhoneCamera() {
        val cam2 = rtmpCamera2 ?: run {
            isUsingPhoneCamera = false
            return
        }
        rtmpCamera2 = null
        isUsingPhoneCamera = false
        // stopPreview first — GL thread stops.
        // Sleep 100 ms — GL thread fully exits before Camera2 session closes.
        // Reverse order (stopStream → stopPreview) would close the Camera2 session while
        // GL thread still calls updateTexImage() → EGL_BAD_ACCESS → native abort.
        runCatching { if (cam2.isOnPreview) cam2.stopPreview() }
        try { Thread.sleep(100) } catch (_: InterruptedException) {}
        runCatching { if (cam2.isStreaming) cam2.stopStream() }
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
            Log.d(TAG, "Starting phone camera stream to $endpoint")

            val view = openGlView ?: run {
                Log.e(TAG, "Cannot start phone camera stream: OpenGlView not attached")
                return false
            }

            // Always tear down any existing instance before creating a new one.
            // Reusing an old instance means prepareVideo() calls stopPreview() internally,
            // which races with the still-running GL thread → EGL_BAD_ACCESS → native abort.
            val oldCam = rtmpCamera2
            if (oldCam != null) {
                rtmpCamera2 = null
                isUsingPhoneCamera = false
                runCatching { if (oldCam.isOnPreview) oldCam.stopPreview() }
                Thread.sleep(150) // wait for GL thread to fully stop (called on IO thread)
                runCatching { if (oldCam.isStreaming) oldCam.stopStream() }
                Thread.sleep(50)
            }

            // Fresh instance — no lingering GL thread from a previous session
            val camera2 = RtmpCamera2(view, phoneCameraConnectChecker).also {
                rtmpCamera2 = it
            }

            val videoReady = camera2.prepareVideo(
                /* width           */ 1280,
                /* height          */ 720,
                /* fps             */ 30,
                /* bitrate         */ 2_500_000,
                /* iFrameInterval  */ 2,
                /* rotation        */ 0
            )
            val audioReady = camera2.prepareAudio(
                /* bitrate         */ 128_000,
                /* sampleRate      */ 44_100,
                /* isStereo        */ true,
                /* echoCanceler    */ false,
                /* noiseSuppressor */ false
            )

            if (!videoReady || !audioReady) {
                Log.e(TAG, "RtmpCamera2 prepare failed: video=$videoReady audio=$audioReady")
                rtmpCamera2 = null
                return false
            }

            try {
                camera2.startPreview(CameraHelper.Facing.BACK)
            } catch (e: Exception) {
                Log.w(TAG, "BACK camera failed, trying FRONT: ${e.message}")
                try {
                    camera2.startPreview(CameraHelper.Facing.FRONT)
                } catch (e2: Exception) {
                    Log.e(TAG, "Both cameras failed: ${e2.message}")
                    rtmpCamera2 = null
                    return false
                }
            }

            camera2.startStream(endpoint)
            isUsingPhoneCamera = true
            showNotification("Phone Camera Streaming")
            Log.d(TAG, "Phone camera stream started")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting phone camera stream: ${e.message}")
            rtmpCamera2 = null
            isUsingPhoneCamera = false
            serviceScope.launch { _isStreamingFlow.emit(false) }
            false
        }
    }

    private val phoneCameraConnectChecker = object : ConnectCheckerRtmp {
        override fun onConnectionSuccessRtmp() {
            showNotification("Phone camera connected")
            serviceScope.launch { _isStreamingFlow.emit(true) }
        }

        override fun onConnectionFailedRtmp(reason: String) {
            Log.e(TAG, "Phone camera connection failed: $reason")
            showNotification("Phone camera failed: $reason")
            serviceScope.launch {
                _isStreamingFlow.emit(false)
                _cameraStateFlow.emit(CameraState.Phone)
            }
            rtmpCamera2?.stopStream()
            rtmpCamera2 = null
            isUsingPhoneCamera = false
        }

        override fun onConnectionStartedRtmp(rtmpUrl: String) = showNotification("Phone camera connecting...")
        override fun onNewBitrateRtmp(bitrate: Long) {}

        override fun onDisconnectRtmp() {
            showNotification("Phone camera disconnected")
            serviceScope.launch { _isStreamingFlow.emit(false) }
            rtmpCamera2 = null
            isUsingPhoneCamera = false
        }

        override fun onAuthErrorRtmp() {
            showNotification("Phone camera auth error")
            serviceScope.launch { _isStreamingFlow.emit(false) }
            rtmpCamera2?.stopStream()
            rtmpCamera2 = null
            isUsingPhoneCamera = false
        }

        override fun onAuthSuccessRtmp() = showNotification("Phone camera auth success")
    }

    fun stopStream(force: Boolean = false) {
        if (force) endpoint = null
        try {
            if (isUsingPhoneCamera) {
                rtmpCamera2?.let { cam ->
                    if (force) {
                        // Force stop: GL thread avval to'xtatiladi, keyin Camera2 sessiya
                        runCatching { if (cam.isOnPreview) cam.stopPreview() }
                        runCatching { if (cam.isStreaming) cam.stopStream() }
                        rtmpCamera2 = null
                    } else {
                        // Normal stop: faqat stream, preview davom etadi
                        runCatching { if (cam.isStreaming) cam.stopStream() }
                    }
                }
                isUsingPhoneCamera = false
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
        if (uvcCamera != null) {
            // USB camera preview — unchanged
            uvcCamera?.let { cam ->
                runCatching { rtmpUSB?.startPreview(cam, cameraWidth, cameraHeight) }
            }
        } else {
            // Phone camera: DO NOT start preview here.
            // Starting preview creates GL thread 1; prepareVideo() inside startStream
            // triggers stopPreview() internally → GL thread 2 starts before thread 1 stops
            // → two GL threads on a single OpenGlView → EGL_BAD_ACCESS → native abort.
            // Camera is opened only when streaming begins (startPhoneCameraStream).
            // Just emit Phone state so the UI can update accordingly.
            serviceScope.launch { _cameraStateFlow.emit(CameraState.Phone) }
        }
    }

    fun stopPreview() {
        runCatching { rtmpUSB?.stopPreview(uvcCamera) }
        // Phone camera preview ni ham to'xtatish (faqat to'liq cleanup vaqtida)
        // Oddiy stopStream() da bu chaqirilmaydi — preview davom etishi kerak
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
            openGlView = view
            if (uvcCamera != null) {
                // USB camera ulangan: RtmpUSB ni qayta yarat.
                // replaceView() ishlatilmaydi — u preview/stream bo'lmasa init() chaqirmaydi,
                // startPreview init bo'lmagan view da ishlamaydi.
                runCatching { rtmpUSB?.stopPreview(uvcCamera) }
                rtmpUSB = RtmpUSB(view, connectCheckerRtmp)
                runCatching { rtmpUSB!!.startPreview(uvcCamera!!, cameraWidth, cameraHeight) }
            }
            // Phone camera: startPreview() yoki LaunchedEffect tomonidan hal qilinadi
        }
    }

    fun clearView() {
        openGlView = null

        // USB camera: offscreen rejimga o'tish — streaming to'xtatilmaydi.
        // Camera2Base (RtmpCamera2) uchun replaceView yo'q, shuning uchun phone camera
        // streaming background'da to'xtatilishi kerak (GL crash + audio native abort oldini olish).
        if (uvcCamera != null) {
            // USB: OffScreenGlThread ga o'tish → stream background'da davom etadi
            runCatching { rtmpUSB?.replaceView(applicationContext, uvcCamera) }
            if (rtmpUSB?.isStreaming != true) {
                runCatching { rtmpUSB?.stopPreview(uvcCamera) }
            }
        } else {
            // Phone camera: Camera2Base replaceView yo'q → surface yo'q bo'lsa GL crash + audio abort.
            // Surface yo'qoldi → streamni to'xtatish xavfsizroq.
            cleanupPhoneCamera()
            Log.d(TAG, "clearView: phone camera stopped (surface destroyed)")
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
        cleanupPhoneCamera()

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
    }
}
