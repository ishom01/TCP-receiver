package id.ishom.mediareceiver

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.net.ServerSocket
import java.net.Socket


class MediaStreamerServer(val context: Context, val port: Int) {

    val frequency = 44100
    val channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO
    val audioEncoding = AudioFormat.ENCODING_PCM_16BIT

    var isRecording: Boolean= false

    var recordBufSize: Int = 0
    var audioRecord: AudioRecord
    lateinit var serverSocket: ServerSocket
    lateinit var socket: Socket

    init {
        recordBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, recordBufSize)

        try {
            serverSocket = ServerSocket(port)
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent()
                .setAction("id.ishom.mediareceiver.ERROR")
                .putExtra("msg", e.toString())
            context.sendBroadcast(intent)
        }

        object : Thread() {
            var buffer = ByteArray(recordBufSize)
            override fun run() {
                try {
                    socket = serverSocket.accept()
                } catch (e: Exception) {
                    e.printStackTrace()
                    val intent = Intent()
                        .setAction("id.ishom.mediareceiver.ERROR")
                        .putExtra("msg", e.toString())
                    context.sendBroadcast(intent)
                    return
                }
                audioRecord.startRecording()
                isRecording = true
                while (isRecording) {
                    val readSize = audioRecord.read(buffer, 0, recordBufSize)
                    try {
                        socket.getOutputStream().write(buffer, 0, readSize)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val intent = Intent()
                            .setAction("id.ishom.mediareceiver.ERROR")
                            .putExtra("msg", e.toString())
                        context.sendBroadcast(intent)
                        break
                    }
                }
                audioRecord.stop()
                try {
                    socket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    val intent = Intent()
                        .setAction("id.ishom.mediareceiver.ERROR")
                        .putExtra("msg", e.toString())
                    context.sendBroadcast(intent)
                    return
                }
            }
        }.start()
    }

    fun stop() {
        isRecording = false
        try {
            serverSocket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}