package uz.toshmatov.otg_usb_camera.ui.navigation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
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
    navController: NavHostController,
    mainViewModel: MainViewModel,
    mService: StreamService?,
    settingsViewModel: SettingsViewModel,
    onStartService: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val context = LocalContext.current
            var usbDetected by remember { mutableStateOf(false) }

            // Initial check — app ochilganda allaqachon ulangan USB camera bor-yo'qligini tekshirish
            LaunchedEffect(Unit) {
                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                usbDetected = usbManager.deviceList.isNotEmpty()
            }

            // BroadcastReceiver — USB ulanish hodisasini tinglash
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

            // USB topilganda 600ms kutib avtomatik LivePreview'ga o'tish
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
                    // Phone kamera tanlansa — facing preference ni saqlash
                    // StreamService startPhoneCameraStream() da shu preference'ga qaraydi
                    if (selectedCamera != null && !selectedCamera.isUsbConnected && selectedCamera.facing != null) {
                        context.getSharedPreferences("stream_prefs", Context.MODE_PRIVATE).edit {
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
            // Service faqat shu ekranga kirganda boshlanadi
            LaunchedEffect(Unit) {
                onStartService()
            }
            LivePreviewScreen(
                viewModel = mainViewModel,
                mService = mService,
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenStreamSettings = { navController.navigate(Screen.StreamSettings.route) },
                onOpenPlayer = { navController.navigate(Screen.StreamPlayer.route) }
            )
        }
        composable(Screen.StreamSettings.route) {
            StreamSettingsScreen(
                onBack = { navController.popBackStack() },
                viewModel = mainViewModel
            )
        }
        composable(Screen.StreamPlayer.route) {
            StreamPlayerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}