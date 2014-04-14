package com.example.myfirstapp;

import Jama.Matrix;
import android.util.Log;

public class Classify {
	float [][] Y; // train
	float [][] K;
	float [][] A; // dictionary
	int T0;
	
	public Classify(){
		
	
	}
	
	private float[][] matrixMult(float[][] array1, float[][] array2)
	{
		//assume dim[1][] = choose column; dim[][2] = choose row

		int m = array1[0].length;
		int p = array2.length;
		int n1 = array1.length;
		int n2 = array2[0].length;
		
		float [][] prod = new float[p][m];
		
		if(n1!=n2)
		{
			//you fools
			///Log.d("mult","You have failed");
			return null;
		}
		else
		{
			//Log.d("mult","else executed");
			for(int r1 = 0; r1 < m; r1++){
				for(int c2 = 0; c2 < p; c2++){
						float sum = 0;
						for(int x = 0; x < n1; x++){	
							sum += array1[x][r1] * array2[c2][x];							
						}					
						prod[c2][r1] = sum;
				}
			}
			return prod;			
		}		
	}
	
	private float[][] tranRow2Col(float[] mat){  // TEST LATER----------------------------------------
		float [][] result = new float[0][mat.length];
		for(int c=0;c<mat.length;c++){
			result[0][c] = mat[c];
		}
		return result;
	}
	
	private float[][] transpose(float[][] mat){  // TEST LATER----------------------------------------
		float [][] result = new float[mat[0].length][mat.length];
		for(int c=0;c<mat.length;c++){
			for(int r=0;r<mat[0].length;r++){
				result[c][r] = mat[r][c];
			}
		}
		return result;
	}

	
	private float[][] addSub(float[][] a, float[][] b, int operation){  //if 0 add if 1 subtract
		float[][] result = new float[a.length][a[0].length];
		if(operation == 0){
			//do add
			for(int i=0;i<a.length;i++){
				for(int j=0;j<a[0].length;j++){
					result[i][j] = a[i][j] + b[i][j];
				}
			}
		}
		else{
			//do subtract
			for(int i=0;i<a.length;i++){
				for(int j=0;j<a[0].length;j++){
					result[i][j] = a[i][j] - b[i][j];
				}
			}
		}
		return result;
	}
	
	private float[] mat2RowVect(float[][] a){
		float[] result = new float[a.length];
		for(int i=0;i<a.length;i++){
			result[i]=a[i][0];
		}
		return result;
	}
	
	public float kernel(float[][] x, float[][] y){
		
		int degree = 5;
		int c = 1;
		
		float sum = 0;
		
		for(int i=0;i<x.length;i++){
			sum += x[i][0]*y[i][0];
		}		
		
		sum = (float) Math.pow(sum+1,degree);		
		
		return sum;
	}	
	
	public float [][] classify(float [][] z){
				
		int N = Y[0].length;
		T0 = 10; //TODO: Arbitrary fix this later
		double[][] zhat = new double[N][1];
		double[][] Is = new double[T0][1];
		double [][] testKZ = new double [N][1];
		double [][] K = new double [1][1];
		
		Matrix KM = new Matrix(K);
		
		
		for(int i =0;i<zhat.length;i++) zhat[i][0] = 0;
		
		Matrix zhatM = new Matrix(zhat);
		
		for(int i=0;i<N;i++){
			float[][] interim = new float[0][Y[i].length];
			for(int j=0;j<Y[i].length;j++){
				interim[0][j] = Y[i][j];
			}
			testKZ[i][0] = kernel(z,interim);
		}
		Matrix testKZM = new Matrix(testKZ);
		
		double Kzz = kernel(z,z);
		
		for(int s = 0; s<T0 ; s++){
			float[][] tau = new float[A.length][1];
			matrixMult(addSub(testKZ, matrixMult(transpose(zhat), K), 1), A);
			Matrix alpha = zhatM.transpose();
			Matrix beta = alpha.times(KM);
		}
		
		
		
		
		
		
		
		/*
		for(int i =0;i<zhat.length;i++) zhat[i][0] = 0;
		
		float[][] Is; // index set
		
		float [][] testKZ = new float [N][1];
		for(int i=0;i<N;i++){
			float[][] interim = new float[0][Y[i].length];
			for(int j=0;j<Y[i].length;j++){
				interim[0][j] = Y[i][j];
			}
			testKZ[i][0] = kernel(z,interim);
		}
		
		float Kzz = kernel(z,z);
		
		for(int s = 0; s<T0 ; s++){
			float[][] tau = new float[A.length][1];
			matrixMult(addSub(testKZ, matrixMult(transpose(zhat), K), 1), A);
		}
		
		*/
		
		

		return null;
	}
	

	
	
}
