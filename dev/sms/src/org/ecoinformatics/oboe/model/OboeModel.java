package org.ecoinformatics.oboe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;



public class OboeModel {

	 private List<EntityInstance> m_entityInstances;
	 private List<ObservationInstance> m_observationInstances;
	 private List<MeasurementInstance> m_measurementInstances;
	 
	 private List<ContextInstance> m_contextInstances;
	 
	 //index from oi --> ci list, used in materialize DB
	 private Map<ObservationInstance, List<ContextInstance>> m_oi2ciList;
	 
	 //index from oi --> mi list, used in materialize DB
	 private Map<ObservationInstance, List<MeasurementInstance>> m_oi2miList;
	 
	 public OboeModel()
	 {
		 m_entityInstances = new ArrayList<EntityInstance>();
		 m_observationInstances = new ArrayList<ObservationInstance>();
		 m_measurementInstances = new ArrayList<MeasurementInstance>();
		 m_contextInstances = new ArrayList<ContextInstance>();
		 
		 m_oi2ciList = new TreeMap<ObservationInstance, List<ContextInstance>>();
		 m_oi2miList = new TreeMap<ObservationInstance, List<MeasurementInstance>>();
	 }
	 
	 public void AddEntityInstance(EntityInstance ei){
		 m_entityInstances.add(ei);
	 }
	 
	 public void AddObservationInstance(ObservationInstance oi){
		 m_observationInstances.add(oi);
	 }
	 
	 /**
	  * from the observation instance id, get the observation instance
	  * @param oiId
	  * @return
	  */
	 public ObservationInstance GetObservationInstance(long oiId)
	 {
		 if(m_observationInstances==null)
			 return null;
		 
		 //FIXME: this is inefficient
		 for(ObservationInstance oi: m_observationInstances){
			 if(oi.getObsId()==oiId){
				 return oi;
			 }
		 }		 
		 return null;		 
	 }
	 
	 /**
	  * From the entity instance id, get the entity instance
	  * @param eiId
	  * @return
	  */
	 public EntityInstance GetEntityInstance(long eiId)
	 {
		 if(m_entityInstances==null){
			 return null;
		 }
		 
		//FIXME: this is inefficient
		 for(EntityInstance ei: m_entityInstances){
			 if(ei.getEntId()==eiId){
				 return ei;
			 }
		 }
		 return null;
	 }
	 public void AddMeasurementInstance(MeasurementInstance mi){
		 m_measurementInstances.add(mi);
		 
		//maintain the index
		 List<MeasurementInstance> miList = m_oi2miList.get(mi.getObservationInstance());
		 if(miList ==null){
			 miList = new ArrayList<MeasurementInstance>();
			 m_oi2miList.put(mi.getObservationInstance(),miList);
		 }
		 miList.add(mi);
	 }
	 
	 public boolean AddContextInstance(ContextInstance ci) throws Exception
	 {
		 
		 //no need to add the existing ones
		 for(ContextInstance oldCi: m_contextInstances){
			 if(oldCi.isSame(ci))
				 return false;
		 }
		 
		 m_contextInstances.add(ci);
		 
		 //maintain the index
		 List<ContextInstance> ciList = m_oi2ciList.get(ci.getObservationInstance());
		 if(ciList ==null){
			 ciList = new ArrayList<ContextInstance>();
			 m_oi2ciList.put(ci.getObservationInstance(),ciList);
		 }
		 ciList.add(ci);
		 return true;
	 }
	 
	 public String toString()
	 {
		 String str="OBOE:\n";
		 str +=("m_measurementInstances: "+m_measurementInstances+"\n");
		 str +=("m_entityInstances: "+m_entityInstances+"\n");
		 str +=("m_observationInstances: "+m_observationInstances+"\n");
		 str +=("m_contextInstances: "+m_contextInstances+"\n");
		 
		 return str;
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
//		List<MeasurementInstance> oiMeasInstanceSet = m_oi2miList.get(oi);
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
//		List<ContextInstance> contextInstanceList = m_oi2ciList.get(oi);
//		for(ContextInstance ci: contextInstanceList){
//			keyValue += getDirectObsKeys(ci.getContextObservationInstance(), true);			
//		}
//		
//		return keyValue;
//	}
	 
	 private void toPrintStream(PrintStream p)
	 	throws Exception
	 {
		 p.println("EI " + this.m_entityInstances.size());
		 for(EntityInstance ei: this.m_entityInstances){
			 ei.toPrintStream(p);
		 }
		 p.println("OI " + this.m_observationInstances.size());
		 for(ObservationInstance oi: this.m_observationInstances){
			 oi.toPrintStream(p);
		 }
		 p.println("MI " + this.m_measurementInstances.size());
		 for(MeasurementInstance mi: this.m_measurementInstances){
			 mi.toPrintStream(p);
		 }
		 p.println("CI " + this.m_contextInstances.size());
		 for(ContextInstance ci: this.m_contextInstances){
			 ci.toPrintStream(p);
		 }
	 }
	 /**
	  * @param csvFileName: the absolute file name to store the CSV file 
	  */
	 public void toCSV(String csvFileName)
	 	throws FileNotFoundException, Exception
	 {
		 //FileOutputStream out; 
		 PrintStream p; 
		 
		 try {
			//out = new FileOutputStream(csvFileName);
			//p = new PrintStream(out);
			p = new PrintStream( csvFileName );
			toPrintStream(p);
			p.close();
			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			throw e;
		}
	 }
}
