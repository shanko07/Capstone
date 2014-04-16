package com.example.myfirstapp;

import Jama.Matrix;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;




//package com.android.audiorecordtest;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

import java.io.IOException;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;



import java.math.*;



public class MainActivity extends Activity {
/*
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

*/
	
	protected float[] fftResults;
	protected float[] timeDomain;
	float zeroCrossingRate;
	float rmsAmplitude;
	float [] prevSpectrum;
	float [] spectrum; // entire spectrum
	float []RHSspectrum; //absolute value of fft
	float totalEnergy; //total energy of the signal
	int below91; //bins below which 91 percent of signal energy is contained
	float runningSum;
	float spectralRolloff;
	float spectralCentroid;
	float spectralEntropy;
	float spectralFlux;
	int N;
	GraphViewSeries exampleSeries;
	GraphView graphView;	
	GraphViewData[] gViewData;
	
	
	private final Handler mHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    
	    	switch (msg.what){
	    	
	    	case 0:
	    		
	    		//Log.d("feature", "case 0");
	    		
	    		if(spectrum!=null)
	    		{
	    			prevSpectrum = spectrum;	    			
	    		}
	    	
	    	spectrum = (float[]) msg.obj;  //Acquire spectrum
	    	
	    	totalEnergy = 0;
	    	for(int i=0; i<spectrum.length; i++)  //calculate total energy
	    	{
	    		totalEnergy += spectrum[i]*spectrum[i];
	    	}
	    	//Log.d("SPR Debug", "totalEnergy: " + Float.toString(totalEnergy));
	    	
	    	
	    	RHSspectrum = new float[spectrum.length/2];   //create RHSspectrum
	    	for(int i=0; i<spectrum.length/2; i++)
	    	{
	    		RHSspectrum[i] = spectrum[i];
	    		//Log.d("SPR Debug", "RHSspectrum: " + Float.toString(RHSspectrum[i]));
	    	}	    	

	    	
	    	below91 = 0;
	    	runningSum = 0;
	    	for(int i=0;i<RHSspectrum.length;i++)
	    	{
	    		runningSum += RHSspectrum[i]*RHSspectrum[i];
	    		Log.d("SPR Debug", "runningSum: " + Float.toString(runningSum));
	    		Log.d("SPR Debug", "i: " + Integer.toString(i));
	    		//Log.d("SPR Debug", "totalEnergy: " + Float.toString(totalEnergy));
	    		if((runningSum*2)/totalEnergy >= .91)
	    		{
	    			//Log.d("feature", "runningSum: " + Float.toString(runningSum));
	    			//Log.d("feature", "totalEnergy: " + Float.toString(totalEnergy));
	    			below91=i;
	    			i=RHSspectrum.length;
	    			Log.d("SPR Debug", "below91: " + Integer.toString(below91));
	    		}
	    	}
	    	
	    	
	    	
	    	calculateSpectralRolloff();
	    	calculateSpectralCentroid();
	    	calculateSpectralEntropy();
	    	calculateSpectralFlux();
	    	
	    	if(prevSpectrum!=null){
	    	Classify carClassifier = new Classify(3, 0);
	    	
	    	double[][]  blah = new double[6][1];
	    	
	    	blah[0][0] = rmsAmplitude;
	    	blah[1][0] = zeroCrossingRate;
	    	blah[2][0] = spectralRolloff;
	    	blah[3][0] = spectralCentroid;
	    	blah[4][0] = spectralEntropy;
	    	blah[5][0] = spectralFlux;
	    	
	    			
			double d = carClassifier.classify(blah);
			
			Log.d("Residual", Double.toString(d));
			Log.d("Residual", "RMS: " + Float.toString(rmsAmplitude));
			Log.d("Residual", "ZCR: " + Float.toString(zeroCrossingRate));
			Log.d("Residual", "SPR: " + Float.toString(spectralRolloff));
			Log.d("Residual", "SPC: " + Float.toString(spectralCentroid));
			Log.d("Residual", "SPE: " + Float.toString(spectralEntropy));
			Log.d("Residual", "SPF: " + Float.toString(spectralFlux));
			
			
	    	}
	    	
	    	break;
	    	
	    	
	    	case 1:
	    		
	    		//Log.d("feature", "case 1");
	    		
	    		timeDomain = (float[]) msg.obj;
	    		//Log.d("feature", "after td assignment");
	    		N = msg.arg1;
	    		
	    		
	    		calculateRMSAmplitude();  // gets the RMS amplitude
	    		calculateZeroCrossing();  // gets the zero crossing rate
	    		
	    		
	    		break;
	    	
	    	default:
	    		//Log.d("feature", "Error not 0 or 1");
	    	
	    	}
	    
	    
	    }

		
	};
	   
	private void calculateZeroCrossing() 
	{
	int zeroCrossings = 0;
	
	for(int i=0; i<timeDomain.length-1; i++)
	{
		//Log.d("TD", Float.toString(timeDomain[i]));
		//Log.d("TD", Float.toString(timeDomain[i]));
		if(timeDomain[i] > 0 && timeDomain[i+1] < 0)
		{
			zeroCrossings++;
		}
		if(timeDomain[i] < 0 && timeDomain[i+1] > 0)
		{
			zeroCrossings++;
		}
		//Log.d("TD", Integer.toString(zeroCrossings));
	}
	
	//zeroCrossingRate = zeroCrossings/N;
	zeroCrossingRate = zeroCrossings;
	
	Log.d("feature", "ZCR: " + Float.toString(zeroCrossingRate));
	
	}
	
	
	private void calculateRMSAmplitude() {
		rmsAmplitude = 0;
		float sum = 0;
		for(int i=0; i<timeDomain.length; i++){
			sum += timeDomain[i]*timeDomain[i];
		}
		sum/=N;
		rmsAmplitude = (float) Math.sqrt(sum);
		
		Log.d("feature", "RMS: " + Float.toString(rmsAmplitude));
		
	}
	
	
	private void calculateSpectralRolloff() //Erroneous in all cases
	{
		spectralRolloff = (float) ((below91+1)*44100.0/spectrum.length);
		
		Log.d("feature", "SPR: " + Float.toString(spectralRolloff));
		
	}
	
	private void calculateSpectralCentroid() //Accurate in all cases except for a slight error probably due to float usage
	{
		float []RHSSpectNum = new float [RHSspectrum.length];
		for(int i=1;i<=RHSspectrum.length;i++){
			RHSSpectNum[i-1] = RHSspectrum[i-1] * i;
		}
		float sumN = 0;
		for(int i=0;i<RHSSpectNum.length;i++){
			sumN += RHSSpectNum[i];
		}
		float sumD = 0;
		for(int i=0;i<RHSspectrum.length;i++){
			sumD += RHSspectrum[i];
		}
		Log.d("SPC Debug", "sumN: " + Float.toString(sumN));
		Log.d("SPC Debug", "sumD: " + Float.toString(sumD));
		float centroid = sumN/ sumD;
		
		// fs = 44100 ------------------------------
		
		spectralCentroid = (44100 / spectrum.length) * centroid;
		
		Log.d("feature", "SPC: " + Float.toString(spectralCentroid));
		
	}
	
	private void calculateSpectralEntropy() //Accurate in all cases
	{
		float [] PSD = new float [spectrum.length];
		for(int i=0;i<spectrum.length;i++){
			PSD[i] = spectrum[i] * spectrum[i] / totalEnergy;
		}
		float [] PSDmultLog = new float [PSD.length];
		for(int i = 0; i<PSDmultLog.length;i++){
			PSDmultLog[i] = (float) Math.log(PSD[i]) / (float) Math.log(2) * (float) PSD[i];
		}
		float sum = 0;
		for(int i = 0; i<PSDmultLog.length;i++){
			sum += PSDmultLog[i];
		}
		sum = sum * -1;
		spectralEntropy = sum;	
		
		Log.d("feature", "SPE: " + Float.toString(spectralEntropy));
		
	}
	
	private void calculateSpectralFlux() //Innaccurate for the first iteration only
	{		
		
		if(prevSpectrum!=null){
		float [] diff = new float [spectrum.length];
		for (int i=0;i<diff.length;i++){
			diff[i] = spectrum[i] - prevSpectrum[i];
		}
		float sqSum = 0;
		for (int i=0;i<diff.length;i++){
			sqSum += diff[i] * diff[i];
		}
		spectralFlux = (float) Math.sqrt(sqSum); 
		
		Log.d("feature", "SPF: " + Float.toString(spectralFlux));
		}
	}
	
	
	    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	
	
	
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        Log.d("testapp", "start rec before audio source set");
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        Log.d("testapp", "after set");
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    public MainActivity() { //AudioRecordTest
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);     
               
        Audio a1 = new Audio(mHandler);
        
       
        

        LinearLayout ll = new LinearLayout(this);
        /*
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        mPlayButton = new PlayButton(this);
        ll.addView(mPlayButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        setContentView(ll);
        
        
        */
        
     // init example series data
        exampleSeries = new GraphViewSeries(new GraphViewData[] {
              new GraphViewData(1, 2.0d)
              , new GraphViewData(2, 1.5d)
              , new GraphViewData(3, 2.5d)
              , new GraphViewData(4, 1.0d)
        });
        
        Log.d("123", "alpha");
        
        GraphView graphView = new BarGraphView(
              this // context
              , "GraphViewDemo" // heading
        );
        
        Log.d("123", "beta");
        
        graphView.addSeries(exampleSeries); // data
        
        Log.d("123", "gamma");
         
        LinearLayout layout = (LinearLayout) findViewById(R.id.barGraph);
        
        Log.d("123", "delta");
        ll.addView(graphView);
        
        Log.d("123", "epsilon");
        
        setContentView(ll);
        
        
        
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
	
	
	
	
	
	
	
	

}


/*
int bins = 160;
int perbin = 1;
int sampfreq = 44100;


int N = msg.what;
//gViewData = new GraphViewData[fftResults.length];
gViewData = new GraphViewData[bins];
float[] temp = new float[bins];

/*
Context context = getApplicationContext();
CharSequence text = Integer.toString(fftResults.length);
int duration = Toast.LENGTH_SHORT;

Toast toast = Toast.makeText(context, text, duration);
toast.show();
*/



/*	
for(int i = 0; i < fftResults.length; i++)
{
gViewData[i] = new GraphViewData((8000/160)*i, fftResults[i]);
}
*/

/*
float max = 0;
float min = 0;

for(int i = 0; i < bins; i++)
{
	float sum = 0;
	for(int j = 0; j < perbin; j++)
	{
		sum += Math.abs(fftResults[perbin*i+j]);
	}
	//gViewData[i] = new GraphViewData(1000*(i+1), sum/20);
	temp[i] = sum/perbin;
}


//finding min and max
for(int i = 0; i < bins; i++)
{
	if(temp[i] > max)
	{
		max = temp[i];
	}
	if(temp[i] < min)
	{
		min = temp[i];
	}
}

//fixing the values between 0 and 10 for viewing purposes
float x = (max-min)/10;

float[] newtemp = new float[bins];

for(int i = 0; i < bins; i++)
{
	newtemp[i] = (temp[i]/x);
	//newtemp[i] = (float) (20*Math.log10(temp[i]));
}

min = 0;


//finding min of new set
for(int i = 0; i < bins; i++)
{
	if(newtemp[i] < min)
	{
		min = newtemp[i];
	}
}


float y = 0-min;



for(int i = 0; i < bins; i++)
{
	gViewData[i] = new GraphViewData((sampfreq/bins)*(i+1)/2, (newtemp[i]+y));
	//gViewData[i] = new GraphViewData((sampfreq/bins)*(i+1)/2, (newtemp[i]));
}


exampleSeries.resetData(gViewData);
*/
