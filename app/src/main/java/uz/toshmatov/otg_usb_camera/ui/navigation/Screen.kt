package uz.toshmatov.otg_usb_camera.ui.navigation

sealed class Screen(val route: String) {
    data object Splash         : Screen("splash")
    data object CameraSelect   : Screen("camera_select")
    data object LivePreview    : Screen("live_preview")
    data object StreamSettings : Screen("stream_settings")
    data object StreamPlayer   : Screen("stream_player")
    data object Settings       : Screen("settings")
}
