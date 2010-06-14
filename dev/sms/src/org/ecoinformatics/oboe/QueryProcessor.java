package org.ecoinformatics.oboe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.ecoinformatics.oboe.Constant;
import org.ecoinformatics.oboe.query.OMQuery;
import org.ecoinformatics.oboe.query.QueryList;
import org.ecoinformatics.oboe.query.OboeQueryResult;
import org.ecoinformatics.oboe.query.OboeQueryResultContainer;
import org.ecoinformatics.oboe.util.Debugger;

public class QueryProcessor {

	private static void usage(){
		System.out.println("Usage: ./QueryProcessor <1. query file> <2. query strategy> <3. dbname> <4.result form>" +
			"[5. query result file name]");
		System.out.println("\t <2. Query stragety> 1-query rewriting; 2-query materialized db; 3-others; ");
		//System.out.println("\t <3. dbname> ");
		System.out.println("\t <4. result form> 0: only has dataset id; 1, has dataset id and record id");
	}
	public static void main(String[] args) throws Exception {
		if(args.length<4||args.length>5){
			usage();
			return;
		}
		
		long t1 = System.currentTimeMillis();
		
		//1. process parameters
		String queryFile = Constant.localUriPrefix+args[0];
		int queryStrategy = Integer.parseInt(args[1]);
		if(queryStrategy<=0||queryStrategy>3){
			System.out.println("Wrong query strategy!");
			usage();
			return;
		}
		String dbname = args[2];
		
		int resultFormat = Integer.parseInt(args[3]);
		if(resultFormat!=0&&resultFormat!=1){
			System.out.println("Wrong result format!");
			usage();
			return;
		}
		boolean resultWithRecord = (resultFormat==1); //=1 means the result need to have record id
		
		String queryResultFile = "";
		if(args.length==5){
			queryResultFile = args[4];
		}
		
		System.out.println(Debugger.getCallerPosition()+"1. queryFile="+queryFile);
		System.out.println(Debugger.getCallerPosition()+"2. queryStrategy="+queryStrategy);
		System.out.println(Debugger.getCallerPosition()+"3. dbname="+dbname);
		System.out.println(Debugger.getCallerPosition()+"4. resultWithRecord="+resultWithRecord);
		System.out.println(Debugger.getCallerPosition()+"5. queryResultFile="+queryResultFile);
		
		//2. read queries
		QueryList queryList= new QueryList();
		queryList.read(queryFile);
		System.out.println(Debugger.getCallerPosition()+"queryList=\n"+queryList);
		
	 	//3. perform queries
		OboeQueryResultContainer queryResultContainer = new OboeQueryResultContainer();
		for(int i=0;i<queryList.size();i++){
			if(i>0) break;
			
			OMQuery queryI = queryList.getQuery(i);
			System.out.println(Debugger.getCallerPosition()+"\n*****\nProcess query: "+queryI+"\n********");
			
			Set<OboeQueryResult> queryResult = queryI.execute(dbname,queryStrategy, resultWithRecord);
						
			System.out.println(Debugger.getCallerPosition()+"Query i="+i+",result size="+queryResult.size());//+":"+queryResult);
			queryResultContainer.add(queryResult);
		}
		
		long t2 = System.currentTimeMillis();
		
		System.out.println("\n-----------\n");
		if(queryStrategy==org.ecoinformatics.oboe.query.Constant.QUERY_MATERIALIZED_DB){
			System.out.println(Debugger.getCallerPosition() +" Query materialized database.");
		}else if(queryStrategy==org.ecoinformatics.oboe.query.Constant.QUERY_REWRITE){
			System.out.println(Debugger.getCallerPosition() +" Query raw database.");
		}else{
			System.out.println(Debugger.getCallerPosition() +" Query: other strategy.");
		}
		System.out.println(Debugger.getCallerPosition()+"Time used (Query): " 
				+ (t2-t1) +" ms" +" = "+ ((t2-t1)/1000) +"s\n-----------\n");
		
		//4. write the query result to the result file
		queryResultContainer.write(queryResultFile);
		
		long t3 = System.currentTimeMillis();
		
		System.out.println("\n-----------\n"+Debugger.getCallerPosition()+"Time used (Total=query+write out): " 
				+ (t3-t1) +" ms" +" = "+ ((t3-t1)/1000) +"s\n-----------\n");
	}
}