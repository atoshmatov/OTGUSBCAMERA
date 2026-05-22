package uz.toshmatov.otg_usb_camera.ui.splash

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    // Status pill colors depend on detection state
    val pillBg = if (usbDetected) OtgGood.copy(alpha = 0.15f) else OtgSurface
    val pillBorder = if (usbDetected) OtgGood.copy(alpha = 0.4f) else OtgHairline
    val dotColor = if (usbDetected) OtgGood else OtgWarn
    val pillText = if (usbDetected) "Topildi!" else "kutilmoqda..."
    val pillTextColor = if (usbDetected) OtgGood else OtgTextDim

    // Icon tint: yashil bo'lganda OtgGood, aks holda OtgText
    val iconTint = if (usbDetected) OtgGood else OtgText
    val iconBoxBorder = if (usbDetected) OtgGood.copy(alpha = 0.4f) else OtgHairline2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OtgBg)
            .systemBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Brand
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

        // Center — USB illustration
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Pulse rings + icon
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                listOf(0.5f, 0.75f, 1.0f).forEach { scale ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(scale)
                            .clip(CircleShape)
                            .border(1.dp, OtgHairline, CircleShape)
                    )
                }
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

            // Status pill — kutilmoqda (sariq) yoki Topildi! (yashil)
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
                    pillText,
                    color = pillTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Bottom button
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
                "Qo'llanma · Yordam",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
        }
    }
}
