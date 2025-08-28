package uz.toshmatov.strem_lib

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import com.pedro.encoder.Frame
import com.pedro.encoder.audio.AudioEncoder
import com.pedro.encoder.audio.GetAacData
import com.pedro.encoder.input.audio.GetMicrophoneData
import com.pedro.encoder.input.audio.MicrophoneManager
import com.pedro.encoder.input.audio.MicrophoneManagerManual
import com.pedro.encoder.input.audio.MicrophoneMode
import com.pedro.encoder.input.video.GetCameraData
import com.pedro.encoder.utils.CodecUtil
import com.pedro.encoder.video.FormatVideoEncoder
import com.pedro.encoder.video.GetVideoData
import com.pedro.encoder.video.VideoEncoder
import com.pedro.rtplibrary.view.GlInterface
import com.pedro.rtplibrary.view.LightOpenGlView
import com.pedro.rtplibrary.view.OffScreenGlThread
import com.pedro.rtplibrary.view.OpenGlView
import com.serenegiant.usb.UVCCamera
import java.io.IOException
import java.nio.ByteBuffer

abstract class USBBaseNew : GetAacData, GetCameraData, GetVideoData, GetMicrophoneData {

    companion object {
        private const val TAG = "Camera1Base"
    }

    private val context: Context
    protected lateinit var videoEncoder: VideoEncoder
    private lateinit var microphoneManager: MicrophoneManager
    private lateinit var audioEncoder: AudioEncoder
    private var glInterface: GlInterface? = null

    private var streaming = false
    private var videoEnabled = true

    // record
    private var mediaMuxer: MediaMuxer? = null
    private var videoTrack = -1
    private var audioTrack = -1
    private var recording = false
    private var canRecord = false
    private var onPreview = false
    private var videoFormat: MediaFormat? = null
    private var audioFormat: MediaFormat? = null

    constructor(openGlView: OpenGlView) {
        context = openGlView.context
        glInterface = openGlView.apply { init() }
        init()
    }

    constructor(lightOpenGlView: LightOpenGlView) {
        context = lightOpenGlView.context
        glInterface = lightOpenGlView.apply { init() }
        init()
    }

    constructor(context: Context) {
        this.context = context
        glInterface = OffScreenGlThread(context).apply { init() }
        init()
    }

    private fun init() {
        videoEncoder = VideoEncoder(this)
        setMicrophoneMode(MicrophoneMode.SYNC)
    }

    abstract fun setAuthorization(user: String, password: String)

    fun prepareVideo(
        width: Int,
        height: Int,
        fps: Int,
        bitrate: Int,
        iFrameInterval: Int,
        rotation: Int,
        uvcCamera: UVCCamera?
    ): Boolean {
        if (onPreview) {
            stopPreview(uvcCamera)
            onPreview = true
        }
        return videoEncoder.prepareVideoEncoder(
            width,
            height,
            fps,
            bitrate,
            rotation,
            iFrameInterval,
            FormatVideoEncoder.SURFACE
        )
    }

    fun prepareVideo(
        width: Int,
        height: Int,
        fps: Int,
        bitrate: Int,
        rotation: Int,
        uvcCamera: UVCCamera?
    ): Boolean = prepareVideo(width, height, fps, bitrate, 2, rotation, uvcCamera)

    protected abstract fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int)

    fun prepareAudio(
        bitrate: Int,
        sampleRate: Int,
        isStereo: Boolean,
        echoCanceler: Boolean,
        noiseSuppressor: Boolean
    ): Boolean {
        microphoneManager.createMicrophone(sampleRate, isStereo, echoCanceler, noiseSuppressor)
        prepareAudioRtp(isStereo, sampleRate)
        return audioEncoder.prepareAudioEncoder(
            bitrate,
            sampleRate,
            isStereo,
            microphoneManager.maxInputSize
        )
    }

    fun prepareAudio(): Boolean = prepareAudio(64 * 1024, 32000, true, false, false)

    fun setForce(forceVideo: CodecUtil.Force, forceAudio: CodecUtil.Force) {
        videoEncoder.setForce(forceVideo)
        audioEncoder.setForce(forceAudio)
    }

    @Throws(IOException::class)
    fun startRecord(uvcCamera: UVCCamera, path: String) {
        mediaMuxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        recording = true
        if (!streaming) {
            startEncoders(uvcCamera)
        } else if (videoEncoder.isRunning) {
            resetVideoEncoder()
        }
    }

    fun stopRecord(uvcCamera: UVCCamera) {
        recording = false
        mediaMuxer?.let {
            if (canRecord) {
                it.stop()
                it.release()
                canRecord = false
            }
            mediaMuxer = null
        }
        videoTrack = -1
        audioTrack = -1
        if (!streaming) stopStream(uvcCamera)
    }

    fun startPreview(uvcCamera: UVCCamera?, width: Int, height: Int) {
        if (!isStreaming() && !onPreview && glInterface !is OffScreenGlThread) {
            glInterface?.apply {
                setEncoderSize(width, height)
                setRotation(0)
                start()
            }
            uvcCamera?.setPreviewTexture(glInterface?.surfaceTexture)
            uvcCamera?.startPreview()
            onPreview = true
        } else {
            Log.e(TAG, "Streaming or preview started, ignored")
        }
    }

    fun stopPreview(uvcCamera: UVCCamera?) {
        if (!isStreaming() && onPreview && glInterface !is OffScreenGlThread) {
            glInterface?.stop()
            uvcCamera?.stopPreview()
            onPreview = false
        } else {
            Log.e(TAG, "Streaming or preview stopped, ignored")
        }
    }

    protected abstract fun startStreamRtp(url: String)

    fun startStream(uvcCamera: UVCCamera?, url: String) {
        streaming = true
        if (!recording) {
            startEncoders(uvcCamera)
        } else {
            resetVideoEncoder()
        }
        startStreamRtp(url)
        onPreview = true
    }

    private fun startEncoders(uvcCamera: UVCCamera?) {
        videoEncoder.start()
        audioEncoder.start()
        microphoneManager.start()

        uvcCamera?.stopPreview()
        glInterface?.stop()
        glInterface?.apply {
            setEncoderSize(videoEncoder.width, videoEncoder.height)
            setRotation(0)
            start()
            videoEncoder.inputSurface?.let { addMediaCodecSurface(it) }
        }
        uvcCamera?.setPreviewTexture(glInterface?.surfaceTexture)
        uvcCamera?.startPreview()
        onPreview = true
    }

    private fun resetVideoEncoder() {
        glInterface?.removeMediaCodecSurface()
        videoEncoder.reset()
        videoEncoder.inputSurface?.let { glInterface?.addMediaCodecSurface(it) }
    }

    protected abstract fun stopStreamRtp()

    fun stopStream(uvcCamera: UVCCamera?) {
        if (streaming) {
            streaming = false
            stopStreamRtp()
        }
        if (!recording) {
            microphoneManager.stop()
            glInterface?.apply {
                removeMediaCodecSurface()
                if (this is OffScreenGlThread) {
                    stop()
                    uvcCamera?.stopPreview()
                }
            }
            videoEncoder.stop()
            audioEncoder.stop()
            videoFormat = null
            audioFormat = null
        }
    }

    fun disableAudio() = microphoneManager.mute()
    fun enableAudio() = microphoneManager.unMute()
    fun isAudioMuted(): Boolean = microphoneManager.isMuted
    fun isVideoEnabled(): Boolean = videoEnabled
    fun getBitrate(): Int = videoEncoder.bitRate
    fun getResolutionValue(): Int = videoEncoder.width * videoEncoder.height
    fun getStreamWidth(): Int = videoEncoder.width
    fun getStreamHeight(): Int = videoEncoder.height

    fun getGlInterface(): GlInterface =
        glInterface ?: throw RuntimeException("You can't do it. You are not using Opengl")

    fun setVideoBitrateOnFly(bitrate: Int) = videoEncoder.setVideoBitrateOnFly(bitrate)
    fun setLimitFPSOnFly(fps: Int) = videoEncoder.apply { this.fps = fps }
    fun isStreaming(): Boolean = streaming
    fun isOnPreview(): Boolean = onPreview
    fun isRecording(): Boolean = recording

    protected abstract fun getAacDataRtp(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo)
    override fun getAacData(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (recording && canRecord) {
            mediaMuxer?.writeSampleData(audioTrack, aacBuffer, info)
        }
        if (streaming) getAacDataRtp(aacBuffer, info)
    }

    protected abstract fun onSpsPpsVpsRtp(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer)

    /*override fun onSpsPpsVps(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer) {
        if (streaming) onSpsPpsVpsRtp(sps, pps, vps)
    }*/
    override fun onSpsPpsVps(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
        if (streaming) {
            val safeVps = vps ?: ByteBuffer.allocate(0) // VPS bo'lmasa bo'sh ByteBuffer
            onSpsPpsVpsRtp(sps, pps, safeVps)
        }
    }


    protected abstract fun getH264DataRtp(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo)
    override fun getVideoData(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (recording) {
            if (info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME &&
                !canRecord &&
                videoFormat != null &&
                audioFormat != null
            ) {
                videoTrack = mediaMuxer!!.addTrack(videoFormat!!)
                audioTrack = mediaMuxer!!.addTrack(audioFormat!!)
                mediaMuxer!!.start()
                canRecord = true
            }
            if (canRecord) {
                mediaMuxer?.writeSampleData(videoTrack, h264Buffer, info)
            }
        }
        if (streaming) getH264DataRtp(h264Buffer, info)
    }

    override fun inputPCMData(frame: Frame) {
        audioEncoder.inputPCMData(frame)
    }

    override fun inputYUVData(frame: Frame) {
        videoEncoder.inputYUVData(frame)
    }

    override fun onVideoFormat(mediaFormat: MediaFormat) {
        videoFormat = mediaFormat
    }

    override fun onAudioFormat(mediaFormat: MediaFormat) {
        audioFormat = mediaFormat
    }

    fun setMicrophoneMode(microphoneMode: MicrophoneMode) {
        when (microphoneMode) {
            MicrophoneMode.SYNC -> {
                microphoneManager = MicrophoneManagerManual()
                audioEncoder = AudioEncoder(this)
                audioEncoder.setGetFrame((microphoneManager as MicrophoneManagerManual).getGetFrame())
                audioEncoder.setTsModeBuffer(false)
            }

            MicrophoneMode.ASYNC -> {
                microphoneManager = MicrophoneManager(this)
                audioEncoder = AudioEncoder(this)
                audioEncoder.setTsModeBuffer(false)
            }

            MicrophoneMode.BUFFER -> {
                microphoneManager = MicrophoneManager(this)
                audioEncoder = AudioEncoder(this)
                audioEncoder.setTsModeBuffer(true)
            }
        }
    }

    fun replaceView(context: Context, uvcCamera: UVCCamera?) {
        replaceGlInterface(OffScreenGlThread(context), uvcCamera)
    }

    fun replaceView(openGlView: OpenGlView, uvcCamera: UVCCamera?) {
        replaceGlInterface(openGlView, uvcCamera)
    }

    fun replaceView(lightOpenGlView: LightOpenGlView, uvcCamera: UVCCamera?) {
        replaceGlInterface(lightOpenGlView, uvcCamera)
    }

    private fun replaceGlInterface(glInterface: GlInterface, uvcCamera: UVCCamera?) {
        this.glInterface?.let {
            if (isStreaming() || isRecording() || isOnPreview()) {
                uvcCamera?.stopPreview()
                it.removeMediaCodecSurface()
                it.stop()
                this.glInterface = glInterface
                this.glInterface!!.init()
                this.glInterface!!.setEncoderSize(videoEncoder.width, videoEncoder.height)
                this.glInterface!!.setRotation(0)
                this.glInterface!!.start()
                if (isStreaming() || isRecording()) {
                    this.glInterface!!.addMediaCodecSurface(videoEncoder.inputSurface)
                }
                uvcCamera?.setPreviewTexture(glInterface.surfaceTexture)
                uvcCamera?.startPreview()
            } else {
                this.glInterface = glInterface
            }
        }
    }
}
