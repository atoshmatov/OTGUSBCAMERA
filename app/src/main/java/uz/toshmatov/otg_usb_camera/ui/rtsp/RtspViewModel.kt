package uz.toshmatov.otg_usb_camera.ui.rtsp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RtspViewModel : ViewModel() {

    private val _rtspUrl = MutableStateFlow("rtsp://example.com/stream")
    val rtspUrl: StateFlow<String> = _rtspUrl.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun updateRtspUrl(url: String) {
        _rtspUrl.value = url
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }
}
