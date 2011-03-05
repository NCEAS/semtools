package org.ecoinformatics.sms.annotation.search.data;

import java.io.InputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ecoinformatics.datamanager.DataManager;
import org.ecoinformatics.datamanager.database.Condition;
import org.ecoinformatics.datamanager.database.DatabaseConnectionPoolInterface;
import org.ecoinformatics.datamanager.database.Query;
import org.ecoinformatics.datamanager.database.SelectionItem;
import org.ecoinformatics.datamanager.database.StaticSelectionItem;
import org.ecoinformatics.datamanager.database.TableItem;
import org.ecoinformatics.datamanager.database.Union;
import org.ecoinformatics.datamanager.database.WhereClause;
import org.ecoinformatics.datamanager.database.pooling.DatabaseConnectionPoolFactory;
import org.ecoinformatics.datamanager.download.ConfigurableEcogridEndPoint;
import org.ecoinformatics.datamanager.download.EcogridEndPointInterface;
import org.ecoinformatics.datamanager.parser.Attribute;
import org.ecoinformatics.datamanager.parser.DataPackage;
import org.ecoinformatics.datamanager.parser.Entity;
import org.ecoinformatics.datamanager.util.DocumentDownloadUtil;
import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.ontology.OntologyClass;

import edu.ucsb.nceas.metacat.dataquery.MetacatDatabaseConnectionPoolFactory;
import edu.ucsb.nceas.metacat.dataquery.MetacatEcogridEndPoint;

import au.com.bytecode.opencsv.CSVWriter;

public class Materializer {

	public static int LOCAL_CONFIGURATION = 0;
	
	public static int METACAT_CONFIGURATION = 1;

	private DataManager dataManager;
	
	private EcogridEndPointInterface endPoint = null;
	
	private Materializer(DatabaseConnectionPoolInterface connectionPool, EcogridEndPointInterface endPoint) {
		
		this.endPoint = endPoint;
		String dbAdapterName = connectionPool.getDBAdapterName();
		dataManager = DataManager.getInstance(connectionPool, dbAdapterName);
	}
	
	public static Materializer getInstance(DatabaseConnectionPoolInterface connectionPool, EcogridEndPointInterface endPoint) {
		return new Materializer(connectionPool, endPoint);
	}
	
	public static Materializer getInstance(int configuration) {
		DatabaseConnectionPoolInterface connectionPool = null;
		EcogridEndPointInterface endPoint = null;
		
		switch (configuration) {
		case 0:
			connectionPool = 
				DatabaseConnectionPoolFactory.getDatabaseConnectionPoolInterface();
			endPoint = new ConfigurableEcogridEndPoint();
			((ConfigurableEcogridEndPoint)endPoint).setSessionId("usePublic");
			break;
		case 1:
			connectionPool = MetacatDatabaseConnectionPoolFactory.getDatabaseConnectionPoolInterface();
			endPoint = new MetacatEcogridEndPoint();
			break;			
		default:
			return null;
		}
		return new Materializer(connectionPool, endPoint);
	}
	
	/**
	 * Retrieves tabular data that matches the given condition for the Annotation/Measurement provided
	 * The results are in CSV format or null if no records/data is found to match the condition
	 * @param annotation
	 * @param measurement
	 * @param operator
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private String selectData(Annotation annotation, List<OntologyClass> characteristics, String operator, Object value) throws Exception {
		
		String dataPackageId = annotation.getDataPackage();
		
		// TODO: handle multiple measurements
		List<Measurement> measurements = annotation.getMeasurements(characteristics);
		Measurement measurement = measurements.get(0);
		
		// for selecting the correct attribute from the correct entity
		String dataObject = measurement.getMapping().getDataObject();
		int dataObjectIndex = Integer.parseInt(dataObject);
		String attributeName = measurement.getMapping().getAttribute();

		// First create the DataPackage object that will be used in the query.
		// TODO: get the metadata directly from metacat store?
		DocumentDownloadUtil ddu = new DocumentDownloadUtil();
		InputStream inputStream = ddu.downloadDocument(dataPackageId, endPoint);
		DataPackage dataPackage = dataManager.parseMetadata(inputStream);
		// do this conditionally instead of every time?
		boolean success = dataManager.loadDataToDB(dataPackage, endPoint);
		Entity entity = dataPackage.getEntityList()[dataObjectIndex];
		Attribute[] attributes = entity.getAttributeList().getAttributes();
		// find the index of the attribute based on name (id is not required)
		List<Attribute> annotatedAttributes = new ArrayList<Attribute>();
		int attributeIndex = 0;
		int i = 0;
		for (Attribute a: attributes) {
			// the one we are using for the condition?
			if (a.getName().equals(attributeName)) {
				attributeIndex = i;
			}
			// otherwise annotated?
			if (annotation.getMapping(a.getName(), dataObject) != null) {
				annotatedAttributes.add(a);
			}
			i++;
		}
		Attribute attribute = attributes[attributeIndex];
				
		ResultSet resultSet = null;
		String contents = null;
		boolean hasRows = false;
		
		//Now build a query, execute it, and see what we got
		if (success && dataPackage != null) {
			DataPackage[] dataPackages = {dataPackage};
			Query query = new Query();
			/* SELECT clause */
			for (Attribute a: annotatedAttributes) {
				SelectionItem selectionItem = new SelectionItem(entity, a);
				query.addSelectionItem(selectionItem);
			}	
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
					StringWriter sw = new StringWriter();
					CSVWriter csvwriter = new CSVWriter(sw);
					csvwriter.writeAll(resultSet, true);
					contents = sw.toString();
					System.out.println(contents);
					try {
						hasRows = resultSet.last();
					} catch (Exception e) {
						// another way to determine if there are rows
						hasRows = contents.split(CSVWriter.DEFAULT_LINE_END).length > 1;
					}
					// if no rows, return nothing
					if (!hasRows) {
						contents = null;
					}
				}
			} finally {
				if (resultSet != null) {
					resultSet.close();
				}
				// clean up
				// keep it cached for performance
				//dataManager.dropTables(dataPackage);
			}
		}
		
		return contents;
	}
	
	/**
	 * Filters the Annotations to only include those that match the given [data] criteria.
	 * Annotations that do match will have [CSV] data as the value in the returned map.
	 * @see selectData() for more information on the returned data
	 * @param annotations
	 * @param criteria
	 * @return
	 * @throws Exception 
	 */
	public String filterDataMatches(Map<Annotation, String> annotations, Criteria criteria) throws Exception {
		
		String data = null;
		
		// add matches to the list
		Map<Annotation, String> annotationsCopy = new HashMap<Annotation, String>(annotations);
		
		if (criteria.isGroup()) {
			// iterate through the subcriteria
		   if (criteria.getSubCriteria() != null) {
			   StringBuffer sb = new StringBuffer();
			   Iterator<Criteria> iter = criteria.getSubCriteria().iterator();
			   while (iter.hasNext()) {
				   Criteria subcriteria = iter.next();
				   String temp = filterDataMatches(annotations, subcriteria);
				   if (temp != null) {
					   sb.append(temp);
				   }
			   }
			   if (sb.length() > 0) {
				   data = sb.toString();
			   }
		   }
		} else {
		
			// what criteria were given?
			Class type = criteria.getType();
			OntologyClass subject = criteria.getSubject();
			String operator = criteria.getCondition();
			Object value = criteria.getValue();
			
			// find the Characteristic we are filtering data values for
			List<OntologyClass> characteristics = new ArrayList<OntologyClass>();
			if (type != null && type.equals(Characteristic.class)) {
				characteristics.add(subject);
				// add subclasses
				List<OntologyClass> subclasses = SMS.getInstance().getOntologyManager().getNamedSubclasses(subject, true);
				characteristics.addAll(subclasses);
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
			if (!characteristics.isEmpty() && value != null && operator != null) {
				
				// look for each possible Measurement match to compare data
				for (Annotation annotation: annotationsCopy.keySet()) {
					
					String dataContent = null;
					try {
						dataContent = selectData(annotation, characteristics, operator, value);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					// set the data content for the annotation
					if (dataContent != null) {
						annotations.put(annotation, dataContent);
					} else {
						// if the data did not match, remove the entry
						annotations.remove(annotation);
					}
				}
				// integrate what we can for this set of matches
				List<Annotation> a = new ArrayList<Annotation>(annotations.keySet());
				try {
					data = unionData(a, characteristics, operator, value);
				} catch (Exception e) {
					// this is bad, but not the end of the world
					e.printStackTrace();
				}
				
			}
		}
				
		return data;
	}

	/**
	 * Retrieves tabular data of UNIONED data that matches the given condition for the Annotations/Characteristics
	 * The results are in CSV format or null if no records/data is found to match the condition
	 * @param annotations
	 * @param characteristics
	 * @param operator
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private String unionData(
			List<Annotation> annotations, 
			List<OntologyClass> characteristics, 
			String operator, 
			Object value) throws Exception {
		
		ResultSet resultSet = null;
		String contents = null;
		boolean hasRows = false;
		
		List<DataPackage> dataPackages = new ArrayList<DataPackage>();
		Union union = new Union();
		
		List<OntologyClass> commonCharacteristics = getCommonCharacteristics(annotations);
		
		for (Annotation annotation: annotations) {
			String dataPackageId = annotation.getDataPackage();
			
			// find the first Measurement in the annotation for this Characteristic
			List<Measurement> measurements = annotation.getMeasurements(characteristics);
			// TODO handle each measurement that contains these characteristics
			Measurement measurement = measurements.get(0);
			// for selecting the correct attribute from the correct entity
			String dataObject = measurement.getMapping().getDataObject();
			int dataObjectIndex = Integer.parseInt(dataObject);
			String attributeName = measurement.getMapping().getAttribute();
		
			// First create the DataPackage object that will be used in the query.
			// TODO: get the metadata directly from metacat store?
			DocumentDownloadUtil ddu = new DocumentDownloadUtil();
			InputStream inputStream = ddu.downloadDocument(dataPackageId, endPoint);
			DataPackage dataPackage = dataManager.parseMetadata(inputStream);
			// do this conditionally instead of every time?
			boolean success = dataManager.loadDataToDB(dataPackage, endPoint);
			Entity entity = dataPackage.getEntityList()[dataObjectIndex];
			Attribute[] attributes = entity.getAttributeList().getAttributes();
			// find the index of the attribute based on name (id is not required)
			List<Attribute> annotatedAttributes = new ArrayList<Attribute>();
			int attributeIndex = 0;
			int i = 0;
			for (Attribute a: attributes) {
				// the one we are using for the condition?
				if (a.getName().equals(attributeName)) {
					attributeIndex = i;
				}
				i++;
			}
			Attribute attribute = attributes[attributeIndex];
			
			// build the commonly annotated attributes
			for (OntologyClass cc: commonCharacteristics) {
				List<Measurement> commonMeasurements = annotation.getMeasurements(Arrays.asList(cc));
				for (Measurement cm: commonMeasurements) {
					String commonAttributeName = cm.getMapping().getAttribute();
					for (Attribute a: attributes) {
						// the one we are using for the condition?
						if (a.getName().equals(commonAttributeName)) {
							annotatedAttributes.add(a);
						}
					}
				}
			}
			
			//Now build a query, execute it, and see what we got
			if (success && dataPackage != null) {
				dataPackages.add(dataPackage);
				Query query = new Query();
				/* SELECT clause */
				// add an item for what datapackage it came from
				SelectionItem staticItem = new StaticSelectionItem("dataPacakgeId", dataPackageId);
				query.addSelectionItem(staticItem);
				// select the common attributes
				for (Attribute a: annotatedAttributes) {
					SelectionItem selectionItem = new SelectionItem(entity, a);
					query.addSelectionItem(selectionItem);
				}
				
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
				union.addQuery(query);
			}
		}
			
		try {
			System.out.println("Query SQL = " + union.toSQLString());
			resultSet = dataManager.selectData(union, dataPackages.toArray(new DataPackage[0]));
			if (resultSet != null) {
				StringWriter sw = new StringWriter();
				CSVWriter csvwriter = new CSVWriter(sw);
				csvwriter.writeAll(resultSet, true);
				contents = sw.toString();
				System.out.println(contents);
				try {
					hasRows = resultSet.last();
				} catch (Exception e) {
					// another way to determine if there are rows
					hasRows = contents.split(CSVWriter.DEFAULT_LINE_END).length > 1;
				}
				// if no rows, return nothing
				if (!hasRows) {
					contents = null;
				}
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
		}
				
		return contents;
	}

	private List<OntologyClass> getCommonCharacteristics(List<Annotation> annotations) {
		
		List<OntologyClass> commonCharacteristics = new ArrayList<OntologyClass>();

		// gather every mapped characteristic in the annotations
		List<OntologyClass> characteristics = new ArrayList<OntologyClass>();
		for (Annotation annotation: annotations) {
			List<Mapping> mappings = annotation.getMappings();
			for (Mapping mapping: mappings) {
				characteristics.addAll(mapping.getMeasurement().getCharacteristics());				
			}
		}
		
		// for each characteristic, check that it exists in every annotation
		for (OntologyClass characteristic: characteristics) {
			int charCount = 0;
			for (Annotation annotation: annotations) {
				boolean containsCharacteristic = false;
				List<Mapping> mappings = annotation.getMappings();
				for (Mapping mapping: mappings) {
					containsCharacteristic = containsCharacteristic || mapping.getMeasurement().getCharacteristics().contains(characteristic);
					if (containsCharacteristic) {
						break;
					} 
//					else {
//						// check subclasses
//						List<OntologyClass> subclasses = SMS.getInstance().getOntologyManager().getNamedSubclasses(characteristic, true);
//						for (OntologyClass subChar: subclasses) {
//							containsCharacteristic = mapping.getMeasurement().getCharacteristics().contains(subChar);
//							if (containsCharacteristic) {
//								charCount++;
//								break;
//							}
//						}
//					}
				}	
				if (containsCharacteristic) {
					charCount++;
				}
			}
			// we found it in every annotation
			if (charCount == annotations.size()) {
				if (!commonCharacteristics.contains(characteristic)) {
					commonCharacteristics.add(characteristic);
				}
			}
		}
		
		// now we know what characteristics are common across the entire list of Annotations
		return commonCharacteristics;

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			ConfigurableEcogridEndPoint endPoint = new ConfigurableEcogridEndPoint();
			endPoint.setSessionId("usePublic");
			
			// get the annotation and measurement to check
			String annotationId = "benriver.269.6";
			DocumentDownloadUtil ddu = new DocumentDownloadUtil();
			InputStream annotationInputStream = ddu.downloadDocument(annotationId, endPoint);
			Annotation annotation = Annotation.read(annotationInputStream);
			Measurement measurement = annotation.getMeasurement("m4");
			List characteristics = measurement.getCharacteristics();
			
			// check for values matching condition
			Materializer.getInstance(LOCAL_CONFIGURATION).unionData(
					Arrays.asList(new Annotation[]{annotation}), 
					characteristics, "=", 9.4);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
