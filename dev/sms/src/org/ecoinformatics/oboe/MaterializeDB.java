package org.ecoinformatics.oboe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map.Entry;


import org.ecoinformatics.datamanager.DataManager;
import org.ecoinformatics.datamanager.database.DatabaseConnectionPoolInterface;
import org.ecoinformatics.datamanager.database.pooling.DatabaseConnectionPoolFactory;
import org.ecoinformatics.datamanager.download.ConfigurableEcogridEndPoint;
import org.ecoinformatics.datamanager.download.EcogridEndPointInterface;
import org.ecoinformatics.datamanager.parser.Attribute;
import org.ecoinformatics.datamanager.parser.AttributeList;
import org.ecoinformatics.datamanager.parser.DataPackage;
import org.ecoinformatics.datamanager.parser.Entity;
//import org.ecoinformatics.owlifier.OwlifierSpreadsheet;
import org.ecoinformatics.oboe.model.ConditionInstance;
import org.ecoinformatics.oboe.model.ContextInstance;
import org.ecoinformatics.oboe.model.EntityInstance;
import org.ecoinformatics.oboe.model.MeasurementInstance;
import org.ecoinformatics.oboe.model.OboeModel;
import org.ecoinformatics.oboe.model.ObservationInstance;
import org.ecoinformatics.oboe.syntheticdata.AnnotationSpecifier;
import org.ecoinformatics.sms.annotation.*;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.AnnotationManager;
import org.ecoinformatics.sms.SMS;

//import org.ecoinformatics.owlifier.*;
import org.ecoinformatics.sms.annotation.*;

public class MaterializeDB {

	private static boolean test = false;
	
	private static ArrayList readDataFromDataManager(String emlFileName,ArrayList<String> oRowStruct)
	{
		ArrayList dataset = new ArrayList();
		
		DatabaseConnectionPoolInterface connectionPool = 
            DatabaseConnectionPoolFactory.getDatabaseConnectionPoolInterface();
		String dbAdapterName = connectionPool.getDBAdapterName();
		DataManager dataManager = DataManager.getInstance(connectionPool, dbAdapterName);
		
		EcogridEndPointInterface endPointInfo = 
			new ConfigurableEcogridEndPoint();
		AttributeList attributeList;
	    Attribute attribute;
	    Attribute countAttribute;
	    DataPackage dataPackage = null;
	    DataPackage[] dataPackages = null;
		String documentURL = emlFileName;
		System.out.println("documentURL="+documentURL);
		 Entity entity = null;
		    //InputStream inputStream = null;
		    String operator = ">";
		    boolean success;
		    Integer value = new Integer(2);
		    ResultSet resultSet = null;
		    FileInputStream inputStream;
		try {
			  inputStream = new FileInputStream(documentURL);
			  dataPackage = dataManager.parseMetadata(inputStream);
			  System.out.println("Parse successfully");
			  success = dataManager.loadDataToDB(dataPackage, endPointInfo);
			  System.out.println("Load data to DB successfully");
			  Entity[] entityList = dataPackage.getEntityList();
			  System.out.println("entityList="+entityList.length);
			  entity = entityList[0];
			  System.out.println("entity="+entity.getName());
			  attributeList = entity.getAttributeList();
			  Attribute[] attributes = attributeList.getAttributes();
			  System.out.println("attributes="+attributes.length);
			  attribute = attributes[0];
			  System.out.println("attribute="+attribute.getName());
			  //countAttribute = attributes[6];
		    }
		    catch (MalformedURLException e) {
		      e.printStackTrace();
		      //throw(e);
		    }
		    catch (IOException e) {
		      e.printStackTrace();
		      //throw(e);
		    }
		    catch (Exception e) {
		      e.printStackTrace();
		      //throw(e);
		    }
		    
		    return dataset;
	}
	
//	private static void readDataFromOwlifier(String dataFileName)
//	{
//		OwlifierSpreadsheet sheet = null; 
//		try {
//			sheet = Owlifier.read(new FileInputStream(dataFileName));
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//			System.out.println("Unable to read: " + dataFileName);
//		}
//     
//		System.out.println("sheet="+sheet);
//	}
	
	/**
	 * Read annotation
	 * 
	 * @param annotFileName
	 * @return
	 */
	private static Annotation readAnnotation(String annotFileName)
	{
	
		String annot1 = annotFileName;
		URL url = null;
	    URLConnection connection = null;
	    
		try {
			url = new URL(annot1);
			connection = url.openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    // get annotation manager
	    AnnotationManager annotationManager = SMS.getInstance().getAnnotationManager();
	    try {
			annotationManager.importAnnotation(connection.getInputStream(), url.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    List<String> annotationIds =  annotationManager.getAnnotationIds();
	    System.out.println("annotationIds: " + annotationIds);
	    
	    List<Annotation> annotations =  annotationManager.getAnnotations();
	    System.out.println("annotations size= " + annotations.size());
	    
	    List<OntologyClass> entityList = annotationManager.getActiveEntities();
	    System.out.println("entityList= " + entityList);
	    
	    List<OntologyClass> charList = annotationManager.getActiveCharacteristics();
	    System.out.println("charList= " + charList);
	    
	    for(Annotation a: annotations){
	    	List<Mapping> mappingList = a.getMappings();
	    	//System.out.println("a : EML package = " +a.getEMLPackage()+", Data Table = " + a.getDataTable());
	    	System.out.println("a : mappingList size = " +mappingList.size());
	    	for(Mapping m: mappingList){
	    		System.out.println(m.getAttribute()+", " + m.getMeasurement().getLabel() +", "+m.getValue() +", "+m.getConditions());
	    	}
	    }
	    
	    if(annotations.size()>0)
	    	return annotations.get(0);
	    else
	    	return null;
	}
	
	/**
	 * Check whether this column satisfies all the conditions
	 * 
	 * @param attrName
	 * @param attValue
	 * @param conditionList
	 * 
	 * @return true if all the conditions are satisfies; otherwise, return false.
	 */
//	private boolean ifConditionSatisfied(String attrName, String attValue, List<Condition> conditionList)
//	{
//		boolean isSatisfyAllCondition = true;
//		for(Condition cond: conditionList){
//			if(!cond.getAttribute().equals(attrName))
//				continue;
//			
//			//this condition can apply to this column
//			//FIXME: ugly implementation
//			
//			String op = cond.getOperator();
//			if(op.equals(cond.LESS_THAN)){
//			}else if(op.equals(cond.GREATER_THAN)){
//			}else if(op.equals(cond.EQUAL)){
//			}else if(op.equals(cond.LESS_THAN_EQUAL)){
//			}else if(op.equals(cond.GREATER_THAN_EQUAL)){
//			}else if(op.equals(cond.NOT_EQUAL)){
//			}
//		}
//		return isSatisfyAllCondition;
//	}
	
	/**
	 * Create new orphan measurement instances
	 * @param row
	 * @param A
	 * @return
	 */
	private static Set<MeasurementInstance> CrtMeasurement(ArrayList rowStruct, ArrayList row, Annotation A)
	{
		//Keep the set of new measurement instances
		Set<MeasurementInstance> measSet = new TreeSet<MeasurementInstance>();
		
		List<Mapping> mappingList = A.getMappings();
		for(Mapping m: mappingList){
			//from this mapping get the measurement information
			Measurement measurementType = m.getMeasurement();
			String resAttribute = m.getAttribute().trim();
			List<Condition> conditionList = m.getConditions();
			String mapValue = null; 
			if(m.getValue()!=null)
				mapValue = m.getValue().trim();
			
			for(int i=0;i<rowStruct.size();i++){
				String rowAttributeName = ((String) rowStruct.get(i)).trim();
				if(!resAttribute.equals(rowAttributeName))
					continue;
				
				ConditionInstance ci = new ConditionInstance<String>();
				boolean satisfyCondition = ci.ifConditionSatisfied((String)rowStruct.get(i), row.get(i).toString(),conditionList); 
				if(!satisfyCondition)
					continue;
				
				//get measurement instance value
				String measVal = row.get(i).toString();
				if((mapValue!=null)&&(mapValue.length()>0)){
					measVal = mapValue;
				}
			
				//create a new measurement instance and add it to the set
				MeasurementInstance<String> mi = new MeasurementInstance<String>(measurementType, null, measVal);
				measSet.add(mi);
			} 
			//end of dealing with each column
		}
	
		//System.out.println("measSet = "+measSet);
		return measSet;
		
	}
	
	/**
	 * Partition measurement instances according to their observation types
	 * @param measSet
	 * @param A
	 * @return
	 */
	private static Map<Observation, Set<MeasurementInstance>> PartitionMeas(Set<MeasurementInstance> measSet, Annotation A)
	{
		Map<Observation, Set<MeasurementInstance> > obsType2MeasIdx = new TreeMap<Observation, Set<MeasurementInstance>>();
		
		for(MeasurementInstance mi : measSet){
			//get the measurement type of this measurement instance
			Measurement measType = mi.getMeasurementType();
			
			//get observation type of this measurement type
			Observation obsType = A.getObservation(measType);
			
			//add this measurement instance to the map
			Set<MeasurementInstance> obsTypeMeasurements = obsType2MeasIdx.get(obsType);
			if(obsTypeMeasurements==null){
				obsTypeMeasurements = new TreeSet<MeasurementInstance>();
				obsType2MeasIdx.put(obsType, obsTypeMeasurements);
			}
			obsTypeMeasurements.add(mi);
		}
		
		return obsType2MeasIdx;
		
	}
	
	/**
	 * Materialize entity for a given observation type
	 * 
	 * @param obsType
	 * @param obsType2MeasIdx
	 * @param entIdx
	 * @param A
	 * @param OBOE
	 * @return entity instance
	 */
	private static EntityInstance MaterializeEntity(
			Observation obsType, 
			Map<Observation, Set<MeasurementInstance>> obsType2MeasIdx, 
			Map<ObsTypeKey, EntityInstance> entIdx, 
			Annotation A, OboeModel OBOE)
	{
		String keyValue = GetObsTypeKeys(obsType, obsType2MeasIdx); 
		List<Measurement> keyMeasTypes = obsType.getKeyMeasurements();
		
		boolean hasKey = false;
		EntityInstance entityInstance = null;
		
		// when this observation is marked with "distinct yes" 
		// and some of its measurements are marked with "key yes"
		// the entity of this observation type should be unique
		//if(obsType.isDistinct()&&keyMeasTypes.size()>0) //this is used to determine the unique observation
		if(keyMeasTypes.size()>0) //this is used to determine the unique entity
		{
			hasKey = true;
		}
		
		ObsTypeKey obsTypeKey = new ObsTypeKey(obsType.getLabel(),keyValue);
		
		// Check whether I need to create a new entity instance
		boolean crtNewEntityInstance = true;
		if(hasKey){
			entityInstance = entIdx.get(obsTypeKey);			
			if(entityInstance!=null){//this entity with the key exists
				//System.out.println("Old ei: " + entityInstance);
				crtNewEntityInstance = false;
			}
		}
		
		if(crtNewEntityInstance){
			org.ecoinformatics.sms.annotation.Entity entType = obsType.getEntity();
			entityInstance = new EntityInstance(entType);
			//System.out.println("New ei: " + entityInstance);
			OBOE.AddEntityInstance(entityInstance);
			
			if(hasKey){
				//only when this is the key measurement, we need to maintain the index
				entIdx.put(obsTypeKey, entityInstance);
			}
		}
		
		return entityInstance;
	}
	
	/**
	 * Get the key value of this observation instance 
	 * The key value is the key value of ONLY this observation's key measurements
	 * 
	 * @param obsType
	 * @param obsType2MeasIdx
	 * @param isContext
	 * @return
	 */
	private static String getDirectObsTypeKeys(Observation obsType, 
			Map<Observation, Set<MeasurementInstance>> obsType2MeasIdx,boolean isContext)
	{
		String keyValue = "";
		
		//get the key value from this observation's key measurements
		Set<MeasurementInstance> thisObsTypeMeasInstanceSet = obsType2MeasIdx.get(obsType);
		for(MeasurementInstance mi: thisObsTypeMeasInstanceSet){
			if(mi.getMeasurementType().isKey()){
				if(!isContext){
					//keyValue +="mt:"+mi.getMeasurementType().getLabel()+"mv:"+mi.getMeasValue().toString();
					keyValue +=mi.getMeasurementType().getLabel()+"_"+mi.getMeasValue().toString();
				}else{
					//keyValue +="cmt:"+mi.getMeasurementType().getLabel()+"cmv:"+mi.getMeasValue().toString();
					keyValue +="c"+mi.getMeasurementType().getLabel()+"_"+mi.getMeasValue().toString();
				}
			}
		}
		return keyValue;
	}
	
	/**
	 * Get the key value of this observation instance 
	 * The key value is the key value of this observation's key measurements
	 * and also its context observation's key measurements
	 * 
	 * @param obsType
	 * @param obsType2MeasIdx
	 * @return
	 */
	private static String GetObsTypeKeys(Observation obsType, 
			Map<Observation, Set<MeasurementInstance>> obsType2MeasIdx)
	{
		String keyValue = "";
		
		//get the key value from this observation's key measurements
		keyValue +=getDirectObsTypeKeys(obsType, obsType2MeasIdx, false);	
		
	
		//get the key value from the context observation's key measurements
		List<Context> contextList = obsType.getContexts();
		for(Context c: contextList){
			Observation contextObsType = c.getObservation();
			if(c.isIdentifying()){
				keyValue += getDirectObsTypeKeys(contextObsType, obsType2MeasIdx, true);
			}
		}
		
		return keyValue;
	}
	
	/**
	 * Materialize the observation instances 
	 * 
	 * @param obsType
	 * @param entInstance
	 * @param obsType2MeasIdx
	 * @param obsIdx
	 * @param ioContextIdx
	 * @param A
	 * @param ioOBOE
	 */
	private static void MaterializeObs(Observation obsType, 
			EntityInstance entInstance,
			Map<Observation, Set<MeasurementInstance>> obsType2MeasIdx, 
			Map<ObsTypeKey, ObservationInstance> obsIdx, 
			Map<Observation, ObservationInstance> ioContextIdx, 
			Annotation A, OboeModel ioOBOE)
	{
		String keyValue = GetObsTypeKeys(obsType, obsType2MeasIdx);
		boolean crtNewObsInstance  = true;
		ObservationInstance obsInstance = null;
		
		ObsTypeKey obsTypeKey = new ObsTypeKey(obsType.getLabel(),keyValue);
		
		// check whether there exists an observation instance or not
		if(obsType.isDistinct()){
			obsInstance = obsIdx.get(obsTypeKey);
			if(obsInstance!=null){
				crtNewObsInstance = false;
				//System.out.println("Old oi: " + obsInstance);
			}
		}
		
		//need to create a new observation instance
		if(crtNewObsInstance){
			obsInstance = new ObservationInstance(obsType,entInstance);
			ioOBOE.AddObservationInstance(obsInstance);
			//System.out.println("New oi: " + obsInstance);
			if(obsType.isDistinct()){
				obsIdx.put(obsTypeKey,obsInstance);
			}
			
			//connect the measurement instances to this observation instance
			Set<MeasurementInstance> miSet = obsType2MeasIdx.get(obsType);
			for(MeasurementInstance mi: miSet){
				mi.setObservationInstance(obsInstance);
				//System.out.println("Connect mi: " + mi);
				ioOBOE.AddMeasurementInstance(mi);
			}
		}	
		
		//update the context index, which is also output
		ioContextIdx.put(obsType, obsInstance);
	}
	
	/**
	 * Materialize context instances
	 * 
	 * @param contextIdx
	 * @param A
	 * @param ioOBOE
	 * @throws Exception 
	 */
	private static void MaterializeContext(
			Map<Observation, ObservationInstance> contextIdx, 
			Annotation A, OboeModel ioOBOE) throws Exception
	{
		Iterator<Entry<Observation, ObservationInstance>> iter = contextIdx.entrySet().iterator(); 
		while(iter.hasNext()){
			Entry<Observation, ObservationInstance> entry = iter.next();
			Observation obsType = entry.getKey();
			ObservationInstance obsInstance = entry.getValue();
			
			List<Context> contextList = obsType.getContexts();
			//List<Observation> contextObsTypeList = new ArrayList<Observation>();
			
			for(Context c: contextList){
				Observation contextObsType = c.getObservation();
				ObservationInstance contextObsInstance = contextIdx.get(contextObsType);
				
				//create a new context instance and put it into oboe
				ContextInstance contextInstance = new ContextInstance(obsInstance,c,contextObsInstance);
								
				boolean added = ioOBOE.AddContextInstance(contextInstance);
				//if(added){
				//	System.out.println("Add contextInstance="+contextInstance);
				//}
			}
		}
	}
	
	/**
	 * @author cao
	 * @param dataFileName
	 * @param annotFileName
	 * @param oboeFilePrefix
	 * @throws Exception 
	 */
	public static OboeModel MaterializeDB(
			String emlFileName, String dataFileName, String annotFileName, String oboeFileName,
			String rdfFileName) 
		throws Exception		
	{
		//1. read data
		ArrayList<String> rowStruct = new ArrayList<String>();
		ArrayList dataset = null; 		//each element is a row, which is also an arraylist		
		if(test){
			dataset = TestData.setTestData1(rowStruct);				
		}else{
			dataset = CSVDataReader.read(dataFileName, rowStruct);			
		}
		System.out.println("rowStruct = "+ rowStruct);
		//System.out.println("dataset = "+ dataset);
		
		//2. read annotation
		Annotation A = null;
		if(annotFileName.endsWith(".xml")){ //this is an annotation file
			A = readAnnotation(annotFileName);
		}else{
			//this is an annotation specification file
			AnnotationSpecifier a = new AnnotationSpecifier();
			a.readAnnotationSpecFile(annotFileName);
			a.setDefaultMapping(); //set default mapping
			A = a.getAnnotation();
		}
        
        //3. materialization
		long t1 = System.currentTimeMillis();
		OboeModel OBOE = new OboeModel();
		Map<ObsTypeKey, EntityInstance> entIdx = 
			new TreeMap<ObsTypeKey, EntityInstance>(); //<ObsTypeId, KeyVal> --> entity instance
		Map<ObsTypeKey, ObservationInstance> obsIdx = 
			new TreeMap<ObsTypeKey, ObservationInstance>(); //<ObsTypeId, KeyVal> --> observation instance
		
		for(int i=0;i<dataset.size();i++){
			ArrayList row = (ArrayList)dataset.get(i);
			//System.out.println("i="+i+", dataset size="+dataset.size());
			//Step 1: define measurement instances
			Set<MeasurementInstance> measSet = CrtMeasurement(rowStruct, row,A);
			
			//Step 2: partitiono the measurement instances according to observation types
			Map<Observation, Set<MeasurementInstance>> obsType2MeasIdx = PartitionMeas(measSet, A);
			
			Map contextIdx = new TreeMap();
			for(Observation obsType : obsType2MeasIdx.keySet()){
				//Step 3: Find or create the entity instance for each observation type partition
				EntityInstance entInstance = MaterializeEntity(obsType, obsType2MeasIdx, entIdx, A, OBOE);
				
				//Step 4: Find or create the observation instance for each observation type partition
				MaterializeObs(obsType, entInstance,obsType2MeasIdx,obsIdx,contextIdx,A, OBOE);
			}
			
			//if(i==50){
			//	System.out.println("test.");
			//}
			//Step 5: Assign the context observation instances
			MaterializeContext(contextIdx, A, OBOE);
		}
		long t2 = System.currentTimeMillis();
		
		//System.out.println(OBOE);
		System.out.println("\n-----------\nTime used (Materialization): " + (t2-t1) +" ms" +" = "+ ((t2-t1)/1000) +"s\n-----------\n");
		
		t1 = System.currentTimeMillis();
		OBOE.toCSV(oboeFileName);
		OBOE.toRDF(rdfFileName);		
		t2 = System.currentTimeMillis();
		System.out.println("\n-----------\nTime used (File writing): " + (t2-t1) +" ms" +" = "+ ((t2-t1)/1000) +"s\n-----------\n");
		
		return OBOE;	
	}
	
	
	
	
	/**
	 * @author cao
	 * @param args
	 * 
	 * 
	E.g., to set parameters in the Arguments field

	er-2008-ex3-eml.xml //now this file is not used
	er-2008-ex3-data.txt
	er-2008-ex3-annot.xml
	er-2008-ex3-oboe.csv

	 */
	public static void main(String[] args) {
		
		//if(args.length!=4){
		//	System.out.println("Usage: ./MaterializeDB <0. Eml file name> <1. data file name> " +
		//			"<2. annotation [specification] file name> " +
		//			"<3. output OBOE file prefix>");
		//		return;
		//}
		// Get input parameters
		//String emlFileName = Constant.localOutputUriPrefix + args[0];
		//String dataFileName = Constant.localOutputUriPrefix + args[1];
		//String annotFileName = Constant.localOutputUriPrefix + args[2]; 
		//String oboeFileName = Constant.localOutputUriPrefix +args[3];
		
		if(args.length!=2){
			System.out.println("Usage: ./MaterializeDB <1. file prefix name> <2. row num>");
			return;
		}
		String emlFileName = Constant.localOutputUriPrefix + args[0] + Constant.C_EML_FILE_SUFFIX;
		String annotFileName = Constant.localOutputUriPrefix + args[0] + Constant.C_ANNOT_SPEC_FILE_SUFFIX;
		String dataFileName = Constant.localOutputUriPrefix +args[0] + "-n"+args[1]+ Constant.C_DATA_FILE_SUFFIX;
		String oboeFileName = Constant.localOutputUriPrefix +args[0] + "-n"+args[1]+Constant.C_OUT_CSV_FILE_SUFFIX;
		String rdfFileName =  Constant.localOutputUriPrefix +args[0] + "-n"+args[1]+Constant.C_OUT_RDF_FILE_SUFFIX;
		
		int numOfRows = Integer.parseInt(args[1]);
		
		// Confirm parameters
		System.out.println("emlFileName="+emlFileName);
		System.out.println("annotFileName="+annotFileName);
		System.out.println("dataFileName="+dataFileName);
		System.out.println("oboeFileName="+oboeFileName);
		System.out.println("rdfFileName="+rdfFileName);
		System.out.println("numOfRows="+numOfRows+"\n");
		
		try {
			OboeModel OBOE = MaterializeDB(emlFileName,dataFileName, annotFileName, oboeFileName, rdfFileName);
			System.out.println("********************\nOutput OBOE CSV file is in: "+oboeFileName+
					"\nOutput OBOE RDF file is in: "+rdfFileName+
					"\n********************");
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
	}

}
