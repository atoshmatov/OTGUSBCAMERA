package uz.toshmatov.otg_usb_camera.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedro.rtplibrary.view.OpenGlView
import org.koin.androidx.viewmodel.ext.android.viewModel
import uz.toshmatov.strem_lib.StreamService

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    private var mService: StreamService? = null

    private var openGlViewRef: OpenGlView? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    private var isServiceBound = false
    private var hasPermissions = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val service = (binder as StreamService.LocalBinder).getService()
            mService = service
            isServiceBound = true
            viewModel.attachService(service)

            openGlViewRef?.let { view ->
                mainHandler.post {
                    try {
                        mService?.setView(view)
                        mService?.startPreview()
                    } catch (e: Exception) {
                        Log.e(
                            "MainActivity",
                            "Error binding view after service connected: ${e.message}"
                        )
                    }
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

        hasPermissions = hasAllPermissions()
        if (!hasPermissions) requestAllPermissions()

        setContent {
            val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF030303))
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))

                AndroidView(
                    factory = { ctx ->
                        OpenGlView(ctx).apply {
                            openGlViewRef = this

                            holder.addCallback(
                                object : SurfaceHolder.Callback {
                                    override fun surfaceCreated(holder: SurfaceHolder) {
                                        openGlViewRef?.let { view ->
                                            mService?.setView(view)
                                        }
                                    }

                                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                                        mService?.clearView()
                                    }

                                    override fun surfaceChanged(
                                        holder: SurfaceHolder,
                                        format: Int,
                                        width: Int,
                                        height: Int
                                    ) {
                                    }
                                }
                            )

                            /*holder.addCallback(
                                object : SurfaceHolder.Callback {
                                    override fun surfaceCreated(holder: SurfaceHolder) {
                                        Log.d("MainActivity", "Surface created")
                                        if (isServiceBound) {
                                            mainHandler.post {
                                                try {
                                                    openGlViewRef.let {
                                                        mService?.let {
                                                            it.setView(openGlViewRef!!)
                                                            it.startPreview()
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(
                                                        "MainActivity",
                                                        "Error on surfaceCreated bind: ${e.message}"
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    override fun surfaceChanged(
                                        holder: SurfaceHolder,
                                        format: Int,
                                        width: Int,
                                        height: Int
                                    ) {
                                        if (isServiceBound) {
                                            mainHandler.post {
                                                try {
                                                    openGlViewRef.let {
                                                        mService?.let {
                                                            it.setView(openGlViewRef!!)
                                                            it.startPreview()
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(
                                                        "MainActivity",
                                                        "Error on surfaceCreated bind: ${e.message}"
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                                        Log.d("MainActivity", "Surface destroyed")
                                        mainHandler.post {
                                            try {
                                                mService?.setView(applicationContext)
                                                mService?.stopPreview()
                                            } catch (e: Exception) {
                                                Log.e(
                                                    "MainActivity",
                                                    "Error on surfaceDestroyed: ${e.message}"
                                                )
                                            }
                                        }
                                    }
                                }
                            )*/
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = if (isStreaming) Color(0xFF08CB00) else Color(0xFFE74C3C),
                        )
                )

                DisposableEffect(lifecycleOwner, mService, openGlViewRef) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                val view = openGlViewRef
                                if (view != null && isServiceBound) {
                                    mainHandler.post {
                                        try {
                                            openGlViewRef.let {
                                                mService?.setView(view)
                                                mService?.startPreview()
                                            }
                                        } catch (e: Exception) {
                                            Log.e(
                                                "MainActivity",
                                                "Error on ON_RESUME bind: ${e.message}"
                                            )
                                        }
                                    }
                                }
                            }

                            Lifecycle.Event.ON_PAUSE -> {
                                mainHandler.post {
                                    try {
                                        mService?.stopPreview()
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Error on ON_PAUSE: ${e.message}")
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val endpoint = "rtmp://84.54.117.248:10005/live/stream1" // TODO
                            viewModel.onStreamControlButtonClick(endpoint)
                        }
                        .background(if (isStreaming) Color(0xFFE74C3C) else Color(0xFF155E95))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isStreaming) "Stop Stream" else "Start Stream",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // 34+
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_CAMERA)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // <29
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
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startAndBindService() {
        val intent = Intent(this, StreamService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent) // 26+
        } else {
            startService(intent)
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStart() {
        super.onStart()
        if (hasPermissions && !isServiceBound) startAndBindService()
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

