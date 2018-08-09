package in.hoptec.audizeriot;

import android.Manifest;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity {

    ArrayList<Float> freqs=new ArrayList<>();
    AudizerRecorder audizerRecorder=new AudizerRecorder(new OnAudioRecieved() {
        @Override
        public void onAudio(short[] audioBuffer) {

            processAudioBuf(audioBuffer);

        }
    },250);
    boolean isOn=true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(this);
        setContentView(R.layout.activity_main);
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
       // audizerRecorder.recordAudio();

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.e("Pitch",""+curPitch);
                        curPitch=pitchInHz;

                        if(curPitch!=-1 )
                        {
                            if( Math.abs(curPitch-prevPitch)<20)
                            {
                                Log.e("Repeat Pitch",""+curPitch);
                                updateFreq(-1);
                                updateFreq(curPitch);

                                return;
                            }
                            freqs.add(curPitch);
                            updateFreq(curPitch);
                        }

                        prevPitch=curPitch;

                    }
                });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        rec=new Thread(dispatcher,"Audio Dispatcher");
        rec.start();;

    }
    Thread rec;
    float curPitch=0;
    float prevPitch=0;

    boolean reqInProgress=false;
    private void updateFreq (float pitch ){

/*
        final Timer tm=new Timer();
        tm.schedule(new TimerTask() {
            @Override
            public void run() {
                reqInProgress=false;
            }
        },1000);*/


        if(true)
            return;

        float h=(pitch/400)*360;
        JSONObject jsonObject= null;
        try {
            jsonObject = new JSONObject();
            if(h<10)
                return;
            jsonObject.put("h",h);
            jsonObject.put("s",1);
            jsonObject.put("v",1);
            Log.e("API REQ",jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        reqInProgress=true;

        AndroidNetworking.post("http://192.168.4.1/rpc/color").addJSONObjectBody(jsonObject).build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                   //   tm.cancel();
                        reqInProgress=false;
                        Log.e("API",response);
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    
    };
    @Override
    protected void onDestroy() {

        isOn=false;
        for(int i=0;i<freqs.size();i++)
        {
            Log.d(""+i,""+freqs.get(i));
        }
       // rec.stop();


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
