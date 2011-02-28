package org.ecoinformatics.sms.annotation.search.data;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cayenne.exp.Expression;
import org.ecoinformatics.datamanager.DataManager;
import org.ecoinformatics.datamanager.database.Condition;
import org.ecoinformatics.datamanager.database.DatabaseConnectionPoolInterface;
import org.ecoinformatics.datamanager.database.Query;
import org.ecoinformatics.datamanager.database.SelectionItem;
import org.ecoinformatics.datamanager.database.TableItem;
import org.ecoinformatics.datamanager.database.WhereClause;
import org.ecoinformatics.datamanager.database.pooling.DatabaseConnectionPoolFactory;
import org.ecoinformatics.datamanager.download.ConfigurableEcogridEndPoint;
import org.ecoinformatics.datamanager.parser.Attribute;
import org.ecoinformatics.datamanager.parser.DataPackage;
import org.ecoinformatics.datamanager.parser.Entity;
import org.ecoinformatics.datamanager.util.DocumentDownloadUtil;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.annotation.Triple;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.ontology.OntologyClass;

public class Materializer {

	private DataManager dataManager;
	
	private ConfigurableEcogridEndPoint endPoint = null;

	private static Materializer instance = null;
	
	private Materializer() {
		DatabaseConnectionPoolInterface connectionPool = 
			DatabaseConnectionPoolFactory.getDatabaseConnectionPoolInterface();
		String dbAdapterName = connectionPool.getDBAdapterName();
		endPoint = new ConfigurableEcogridEndPoint();
		endPoint.setSessionId("usePublic");
		dataManager = DataManager.getInstance(connectionPool, dbAdapterName);
	}
	
	public static Materializer getInstance() {
		if (instance == null) {
			instance = new Materializer();
		}
		return instance;
	}

	public boolean checkData(Annotation annotation, Measurement measurement, String operator, Object value) throws Exception {
		
		String dataPackageId = annotation.getDataPackage();
		
		// for selecting the correct attribute from the correct entity
		String dataObject = measurement.getMapping().getDataObject();
		int dataObjectIndex = Integer.parseInt(dataObject);
		String attributeName = measurement.getMapping().getAttribute();

		// First create the DataPackage object that will be used in the query.
		DocumentDownloadUtil ddu = new DocumentDownloadUtil();
		InputStream inputStream = ddu.downloadDocument(dataPackageId, endPoint);
		DataPackage dataPackage = dataManager.parseMetadata(inputStream);
		boolean success = dataManager.loadDataToDB(dataPackage, endPoint);
		Entity entity = dataPackage.getEntityList()[dataObjectIndex];
		Attribute[] attributes = entity.getAttributeList().getAttributes();
		// find the index of the attribute based on name (id is not required)
		int attributeIndex = 0;
		for (Attribute a: attributes) {
			if (a.getName().equals(attributeName)) {
				break;
			}
			attributeIndex++;
		}
		Attribute attribute = attributes[attributeIndex];
		
		ResultSet resultSet = null;
		int rows = 0;
		
		//Now build a query, execute it, and see what we got
		if (success && dataPackage != null) {
			DataPackage[] dataPackages = {dataPackage};
			Query query = new Query();
			/* SELECT clause */
			SelectionItem selectionItem = new SelectionItem(entity, attribute);
			query.addSelectionItem(selectionItem);
			/* FROM clause */
			TableItem tableItem = new TableItem(entity);
			query.addTableItem(tableItem);
			/* WHERE clause with condition */
			// type the value correctly
			try {
				value = Double.parseDouble(value.toString());
			} catch (Exception e) {
				// not a double
			}
			
			Condition condition = 
				new Condition(entity, attribute, operator, value);
			WhereClause whereClause = new WhereClause(condition);
			query.setWhereClause(whereClause);
			System.out.println("Query SQL = " + query.toSQLString());

			try {
				resultSet = dataManager.selectData(query, dataPackages);
				if (resultSet != null) {
					while (resultSet.next()) {
						Object val = resultSet.getObject(1);
						System.out.println("resultSet[" + rows + "], value =  " + val);
						rows++;
					}
				}
			} finally {
				if (resultSet != null) {
					resultSet.close();
				}
				// clean up
				dataManager.dropTables(dataPackage);
			}
		}
		
		// were there any data rows for our criteria?
		return (rows > 0);
	}
	
	public List<Annotation> filterDataMatches(List<Annotation> annotations, Criteria criteria) {
		
		// add matches to the list
		List<Annotation> matches = new ArrayList<Annotation>(annotations);
		
		if (!criteria.isGroup() && !criteria.isContext()) {
								
				// what criteria were given?
				Class type = criteria.getType();
				OntologyClass subject = criteria.getSubject();
				String operator = criteria.getCondition();
				Object value = criteria.getValue();
				
				// find the Characteristic we are filtering data values for
				List<OntologyClass> characteristics = new ArrayList<OntologyClass>();
				
				if (type != null && type.equals(Characteristic.class)) {
					characteristics.add(subject);
				}
				// expand the measurement template if given
				if (type != null && type.equals(Measurement.class)) {
					List<OntologyClass> classes = null;
					// characteristic
					classes = Measurement.lookupRestrictionClasses(subject, Characteristic.class);
					if (classes != null) {
						characteristics.addAll(classes);
					}
				}
				
				// if there's nothing to compare, then we are done
				if (characteristics.isEmpty() || value == null || operator == null) {
					return matches;
				}	
				
				// look for each possible Measurement match to compare data
				for (Annotation annotation: annotations) {
					int dataMatchCount = 0;
					List<Measurement> measurements = annotation.getMeasurements(characteristics);
					for (Measurement measurement: measurements) {
						boolean dataMatch = false;
						try {
							dataMatch = checkData(annotation, measurement, operator, value);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (dataMatch) {
							dataMatchCount++;
						}
					}
					// HACKALERT, TODO: not true SQL-like conditions on data (i.e. ranges) 
					// if the data matched, then add the annotation to the result
					if (dataMatchCount < 1) {
						matches.remove(annotation);
					}
				}
		   }
		   else {
			   // iterate through the subcriteria
			   if (criteria.getSubCriteria() != null) {
				   Iterator<Criteria> iter = criteria.getSubCriteria().iterator();
				   while (iter.hasNext()) {
					   Criteria subcriteria = iter.next();
					   matches = filterDataMatches(matches, subcriteria);
				   }
			   }
		   }
		
		
		return matches;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			// get the annotation and measurement to check
			String annotationId = "benriver.278.3";
			DocumentDownloadUtil ddu = new DocumentDownloadUtil();
			InputStream annotationInputStream = ddu.downloadDocument(annotationId, Materializer.getInstance().endPoint);
			Annotation annotation = Annotation.read(annotationInputStream);
			Measurement measurement = annotation.getMeasurement("m1");
			
			// check for values matching condition
			Materializer.getInstance().checkData(annotation, measurement, ">", 35);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
