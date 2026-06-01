package uz.toshmatov.otg_usb_camera.ui.splash

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.toshmatov.otg_usb_camera.ui.theme.OtgAccent
import uz.toshmatov.otg_usb_camera.ui.theme.OtgBg
import uz.toshmatov.otg_usb_camera.ui.theme.OtgGood
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline2
import uz.toshmatov.otg_usb_camera.ui.theme.OtgSurface
import uz.toshmatov.otg_usb_camera.ui.theme.OtgSurface2
import uz.toshmatov.otg_usb_camera.ui.theme.OtgText
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextDim
import uz.toshmatov.otg_usb_camera.ui.theme.OtgWarn

@Composable
fun SplashScreen(
    usbDetected: Boolean = false,
    onContinue: () -> Unit
) {
    // ── Smooth color transitions ────────────────────────────────────
    val pillBg by animateColorAsState(
        targetValue = if (usbDetected) OtgGood.copy(alpha = 0.15f) else OtgSurface,
        animationSpec = tween(500), label = "pillBg"
    )
    val pillBorder by animateColorAsState(
        targetValue = if (usbDetected) OtgGood.copy(alpha = 0.4f) else OtgHairline,
        animationSpec = tween(500), label = "pillBorder"
    )
    val dotColor by animateColorAsState(
        targetValue = if (usbDetected) OtgGood else OtgWarn,
        animationSpec = tween(500), label = "dotColor"
    )
    val pillTextColor by animateColorAsState(
        targetValue = if (usbDetected) OtgGood else OtgTextDim,
        animationSpec = tween(500), label = "pillTextColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (usbDetected) OtgGood else OtgText,
        animationSpec = tween(600), label = "iconTint"
    )
    val iconBoxBorder by animateColorAsState(
        targetValue = if (usbDetected) OtgGood.copy(alpha = 0.4f) else OtgHairline2,
        animationSpec = tween(600), label = "iconBoxBorder"
    )
    val ringColor by animateColorAsState(
        targetValue = if (usbDetected) OtgGood else OtgHairline,
        animationSpec = tween(600), label = "ringColor"
    )

    // ── Pulse ring animation ────────────────────────────────────────
    // 3 ta halqa ketma-ket 667ms interval bilan "portlab" chiqadi
    val pulse = rememberInfiniteTransition(label = "pulse")

    // Har ring uchun 0f→1f qiymati: scale + alpha hisoblanadi
    val r1 by pulse.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = EaseOut),
            RepeatMode.Restart,
            StartOffset(0)
        ), label = "r1"
    )
    val r2 by pulse.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = EaseOut),
            RepeatMode.Restart,
            StartOffset(667)
        ), label = "r2"
    )
    val r3 by pulse.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = EaseOut),
            RepeatMode.Restart,
            StartOffset(1334)
        ), label = "r3"
    )

    // scale: 0.35f → 1.0f,  alpha: 0.6f → 0.0f
    fun ringScale(t: Float) = 0.35f + t * 0.65f
    fun ringAlpha(t: Float) = (0.6f * (1f - t)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OtgBg)
            .systemBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Brand ───────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OtgAccent),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )
            }
            Text(
                "otgcamera",
                color = OtgText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        }

        // ── Center — USB illustration ────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Pulse halqalar + icon
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulslanuvchi halqalar
                listOf(r1, r2, r3).forEach { t ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(ringScale(t))
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = ringColor.copy(alpha = ringAlpha(t)),
                                shape = CircleShape
                            )
                    )
                }

                // Icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(OtgSurface2)
                        .border(1.dp, iconBoxBorder, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Usb,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Matn
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "USB kamerani ulang",
                    color = OtgText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    text = if (usbDetected)
                        "Kamera aniqlandi. Ulanmoqda..."
                    else
                        "OTG kabel orqali kamerani telefonga ulang.\nAvtomatik aniqlanadi.",
                    color = OtgTextDim,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Status pill + progress
            if (usbDetected) {
                // Topildi: pill + kichik progress indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(pillBg)
                            .border(1.dp, pillBorder, RoundedCornerShape(99.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Text(
                            "Topildi!",
                            color = pillTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = OtgGood,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                // Kutilmoqda: oddiy pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(pillBg)
                        .border(1.dp, pillBorder, RoundedCornerShape(99.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Text(
                        "Kutilmoqda...",
                        color = pillTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ── Pastki tugma ─────────────────────────────────────────────
        // USB ulashni kutmasdan qo'lda davom etish
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OtgSurface2,
                contentColor = OtgTextDim
            )
        ) {
            Text(
                "Qo'lda davom etish",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
        }
    }
}
