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

import org.ecoinformatics.oboe.Debugger;
import org.ecoinformatics.oboe.datastorage.*;
import org.ecoinformatics.oboe.model.*;
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
public class OMQueryBasic {
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
		for(QueryMeasurement qm: measAND){
			//This query measurement need to be aggregated
			if(qm.getAggregationFunc()!=null&qm.getAggregationFunc().length()>0){
				aggMeas.add(qm);
			}else{
				nonAggMeas.add(qm);
			}
		}
		
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
		//TODO: check whether this is right or not
		//FIXME, when there is aggregation condition, this non-aggregate condition should be incorporated to the 
		//aggregate function
		//if(first||(!first&&result.size()>0))
		if(aggMeas==null||aggMeas.size()==0){
			//when there is no aggregation function, we perform the non aggregation CNF
			Set<OboeQueryResult> nonAggQueryResult = //executeNonAggregateCNF(rawdb,resultWithRecord,nonAggMeas);
				executeNonAggregateCNF(rawdb,resultWithRecord,tb2Attribute);
			if(first){
				result.addAll(nonAggQueryResult);
			}else{
				result.retainAll(nonAggQueryResult);
			}
		}
		
		System.out.println(Debugger.getCallerPosition()+"oneCNF result="+result);
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
	private Set<OboeQueryResult> executeNonAggregateCNF(
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
			String whereSql = formNonAggCNFWhereSQL(chaAttributeNamePairList);
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
		
		//Set<String> characteristics = new HashSet<String>();
		Map<String, QueryMeasurement> cha2qm= new HashMap<String, QueryMeasurement>();
		for(QueryMeasurement qm: nonAggMeasAND){
			String chaCond = qm.getCharacteristicCond();
			if(chaCond!=null&&chaCond.length()>0){
				//characteristics.add(chaCond);
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
			String whereSql = formNonAggCNFWhereSQL(chaAttributeNamePairList);
			tbid2nonAggWhereclasue.put(tbId, whereSql);
		}
		
		return tbid2nonAggWhereclasue;
	}
	
	/**
	 *TODO: HP strange to put this as a static function
	 * @param chaAttributeNamePairList, pair is <characteristic,attribute name>
	 * @return
	 */
	public static String formNonAggCNFWhereSQL(//Map<String, QueryMeasurement> cha2qm,
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
	/**
	 * Execute this query against the materialized database 
	 * The other way is to form simple query and work on the results from the program
	 * 
	 * @deprecated
	 * @param mdb
	 * @return
	 * @throws Exception 
	 */
	public Set<OboeQueryResult> execute(MDB mdb, boolean resultWithRid) throws Exception
	{
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//for different DNF, union their results		
		for(Map.Entry<Integer, List<QueryMeasurement>> entry: m_queryMeasDNF.entrySet()){
			
			
			//for the query measurement conditions ONE DNF, intersect all the results
			List<QueryMeasurement> measAND = entry.getValue();
			
			Set<OboeQueryResult> oneDNFresult = executeOneCNF(mdb, resultWithRid, measAND);
			result.addAll(oneDNFresult);
		}
		
		System.out.println(Debugger.getCallerPosition()+"Basic query result="+result);
		return result;
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
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//for different DNF, union their results		
		boolean first= true;
		for(QueryMeasurement qm: measAND){
			Set<OboeQueryResult> oneQMresult = qm.execute(mdb, m_entityTypeNameCond);
			if(!first){
				result.retainAll(oneQMresult);				
			}else{
				result.addAll(oneQMresult);
				first=false;
			}
		}
		System.out.println(Debugger.getCallerPosition()+"oneDNF result="+result);
		return result;
	}
	
}
