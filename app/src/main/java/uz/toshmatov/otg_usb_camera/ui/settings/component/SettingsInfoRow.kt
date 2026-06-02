package uz.toshmatov.otg_usb_camera.ui.settings.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import uz.toshmatov.otg_usb_camera.ui.theme.OtgText
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextDim

@Composable
fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OtgTextDim, fontSize = 14.sp)
        Text(value, color = OtgText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}