package org.ecoinformatics.oboe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.ecoinformatics.oboe.query.OMQuery;
import org.ecoinformatics.oboe.query.QueryList;
import org.ecoinformatics.oboe.query.OboeQueryResult;
import org.ecoinformatics.oboe.query.OboeQueryResultContainer;

public class QueryProcessor {

	public static void main(String[] args) throws Exception {
		if(args.length<3||args.length>4){
			System.out.println("Usage: ./QueryProcessor <1. query file> <2. query strategy> <3.result form>" +
					"[4. query result file name]");
			System.out.println("\t <2. Query stragety> 1-query rewriting; 2-query materialized db; 3-others; ");
			System.out.println("\t <3. result form> 0: only has dataset id; 1, has dataset id and record id");
			return;
		}
		
		//1. process parameters
		String queryFile = Constant.localUriPrefix+args[0];
		int queryStrategy = Integer.parseInt(args[1]);
		if(queryStrategy<=0||queryStrategy>3){
			System.out.println("<2. Query stragety> 1-query rewriting; 2-query materialized db; 3-others; ");
			return;
		}
		boolean resultWithRecord = (Integer.parseInt(args[2])==1); //=1 means the result need to have record id
		
		String queryResultFile = "";
		if(args.length==4){
			queryResultFile = args[3];
		}
		
		System.out.println(Debugger.getCallerPosition()+"1. queryFile="+queryFile);
		System.out.println(Debugger.getCallerPosition()+"2. queryStrategy="+queryStrategy);
		System.out.println(Debugger.getCallerPosition()+"3. resultWithRecord="+resultWithRecord);
		System.out.println(Debugger.getCallerPosition()+"4. queryResultFile="+queryResultFile);
		
		//2. read queries
		QueryList queryList= new QueryList();
		queryList.read(queryFile);
		System.out.println(Debugger.getCallerPosition()+"queryList="+queryList);
		
	 	//3. perform queries
		OboeQueryResultContainer queryResultContainer = new OboeQueryResultContainer();
		for(int i=0;i<queryList.size();i++){
			OMQuery queryI = queryList.getQuery(i);
			System.out.println(Debugger.getCallerPosition()+"\n*****\nProcess query: "+queryI+"\n********");
			Set<OboeQueryResult> queryResult = queryI.execute(queryStrategy, resultWithRecord);
			System.out.println(Debugger.getCallerPosition()+"i="+i+",result="+queryResult);
			queryResultContainer.add(queryResult);
		}
		
		//4. write the query result to the result file
		queryResultContainer.write(queryResultFile);		
	}
}