package org.ecoinformatics.oboe.util;

import java.util.Random;

public class MyRandom{

	final static Random randomGenerator = new Random();
	
	public static int getRand(int minv, int maxv)
	{
		int randomDouble = minv+randomGenerator.nextInt(maxv-minv); 
		return randomDouble;
	}

}
