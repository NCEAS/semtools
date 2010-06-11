package org.ecoinformatics.oboe.query;

import java.sql.ResultSet;
import java.sql.SQLException;
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

import org.ecoinformatics.oboe.datastorage.*;
import org.ecoinformatics.oboe.model.*;
import org.ecoinformatics.oboe.util.Debugger;
import org.ecoinformatics.oboe.util.Pair;


// Query with one entity type: the measurement conditions can be AND and OR
// The measurement conditions form a logic form
// (1) all logical formulas can be converted to a disjunctive normal form (DNF) 
// http://en.wikipedia.org/wiki/Disjunctive_normal_form
// (2)  all logical formulas can be converted to a conjunctive normal form (CNF)
// http://en.wikipedia.org/wiki/Conjunctive_normal_form
// I will use disjunctive normal form
//
//E.g.
//Q1: Tree[Height > 5 Meter] : simple query obs: measurement AND
//for this query, OM QueryBasic: 
//m_queryObs has observation type's entity name = tree
//m_queryMeasDNF contains one element
//This element is a list with one query measurement "qm1" whose observation instance is "m_queryObs"
//
//Q2: Tree[avg(Height) > 7 Meter, Dbh < 4 Meter]: simple query: measurement OR
//for this query, OM QueryBasic: 
//m_queryObs has observation type's entity name = tree
//m_queryMeasDNF contains two elements
//The first element is a list with one query measurement "qm1" whose observation instance is "m_queryObs"
//Its avg(Height) need to be bigger than 7 Meter
//The second element is a list with one query measurement "qm2" whose condition is (Dbh < 4 Meter)
//
//Q3: Tree[Height > 5 Meter], Soil[Acidity >= 7 pH]
//This can be modeled as a list of two OMQuerryBasic elements.
//
public class OMQueryBasic implements Comparable<OMQueryBasic>{
	private String m_queryLabel;
	private String m_entityTypeNameCond;
	
	//Entry: Integer: DNF no; List<>: (AND) query measurements
	//ALL the entries: disjunctive normal form (OR condition)
	private Map<Integer,List<QueryMeasurement> > m_queryMeasDNF;
	
	
	public OMQueryBasic()
	{	
		m_queryMeasDNF = new HashMap<Integer, List<QueryMeasurement> >();
	}
	
	public String getQueryLabel() {
		return m_queryLabel;
	}
	public void setQueryLabel(String mQueryLabel) {
		m_queryLabel = mQueryLabel;
	}
	
	public String getEntityTypeNameCond() {
		return m_entityTypeNameCond;
	}
	public void setEntityTypeName(String mEntityTypeName) {
		m_entityTypeNameCond = mEntityTypeName;
	}
	
	public Map<Integer,List<QueryMeasurement>> getQueryMeasDNF() {
		return m_queryMeasDNF;
	}
	
	public void setQueryMeasDNF(Map<Integer,List<QueryMeasurement>> mQueryMeasDNF) {
		m_queryMeasDNF = mQueryMeasDNF;
	}
	
	//tested ok
	public String toString()
	{
		String str=m_queryLabel+": "+m_entityTypeNameCond;
		for(Entry<Integer, List<QueryMeasurement>> entry: m_queryMeasDNF.entrySet()){
			//str+=entry.getKey()+": ";
			str+=entry.getValue();
			//str+="]";
		}
		
		return str;
	}
	public List<QueryMeasurement> getMeasDNF(int dnfNo){
		return m_queryMeasDNF.get(dnfNo);
	}
	
	public void addMeasDNF(Integer dnfNo, QueryMeasurement queryMeas){
		List<QueryMeasurement> dnfMeasList = m_queryMeasDNF.get(dnfNo);
		if(dnfMeasList==null){
			dnfMeasList = new ArrayList<QueryMeasurement>();	
			m_queryMeasDNF.put(dnfNo,dnfMeasList);
		}
		dnfMeasList.add(queryMeas);
	}
	
	//one way is to form subqueries	
//	public String formSQL(String tbName)
//	{
//		String condition = "";
//		
//		for(Map.Entry<Integer, List<QueryMeasurement>> entry: m_queryMeasDNF.entrySet()){
//			List<QueryMeasurement> measAND = entry.getValue();
//			if(measAND!=null){
//				for(int i=0;i<measAND.size();i++){
//					QueryMeasurement qm = measAND.get(i);
//					String qmCond = qm.
//				}
//			}
//		}
//		
//	}
	
	/**
	 * For one whole SQL statement for a query.
	 * Return (did, record_id,eid,oid)
	 * 
	 * return () 
	 */
	public String formSQL(PostgresDB db, boolean resultWithRecord) throws Exception
	{
		String sql = "";
		
		//for different DNF, union their results
		boolean first = true;
		for(Map.Entry<Integer, List<QueryMeasurement>> entry: m_queryMeasDNF.entrySet()){
			
			//for the query measurement conditions ONE DNF, intersect all the results
			List<QueryMeasurement> measAND = entry.getValue();
			
			String tmpSql ="";
			if(db instanceof MDB){
				tmpSql = formSQLOneCNF((MDB)db,resultWithRecord, measAND);
			}else if(db instanceof RawDB){
				;//sql = formSQLOneCNF((RawDB)db,resultWithRecord, measAND);
			}else{
				System.out.println(Debugger.getCallerPosition()+"Not implemented yet.");
			}
			
			if(tmpSql.length()>0){
				if(!first){
					sql += " INTERSECT ("+tmpSql+")";
				}else{
					sql += "("+tmpSql+")";
					first= false;
				}
			}
		}
		
		return sql;
	}
	
	/**
	 * For one whole SQL statement for a query.
	 * Return (did, record_id,oid)
	 * 
	 * return () 
	 */
	public String formContextSQL(PostgresDB db, boolean resultWithRecord,String targetSql) throws Exception
	{
		String sql = "";
		
		//for different DNF, union their results
		boolean first = true;
		for(Map.Entry<Integer, List<QueryMeasurement>> entry: m_queryMeasDNF.entrySet()){
			
			//for the query measurement conditions ONE DNF, intersect all the results
			List<QueryMeasurement> measAND = entry.getValue();
			
			String tmpSql ="";
			if(db instanceof MDB){
				tmpSql = formSQLOneCNFContext((MDB)db,resultWithRecord, measAND, targetSql);
			}else if(db instanceof RawDB){
				;//sql = formSQLOneCNF((RawDB)db,resultWithRecord, measAND);
			}else{
				System.out.println(Debugger.getCallerPosition()+"Not implemented yet.");
			}
			
			if(tmpSql.length()>0){
				if(!first){
					sql += " INTERSECT ("+tmpSql+")";
				}else{
					sql += "("+tmpSql+")";
				}
			}
		}
		
		return sql;
	}
	
	
	
	
	/**
	 * Perform the basic query oover the original raw data base
	 * 
	 * @param rawdb
	 */
	public Set<OboeQueryResult> execute(PostgresDB db, boolean resultWithRecord) throws Exception
	{
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//for different DNF, union their results		
		for(Map.Entry<Integer, List<QueryMeasurement>> entry: m_queryMeasDNF.entrySet()){
			
			//for the query measurement conditions ONE DNF, intersect all the results
			List<QueryMeasurement> measAND = entry.getValue();
			
			Set<OboeQueryResult> oneDNFresult = null;
			if(db instanceof MDB){
				result = executeOneCNF((MDB)db,resultWithRecord, measAND);
			}else if(db instanceof RawDB){
				result = executeOneCNF((RawDB)db,resultWithRecord, measAND);
			}else{
				System.out.println(Debugger.getCallerPosition()+"Not implemented yet.");
			}
			
			if(oneDNFresult!=null){
				result.addAll(oneDNFresult);
			}
		}
		
		return result;
	}

	
	
	
	
	/**
	 * Classify the measurement conditions to those with aggregate functions and those without aggregate functions 
	 * @param measAND
	 * @param aggMeas
	 * @param nonAggMeas
	 */
	private void classifyMeasAND(List<QueryMeasurement> measAND,
			List<QueryMeasurement> aggMeas,
			List<QueryMeasurement> nonAggMeas)
	{
		for(QueryMeasurement qm: measAND){
			//This query measurement need to be aggregated
			if(qm.getAggregationFunc()!=null&qm.getAggregationFunc().length()>0){
				aggMeas.add(qm);
			}else{
				nonAggMeas.add(qm);
			}
		}
	}
	
	/**
	 * For the sql for one meas AND
	 * Return (did, record_id,eid, oid)
	 * 
	 * @param mdb
	 * @param resultWithRid
	 * @param measAND
	 * @return
	 * @throws Exception 
	 * @throws SQLException 
	 */
	private String formSQLOneCNF(MDB mdb, boolean resultWithRid, List<QueryMeasurement> measAND) 
		throws SQLException, Exception
	{
		System.out.println(Debugger.getCallerPosition()+"measAND="+measAND);
		String sql ="";
		
		//1. Get the measurements that need to be aggregated
		List<QueryMeasurement> aggMeas = new ArrayList<QueryMeasurement>();
		List<QueryMeasurement> nonAggMeas = new ArrayList<QueryMeasurement>();
		classifyMeasAND(measAND,aggMeas,nonAggMeas);
		
		String nonAggQuerySql = formSqlOneCNFNonAggregateMDB(mdb,m_entityTypeNameCond,nonAggMeas);
		
		if(aggMeas==null||aggMeas.size()==0){
			sql += (nonAggQuerySql);
		}else{
			boolean first = true;
			for(QueryMeasurement qm: aggMeas){
				String aggQuerySql = qm.formSQL(mdb, nonAggQuerySql);
				if(!first){
					sql+=" INTERSECT ("+aggQuerySql+")";
				}else{
					sql+=" ("+aggQuerySql+")";
					first = false;
				}
			}
		}
		
		//System.out.println(Debugger.getCallerPosition()+Debugger.getWhoCalledMe()+"=====sql="+sql);
		
		return sql;
	}
	
	/**
	 * form one context sql
	 * (did, record_id, eid, oid)
	 * 
	 * @param mdb
	 * @param resultWithRid
	 * @param measAND
	 * @param targetSql
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	private String formSQLOneCNFContext(MDB mdb, boolean resultWithRid, 
			List<QueryMeasurement> measAND, String targetSql) 
		throws SQLException, Exception
	{
		System.out.println(Debugger.getCallerPosition()+"measAND="+measAND);
		String contextSql = formSQLOneCNF (mdb,resultWithRid,measAND);
		
		String sql = "SELECT tmp1.did, tmp1.record_id, tmp1.eid, tmp1.oid \n";
		sql+=" FROM ("+targetSql+") as tmp1, ";
		sql+=" ("+contextSql+") as tmp2, ";
		sql+= mdb.getContextInstanceTable() +" as oi \n";
		
		sql+="WHERE tmp1.oid = oi.oid AND oi.context_oid=tmp2.oid AND tmp1.did=tmp2.did AND tmp1.record_id=tmp2.record_id";
		
		System.out.println(Debugger.getCallerPosition()+"sql=\n"+sql);
		return sql;
	}
	
	
	/**
	 * Execute the query one DNF, i.e., all its conditions should be intersected 
	 * 
	 * @param mdb
	 * @param measAND
	 * @return
	 * @throws Exception 
	 */
	private Set<OboeQueryResult> executeOneCNF(MDB mdb, boolean resultWithRid, List<QueryMeasurement> measAND)
		throws Exception
	{
		System.out.println(Debugger.getCallerPosition()+"measAND="+measAND);
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//1. Get the measurements that need to be aggregated
		List<QueryMeasurement> aggMeas = new ArrayList<QueryMeasurement>();
		List<QueryMeasurement> nonAggMeas = new ArrayList<QueryMeasurement>();
		classifyMeasAND(measAND,aggMeas,nonAggMeas);
		
		//Execute each aggregate query measurements and "AND" their results
		Set<OboeQueryResult> nonAggQueryResult =  
			executeOneCNFNonAggregateMDB(mdb,m_entityTypeNameCond,nonAggMeas);
		result.addAll(nonAggQueryResult);
		String nonAggQuerySql = formSqlOneCNFNonAggregateMDB(mdb,m_entityTypeNameCond,nonAggMeas);
		
		System.out.println(Debugger.getCallerPosition()+"result size="+result.size());
		
		if(result.size()>0){
			//boolean first= true;
			for(QueryMeasurement qm: aggMeas){
				//if(!first&&result.size()==0){
					//this is not the first query, but no result, so, we don't need to execute other queries 
				//	break;
				//}
				
				//FIXME: will it has efficiency problem? 
				//The embedded CNF and query is performed |aggMeas| times. 
				//CHECK THIS with big dataset
				Set<OboeQueryResult> oneAggQueryResult = 
					qm.executeAggQueryMDB(mdb, nonAggQuerySql); 
				result.retainAll(oneAggQueryResult);
			}
		}
		
		System.out.println(Debugger.getCallerPosition()+"oneCNF result size="+result.size());
		return result;
	}
	
	/**
	 * the SQL for non-aggregate conditions
	 * (did,record_id,oid,eid,mvalue,characteristic)
	 * 
	 * @param mdb
	 * @param entityNameCond
	 * @param nonAggMeas
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public static String formSqlOneCNFNonAggregateMDB(MDB mdb, String entityNameCond,
			List<QueryMeasurement> nonAggMeas) throws SQLException, Exception
	{
		//For the SQL for all the non aggregate conditions
		String sqlNonAggCond = "";
		for(int i=0;i<nonAggMeas.size();i++){
			QueryMeasurement qm = nonAggMeas.get(i); //get 
			//(did,record_id,oid,eid,mvalue,characteristic)
			String oneNonAggCondSql = qm.formSQLNonAggCondOverMDB(mdb,entityNameCond);
			if(i==0){
				sqlNonAggCond = "("+oneNonAggCondSql+")";
			}else{
				sqlNonAggCond = "INTERSECT \n("+oneNonAggCondSql+")";
			}
		}
		
		System.out.println(Debugger.getCallerPosition()+"sqlNonAggCond:\n"+sqlNonAggCond+"\n");
		
		return sqlNonAggCond;
	}
	/**
	 * Execute one CNF non-aggregate query condition
	 *  
	 * @param mdb
	 * @param entityNameCond
	 * @param nonAggMeas
	 * @return
	 * @throws Exception 
	 * @throws SQLException 
	 */
	private Set<OboeQueryResult> executeOneCNFNonAggregateMDB(MDB mdb, String entityNameCond,
			List<QueryMeasurement> nonAggMeas) throws SQLException, Exception 
	{
		
		//get the SQL for non-aggregate conditions
		String sqlNonAggCond = formSqlOneCNFNonAggregateMDB(mdb,entityNameCond,nonAggMeas);
		
		String sql = "SELECT DISTINCT did, record_id FROM ("+sqlNonAggCond+") AS tmp";
		Set<OboeQueryResult> resultSet = mdb.executeSQL(sql);
		
		System.out.println(Debugger.getCallerPosition()+"sql:"+sql);
		System.out.println(Debugger.getCallerPosition()+"resultSet:"+resultSet.size());
		return resultSet;
	
	}
	
	
	/**
	 * For one DNF, using AND connect the conditions
	 * 
	 * @param rawdb
	 * @param measAND
	 * @return
	 * @throws Exception
	 */
	private Set<OboeQueryResult> executeOneCNF(RawDB rawdb, boolean resultWithRecord, List<QueryMeasurement> measAND)
		throws Exception
	{	
		
		//1. Get the measurements that need to be aggregated
		List<QueryMeasurement> aggMeas = new ArrayList<QueryMeasurement>();
		List<QueryMeasurement> nonAggMeas = new ArrayList<QueryMeasurement>();
		classifyMeasAND(measAND,aggMeas,nonAggMeas);
		
		//2. Get this entity's key characteristics <dataset id: list of key attributes> 
		Map<Long, List<String>>  annotId2KeyAttrList = rawdb.calKeyAttr(m_entityTypeNameCond);

		//3. The results need to be intersect-ed result = result(each qm in aggMeas) AND result(nonAggMeas)
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//Map<Long, String> tbid2nonAggWhereclasue = new HashMap<Long, String>();
		Map<Long, List<Pair<QueryMeasurement,String>> > tb2Attribute = new TreeMap<Long, List<Pair<QueryMeasurement,String> >>(); 
		Map<Long, String> tbid2nonAggWhereclasue = formNonAggCNFWhereSQL(rawdb,nonAggMeas,tb2Attribute);
		
		//3.1 Execute each aggregate query measurements and "AND" their results
		boolean first = true;
		for(QueryMeasurement qm: aggMeas){
			if(!first&&result.size()==0){
				//this is not the first query, but no result, so, we don't need to execute other queries 
				break;
			}
			Set<OboeQueryResult> oneAggQueryResult = //qm.executeAggQueryRawDB(rawdb,resultWithRecord,annotId2KeyAttrList,nonAggMeas);
				qm.executeAggQueryRawDB(rawdb,resultWithRecord,annotId2KeyAttrList,tbid2nonAggWhereclasue);
			if(!first){
				result.retainAll(oneAggQueryResult);
			}else{
				result.addAll(oneAggQueryResult);
				first = false;
			}
		}
		
		//3.2. Execute the non-aggregate query measurement as a whole and "AND" its results with the previous step.
		//when there is aggregation condition, this non-aggregate condition should be incorporated to the aggregate function
		if(aggMeas==null||aggMeas.size()==0){
			//when there is no aggregation function, we perform the non aggregation CNF
			Set<OboeQueryResult> nonAggQueryResult = //executeNonAggregateCNF(rawdb,resultWithRecord,nonAggMeas);
				executeOneCNFNonAggregateRawDB(rawdb,resultWithRecord,tb2Attribute);
			if(first){
				result.addAll(nonAggQueryResult);
			}else{
				result.retainAll(nonAggQueryResult);
			}
		}
		
		System.out.println(Debugger.getCallerPosition()+"oneCNF result size ="+result.size());
		return result;
	}
	
	
	/**
	 * Perform a query with the "AND" query measurements without aggregation function
	 * 
	 * @param rawdb
	 * @param nonAggMeasAND
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	private Set<OboeQueryResult> executeOneCNFNonAggregateRawDB(
			RawDB rawdb, boolean resultWithRecord, 
			//List<QueryMeasurement> nonAggMeasAND
			Map<Long, List<Pair<QueryMeasurement,String>> > tb2Attribute
			) 
		throws SQLException, Exception
	{
		Set<OboeQueryResult> nonAggQueryResult = new TreeSet<OboeQueryResult>();
		
//		//1. find table id and related attribute names
//		//Set<String> characteristics = new HashSet<String>();
//		Map<String, QueryMeasurement> cha2qm= new HashMap<String, QueryMeasurement>();
//		for(QueryMeasurement qm: nonAggMeasAND){
//			String chaCond = qm.getCharacteristicCond();
//			if(chaCond!=null&&chaCond.length()>0){
//				//characteristics.add(chaCond);
//				cha2qm.put(chaCond, qm);
//			}
//		}
//		Map<Long, List<Pair<QueryMeasurement,String>> > tb2Attribute = rawdb.retrieveTbAttribute(cha2qm);
		
		//2. form SQL and execute query, the results should be unioned since they come from data table		
		for(Map.Entry<Long, List<Pair<QueryMeasurement,String> >> entry: tb2Attribute.entrySet()){
			Long tbId = entry.getKey();
			//pair is <queryMeasurement,attribute name>
			List<Pair<QueryMeasurement,String>> chaAttributeNamePairList= entry.getValue();
			
			
			String sql = "SELECT DISTINCT "+tbId +" AS did";
			if(resultWithRecord){
				sql +=", record_id ";
			}
			sql += " FROM " + rawdb.TB_PREFIX+tbId;
			String whereSql = formNonAggCNFWhereSQLsub(chaAttributeNamePairList);
			sql +=whereSql;
			
			sql += ";";
			
			//3. execute sql
			System.out.println(Debugger.getCallerPosition()+"sql= "+sql);
			Set<OboeQueryResult> oneTbResult = rawdb.dataQuery(sql);
			if(oneTbResult!=null&&oneTbResult.size()>0){
				nonAggQueryResult.addAll(oneTbResult);
			}
		}
		
		return nonAggQueryResult;
	}
	
	
	/**
	 * From the non-aggregation measurements (should be AND), form the sql where clause
	 * 
	 * @param rawdb
	 * @param nonAggMeasAND
	 * @return
	 * @throws SQLException
	 */
	private Map<Long, String> formNonAggCNFWhereSQL(RawDB rawdb, List<QueryMeasurement> nonAggMeasAND,
			Map<Long, List<Pair<QueryMeasurement,String>> > tb2Attribute) 
		throws SQLException
	{
		Map<Long, String> tbid2nonAggWhereclasue = new HashMap<Long, String>();
		
		Map<String, QueryMeasurement> cha2qm= new HashMap<String, QueryMeasurement>();
		for(QueryMeasurement qm: nonAggMeasAND){
			String chaCond = qm.getCharacteristicCond();
			if(chaCond!=null&&chaCond.length()>0){
				cha2qm.put(chaCond, qm);
			}
		}
		Map<Long, List<Pair<QueryMeasurement,String>> > tmptb2Attribute = rawdb.retrieveTbAttribute(cha2qm);
		tb2Attribute.putAll(tmptb2Attribute);
		
		//2. form SQL and execute query, the results should be unioned since they come from data table		
		for(Map.Entry<Long, List<Pair<QueryMeasurement,String> >> entry: tb2Attribute.entrySet()){
			Long tbId = entry.getKey();
			//pair is <queryMeasurement,attribute name>
			List<Pair<QueryMeasurement,String>> chaAttributeNamePairList= entry.getValue();
			String whereSql = formNonAggCNFWhereSQLsub(chaAttributeNamePairList);
			tbid2nonAggWhereclasue.put(tbId, whereSql);
		}
		
		return tbid2nonAggWhereclasue;
	}
	
	
	
	/**
	 *TODO: HP strange to put this as a static function
	 * @param chaAttributeNamePairList, pair is <characteristic,attribute name>
	 * @return
	 */
	private static String formNonAggCNFWhereSQLsub(//Map<String, QueryMeasurement> cha2qm,
			List<Pair<QueryMeasurement,String>> chaAttributeNamePairList)
	{
		String sql="";
		if(chaAttributeNamePairList!=null&&chaAttributeNamePairList.size()>0){
	
			sql +=" WHERE (";
			for(int i=0;i<chaAttributeNamePairList.size();i++){
				Pair<QueryMeasurement,String> pair = chaAttributeNamePairList.get(i);
				QueryMeasurement qm = pair.getFirst();//cha2qm.get(pair.getFirst());
				String valueCond = qm.getValueCond();
				if(i>0){
					sql +=" AND ";
				}
				sql += pair.getSecond() + valueCond; 
			}
			sql +=")";
		}
		return sql;
	}
//	/**
//	 * Execute this query against the materialized database 
//	 * The other way is to form simple query and work on the results from the program
//	 * 
//	 * @deprecated
//	 * @param mdb
//	 * @return
//	 * @throws Exception 
//	 */
//	public Set<OboeQueryResult> execute(MDB mdb, boolean resultWithRid) throws Exception
//	{
//		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
//		
//		//for different DNF, union their results		
//		for(Map.Entry<Integer, List<QueryMeasurement>> entry: m_queryMeasDNF.entrySet()){
//			
//			
//			//for the query measurement conditions ONE DNF, intersect all the results
//			List<QueryMeasurement> measAND = entry.getValue();
//			
//			Set<OboeQueryResult> oneDNFresult = executeOneCNF(mdb, resultWithRid, measAND);
//			result.addAll(oneDNFresult);
//		}
//		
//		System.out.println(Debugger.getCallerPosition()+"Basic query result="+result);
//		return result;
//	}

	public int compareTo(OMQueryBasic o) {
		
		int cmp = m_queryLabel.compareTo(o.getQueryLabel());
		//if(cmp!=0) return cmp;
		return cmp;
	}
	

	
}
