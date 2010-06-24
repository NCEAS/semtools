package org.ecoinformatics.oboe.query;

public class Constant {
	
	//used for passing query files
	public static String QUERY_COMMENT_PREFIX = "#";
	public static String QUERY_START = "BEGIN QUERY:";
	public static String QUERY_END = "END QUERY";
	
	public static String BASIC_QUERY_START = "BEGIN QO:";
	public static String BASIC_QUERY_END = "END QO";
	public static String BASIC_QUERY_ENTITY = "EntityId:";
	
	public static String MEASUREMENT_START = "BEGIN QM:";
	public static String MEASUREMENT_END = "END QM";
	
	public static String CONTEXT_START = "BEGIN CONTEXT";
	public static String CONTEXT_END = "END CONTEXT";
	
	public static String CHARACTERISTIC = "Characteristic:";
	public static String STANDARD = "Standard:";
	public static String COND = "Cond:";
	public static String AGGREGATION = "Aggregation:";
	public static String DNFNO = "DNF:";
	public static String CONTEXT_SEPARATOR = "->";
	//
	
	public static String QUERY_MEAS_CHA = "cha";
	
	//Query strategy, this need to be consistent with the values used to call QueryProcessor.main function
	public static int QUERY_REWRITE = 1;
	public static int QUERY_MATERIALIZED_DB_MIN_STRATEGY = 2;
	public static int QUERY_MATERIALIZED_DB_MAX_STRATEGY = 5;
	//public static int QUERY_PARTIAL_MATERIALIZED_DB = 3;

}
