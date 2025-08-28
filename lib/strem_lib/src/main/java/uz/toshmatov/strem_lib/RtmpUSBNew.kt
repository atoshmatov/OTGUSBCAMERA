package uz.toshmatov.strem_lib

import android.content.Context
import android.media.MediaCodec
import com.pedro.rtmp.rtmp.RtmpClient
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.view.LightOpenGlView
import com.pedro.rtplibrary.view.OpenGlView
import java.nio.ByteBuffer


class RtmpUSBNew : USBBaseNew {

    private val rtmpClient: RtmpClient

    constructor(openGlView: OpenGlView, connectChecker: ConnectCheckerRtmp) : super(openGlView) {
        rtmpClient = RtmpClient(connectChecker)
    }

    constructor(lightOpenGlView: LightOpenGlView, connectChecker: ConnectCheckerRtmp) : super(
        lightOpenGlView
    ) {
        rtmpClient = RtmpClient(connectChecker)
    }

    constructor(context: Context, connectChecker: ConnectCheckerRtmp) : super(context) {
        rtmpClient = RtmpClient(connectChecker)
    }

    override fun setAuthorization(user: String, password: String) {
        rtmpClient.setAuthorization(user, password)
    }

    override fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int) {
        rtmpClient.setAudioInfo(sampleRate, isStereo)
    }

    override fun startStreamRtp(url: String) {
        if (videoEncoder.rotation == 90 || videoEncoder.rotation == 270) {
            rtmpClient.setVideoResolution(videoEncoder.height, videoEncoder.width)
        } else {
            rtmpClient.setVideoResolution(videoEncoder.width, videoEncoder.height)
        }
        rtmpClient.connect(url)
    }

    override fun stopStreamRtp() {
        rtmpClient.disconnect()
    }

    override fun getAacDataRtp(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        rtmpClient.sendAudio(aacBuffer, info)
    }

    override fun onSpsPpsVpsRtp(
        sps: ByteBuffer,
        pps: ByteBuffer,
        vps: ByteBuffer
    ) {
        rtmpClient.setVideoInfo(sps, pps, vps)
    }

    override fun getH264DataRtp(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        rtmpClient.sendVideo(h264Buffer, info)
    }
}
