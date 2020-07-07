package id.ishom.mediareceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_sample.*
import java.net.NetworkInterface
import java.net.SocketException


class SampleActivity : AppCompatActivity() {

    var mediaServerClient: UpdateMediaClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        myIpAddressTextView.append(getLocalIPAddress())

        volumeSeekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
            override fun onStartTrackingTouch(p0: SeekBar) {}
            override fun onStopTrackingTouch(seekbar: SeekBar) {
                val newVolume = seekbar.progress.toDouble() / seekbar.max.toDouble()
                if (mediaServerClient != null) {
                    mediaServerClient?.setVolume(newVolume.toFloat(), newVolume.toFloat())
                }
            }

        })

        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "id.ishom.mediareceiver.ERROR") {
                    myIpAddressTextView.append("Error ${intent.getStringExtra("message")}\n")
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction("id.ishom.mediareceiver.ERROR")
        registerReceiver(receiver, filter)
    }

    fun stopCLicked(view: View) {
        if (mediaServerClient != null) {
            myIpAddressTextView.append("Stop client\n")
            mediaServerClient?.stop()
        }
    }

    fun playClicked(view: View) {
        val ip = ipEditText.text.toString()
        val port = portEditText.text.toString().toInt()
        myIpAddressTextView.append("Starting client, $ip : $port\n")
        mediaServerClient = UpdateMediaClient(this, ip, port)
    }

    fun getLocalIPAddress(): String {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            if (networkInterfaces.hasMoreElements()) {
                for (networkInterface in networkInterfaces) {
                    for (inetAddress in networkInterface.inetAddresses) {
                        if (inetAddress.isLoopbackAddress) {
                            return inetAddress.hostAddress.toString() + "\n"
                        }
                    }
                }
                return ""
            } else {
                return "IP not found\n"
            }
        } catch (e: SocketException) {
            e.printStackTrace()
            return "IP not found\n"
        }
    }
}