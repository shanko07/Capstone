package com.example.myfirstapp;

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






public class MainActivity extends Activity {
/*
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

*/
	
	protected float[] fftResults;
	GraphViewSeries exampleSeries;
	GraphView graphView;	
	GraphViewData[] gViewData;
	
	
	private final Handler mHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    
	    	int bins = 160;
	    	int perbin = 1;
	    	int sampfreq = 44100;
	    	
	    	fftResults = (float[]) msg.obj;
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
	    
	    
	    }
	};
	   
	    
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
               
        // Call thread
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
