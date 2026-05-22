package uz.toshmatov.otg_usb_camera.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.toshmatov.otg_usb_camera.ui.theme.OtgAccent
import uz.toshmatov.otg_usb_camera.ui.theme.OtgBg
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline
import uz.toshmatov.otg_usb_camera.ui.theme.OtgHairline2
import uz.toshmatov.otg_usb_camera.ui.theme.OtgSurface
import uz.toshmatov.otg_usb_camera.ui.theme.OtgText
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextDim
import uz.toshmatov.otg_usb_camera.ui.theme.OtgTextMute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var rtmpUrlInput by remember(settings.defaultRtmpUrl) {
        mutableStateOf(settings.defaultRtmpUrl)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sozlamalar",
                        color = OtgText,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,

                        letterSpacing = (-0.4).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = OtgText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OtgSurface,
                    titleContentColor = OtgText
                )
            )
        },
        containerColor = OtgBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Stream Settings Section
            SettingsSectionTitle("Stream Settings")

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = OtgSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OtgHairline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkWifi,
                            contentDescription = null,
                            tint = OtgAccent
                        )
                        Text(
                            "RTMP Server URL",
                            color = OtgText,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                    OutlinedTextField(
                        value = rtmpUrlInput,
                        onValueChange = { rtmpUrlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("rtmp://server/live/stream", color = OtgTextMute)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OtgAccent,
                            unfocusedBorderColor = OtgHairline2,
                            focusedTextColor = OtgText,
                            unfocusedTextColor = OtgText,
                            cursorColor = OtgAccent,
                            focusedContainerColor = OtgSurface,
                            unfocusedContainerColor = OtgSurface
                        ),
                        trailingIcon = {
                            if (rtmpUrlInput != settings.defaultRtmpUrl) {
                                IconButton(onClick = { viewModel.updateRtmpUrl(rtmpUrlInput) }) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Save",
                                        tint = OtgAccent
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Quality Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = OtgSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OtgHairline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = OtgAccent
                        )
                        Text(
                            "Stream Quality",
                            color = OtgText,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StreamQuality.entries.forEach { quality ->
                            FilterChip(
                                selected = settings.streamQuality == quality,
                                onClick = { viewModel.updateQuality(quality) },
                                label = { Text(quality.label) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OtgAccent,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                                    containerColor = OtgSurface,
                                    labelColor = OtgTextDim
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = settings.streamQuality == quality,
                                    borderColor = OtgHairline2,
                                    selectedBorderColor = OtgAccent
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // About Section
            SettingsSectionTitle("About")

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = OtgSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OtgHairline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsInfoRow(label = "App", value = "OTG USB Camera")
                    HorizontalDivider(color = OtgHairline, thickness = 0.5.dp)
                    SettingsInfoRow(label = "Version", value = "1.0.0")
                    HorizontalDivider(color = OtgHairline, thickness = 0.5.dp)
                    SettingsInfoRow(label = "Protocol", value = "RTMP / RTSP")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        color = OtgTextMute,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OtgTextDim, fontSize = 14.sp)
        Text(value, color = OtgText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
