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
              
                
                String youFilePath = Environment.getExternalStorageDirectory().toString()+"/test_features.wav";
                
                /*
                WavFile wavFile1 = WavFile.readFromFilePath(youFilePath);
                int outerLength = wavFile1.samplesForChannels.length;
                Log.d("tag1", "out: " + Integer.toString(outerLength));
                int innerLength = wavFile1.samplesForChannels[0].length;
                Log.d("tag1", "in: " + Integer.toString(innerLength));
                for(int i=0;i<innerLength;i++){
                	double sampval = wavFile1.samplesForChannels[0][i].convertToDouble();
                	//byte [] byte1 = new byte[wavFile1.samplesForChannels[i][0].convertToBytes().length];
                	//for(int j=0;j<byte1.length;j++){
                		//Log.d("tag1",Double.toString(sampval));
                	//}
                }
                
                */
                
                
                
                File file = new File(youFilePath);
                
                        FileInputStream fin = null;
                
                        try {
                
                            // create FileInputStream object
                
                            fin = new FileInputStream(file);
                
                 
                
                            byte fileContent[] = new byte[(int)file.length()];
                
                             
                
                            // Reads up to certain bytes of data from this input stream into an array of bytes.
                
                            fin.read(fileContent);                

                            //create string from byte array
                            
                            
                            File output1 = new File(Environment.getExternalStorageDirectory().toString()+"/output.dat");
                			if (!output1.exists()) {
                				output1.createNewFile();
                			}
                 
                			FileWriter fw = new FileWriter(output1.getAbsoluteFile());
                			BufferedWriter bw = new BufferedWriter(fw);
                			
                			//bw.close();
                			
                            for(int i =0; i < fileContent.length; i++)
                            {
                            	int thing = fileContent[i] & 0xff;
                            	//double realthing = thing/255.0;
                            	//Log.d("tag1", Integer.toString(thing));
                            	bw.write(Integer.toString(thing));
                            	bw.write(",");
                            	
                            }
                            bw.close();
                            
                            
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

                
                
            }
                
                
                
            	
            	
            	//FloatFFT_1D myFFT = new FloatFFT_1D(N);
                //myFFT.realForward(data);
                //Log.i("Map", "fft successful");
                
            	
                
                
                mainHandler.obtainMessage(1, N, -1, buffer).sendToTarget(); //what is 1
                //mainHandler.obtainMessage(0, data).sendToTarget(); //what is 0 
                
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
