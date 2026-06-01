package uz.toshmatov.otg_usb_camera.ui.main

import android.content.SharedPreferences
import androidx.core.content.edit
import uz.toshmatov.strem_lib.StreamConfig

data class StreamSettings(
    val rtmpUrl: String = "",
    val videoWidth: Int = 1280,
    val videoHeight: Int = 720,
    val fps: Int = 30,
    val videoBitrateKbps: Int = 2500,
    val audioBitrateKbps: Int = 128,
    val autoReconnect: Boolean = true,
    val adaptiveBitrate: Boolean = false,
    val hardwareEncoder: Boolean = false,
    val preferredFacing: Int = 1  // 1=BACK, 0=FRONT
) {
    val resolutionLabel: String
        get() = RESOLUTION_OPTIONS.find { it.first == videoWidth && it.second == videoHeight }?.third
            ?: "$videoWidth × $videoHeight"

    val fpsLabel: String get() = "$fps fps"
    val videoBitrateLabel: String get() = "$videoBitrateKbps kbps"
    val audioBitrateLabel: String get() = "$audioBitrateKbps kbps"

    fun toStreamConfig() = StreamConfig(
        videoWidth = videoWidth,
        videoHeight = videoHeight,
        fps = fps,
        videoBitrateKbps = videoBitrateKbps,
        audioBitrateKbps = audioBitrateKbps,
        preferredFacing = preferredFacing
    )

    companion object {
        const val PREFS_NAME = "stream_settings"

        val RESOLUTION_OPTIONS = listOf(
            Triple(640, 480, "640 × 480"),
            Triple(1280, 720, "1280 × 720"),
            Triple(1920, 1080, "1920 × 1080")
        )
        val FPS_OPTIONS = listOf(15, 24, 30, 60)
        val VIDEO_BITRATE_OPTIONS = listOf(500, 1000, 2500, 4000, 8000)
        val AUDIO_BITRATE_OPTIONS = listOf(64, 128, 192)

        fun fromPrefs(prefs: SharedPreferences) = StreamSettings(
            rtmpUrl = prefs.getString("rtmp_url", "") ?: "",
            videoWidth = prefs.getInt("video_width", 1280),
            videoHeight = prefs.getInt("video_height", 720),
            fps = prefs.getInt("fps", 30),
            videoBitrateKbps = prefs.getInt("video_bitrate_kbps", 2500),
            audioBitrateKbps = prefs.getInt("audio_bitrate_kbps", 128),
            autoReconnect = prefs.getBoolean("auto_reconnect", true),
            adaptiveBitrate = prefs.getBoolean("adaptive_bitrate", false),
            hardwareEncoder = prefs.getBoolean("hardware_encoder", false),
            preferredFacing = prefs.getInt("preferred_facing", 1)
        )

        fun saveToPrefs(settings: StreamSettings, prefs: SharedPreferences) {
            prefs.edit {
                putString("rtmp_url", settings.rtmpUrl)
                putInt("video_width", settings.videoWidth)
                putInt("video_height", settings.videoHeight)
                putInt("fps", settings.fps)
                putInt("video_bitrate_kbps", settings.videoBitrateKbps)
                putInt("audio_bitrate_kbps", settings.audioBitrateKbps)
                putBoolean("auto_reconnect", settings.autoReconnect)
                putBoolean("adaptive_bitrate", settings.adaptiveBitrate)
                putBoolean("hardware_encoder", settings.hardwareEncoder)
                putInt("preferred_facing", settings.preferredFacing)
            }
        }
    }
}
