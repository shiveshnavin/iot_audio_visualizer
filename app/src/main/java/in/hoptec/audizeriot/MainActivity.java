package in.hoptec.audizeriot;

import android.Manifest;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends SoundRecordAndAnalysisActivity {

    AudizerRecorder audizerRecorder=new AudizerRecorder(new OnAudioRecieved() {
        @Override
        public void onAudio(short[] audioBuffer) {

            processAudioBuf(audioBuffer);

        }
    },250);
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
       // audizerRecorder.recordAudio();

    }


    @Override
    protected void onDestroy() {
     //   audizerRecorder.stop();
        super.onDestroy();

    }

    public static int calculate(int sampleRate, short [] audioData){

        int numSamples = audioData.length;
        int numCrossing = 0;
        for (int p = 0; p < numSamples-1; p++)
        {
            if ((audioData[p] > 0 && audioData[p + 1] <= 0) ||
                    (audioData[p] < 0 && audioData[p + 1] >= 0))
            {
                numCrossing++;
            }
        }

        float numSecondsRecorded = (float)numSamples/(float)sampleRate;
        float numCycles = numCrossing/2;
        float frequency = numCycles/numSecondsRecorded;

        return (int)frequency;
    }



    private void processAudioBuf (short [] data ){

        ;

        Log.e("Freq"," "+calculate(audizerRecorder.SAMPLE_RATE,data)+" Hz");
    };
}
