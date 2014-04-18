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
	double [][] Y; // Training Matrix
	double [][] K; // Kernel Matrix
	double [][] A; // Dictionary Matrix
	int T0;
	String dictionaryPath;
	String trainingPath;
	int N;
	
	public Classify(int t0, int fileForDictandTrain){
		T0=t0; //Initialize T0
		
/* FILE READING FROM CSVs which contain training and dictionary matrices */
		
		
		//Initialize the file paths for all classes
		switch(fileForDictandTrain){
		
		//TODO: Remove the hard coding of sizes and perhaps use a container or iterator of some kind
		
		//Car class
		case 0: 
			dictionaryPath = Environment.getExternalStorageDirectory().toString()+"/carA.csv";
			trainingPath = Environment.getExternalStorageDirectory().toString()+"/carY.csv";
			Y = new double[6][40];
			A = new double[40][120];
			break;
		
		//Silence class
		case 1:
			dictionaryPath = Environment.getExternalStorageDirectory().toString()+"/silent_aptA.csv";
			trainingPath = Environment.getExternalStorageDirectory().toString()+"/silent_aptY.csv";
			Y = new double[6][40];
			A = new double[40][120];
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
		
		
		K = new double[Y[0].length][Y[0].length];  //Initialize the size of the Kernel Matrix
		
		//Create the Kernel Matrix from the columns of Y
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
		
		N = Y[0].length;
		
	}
	
	public double kernel(double[][] x, double[][] y){
		
		//double degree = 5;
		//double c = 1;
		double sigma = 1000.0;
		
		double sum = 0;
		
		//Gaussian kernel implementation
		//K(x, y) = exp(-||x-y||^2 / c)
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
		
		//Declare and Instantiate 2D arrays
		double[][] zhat = new double[N][1];
		double [][] testKZ = new double [1][N];			
		double[][] Kzz = new double[1][1];
		Kzz[0][0] = kernel(z,z);
		double[][] IS = new double[1][T0];
		
		//Assign values to testKZ 2d array
		for(int i=0;i<N;i++){
			double[][] interim = new double[Y.length][1];
			for(int j=0;j<Y.length;j++){
				interim[j][0] = Y[j][i];
			}
			testKZ[0][i] = kernel(z,interim);
		}
		
		//Assign all zhat values to 0
		for(int i =0;i<zhat.length;i++) zhat[i][0] = 0;
		
		//Create matrices using 2D arrays
		Matrix KM = new Matrix(K);
		Matrix AM = new Matrix(A);
		Matrix KZZM = new Matrix(Kzz);
		Matrix zhatM = new Matrix(zhat);
		Matrix testKZM = new Matrix(testKZ);
		Matrix ISM = new Matrix(IS);
		Matrix xsM = new Matrix(T0, 1);
		
		
		//begin the loop to determine T0 largest values in tau
		int ISlength = 0;
		for(int s = 0; s<T0 ; s++){

			//Compute the tau vector
			//tau = (testKz - zhat'*K)*A
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
			
			//Add the next largest index to ISM
			ISM.set(0, s, largestIndex);
			
			//find the indices of the largest values in A
			double[][] iscopy = ISM.getArrayCopy();
			int[] colindices = new int[ISlength+1];
			for(int i=0; i<colindices.length; i++){
				colindices[i] = (int) iscopy[0][i];
			}
			
			//Create A(:IS) and use to calculate xs
			Matrix AsubIS = new Matrix(N,ISlength+1);
			AsubIS = AM.getMatrix(0,N-1,colindices);
			
			ISlength++;
			
			//calculate xs
			//xs = inv(A(:,Is)' * K*A(:,Is)) * (testKz*A(:,Is))';
			Matrix M1 = AsubIS.transpose();
			Matrix M2 = KM.times(AsubIS);
			Matrix M3 = M1.times(M2);
			
			if(M3.det() <= .000000001 && M3.det() >= -.000000001){
				return -1.0;
			}
			
			Matrix M4 = M3.inverse();
			Matrix M5 = testKZM.times(AsubIS);
			Matrix M6 = M5.transpose();
			xsM = M4.times(M6);
			
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
