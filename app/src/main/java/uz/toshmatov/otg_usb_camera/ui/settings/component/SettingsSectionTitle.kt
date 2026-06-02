package uz.toshmatov.otg_usb_camera.ui.settings.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextMute

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        color = OtgTextMute,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}