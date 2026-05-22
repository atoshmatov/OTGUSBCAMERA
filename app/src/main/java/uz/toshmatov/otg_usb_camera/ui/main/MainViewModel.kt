package uz.toshmatov.otg_usb_camera.ui.main

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.toshmatov.strem_lib.CameraState
import uz.toshmatov.strem_lib.StreamService

class MainViewModel : ViewModel() {

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _rtmpUrl = MutableStateFlow("rtmp://84.54.117.248:10005/live/stream1")
    val rtmpUrl: StateFlow<String> = _rtmpUrl.asStateFlow()

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
            streamService.isStreamingFlow.collect { state ->
                _isStreaming.value = state
            }
        }

        cameraStateCollectJob = viewModelScope.launch(Dispatchers.IO) {
            streamService.cameraStateFlow.collect { state ->
                _cameraState.value = state
            }
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

    fun updateRtmpUrl(url: String) {
        _rtmpUrl.value = url
    }

    fun getRtmpEndpoint(): String = _rtmpUrl.value

    fun onStreamControlButtonClick(endpoint: String) {
        val s = service ?: return
        if (_isStreaming.value) {
            // Run on IO — stopStream → cleanupPhoneCamera uses Thread.sleep
            viewModelScope.launch(Dispatchers.IO) {
                s.stopStream(true)
            }
        } else {
            // Run on IO — startPhoneCameraStream uses Thread.sleep for GL thread teardown
            viewModelScope.launch(Dispatchers.IO) {
                s.startStreamRtp(endpoint)
            }
        }
    }
}
