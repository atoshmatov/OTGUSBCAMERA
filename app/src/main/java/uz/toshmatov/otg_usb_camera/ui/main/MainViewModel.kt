package uz.toshmatov.otg_usb_camera.ui.main

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.toshmatov.strem_lib.CameraState
import uz.toshmatov.strem_lib.StreamService

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
}
