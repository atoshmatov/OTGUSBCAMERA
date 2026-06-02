package uz.toshmatov.otg_usb_camera.ui.select.util

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Videocam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.toshmatov.otg_usb_camera.ui.select.model.CameraDevice

/**
 * Qurilmadagi kameralarni aniqlaydi.
 * USB: faqat UVC (Video Interface Class = 0x0E) qurilmalar.
 * Phone: Camera2 API orqali barcha ichki kameralar.
 * IO thread da ishlatish kerak.
 */
suspend fun detectCameras(context: Context): List<CameraDevice> = withContext(Dispatchers.IO) {
    val result = mutableListOf<CameraDevice>()

    // USB kameralar — faqat USB Video Class (UVC) qurilmalar
    val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
    val usbDevices = usbManager?.deviceList?.values ?: emptyList()
    usbDevices
        .filter { device ->
            // Qurilmaning kamida bitta interfeysi USB Video Class bo'lishi kerak
            (0 until device.interfaceCount).any { i ->
                device.getInterface(i).interfaceClass == UsbConstants.USB_CLASS_VIDEO
            }
        }
        .forEach { device ->
            result.add(
                CameraDevice(
                    name = device.productName ?: "USB Camera ${device.deviceId}",
                    sub = "VID:${"%04X".format(device.vendorId)} · PID:${
                        "%04X".format(device.productId)
                    } · UVC",
                    isUsbConnected = true,
                    icon = Icons.Default.Usb,
                    facing = null
                )
            )
        }

    // Phone kameralari — Camera2 API orqali
    try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        cameraManager?.cameraIdList?.forEach { id ->
            val chars = cameraManager.getCameraCharacteristics(id)
            val facing = chars.get(CameraCharacteristics.LENS_FACING)
            val (label, detail) = when (facing) {
                CameraCharacteristics.LENS_FACING_BACK -> "Phone Back Camera" to "Internal · Main"
                CameraCharacteristics.LENS_FACING_FRONT -> "Phone Front Camera" to "Internal · Selfie"
                else -> "Camera $id" to "Internal"
            }
            result.add(
                CameraDevice(
                    name = label,
                    sub = detail,
                    isUsbConnected = false,
                    icon = if (facing == CameraCharacteristics.LENS_FACING_FRONT)
                        Icons.Default.PhoneAndroid else Icons.Default.Videocam,
                    facing = facing
                )
            )
        }
    } catch (_: Exception) {
    }

    result
}