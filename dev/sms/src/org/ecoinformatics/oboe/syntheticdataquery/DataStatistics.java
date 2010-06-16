package org.ecoinformatics.oboe.syntheticdataquery;

public class DataStatistics {

	//m1,m2 100%, i.e., they appear in all the tables
	//m3,m4 80%, i.e., they appear in 80% of the tables (e.g., when there are 100 tables, 80 tables have m3 and m4)
	//m5,m6 50%, similar
	//m7,m8 20%
	//m9,m10 10%
	//m11,m12 5%
	//m13,m14 1%
	protected static double[] m_probability = new double[]{1.0,1.0,0.8,0.8,0.5,0.5,0.2,0.2,0.1,0.1,0.05,0.05,0.01,0.01};
	
	protected static String[] m_measurements = new String[]{"m1","m2","m3","m4","m5","m6","m7","m8","m9","m10","m11","m12","m13","m14"};

	//the ratio of the unique values of the key measurements among all the values 
	protected static double m_key_uniquevalue_ratio = 0.5;
	
	//each observation can have [1,5] (i.e., [3-2, 3+2]) measurements 
	protected static int m_measnum_per_obs_mean = 3;
	protected static int m_measnum_per_obs_var = 2;
}
