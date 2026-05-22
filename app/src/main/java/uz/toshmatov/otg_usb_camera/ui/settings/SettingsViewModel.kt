package uz.toshmatov.otg_usb_camera.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class StreamQuality(val label: String, val width: Int, val height: Int) {
    HD("720p", 1280, 720),
    FULL_HD("1080p", 1920, 1080),
    SD("480p", 854, 480)
}

data class AppSettings(
    val isDarkTheme: Boolean = true,
    val defaultRtmpUrl: String = "rtmp://84.54.117.248:10005/live/stream1",
    val streamQuality: StreamQuality = StreamQuality.HD
)

class SettingsViewModel : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkThemeFlow: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        _settings.value = _settings.value.copy(isDarkTheme = _isDarkTheme.value)
    }

    fun updateRtmpUrl(url: String) {
        _settings.value = _settings.value.copy(defaultRtmpUrl = url)
    }

    fun updateQuality(quality: StreamQuality) {
        _settings.value = _settings.value.copy(streamQuality = quality)
    }
}
