package uz.toshmatov.otg_usb_camera.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uz.toshmatov.otg_usb_camera.R
import uz.toshmatov.otg_usb_camera.livedata.SingleLiveEvent
import uz.toshmatov.strem_lib.StreamService

class MainViewModel : ViewModel() {

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