package uz.toshmatov.otg_usb_camera.ui.main

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.toshmatov.strem_lib.CameraState
import uz.toshmatov.strem_lib.StreamService

/** Snapshot natijasi — UI uchun */
sealed class SnapshotState {
    data object Idle : SnapshotState()
    data object Saving : SnapshotState()
    data object Success : SnapshotState()
    data object Error : SnapshotState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(StreamSettings.PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(StreamSettings.fromPrefs(prefs))
    val settings: StateFlow<StreamSettings> = _settings.asStateFlow()

    val rtmpUrl: StateFlow<String> = _settings
        .map { it.rtmpUrl }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _settings.value.rtmpUrl)

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Disconnected)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _snapshotState = MutableStateFlow<SnapshotState>(SnapshotState.Idle)
    val snapshotState: StateFlow<SnapshotState> = _snapshotState.asStateFlow()

    @SuppressLint("StaticFieldLeak")
    private var service: StreamService? = null
    private var streamCollectJob: Job? = null
    private var cameraStateCollectJob: Job? = null

    fun attachService(streamService: StreamService) {
        service = streamService
        streamCollectJob?.cancel()
        cameraStateCollectJob?.cancel()
        streamCollectJob = viewModelScope.launch(Dispatchers.IO) {
            streamService.isStreamingFlow.collect { _isStreaming.value = it }
        }
        cameraStateCollectJob = viewModelScope.launch(Dispatchers.IO) {
            streamService.cameraStateFlow.collect { _cameraState.value = it }
        }
    }

    fun detachService() {
        streamCollectJob?.cancel()
        cameraStateCollectJob?.cancel()
        streamCollectJob = null
        cameraStateCollectJob = null
        service = null
        _isStreaming.value = false
    }

    fun updateStreamingState(isStreaming: Boolean) {
        _isStreaming.value = isStreaming
    }

    fun updateSettings(settings: StreamSettings) {
        _settings.value = settings
        StreamSettings.saveToPrefs(settings, prefs)
    }

    fun updateRtmpUrl(url: String) {
        val updated = _settings.value.copy(rtmpUrl = url)
        _settings.value = updated
        StreamSettings.saveToPrefs(updated, prefs)
    }

    fun getRtmpEndpoint(): String = _settings.value.rtmpUrl

    fun onStreamControlButtonClick(endpoint: String) {
        val s = service ?: return
        if (_isStreaming.value) {
            viewModelScope.launch(Dispatchers.IO) { s.stopStream(true) }
        } else {
            val config = _settings.value.toStreamConfig()
            viewModelScope.launch(Dispatchers.IO) { s.startStreamRtp(endpoint, config) }
        }
    }

    // ── Snapshot ─────────────────────────────────────────────────────

    /**
     * Joriy kamera kadrini JPEG sifatida galereya ga saqlaydi.
     * Pictures/OTGCamera/ papkasiga yoziladi.
     */
    fun takeSnapshot(context: Context) {
        val s = service ?: run {
            _snapshotState.value = SnapshotState.Error
            resetSnapshotStateAfterDelay()
            return
        }
        _snapshotState.value = SnapshotState.Saving
        s.takeSnapshot { bitmap ->
            if (bitmap != null) {
                val saved = saveBitmapToGallery(context, bitmap)
                _snapshotState.value = if (saved) SnapshotState.Success else SnapshotState.Error
            } else {
                _snapshotState.value = SnapshotState.Error
            }
            resetSnapshotStateAfterDelay()
        }
    }

    private fun resetSnapshotStateAfterDelay() {
        viewModelScope.launch {
            delay(2000)
            _snapshotState.value = SnapshotState.Idle
        }
    }

    /**
     * Bitmap → Pictures/OTGCamera/OTGCamera_<timestamp>.jpg
     * MediaStore API — ruxsat talab qilmaydi (API 29+).
     * API 26-28 uchun WRITE_EXTERNAL_STORAGE kerak (manifest da bor).
     */
    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val filename = "OTGCamera_${System.currentTimeMillis()}.jpg"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/OTGCamera")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            ) ?: return false

            context.contentResolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }
            true
        } catch (e: Exception) {
            Log.e("MainViewModel", "saveBitmapToGallery failed: ${e.message}")
            false
        }
    }
}
