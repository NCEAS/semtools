package org.ecoinformatics.oboe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.RDFNode;

import org.ecoinformatics.oboe.Constant;
import org.ecoinformatics.oboe.datastorage.MDB;
import org.ecoinformatics.oboe.util.Debugger;
import org.ecoinformatics.sms.annotation.*;

public class OboeModel {
	public static long gOldMaxEntId=0;
	public static long gOldMaxObsId=0;
	public static long gOldMaxMeasId=0;
	
	public List<EntityInstance> m_entityInstances;
	public List<ObservationInstance> m_observationInstances;
	public List<MeasurementInstance> m_measurementInstances;
 
	public List<ContextInstance> m_contextInstances;
 
	//index from oi --> ci list, used in materialize DB
	private Map<ObservationInstance, List<ContextInstance>> m_oi2ciList;
 
	//index from oi --> mi list, used in materialize DB
	private Map<ObservationInstance, List<MeasurementInstance>> m_oi2miList;
 
	private String m_datasetFile = "";
	 
	public OboeModel() throws IOException, Exception
	 {
		 m_entityInstances = new ArrayList<EntityInstance>();
		 m_observationInstances = new ArrayList<ObservationInstance>();
		 m_measurementInstances = new ArrayList<MeasurementInstance>();
		 m_contextInstances = new ArrayList<ContextInstance>();
		 
		 m_oi2ciList = new TreeMap<ObservationInstance, List<ContextInstance>>();
		 m_oi2miList = new TreeMap<ObservationInstance, List<MeasurementInstance>>();
		  
		 initializeInstanceId();
	 }
	
	/**
	 * From the property file, get the maximum entity/observation/measurement instance id
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	private void initializeInstanceId() throws IOException,Exception
	{
		 // Read properties file.
		 Properties prop = new Properties();
		 try {
			 //FIXME: where to put the property file???? 
			 FileInputStream is = new FileInputStream(Constant.localUriPrefix+"instanceid.properties");
			 prop.load(is);
		     String str = prop.getProperty("gMaxEndId");
		     if(str!=null&&str.length()>0){
		    	 gOldMaxEntId = Long.parseLong(str);
		     }else{
		    	 throw new Exception("Cannot get old maximum entity id.");
		     }
		     
		     str = prop.getProperty("gMaxObsId");
		     if(str!=null&&str.length()>0){
		    	 gOldMaxObsId = Long.parseLong(str);
		     }else{
		    	 throw new Exception("Cannot get old maximum observation id.");
		     }
		     
		     str = prop.getProperty("gMaxMeasId");
		     if(str!=null&&str.length()>0){
		    	 gOldMaxMeasId = Long.parseLong(str);
		     }else{
		    	 throw new Exception("Cannot get old maximum measurement id.");
		     }
		     is.close();
		     System.out.println(Debugger.getCallerPosition()+
					 	"gOldMaxEntId="+gOldMaxEntId+",gOldMaxObsId="+gOldMaxObsId+",gOldMaxMeasId="+gOldMaxMeasId);
		 } catch (IOException e) {
			 throw e;
		 }
		 
	}
	
	/**
	 * Save the maximum entity/observation/measurement instance id to the property file
	 * @throws IOException
	 * @throws Exception
	 */
	public void saveInstanceId() throws IOException,Exception
	{
		 // Write properties file.
		 Properties prop = new Properties();
		 try {
			 FileOutputStream outs = new FileOutputStream(Constant.localUriPrefix+"instanceid.properties");
			 prop.setProperty("gMaxMeasId", (new Long(gOldMaxMeasId)).toString());
			 prop.setProperty("gMaxObsId", (new Long(gOldMaxObsId)).toString());
			 prop.setProperty("gMaxEndId", (new Long(gOldMaxEntId)).toString());
			 prop.store(outs,"");
			 //prop.store(outs,("gMaxObsId="+gOldMaxObsId));
			 //prop.store(outs,("gMaxMeasId="+gOldMaxMeasId));
		    outs.close();
		    System.out.println(Debugger.getCallerPosition()+
				 	"gOldMaxEntId="+gOldMaxEntId+",gOldMaxObsId="+gOldMaxObsId+",gOldMaxMeasId="+gOldMaxMeasId);
		 } catch (IOException e) {
			 throw e;
		 }
	}
	
	 public String getDatasetFile() {
		 return m_datasetFile;
	 }

	 public void setDatasetFile(String mDatasetFile) {
		 m_datasetFile = mDatasetFile;
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
		 for(EntityInstance ei: this.m_entityInstances){
			 Resource r = model.createResource(uri+RDFConstant.ENTITY+ei.getEntId());
			 
			 r.addProperty(property, ei.getEntityType().getName());
		 }
		 
		// Put the observation information to the model
		 Property propertyEntId = model.createProperty(uri, RDFConstant.ENTITY_ID);
		 Property propertyObsType = model.createProperty(uri, RDFConstant.OBSERVATION_TYPE);
		 for(ObservationInstance oi: this.m_observationInstances){
			 Resource r = model.createResource(uri+RDFConstant.OBSERVATION+oi.getObsId());
			 
			 r.addProperty(propertyObsType, oi.getObsType().getLabel());
			 
			 //add it's entity resource to this observation to represent <r has_entity_id refR>
			 //Way 1: This does not work, because we need to make sure that r reference some EXISTING entity resource
			 //r.addProperty(propertyEntId,new Long(oi.getEntity().getEntId()).toString());
			 
			 //Way 2: this need to reference an existing resource, so, we retrieve it first
			 Resource refR = model.getResource(uri+RDFConstant.ENTITY+oi.getEntity().getEntId());
			 //RDFNode objectEndId = entId2resource.get(oi.getEntity().getEntId());
			 r.addProperty(propertyEntId,refR);
			 
			 //Way 3: use statement, works too
			 //Statement statement = model.createStatement(r, propertyEntId, refR);
			 //model.add(statement);
		 }
		
		// Put the measurement information to the model
		 Property propertyObsId = model.createProperty(uri, RDFConstant.OBSERVATION_ID);
		 Property propertyMeasType = model.createProperty(uri, RDFConstant.MEASUREMENT_TYPE);
		 Property propertyMeasValue = model.createProperty(uri, RDFConstant.MEASUREMENT_VALUE);
		 for(MeasurementInstance mi: this.m_measurementInstances){
			 Resource r = model.createResource(uri+RDFConstant.MEASUREMENT+mi.getMeasId());
			 r.addProperty(propertyMeasType, mi.getMeasurementType().getLabel());
			 
			 Resource refR = model.getResource(uri+RDFConstant.OBSERVATION+mi.getObservationInstance().getObsId());
			 r.addProperty(propertyObsId, refR);
			 //Statement statement1 = model.createStatement(r, propertyObsId, objectObsId);			 
			 //model.add(statement1);
			 
			 RDFNode objectMeasValue = model.createLiteral(mi.getMeasValue());
			 Statement statement2 = model.createStatement(r, propertyMeasValue, objectMeasValue); 
			 model.add(statement2);
		 }
		 
		// Put the context information to the model
		 for(ContextInstance ci: this.m_contextInstances){
			 Long obsId = ci.getObservationInstance().getObsId();
			 String relationship = Constant.DEFAULT_RELATIONSHIP;
			 if(ci.getContextType()!=null&&ci.getContextType().getRelationship()!=null)
				 relationship = ci.getContextType().getRelationship().getName();
			 Long contextObsId = ci.getContextObservationInstance().getObsId();
			 
			 Resource subjectResource = model.getResource(uri+RDFConstant.OBSERVATION+obsId);
			 Property predicateProperty = model.createProperty(uri, relationship);
			 Resource objectRDFNode = model.getResource(uri+RDFConstant.OBSERVATION+contextObsId);
			 
			 Statement statement = model.createStatement(subjectResource, predicateProperty, objectRDFNode);
			 model.add(statement);
		 }
		 
		 // Output the model to the file
		 model.write(p);
	 }
	 
	 
	 /**
	  * Import this the materialized dataset and the annotation (with type information) to databases
	  * 
	  * @param A
	 * @throws Exception 
	  */
	 public void toRDB(String dbname,String dataFileName,String annotationFileName, Annotation A) throws Exception
	 {
		 
		 System.out.println(Debugger.getCallerPosition()+"Begin toRDB");
		 long t1 = System.currentTimeMillis();
		 MDB db = new MDB(dbname);
		 
		 db.open();
		 System.out.println(Debugger.getCallerPosition()+"Import annotation file...");
		 long annotId = db.importAnnotation(A, annotationFileName); //export type information
		 
		 System.out.println(Debugger.getCallerPosition()+"Import data instances...");
		 db.importInstance(this,annotId); //export data instance information.
		 
		 //db.updateDataAnnotation(dataFileName,annotId);
		 db.close();
		 long t2 = System.currentTimeMillis();
		 System.out.println(Debugger.getCallerPosition()+"End toRDB, time used to LOAD ro RDB="+ (t2-t1) +" ms" +" = "+ ((t2-t1)/1000) +"s\n");
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
			System.out.println(Debugger.getCallerPosition()+"csvFileName="+csvFileName);
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
	 

	 
	 /**
	  * Show the space used for this materialized OBOE view
	  */
	 public void calSpace()
	 {
		 System.out.println("Number of entities:"+m_entityInstances.size());
		 System.out.println("Number of observations:"+m_observationInstances.size());
		 System.out.println("Number of measurements:" + m_measurementInstances.size());
		 System.out.println("Number of contexts:" + m_contextInstances.size());
		 
		 System.out.println("Assistating structure size:" + (m_oi2ciList.size() + m_oi2miList.size()));
	 }
}
