package org.ecoinformatics.oboe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.RDFNode;

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
	 
	 private void toRDF(OutputStream p)
	 	throws Exception
	 {
		 Model model = ModelFactory.createDefaultModel();
		 String uri    = RDFConstant.URI;
		 String namespace    = RDFConstant.NAMESPACE;
		 model.setNsPrefix(namespace,uri);
		 
		 // Put the entity information to the model
		 Property property = model.createProperty(uri, RDFConstant.ENTITY_TYPE);
		 Map<Long, Resource> entId2resource = new HashMap<Long, Resource>();
		 for(EntityInstance ei: this.m_entityInstances){
			 Resource r = model.createResource(uri+RDFConstant.ENTITY+ei.getEntId());
			 entId2resource.put(ei.getEntId(), r);
			 
			 r.addProperty(property, ei.getEntityType().getName());
		 }
		 
		// Put the observation information to the model
		 Property propertyEntId = model.createProperty(uri, RDFConstant.ENTITY_ID);
		 Property propertyObsType = model.createProperty(uri, RDFConstant.OBSERVATION_TYPE);
		 Map<Long, Resource> obsId2resource = new HashMap<Long, Resource>();
		 for(ObservationInstance oi: this.m_observationInstances){
			 Resource r = model.createResource(uri+RDFConstant.OBSERVATION+oi.getObsId());
			 obsId2resource.put(oi.getObsId(), r);
			 
			 r.addProperty(propertyObsType, oi.getObsType().getLabel());
			 
			 RDFNode objectEndId = entId2resource.get(oi.getEntity().getEntId());
			 Statement statement = model.createStatement(r, propertyEntId, objectEndId);
			 model.add(statement);
		 }
		
		// Put the measurement information to the model
		 Property propertyObsId = model.createProperty(uri, RDFConstant.OBSERVATION_ID);
		 Property propertyMeasType = model.createProperty(uri, RDFConstant.MEASUREMENT_TYPE);
		 Property propertyMeasValue = model.createProperty(uri, RDFConstant.MEASUREMENT_VALUE);
		 for(MeasurementInstance mi: this.m_measurementInstances){
			 Resource r = model.createResource(uri+RDFConstant.MEASUREMENT+mi.getMeasId());
			 r.addProperty(propertyMeasType, mi.getMeasurementType().getLabel());
			 
			 RDFNode objectObsId = obsId2resource.get(mi.getObservationInstance().getObsId());	
			 Statement statement1 = model.createStatement(r, propertyObsId, objectObsId);			 
			 model.add(statement1);
			 
			 RDFNode objectMeasValue = model.createLiteral(mi.getMeasValue());
			 Statement statement2 = model.createStatement(r, propertyMeasValue, objectMeasValue); 
			 model.add(statement2);
		 }
		 
		// Put the context information to the model
		 for(ContextInstance ci: this.m_contextInstances){
			 Long obsId = ci.getObservationInstance().getObsId();
			 String relationship = ci.getContextType().getRelationship().getName();
			 Long contextObsId = ci.getContextObservationInstance().getObsId();
			 
			 Resource subjectResource =  obsId2resource.get(obsId);			 
			 Property predicateProperty = model.createProperty(uri, relationship);
			 RDFNode objectRDFNode = obsId2resource.get(contextObsId);
			 
			 Statement statement = model.createStatement(subjectResource, predicateProperty, objectRDFNode);
			 model.add(statement);
		 }
		 
		 // Output the model to the file
		 model.write(p);
	 }
	 
	 
	 /**
	  * output the materialized data to CSV file
	  * @param csvFileName: the absolute file name to store the CSV file 
	  */
	 public void toCSV(String csvFileName)
	 	throws FileNotFoundException, Exception
	 {
		 PrintStream p; 
		 
		 try {
			p = new PrintStream( csvFileName );
			toPrintStream(p);
			p.close();
			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			throw e;
		}
	 }
	 
	 /**
	  * print the materialized data to RDF file
	  * 
	  * @param rdfFileName
	  * @throws FileNotFoundException
	  * @throws Exception
	  */
	 public void toRDF(String rdfFileName)
	 	throws FileNotFoundException, Exception
	 {
		 OutputStream out;
		 try {			
			out = new FileOutputStream(rdfFileName);			 
			toRDF(out);
			out.close();
			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			throw e;
		}
	 }
}
