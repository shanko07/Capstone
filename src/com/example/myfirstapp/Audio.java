package com.example.myfirstapp;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

/*
 * Thread to manage live recording/playback of voice input from the device's microphone.
 */

// use following link to build a wav reader and do testing between here and matlab
//http://web.archive.org/web/20120531113946/http://www.builogic.com/java/javasound-read-write.html
public class Audio extends Thread
{ 
    private boolean stopped = false;
    private Handler mainHandler;
    private static String WAV_FILE = "/assets/test_features.wav";

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
        //AudioTrack track = null;
        //short[][]   buffers  = new short[256][160];  //TODO: arg [][x] is buffer read size
        short[][]   buffers;
        int ix = 0;

        /*
         * Initialize buffer to hold continuously recorded audio data, start recording, and start
         * playback.
         */
        try
        {
        	
        	//Figure out the supported sampling rate of the phone
        	int[] supportedRates = new int[5];
        	int numSupportedRates = 0;
        	for (int rate : new int[] {8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
                int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize > 0) {
                	supportedRates[numSupportedRates] = rate;
                	numSupportedRates++;
                }
            }
        	
        	
        	//use the highest sampling rate available.  TODO: we may need to change this to the highest rate below 44100 for computational savings
        	int sampRate = supportedRates[numSupportedRates-1-2];
        	sampRate = 16000;  //TODO: don't hard code this
        	//sampRate*50mS = sample size for time domain and DFT
        	//inmpose a 50mS frame size and based on the sampling rate calculate the frame size in samples
        	int frameSize = (int) Math.ceil(sampRate*.025);
        	
        	buffers = new short[1024][frameSize];
            int N = AudioRecord.getMinBufferSize(sampRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
           
            recorder = new AudioRecord(AudioSource.MIC, sampRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 10*N);
            //track = new AudioTrack(AudioManager.STREAM_MUSIC, sampRate, 
                    //AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 10*N, AudioTrack.MODE_STREAM);
            recorder.startRecording();
            
            
            //track.play();
            /*
             * Loops until something outside of this thread stops it.
             * Reads the data from the recorder and writes it to the audio track for playback.
             */
            
            /*
            File output1 = new File(Environment.getExternalStorageDirectory().toString()+"/samplesFromAndroidMic.csv");
			if (!output1.exists()) {
				output1.createNewFile();
			}
 
			FileWriter fw = new FileWriter(output1.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
            */
            
            
            while(!stopped)
            { 
            	
                //Log.i("Map", "Writing new data to buffer");
                //short[] buffer = buffers[ix++ % buffers.length];
                //N = recorder.read(buffer,0,buffer.length);
                //track.write(buffer, 0, buffer.length);
            	
            	
    			//int x = 0;
                
               // while(x<400){
                
            	
            	short[] buffer1 = buffers[ix++ % buffers.length];
            	N = recorder.read(buffer1, 0,buffer1.length);
            	//track.write(buffer1, 0, buffer1.length);
            	
            	
            	
            	/*
            	for(int i =0; i < buffer1.length; i++)
                {
                	bw.write(Short.toString(buffer1[i]));
                	bw.write(",");
                }
                */
            	
            	short[] buffer2 = buffers[ix++ % buffers.length];
            	N = recorder.read(buffer2, 0,buffer2.length);
            	//track.write(buffer2, 0, buffer2.length);
            	
            	/*
            	for(int i =0; i < buffer1.length; i++)
                {
                	bw.write(Short.toString(buffer2[i]));
                	bw.write(",");
                }
                */
            	
            	
            	short[] buffer3 = buffers[ix++ % buffers.length];
            	N = recorder.read(buffer3, 0,buffer3.length);
            	//track.write(buffer3, 0, buffer3.length);
            	
            	/*
            	for(int i =0; i < buffer1.length; i++)
                {
                	bw.write(Short.toString(buffer3[i]));
                	bw.write(",");
                }
                */
            	
            	//short[] buffer4 = buffers[ix++ % buffers.length];
            	//N = recorder.read(buffer4, 0,buffer4.length);
            	//track.write(buffer4, 0, buffer4.length);
            	
            	/*
            	for(int i =0; i < buffer1.length; i++)
                {
                	bw.write(Short.toString(buffer4[i]));
                	bw.write(",");
                }
                */
            	
            	//short[] buffer5 = buffers[ix++ % buffers.length];
            	//N = recorder.read(buffer5, 0,buffer5.length);
            	//track.write(buffer5, 0, buffer5.length);
            	
            	/*
            	for(int i =0; i < buffer1.length; i++)
                {
                	bw.write(Short.toString(buffer5[i]));
                	bw.write(",");
                }
                */
            	
            	//short[] buffer6 = buffers[ix++ % buffers.length];
            	//N = recorder.read(buffer6, 0,buffer6.length);
            	//track.write(buffer5, 0, buffer5.length);
            	
            	/*
            	for(int i =0; i < buffer1.length; i++)
                {
                	bw.write(Short.toString(buffer6[i]));
                	bw.write(",");
                }
                */
            	
            	//short[] buffer7 = buffers[ix++ % buffers.length];
            	//N = recorder.read(buffer7, 0,buffer7.length);
            	//track.write(buffer5, 0, buffer5.length);
            	
            	/*
            	for(int i =0; i < buffer1.length; i++)
                {
                	bw.write(Short.toString(buffer7[i]));
                	bw.write(",");
                }
                */
            	
            	//short[] buffer8 = buffers[ix++ % buffers.length];
            	//N = recorder.read(buffer8, 0,buffer8.length);
            	//track.write(buffer5, 0, buffer5.length);
            	
            	/*
            	for(int i =0; i < buffer1.length; i++)
                {
                	bw.write(Short.toString(buffer8[i]));
                	bw.write(",");
                }
                */
            	
         
            	
            	//x++;
            	
                //}
            	//bw.close();
                
                //stopped = true;
                
            	// commenting out the messages just in recording mode
                
               //int counter=0;
                
               //if(counter == 0){ 
               double[] bufferData = shortToDouble(buffer1);
               //double[] data = new double[frameSize];
              // for(int i=0; i<frameSize; i++){
            	   //data[i]=bufferData[i];
               //}
               
               
               //TODO: Remove hard codes below for frame size and hamming
               double[] windowed = hamming(bufferData.length, bufferData);
               double[] fftwavlet = doComplexFFT(windowed);
               double[] spectrum = doSpectrum(fftwavlet);
               mainHandler.obtainMessage(1, bufferData.length, -1, windowed).sendToTarget();
               mainHandler.obtainMessage(0, sampRate, -1, spectrum).sendToTarget();
               mainHandler.obtainMessage(33, buffer1).sendToTarget();
               mainHandler.obtainMessage(33, buffer2).sendToTarget();
               mainHandler.obtainMessage(33, buffer3).sendToTarget();
               //counter++;
               //if(counter==6) counter=0;
               //}
                 
               
                
                
                
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
            //track.stop();
            //track.release();
        }
    }

    /**
     * Called from outside of the thread in order to stop the recording/playback loop
     */
    protected void close()
    { 
         stopped = true;
    }
    
    private double[] shortToDouble(short[] input)
    {
    	double[] output = new double[input.length];
    	for(int i = 0; i < input.length; i++)
    	{
    		output[i] = (double) input[i]/32768.0;
    	}
    	return output;
    }
    
    private double[] doComplexFFT(double[] timeDomainSignal){
    	int N = timeDomainSignal.length;
    	double complexDFT[] = new double[N*2];

        for(int k=0; k<N*2;k+=2)
        {
        	for(int n=0; n<N;n++)
        	{
        		complexDFT[k] = complexDFT[k] + (double) (timeDomainSignal[n]*Math.cos(2.0*Math.PI*(k/2)*n/(double)N));
        		complexDFT[k+1] = complexDFT[k+1] + (double) (-1.0 * (double) timeDomainSignal[n]*Math.sin(2.0*Math.PI*(k/2)*n/(double)N));
        	}
        }
    	return complexDFT;
    }
    
    private double[] doSpectrum(double[] complexFFT){
    	double spectrum[] = new double[complexFFT.length/2];
    	
    	for(int i=0; i<complexFFT.length;i+=2)
    	{
    		spectrum[i/2] = (double) Math.sqrt(Math.pow(complexFFT[i], 2.0) + Math.pow(complexFFT[i+1], 2.0));
    	}
    	return spectrum;
    }
    
    private double[] hamming(int L, double[] input)
    {
    	double hamming[] = new double[L];
    	for(int i=0;i<L;i++)
    	{
    		hamming[i]=(double) (.54-.46*Math.cos(2*Math.PI*i/(L-1))) * input[i];
    	}
    	return hamming;
    }
        
    

}
