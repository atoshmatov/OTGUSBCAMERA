package uz.toshmatov.otg_usb_camera.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import com.pedro.rtmp.rtmp.RtmpClient
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class PhoneCameraStreamer(
    private val context: Context,
    private val scope: CoroutineScope,
    private val connectChecker: ConnectCheckerRtmp
) {

    private var camera: Camera? = null
    private var rtmpClient: RtmpClient? = null
    private var isStreaming = false
    private var cameraId = 0 // Front camera

    companion object {
        private const val TAG = "PhoneCameraStreamer"
    }

    fun startStream(endpoint: String) {
        scope.launch(Dispatchers.IO) {
            try {
                if (rtmpClient == null) {
                    rtmpClient = RtmpClient(connectChecker)
                }

                // Setup camera
                if (camera == null) {
                    openCamera()
                }

                // Setup RTMP
                rtmpClient?.apply {
                    setVideoResolution(1280, 720)
                    connect(endpoint)
                }

                isStreaming = true
                Log.d(TAG, "Phone camera streaming started")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting stream: ${e.message}")
                connectChecker.onConnectionFailedRtmp("Phone camera stream failed: ${e.message}")
            }
        }
    }

    fun stopStream() {
        scope.launch(Dispatchers.IO) {
            try {
                isStreaming = false
                rtmpClient?.disconnect()
                releaseCamera()
                Log.d(TAG, "Phone camera streaming stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping stream: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        try {
            val numCameras = Camera.getNumberOfCameras()
            if (numCameras == 0) {
                Log.e(TAG, "No cameras available")
                return
            }

            // Find front camera
            val cameraInfo = Camera.CameraInfo()
            for (i in 0 until numCameras) {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraId = i
                    break
                }
            }

            camera = Camera.open(cameraId)

            camera?.apply {
                val params = parameters

                // Set preview size
                val previewSizes = params.supportedPreviewSizes
                val previewSize = previewSizes.maxByOrNull { it.width * it.height } ?: params.previewSize
                params.setPreviewSize(previewSize.width, previewSize.height)

                // Set frame rate
                params.setPreviewFrameRate(25)
                params.previewFormat = ImageFormat.NV21

                parameters = params

                // Set dummy surface texture
                val texture = SurfaceTexture(0)
                setPreviewTexture(texture)
                startPreview()

                // Set preview callback
                setPreviewCallbackWithBuffer { data, cam ->
                    if (isStreaming && data != null) {
                        Log.d(TAG, "Frame received: ${data.size} bytes")
                    }
                    cam?.addCallbackBuffer(data)
                }

                // Allocate callback buffer
                val previewBufferSize = (previewSize.width * previewSize.height * 1.5).toInt()
                addCallbackBuffer(ByteArray(previewBufferSize))
            }

            Log.d(TAG, "Camera opened - ID: $cameraId")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}")
        }
    }

    private fun releaseCamera() {
        try {
            camera?.apply {
                stopPreview()
                setPreviewCallbackWithBuffer(null)
                release()
            }
            camera = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing camera: ${e.message}")
        }
    }

    fun release() {
        stopStream()
    }
}
