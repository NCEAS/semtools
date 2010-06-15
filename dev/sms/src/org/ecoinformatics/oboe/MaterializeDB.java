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
import org.ecoinformatics.oboe.model.ConditionInstance;
import org.ecoinformatics.oboe.model.ContextInstance;
import org.ecoinformatics.oboe.model.EntityInstance;
import org.ecoinformatics.oboe.model.MeasurementInstance;
import org.ecoinformatics.oboe.model.OboeModel;
import org.ecoinformatics.oboe.model.ObsTypeKey;
import org.ecoinformatics.oboe.model.ObservationInstance;
import org.ecoinformatics.oboe.syntheticdata.AnnotationSpecifier;
import org.ecoinformatics.oboe.util.Debugger;
import org.ecoinformatics.sms.annotation.*;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.AnnotationManager;
import org.ecoinformatics.sms.SMS;

//import org.ecoinformatics.owlifier.*;
import org.ecoinformatics.sms.annotation.*;


public class MaterializeDB {

	private static boolean test = false;
	private static final boolean TEST_CONTEXT = false;
	
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
	private static Annotation readRemoteAnnotation(String annotFileName)
	{
	
		annotFileName = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/er-2008-ex2-annot.xml";
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
	private static Set<MeasurementInstance> CrtMeasurement(ArrayList rowStruct, ArrayList row, Annotation A, long recordId)
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
				mi.setRecordId(recordId);
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
			Long recordId,
			Map<Observation, Set<MeasurementInstance>> obsType2MeasIdx, 
			Map<ObsTypeKey, EntityInstance> entIdx, 
			Annotation A, OboeModel OBOE)
	{
		String keyValue = GetObsTypeKeys(obsType, obsType2MeasIdx); 
		List<Measurement> keyMeasTypes = obsType.getKeyMeasurements();
		//String recordId = recordIdPrefix + obsType.getLabel();
		//System.out.println(Debugger.getCallerPosition()+",keyValue="+keyValue+",keyMeasTypes="+keyMeasTypes);
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
			entityInstance.setRecordId(recordId);
			
			//System.out.println(Debugger.getCallerPosition()+"New ei: " + entityInstance);
			OBOE.AddEntityInstance(entityInstance);
			
			if(hasKey){
				//only when this is the key measurement, we need to maintain the index
				entIdx.put(obsTypeKey, entityInstance);
			}
		}
		
		//these are the record ids that are compressed by this entity instance
		entityInstance.addRecordId(recordId); 
		
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
			Annotation A, OboeModel ioOBOE,
			long recordId)
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
			obsInstance.setRecordId(recordId);
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
		
		//these are the record ids that are compressed by this observation instances
		obsInstance.addRecordId(recordId);
		
		//update the context index, which is also output
		ioContextIdx.put(obsType, obsInstance);
	}
	
	/**
	 * Materialize context instances
	 * E.g., 
	 * A -->B-->C 
	 * B -->C
	 * 
	 * if chain is false: 
	 * To materialize B, we need to put the information about B, C together for B.
	 * To materialize A, we need to put the information about A, B together for A. (note, here, without C because C is in the chain)
	 * 
	 * if chain is true
	 * To materialize B, we need to put the information about B, C together for B.
	 * To materialize A, we need to put the information about A, B, C together for A.
	 * This way, the materialized database uses more space.
	 * 
	 * @param contextIdx
	 * @param A
	 * @param ioOBOE
	 * @throws Exception 
	 */
	private static void MaterializeContext(
			Map<Observation, ObservationInstance> contextIdx, 
			Annotation A, OboeModel ioOBOE,
			boolean bMaterializeChain) throws Exception
	{
		Iterator<Entry<Observation, ObservationInstance>> iter = contextIdx.entrySet().iterator(); 
		while(iter.hasNext()){
			Entry<Observation, ObservationInstance> entry = iter.next();
			Observation obsType = entry.getKey();
			ObservationInstance obsInstance = entry.getValue();
			
			MaterializeOneObsContect(contextIdx,A,ioOBOE,bMaterializeChain,obsType,obsInstance);
			
//			List<Context> contextList = obsType.getContexts();
//			
//			for(Context c: contextList){
//				Observation contextObsType = c.getObservation();
//				ObservationInstance contextObsInstance = contextIdx.get(contextObsType);
//				
//				//create a new context instance and put it into oboe
//				ContextInstance contextInstance = new ContextInstance(obsInstance,c,contextObsInstance);
//								
//				boolean added = ioOBOE.AddContextInstance(contextInstance);
//				if(added){
//					System.out.println(Debugger.getCallerPosition()+"Add contextInstance="+contextInstance);
//				}
//				
//				//With the chain switch on, materialize the chain contexts.
//				if(bMaterializeChain){					
//					while(true){
//						Observation chainContextObsType = contextObsInstance.getObsType();
//						if(chainContextObsType==null){
//							break;
//						}
//						ObservationInstance chainContextObsInstance = contextIdx.get(chainContextObsType);
//						if(chainContextObsInstance==null){
//							break;
//						}
//						ContextInstance chainContextInstance = new ContextInstance(obsInstance,c,chainContextObsInstance);
//						
//						added = ioOBOE.AddContextInstance(chainContextInstance);
//						if(added){
//							System.out.println(Debugger.getCallerPosition()+"Add contextInstance="+contextInstance);
//						}
//						
//						contextObsInstance = chainContextObsInstance;
//					}
//				}
//			}
		}
	}
	
	/**
	 * For the given observation type (obsTypeToExpand), 
	 * find its DIREECT context instances, and link them to the given observation instance (obsInstance)
	 * 
	 * If (bMaterializeChain) is true, 
	 * this will link ALL the CHAIN context instance(s) to obsInstance.
	 * 
	 * If (bMaterializeChain) is false, 
	 * this will link ONLY the DIRECT context instance(s) to obsInstance.
	 * 
	 * @param contextIdx
	 * @param A
	 * @param ioOBOE
	 * @param bMaterializeChain
	 * @param obsTypeToExpand
	 * @param obsInstance
	 * @throws Exception
	 */
	private static void MaterializeOneObsContect(
			Map<Observation, ObservationInstance> contextIdx, 
			Annotation A, OboeModel ioOBOE,
			boolean bMaterializeChain,
			Observation obsTypeToExpand,
			ObservationInstance obsInstance) throws Exception
	{
		//Get all the context types of this observation type
		List<Context> contextList = obsTypeToExpand.getContexts();
		
		//For each context type, get the context observation type and context observation instance
		for(Context c: contextList){
			Observation contextObsType = c.getObservation();
			ObservationInstance contextObsInstance = contextIdx.get(contextObsType);
			
			//create a new context instance and put it into oboe
			ContextInstance contextInstance = new ContextInstance(obsInstance,c,contextObsInstance);
							
			boolean added = ioOBOE.AddContextInstance(contextInstance);
			if(added&&TEST_CONTEXT){
				System.out.println(Debugger.getCallerPosition()+"Add contextInstance="+contextInstance);
			}
			
			//With the chain switch on, materialize the chain contexts.
			if(bMaterializeChain){					
				MaterializeOneObsContect(contextIdx,A,ioOBOE,bMaterializeChain,contextObsType,obsInstance);
			}
		}
	}
	
	/**
	 * Materialize context instances with chains
	 * 
	 * 
	 * @param contextIdx
	 * @param A
	 * @param ioOBOE
	 * @throws Exception 
	 */
	private static void MaterializeContextChain(
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
				if(added){
					System.out.println(Debugger.getCallerPosition()+"Add contextInstance="+contextInstance);
				}
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
	public static OboeModel MateriaDB(
			final String emlFileName, String dataFileName, String annotFileName, String oboeFileName,
			String rdfFileName, boolean bMaterializeContextChain,
			String dbname) 
		throws Exception		
	{
		
		//1. read data
		System.out.println(Debugger.getCallerPosition()+"1. Read data ...");
		ArrayList<String> rowStruct = new ArrayList<String>();
		ArrayList<String> colType = new ArrayList<String>();
		List dataset = null; 		//each element is a row, which is also an arraylist		
		if(test){
			dataset = TestData.setTestData1(rowStruct);				
		}else{
			dataset = CSVDataReader.read(dataFileName, rowStruct,colType);			
		}
		System.out.println(Debugger.getCallerPosition()+"rowStruct = "+ rowStruct);
		//System.out.println("dataset = "+ dataset);
		
		//2. read annotation
		System.out.println(Debugger.getCallerPosition() +"2. Read annotation ...");
		Annotation A = null;
		if(annotFileName.endsWith(".xml")){ //this is an annotation file
			//A = readRemoteAnnotation(annotFileName);
			InputStream is = new FileInputStream(annotFileName);
			A = Annotation.read(is);
			//A = readAnnotation(annotFileName);
		}else{
			//this is an annotation specification file
			AnnotationSpecifier a = new AnnotationSpecifier();
			a.readAnnotationSpecFile(annotFileName);
			a.setDefaultMapping(); //set default mapping
			A = a.getAnnotation();
		}
        
        //3. Materialization
		System.out.println(Debugger.getCallerPosition()+"3. Materialization ...");
		long t1 = System.currentTimeMillis();
		OboeModel OBOE = new OboeModel();
		
		Map<ObsTypeKey, EntityInstance> entIdx = 
			new TreeMap<ObsTypeKey, EntityInstance>(); //<ObsTypeId, KeyVal> --> entity instance
		Map<ObsTypeKey, ObservationInstance> obsIdx = 
			new TreeMap<ObsTypeKey, ObservationInstance>(); //<ObsTypeId, KeyVal> --> observation instance
		
		//given a data file name, e.g., /Users/cao/DATA/SONET/svntrunk/semtools/dev/sms/oboedb/eg20-n10-data.txt
		//get the pure file name, e.g, eg20-n10-data.txt
		int pos = dataFileName.lastIndexOf('/');
		String pureFileName = dataFileName;
		if(pos>0&&pos+1<dataFileName.length()){
			pureFileName = dataFileName.substring(pos+1);
		}
		OBOE.setDatasetFile(pureFileName);
		
		//String recordIdPrefix;
		for(int i=0;i<dataset.size();i++){
			// uniqueRecordId is like eg20-n10-data.txt_R0_Cm1
			//recordIdPrefix =  "R"+i+"_C_";
			long recordId = i;
			//System.out.println(Debugger.getCallerPosition()+"uniqueRecordIdPrefix="+uniqueRecordIdPrefix);
			
			ArrayList row = (ArrayList)dataset.get(i);
			//System.out.println("i="+i+", dataset size="+dataset.size());
			//Step 3.1: define measurement instances
			Set<MeasurementInstance> measSet = CrtMeasurement(rowStruct,row,A,recordId);
			
			//Step 3.2: partition the measurement instances according to observation types
			Map<Observation, Set<MeasurementInstance>> obsType2MeasIdx = PartitionMeas(measSet, A);
			
			Map contextIdx = new TreeMap();
			for(Observation obsType : obsType2MeasIdx.keySet()){
				//Step 3.3: Find or create the entity instance for each observation type partition
				EntityInstance entInstance = MaterializeEntity(obsType, recordId,obsType2MeasIdx, entIdx, A, OBOE);
				
				//Step 3.4: Find or create the observation instance for each observation type partition
				MaterializeObs(obsType, entInstance,obsType2MeasIdx,obsIdx,contextIdx,A, OBOE,recordId);
			}
			
			//Step 3.5: Assign the context observation instances
			MaterializeContext(contextIdx, A, OBOE, bMaterializeContextChain);
		}
		long t2 = System.currentTimeMillis();
		
		//System.out.println(OBOE);
		System.out.println("\n-----------\n"+Debugger.getCallerPosition()+"Time used (Materialization): " + (t2-t1) +" ms" +" = "+ ((t2-t1)/1000) +"s\n-----------\n");
		OBOE.calSpace();
		System.out.println("\n-----------\n");
		
		t1 = System.currentTimeMillis();
		//OBOE.toCSV(oboeFileName);
		//OBOE.toRDF(rdfFileName);
		OBOE.toRDB(dbname,dataFileName,annotFileName,A);
		OBOE.saveInstanceId();
		t2 = System.currentTimeMillis();
		System.out.println(Debugger.getCallerPosition()+"Time used (File writing): " + (t2-t1) +" ms" +" = "+ ((t2-t1)/1000) +"s\n-----------\n");
		
		return OBOE;	
	}
	
	
	
	
	/**
	 * @author cao
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		
		boolean bMaterializeContextChain = true;
		
		//if(args.length!=2&&args.length!=3){
			//System.out.println("Usage: ./MaterializeDB <1. file prefix name> <2. row num> [<3. bool: materialize context chain>]");
			//System.out.println("[<3. bool: materialize context chain> default false\n");		
			//return;
		//}
		//String emlFileName = Constant.localOutputUriPrefix + args[0] + Constant.C_EML_FILE_SUFFIX;
		//String annotFileName = Constant.localOutputUriPrefix + args[0] + Constant.C_ANNOT_SPEC_FILE_SUFFIX;
		//String dataFileName = Constant.localOutputUriPrefix +args[0] + "-n"+args[1]+ Constant.C_DATA_FILE_SUFFIX;
		//String oboeFileName = Constant.localOutputUriPrefix +args[0] + "-n"+args[1]+Constant.C_OUT_CSV_FILE_SUFFIX;
		//String rdfFileName =  Constant.localOutputUriPrefix +args[0] + "-n"+args[1]+Constant.C_OUT_RDF_FILE_SUFFIX;
		//int numOfRows = Integer.parseInt(args[1]);
		
		//if(args.length==3){
		//	bMaterializeContextChain = Boolean.parseBoolean(args[2]);
		//}
		
		// E.g.
		// ./MaterializeDB null er-2008-ex2-annot.xml er-2008-ex2-data.txt er-2008-ex2 
		if(args.length<5||args.length>6){
			System.out.println("Usage: ./MaterializeDB <1. eml file name> <2. annotation file name> " +
					"<3. data file name> <4. output file prefix> <5. dbname> [<5. bool: materialize context chain>]");
			System.out.println("[<5. bool: materialize context chain> default false\n");
			return;
		}
		
		String emlFileName = Constant.localUriPrefix + args[0]; 
		String annotFileName = Constant.localUriPrefix + args[1]; 
		String dataFileName = Constant.localUriPrefix +args[2];
		String oboeFileName = Constant.localUriPrefix +args[3] +Constant.C_OUT_CSV_FILE_SUFFIX;
		String rdfFileName =  Constant.localUriPrefix +args[3] +Constant.C_OUT_RDF_FILE_SUFFIX;
		String dbname = args[4];
		if(args.length==6){
			bMaterializeContextChain = Boolean.parseBoolean(args[5]);
		}
			
		// 1. Confirm parameters
		System.out.println("\n"+Debugger.getCallerPosition()+"nemlFileName="+emlFileName);
		System.out.println(Debugger.getCallerPosition()+"annotFileName="+annotFileName);
		System.out.println(Debugger.getCallerPosition()+"dataFileName="+dataFileName);
		System.out.println(Debugger.getCallerPosition()+"oboeFileName="+oboeFileName);
		System.out.println(Debugger.getCallerPosition()+"rdfFileName="+rdfFileName);
		System.out.println(Debugger.getCallerPosition()+"materialize context chain ="+bMaterializeContextChain+"\n---------------\n");
		
		// 2. Materialize DB
		try {
			OboeModel OBOE = MateriaDB(emlFileName,dataFileName, annotFileName, oboeFileName, rdfFileName, bMaterializeContextChain,dbname);
			System.out.println(Debugger.getCallerPosition()+"********************\n" +
					Debugger.getCallerPosition()+"Output OBOE CSV file is in: "+oboeFileName+"\n"+
					Debugger.getCallerPosition()+"Output OBOE RDF file is in: "+rdfFileName+"\n"+
					Debugger.getCallerPosition()+"********************");
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
	}

}
