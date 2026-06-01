package uz.toshmatov.otg_usb_camera.ui.rtsp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import org.koin.androidx.compose.koinViewModel
import uz.toshmatov.otg_usb_camera.ui.theme.OtgAccent
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline2
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextMute

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun StreamPlayerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: RtspViewModel = koinViewModel()

    val rtspUrl by viewModel.rtspUrl.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

    // ExoPlayer — remember orqali bir marta yaratiladi
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        viewModel.updatePlayerState(PlayerState.Playing)
                    }
                    // Paused → listener emas, tugma orqali boshqariladi
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING ->
                            viewModel.updatePlayerState(PlayerState.Buffering)
                        Player.STATE_ENDED ->
                            viewModel.updatePlayerState(PlayerState.Idle)
                        Player.STATE_IDLE ->
                            if (playerState !is PlayerState.Error)
                                viewModel.updatePlayerState(PlayerState.Idle)
                        else -> {}
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    val msg = when (error.errorCode) {
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                            "Server topilmadi"
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                            "Ulanish vaqti tugdi"
                        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ->
                            "Format qo'llab-quvvatlanmaydi"
                        else -> error.message ?: "Ulanish xatosi"
                    }
                    viewModel.updatePlayerState(PlayerState.Error(msg))
                }
            })
        }
    }

    // Lifecycle: pause/resume + release
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        viewModel.updatePlayerState(PlayerState.Paused)
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (playerState == PlayerState.Paused) {
                        exoPlayer.play()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    val isActive = playerState == PlayerState.Playing || playerState == PlayerState.Buffering
    val isPlaying = playerState == PlayerState.Playing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video player
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Brush.verticalGradient(listOf(Color(0x8C000000), Color.Transparent)))
        )
        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
        )

        // ── TOP bar ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)   // edge-to-edge to'g'ri
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x8C000000))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            // LIVE pill — faqat isActive bo'lganda
            if (isActive) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(OtgAccent)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Text(
                        "LIVE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }

        // ── BOTTOM controls ───────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Error xabari
            if (playerState is PlayerState.Error) {
                Text(
                    text = "⚠ ${(playerState as PlayerState.Error).message}",
                    color = OtgAccent,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x8C000000))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // URL input
            OutlinedTextField(
                value = rtspUrl,
                onValueChange = { viewModel.updateRtspUrl(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("rtsp://server:port/stream", color = OtgTextMute, fontSize = 12.sp)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OtgAccent,
                    unfocusedBorderColor = OtgHairline2,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0x8C000000),
                    unfocusedContainerColor = Color(0x8C000000),
                    cursorColor = OtgAccent
                ),
                textStyle = TextStyle(fontSize = 12.sp)
            )

            // Controls pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Color(0x8C000000))
                    .border(1.dp, OtgHairline, CircleShape)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play / Pause
                IconButton(
                    onClick = {
                        when {
                            rtspUrl.isBlank() ->
                                Toast.makeText(context, "RTSP URL kiriting", Toast.LENGTH_SHORT).show()

                            playerState == PlayerState.Idle || playerState is PlayerState.Error -> {
                                // Yangi stream boshlash
                                exoPlayer.setMediaItem(MediaItem.fromUri(rtspUrl))
                                exoPlayer.prepare()
                                exoPlayer.play()
                            }

                            playerState == PlayerState.Playing -> {
                                // Pause
                                exoPlayer.pause()
                                viewModel.updatePlayerState(PlayerState.Paused)
                            }

                            playerState == PlayerState.Paused ||
                            playerState == PlayerState.Buffering -> {
                                // Resume
                                exoPlayer.play()
                            }

                            else -> {}
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Progress — buffering: indeterminate, playing: to'liq, idle: bo'sh
                when (playerState) {
                    PlayerState.Buffering -> LinearProgressIndicator(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = OtgAccent,
                        trackColor = Color.White.copy(alpha = 0.18f)
                    )
                    PlayerState.Playing -> Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(OtgAccent)
                    )
                    else -> Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.18f))
                    )
                }

                // Stop
                IconButton(
                    onClick = {
                        exoPlayer.stop()
                        exoPlayer.clearMediaItems()
                        viewModel.updatePlayerState(PlayerState.Idle)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}