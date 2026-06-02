package uz.toshmatov.otg_usb_camera.ui.select

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.toshmatov.otg_usb_camera.ui.select.model.CameraDevice
import uz.toshmatov.otg_usb_camera.ui.select.util.detectCameras
import uz.toshmatov.otg_usb_camera.ui.theme.OtgAccent
import uz.toshmatov.otg_usb_camera.ui.theme.OtgBg
import uz.toshmatov.otg_usb_camera.ui.theme.OtgGood
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline2
import uz.toshmatov.otg_usb_camera.ui.theme.OtgSurface
import uz.toshmatov.otg_usb_camera.ui.theme.OtgSurface2
import uz.toshmatov.otg_usb_camera.ui.theme.OtgText
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextDim


@Composable
fun CameraSelectScreen(
    onBack: () -> Unit,
    onContinue: (selected: CameraDevice?) -> Unit
) {
    val context = LocalContext.current
    var cameras by remember { mutableStateOf<List<CameraDevice>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTick by remember { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    // Async yuklash — main thread bloklanmaydi
    LaunchedEffect(refreshTick) {
        isLoading = true
        cameras = detectCameras(context)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OtgBg)
            .systemBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OtgSurface2)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = OtgText
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Kamera tanlash",
                    color = OtgText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    if (isLoading) "Aniqlanmoqda..."
                    else "${cameras.size} qurilma · ${cameras.count { it.isUsbConnected }} USB",
                    color = OtgTextDim,
                    fontSize = 12.sp
                )
            }
            // Refresh
            IconButton(
                onClick = { refreshTick++ },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OtgSurface2)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = OtgText)
            }
        }

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = OtgAccent, strokeWidth = 2.dp)
                }

                cameras.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Usb,
                            contentDescription = null,
                            tint = OtgTextDim,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Qurilma topilmadi",
                            color = OtgText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "USB kamerani OTG kabel bilan ulang",
                            color = OtgTextDim,
                            fontSize = 13.sp
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cameras.forEachIndexed { index, cam ->
                            val isSelected = selectedIndex == index
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) OtgSurface2 else OtgSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) OtgHairline2 else Color.Transparent,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedIndex = index }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) OtgSurface2 else OtgBg)
                                        .border(1.dp, OtgHairline, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        cam.icon,
                                        contentDescription = null,
                                        tint = if (cam.isUsbConnected) OtgGood else OtgTextDim,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        cam.name,
                                        color = OtgText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = (-0.2).sp
                                    )
                                    Text(
                                        cam.sub,
                                        color = OtgTextDim,
                                        fontSize = 12.sp
                                    )
                                }
                                if (cam.isUsbConnected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(OtgGood.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            "USB",
                                            color = OtgGood,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                // Radio button
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) OtgAccent else Color.Transparent)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) OtgAccent else OtgHairline2,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom button
        Box(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = { onContinue(cameras.getOrNull(selectedIndex)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OtgText,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    "Davom etish",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
