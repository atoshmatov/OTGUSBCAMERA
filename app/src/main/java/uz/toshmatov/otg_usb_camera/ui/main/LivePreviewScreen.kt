package uz.toshmatov.otg_usb_camera.ui.main

import android.view.SurfaceHolder
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.pedro.rtplibrary.view.OpenGlView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.toshmatov.otg_usb_camera.ui.theme.OtgAccent
import uz.toshmatov.otg_usb_camera.ui.theme.OtgGood
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline2
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextDim
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextMute
import uz.toshmatov.strem_lib.CameraState
import uz.toshmatov.strem_lib.StreamService

@Composable
fun LivePreviewScreen(
    viewModel: MainViewModel,
    mService: StreamService?,
    onOpenSettings: () -> Unit,
    onOpenStreamSettings: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
    val cameraState by viewModel.cameraState.collectAsStateWithLifecycle()
    val rtmpUrl by viewModel.rtmpUrl.collectAsStateWithLifecycle()

    var openGlViewRef by remember { mutableStateOf<OpenGlView?>(null) }

    // ── Service ulanishi bilanoq view'ni qayta ulash ─────────────
    // surfaceCreated vaqtida mService null bo'lishi mumkin,
    // shuning uchun service tayyor bo'lganda setView + startPreview qayta chaqiriladi
    LaunchedEffect(mService, openGlViewRef) {
        val service = mService ?: return@LaunchedEffect
        val view = openGlViewRef ?: return@LaunchedEffect
        service.setView(view)
        service.startPreview()
    }

    // ── Timer ────────────────────────────────────────────────────
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isStreaming) {
        if (isStreaming) {
            timerRunning = true
            elapsedSeconds = 0L
        } else {
            timerRunning = false
            elapsedSeconds = 0L
        }
    }

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    val timerText = remember(elapsedSeconds) {
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        "%02d:%02d:%02d".format(h, m, s)
    }

    // ── Stream button animation ──────────────────────────────────
    // Har doim QIZIL. IDLE = katta doira, LIVE = kichik to'rtburchak (stop)
    val streamInteraction = remember { MutableInteractionSource() }
    val isPressed by streamInteraction.collectIsPressedAsState()

    // Bosganda kichrayish
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.87f else 1f,
        animationSpec = tween(100),
        label = "buttonScale"
    )
    // Streaming holatida ichki shape o'zgaradi
    val innerSize by animateDpAsState(
        targetValue = if (isStreaming) 30.dp else 54.dp,
        animationSpec = tween(250),
        label = "innerSize"
    )
    val innerCorner by animateDpAsState(
        targetValue = if (isStreaming) 7.dp else 27.dp,
        animationSpec = tween(250),
        label = "innerCorner"
    )
    // Rang HAR DOIM qizil
    val innerColor = OtgAccent

    // Kamera aspect ratio — USB camera o'z o'lchamini beradi, phone uchun 16:9
    val cameraAspect = remember(cameraState, mService) {
        when {
            cameraState == CameraState.USB -> {
                val w = mService?.cameraWidth ?: 1280
                val h = mService?.cameraHeight ?: 960
                if (h != 0) w.toFloat() / h.toFloat() else 4f / 3f
            }
            else -> 16f / 9f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── Camera feed — crop bo'lmay, aspect ratio saqlanib ko'rsatiladi ──
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    OpenGlView(ctx).apply {
                        openGlViewRef = this
                        holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(h: SurfaceHolder) {
                                lifecycleOwner.lifecycleScope.launch {
                                    mService?.setView(this@apply)
                                    mService?.startPreview()
                                }
                            }
                            override fun surfaceDestroyed(h: SurfaceHolder) {
                                lifecycleOwner.lifecycleScope.launch { mService?.clearView() }
                            }
                            override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, ht: Int) {}
                        })
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(cameraAspect, matchHeightConstraintsFirst = false)
            )
        }

        // ── Gradients ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.verticalGradient(listOf(Color(0xAA000000), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
        )

        // ── TOP overlay ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status pill — LIVE = yashil, IDLE = shaffof
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        if (isStreaming) OtgAccent else Color(0x8C000000)
                    )
                    .border(
                        1.dp,
                        if (isStreaming) Color.Transparent else OtgHairline2,
                        RoundedCornerShape(99.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isStreaming -> Color.White
                                cameraState != CameraState.Disconnected -> OtgGood
                                else -> OtgTextMute
                            }
                        )
                )
                Text(
                    text = when {
                        isStreaming -> "ON AIR"
                        cameraState != CameraState.Disconnected -> "READY"
                        else -> "OFFLINE"
                    },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                // Timer — faqat streaming holatda
                if (isStreaming) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(11.dp)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                    Text(
                        timerText,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Settings
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x8C000000))
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // ── BOTTOM controls ──────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // RTMP URL bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x8C000000))
                    .border(1.dp, OtgHairline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.NetworkWifi, contentDescription = null, tint = OtgTextDim, modifier = Modifier.size(16.dp))
                Text(
                    text = rtmpUrl.ifBlank { "rtmp://server/live/stream" },
                    color = if (rtmpUrl.isBlank()) OtgTextMute else OtgTextDim,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onOpenStreamSettings, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = OtgTextDim, modifier = Modifier.size(16.dp))
                }
            }

            // Main controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // RTSP Player button
                IconButton(
                    onClick = onOpenPlayer,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0x80000000))
                        .border(1.dp, OtgHairline2, CircleShape)
                ) {
                    Icon(Icons.Default.Cast, contentDescription = null, tint = Color.White)
                }

                // ── PRIMARY stream button ────────────────────────
                // Tashqi: oq doira (clip YO'Q → ichki shape ishlaydi)
                // Idle  → katta QIZIL doira  ●
                // Live  → kichik QIZIL square ■ (stop belgisi)
                // Press → kichrayadi (scale animatsiya)
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                        .border(3.5.dp, Color.White.copy(alpha = 0.88f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(innerSize)
                            .clip(RoundedCornerShape(innerCorner))
                            .background(innerColor)
                            .clickable(
                                interactionSource = streamInteraction,
                                indication = null
                            ) {
                                if (rtmpUrl.isNotBlank()) {
                                    viewModel.onStreamControlButtonClick(rtmpUrl)
                                }
                            }
                    )
                }

                // Camera snapshot
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0x80000000))
                        .border(1.dp, OtgHairline2, CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                }
            }

            // Hint
            Text(
                text = when {
                    isStreaming -> "STOP UCHUN BOSING"
                    rtmpUrl.isBlank() -> "RTMP URL KIRITING"
                    else -> "LIVE UCHUN BOSING"
                },
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    // Lifecycle observer
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
                    // Streaming davom etayotgan bo'lsa preview to'xtatilmaydi —
                    // background'da ham stream uzilmasin
                    lifecycleOwner.lifecycleScope.launch {
                        if (mService?.isStreaming != true) {
                            mService?.stopPreview()
                        }
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
