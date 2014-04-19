package com.example.myfirstapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.os.Environment;
import android.util.Log;

public class WavProcessing {
	
	boolean isRead;
	String filePath;
	RandomAccessFile file;
	int byteRate;
	int blockAlign;
	long chunkSize;
	long subChunk2Size;
	long numberOfSamples;
	int numberOfChannels;
	int bitsPerSample;
	int samplingRate;
	
	WavProcessing(String filepath, boolean isread){
		filePath = filepath;
		isRead = isread;
		try {
			file = new RandomAccessFile(filePath, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(isRead){
			//TODO: This is a stub we should do something for reading stuffs
		}
		else{
			//TODO: This is a stub do something for writing stuffs
			
		}
		
		
		
	}
	
	public void writeHeader(int channels, int samplingrate, int bitspersample) throws IOException{
		
		numberOfChannels = channels;
		bitsPerSample = bitspersample;
		samplingRate = samplingrate;
		
		file.write(0x52);file.write(0x49);file.write(0x46);file.write(0x46); //RIFF chunk descriptor
		
		file.write(0x00);file.write(0x00);file.write(0x00);file.write(0x00); //(Size of file - 8) Bytes  //TODO: Fill in post write
		//4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
		
		
		file.write(0x57);file.write(0x41);file.write(0x56);file.write(0x45); //Format = WAVE
		
		file.write(0x66);file.write(0x6D);file.write(0x74);file.write(0x20); //fmt[] subchunk1 ID
		
		file.write(0x10);file.write(0x00);file.write(0x00);file.write(0x00); //16 for PCM this is subchunk1 Size
		
		file.write(0x01);file.write(0x00);									 //Audio format linear quantization PCM format
		
		file.write(0x01);file.write(0x00);									 //Number of channels.  We will leave it at 1 here
		
		int a = samplingRate & 0xFF;  int b = (samplingRate & 0xFF00) >> 8;  
		int c = (samplingRate & 0xFF0000) >> 16;  int d = (samplingRate & 0xFF000000) >> 24;
		
		file.write(a);file.write(b);file.write(c);file.write(d); 			 //Sampling Rate
		
		byteRate = samplingRate*channels*bitsPerSample/8;
		
		a = byteRate & 0xFF;  b = (byteRate & 0xFF00) >> 8;  
		c = (byteRate & 0xFF0000) >> 16;  d = (byteRate & 0xFF000000) >> 24;
		
		file.write(a);file.write(b);file.write(c);file.write(d); 			 //Byte Rate
		
		blockAlign = channels*bitsPerSample/8;
		
		a = blockAlign & 0xFF;  b = (blockAlign & 0xFF00) >> 8;
		
		file.write(a);file.write(b);										 //Block Align ie Bytes per Block
		
		a = bitsPerSample & 0xFF;  b = (bitsPerSample & 0xFF00) >> 8;
		
		file.write(a);file.write(b);										 //Bits per Sample not per Block??
		
		file.write(0x64);file.write(0x61);file.write(0x74);file.write(0x61); //data subchunk2 ID
		
		file.write(0x00);file.write(0x00);file.write(0x00);file.write(0x00); //#Samples*#Channels*bitspersample/8 Bytes to follow  //TODO: Fill in post write
		
		
		
		
	}
	
	
	public boolean writeBlock(byte[] block) throws IOException{
		if(block.length != blockAlign){
			return false;
		}
		else{
			file.write(block);
			return true;
		}
		
	}
	
	
	public void writeShort(short twoBytes) throws IOException{
		
		byte a = (byte) (twoBytes & 0xFF);
		byte b = (byte) ((twoBytes & 0xFF00) >> 8);
		
		byte[]x = {a,b};
		
		file.write(x);
		
	}
	
	public void finalizeWrite(long numberOfsamples) throws IOException{
		numberOfSamples = numberOfsamples;
		
		subChunk2Size = numberOfSamples*numberOfChannels*bitsPerSample/8;
		
		file.seek(4);
		
		chunkSize = 36 + subChunk2Size;
		//4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
		
		int a = (int) (chunkSize & 0xFF);  int b = (int) ((chunkSize & 0xFF00) >> 8);  
		int c = (int) ((chunkSize & 0xFF0000) >> 16);  int d = (int) ((chunkSize & 0xFF000000) >> 24);
		
		file.write(a);file.write(b);file.write(c);file.write(d);
		
		file.seek(40);
		
		a = (int) (subChunk2Size & 0xFF);  b = (int) ((subChunk2Size & 0xFF00) >> 8);  
		c = (int) ((subChunk2Size & 0xFF0000) >> 16);  d = (int) ((subChunk2Size & 0xFF000000) >> 24);
		
		file.write(a);file.write(b);file.write(c);file.write(d);
		
		file.close();
		
	}
	
	
	
}