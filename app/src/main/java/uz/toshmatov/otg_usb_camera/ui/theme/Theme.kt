package uz.toshmatov.otg_usb_camera.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OtgColorScheme = darkColorScheme(
    primary              = MdPrimary,
    onPrimary            = MdOnPrimary,
    primaryContainer     = MdPrimaryContainer,
    onPrimaryContainer   = MdOnPrimaryContainer,
    background           = MdBackground,
    surface              = MdSurface,
    surfaceVariant       = MdSurfaceVariant,
    onBackground         = MdOnBackground,
    onSurface            = MdOnSurface,
    onSurfaceVariant     = MdOnSurfaceVariant,
    outline              = MdOutline,
    error                = MdError,
)

@Composable
fun OTGUSBCAMERATheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = OtgColorScheme,
        typography = Typography,
        content = content
    )
}
