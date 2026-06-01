package uz.toshmatov.otg_usb_camera.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.toshmatov.otg_usb_camera.ui.theme.OtgAccent
import uz.toshmatov.otg_usb_camera.ui.theme.OtgBg
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline2
import uz.toshmatov.otg_usb_camera.ui.theme.OtgSurface
import uz.toshmatov.otg_usb_camera.ui.theme.OtgSurface2
import uz.toshmatov.otg_usb_camera.ui.theme.OtgText
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextMute
import uz.toshmatov.strem_lib.CameraState

@Composable
fun StreamSettingsScreen(onBack: () -> Unit, viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val cameraState by viewModel.cameraState.collectAsStateWithLifecycle()
    // Draft — foydalanuvchi tahrirlaydi, "Saqlash" bosilganda commit bo'ladi
    var draft by remember(settings) { mutableStateOf(settings) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OtgBg)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = OtgText)
            }
            Text(
                "Stream sozlamalari",
                color = OtgText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // RTMP Server
            OtgSection(title = "RTMP Server") {
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(
                        "Server URL",
                        color = OtgTextMute,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    OutlinedTextField(
                        value = draft.rtmpUrl,
                        onValueChange = { draft = draft.copy(rtmpUrl = it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        placeholder = {
                            Text("rtmp://server/live/stream", color = OtgTextMute, fontSize = 13.sp)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OtgAccent,
                            unfocusedBorderColor = OtgHairline2,
                            focusedTextColor = OtgText,
                            unfocusedTextColor = OtgText,
                            cursorColor = OtgAccent,
                            focusedContainerColor = OtgSurface2,
                            unfocusedContainerColor = OtgSurface2
                        ),
                        textStyle = TextStyle(fontSize = 13.sp, color = OtgText)
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Video
            OtgSection(title = "Video") {
                OtgPickerRow(
                    label = "Resolution",
                    currentValue = draft.resolutionLabel,
                    options = StreamSettings.RESOLUTION_OPTIONS.map { it.third },
                    onSelect = { label ->
                        StreamSettings.RESOLUTION_OPTIONS.find { it.third == label }
                            ?.let { (w, h, _) -> draft = draft.copy(videoWidth = w, videoHeight = h) }
                    }
                )
                OtgPickerRow(
                    label = "Frame rate",
                    currentValue = draft.fpsLabel,
                    options = StreamSettings.FPS_OPTIONS.map { "$it fps" },
                    onSelect = { label ->
                        label.removeSuffix(" fps").toIntOrNull()
                            ?.let { draft = draft.copy(fps = it) }
                    }
                )
                OtgPickerRow(
                    label = "Video bitrate",
                    currentValue = draft.videoBitrateLabel,
                    options = StreamSettings.VIDEO_BITRATE_OPTIONS.map { "$it kbps" },
                    onSelect = { label ->
                        label.removeSuffix(" kbps").toIntOrNull()
                            ?.let { draft = draft.copy(videoBitrateKbps = it) }
                    }
                )
            }

            // Audio
            OtgSection(title = "Audio") {
                OtgPickerRow(
                    label = "Bitrate",
                    currentValue = draft.audioBitrateLabel,
                    options = StreamSettings.AUDIO_BITRATE_OPTIONS.map { "$it kbps" },
                    onSelect = { label ->
                        label.removeSuffix(" kbps").toIntOrNull()
                            ?.let { draft = draft.copy(audioBitrateKbps = it) }
                    }
                )
                OtgInfoRow(
                    label = "Source",
                    value = if (cameraState == CameraState.USB) "USB camera mic" else "Device mic"
                )
            }

            // Advanced
            OtgSection(title = "Advanced") {
                OtgToggleRow("Auto-reconnect", draft.autoReconnect) { draft = draft.copy(autoReconnect = it) }
                OtgToggleRow("Adaptive bitrate", draft.adaptiveBitrate) { draft = draft.copy(adaptiveBitrate = it) }
                OtgToggleRow("Hardware encoder", draft.hardwareEncoder) { draft = draft.copy(hardwareEncoder = it) }
            }

            // Save button
            Button(
                onClick = {
                    viewModel.updateSettings(draft)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OtgAccent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Sozlamalarni saqlash",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

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

@Composable
internal fun OtgPickerRow(
    label: String,
    currentValue: String,
    sub: String? = null,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
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
            Column(horizontalAlignment = Alignment.End) {
                Text(currentValue, color = OtgText, fontSize = 13.sp)
                if (sub != null) Text(sub, color = OtgTextMute, fontSize = 10.sp)
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = OtgTextMute,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(OtgSurface2)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = OtgText, fontSize = 14.sp) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
    HorizontalDivider(color = OtgHairline, thickness = 0.5.dp)
}

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

@Composable
internal fun OtgToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OtgAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = OtgSurface2
            )
        )
    }
    HorizontalDivider(color = OtgHairline, thickness = 0.5.dp)
}
