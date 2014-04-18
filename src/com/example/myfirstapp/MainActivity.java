package com.example.myfirstapp;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;




//package com.android.audiorecordtest;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

import java.io.IOException;
import java.nio.Buffer;
import java.util.Collections;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;



public class MainActivity extends Activity {
/*
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

*/
	
	Audio a1;
	
	
	/* Frequency and Time Analysis Variables */
	protected double[] fftResults;  //Result of DFT on the current frame of data
	protected double[] timeDomain;  //Time domain representation of the current frame of data
	double zeroCrossingRate;
	double rmsAmplitude;
	double [] prevSpectrum = null;  //Previous frame spectrum
	double [] spectrum; // absolute value of fftResults
	double []RHSspectrum; //right half spectrum
	double totalEnergy; //total energy of the signal
	int below91; //bins below which 91 percent of signal energy is contained
	double runningSum;
	double spectralRolloff;
	double spectralCentroid;
	double spectralEntropy;
	double spectralFlux;
	int N;  //Number of samples in time and frequency domain
	int sampRate;  //Sampling rate
	
	/* GraphView Variables */
	GraphViewSeries exampleSeries;
	GraphView graphView;	
	GraphViewData[] gViewData;
	
	/* Classifier Variables */
	double[][] pastResidual = new double[2][10];  //2 classes, 2 values of previous results
	//TODO: Finish this ^ ^ 
	TextView carText;
	TextView noiseText;
	double carResidual;
	double noiseResidual;
	final double[][]  blah = new double[6][1];
	//Car class arg1=0
	final Classify car = new Classify(3, 0);
	//Noise class arg1=1
	final Classify noise = new Classify(3, 1);
	int testimer = 0;
	int ringBuffer = 0;
	
	
	
	
	//Message Handler to receive messages from the Audio thread
	private final Handler mHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	
	    	testimer++;
	    
	    	switch (msg.what){
	    	
	    	//Case 0 is when the message passed is the fft results
	    	case 0:
	    		
	    	//do not attempt to assign previous spectrum on the first running frame since there is no previous data
	    	if(spectrum!=null)
	    	{
	    		prevSpectrum = spectrum;	    			
	    	}
	    	
	    	//Acquire spectrum
	    	spectrum = (double[]) msg.obj;
	    	sampRate = msg.arg1;
	    	
	    	//calculate total energy of the signal
	    	totalEnergy = 0;
	    	for(int i=0; i<spectrum.length; i++)
	    	{
	    		totalEnergy += spectrum[i]*spectrum[i];
	    	}
	    	
	    	//create the RHSspectrum from the original spectrum
	    	RHSspectrum = new double[spectrum.length/2];
	    	for(int i=0; i<spectrum.length/2; i++)
	    	{
	    		RHSspectrum[i] = spectrum[i];
	    	}	    	

	    	//calculate the frequency bin at which 91% of the energy is contained below said frequency bin
	    	below91 = 0;
	    	runningSum = 0;
	    	for(int i=0;i<RHSspectrum.length;i++)
	    	{
	    		runningSum += RHSspectrum[i]*RHSspectrum[i];
	    		if((runningSum*2)/totalEnergy >= .91)
	    		{
	    			below91=i;
	    			i=RHSspectrum.length;
	    		}
	    	}
	    	
	    	
	    	/* Calculate frequency domain features */
	    	calculateSpectralRolloff();
	    	calculateSpectralCentroid();
	    	calculateSpectralEntropy();
	    	calculateSpectralFlux();
	    	
	    	//Run the KOMP algorithm on the current feature and obtain the residual based on classification with noise and car dictionaries
	    	if(prevSpectrum!=null){
	    	
	    	blah[0][0] = rmsAmplitude;
	    	blah[1][0] = zeroCrossingRate;
	    	blah[2][0] = spectralRolloff;
	    	blah[3][0] = spectralCentroid;
	    	blah[4][0] = spectralEntropy;
	    	blah[5][0] = spectralFlux;
	    	
			carResidual = car.classify(blah);
			noiseResidual = noise.classify(blah);
			
			pastResidual[0][ringBuffer] = carResidual;
			pastResidual[1][ringBuffer] = noiseResidual;
			
			ringBuffer++;
			if(ringBuffer == 10) ringBuffer = 0;
			
			int carMatch = 0;
			for(int i = 0; i<pastResidual[0].length; i++){
				if(pastResidual[0][i] < pastResidual[1][i]) carMatch++;
			}
			
			if(carResidual < noiseResidual) {carText.setText("Car"); Log.d("first order classifier", "Car");}
			else {carText.setText(""); Log.d("first order classifier", "xxxxxxxx");}
			
			//if(carMatch >= 4) carText.setText("Match");
			//else carText.setText("Failure");
			
			Log.d("Residual", "z1: " + Double.toString(blah[0][0]));
			Log.d("Residual", "z2: " + Double.toString(blah[1][0]));
			Log.d("Residual", "z3: " + Double.toString(blah[2][0]));
			Log.d("Residual", "z4: " + Double.toString(blah[3][0]));
			Log.d("Residual", "z5: " + Double.toString(blah[4][0]));
			Log.d("Residual", "z6: " + Double.toString(blah[5][0]));
			Log.d("Residual", "car: " + Double.toString(carResidual));
			Log.d("Residual", "noise: " + Double.toString(noiseResidual));
			
			
			
			//Toast.makeText(MainActivity.this, Double.toString(5.67), Toast.LENGTH_SHORT).show();
			
					//carText.setText(" " + Double.toString(carResidual));
					//noiseText.setText(" " + Double.toString(noiseResidual));
			
	    	}
	    	
	    	break;
	    	
	    	
	    	//Case 1 is when the message passed is the time domain representation
	    	case 1:
	    		
	    		timeDomain = (double[]) msg.obj;
	    		N = msg.arg1;
	    		
	    		//noiseText.setText(Double.toString(noiseResidual));
	    		
	    		/* Calculate time domain features */
	    		calculateRMSAmplitude();
	    		calculateZeroCrossing();
	    		
	    		break;
	    		
	    		
	    	
	    	default:	    	
	    	}
	    	//carText.setText(Integer.toString(testimer));
			//noiseText.setText(Integer.toString(testimer));
	    }	
	};
	
	/*
	private void setStuff(double d, double e){
		carText.setText(Double.toString(d));
		noiseText.setText(Double.toString(e));
	}
	*/
	   
	private void calculateZeroCrossing() 
	{
	int zeroCrossings = 0;
	
	for(int i=0; i<timeDomain.length-1; i++)
	{

		if(timeDomain[i] > 0 && timeDomain[i+1] < 0)
		{
			zeroCrossings++;
		}
		if(timeDomain[i] < 0 && timeDomain[i+1] > 0)
		{
			zeroCrossings++;
		}
	}

	zeroCrossingRate = zeroCrossings;
	}
	
	
	private void calculateRMSAmplitude() {
		rmsAmplitude = 0;
		double sum = 0;
		for(int i=0; i<timeDomain.length; i++){
			sum += timeDomain[i]*timeDomain[i];
		}
		sum/=N;
		rmsAmplitude = (double) Math.sqrt(sum);
	}
	
	
	private void calculateSpectralRolloff()
	{
		spectralRolloff = (double) ((below91+1)*(double)sampRate/spectrum.length);
	}
	
	private void calculateSpectralCentroid()
	{
		double []RHSSpectNum = new double [RHSspectrum.length];
		for(int i=1;i<=RHSspectrum.length;i++){
			RHSSpectNum[i-1] = RHSspectrum[i-1] * i;
		}
		double sumN = 0;
		for(int i=0;i<RHSSpectNum.length;i++){
			sumN += RHSSpectNum[i];
		}
		double sumD = 0;
		for(int i=0;i<RHSspectrum.length;i++){
			sumD += RHSspectrum[i];
		}
		double centroid = sumN/ sumD;
		
		spectralCentroid = (sampRate / spectrum.length) * centroid;
	}
	
	private void calculateSpectralEntropy() 
	{
		double [] PSD = new double [spectrum.length];
		for(int i=0;i<spectrum.length;i++){
			PSD[i] = spectrum[i] * spectrum[i] / totalEnergy;
		}
		double [] PSDmultLog = new double [PSD.length];
		for(int i = 0; i<PSDmultLog.length;i++){
			PSDmultLog[i] = (double) Math.log(PSD[i]) / (double) Math.log(2) * (double) PSD[i];
		}
		double sum = 0;
		for(int i = 0; i<PSDmultLog.length;i++){
			sum += PSDmultLog[i];
		}
		sum = sum * -1;
		spectralEntropy = sum;	
	}
	
	private void calculateSpectralFlux() 
	{		
		
		if(prevSpectrum!=null){
		double [] diff = new double [spectrum.length];
		for (int i=0;i<diff.length;i++){
			diff[i] = spectrum[i] - prevSpectrum[i];
		}
		double sqSum = 0;
		for (int i=0;i<diff.length;i++){
			sqSum += diff[i] * diff[i];
		}
		spectralFlux = (double) Math.sqrt(sqSum); 
		}
	}
	
	
	    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	/*
	
	
	
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
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
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
    
    */

    public MainActivity() { //AudioRecordTest
        //mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        //mFileName += "/audiorecordtest.3gp";
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);     
        setContentView(R.layout.activity_main);   
        
        a1 = new Audio(mHandler);

        carText = (TextView) findViewById(R.id.textView3);
		noiseText = (TextView) findViewById(R.id.textView4);
		
		carText.setText("A");
		carText.setText("B");
		carText.setText("c");
        //LinearLayout ll = new LinearLayout(this);
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
        */

        
     // init example series data
       // exampleSeries = new GraphViewSeries(new GraphViewData[] {
        //      new GraphViewData(1, 2.0d)
        //      , new GraphViewData(2, 1.5d)
       //       , new GraphViewData(3, 2.5d)
        //      , new GraphViewData(4, 1.0d)
       // });
        
        
       // GraphView graphView = new BarGraphView(
       //       this // context
       //       , "GraphViewDemo" // heading
       // );
        
        
       // graphView.addSeries(exampleSeries); // data
        
         
        //LinearLayout layout = (LinearLayout) findViewById(R.id.barGraph);
        
        //ll.addView(graphView);
        
        
        //setContentView(ll);
        
        
    }

    @Override
    public void onPause() {
        super.onPause();
       /*
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        */
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
