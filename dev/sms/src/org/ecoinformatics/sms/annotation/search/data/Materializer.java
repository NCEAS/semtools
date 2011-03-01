package org.ecoinformatics.sms.annotation.search.data;

import java.io.InputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.ontology.OntologyClass;

import au.com.bytecode.opencsv.CSVWriter;

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
	public String selectData(Annotation annotation, Measurement measurement, String operator, Object value) throws Exception {
		
		String dataPackageId = annotation.getDataPackage();
		
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
	 */
	public Map<Annotation, String> filterDataMatches(Map<Annotation, String> annotations, Criteria criteria) {
		
		// add matches to the list
		Map<Annotation, String> matches = new HashMap<Annotation, String>(annotations);
		
		
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
				if (characteristics.isEmpty() || value == null || operator == null) {
					return matches;
				}	
				
				// look for each possible Measurement match to compare data
				for (Annotation annotation: annotations.keySet()) {
					int dataMatchCount = 0;
					List<Measurement> measurements = annotation.getMeasurements(characteristics);
					StringBuffer annotationData = new StringBuffer();
					for (Measurement measurement: measurements) {
						String dataContent = null;
						try {
							dataContent = selectData(annotation, measurement, operator, value);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (dataContent != null) {
							annotationData.append(dataContent);
							dataMatchCount++;
						}
					}
					
					// set the data content for the annotation
					if (dataMatchCount > 0) {
						matches.put(annotation, annotationData.toString());
					} else {
						// if the data did not match, remove the entry
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
			Materializer.getInstance().selectData(annotation, measurement, ">", 35);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
