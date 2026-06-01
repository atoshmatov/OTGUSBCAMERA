package uz.toshmatov.otg_usb_camera.ui.select.model

import androidx.compose.ui.graphics.vector.ImageVector

data class CameraDevice(
    val name: String,
    val sub: String,
    val isUsbConnected: Boolean,
    val icon: ImageVector,
    /** CameraCharacteristics.LENS_FACING_BACK / FRONT — phone kameralarda, USB da null */
    val facing: Int? = null
)
