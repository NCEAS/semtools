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

import org.ecoinformatics.datamanager.DataManager;
import org.ecoinformatics.datamanager.database.DatabaseConnectionPoolInterfaceTest;
import org.ecoinformatics.datamanager.download.EcogridEndPointInterfaceTest;
import org.ecoinformatics.datamanager.parser.Attribute;
import org.ecoinformatics.datamanager.parser.AttributeList;
import org.ecoinformatics.datamanager.parser.DataPackage;
import org.ecoinformatics.datamanager.parser.Entity;
import org.ecoinformatics.owlifier.OwlifierSpreadsheet;
import org.ecoinformatics.sms.annotation.*;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.AnnotationManager;
import org.ecoinformatics.sms.SMS;

import org.ecoinformatics.owlifier.*;

public class MaterializaDB {

	private static String inputUriPrefix = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/";
	private static String localInputUriPrefix = "/Users/cao/DATA/SONET/svntrunk/semtools/dev/sms/examples/";
	private static String outputUriPrefix = "https://code.ecoinformatics.org/code/semtools/trunk/dev/oboedb/";
	
	/**
	 * @author cao
	 * @param dataFileName
	 * @param annotFileName
	 * @param oboeFilePrefix
	 */
	private static void MaterializeDB(String dataFileName, String annotFileName, String oboeFilePrefix)		
	{
		//1. read data
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
		DatabaseConnectionPoolInterfaceTest connectionPool = 
            new DatabaseConnectionPoolInterfaceTest();
		String dbAdapterName = connectionPool.getDBAdapterName();
		DataManager dataManager = DataManager.getInstance(connectionPool, dbAdapterName);
		
		EcogridEndPointInterfaceTest endPointInfo = new EcogridEndPointInterfaceTest();
		AttributeList attributeList;
	    Attribute attribute;
	    Attribute countAttribute;
	    DataPackage dataPackage = null;
	    DataPackage[] dataPackages = null;
		String documentURL = localInputUriPrefix + "er-2008-ex1-eml.xml";
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
		      //inputStream = url.openStream();
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
		    
		
		//2. read annotation
//		String annot1 = annotFileName;
//        URLConnection connection = null;
//        
//		try {
//			URL url = new URL(annot1);
//			connection = url.openConnection();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//        
//        // get annotation manager
//        AnnotationManager annotationManager = SMS.getInstance().getAnnotationManager();
//        try {
//			annotationManager.importAnnotation(connection.getInputStream(), "annot1");
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//        List<String> annotationIds =  annotationManager.getAnnotationIds();
//        System.out.println("annotationIds: " + annotationIds);
//        
//        List<Annotation> annotations =  annotationManager.getAnnotations();
//        System.out.println("annotations size= " + annotations.size());
//        
//        List<OntologyClass> entityList = annotationManager.getActiveEntities();
//        System.out.println("entityList= " + entityList);
//        
//        List<OntologyClass> charList = annotationManager.getActiveCharacteristics();
//        System.out.println("charList= " + charList);
//        
//        for(Annotation a: annotations){
//        	List<Mapping> mappingList = a.getMappings();
//        	System.out.println("a : EML package = " +a.getEMLPackage()+", Data Table = " + a.getDataTable());
//        	System.out.println("a : mappingList size = " +mappingList.size());
//        	for(Mapping m: mappingList){
//        		System.out.println(m.getAttribute()+", " + m.getMeasurement().getLabel() +", "+m.getValue() +", "+m.getConditions());
//        	}
//        }
        
        //3. materialization 
	}
	
	
	
	
	/**
	 * @author cao
	 * @param args
	 * [1] data file name
	 * [2] Annotation file name
	 * [3] Output file name prefix
	 */
	public static void main(String[] args) {
		
//		if(args.length!=4){
//			System.out.println("Usage: ./MaterializeDB <1. data file name> <2. annotation file name> <3. output OBOE file prefix>");
//			return;
//		}
//		// Get input parameters
//		String dataFileName = args[1];
//		String annotFileName = args[2]; 
//		String oboeFilePrefix = args[3];
		
//		public static final String PHYSICAL_URI = "https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-gce.owl";
		
		//TODO: for testing purpose, hard code the three files, need to get this from parameters
		String dataFileName = inputUriPrefix + "er-2008-ex2-data.txt";
		String annotFileName = inputUriPrefix + "er-2008-ex2-annot.xml";
		String oboeFilePrefix = outputUriPrefix + "er-2008-ex2-oboe";
		
		MaterializeDB(dataFileName, annotFileName, oboeFilePrefix); 
		
	}

}
