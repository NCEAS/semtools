package org.ecoinformatics.oboe.query;

import java.util.List;
import java.util.ArrayList;

import org.ecoinformatics.oboe.Debugger;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Characteristic;

import org.ecoinformatics.oboe.model.*;

public class OMQuery {
	
	private Observation m_qObsType = null;
	private MeasurementInstance m_queryMi = null;
	private Context m_qContextType = null;
	
	/**
	 * Parse the query text to a query structure
	 * @param queryText
	 */
	public void parse(String queryText){
		System.out.println(queryText);
		System.out.println(Debugger.getCallerPosition()+"Notfinished...");
		System.exit(0);
	}
	
	/**
	 * Set a test query: with TaxonomicTypeName characteristic = Picea rubens
	 */
	public void setTest()
	{
		Characteristic characteristic = new Characteristic();
		characteristic.setName("TaxonomicTypeName");
		Measurement measurementType = new Measurement();
		measurementType.addCharacteristic(characteristic);
		m_queryMi = new MeasurementInstance(measurementType,"Picea rubens");
	}
	
	/**
	 * Convert a query to a string, for output purpose
	 */
	public String toString()
	{
		String str="";
		if(m_queryMi!=null){
			Measurement mt = m_queryMi.getMeasurementType();
			str +="Measurment ";
			if(mt.getCharacteristics()!=null){
				Characteristic cha = mt.getCharacteristics().get(0);
				str += ("Characteristic "+cha.getName()+" is ");
			}
			String mVal = m_queryMi.getMeasValue();
			if(mVal!=null){
				str +=" Value: " + mVal;
			}
		}
		
		return str;
	}
	
	/**
	 * Get the list of query measurement instances
	 * 	
	 * @return
	 */
	public List<MeasurementInstance> getQueryMeasements(){
		List<MeasurementInstance> queryMeasList = new ArrayList<MeasurementInstance>();
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
		List<MeasurementInstance> queryMeasList = getQueryMeasements();
		
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
