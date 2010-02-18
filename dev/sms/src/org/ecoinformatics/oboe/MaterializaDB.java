package org.ecoinformatics.oboe;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;

import org.ecoinformatics.datamanager.DataManager;
import org.ecoinformatics.datamanager.database.DatabaseConnectionPoolInterfaceTest;
import org.ecoinformatics.datamanager.database.Query;
import org.ecoinformatics.datamanager.database.SelectionItem;
import org.ecoinformatics.datamanager.database.TableItem;
import org.ecoinformatics.datamanager.download.ConfigurableEcogridEndPoint;
import org.ecoinformatics.datamanager.download.EcogridEndPointInterface;
import org.ecoinformatics.datamanager.download.EcogridEndPointInterfaceTest;
import org.ecoinformatics.datamanager.parser.Attribute;
import org.ecoinformatics.datamanager.parser.AttributeList;
import org.ecoinformatics.datamanager.parser.DataPackage;
import org.ecoinformatics.datamanager.parser.Entity;
import org.ecoinformatics.sms.annotation.*;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.AnnotationManager;
import org.ecoinformatics.sms.SMS;


public class MaterializaDB {

	private static String inputUriPrefix = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/";
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
		
		//  the EML file describing the data
		//String documentURL = inputUriPrefix + "er-2008-ex1-eml.xml";
		String documentURL = "http://knb.ecoinformatics.org/knb/metacat/tao.1.1/xml";
		System.out.println("documentURL="+documentURL);
		
		// the connection pool and DB adapter for DML
		DatabaseConnectionPoolInterfaceTest connectionPool = 
            new DatabaseConnectionPoolInterfaceTest();
		String dbAdapterName = connectionPool.getDBAdapterName();
		// the ecogrid endpoint interface for use by DML
		//EcogridEndPointInterface endPointInfo = new EcogridEndPointInterfaceTest();
		EcogridEndPointInterface endPointInfo = new ConfigurableEcogridEndPoint();

		
		// get an instance of the DM
		DataManager dataManager = DataManager.getInstance(connectionPool, dbAdapterName);
		
		
		AttributeList attributeList;
	    Attribute attribute;
	    DataPackage dataPackage = null;
	    DataPackage[] dataPackages = null;
		Entity entity = null;
		ResultSet resultSet = null;
		InputStream inputStream;
		URL url;
		try {
			// parse the metadata from it's URL
			url = new URL(documentURL);
			inputStream = url.openStream();
			dataPackage = dataManager.parseMetadata(inputStream);
			System.out.println("Parse successfully");
			
			// load the data into local cache DB
			boolean success = 
				dataManager.loadDataToDB(dataPackage, endPointInfo);
			System.out.println("Load data to DB successfully");
			
			// inspect the tables and columns
			Entity[] entityList = dataPackage.getEntityList();
			System.out.println("entityList=" + entityList.length);
			entity = entityList[0];
			System.out.println("entity=" + entity.getName());
			attributeList = entity.getAttributeList();
			Attribute[] attributes = attributeList.getAttributes();
			System.out.println("attributes=" + attributes.length);
			attribute = attributes[0];
			System.out.println("attribute=" + attribute.getName());
			
			// construct a 'SELECT * FROM ...' query
			Query query = new Query();
			for (Attribute a : attributes) {
				SelectionItem selection = new SelectionItem(entity, a);
				query.addSelectionItem(selection);
			}
			TableItem tableItem = new TableItem(entity);
			query.addTableItem(tableItem);
			dataPackages = new DataPackage[1];
			dataPackages[0] = dataPackage;
			
			String sqlString = query.toSQLString();
			System.out.println("Query SQL = " + sqlString);
			
			// select the data from the cache DB
			try {
				resultSet = dataManager.selectData(query, dataPackages);

				if (resultSet != null) {
					System.out.println("selecting results");
					int columnCount = resultSet.getMetaData().getColumnCount();
					while (resultSet.next()) {
						for (int i = 1; i <= columnCount; i++) {
							Object column = resultSet.getString(i);
							System.out.println("resultSet[" + i + "], column =  " + column);
						}
					}
				} else {
					throw new Exception("resultSet is null");
				}
			} catch (Exception e) {
				System.err.println("Exception in DataManager.selectData()"
						+ e.getMessage());
				throw (e);
			} finally {
				if (resultSet != null)
					resultSet.close();
				dataManager.dropTables(dataPackage); // Clean-up test tables
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// throw(e);
		} catch (IOException e) {
			e.printStackTrace();
			// throw(e);
		} catch (Exception e) {
			e.printStackTrace();
			// throw(e);
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
