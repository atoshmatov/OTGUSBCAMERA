package uz.toshmatov.otg_usb_camera.ui

import android.content.Context
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun RtspViewerScreen(
    context: Context,
    onNavigateBack: () -> Unit
) {
    var rtspUrl by remember { mutableStateOf("rtsp://example.com/stream") }
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "RTSP Stream Viewer",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // URL Input Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1a1a1a))
                    .padding(16.dp)
            ) {
                Text(
                    "RTSP Stream URL",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = rtspUrl,
                    onValueChange = { rtspUrl = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = {
                        Text(
                            "rtsp://server:port/stream",
                            color = Color(0xFF666666),
                            fontSize = 12.sp
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
                        fontSize = 13.sp,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Video Player Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF333333),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            exoPlayer = ExoPlayer.Builder(ctx).build().also { player ->
                                setPlayer(player)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls Section
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            try {
                                if (rtspUrl.isBlank()) {
                                    Toast.makeText(context, "Enter RTSP URL", Toast.LENGTH_SHORT).show()
                                } else {
                                    val mediaItem = MediaItem.fromUri(rtspUrl)
                                    exoPlayer?.setMediaItem(mediaItem)
                                    exoPlayer?.prepare()
                                    exoPlayer?.play()
                                    Toast.makeText(context, "Playing...", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .background(Color(0xFF08CB00))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "▶ Play",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                // Stop Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            exoPlayer?.stop()
                            Toast.makeText(context, "Stopped", Toast.LENGTH_SHORT).show()
                        }
                        .background(Color(0xFFE74C3C))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "⏹ Stop",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1a1a1a))
                    .padding(14.dp)
            ) {
                Text(
                    "📺 How to use",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "1. Enter RTSP stream URL above\n2. Press Play to start viewing\n3. Press Stop to disconnect",
                    color = Color(0xFF999999),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stream Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0a3a5a))
                    .padding(14.dp)
            ) {
                Text(
                    "ℹ️ Supported protocols",
                    color = Color(0xFF1E90FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "RTSP/RTSPS streams\nRaw H.264/H.265 video",
                    color = Color(0xFF66CCFF),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }
}
