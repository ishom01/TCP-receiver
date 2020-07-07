package id.ishom.mediareceiver

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.net.Socket


class MediaServerClient(val context: Context, val ip: String, val port: Int) {

//    val frequency = 10_000
    val frequency = 16_000
//    val channelConfiguration = AudioFormat.CHANNEL_OUT_STEREO
    val channelConfiguration = AudioFormat.CHANNEL_OUT_MONO
    val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
    val minBuffSize = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding)

    var isPlaying = false
    lateinit var socket: Socket
    var audioTrack: AudioTrack

    init {
        audioTrack = AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency, channelConfiguration, audioEncoding, minBuffSize * 2, AudioTrack.MODE_STREAM)
        audioTrack.setStereoVolume(1f, 1f)

        object : Thread() {
//            var buffer = ByteArray(4096)
            override fun run() {
                try {
                    socket = Socket(ip, port)
                } catch (e: Exception) {
                    e.printStackTrace()
                    val intent = Intent()
                        .setAction("id.ishom.mediareceiver.ERROR")
                        .putExtra("message", e.toString())
                    context.sendBroadcast(intent)
                    return
                }

                audioTrack.play()
                isPlaying = true
                val inputStream = socket.getInputStream()
                val buffer = ByteArray(1024 * 16)

                while (isPlaying) {
//                    val readSize = try {
//                        socket.getInputStream().read(buffer)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        val intent = Intent()
//                            .setAction("id.ishom.mediareceiver.ERROR")
//                            .putExtra("message", e.toString())
//                        context.sendBroadcast(intent)
//                        break
//                    }
//                    audioTrack.write(buffer, 0, readSize)
                    var byteCount = inputStream.read(buffer)
                    if (byteCount != -1) {
                        audioTrack.write(buffer, 0, byteCount)
                        audioTrack.flush()
                    }
                }
                audioTrack.stop()
                audioTrack.release()
                try {
                    socket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    val intent = Intent()
                        .setAction("id.ishom.mediareceiver.ERROR")
                        .putExtra("message", e.toString())
                    context.sendBroadcast(intent)
                    return
                }
            }
        }.start()
    }

    fun stop() {
        isPlaying = false
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        audioTrack.setStereoVolume(leftVolume, rightVolume)
    }
}