package uz.toshmatov.strem_lib

/**
 * Stream parametrlari — StreamService.startStreamRtp() ga uzatiladi.
 * facing: 0 = FRONT (CameraCharacteristics.LENS_FACING_FRONT)
 *         1 = BACK  (CameraCharacteristics.LENS_FACING_BACK)
 */
data class StreamConfig(
    val videoWidth: Int = 1280,
    val videoHeight: Int = 720,
    val fps: Int = 30,
    val videoBitrateKbps: Int = 2500,
    val audioBitrateKbps: Int = 128,
    val preferredFacing: Int = 1  // 1 = BACK (default)
)
