package uz.toshmatov.otg_usb_camera.ui.main.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline
import uz.toshmatov.otg_usb_camera.ui.theme.OtgText
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextMute

@Composable
internal fun OtgInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = OtgText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(value, color = OtgTextMute, fontSize = 13.sp)
    }
    HorizontalDivider(color = OtgHairline, thickness = 0.5.dp)
}
