package uz.toshmatov.otg_usb_camera.ui.navigation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import uz.toshmatov.otg_usb_camera.ui.main.LivePreviewScreen
import uz.toshmatov.otg_usb_camera.ui.main.MainViewModel
import uz.toshmatov.otg_usb_camera.ui.main.StreamSettingsScreen
import uz.toshmatov.otg_usb_camera.ui.rtsp.StreamPlayerScreen
import uz.toshmatov.otg_usb_camera.ui.select.CameraSelectScreen
import uz.toshmatov.otg_usb_camera.ui.settings.SettingsScreen
import uz.toshmatov.otg_usb_camera.ui.settings.SettingsViewModel
import uz.toshmatov.otg_usb_camera.ui.splash.SplashScreen
import uz.toshmatov.strem_lib.StreamService

@Composable
fun AppNavHost(
    mService: StreamService?,
    onStartService: () -> Unit = {}
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val context = LocalContext.current
            var usbDetected by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                usbDetected = usbManager.deviceList.isNotEmpty()
            }

            DisposableEffect(Unit) {
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(ctx: Context, intent: Intent) {
                        if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                            usbDetected = true
                        }
                    }
                }
                val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                context.registerReceiver(receiver, filter)
                onDispose { context.unregisterReceiver(receiver) }
            }

            LaunchedEffect(usbDetected) {
                if (usbDetected) {
                    delay(600)
                    navController.navigate(Screen.LivePreview.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }

            SplashScreen(
                usbDetected = usbDetected,
                onContinue = {
                    navController.navigate(Screen.CameraSelect.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.CameraSelect.route) {
            val context = LocalContext.current
            CameraSelectScreen(
                onBack = { navController.popBackStack() },
                onContinue = { selectedCamera ->
                    if (selectedCamera != null && !selectedCamera.isUsbConnected && selectedCamera.facing != null) {
                        context.getSharedPreferences("stream_settings", Context.MODE_PRIVATE).edit {
                            putInt("preferred_facing", selectedCamera.facing)
                        }
                    }
                    navController.navigate(Screen.LivePreview.route) {
                        popUpTo(Screen.CameraSelect.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.LivePreview.route) {
            LaunchedEffect(Unit) {
                onStartService()
            }
            LivePreviewScreen(
                mService = mService,
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenStreamSettings = { navController.navigate(Screen.StreamSettings.route) },
                onOpenPlayer = { navController.navigate(Screen.StreamPlayer.route) }
            )
        }
        composable(Screen.StreamSettings.route) {
            StreamSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.StreamPlayer.route) {
            StreamPlayerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}