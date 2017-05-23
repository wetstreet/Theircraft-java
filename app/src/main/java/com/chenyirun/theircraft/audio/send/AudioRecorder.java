package com.chenyirun.theircraft.audio.send;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

public class AudioRecorder implements Runnable {
    public static final String TAG = "AudioRecorder";
    private boolean isRecording = false;
    private AudioRecord audioRecord;

    private int audioBufSize;
    private byte[] samples;
    private int bufferRead;
    private static final int bufferSize = 480;

    private String ip;

    public AudioRecorder(String ip){
        this.ip = ip;
    }

    public void startRecording() throws Exception {
        audioBufSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (audioBufSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "audioBufSize error");
            return;
        }
        samples = new byte[audioBufSize];
        // 初始化recorder
        if (null == audioRecord) {
            //audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioBufSize);
            audioRecord = findAudioRecord();
            if (audioRecord == null){
                Log.e(TAG, "startRecording: AudioRecord initialization failed");
                throw new Exception();
            }
        }
        new Thread(this).start();
    }

    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        Log.d(TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                + channelConfig);
                        audioBufSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (audioBufSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, channelConfig, audioFormat, audioBufSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, rate + "Exception, keep trying.",e);
                    }
                }
            }
        }
        return null;
    }

    public void stopRecording() {
        this.isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void run() {
        AudioSender sender = new AudioSender(ip);
        sender.startSending();
        Log.i(TAG, "run: start recording");
        audioRecord.startRecording();

        this.isRecording = true;
        while (isRecording) {
            bufferRead = audioRecord.read(samples, 0, bufferSize);
            if (bufferRead > 0) {
                // add data to encoder
                sender.addData(samples, bufferRead);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "run: end recording");
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        sender.stopSending();
    }
}
