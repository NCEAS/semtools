package org.ecoinformatics.oboe.query;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.ecoinformatics.oboe.Debugger;
import org.ecoinformatics.oboe.datastorage.MDB;
import org.ecoinformatics.oboe.model.*;


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
	 * Execute this query against the materialized database 
	 * The other way is to form simple query and work on the results from the program
	 * 
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
