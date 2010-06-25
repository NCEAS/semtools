package org.ecoinformatics.oboe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.ecoinformatics.oboe.Constant;
import org.ecoinformatics.oboe.query.OMQuery;
import org.ecoinformatics.oboe.query.QueryList;
import org.ecoinformatics.oboe.query.OboeQueryResult;
import org.ecoinformatics.oboe.query.OboeQueryResultContainer;
import org.ecoinformatics.oboe.util.Debugger;

public class QueryProcessor {

	private static String m_param_query_file;
	private static int m_param_query_strategy;
	private static String m_param_dbname;
	private static boolean m_param_result_with_record = false;
	private static String m_param_query_result_file = "";
	
	private static void usage(){
		System.out.println("Usage: ./QueryProcessor <1. query file> <2. query strategy> <3. dbname> <4.result form>" +
			"[5. query result file name]");
		System.out.println("\t <2. Query stragety>\n" +
				"\t1-query rewriting over RawDB;" +
				"\t2-query materialized db (raw, mvalue is varchar);" +
				"\t3-query materialized db (2+mvalue is partitioned based on its type);" +
				"\t4-query materialized db (3+merge oi+mi);" + 
				"\t5-query materialized db (3+merge oi+mi+mt);");
		
		//Materialized db: 2/3/4/5
		System.out.println("\t <4. result form> 0: only has dataset id; 1, has dataset id and record id");
	}
	
	/**
	 * Extract parameters
	 * 
	 * @param args
	 * @return
	 */
	private static int processParameter(String[] args){
		if(args.length<4||args.length>5){
			usage();
			return (-1);
		}
		
		//1. process parameters
		m_param_query_file = Constant.localUriPrefix+args[0];
		
		//2. query strategy
		m_param_query_strategy = Integer.parseInt(args[1]);
		if((m_param_query_strategy<=0||m_param_query_strategy>15)){
			System.out.println("Wrong query strategy!");
			usage();
			return (-1);
		}
		
		//(3) database name
		m_param_dbname = args[2];
		
		//(4) result format
		int resultFormat = Integer.parseInt(args[3]);
		if(resultFormat!=0&&resultFormat!=1){
			System.out.println("Wrong result format!");
			usage();
			return (-1);
		}
		m_param_result_with_record = (resultFormat==1); //=1 means the result need to have record id
		
		//(5) output query result file name
		if(args.length==5){
			m_param_query_result_file = args[4];
		}
		
		System.out.println(Debugger.getCallerPosition()+"1. queryFile="+m_param_query_file);
		System.out.println(Debugger.getCallerPosition()+"2. queryStrategy="+m_param_query_strategy);
		System.out.println(Debugger.getCallerPosition()+"3. dbname="+m_param_dbname);
		System.out.println(Debugger.getCallerPosition()+"4. resultWithRecord="+m_param_result_with_record);
		System.out.println(Debugger.getCallerPosition()+"5. queryResultFile="+m_param_query_result_file);
		
		return 0;
	}
	
	/**
	 * Usage see the function
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		//1. process params
		int rc = processParameter(args);
		if(rc<0) return;
		
		long t1 = System.currentTimeMillis();
		//2. read queries
		QueryList queryList= new QueryList();
		queryList.read(m_param_query_file);
		System.out.println(Debugger.getCallerPosition()+"queryList=\n"+queryList);
		
	 	//3. perform queries
		OboeQueryResultContainer queryResultContainer = new OboeQueryResultContainer();
		Map<Integer,Integer> queryno2resultsize = new TreeMap<Integer,Integer>();
		
		for(int i=0;i<queryList.size();i++){
			//if(i>0) break;
			
			OMQuery queryI = queryList.getQuery(i);
			System.out.println("\n"+Debugger.getCallerPosition()+"Process query: "+queryI);
			
			//perform query i
			Set<OboeQueryResult> queryResult = queryI.execute(m_param_dbname,m_param_query_strategy, m_param_result_with_record);
			
			//put the query result size to a map to print out batch process results 
			System.out.println(Debugger.getCallerPosition()+"Query i="+i+",result size="+queryResult.size());//+":"+queryResult);
			queryno2resultsize.put((i+1),queryResult.size());
			
			//put the query results to a container to print the detailed query results
			queryResultContainer.add(queryResult);
		}
		
		long t2 = System.currentTimeMillis();
		
		//4. print hinting information
		System.out.println("\n-----------\n");
		if(m_param_query_strategy>=org.ecoinformatics.oboe.query.Constant.QUERY_MATERIALIZED_DB_MIN_STRATEGY
				&&m_param_query_strategy<=org.ecoinformatics.oboe.query.Constant.QUERY_MATERIALIZED_DB_MAX_STRATEGY){
			System.out.println(Debugger.getCallerPosition() +" Query materialized database. strategy = "+m_param_query_strategy);
		}else if(m_param_query_strategy==org.ecoinformatics.oboe.query.Constant.QUERY_REWRITE){
			System.out.println(Debugger.getCallerPosition() +" Query raw database.");
		}else{
			System.out.println(Debugger.getCallerPosition() +" Query other strategy.");
		}
		
		//5. print time and result size
		System.out.println(Debugger.getCallerPosition()+"Time used (Query): " 
				+ (t2-t1) +" ms" +" = "+ ((t2-t1)/1000) +"s\n-----------\n");
		for(Integer qno: queryno2resultsize.keySet()){
			System.out.println(Debugger.getCallerPosition()+"qno="+qno+", result size="+queryno2resultsize.get(qno)+",queryI="+queryList.getQuery(qno-1));
		}
		System.out.println(Debugger.getCallerPosition()+"AVERAGE time used (Query): " 
				+ (t2-t1)/(queryList.size()) +" ms" +" = "+ ((t2-t1)/(1000*queryList.size())) +"s\n-----------\n");
		
		//6. write the query result to the result file
		queryResultContainer.write(m_param_query_result_file);
		
		long t3 = System.currentTimeMillis();
		
		System.out.println("\n-----------\n"+Debugger.getCallerPosition()+"Time used (Total=query+write out): " 
				+ (t3-t1) +" ms" +" = "+ ((t3-t1)/1000) +"s\n-----------\n");
	}
}