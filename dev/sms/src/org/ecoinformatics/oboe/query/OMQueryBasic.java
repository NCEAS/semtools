package org.ecoinformatics.oboe.query;

import java.sql.ResultSet;
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
				result = executeDNF((MDB)db,measAND);
			}else if(db instanceof RawDB){
				result = executeDNF((RawDB)db,measAND);
			}else{
				System.out.println(Debugger.getCallerPosition()+"Not implemented yet.");
			}
			result.addAll(oneDNFresult);
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
	private Set<OboeQueryResult> executeDNF(RawDB rawdb, List<QueryMeasurement> measAND)
		throws Exception
	{	
		//1. find table id and related attribute names
		Set<String> characteristics = new HashSet<String>();
		Map<String, QueryMeasurement> cha2qm= new HashMap<String, QueryMeasurement>();
		for(QueryMeasurement qm: measAND){
			String chaCond = qm.getCharacteristicCond();
			if(chaCond!=null&&chaCond.length()>0){
				characteristics.add(chaCond);
				cha2qm.put(chaCond, qm);
			}
		}
		Map<Long, List<Pair>> tbAttribute = rawdb.retrieveTbAttribute(characteristics);
		
		//FIXME: get KEY measurements
		
		//2. form SQL and execute query, the results should be unioned since they come from data table
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		for(Map.Entry<Long, List<Pair>> entry: tbAttribute.entrySet()){
			Long tbId = entry.getKey();
			List<Pair> chaAttributeNamePairList= entry.getValue();//pair is <characteristic,attributename>
			
			String sql = "SELECT DISTINCT rid FROM " + rawdb.TB_PREFIX+tbId;
			if(chaAttributeNamePairList!=null&&chaAttributeNamePairList.size()>0){
				sql +=" WHERE (";
				for(int i=0;i<chaAttributeNamePairList.size();i++){
					Pair pair = chaAttributeNamePairList.get(i);
					
					QueryMeasurement qm = cha2qm.get(pair.getFirst());
					String valueCond = qm.getValueCond();
					if(i>0){
						sql +=" AND ";
					}
					sql += pair.getSecond() + "=" + valueCond; 
				}
				sql +=")";
			}
			//FIXME: GROUP BY, HAVING condition
			sql += ";";
			
			//3. execute sql
			Set<OboeQueryResult> oneTbResult = rawdb.dataQuery(sql);
			if(oneTbResult!=null&&oneTbResult.size()>0){
				result.addAll(oneTbResult);
			}
		}
		
		System.out.println(Debugger.getCallerPosition()+"oneDNF result="+result);
		return result;
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
	public Set<OboeQueryResult> execute(MDB mdb) throws Exception
	{
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		//for different DNF, union their results		
		for(Map.Entry<Integer, List<QueryMeasurement>> entry: m_queryMeasDNF.entrySet()){
			
			
			//for the query measurement conditions ONE DNF, intersect all the results
			List<QueryMeasurement> measAND = entry.getValue();
			
			Set<OboeQueryResult> oneDNFresult = executeDNF(mdb, measAND);
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
	private Set<OboeQueryResult> executeDNF(MDB mdb, List<QueryMeasurement> measAND)
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
