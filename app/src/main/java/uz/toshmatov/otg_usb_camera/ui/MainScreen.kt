package uz.toshmatov.otg_usb_camera.ui

import android.content.Context
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.pedro.rtplibrary.view.OpenGlView
import kotlinx.coroutines.launch
import uz.toshmatov.strem_lib.CameraState
import uz.toshmatov.strem_lib.StreamService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    context: Context,
    viewModel: MainViewModel,
    onNavigateToRtsp: () -> Unit,
    mService: StreamService?,
    lifecycleOwner: LifecycleOwner
) {
    val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
    val rtmpUrl by viewModel.rtmpUrl.collectAsStateWithLifecycle()
    val cameraState by viewModel.cameraState.collectAsStateWithLifecycle()

    var openGlViewRef by remember { mutableStateOf<OpenGlView?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "OTG USB Camera",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            actions = {
                IconButton(onClick = { /* Settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1a1a1a),
                titleContentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Camera Status Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isStreaming -> Color(0xFF0a3a0a)
                            cameraState == CameraState.USB -> Color(0xFF0a3a1a)
                            cameraState == CameraState.Phone -> Color(0xFF0a1a3a)
                            else -> Color(0xFF3a0a0a)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = when {
                            isStreaming -> Color(0xFFE74C3C)
                            cameraState == CameraState.USB -> Color(0xFF08CB00)
                            cameraState == CameraState.Phone -> Color(0xFF66CCFF)
                            else -> Color(0xFFE74C3C)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Camera Status",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            when {
                                isStreaming && cameraState == CameraState.USB -> "🔌 USB Camera • Streaming"
                                isStreaming && cameraState == CameraState.Phone -> "📱 Phone Camera • Streaming"
                                cameraState == CameraState.USB -> "🔌 USB Camera Connected"
                                cameraState == CameraState.Phone -> "📱 Phone Camera Ready"
                                else -> "❌ No Camera"
                            },
                            color = when {
                                isStreaming -> Color(0xFFE74C3C)
                                cameraState == CameraState.USB -> Color(0xFF08CB00)
                                cameraState == CameraState.Phone -> Color(0xFF66CCFF)
                                else -> Color(0xFFE74C3C)
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isStreaming) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE74C3C))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🔴 LIVE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Video Preview
            if (cameraState == CameraState.USB) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .border(
                            width = 2.dp,
                            color = if (isStreaming) Color(0xFF08CB00) else Color(0xFFE74C3C),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    AndroidView(
                        factory = { ctx ->
                            OpenGlView(ctx).apply {
                                openGlViewRef = this
                                holder.addCallback(object : SurfaceHolder.Callback {
                                    override fun surfaceCreated(holder: SurfaceHolder) {
                                        lifecycleOwner.lifecycleScope.launch {
                                            openGlViewRef?.let { view ->
                                                mService?.setView(view)
                                                mService?.startPreview()
                                            }
                                        }
                                    }

                                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                                        lifecycleOwner.lifecycleScope.launch {
                                            mService?.clearView()
                                        }
                                    }

                                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                                })
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            } else {
                PhoneCameraPreview(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    onPhoneCameraReady = {
                        mService?.notifyPhoneCameraReady()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RTMP Endpoint Input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1a1a1a))
                    .padding(16.dp)
            ) {
                Text(
                    "RTMP Endpoint",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = rtmpUrl,
                    onValueChange = { viewModel.updateRtmpUrl(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = {
                        Text(
                            "rtmp://server/live/stream",
                            color = Color(0xFF666666),
                            fontSize = 11.sp
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0a0a0a),
                        unfocusedContainerColor = Color(0xFF0a0a0a),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color(0xFF1E90FF),
                        unfocusedIndicatorColor = Color(0xFF333333)
                    ),
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stream Control Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val endpoint = viewModel.getRtmpEndpoint()
                            if (endpoint.isBlank()) {
                                Toast.makeText(context, "Enter RTMP endpoint", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.onStreamControlButtonClick(endpoint)
                            }
                        }
                        .background(if (isStreaming) Color(0xFFE74C3C) else Color(0xFF155E95))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isStreaming) "⏹ Stop" else "▶ Start",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                // RTSP Viewer Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToRtsp() }
                        .background(Color(0xFF1E90FF))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📺 RTSP",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0a2a4a))
                    .padding(14.dp)
            ) {
                Text(
                    "ℹ️ Features",
                    color = Color(0xFF1E90FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• Live RTMP streaming from USB camera\n• Real-time video preview\n• Background streaming support\n• RTSP viewer for remote streams",
                    color = Color(0xFF66CCFF),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Lifecycle management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    lifecycleOwner.lifecycleScope.launch {
                        openGlViewRef?.let { view ->
                            mService?.setView(view)
                            mService?.startPreview()
                        }
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    lifecycleOwner.lifecycleScope.launch {
                        mService?.stopPreview()
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
}
