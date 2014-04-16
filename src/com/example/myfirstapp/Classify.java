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
			Y = new double[6][59];
			A = new double[59][177];
			break;
			
		case 1:
			dictionaryPath = Environment.getExternalStorageDirectory().toString()+"/noiseA.csv";
			trainingPath = Environment.getExternalStorageDirectory().toString()+"/noiseY.csv";
			Y = new double[6][88];
			A = new double[88][264];
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
					A[rowCounter][i] = Double.parseDouble(values[i]);
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
					Y[rowCounter][i] = Double.parseDouble(values[i]);
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
		
		
		K = new double[Y[0].length][Y[0].length];
	}
	
	public double kernel(double[][] x, double[][] y){
		
		double degree = 5;
		double c = 1;
		double sigma = 1000.0;
		
		double sum = 0;
		
		double[][] temp = new double[x.length][1];
		for(int i=0;i<x.length;i++){
			temp[i][0] = (x[i][0] - y[i][0]) * (x[i][0] - y[i][0]);
		}
		
		for(int i=0;i<x.length;i++){
			
			sum += temp[i][0];
		}		
		
		double result = Math.pow(Math.E, (-1*sum/(2*Math.pow(sigma,2.0))));
		
		//sum = Math.pow(sum+c,degree);		
		
		return result;
	}	
	
	public double classify(double [][] z){
		
		for(int i = 0; i<Y[0].length; i++){
			for(int j=0; j<Y[0].length;j++){
				double[][] tempi = new double[Y.length][1];
				double[][] tempj = new double[Y.length][1];
				for(int a=0; a<Y.length;a++){
					tempi[a][0] = Y[a][i];
					tempj[a][0] = Y[a][j];
				}
				K[i][j] = kernel(tempi,tempj);
			}
		}
		
		Log.d("CLASSIFIER!", "Kernel Matrix Created");
		// WE HAVE SUCCESSFULLY MADE IT TO HERE!
		
		int N = Y[0].length;
		//T0 = 10; //TODO: Arbitrary fix this later
		double[][] zhat = new double[N][1];
		double [][] testKZ = new double [1][N];			
		double[][] Kzz = new double[1][1];
		Kzz[0][0] = kernel(z,z);
		
		Log.d("CLASSIFIER!", "2D arrays Created");
		
		Matrix KM = new Matrix(K);
		Matrix AM = new Matrix(A);
		Matrix KZZM = new Matrix(Kzz);
		
		Log.d("CLASSIFIER!", "KM AM KZZM Created");
		
		
		for(int i =0;i<zhat.length;i++) zhat[i][0] = 0;
		
		Matrix zhatM = new Matrix(zhat);
		
		
		Log.d("CLASSIFIER!", "zhatM Created");
		
		
		Log.d("CLASSIFIER!", "A rows: " + Integer.toString(AM.getRowDimension()));	
		Log.d("CLASSIFIER!", "A cols: " + Integer.toString(AM.getColumnDimension()));	
		Log.d("CLASSIFIER!", "zhat rows: " + Integer.toString(zhatM.getRowDimension()));	
		Log.d("CLASSIFIER!", "zhat cols: " + Integer.toString(zhatM.getColumnDimension()));	
		/*
		Log.d("CLASSIFIER!", "testKZ rows: " + Integer.toString(testKZM.getRowDimension()));	
		Log.d("CLASSIFIER!", "testKZ cols: " + Integer.toString(testKZM.getRowDimension()));	
		Log.d("CLASSIFIER!", "IS rows: " + Integer.toString(ISM.getRowDimension()));	
		Log.d("CLASSIFIER!", "IS cols: " + Integer.toString(ISM.getRowDimension()));	
		Log.d("CLASSIFIER!", "xs rows: " + Integer.toString(xsM.getRowDimension()));	
		Log.d("CLASSIFIER!", "xs cols: " + Integer.toString(xsM.getRowDimension()));	
		*/
		
		for(int i=0;i<N;i++){
			double[][] interim = new double[Y.length][1];
			for(int j=0;j<Y.length;j++){
				interim[j][0] = Y[j][i];
			}
			testKZ[0][i] = kernel(z,interim);
		}
		
		Log.d("CLASSIFIER!", "pre testKZM");
		
		Matrix testKZM = new Matrix(testKZ);
		
		Log.d("CLASSIFIER!", "testKZ Matrix Created");
		
				
		double[][] IS = new double[1][T0];
		Matrix ISM = new Matrix(IS);
		
		int ISlength = 0;
		
		Matrix xsM = new Matrix(K);
		
		Log.d("CLASSIFIER!", "ISM and xsM Created");
		
		for(int s = 0; s<T0 ; s++){

			
			Log.d("CLASSIFIER!", "testKZ rows: " + Integer.toString(testKZM.getRowDimension()));	
			Log.d("CLASSIFIER!", "testKZ cols: " + Integer.toString(testKZM.getRowDimension()));	
			Log.d("CLASSIFIER!", "IS rows: " + Integer.toString(ISM.getRowDimension()));	
			Log.d("CLASSIFIER!", "IS cols: " + Integer.toString(ISM.getRowDimension()));	
			Log.d("CLASSIFIER!", "xs rows: " + Integer.toString(xsM.getRowDimension()));	
			Log.d("CLASSIFIER!", "xs cols: " + Integer.toString(xsM.getRowDimension()));
			
			//Compute the tau vector
			Matrix alpha = zhatM.transpose();
			Log.d("CLASSIFIER!", "post trans");
			Matrix beta = alpha.times(KM);
			Log.d("CLASSIFIER!", "post mult");
			Matrix gamma = testKZM.minus(beta);
			Log.d("CLASSIFIER!", "post minus");
			Matrix tau = gamma.times(AM);
			
			Log.d("CLASSIFIER!", "post times");
			
			//Set tau at previously found indices to 0
			for(int a = 0; a<s; a++){
				tau.set(0, (int) ISM.get(0,a), 0.0);
			}
			
			Log.d("CLASSIFIER!", "tau zeroed about to find next largest index");
			
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
			Matrix AsubIS1 = new Matrix(AM.getRowDimension(), AM.getColumnDimension());
			AsubIS1 = AM.copy();
			double[][] iscopy = ISM.getArrayCopy();
			int[] colindices = new int[ISlength+1];
			for(int i=0; i<colindices.length; i++){
				colindices[i] = (int) iscopy[0][i];
			}
			Matrix AsubIS = new Matrix(N,ISlength+1);
			
			AsubIS = AM.getMatrix(0,N-1,colindices);
			
			ISlength++;
			
			//calculate xs
			Matrix M1 = AsubIS.transpose();
			Matrix M2 = KM.times(AsubIS);
			Matrix M3 = M1.times(M2);
			Matrix M4 = M3.inverse();
			Matrix M5 = testKZM.times(AsubIS);
			Matrix M6 = M5.transpose();
			xsM = M4.times(M6);
			//xsM = ( AsubIS.transpose().times(KM.times(AsubIS)).inverse() ).times(testKZM.times(AsubIS).transpose());
			
			//calculate zhat
			zhatM = AsubIS.times(xsM);			
			
		}
		
		int colA = A[0].length;
		double[][] x = new double[colA][1];
		Matrix xM = new Matrix(x);
		for(int i=0; i<ISM.getColumnDimension(); i++){
			xM.set((int) ISM.get(0, i), 0, xsM.get(i, 0));
		}
		
		//calculate residual
		Matrix b = testKZM.timesEquals(2.0).times(AM).times(xM);
		Matrix c = xM.transpose().times(AM.transpose()).times(KM).times(AM).times(xM);
		
		Matrix Residual = KZZM.minus(b).plus(c);		
		
		//return residual
		return Residual.get(0,0);
	}
	

	
	
}
