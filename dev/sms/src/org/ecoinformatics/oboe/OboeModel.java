package org.ecoinformatics.oboe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class OboeModel {

	 private List<EntityInstance> entityInstances;
	 private List<ObservationInstance> observationInstances;
	 private List<MeasurementInstance> measurementInstances;
	 
	 private List<ContextInstance> contextInstances;
	 
	 //index from oi --> ci list, used in materialize DB
	 private Map<ObservationInstance, List<ContextInstance>> oi2ciList;
	 
	 //index from oi --> mi list, used in materialize DB
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
	 
	 /**
	  * from the observation instance id, get the observation instance
	  * @param oiId
	  * @return
	  */
	 public ObservationInstance GetObservationInstance(long oiId)
	 {
		 if(observationInstances==null)
			 return null;
		 
		 //FIXME: this is inefficient
		 for(ObservationInstance oi: observationInstances){
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
		 if(entityInstances==null){
			 return null;
		 }
		 
		//FIXME: this is inefficient
		 for(EntityInstance ei: entityInstances){
			 if(ei.getEntId()==eiId){
				 return ei;
			 }
		 }
		 return null;
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
	 
	 public boolean AddContextInstance(ContextInstance ci){
		 
		 //no need to add the existing ones
		 for(ContextInstance oldCi: contextInstances){
			 if(oldCi.isSame(ci))
				 return false;
		 }
		 
		 contextInstances.add(ci);
		 
		 //maintain the index
		 List<ContextInstance> ciList = oi2ciList.get(ci.getObservationInstance());
		 if(ciList ==null){
			 ciList = new ArrayList<ContextInstance>();
			 oi2ciList.put(ci.getObservationInstance(),ciList);
		 }
		 ciList.add(ci);
		 return true;
	 }
	 
	 public String toString()
	 {
		 String str="OBOE:\n";
		 str +=("measurementInstances: "+measurementInstances+"\n");
		 str +=("entityInstances: "+entityInstances+"\n");
		 str +=("observationInstances: "+observationInstances+"\n");
		 str +=("contextInstances: "+contextInstances+"\n");
		 
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
	 
	 private void toPrintStream(PrintStream p)
	 	throws Exception
	 {
		 p.println("EI " + this.entityInstances.size());
		 for(EntityInstance ei: this.entityInstances){
			 ei.toPrintStream(p);
		 }
		 p.println("OI " + this.observationInstances.size());
		 for(ObservationInstance oi: this.observationInstances){
			 oi.toPrintStream(p);
		 }
		 p.println("MI " + this.measurementInstances.size());
		 for(MeasurementInstance mi: this.measurementInstances){
			 mi.toPrintStream(p);
		 }
		 p.println("CI " + this.contextInstances.size());
		 for(ContextInstance ci: this.contextInstances){
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
