package id.ishom.mediareceiver

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Process
import android.util.Log
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException


class UpdateMediaClient(val context: Context, val ip: String, val port: Int) {

    var isPlaying = false
    var audioTrack :AudioTrack? = null

//    val frequency = 22_050
    val frequency = 16_000
    val channel = AudioFormat.CHANNEL_OUT_MONO
    val channelEncoding = AudioFormat.ENCODING_PCM_8BIT

    init {
        object : Thread() {
            override fun run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

                val socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 0)
                val inputStream = TTSInputStream(socket.getInputStream())

                var bufferSize = AudioTrack.getMinBufferSize(frequency, channel, channelEncoding)

                if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                    bufferSize = frequency * 2
                }

                var readBytes: Int
                bufferSize *= 2
                val buffer = ByteArray(bufferSize)
                val audioTrack = AudioTrack(
                    AudioManager.STREAM_VOICE_CALL,
                    frequency,
                    channel,
                    channelEncoding,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
                audioTrack.play()

                isPlaying = true
                while (isPlaying) {
                    try {
                        if (inputStream.available() > 0) {
                            readBytes = inputStream.readFullyUntilEof(buffer, 0, buffer.size)
                            Log.d("Bytes Count", "Read $readBytes bytes.");
                            audioTrack.write(buffer, 0, readBytes)
                        }
                    } catch (s: SocketTimeoutException) {
                        println("Socket timed out!")
                        break
                    } catch (e: IOException) {
                        e.printStackTrace()
                        audioTrack.release()
                        inputStream.close()
                        socket.close()
                        break
                    }
                }
            }
        }.start()
    }

    fun stop() {
        isPlaying = false
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        audioTrack?.setStereoVolume(leftVolume, rightVolume)
    }
}