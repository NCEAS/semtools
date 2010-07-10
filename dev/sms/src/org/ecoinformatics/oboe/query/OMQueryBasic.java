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
	public String formSQL(PostgresDB db, NonAggSubQueryReturn returnStru) throws Exception
	{
		String sql = "";
		
		//for different DNF, union their results
		boolean first = true;
		for(Map.Entry<Integer, List<QueryMeasurement>> entry: m_queryMeasDNF.entrySet()){
			
			//for the query measurement conditions ONE DNF, intersect all the results
			List<QueryMeasurement> measAND = entry.getValue();
			
			String tmpSql ="";
			if(db instanceof MDB){		
				tmpSql = formSQLOneCNF((MDB)db, measAND, returnStru);
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
	private String formSQLOneCNF(MDB mdb, //boolean resultWithRid, 
			List<QueryMeasurement> measAND,
			NonAggSubQueryReturn requiredReturnStru) 
		throws SQLException, Exception
	{
		System.out.println(Debugger.getCallerPosition()+"measAND="+measAND);
		String sql ="";
		
		//1. Get the measurements that need to be aggregated
		List<QueryMeasurement> aggMeas = new ArrayList<QueryMeasurement>();
		List<QueryMeasurement> nonAggMeas = new ArrayList<QueryMeasurement>();
		classifyMeasAND(measAND,aggMeas,nonAggMeas);
		
		NonAggSubQueryReturn returnStru = new NonAggSubQueryReturn();
		returnStru.m_include_did = true;
		returnStru.m_include_record_id = requiredReturnStru.m_include_record_id;
		if(requiredReturnStru.m_include_oid||(aggMeas!=null&&aggMeas.size()>0))
			returnStru.m_include_oid = true;
		returnStru.m_include_characteristic = true;
		
		String nonAggQuerySql = formSqlOneCNFNonAggregateMDB(mdb,m_entityTypeNameCond,nonAggMeas,returnStru);
		
		if(aggMeas==null||aggMeas.size()==0){
			sql += (nonAggQuerySql);
		}else{
			boolean first = true;
			for(QueryMeasurement qm: aggMeas){
				//this needs (did, record_id (depends on), oid) from the non-agg query sql
				NonAggSubQueryReturn aggReturnStru = new NonAggSubQueryReturn();
				aggReturnStru.m_include_did = true;
				aggReturnStru.m_include_record_id = requiredReturnStru.m_include_record_id;
				aggReturnStru.m_include_oid = requiredReturnStru.m_include_oid;
				
				String aggQuerySql = qm.formSQL(mdb, nonAggQuerySql,aggReturnStru);
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
		
		NonAggSubQueryReturn returnStru = new NonAggSubQueryReturn();
		returnStru.m_include_record_id = resultWithRid;
		returnStru.m_include_oid = true;
		
		String contextSql = formSQLOneCNF (mdb,measAND,returnStru);
		
		String sql = "SELECT targetTb.did";
		if(resultWithRid){
				sql+=", targetTb.record_id";
		}
		sql+=", targetTb.oid \n";
		sql+=" FROM ("+targetSql+") as targetTb, \n";
		sql+=" ("+contextSql+") as contextTb, \n";
		sql+= mdb.getContextInstanceTable() +" as ci \n";
		
		sql+="WHERE targetTb.oid = ci.oid AND ci.context_oid=contextTb.oid "; 
		//		 + "ci.context_oid=contextTb.oid AND " +
		//		"targetTb.did=contextTb.did "; 
		//if(resultWithRid){
		//	sql += "AND targetTb.record_id=contextTb.record_id";
		//}
		
		//System.out.println(Debugger.getCallerPosition()+"sql=\n"+sql+"\n");
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
		//System.out.println(Debugger.getCallerPosition()+"measAND="+measAND);
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//1. Get the measurements that need to be aggregated
		List<QueryMeasurement> aggMeas = new ArrayList<QueryMeasurement>();
		List<QueryMeasurement> nonAggMeas = new ArrayList<QueryMeasurement>();
		classifyMeasAND(measAND,aggMeas,nonAggMeas);
		
		//2. perform the non-aggregate query
		Set<OboeQueryResult> nonAggQueryResult = null;
		if(nonAggMeas.size()>0){
			nonAggQueryResult = executeOneCNFNonAggregateMDB(mdb,m_entityTypeNameCond,nonAggMeas,resultWithRid);
			result.addAll(nonAggQueryResult);
		}
		
		NonAggSubQueryReturn returnStru = new NonAggSubQueryReturn();
		returnStru.m_include_record_id = resultWithRid;
		returnStru.m_include_oid = true;
		String nonAggQuerySql = "";
		if(nonAggMeas.size()>0){
			nonAggQuerySql = formSqlOneCNFNonAggregateMDB(mdb,m_entityTypeNameCond,nonAggMeas,returnStru);
		}
		
		System.out.println(Debugger.getCallerPosition()+"result size="+result.size());
		
		//2. Execute each aggregate query measurements and "AND" their results
		if((nonAggMeas.size()==0)||(nonAggMeas.size()>0&&result.size()>0)){
			for(QueryMeasurement qm: aggMeas){
				//FIXME: will it has efficiency problem? 
				//The embedded CNF and query is performed |aggMeas| times. 
				//CHECK THIS with big dataset
				//GROUP by is done on (did,oid), 
				//no matter the resultWithRid is true or false, the group by need to be performed on (did,oid)
				//So, when there is aggregate condition, the non-aggregate results should return oid 
				Set<OboeQueryResult> oneAggQueryResult = 
					qm.executeAggQueryMDB(mdb, nonAggQuerySql,nonAggQueryResult,resultWithRid); 
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
			List<QueryMeasurement> nonAggMeas, NonAggSubQueryReturn returnStru) throws SQLException, Exception
	{
		//For the SQL for all the non aggregate conditions
		String sqlNonAggCond = "";
		for(int i=0;i<nonAggMeas.size();i++){
			QueryMeasurement qm = nonAggMeas.get(i); //get 
			//(did,record_id,oid,eid,mvalue,characteristic,standard)
			String oneNonAggCondSql = qm.formSQLNonAggCondOverMDBView(mdb,entityNameCond, returnStru);
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
			List<QueryMeasurement> nonAggMeas,boolean resultwithRecordId) throws SQLException, Exception 
	{
		
		NonAggSubQueryReturn returnStru = new NonAggSubQueryReturn();
		returnStru.m_include_did = true;
		returnStru.m_include_record_id = resultwithRecordId;
		returnStru.m_include_oid = false;
		returnStru.m_include_mvalue = false;
		returnStru.m_include_characteristic = false;
		returnStru.m_include_standard = false;
		
		//get the SQL for non-aggregate conditions
		String sqlNonAggCond = formSqlOneCNFNonAggregateMDB(mdb,entityNameCond,nonAggMeas,returnStru);
				
		String sql = "SELECT DISTINCT did"; 
		if(resultwithRecordId){
			sql +=", record_id ";
		}
		
		if(sqlNonAggCond!=null&&sqlNonAggCond.trim().length()>0)
			sql +=" FROM ("+sqlNonAggCond+") AS tmp";
		else{
			sql += " FROM " + mdb.m_nonAggMeasView;
		}
		
		System.out.println(Debugger.getCallerPosition()+"sql:"+sql);
		
		Set<OboeQueryResult> resultSet = mdb.executeSQL(sql,resultwithRecordId);
		
		
		System.out.println(Debugger.getCallerPosition()+"resultSet:"+resultSet.size());
		return resultSet;
	
	}
	
	/**
	 * Form the where clause for one table with given id tbid
	 * The DNFs in this basic query is connected with logic OR
	 * 
	 * Return empty string if there is no required characteristics
	 * 
	 * @param rawdb
	 * @param tbid
	 * @return
	 * @throws SQLException
	 */
	public String getWhereClause(RawDB rawdb, long tbid)
		throws SQLException
	{
		String sql = "";
		boolean first = true; 
		for(int dnfno: this.m_queryMeasDNF.keySet()){
			List<QueryMeasurement> nonAggMeasAnd = m_queryMeasDNF.get(dnfno);
			List<Pair<QueryMeasurement,String>> Attributes = new ArrayList<Pair<QueryMeasurement,String> >();
			String nonAggWhereclasue = formNonAggCNFWhereSQL(rawdb,tbid,nonAggMeasAnd,Attributes);
			
			if(Attributes!=null&&Attributes.size()>0){	
				if(first){
					sql+=nonAggWhereclasue;
					first = false;
				}else{
					sql+= " OR " +nonAggWhereclasue;
				}
			}
		}
		
		if(sql.length()>0){
			sql = "("+sql+")";
		}
		return sql;
	}
	
	/**
	 * get the where clause conditions (no where keyword) for this OM query basic
	 * @param rawdb
	 * @return
	 * @throws SQLException
	 */
	public Map<Long, String> getWhereClause(RawDB rawdb) throws SQLException
	{
		
		List oneBasicQueryDNFWhereClause = new ArrayList();
		Set<Long> commonTid = new TreeSet<Long>();
		boolean first = true; 
		
		for(int dnfno: this.m_queryMeasDNF.keySet()){
			List<QueryMeasurement> nonAggMeasAnd = m_queryMeasDNF.get(dnfno);
			Map<Long, List<Pair<QueryMeasurement,String>> > tb2Attribute = new TreeMap<Long, List<Pair<QueryMeasurement,String> >>(); 
			Map<Long, String> tbid2nonAggWhereclasue = formNonAggCNFWhereSQL(rawdb,nonAggMeasAnd,tb2Attribute);
			
			oneBasicQueryDNFWhereClause.add(tbid2nonAggWhereclasue);
			
			if(first){
				commonTid.addAll(tbid2nonAggWhereclasue.keySet());
				first = false;
			}else{
				commonTid.retainAll(tbid2nonAggWhereclasue.keySet());
			}
		}
		
		
		//merge the where clause for different tables
		Map<Long, String> tbid2nonAggWhereClause = OMQuery.MergeRDBWhere(oneBasicQueryDNFWhereClause, commonTid, "OR");
		
		return tbid2nonAggWhereClause;
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
			Map<Long, List<Pair<QueryMeasurement,String>> > tb2Attribute) 
		throws SQLException, Exception
	{
		Set<OboeQueryResult> nonAggQueryResult = new TreeSet<OboeQueryResult>();
	
		//2. form SQL and execute query, the results should be unioned since they come from data table		
		for(Map.Entry<Long, List<Pair<QueryMeasurement,String> >> entry: tb2Attribute.entrySet()){
			Long tbId = entry.getKey();
			//pair is <queryMeasurement,attribute name>
			List<Pair<QueryMeasurement,String>> chaAttributeNamePairList= entry.getValue();
			
			String sql = "SELECT DISTINCT "+tbId +" AS did";
			if(resultWithRecord){
				sql +=", record_id ";
			}
			sql += " FROM " + RawDB.TB_PREFIX+tbId;
			String whereSql = " WHERE "+formNonAggCNFWhereSQLsub(chaAttributeNamePairList);
			sql +=whereSql;
			
			sql += ";";
			
			//3. execute sql
			//System.out.println(Debugger.getCallerPosition()+"sql= "+sql);
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
			Map<Long, List<Pair<QueryMeasurement,String>> > outtb2Attribute) 
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
		outtb2Attribute.putAll(tmptb2Attribute);
		
		//2. form SQL and execute query, the results should be unioned since they come from data table		
		for(Map.Entry<Long, List<Pair<QueryMeasurement,String> >> entry: outtb2Attribute.entrySet()){
			Long tbId = entry.getKey();
			//pair is <queryMeasurement,attribute name>
			List<Pair<QueryMeasurement,String>> chaAttributeNamePairList= entry.getValue();
			String whereSql = formNonAggCNFWhereSQLsub(chaAttributeNamePairList);
			tbid2nonAggWhereclasue.put(tbId, whereSql);
		}
		
		return tbid2nonAggWhereclasue;
	}
	
	/**
	 * Form the where clause for a basic query for table with given id "tbid"
	 * @param rawdb
	 * @param tbid
	 * @param nonAggMeasAND
	 * @param outAttributes
	 * @return
	 * @throws SQLException
	 */
	private String formNonAggCNFWhereSQL(RawDB rawdb, long tbid, List<QueryMeasurement> nonAggMeasAND,
			List<Pair<QueryMeasurement,String> > outTbAttributes) 
		throws SQLException
	{
		Map<String, QueryMeasurement> cha2qm= new HashMap<String, QueryMeasurement>();
		for(QueryMeasurement qm: nonAggMeasAND){
			String chaCond = qm.getCharacteristicCond();
			if(chaCond!=null&&chaCond.length()>0){
				cha2qm.put(chaCond, qm);
			}
		}
		List<Pair<QueryMeasurement,String>> chaAttributeNamePairList = rawdb.retrieveTbAttribute(cha2qm,tbid);
		
				
		//2. form SQL and execute query, the results should be unioned since they come from data table		
		String whereSql = "";
		if(chaAttributeNamePairList!=null&&chaAttributeNamePairList.size()>0){
			outTbAttributes.addAll(chaAttributeNamePairList);
			whereSql = formNonAggCNFWhereSQLsub(chaAttributeNamePairList);
		}
		
		//return tbid2nonAggWhereclasue;
		return whereSql;
	}
	
	/**
	 * Form the basic non-aggregation where SQL for CNF (so connected with AND)
	 * 
	 * @param chaAttributeNamePairList, pair is <characteristic,attribute name>
	 * @return
	 */
	private String formNonAggCNFWhereSQLsub(
			List<Pair<QueryMeasurement,String>> chaAttributeNamePairList)
	{
		String sql="";
		if(chaAttributeNamePairList!=null&&chaAttributeNamePairList.size()>0){
	
			sql +=" (";
			for(int i=0;i<chaAttributeNamePairList.size();i++){
				Pair<QueryMeasurement,String> pair = chaAttributeNamePairList.get(i);
				QueryMeasurement qm = pair.getFirst();
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


	public int compareTo(OMQueryBasic o) {
		int cmp = m_queryLabel.compareTo(o.getQueryLabel());
		return cmp;
	}
	
	/**
	 * Form a query string (to put to query file) from this basic OM query
	 * @return
	 */
	public String formQueryString()
	{
		String queryString = "";
		queryString +=Constant.BASIC_QUERY_START+this.m_queryLabel+"\n";		
		queryString +=Constant.BASIC_QUERY_ENTITY+"'"+this.m_entityTypeNameCond+"'\n";
		
		for(int dnfno: this.m_queryMeasDNF.keySet()){
			List<QueryMeasurement> qmlist = m_queryMeasDNF.get(dnfno);
			for(int qmno = 0; qmno< qmlist.size();qmno++){
				QueryMeasurement qm = qmlist.get(qmno);
				queryString += qm.formQueryString(qmno+1,dnfno)+"\n";
			}
		}
			
		queryString +=Constant.BASIC_QUERY_END+"\n";
		
		return queryString;
	}
	

	
	/**
	 * if any query measurement contains an aggregation condition, return true
	 * else, return false
	 * @return
	 */
	public boolean containsAggregate()
	{
		for(int dnfno: this.m_queryMeasDNF.keySet()){
			List<QueryMeasurement> qmlist = m_queryMeasDNF.get(dnfno);
			for(int qmno = 0; qmno< qmlist.size();qmno++){
				QueryMeasurement qm = qmlist.get(qmno);
				if(qm.getAggregationCond()!=null&&(qm.getAggregationCond().trim().length()>0)){
					return true;
				}
			}
		}
		return false;
	}

	
}
