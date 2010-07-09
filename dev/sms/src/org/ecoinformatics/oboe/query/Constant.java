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
	public static String AGGREGATION_COND = "AggCond:";
	public static String DNFNO = "DNF:";
	public static String CONTEXT_SEPARATOR = "->";
	final public static String DEFAULT_AGGREGATION_FUNC = "count";
	//
	
	public static String QUERY_MEAS_CHA = "cha";
	
	//Query strategy, this need to be consistent with the values used to call QueryProcessor.main function
	public static int QUERY_REWRITE = 1;
	public static int QUERY_MATERIALIZED_DB_MIN_STRATEGY = 2;
	public static int QUERY_MATERIALIZED_DB_MAX_STRATEGY = 5;
	
	public static int QUERY_REWRITE_HOLISTIC = 11;
	public static int QUERY_MATERIALIZED_DB_MIN_STRATEGY2 = 12;
	public static int QUERY_MATERIALIZED_DB_MAX_STRATEGY2 = 15;
	
	//get the meta data for tables one by one, this would be the worst
	public static int QUERY_REWRITE_HOLISTIC_TB_ONE_BY_ONE = 101; 

}
