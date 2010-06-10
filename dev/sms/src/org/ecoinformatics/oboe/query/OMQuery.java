package org.ecoinformatics.oboe.query;

import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Characteristic;

import org.ecoinformatics.oboe.datastorage.MDB;
import org.ecoinformatics.oboe.datastorage.RawDB;
import org.ecoinformatics.oboe.datastorage.PostgresDB;
import org.ecoinformatics.oboe.model.*;
import org.ecoinformatics.oboe.util.Debugger;

//Example queries: 

//  Q3: Tree[Height > 5 Meter] -> Soil[Acidity >= 7pH]: Contextualized query
// This query incorporates context via the "->" (arrow) symbol, which can
//	be read as "contextualized by" or "has context". The query returns
//	datasets that contain at least one Tree observation (with the
//	corresponding height value) where the observation was taken within the
//	context of a Soil observation (with the corresponding acidity value).

//	Q4: (Tree[Height], Plant[Biomass]) -> (Plot[Name], Air[Temp]) -> Site[Name]
//  Contextualized query: list of simple queries (or simple query )
//
//	This query returns datasets that have Tree and Plant observations with
//	Height and Biomass measurements, respectively, where these are
//	contextualized by the same Plot and Air observations, and these Plot
//	and Air observations are contextualized by the same Site observation.
//
//	Tree[Height] -> Plot[Name], (Air[Temp] -> Site[Name])
// A list of OMquery
//
//	This query shows a different grouping for context relationships: Tree
//	observations are contextualized by a Plot observation and an Air
//	observation, where the Air observation is contextualized by a Site
//	observation (but the Plot observation is not).

public class OMQuery {
	List<OMQueryBasic> m_query=null; 
	Map<String, OMQueryBasic> m_queryIndex; 
	
	//this is null or size is zero, then, no context
	//All the queries in the structure are formed using context
	//Otherwise, they are just put together using logic OR.
 	Map<String, List<String> > m_queryContext; 

 	public OMQuery()
 	{
 		m_query = new ArrayList<OMQueryBasic>();
 		//m_queryIndex = new TreeMap<String, List<OMQueryBasic> >();
 		m_queryIndex = new TreeMap<String, OMQueryBasic>();
 		m_queryContext = new TreeMap<String, List<String>>();
 	}
	/**
	 * Parse the query text to a query structure
	 * @param queryText
	 * @throws Exception 
	 */
	public void parse(List<String> queryLines) 
		throws Exception
	{
		
		String oneLine = "";
		if(queryLines.size()<=2){
			throw new Exception("Query is not valid, query contains less then two lines"+queryLines);
		}
		int i=0;
		while(i<queryLines.size()){
			oneLine = queryLines.get(i);
			//1. parse the basic query
			if(oneLine.contains(Constant.BASIC_QUERY_START)){
				OMQueryBasic oneBasicQuery = new OMQueryBasic();
			
				//1.1. parse entity
				String queryLabel = oneLine.substring(Constant.BASIC_QUERY_START.length());
				oneBasicQuery.setQueryLabel(queryLabel);//the first row is basic query id
				
				String entityName = queryLines.get(++i).substring(Constant.BASIC_QUERY_ENTITY.length());				
				oneBasicQuery.setEntityTypeName(entityName);
				
				//1.2. parse all the measurements belong to this entity				
				while(i<queryLines.size()){
					oneLine = queryLines.get(++i);
					if(!oneLine.contains(Constant.MEASUREMENT_START))
						break;
					
					if((i+6)>queryLines.size()){
						throw new Exception("Query is not valid, i="+i+",queryLines.size="+queryLines.size());
					}
					String cha = queryLines.get(++i).substring(Constant.CHARACTERISTIC.length());
					String standard = queryLines.get(++i).substring(Constant.STANDARD.length());
					String condition = queryLines.get(++i).substring(Constant.COND.length());
					String aggregationFunc = queryLines.get(++i).substring(Constant.AGGREGATION.length());
					String DNFnoStr = queryLines.get(++i).substring(Constant.CNFNO.length());
					
					int dnfNo = Integer.parseInt(DNFnoStr);
					QueryMeasurement queryMeas = new QueryMeasurement();
					queryMeas.setCharacteristicCond(cha);
					queryMeas.setStandardCond(standard);
					queryMeas.setValueCond(condition);
					queryMeas.setAggregationFunc(aggregationFunc);
					System.out.println(Debugger.getCallerPosition()+"dnfNo="+dnfNo+",queryMeas: "+queryMeas);
					
					oneBasicQuery.addMeasDNF(dnfNo, queryMeas);
					System.out.println(Debugger.getCallerPosition()+"oneBasicQuery: "+oneBasicQuery);
					
					oneLine = queryLines.get(++i);
					if(oneLine.equals(Constant.MEASUREMENT_END)==false){
						throw new Exception("Query is in valid, measurement does not have end symbol.");
					}
				}
				
				if(oneLine.equals(Constant.BASIC_QUERY_END)==false){
					throw new Exception("Query is in valid, entity does not have end symbol.");
				}
				m_query.add(oneBasicQuery);
				//List<OMQueryBasic> tmplist = m_queryIndex.get(oneBasicQuery.getQueryLabel());
				//if(tmplist == null){
				//	tmplist = new ArrayList<OMQueryBasic>();
				//	m_queryIndex.put(oneBasicQuery.getQueryLabel(),tmplist);
				//}
				//tmplist.add(oneBasicQuery);
				m_queryIndex.put(oneBasicQuery.getQueryLabel(), oneBasicQuery);
			}else if(oneLine.contains(Constant.CONTEXT_START)){
				//parse the context
				oneLine = queryLines.get(++i);
				while(!oneLine.equals(Constant.CONTEXT_END)){
					int pos = oneLine.indexOf(Constant.CONTEXT_SEPARATOR);
					String bq1str = oneLine.substring(0, pos);
					String bq2str = oneLine.substring(pos+Constant.CONTEXT_SEPARATOR.length());
					List<String> tmplist = m_queryContext.get(bq1str);
					if(tmplist==null){
						tmplist = new ArrayList<String>();
						m_queryContext.put(bq1str,tmplist);
					}
					tmplist.add(bq2str);
					//m_queryContext.put(bq1str,bq2str);
					oneLine = queryLines.get(++i);
				}
			}else{
				throw new Exception("Query is in valid, It contains another section:"+oneLine);
			}
			
			++i;
		}
		
		//return resultQueryDFN;
	}
	
	/**
	 * Set a test query: with TaxonomicTypeName characteristic = Picea rubens
	 */
	public void setTest()
	{
		OMQueryBasic q = new OMQueryBasic();
		q.setQueryLabel("test");
		q.setEntityTypeName("gce:Tree");
		QueryMeasurement queryMeas = new QueryMeasurement();
		queryMeas.setCharacteristicCond("=TaxonomicTypeName");
		queryMeas.setStandardCond(null);
		queryMeas.setValueCond("=Picea rubens");
		q.addMeasDNF(1, queryMeas);
	}
	
	/**
	 * Convert a query to a string, for output purpose
	 */
	public String toString()
	{
		String str="";

		for(int i=0;i<m_query.size();i++){
			str+=m_query.get(i).toString()+", ";
		}
		str+="Context: ";
		str+=this.m_queryContext.toString();
		
		return str;
	}
	
	/**
	 * Get the context chains from the OM Query
	 * 
	 * @return
	 */
	public List<ContextChain> getContextChains()
	{
		List<ContextChain> resultContextChain = new ArrayList<ContextChain>();
		
		Set<Entry<String, List<String> >> entrySet = m_queryContext.entrySet();
		Set<String> basicQueryInContext = new HashSet<String>();
		
		//1.1 grouping the basic queries
		List<Set<String> > tmpBasicQueryGroup = new ArrayList<Set<String> >();
		for(Entry<String, List<String> > entry: entrySet){
			insertBasicQuery(tmpBasicQueryGroup,entry);
			basicQueryInContext.add(entry.getKey());
			basicQueryInContext.addAll(entry.getValue());
		}
				
		//1.2 put the group context to chains
		for(int i=0;i<tmpBasicQueryGroup.size();i++){
			ContextChain newChain = new ContextChain();
			Set<String> curBQset = tmpBasicQueryGroup.get(i);
			for(String keyQueryLabel: curBQset){
				List<String> valueQueryLabelList = m_queryContext.get(keyQueryLabel);
				if(valueQueryLabelList==null) continue;
				for(String valueQueryLabel: valueQueryLabelList){
					OMQueryBasic qKey = m_queryIndex.get(keyQueryLabel);				
					OMQueryBasic qValue = m_queryIndex.get(valueQueryLabel);
					newChain.addGroup(qKey,qValue);				
				}
			}
			resultContextChain.add(newChain);
		}
		
		//2. insert the basic queries that are not in the context to a separate set
		for(OMQueryBasic query: m_query){
			if(!basicQueryInContext.contains(query.getQueryLabel())){
				ContextChain newChain = new ContextChain();
				newChain.addGroup(query);//add this query as a single group
				resultContextChain.add(newChain);
			}
		}
		
		return resultContextChain;
	}
	
	
	/**
	 * put entry (observation query, context observation query) to the query group
	 * 
	 * @param tmpBasicQuery
	 * @param entry
	 */
	private void insertBasicQuery(List<Set<String> > tmpBasicQuery, Entry<String, List<String> > entry)
	{
		boolean inserted = false;		
		String bq1 = entry.getKey();
		
		//check whether some of entry's string can be inserted to the list or not
		for(int i=0;i<tmpBasicQuery.size();i++){
			Set<String> curBQset = tmpBasicQuery.get(i);
			boolean exist = curBQset.contains(bq1);
			for(String bq2: entry.getValue()){
				if(!exist)	exist = curBQset.contains(bq2);
					
				if(exist){
					curBQset.add(bq1);
					curBQset.add(bq2);
					inserted = true;
					break;
				}
			}
		}
		
		//this pair does not belong to any existing group
		if(inserted==false){
			Set<String> curBQset  = new HashSet<String>();
			curBQset.add(bq1);
			for(String bq2: entry.getValue())
				curBQset.add(bq2);		
			tmpBasicQuery.add(curBQset);
		}
	}
	
//	/**
//	 * Get the list of query measurement instances
//	 * 	
//	 * @return
//	 */
//	public List<QueryMeasurement> getQueryMeasements(){
//		List<QueryMeasurement> queryMeasList = new ArrayList<QueryMeasurement>();
//		queryMeasList.add(m_queryMi);
//		
//		return queryMeasList;
//	}
	
//	/**
//	 * Get the measurement conditions
//	 * @return
//	 */
//	public String getQueryMeasurementString()
//	{
//		String str = "";
//		
//		//TODO: a lot more to do
//		List<QueryMeasurement> queryMeasList = getQueryMeasements();
//		
//		for(int i=0;i<queryMeasList.size();i++){
//			QueryMeasurement qm = queryMeasList.get(i);
//			Measurement mt = qm.getMeasurementType();
//			Characteristic cha = null;
//			if(mt.getCharacteristics()!=null){
//				cha = mt.getCharacteristics().get(0);
//				str += ("mi.mlabel= mt.mtype AND mt.characteristic='"+ cha+"'");
//			}
//			String cond = qm.getCondition();
//			if(cha!=null&&cond!=null){
//				str += (" AND mi.mvalue '"+ cond+"'");
//			}
//		}
//		return str;
//	}
	
	/**
	 * Perform a query over the materialized database
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	private Set<OboeQueryResult> execute(PostgresDB db, boolean resultWithRecord) 
		throws Exception
	{
		Set<OboeQueryResult> resultSet = new TreeSet<OboeQueryResult>();
		
		//open database connection
		db.open();
		
		//The results of each context query should be unioned
		List<ContextChain> contextQueryDNF = getContextChains();
		System.out.println(Debugger.getCallerPosition()+"contextQueryDNF = "+contextQueryDNF);
		for(int i=0;i<contextQueryDNF.size(); i++){
			ContextChain oneContextQuery = contextQueryDNF.get(i);
			
			System.out.println("---------------\n"+Debugger.getCallerPosition()+"["+(i+1)+"/"+contextQueryDNF.size()+
					"] contextQuery:"+oneContextQuery);
					
			Set<OboeQueryResult> oneDNFqueryResultSet = oneContextQuery.execute(db, resultWithRecord);
		
			System.out.println(Debugger.getCallerPosition()+"OMQuery DNF "+i+",oneDNFqueryResultSet size="+oneDNFqueryResultSet.size());
			resultSet.addAll(oneDNFqueryResultSet);			
		}
		
		System.out.println(Debugger.getCallerPosition()+"OMQuery result size="+resultSet.size());
		//close database connection
		db.close();
		
		return resultSet;		
	}
	
//	/**
//	 * Perform a query over the original database
//	 * 
//	 * @param query
//	 * @return
//	 * @throws Exception
//	 */
//	private Set<OboeQueryResult> execute(RawDB rawdb, boolean resultWithRecord) 
//		throws Exception
//	{
//		Set<OboeQueryResult> resultSet = new TreeSet<OboeQueryResult>();
//		
//		//open database connection
//		rawdb.open();
//		
//		//The results of each context query should be unioned
//		List<ContextChain> contextChains = getContextChains();
//		System.out.println(Debugger.getCallerPosition()+"contextChains = "+contextChains);
//		
//		
//		for(int i=0;i<contextChains.size(); i++){
//			ContextChain oneContextQuery = contextChains.get(i);
//			
//			Set<OboeQueryResult> oneDNFqueryResultSet = oneContextQuery.execute(rawdb, resultWithRecord);
//		
//			resultSet.addAll(oneDNFqueryResultSet);			
//		}
//		
//		System.out.println(Debugger.getCallerPosition()+"OMQuery result="+resultSet);
//		
//		//close database connection
//		rawdb.close();
//		
//		return resultSet;		
//	}
	
	/**
	 * Based on different query evaluation strategy, perform query.
	 * 
	 * @param queryStrategy
	 * @return
	 * @throws Exception 
	 */
	public Set<OboeQueryResult> execute(int queryStrategy, boolean resultWithRecord) 
		throws Exception
	{
		Set<OboeQueryResult> queryResultSet = null;
		if(queryStrategy == Constant.QUERY_REWRITE){
			RawDB rawdb = new RawDB();
			queryResultSet = execute(rawdb,resultWithRecord);
		}else if(queryStrategy == Constant.QUERY_MATERIALIZED_DB){
			MDB materializedDB = new MDB();
			queryResultSet = execute(materializedDB,resultWithRecord);			
		}else if(queryStrategy == Constant.QUERY_PARTIAL_MATERIALIZED_DB){
			System.out.println(Debugger.getCallerPosition() + "To come...");
			System.exit(0);
		}
		
		System.out.println(Debugger.getCallerPosition()+"Total queryResultSet size="+queryResultSet.size());
		return queryResultSet;
	}
}
