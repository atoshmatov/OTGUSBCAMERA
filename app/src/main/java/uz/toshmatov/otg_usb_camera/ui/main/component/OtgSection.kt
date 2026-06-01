package uz.toshmatov.otg_usb_camera.ui.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline
import uz.toshmatov.otg_usb_camera.ui.theme.OtgSurface
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextMute

@Composable
internal fun OtgSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title.uppercase(),
            color = OtgTextMute,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(OtgSurface)
                .border(1.dp, OtgHairline, RoundedCornerShape(16.dp)),
            content = content
        )
    }
}