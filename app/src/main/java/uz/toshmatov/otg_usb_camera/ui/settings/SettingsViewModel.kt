package uz.toshmatov.otg_usb_camera.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-level sozlamalar — stream sozlamalari emas.
 * Stream sozlamalari MainViewModel + StreamSettings da saqlanadi.
 */
data class AppSettings(
    val isDarkTheme: Boolean = true
)

class SettingsViewModel : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun toggleTheme() {
        _settings.value = _settings.value.copy(isDarkTheme = !_settings.value.isDarkTheme)
    }
}
