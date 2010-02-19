package org.ecoinformatics.oboe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Observation;



public class OboeModel {

	 private List<EntityInstance> entityInstances;
	 private List<ObservationInstance> observationInstances;
	 private List<MeasurementInstance> measurementInstances;
	 private List<ContextInstance> contextInstances;
	 
	 //index from oi --> ci list
	 private Map<ObservationInstance, List<ContextInstance>> oi2ciList;
	 
	 //index from oi --> mi list
	 private Map<ObservationInstance, List<MeasurementInstance>> oi2miList;
	 
	 public OboeModel()
	 {
		 entityInstances = new ArrayList<EntityInstance>();
		 observationInstances = new ArrayList<ObservationInstance>();
		 measurementInstances = new ArrayList<MeasurementInstance>();
		 contextInstances = new ArrayList<ContextInstance>();
		 
		 oi2ciList = new TreeMap<ObservationInstance, List<ContextInstance>>();
		 oi2miList = new TreeMap<ObservationInstance, List<MeasurementInstance>>();
	 }
	 
	 
	 public void AddEntityInstance(EntityInstance ei){
		 entityInstances.add(ei);
	 }
	 
	 public void AddObservationInstance(ObservationInstance oi){
		 observationInstances.add(oi);
	 }
	 
	 public void AddMeasurementInstance(MeasurementInstance mi){
		 measurementInstances.add(mi);
		 
		//maintain the index
		 List<MeasurementInstance> miList = oi2miList.get(mi.getObservationInstance());
		 if(miList ==null){
			 miList = new ArrayList<MeasurementInstance>();
			 oi2miList.put(mi.getObservationInstance(),miList);
		 }
		 miList.add(mi);
	 }
	 
	 public void AddContextInstance(ContextInstance ci){
		 contextInstances.add(ci);
		 
		 //maintain the index
		 List<ContextInstance> ciList = oi2ciList.get(ci.getObservationInstance());
		 if(ciList ==null){
			 ciList = new ArrayList<ContextInstance>();
			 oi2ciList.put(ci.getObservationInstance(),ciList);
		 }
		 ciList.add(ci);
	 }
	 
	 /**
	 * Get the key value of this observation instance 
	 * The key value is the key value of ONLY this observation's key measurements
	 */
//	 private String getDirectObsKeys(ObservationInstance oi,
//				boolean isContext)
//	 {
//		String keyValue = "";
//		
//		//get the key value from this observation's key measurements
//		List<MeasurementInstance> oiMeasInstanceSet = oi2miList.get(oi);
//		for(MeasurementInstance mi: oiMeasInstanceSet){
//			if(mi.getMeasurementType().isKey()){
//				if(!isContext){
//					keyValue +="mt:"+mi.getMeasurementType().getLabel()+"mv:"+mi.getMeasValue().toString();
//				}else{
//					keyValue +="cmt:"+mi.getMeasurementType().getLabel()+"cmv:"+mi.getMeasValue().toString();
//				}
//			}
//		}
//		return keyValue;
//	 }
//	
//	/**
//	 * Get the key value of this observation instance 
//	 * The key value is the key value of this observation's key measurements
//	 * and also its context observation's key measurements
//	 * 
//	 * @param oi
//	 * @return
//	 */
//	public String GetObsKeys(ObservationInstance oi)
//	{
//		String keyValue = "";
//		
//		//get the key value from this observation's key measurements
//		keyValue +=getDirectObsKeys(oi, false);	
//		
//		//get the key value from the context observation's key measurements
//		List<ContextInstance> contextInstanceList = oi2ciList.get(oi);
//		for(ContextInstance ci: contextInstanceList){
//			keyValue += getDirectObsKeys(ci.getContextObservationInstance(), true);			
//		}
//		
//		return keyValue;
//	}
}
