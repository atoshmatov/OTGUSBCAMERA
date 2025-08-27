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
import android.view.SurfaceHolder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedro.rtplibrary.view.OpenGlView
import org.koin.androidx.viewmodel.ext.android.viewModel
import uz.toshmatov.otg_usb_camera.ui.theme.OTGUSBCAMERATheme
import uz.toshmatov.strem_lib.StreamService

class MainActivity : ComponentActivity(), SurfaceHolder.Callback, ServiceConnection {

    private val viewModel: MainViewModel by viewModel()
    private var mService: StreamService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = (service as StreamService.LocalBinder).getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mService = null
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.values.all { it }
            if (allGranted) {
                startAndBindService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.serviceLiveEvent.observe(this) { mService?.let(it) }
        if (hasAllPermissions()) {
            startAndBindService()
        } else {
            requestAllPermissions()
        }

        setContent {
            OTGUSBCAMERATheme {
                val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    var openGlViewRef by remember { mutableStateOf<OpenGlView?>(null) }

                    AndroidView(
                        factory = { context ->
                            OpenGlView(context).apply {
                                openGlViewRef = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    LaunchedEffect(mService, openGlViewRef) {
                        if (mService != null && openGlViewRef != null) {
                            mService!!.setView(openGlViewRef!!)
                            mService!!.startPreview()
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.onStreamControlButtonClick()
                            }
                            .background(Color(0xFF77BEF0))
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
    }

    private fun hasAllPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
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
            Manifest.permission.CAMERA
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startAndBindService() {
        val intent = Intent(this, StreamService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        isServiceBound = true
    }

    private fun stopService() {
        stopService(Intent(this, StreamService::class.java))
        mService = null
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    private fun getServiceIntent(): Intent {
        return Intent(this, StreamService::class.java).also {
            bindService(it, this, Context.BIND_AUTO_CREATE)
        }
    }


    override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mService?.let {
            it.setView(applicationContext)
            it.stopPreview()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        mService = (service as StreamService.LocalBinder).getService()
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
        mService = null
    }

    override fun onStart() {
        super.onStart()
        getServiceIntent()
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }


    override fun onDestroy() {
        if (mService?.isStreaming == false) stopService()
        super.onDestroy()
    }
}

