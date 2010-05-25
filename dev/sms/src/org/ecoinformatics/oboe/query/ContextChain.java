package org.ecoinformatics.oboe.query;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;

import org.ecoinformatics.oboe.Debugger;
import org.ecoinformatics.oboe.datastorage.*;

public class ContextChain {
	Map<OMQueryBasic, OMQueryBasic> m_queryChain; //shall I use set or list or map, TODO
	
	public ContextChain()
	{
		m_queryChain = new TreeMap<OMQueryBasic, OMQueryBasic>();
	}

	public Map<OMQueryBasic, OMQueryBasic> getQueryChain() {
		return m_queryChain;
	}

	public void setQueryChain(Map<OMQueryBasic, OMQueryBasic> mQueryChain) {
		m_queryChain = mQueryChain;
	}
	
	public String toString()
	{
		String str="";
		for(Map.Entry<OMQueryBasic,OMQueryBasic> entry: m_queryChain.entrySet()){
			str += "("+entry.getKey().getQueryLabel();
			if(entry.getValue()!=null){
				str +="->"+entry.getValue().getQueryLabel();
			}
			str += ") ";
		}
		return str;
	}
	
	public void addGroup(OMQueryBasic oneQueryChain){
		m_queryChain.put(oneQueryChain,null);
	}
	
	public void addGroup(OMQueryBasic keyQuery, OMQueryBasic valueQuery){
		m_queryChain.put(keyQuery,valueQuery);
	}
	
//	/**
//	 * Formulate the SQL statement for this context chain with the measurement conditions
//	 * The logic connecting each query should be AND. 
//	 * 
//	 * @return
//	 */
//	private String formQueryMeasurementSQL()
//	{
//		//TODO: 
//		Set<OMQueryBasic> chainQuerySet = new TreeSet<OMQueryBasic>(); 
//		chainQuerySet.addAll(m_queryChain.keySet());
//		chainQuerySet.addAll(m_queryChain.values());
//		System.out.println(Debugger.getCallerPosition()+"chainQuerySet="+chainQuerySet);
//		
//		String outputSQLWhereStr = null;
//		boolean first=true;
//		for(OMQueryBasic query: chainQuerySet){
//			String oneQueryWhereSql = query.formQueryMeasurementSQL();
//			if(oneQueryWhereSql!=null&&oneQueryWhereSql.length()>0){
//				if(!first){
//					outputSQLWhereStr += "AND "+oneQueryWhereSql;
//				}else{
//					outputSQLWhereStr = "("+oneQueryWhereSql;
//					first=false;
//				}
//			}
//		}
//		
//		if(outputSQLWhereStr!=null){
//			outputSQLWhereStr +=")";
//		}
//		return outputSQLWhereStr;
//	}
	
//	/**
//	 * Translate the query for materialized database
//	 * @param materializedDB
//	 * @return
//	 */
//	public String translateQuery(MDB materializedDB, boolean resultWithRecord){
//		String sql = "SELECT DISTINCT did";
//		
//		if(resultWithRecord){
//			sql +=", record_id ";
//		}
//		sql +="FROM "+materializedDB.getMeasInstanceTable()+" AS mi," 
//			+ materializedDB.getMmeasTypeTable() + " AS mt ";
//		String whereSql = formQueryMeasurementSQL();
//		if(whereSql.length()>0){
//			sql +="WHERE " + whereSql;
//		}
//		//TODO: add GROUP BY and HAVING clause
//		sql+=";";
//		System.out.println(Debugger.getCallerPosition()+"\n"+sql);
//		return sql;
//	}
	
	/**
	 * @deprecated
	 */
	public Set<OboeQueryResult> execute1(PostgresDB db, boolean resultWithRecord) 
		throws Exception
	{
		Set<OboeQueryResult> result = null;
		
		if(db instanceof MDB){
			result = execute((MDB)db,resultWithRecord);
		}else if(db instanceof RawDB){
			result = executeOverRawDB((RawDB)db,resultWithRecord);
		}else{
			System.out.println(Debugger.getCallerPosition()+"Not implemented yet.");
		}
		return result;
	}
	
	/**
	 * Execute one context query over original database
	 * 
	 * 1. Get the key measurement (for group by)
	 * 2. Get the characteristics and their related attributes
	 *  
	 * @deprecated
	 * @param db
	 * @param resultWithRecord
	 * @return
	 * @throws Exception
	 * 
	 */
	private Set<OboeQueryResult> executeOverRawDB(RawDB rawdb, boolean resultWithRecord) 
		throws Exception
	{
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//TODO: need to use a set or a map for chain queries??? 
		Set<OMQueryBasic> chainQuerySet = getChainQuerySet();
		
		boolean first = false;
		for(OMQueryBasic q: chainQuerySet){
			//execute each basic query and inersect the results
			Set<OboeQueryResult> oneBasicQueryResult = q.execute(rawdb,resultWithRecord);
			if(!first){
				result.retainAll(oneBasicQueryResult);
			}else{
				result.addAll(oneBasicQueryResult);
				first = false;
			}
		}
		
		return result;
	}
	
	/**
	 * Execute one context query
	 * Perform each basic query, get their did and record id
	 * Intersect their results (because it's in the context)
	 * 
	 * @param materializedDB
	 * @param resultWithRecord
	 * @return
	 * @throws Exception 
	 */
	public Set<OboeQueryResult> execute(PostgresDB db, boolean resultWithRecord) 
		throws Exception
	{
		
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//TODO: need to use a set or a map for chain queries??? 
		Set<OMQueryBasic> chainQuerySet = getChainQuerySet();
		
		//The results need to be intersect-ed
		boolean first = true;
		for(OMQueryBasic q: chainQuerySet){
			Set<OboeQueryResult> oneBasicQueryResult = q.execute(db,resultWithRecord);
			if(!first){
				result.retainAll(oneBasicQueryResult);
			}else{
				result.addAll(oneBasicQueryResult);
				first = false;
			}
		}
		
		System.out.println(Debugger.getCallerPosition()+"Context chain result="+result);
		return result;
	}
	
	/**
	 * Get the set of basic OM queries in this chain
	 * 
	 * @return
	 */
	private Set<OMQueryBasic> getChainQuerySet(){
		Set<OMQueryBasic> chainQuerySet = new TreeSet<OMQueryBasic>();
		chainQuerySet.addAll(this.m_queryChain.keySet());
		if(m_queryChain.values()!=null){
			for(OMQueryBasic q: m_queryChain.values()){
				if(q!=null){
					chainQuerySet.add(q);
				}
			}
		}
		return chainQuerySet;
	}
	
}
