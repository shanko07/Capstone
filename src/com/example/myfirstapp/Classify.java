package com.example.myfirstapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import Jama.Matrix;
import android.os.Environment;
import android.util.Log;

public class Classify {
	double [][] Y; // train
	double [][] K; // kernel
	double [][] A; // dictionary
	int T0;
	String dictionaryPath;
	String trainingPath;
	
	public Classify(int t0, int fileForDictandTrain){
		T0=t0;
		
		
/* FILE READING FROM CSVs */
		
		switch(fileForDictandTrain){
		
		case 0: 
			dictionaryPath = Environment.getExternalStorageDirectory().toString()+"/carA.csv";
			trainingPath = Environment.getExternalStorageDirectory().toString()+"/carY.csv";
			Y = new double[59][6];
			A = new double[177][59];
			break;
			
		case 1:
			dictionaryPath = Environment.getExternalStorageDirectory().toString()+"/noiseA.csv";
			trainingPath = Environment.getExternalStorageDirectory().toString()+"/noiseY.csv";
			Y = new double[88][6];
			A = new double[264][88];
			break;
		}
		
        
		BufferedReader br1 = null;
		String line = "";
		String cvsSplitBy = ",";
	 
		try {
	 
			br1 = new BufferedReader(new FileReader(dictionaryPath));
			
			int rowCounter = 0;
			while ((line = br1.readLine()) != null) {
	 
			        // use comma as separator
				String[] values = line.split(cvsSplitBy);
				for(int i=0; i<values.length; i++){
					A[i][rowCounter] = Double.parseDouble(values[i]);
				}
				rowCounter++;
			}
			Log.d("CLASSIFIER!", "Reading dictionary completed");
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br1 != null) {
				try {
					br1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		BufferedReader br2 = null;
	 
		try {
	 
			br2 = new BufferedReader(new FileReader(trainingPath));
			
			int rowCounter = 0;
			while ((line = br2.readLine()) != null) {
	 
			        // use comma as separator
				String[] values = line.split(cvsSplitBy);
				for(int i=0; i<values.length; i++){
					Y[i][rowCounter] = Double.parseDouble(values[i]);
				}
				rowCounter++;
			}
			Log.d("CLASSIFIER!", "Reading dictionary completed");
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br2 != null) {
				try {
					br2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		
		/* FILE READING FROM CSVs */
		
		
		K = new double[Y.length][Y.length];
	}
	
	public double kernel(double[][] x, double[][] y){
		
		double degree = 5;
		double c = 1;
		
		double sum = 0;
		
		for(int i=0;i<x.length;i++){
			sum += x[i][0]*y[i][0];
		}		
		
		sum = Math.pow(sum+c,degree);		
		
		return sum;
	}	
	
	public double classify(double [][] z){
		
		for(int i = 0; i<Y.length; i++){
			for(int j=0; j<Y.length;j++){
				K[i][j] = kernel(z, Y);
			}
		}
		
		Log.d("CLASSIFIER!", "Kernel Matrix Created");
		// WE HAVE SUCCESSFULLY MADE IT TO HERE!
		
		int N = Y.length;
		//T0 = 10; //TODO: Arbitrary fix this later
		double[][] zhat = new double[1][N];
		double [][] testKZ = new double [N][1];			
		double[][] Kzz = new double[1][1];
		Kzz[0][0] = kernel(z,z);
		
		Matrix KM = new Matrix(K);
		Matrix AM = new Matrix(A);
		Matrix KZZM = new Matrix(Kzz);
		
		
		for(int i =0;i<zhat.length;i++) zhat[0][i] = 0;
		
		Matrix zhatM = new Matrix(zhat);
		
		for(int i=0;i<N;i++){
			double[][] interim = new double[0][Y[i].length];
			for(int j=0;j<Y[i].length;j++){
				interim[0][j] = Y[i][j];
			}
			testKZ[i][0] = kernel(z,interim);
		}
		
		Matrix testKZM = new Matrix(testKZ);
		
		Log.d("CLASSIFIER!", "testKZ Matrix Created");
		
				
		double[][] IS = new double[T0][1];
		Matrix ISM = new Matrix(IS);
		
		Matrix xsM = new Matrix(K);
		
		for(int s = 0; s<T0 ; s++){
			
			
			//Compute the tau vector
			Matrix alpha = zhatM.transpose();
			Matrix beta = alpha.times(KM);
			Matrix gamma = testKZM.minus(beta);
			Matrix tau = gamma.times(AM);
			
			//Set tau at previously found indices to 0
			for(int a = 0; a<s; a++){
				tau.set(0, (int) ISM.get(0,a), 0.0);
			}
			
			//Find the next largest index
			int largestIndex = 0;
			double max = 0.0;
			for(int i=0;i<tau.getColumnDimension();i++){
				if(Math.abs(tau.get(0, i)) >= max){
					largestIndex = i;
					max = Math.abs(tau.get(0, i));
				}
			}
			
			//Add the next largest index
			ISM.set(0, s, largestIndex);
			
			//Create A(:IS) and use to calculate xs			
			Matrix AsubIS = AM.copy();
			double[][] iscopy = ISM.getArrayCopy();
			int[] colindices = new int[iscopy.length];
			for(int i=0; i<colindices.length; i++){
				colindices[i] = (int) iscopy[i][0];
			}
			AsubIS.setMatrix(0,0,colindices,AsubIS);
			
			//calculate xs
			xsM = ( AsubIS.transpose().times(KM.times(AsubIS)).inverse() ).times(testKZM.times(AsubIS).transpose());
			
			//calculate zhat
			zhatM = AsubIS.times(xsM);			
			
		}
		
		int colA = A.length;
		double[][] x = new double[1][colA];
		Matrix xM = new Matrix(x);
		for(int i=0; i<ISM.getColumnDimension(); i++){
			xM.set((int) ISM.get(0, i), 0, xsM.get(0, i));
		}
		
		//calculate residual
		Matrix b = testKZM.timesEquals(2.0).times(AM).times(xM);
		Matrix c = xM.transpose().times(AM.transpose()).times(KM).times(AM).times(xM);
		
		Matrix Residual = KZZM.minus(b).plus(c);		
		
		//return residual
		return Residual.get(0,0);
	}
	

	
	
}
