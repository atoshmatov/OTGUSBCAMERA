package uz.toshmatov.otg_usb_camera.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uz.toshmatov.otg_usb_camera.ui.main.MainViewModel
import uz.toshmatov.otg_usb_camera.ui.navigation.AppNavHost
import uz.toshmatov.otg_usb_camera.ui.settings.SettingsViewModel
import uz.toshmatov.otg_usb_camera.ui.theme.OTGUSBCAMERATheme
import uz.toshmatov.strem_lib.StreamService

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()
    private val settingsViewModel: SettingsViewModel by viewModel()

    private var mService: StreamService? by mutableStateOf(null)

    private var isServiceBound = false
    private var hasPermissions = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val service = (binder as StreamService.LocalBinder).getService()
            val streamFlow = binder.streamingFlow()

            mService = service
            isServiceBound = true
            viewModel.attachService(service)

            lifecycleScope.launch {
                streamFlow.collect { isStreaming ->
                    viewModel.updateStreamingState(isStreaming)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            viewModel.detachService()
            mService = null
            isServiceBound = false
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.values.all { it }
            if (allGranted) {
                hasPermissions = true
                startAndBindService()
            } else {
                val permanentlyDenied = result.keys.any { permission ->
                    !shouldShowRequestPermissionRationale(permission) &&
                        ContextCompat.checkSelfPermission(
                            this,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                }

                if (permanentlyDenied) {
                    Toast.makeText(
                        this,
                        "Permissions required, please enable them in Settings",
                        Toast.LENGTH_LONG
                    ).show()
                    openAppSettings()
                } else {
                    Toast.makeText(this, "Permissions denied!", Toast.LENGTH_LONG).show()
                }
            }
        }

    private fun openAppSettings() {
        val intent = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            android.net.Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hasPermissions = hasAllPermissions()
        if (!hasPermissions) requestAllPermissions()

        setContent {
            val navController = rememberNavController()
            OTGUSBCAMERATheme {
                AppNavHost(
                    navController = navController,
                    mainViewModel = viewModel,
                    mService = mService,
                    settingsViewModel = settingsViewModel,
                    onStartService = {
                        if (hasPermissions && !isServiceBound) startAndBindService()
                    }
                )
            }
        }
    }

    private fun hasAllPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_CAMERA)
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestAllPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_CAMERA)
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startAndBindService() {
        val intent = Intent(this, StreamService::class.java)
        startForegroundService(intent)
        bindService(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStart() {
        super.onStart()
        // Background'dan qaytganda service hali ishlayotgan bo'lsa qayta bind qilish.
        // Flag 0 (BIND_AUTO_CREATE yo'q) — service allaqachon ishlamaса bind qilinmaydi.
        if (!isServiceBound && hasPermissions) {
            val intent = Intent(this, StreamService::class.java)
            bindService(intent, serviceConnection, 0)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
            viewModel.detachService()
            mService = null
        }
    }
}
