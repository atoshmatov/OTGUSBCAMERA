package uz.toshmatov.otg_usb_camera.ui

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.toshmatov.strem_lib.StreamService

/*class MainViewModel : ViewModel() {

    private val serviceLiveData = SingleLiveEvent<(StreamService) -> Unit>()
    val serviceLiveEvent: LiveData<(StreamService) -> Unit> get() = serviceLiveData

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming

    fun onStreamControlButtonClick() {
        withService {
            if (it.isStreaming) {
                it.stopStream(true)
                _isStreaming.value = false
            } else {
                val endpoint = "rtmp://84.54.117.248:10005/live/stream1"

                if (endpoint.isBlank()) {
                    return@withService
                }

                it.startStreamRtp(endpoint)
                _isStreaming.value = true
            }
        }
    }

    private fun withService(block: (StreamService) -> Unit) {
        serviceLiveData.value = block
    }
}

// MainViewModel.kt
package your.package*/

class MainViewModel : ViewModel() {

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    @SuppressLint("StaticFieldLeak")
    private var service: StreamService? = null
    private var streamCollectJob: Job? = null

    fun attachService(streamService: StreamService) {
        service = streamService
        streamCollectJob?.cancel()
        streamCollectJob = viewModelScope.launch(Dispatchers.IO) {
            streamService.isStreamingFlow.collect { state ->
                _isStreaming.value = state
            }
        }
    }

    fun detachService() {
        streamCollectJob?.cancel()
        streamCollectJob = null
        service = null
        _isStreaming.value = false
    }

    fun updateStreamingState(isStreaming: Boolean) {
        _isStreaming.value = isStreaming
    }


    fun onStreamControlButtonClick(endpoint: String) {
        val s = service ?: return
        if (_isStreaming.value) {
            s.stopStream(true)
        } else {
            s.startStreamRtp(endpoint)
        }
    }
}
