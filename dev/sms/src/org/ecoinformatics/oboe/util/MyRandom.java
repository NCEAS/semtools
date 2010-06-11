package org.ecoinformatics.oboe.util;

import java.util.Random;

public class MyRandom{

	final static Random randomGenerator = new Random();
	final static double gaussian_max = 3.0;
	final static double gaussian_span = 6.0;
	
	/**
	 * generate a random number in [minv, maxv) satisfying uniform distribution
	 * @param minv
	 * @param maxv
	 * @return
	 */
	public static int getRand(int minv, int maxv)
	{
		int randomInt = minv+randomGenerator.nextInt(maxv-minv); 
		return randomInt;
	}
	
	/**
	 * generate a random number in [minv, maxv) satisfying Gaussian distribution
	 * @param minv
	 * @param maxv
	 * @return
	 */
	public static int getGaussianInt(int minv, int maxv){
		double offsetFromZero = 1.0; 
		//generate a double number in [0.0,1.0) in Gaussian distribution
		while(offsetFromZero<0.0||offsetFromZero>=1.0){
			double rand = randomGenerator.nextGaussian(); //mean 0.0, deviation 1.0
			offsetFromZero = (rand +gaussian_max)/gaussian_span; //a random number in [0,1.0) satisfying normal distribution
		}
		int randInt = minv+(int)(offsetFromZero*(maxv-minv)); //scale this number to be in [minv, maxv)
		
		return randInt;
	}

}
