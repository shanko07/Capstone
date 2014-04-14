package com.example.myfirstapp;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;





import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;




import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
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
            //track.play();
            /*
             * Loops until something outside of this thread stops it.
             * Reads the data from the recorder and writes it to the audio track for playback.
             */
            
            int condition=0;
            double wavTest[] = new double[2000];
            float wavlet1[] = new float[500];
            float wavlet2[] = new float[500];
            float wavlet3[] = new float[500];
            float wavlet4[] = new float[500];
            
            float fftwavlet1[] = new float[500];
            float fftwavlet2[] = new float[500];
            float fftwavlet3[] = new float[500];
            float fftwavlet4[] = new float[500];
            
            
            
            while(!stopped)
            { 
                Log.i("Map", "Writing new data to buffer");
                short[] buffer = buffers[ix++ % buffers.length];
                N = recorder.read(buffer,0,buffer.length);
                track.write(buffer, 0, buffer.length);
                
                /*
                //IMPLEMENT FFT HERE
                FloatFFT_1D myFFT = new FloatFFT_1D(N);
                float[] data = convertFloat(buffer);
                myFFT.realForward(data);
                Log.i("Map", "fft successful");
                
                */
                
                while(condition<1){
                	condition++;
                Log.d("tag1","test");
                //Log.d("feature", "once loop");
                
                String youFilePath = Environment.getExternalStorageDirectory().toString()+"/test_features.wav";
                
                File file = new File(youFilePath);
                
                        FileInputStream fin = null;
                
                        try {
                
                            // create FileInputStream object
                
                            fin = new FileInputStream(file);
                
                 
                
                            byte fileContent[] = new byte[(int)file.length()];
                
                             
                
                            // Reads up to certain bytes of data from this input stream into an array of bytes.
                
                            fin.read(fileContent);                

                            //create string from byte array
                            
                            
                            File output1 = new File(Environment.getExternalStorageDirectory().toString()+"/output.csv");
                			if (!output1.exists()) {
                				output1.createNewFile();
                			}
                 
                			FileWriter fw = new FileWriter(output1.getAbsoluteFile());
                			BufferedWriter bw = new BufferedWriter(fw);
                			
                			//bw.close();
                			//int temp = (int) fileContent[34];
                			//bw.write(Integer.toString(temp));
                			
                			//temp = (int) fileContent[35];
                			//bw.write(temp);
                			
                            for(int i =44; i < fileContent.length; i+=2)
                            {
                            	short result = (short) ((fileContent[i+1] << 8) | (fileContent[i] & 0xFF));
                            	//int thing = fileContent[i] & 0xff;
                            	//double realthing = thing/255.0;
                            	//Log.d("tag1", Integer.toString(thing));
                            	bw.write(Double.toString(result/32768.0));
                            	wavTest[(i-44)/2] = result/32768.0;
                            	bw.write(",");
                            	
                            }
                            bw.close();
                            
                            
                            for(int i=0; i<500; i++)
                            {
                            	wavlet1[i] = (float) wavTest[i];
                            	wavlet2[i] = (float) wavTest[500+i];
                            	wavlet3[i] = (float) wavTest[1000+i];
                            	wavlet4[i] = (float) wavTest[1500+i];
                            	//fftwavlet1[i] = (float) wavTest[i];
                            	//fftwavlet2[i] = (float) wavTest[500+i];
                            	//fftwavlet3[i] = (float) wavTest[1000+i];
                            	//fftwavlet4[i] = (float) wavTest[1500+i];
                            }
                            
                            
                            Log.d("tag1",Integer.toString(fileContent.length));
                
                            //String s = new String(fileContent);
                
                            //System.out.println("File content: " + s);
                
                        }
                
                        catch (FileNotFoundException e) {
                
                            System.out.println("File not found" + e);
                
                        }
                
                        catch (IOException ioe) {
                
                            System.out.println("Exception while reading file " + ioe);
                
                        }
                
                        finally {
                
                            // close the streams using close method
                
                            try {
                
                                if (fin != null) {
                
                                    fin.close();
                
                                }
                
                            }
                
                            catch (IOException ioe) {
                
                                System.out.println("Error while closing stream: " + ioe);
                
                            }
                
                        }
    
                        
                        /*
                        FloatFFT_1D myfft = new FloatFFT_1D(500);
                        
                        mainHandler.obtainMessage(1, 500, -1, wavlet1).sendToTarget();
                        myfft.realForward(fftwavlet1);
                        mainHandler.obtainMessage(0, fftwavlet1).sendToTarget();
                        
                        mainHandler.obtainMessage(1, 500, -1, wavlet2).sendToTarget();
                        myfft.realForward(fftwavlet2);
                        mainHandler.obtainMessage(0, fftwavlet2).sendToTarget();
                        
                        mainHandler.obtainMessage(1, 500, -1, wavlet3).sendToTarget();
                        myfft.realForward(fftwavlet3);
                        mainHandler.obtainMessage(0, fftwavlet3).sendToTarget();
                        
                        mainHandler.obtainMessage(1, 500, -1, wavlet4).sendToTarget();
                        myfft.realForward(fftwavlet4);
                        mainHandler.obtainMessage(0, fftwavlet4).sendToTarget();
                		*/
                        
                        
                        
                        
                        
                        float windowed1[] = hamming(wavlet1.length, wavlet1);
                        fftwavlet1 = doComplexFFT(windowed1);
                    	float spectrum1[] = doSpectrum(fftwavlet1);
                        mainHandler.obtainMessage(1, 500, -1, windowed1).sendToTarget();
                        mainHandler.obtainMessage(0, spectrum1).sendToTarget(); 
                        
                        float windowed2[] = hamming(500, wavlet2);
                        fftwavlet2 = doComplexFFT(windowed2);
                        //fftwavlet2 = doComplexFFT(wavlet2);
                        float spectrum2[] = doSpectrum(fftwavlet2);                        
                        mainHandler.obtainMessage(1, 500, -1, windowed2).sendToTarget();
                        mainHandler.obtainMessage(0, spectrum2).sendToTarget(); 
                        
                        float windowed3[] = hamming(500, wavlet3);
                        fftwavlet3 = doComplexFFT(windowed3);
                        //fftwavlet3 = doComplexFFT(wavlet3);
                        float spectrum3[] = doSpectrum(fftwavlet3);
                        mainHandler.obtainMessage(1, 500, -1, windowed3).sendToTarget();
                        mainHandler.obtainMessage(0, spectrum3).sendToTarget(); 
                        
                        float windowed4[] = hamming(500, wavlet4);
                        fftwavlet4 = doComplexFFT(windowed4);
                        //fftwavlet4 = doComplexFFT(wavlet4);
                        float spectrum4[] = doSpectrum(fftwavlet4);
                        mainHandler.obtainMessage(1, 500, -1, windowed4).sendToTarget();
                        mainHandler.obtainMessage(0, spectrum4).sendToTarget();
                        
                        
                        
                
            }
                
                
                
            	
            	 
                
                
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
    
    private float[] doComplexFFT(float[] timeDomainSignal){
    	int N = timeDomainSignal.length;
    	float complexDFT[] = new float[N*2];

        for(int k=0; k<N*2;k+=2)
        {
        	for(int n=0; n<N;n++)
        	{
        		complexDFT[k] = complexDFT[k] + (float) (timeDomainSignal[n]*Math.cos(2.0*Math.PI*(k/2)*n/(float)N));
        		complexDFT[k+1] = complexDFT[k+1] + (float) (-1.0 * (float) timeDomainSignal[n]*Math.sin(2.0*Math.PI*(k/2)*n/(float)N));
        	}
        	//Log.d("FD", "original Data: " + Float.toString(timeDomainSignal[k/2]));
        	//Log.d("FD", Float.toString(complexDFT[k]) + "  " + Float.toString(complexDFT[k+1]) + "i");
        }
    	return complexDFT;
    }
    
    private float[] doSpectrum(float[] complexFFT){
    	float spectrum[] = new float[complexFFT.length/2];
    	
    	for(int i=0; i<complexFFT.length;i+=2)
    	{
    		spectrum[i/2] = (float) Math.sqrt(Math.pow(complexFFT[i], 2.0) + Math.pow(complexFFT[i+1], 2.0));
    	}
    	return spectrum;
    }
    
    private float[] hamming(int L, float[] input)
    {
    	float hamming[] = new float[L];
    	for(int i=0;i<L;i++)
    	{
    		hamming[i]=(float) (.54-.46*Math.cos(2*Math.PI*i/(L-1))) * input[i];
    	}
    	return hamming;
    }
        
    

}
