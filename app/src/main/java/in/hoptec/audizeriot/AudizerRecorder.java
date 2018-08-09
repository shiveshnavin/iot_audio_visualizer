package in.hoptec.audizeriot;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by shivesh on 9/8/18.
 */

public class AudizerRecorder {
    String  LOG_TAG="Audizer";
    final int SAMPLE_RATE = 44100; // The sampling rate
    boolean mShouldContinue; // Indicates if recording / playback should stop
    OnAudioRecieved onAudioRecieved;
    public short THRESHOLD=1000;
    long interval;
    public AudizerRecorder(OnAudioRecieved cbOnAudioRecieved,long interval)
    {
        onAudioRecieved=cbOnAudioRecieved;
        this.interval=interval;
    }
    public void stop()
    {
        mShouldContinue=false;
    }


    void recordAudio() {
        mShouldContinue=true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                // buffer size in bytes
                int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }

                short[] audioBuffer = new short[bufferSize / 2];

                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(LOG_TAG, "Audio Record can't initialize!");
                    return;
                }
                record.startRecording();

                Log.v(LOG_TAG, "Start recording Buf Size "+audioBuffer.length);

                long shortsRead = 0;
                while (mShouldContinue) {
                    int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
                    shortsRead += numberOfShort;

                    // Do something with the audioBuffer
                    short[] convertedValues = audioBuffer;
                    short max = convertedValues[0];

                    for (int i = 1; i < convertedValues.length; i++) {
                        if (convertedValues[i] > max) {
                            max = convertedValues[i];
                        }
                    }


                    //Log.v(LOG_TAG, "Max amp "+max);

                    if(max>THRESHOLD)
                     onAudioRecieved.onAudio(audioBuffer);
                    try {
                        if(interval>-1)
                            Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                record.stop();
                record.release();

                Log.v(LOG_TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
            }
        }).start();
    }

}
