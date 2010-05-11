package org.ecoinformatics.oboe.query;

public class Constant {
	public static String QUERY_COMMENT_PREFIX = "#";
	
	//Query strategy, this need to be consistent with the values used to call QueryProcessor.main function
	public static int QUERY_REWRITE = 1;
	public static int QUERY_MATERIALIZED_DB = 2;
	public static int QUERY_PARTIAL_MATERIALIZED_DB = 3;
	
}
