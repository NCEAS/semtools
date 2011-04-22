package org.ecoinformatics.sms.annotation.search.data;

import java.io.File;
import java.io.FileOutputStream;
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
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
				SelectionItem staticItem = new StaticSelectionItem("dataPackageId", dataPackageId);
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
			System.out.println("Union query SQL = " + union.toSQLString());
			resultSet = dataManager.selectData(union, dataPackages.toArray(new DataPackage[0]));
			if (resultSet != null) {
				StringWriter sw = new StringWriter();
				CSVWriter csvwriter = new CSVWriter(sw);
				csvwriter.writeAll(resultSet, true);
				csvwriter.flush();
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
			String annotationId = "benriver.302.5";
			DocumentDownloadUtil ddu = new DocumentDownloadUtil();
			InputStream annotationInputStream = ddu.downloadDocument(annotationId, endPoint);
			Annotation annotation = Annotation.read(annotationInputStream);

			// get the triples
			Materializer.getInstance(LOCAL_CONFIGURATION).getTriples(annotation);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public File getTriples(Annotation annotation) throws Exception {
		String dataPackageId = annotation.getDataPackage();
		// First create the DataPackage object that will be used in the query.
		// TODO: get the metadata directly from metacat store?
		DocumentDownloadUtil ddu = new DocumentDownloadUtil();
		InputStream inputStream = ddu.downloadDocument(dataPackageId, endPoint);
		DataPackage dataPackage = dataManager.parseMetadata(inputStream);
		// do this conditionally instead of every time?
		boolean success = dataManager.loadDataToDB(dataPackage, endPoint);
		
		String oboeBase = "http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl";
		String base = "test://my.ontology.instances";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology(IRI.create(base));

        // import the referenced ontologies
        for (Ontology o: annotation.getOntologies().values()) {
	        OWLImportsDeclaration importDeclaration = dataFactory.getOWLImportsDeclaration(IRI.create(o.getURI()));
	        OWLOntologyChange importAxiom = new AddImport(ontology, importDeclaration);
	        manager.applyChange(importAxiom);
        }
        
		// iterate over observations
		List<Observation> observations = annotation.getObservations();
		int obsCount = 0;
		int measurementCount = 0;
		for (Observation observation: observations) {
			// observation
			OWLIndividual observationIndividual = dataFactory.getOWLNamedIndividual(IRI.create(base + "#observation_" + obsCount));
			OWLClass owlObservation = dataFactory.getOWLClass(IRI.create(oboeBase + "#Observation"));
			OWLClassAssertionAxiom observationAssertion = dataFactory.getOWLClassAssertionAxiom(owlObservation, observationIndividual);
            manager.addAxiom(ontology, observationAssertion);
            
			// entity
			OWLIndividual entityIndividual = dataFactory.getOWLNamedIndividual(IRI.create(base + "#entity_" + obsCount));
			OWLClass owlEntity = dataFactory.getOWLClass(IRI.create(observation.getEntity().getURI()));
			OWLClassAssertionAxiom entityAssertion = dataFactory.getOWLClassAssertionAxiom(owlEntity, entityIndividual);
            manager.addAxiom(ontology, entityAssertion);
            
            // connect entity to observation
            OWLObjectProperty ofEntity = dataFactory.getOWLObjectProperty(IRI.create(oboeBase + "#ofEntity"));
            OWLObjectPropertyAssertionAxiom ofEntityAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(ofEntity, observationIndividual, entityIndividual);
            AddAxiom ofEntityAxiomChange = new AddAxiom(ontology, ofEntityAssertion);
            manager.applyChange(ofEntityAxiomChange);
            
            // TODO: context relationships here
			
            // measurements
			List<Measurement> measurements = observation.getMeasurements();
			for (Measurement measurement: measurements) {

				// for selecting the correct attribute from the correct entity
				String dataObject = measurement.getMapping().getDataObject();
				int dataObjectIndex = Integer.parseInt(dataObject);
				String attributeName = measurement.getMapping().getAttribute();

				Entity entity = dataPackage.getEntityList()[dataObjectIndex];
				Attribute[] attributes = entity.getAttributeList().getAttributes();
				// find the index of the attribute based on name (id is not required)
				List<Attribute> annotatedAttributes = new ArrayList<Attribute>();
				for (Attribute a: attributes) {
					if (a.getName().equals(attributeName)) {
						// for this measurement?
						annotatedAttributes.add(a);
					}
				}
				
				//Now build a data query, execute it, and see what we got
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
					/* WHERE clause with condition could go here */
					System.out.println("Query SQL = " + query.toSQLString());

					ResultSet resultSet = null;
					try {
						resultSet = dataManager.selectData(query, dataPackages);
						if (resultSet != null) {
							int columnCount = resultSet.getMetaData().getColumnCount();
							while (resultSet.next()) {
								// get the value for this row to make an instance
								for (int i=0; i < columnCount; i++) {
									Object value = resultSet.getObject(i+1);
									String valueString = null;
									if (value != null) {
										valueString  = value.toString();
									}
									
									// measurement
									OWLIndividual measurementIndividual = dataFactory.getOWLNamedIndividual(IRI.create(base + "#measurement_" + obsCount + "_" + measurementCount));
									OntologyClass template = measurement.getTemplate();
						            OWLClass owlMeasurement = null;
						            // TODO: handle templates?
					    	        owlMeasurement = dataFactory.getOWLClass(IRI.create(Annotation.OBOE_CLASSES.get(Measurement.class).getURI()));
									OWLClassAssertionAxiom measurementAssertion = dataFactory.getOWLClassAssertionAxiom(owlMeasurement, measurementIndividual);
						            manager.addAxiom(ontology, measurementAssertion);
									
									// measurement value individual
									OWLIndividual valueIndividual = dataFactory.getOWLNamedIndividual(IRI.create(base + "#value_" + obsCount + "_" + measurementCount));
									OWLClass owlPrimativeValue = dataFactory.getOWLClass(IRI.create(oboeBase + "#PrimitiveValue"));
									OWLClassAssertionAxiom valueAssertion = dataFactory.getOWLClassAssertionAxiom(owlPrimativeValue, valueIndividual);
									manager.addAxiom(ontology, valueAssertion);

									// add the hasCode <value> data property
									// TODO: better object typing
									if (value != null) {
							            OWLDataProperty hasCode = dataFactory.getOWLDataProperty(IRI.create(oboeBase + "#hasCode"));
							            OWLDataPropertyAssertionAxiom hasCodeAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(hasCode, valueIndividual, valueString);
							            AddAxiom hasCodeAxiomChange = new AddAxiom(ontology, hasCodeAssertion);
							            manager.applyChange(hasCodeAxiomChange);
									}
						            
						            // add the hasValue <PrimativeValue> object property
						            OWLObjectProperty hasValue = dataFactory.getOWLObjectProperty(IRI.create(oboeBase + "#hasValue"));
						            OWLObjectPropertyAssertionAxiom hasValueAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(hasValue, measurementIndividual, valueIndividual);
						            AddAxiom hasValueAxiomChange = new AddAxiom(ontology, hasValueAssertion);
						            manager.applyChange(hasValueAxiomChange);
						            
						            // add the measurementFor <Observation> property
						            // TODO: attach measurement to the correct Observation instance ("is key"/"is identifying")
						            // TODO: need to inspect the context and the data values to determine if we need another Observation instance for this measurement
						            OWLObjectProperty measurementFor = dataFactory.getOWLObjectProperty(IRI.create(oboeBase + "#measurementFor"));
						            OWLObjectPropertyAssertionAxiom measurementForAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(measurementFor, measurementIndividual, observationIndividual);
						            AddAxiom measurementForAxiomChange = new AddAxiom(ontology, measurementForAssertion);
						            manager.applyChange(measurementForAxiomChange);
						            
						            // add the Characteristic, Standard and Protocol here
						            // handle characteristics
									for (Characteristic characteristic: measurement.getCharacteristics()) {
							            OWLObjectProperty ofCharacteristic = dataFactory.getOWLObjectProperty(IRI.create(oboeBase + "#ofCharacteristic"));
										OWLClass owlCharacteristic = dataFactory.getOWLClass(IRI.create(characteristic.getURI()));
										OWLObjectSomeValuesFrom someFromCharacteristic = dataFactory.getOWLObjectSomeValuesFrom(ofCharacteristic, owlCharacteristic);
										OWLClassAssertionAxiom ofCharacteristicAssertion = dataFactory.getOWLClassAssertionAxiom(someFromCharacteristic, measurementIndividual);
										AddAxiom ofCharacteristicAxiomChange = new AddAxiom(ontology, ofCharacteristicAssertion);
							            manager.applyChange(ofCharacteristicAxiomChange);
									}
									// standard
									Standard standard = measurement.getStandard();
									if (standard != null) {
							            OWLObjectProperty usesStandard = dataFactory.getOWLObjectProperty(IRI.create(oboeBase + "#usesStandard"));
										OWLClass owlStandard = dataFactory.getOWLClass(IRI.create(standard.getURI()));
										OWLObjectSomeValuesFrom someFromStandard = dataFactory.getOWLObjectSomeValuesFrom(usesStandard, owlStandard);
										OWLClassAssertionAxiom usesStandardAssertion = dataFactory.getOWLClassAssertionAxiom(someFromStandard, measurementIndividual);
										AddAxiom usesStandardAxiomChange = new AddAxiom(ontology, usesStandardAssertion);
							            manager.applyChange(usesStandardAxiomChange);
									}
						            // protocol
						            Protocol protocol = measurement.getProtocol();
						            if (protocol != null) {
						            	OWLObjectProperty usesProtocol = dataFactory.getOWLObjectProperty(IRI.create(oboeBase + "#usesProtocol"));
										OWLClass owlProtocol = dataFactory.getOWLClass(IRI.create(protocol.getURI()));
										OWLObjectSomeValuesFrom someFromProtocol = dataFactory.getOWLObjectSomeValuesFrom(usesProtocol, owlProtocol);
										OWLClassAssertionAxiom usesProtocolAssertion = dataFactory.getOWLClassAssertionAxiom(someFromProtocol, measurementIndividual);
										AddAxiom usesProtocolAxiomChange = new AddAxiom(ontology, usesProtocolAssertion);
							            manager.applyChange(usesProtocolAxiomChange);
						            }
								}
								measurementCount++;
								//just one row
								//break;
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
			}
			obsCount++;
		}
		File file = new File(annotation.getURI() + "-instances.owl");
		FileOutputStream fos = new FileOutputStream(file);
		
		OWLOntologyFormat format = new RDFXMLOntologyFormat();
		manager.saveOntology(ontology, format, fos);
		
		return file;
	}
}
