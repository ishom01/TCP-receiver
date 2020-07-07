package id.ishom.mediareceiver;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.net.Socket;

public class NewMediaStreamer {
//    static final int frequency = 44100;
    static final int frequency = 16_000;
    static final int channelConfiguration = AudioFormat.CHANNEL_OUT_STEREO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isPlaying;
    int playBufSize;
    Socket connfd;
    AudioTrack audioTrack;

    public NewMediaStreamer(final Context ctx, final String ip, final int port) {
        playBufSize=AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency, channelConfiguration, audioEncoding, playBufSize * 2, AudioTrack.MODE_STREAM);
        audioTrack.setStereoVolume(1f, 1f);

        new Thread() {
            byte[] buffer = new byte[4096];
            public void run() {
                try { connfd = new Socket(ip, port); }
                catch (Exception e) {
                    e.printStackTrace();
                    Intent intent = new Intent()
                            .setAction("id.ishom.mediareceiver.ERROR")
                            .putExtra("msg", e.toString());
                    ctx.sendBroadcast(intent);
                    return;
                }
                audioTrack.play();
                isPlaying = true;
                while (isPlaying) {
                    int readSize = 0;
                    try { readSize = connfd.getInputStream().read(buffer); }
                    catch (Exception e) {
                        e.printStackTrace();
                        Intent intent = new Intent()
                                .setAction("id.ishom.mediareceiver.ERROR")
                                .putExtra("msg", e.toString());
                        ctx.sendBroadcast(intent);
                        break;
                    }
                    short[] sbuffer = new short[1024];
                    for(int i = 0; i < buffer.length; i++)
                    {

                        int asInt = 0;
                        asInt = ((buffer[i] & 0xFF))
                                | ((buffer[i+1] & 0xFF) << 8)
                                | ((buffer[i+2] & 0xFF) << 16)
                                | ((buffer[i+3] & 0xFF) << 24);
                        float asFloat = 0;
                        asFloat = Float.intBitsToFloat(asInt);
                        int k=0;
                        try{k = i/4;} catch(Exception e){}
                        sbuffer[k] = (short)(asFloat * Short.MAX_VALUE);

                        i=i+3;
                    }
                    audioTrack.write(sbuffer, 0, readSize);
                }
                audioTrack.stop();
                try { connfd.close(); }
                catch (Exception e) { e.printStackTrace(); }
            }
        }.start();
    }

    public void stop() {
        isPlaying = false;
    }

    public void setVolume(float lvol, float rvol) {
        audioTrack.setStereoVolume(lvol, rvol);
    }
}
