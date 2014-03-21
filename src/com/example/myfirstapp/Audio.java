package com.example.myfirstapp;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/*
 * Thread to manage live recording/playback of voice input from the device's microphone.
 */

// use following link to build a wav reader and do testing between here and matlab
//http://web.archive.org/web/20120531113946/http://www.builogic.com/java/javasound-read-write.html
public class Audio extends Thread
{ 
    private boolean stopped = false;
    private Handler mainHandler;

    /**
     * Give the thread high priority so that it's not canceled unexpectedly, and start it
     */
    public Audio(Handler mhandler)
    { 
    	mainHandler = mhandler;
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        start();
    }

    @Override
    public void run()
    { 
        Log.i("Audio", "Running Audio Thread");
        AudioRecord recorder = null;
        AudioTrack track = null;
        short[][]   buffers  = new short[256][160];
        int ix = 0;

        /*
         * Initialize buffer to hold continuously recorded audio data, start recording, and start
         * playback.
         */
        try
        {
        	int sampRate = 44100;
            int N = AudioRecord.getMinBufferSize(sampRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
            recorder = new AudioRecord(AudioSource.MIC, sampRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N*10);
            track = new AudioTrack(AudioManager.STREAM_MUSIC, sampRate, 
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, N*10, AudioTrack.MODE_STREAM);
            recorder.startRecording();
            track.play();
            /*
             * Loops until something outside of this thread stops it.
             * Reads the data from the recorder and writes it to the audio track for playback.
             */
            while(!stopped)
            { 
                Log.i("Map", "Writing new data to buffer");
                short[] buffer = buffers[ix++ % buffers.length];
                N = recorder.read(buffer,0,buffer.length);
                track.write(buffer, 0, buffer.length);
                
                //IMPLEMENT FFT HERE
                FloatFFT_1D myFFT = new FloatFFT_1D(N);
                float[] data = convertFloat(buffer);
                myFFT.realForward(data);
                Log.i("Map", "fft successful");
                
                
                
                
                mainHandler.obtainMessage(1, N, -1, buffer).sendToTarget(); //what is 1
                mainHandler.obtainMessage(0, data).sendToTarget(); //what is 0 
                
            }
        }
        catch(Throwable x)
        { 
            Log.w("Audio", "Error reading voice audio", x);
        }
        /*
         * Frees the thread's resources after the loop completes so that it can be run again
         */
        finally
        { 
            recorder.stop();
            recorder.release();
            track.stop();
            track.release();
        }
    }

    /**
     * Called from outside of the thread in order to stop the recording/playback loop
     */
    private void close()
    { 
         stopped = true;
    }
    
    private float[] convertFloat(short[] input)
    {
    	float[] output = new float[input.length];
    	for(int i = 0; i < input.length; i++)
    	{
    		output[i] = (float) input[i];
    	}
    	return output;
    }

}
