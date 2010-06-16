package org.ecoinformatics.oboe.syntheticdataquery;

public class DataStatistics {

	//m1,m2 100%, i.e., they appear in all the tables
	//m3,m4 80%, i.e., they appear in 80% of the tables (e.g., when there are 100 tables, 80 tables have m3 and m4)
	//m5,m6 50%, similar
	//m7,m8 20%
	//m9,m10 10%
	//m11,m12 5%
	//m13,m14 1%
	protected static double[] probability = new double[]{1.0,1.0,0.8,0.8,0.5,0.5,0.2,0.2,0.1,0.1,0.05,0.05,0.01,0.01};
}
