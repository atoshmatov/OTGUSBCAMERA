package uz.toshmatov.otg_usb_camera.ui.rtsp

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PlayerState {
    data object Idle : PlayerState()
    data object Buffering : PlayerState()
    data object Playing : PlayerState()
    data object Paused : PlayerState()
    data class Error(val message: String) : PlayerState()
}

class RtspViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("rtsp_prefs", Context.MODE_PRIVATE)

    private val _rtspUrl = MutableStateFlow(prefs.getString("rtsp_url", "") ?: "")
    val rtspUrl: StateFlow<String> = _rtspUrl.asStateFlow()

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    fun updateRtspUrl(url: String) {
        _rtspUrl.value = url
        prefs.edit { putString("rtsp_url", url) }
    }

    fun updatePlayerState(state: PlayerState) {
        _playerState.value = state
    }
}
