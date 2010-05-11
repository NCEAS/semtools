package org.ecoinformatics.oboe;

import java.io.IOException;
import java.util.ArrayList;

import org.ecoinformatics.oboe.query.Query;
import org.ecoinformatics.oboe.query.QueryList;
import org.ecoinformatics.oboe.query.OboeQueryResult;
import org.ecoinformatics.oboe.query.OboeQueryResultContainer;

public class QueryProcessor {

	public static void main(String[] args) throws Exception {
		if(args.length<2||args.length>3){
			System.out.println("Usage: ./QueryProcessor <1. query file> <2. query strategy> [3. query result file name]");
			System.out.println("\t <2. Query stragety> 1-query rewriting; 2-query materialized db; 3-others; ");
			return;
		}
		
		//1. process parameters
		String queryFile =args[0];
		int queryStrategy = Integer.parseInt(args[1]);
		if(queryStrategy<=0||queryStrategy>3){
			System.out.println("<2. Query stragety> 1-query rewriting; 2-query materialized db; 3-others; ");
			return;
		}
		String queryResultFile = "";
		if(args.length==3){
			queryResultFile = args[2];
		}
		
		System.out.println("1. queryFile="+queryFile);
		System.out.println("2. queryStrategy="+queryStrategy);
		System.out.println("3. queryResultFile="+queryResultFile);
		
		//2. read queries
		QueryList queryList= new QueryList();
		//queryList.read(queryFile);
		queryList.setTest();
		System.out.println(Debugger.getCallerPosition()+"queryList="+queryList);
		OboeQueryResultContainer queryResultList = new OboeQueryResultContainer(); 
		
		//3. perform queries
		for(int i=0;i<queryList.size();i++){
			Query queryI = queryList.getQuery(i);
			OboeQueryResult queryResult = queryI.execute(queryStrategy);
			queryResultList.add(queryResult);
		}
		
		//4. write the query result to the result file
		queryResultList.write(queryResultFile);		
	}
}