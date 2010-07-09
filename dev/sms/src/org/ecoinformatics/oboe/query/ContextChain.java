package org.ecoinformatics.oboe.query;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;

import org.ecoinformatics.oboe.datastorage.*;
import org.ecoinformatics.oboe.util.Debugger;

public class ContextChain {
	Map<OMQueryBasic, List<OMQueryBasic> > m_queryChain; 
	
	public ContextChain()
	{
		m_queryChain = new TreeMap<OMQueryBasic, List<OMQueryBasic>>();
		//m_queryChain = new TreeMap<OMQueryBasic, OMQueryBasic>();
	}

	//public Map<OMQueryBasic, OMQueryBasic> getQueryChain()
	public Map<OMQueryBasic, List<OMQueryBasic>> getQueryChain()
	{
		return m_queryChain;
	}

	//public void setQueryChain(Map<OMQueryBasic, OMQueryBasic> mQueryChain)
	public void setQueryChain(Map<OMQueryBasic, List<OMQueryBasic>> mQueryChain) {	
		m_queryChain = mQueryChain;
	}
	
	public String toString()
	{
		String str="";
		//for(Map.Entry<OMQueryBasic,OMQueryBasic> entry: m_queryChain.entrySet())
		for(Map.Entry<OMQueryBasic,List<OMQueryBasic>> entry: m_queryChain.entrySet())
		{
			str += "("+entry.getKey().getQueryLabel();
			if(entry.getValue()!=null){
				//str +="->"+entry.getValue().getQueryLabel();
				str +="->";
				for(int i=0;i<entry.getValue().size();i++){
					OMQueryBasic qm = entry.getValue().get(i);
					if(i>0){
						str += ",";
					}
					str+=qm.getQueryLabel();
				}
			}
			str += ") ";
		}
		return str;
	}
	
	public void addGroup(OMQueryBasic oneQueryChain){
		m_queryChain.put(oneQueryChain,null);
	}
	
	public void addGroup(OMQueryBasic keyQuery, OMQueryBasic valueQuery){
		List<OMQueryBasic> tmplist = m_queryChain.get(keyQuery);
		if(tmplist==null){
			tmplist = new ArrayList<OMQueryBasic>();
			m_queryChain.put(keyQuery,tmplist);
		}
		tmplist.add(valueQuery);
		//m_queryChain.put(keyQuery,valueQuery);
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
	
//	/**
//	 * @deprecated
//	 */
//	public Set<OboeQueryResult> execute1(PostgresDB db, boolean resultWithRecord) 
//		throws Exception
//	{
//		Set<OboeQueryResult> result = null;
//		
//		if(db instanceof MDB){
//			result = execute((MDB)db,resultWithRecord);
//		}else if(db instanceof RawDB){
//			result = executeOverRawDB((RawDB)db,resultWithRecord);
//		}else{
//			System.out.println(Debugger.getCallerPosition()+"Not implemented yet.");
//		}
//		return result;
//	}
	
//	/**
//	 * Execute one context query over original database
//	 * 
//	 * 1. Get the key measurement (for group by)
//	 * 2. Get the characteristics and their related attributes
//	 *  
//	 * @deprecated
//	 * @param db
//	 * @param resultWithRecord
//	 * @return
//	 * @throws Exception
//	 * 
//	 */
//	private Set<OboeQueryResult> executeOverRawDB(RawDB rawdb, boolean resultWithRecord) 
//		throws Exception
//	{
//		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
//		
//		//TODO: need to use a set or a map for chain queries??? 
//		Set<OMQueryBasic> chainQuerySet = getChainQuerySet();
//		
//		boolean first = false;
//		for(OMQueryBasic q: chainQuerySet){
//			//execute each basic query and inersect the results
//			Set<OboeQueryResult> oneBasicQueryResult = q.execute(rawdb,resultWithRecord);
//			if(!first){
//				result.retainAll(oneBasicQueryResult);
//			}else{
//				result.addAll(oneBasicQueryResult);
//				first = false;
//			}
//		}
//		
//		return result;
//	}
	
	
	public String formHolisticNonAggSQL(MDB db,boolean resultWithRecord) throws Exception
	{
		String sql = "";
		for(Map.Entry<OMQueryBasic, List<OMQueryBasic> > entry: m_queryChain.entrySet()){
			OMQueryBasic targetQuery = entry.getKey();
			List<OMQueryBasic> context = entry.getValue(); 
			System.out.println(Debugger.getCallerPosition()+"targetQuery="+targetQuery+"context="+context);
			
			//deal with mdb 12,13,14,15
			String sql1 = formSQL(targetQuery,context,db,resultWithRecord);
			if(sql.length()==0)
				sql += "("+ sql1 + ")";
			else
				sql += " INTERSECT ("+sql1+")";
		}
		return sql;
				
	}
	/**
	 * form the sql to perfom a context query
	 * return (did, record_id, eid, oid)
	 * 
	 * @throws Exception 
	 */
	private String formSQL(OMQueryBasic targetQuery, List<OMQueryBasic> targetContext,PostgresDB db, 
			boolean resultWithRecord) throws Exception
	{
		String sql ="";
	
		//form target query sql
		//return (did, record_id, oid)
		NonAggSubQueryReturn targetQueryReturnStru = new NonAggSubQueryReturn();
		targetQueryReturnStru.m_include_did = true;
		targetQueryReturnStru.m_include_record_id = resultWithRecord;
		targetQueryReturnStru.m_include_oid = true;
		
		String targetSql = targetQuery.formSQL(db, targetQueryReturnStru);
		
		
		if(targetContext!=null){
			//return (did, record_id, oid)
			for(int i=0; i<targetContext.size();i++){
				OMQueryBasic basic = targetContext.get(i);
				String tmpSql =basic.formContextSQL(db, resultWithRecord,targetSql);
				if(i==0){
					sql = "("+tmpSql+")";
				}else{
					
					sql +=" INTERSECT ("+tmpSql+")";
				}
			}
		}else{
			sql = targetSql;
		}
		
		//System.out.println(Debugger.getCallerPosition()+"======sql==="+sql+"\n");
		
		String sqlReturn = "SELECT DISTINCT did" ;
		if(resultWithRecord){
			sqlReturn +=", record_id";
		}
				
		sqlReturn +=" FROM "+sql+" as cctmp";
			
		System.out.println(Debugger.getCallerPosition()+"======sqlReturn====\n"+sqlReturn+"\n");
		return sqlReturn;
	}
	
	/**
	 * Execute one context query
	 * Perform each basic query, get their did and record id
	 * Intersect their results (because it's in the context)
	 * 
	 * @param materializedDB
	 * @param resultWithRecord
	 * @return
	 * @deprecated
	 * @throws Exception 
	 */
	public Set<OboeQueryResult> execute1(PostgresDB db, boolean resultWithRecord) 
		throws Exception
	{
		
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//The results need to be intersect-ed
		boolean first = true;
		
		//mdb2-mdb12, mdb3-mdb13, mdb4-mdb14, mdb5-mdb15
		boolean useOneSQL = false;
		if(db instanceof MDB){
			int queryStrategy = ((MDB)db).getQueryStrategy();
			if(queryStrategy>10){
				useOneSQL = true;
			}
		}
			
		//For each basic query in the context chain
		for(Map.Entry<OMQueryBasic, List<OMQueryBasic> > entry: m_queryChain.entrySet()){
			OMQueryBasic targetQuery = entry.getKey();
			List<OMQueryBasic> context = entry.getValue(); 
			System.out.println(Debugger.getCallerPosition()+"targetQuery="+targetQuery+"context="+context);
			
			//deal with mdb 12,13,14,15
			if(context!=null&&useOneSQL){
				//form one sql and run the query
				//form the sql for the query, and execute the query
				String sql = formSQL(targetQuery,context,db,resultWithRecord);
				Set<OboeQueryResult> oneBasicQueryResult = db.executeSQL(sql,resultWithRecord);
				System.out.println(Debugger.getCallerPosition()+"oneBasicQueryResult="+oneBasicQueryResult);
				
				//add results
				if(!first){
					result.retainAll(oneBasicQueryResult);
				}else{
					result.addAll(oneBasicQueryResult);
					first = false;
				}
			}else{
				//run each query and intersect their results
				
				//perform the basic target query
				Set<OboeQueryResult> oneBasicQueryResult = targetQuery.execute(db,resultWithRecord);
				if(!first){
					result.retainAll(oneBasicQueryResult);
				}else{
					result.addAll(oneBasicQueryResult);
					first = false;
				}
				
				//System.out.println(Debugger.getCallerPosition()+"++++[1]result="+result);
				//perform the target query's context
				if(context!=null&&oneBasicQueryResult.size()>0){
					for(OMQueryBasic basic: context){
						Set<OboeQueryResult> contextQueryResult = basic.execute(db, resultWithRecord);
						
						//intersect the result from the basic query and its context query
						result.retainAll(contextQueryResult);
						//System.out.println(Debugger.getCallerPosition()+"++++[2]result="+result);
					}
				}
			}
		}
		
		System.out.println(Debugger.getCallerPosition()+"Context chain result size="+result.size()+"\n******\n");
		return result;
	}
	
	/**
	 * Execute a context chain
	 * @param db
	 * @param resultWithRecord
	 * @return
	 * @throws Exception
	 */
	public Set<OboeQueryResult> execute(PostgresDB db, boolean resultWithRecord) 
		throws Exception
	{
		
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//The results need to be intersect-ed
		boolean first = true;
		
		//This keeps all the basic queries that have already been executed
		//To avoid the execution of two times of q1 in context chains like q1-->q2, q1-->q3
		Set<OMQueryBasic> executedQuery = new TreeSet<OMQueryBasic>();
		
		//For each basic query in the context chain
		for(Map.Entry<OMQueryBasic, List<OMQueryBasic> > entry: m_queryChain.entrySet()){
			OMQueryBasic targetQuery = entry.getKey();
			List<OMQueryBasic> context = entry.getValue(); 
			System.out.println(Debugger.getCallerPosition()+"targetQuery="+targetQuery+"context="+context);
			
			//perform the basic target query
			Set<OboeQueryResult> oneBasicQueryResult = targetQuery.execute(db,resultWithRecord);
			if(!first){
				result.retainAll(oneBasicQueryResult);
			}else{
				result.addAll(oneBasicQueryResult);
				first = false;
			}
			
			//System.out.println(Debugger.getCallerPosition()+"++++[1]result="+result);
			//perform the target query's context
			if(context!=null&&oneBasicQueryResult.size()>0){
				for(OMQueryBasic basic: context){
					Set<OboeQueryResult> contextQueryResult = basic.execute(db, resultWithRecord);
					
					//intersect the result from the basic query and its context query
					result.retainAll(contextQueryResult);
					//System.out.println(Debugger.getCallerPosition()+"++++[2]result="+result);
				}
			}
		}
		
		System.out.println(Debugger.getCallerPosition()+"Context chain result size="+result.size()+"\n******\n");
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
			for(List<OMQueryBasic> q: m_queryChain.values()){
				if(q!=null){
					chainQuerySet.addAll(q);
				}
			}
		}
		return chainQuerySet;
	}
	
	
	/**
	 * if any basic query structure contains an aggregation condition, return true
	 * else, return false
	 * 
	 * @return
	 */
	public boolean containsAggregate()
	{
		for(Map.Entry<OMQueryBasic, List<OMQueryBasic>> entry: m_queryChain.entrySet()){
			OMQueryBasic key = entry.getKey();
			List<OMQueryBasic> context = entry.getValue();
			
			if(key.containsAggregate()){
				return true;
			}
			
			if(context!=null){
				for(int i=0;i<context.size();i++){
					if(context.get(i).containsAggregate()){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * For the where clause for non-aggregation CNF for a table (with given tbid)
	 * return empty string if this table does not have the required characteristics etc.
	 * 
	 * @param rawdb
	 * @param tbid
	 * @return
	 * @throws SQLException
	 */
	public String formNonAggCNFWhereSQL(RawDB rawdb, long tbid) 
		throws SQLException
	{
		String sql="";
		String logic = "AND";
		
		Set<OMQueryBasic> checkedOMQueryBasic = new TreeSet<OMQueryBasic>();
		boolean first = true;
		
		//for each condition, get the where clause
		for(Map.Entry<OMQueryBasic, List<OMQueryBasic>> entry: m_queryChain.entrySet()){
			OMQueryBasic keyquery = entry.getKey();
			List<OMQueryBasic> context = entry.getValue();
			
			if(!checkedOMQueryBasic.contains(keyquery)){
				String keyqueryWhereClause = keyquery.getWhereClause(rawdb,tbid);
				
				if(keyqueryWhereClause!=null&&keyqueryWhereClause.trim().length()>0){
					if(first){
						sql+=keyqueryWhereClause;
						first = false;
					}else{
						sql+=" "+logic+" " + keyqueryWhereClause;
					}
				}else{
					break;
				}
			}
			
			if(context!=null){
				for(int i=0;i<context.size();i++){
					OMQueryBasic contextQuery = context.get(i);
					if(!checkedOMQueryBasic.contains(contextQuery)){
						String contextqueryWhereClause = contextQuery.getWhereClause(rawdb,tbid);
						sql+=" "+logic+" " + contextqueryWhereClause;
					}
				}
			}
		}
		
		if(sql.length()>0){
			sql = "("+sql+")";
		}
		return sql;		
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	public Map<Long, String> formNonAggCNFWhereSQL(RawDB rawdb) 
		throws SQLException
	{
		List oneChainWhereClause = new ArrayList();
		Set<Long> commonTid = new TreeSet<Long>();
		Set<OMQueryBasic> checkedOMQueryBasic = new TreeSet<OMQueryBasic>();
		boolean first = true;
		
		//for each condition, get the where clause
		for(Map.Entry<OMQueryBasic, List<OMQueryBasic>> entry: m_queryChain.entrySet()){
			OMQueryBasic keyquery = entry.getKey();
			List<OMQueryBasic> context = entry.getValue();
			
			if(!checkedOMQueryBasic.contains(keyquery)){
				Map<Long, String> keyqueryWhereClause = keyquery.getWhereClause(rawdb);
				
				oneChainWhereClause.add(keyqueryWhereClause);
				if(first){
					commonTid.addAll(keyqueryWhereClause.keySet());
					first = false;
				}else{
					commonTid.retainAll(keyqueryWhereClause.keySet());
				}
			}
			
			if(context!=null){
				for(int i=0;i<context.size();i++){
					OMQueryBasic contextQuery = context.get(i);
					if(!checkedOMQueryBasic.contains(contextQuery)){
						Map<Long, String> contextqueryWhereClause = contextQuery.getWhereClause(rawdb);
						oneChainWhereClause.add(contextqueryWhereClause);
						commonTid.retainAll(contextqueryWhereClause.keySet());
					}
				}
			}
		}
		
		//merge the where clause for different tables
		Map<Long, String> tbid2nonAggWhereClause = OMQuery.MergeRDBWhere(oneChainWhereClause, commonTid, "AND");
		
		return tbid2nonAggWhereClause;
	}
}
