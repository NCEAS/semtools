package org.ecoinformatics.oboe.query;

import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.ecoinformatics.oboe.Debugger;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Characteristic;

import org.ecoinformatics.oboe.model.*;

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
	List<OMQueryBasic> m_query; 
	Map<String, OMQueryBasic> m_queryIndex; 
	
	//this is null or size is zero, then, no context
	//All the queries in the structure are formed using context
	//Otherwise, they are just put together using logic OR.
 	Map<String, String> m_queryContext; 

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
				oneLine = queryLines.get(++i);				
				while(oneLine.contains(Constant.MEASUREMENT_START)){
					if((i+6)<queryLines.size()){
						throw new Exception("Query is not valid, measurement is not right"+queryLines);
					}
					String cha = queryLines.get(++i).substring(Constant.CHARACTERISTIC.length());
					String standard = queryLines.get(++i).substring(Constant.STANDARD.length());
					String condition = queryLines.get(++i).substring(Constant.COND.length());
					String aggregationFunc = queryLines.get(++i).substring(Constant.AGGREGATION.length());
					String DNFnoStr = queryLines.get(++i).substring(Constant.DNFNO.length());
					
					int dnfNo = Integer.parseInt(DNFnoStr);
					QueryMeasurement queryMeas = new QueryMeasurement();
					queryMeas.setCharacteristic(cha);
					queryMeas.setStandard(standard);
					queryMeas.setCondition(condition);
					queryMeas.setAggregationFunc(aggregationFunc);
					oneBasicQuery.addMeasDNF(dnfNo, queryMeas);
					
					oneLine = queryLines.get(++i);
					if(oneLine.equals(Constant.MEASUREMENT_END)==false){
						throw new Exception("Query is in valid, measurement does not have end symbol.");
					}
				}
				
				oneLine = queryLines.get(++i);
				if(oneLine.equals(Constant.BASIC_QUERY_END)==false){
					throw new Exception("Query is in valid, entity does not have end symbol.");
				}
				m_query.add(oneBasicQuery);
				m_queryIndex.put(oneBasicQuery.getQueryLabel(), oneBasicQuery);
			}else if(oneLine.contains(Constant.CONTEXT_START)){
				//parse the context
				oneLine = queryLines.get(++i);
				while(!oneLine.equals(Constant.CONTEXT_END)){
					int pos = oneLine.indexOf(Constant.CONTEXT_SEPARATOR);
					String bq1str = oneLine.substring(0, pos);
					String bq2str = oneLine.substring(pos+Constant.CONTEXT_SEPARATOR.length());
					m_queryContext.put(bq1str,bq2str);
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
		queryMeas.setCharacteristic("TaxonomicTypeName");
		queryMeas.setStandard(null);
		queryMeas.setCondition("=Picea rubens");
		q.addMeasDNF(1, queryMeas);
		
//		Characteristic characteristic = new Characteristic();
//		characteristic.setName("TaxonomicTypeName");
//		Measurement measurementType = new Measurement();
//		measurementType.addCharacteristic(characteristic);
//		m_queryMi = new MeasurementInstance(measurementType,"Picea rubens");
	}
	
	/**
	 * Convert a query to a string, for output purpose
	 */
	public String toString()
	{
		String str="";
//		if(m_queryMi!=null){
//			Measurement mt = m_queryMi.getMeasurementType();
//			str +="Measurment ";
//			if(mt.getCharacteristics()!=null){
//				Characteristic cha = mt.getCharacteristics().get(0);
//				str += ("Characteristic "+cha.getName()+" is ");
//			}
//			String mVal = m_queryMi.getMeasValue();
//			if(mVal!=null){
//				str +=" Value: " + mVal;
//			}
//		}
		for(int i=0;i<m_query.size();i++){
			str+=m_query.get(i).toString()+"\n";
		}
		str+="CONTEXT\n";
		str+=this.m_queryContext.toString();
		
		return str;
	}
	
	/**
	 * Get the list of query measurement instances
	 * 	
	 * @return
	 */
	public List<QueryMeasurement> getQueryMeasements(){
		List<QueryMeasurement> queryMeasList = new ArrayList<QueryMeasurement>();
		queryMeasList.add(m_queryMi);
		
		return queryMeasList;
	}
	
	/**
	 * Get the measurement conditions
	 * @return
	 */
	public String getQueryMeasurementString()
	{
		String str = "";
		
		//TODO: a lot more to do
		List<QueryMeasurement> queryMeasList = getQueryMeasements();
		
		for(int i=0;i<queryMeasList.size();i++){
			MeasurementInstance mi = queryMeasList.get(i);
			Measurement mt = mi.getMeasurementType();
			Characteristic cha = null;
			if(mt.getCharacteristics()!=null){
				cha = mt.getCharacteristics().get(0);
				str += ("mi.mlabel= mt.mtype AND mt.characteristic='"+ cha+"'");
			}
			String mVal = mi.getMeasValue();
			if(cha!=null&&mVal!=null){
				str += (" AND mi.mvalue = '"+ mVal+"'");
			}
		}
		return str;
	}
	
	
	/**
	 * Based on different query evaluation strategy, perform query.
	 * 
	 * @param queryStrategy
	 * @return
	 * @throws Exception 
	 */
	public OboeQueryResult execute(int queryStrategy) throws Exception{
		OboeQueryResult queryResult = null;
		if(queryStrategy == Constant.QUERY_REWRITE){
			System.out.println(Debugger.getCallerPosition() + "To come...");
			System.exit(0);
			
		}else if(queryStrategy == Constant.QUERY_MATERIALIZED_DB){
			MDB materializedDB = new MDB();
			queryResult = materializedDB.query(this);
		}else if(queryStrategy == Constant.QUERY_PARTIAL_MATERIALIZED_DB){
			System.out.println(Debugger.getCallerPosition() + "To come...");
			System.exit(0);
		}
		return queryResult;
	}
}
