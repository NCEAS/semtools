package org.ecoinformatics.sms.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.ontology.OntologyClass;

import edu.ucsb.nceas.metacat.client.Metacat;
import edu.ucsb.nceas.metacat.client.MetacatFactory;

public class AnnotationFactory {

	private static String EML_TEMPLATE = "emlTemplate.xml";
	//private static String METACAT_URL = "http://fred.msi.ucsb.edu:8080/knb/metacat";
	private static String METACAT_URL = "http://localhost:8080/knb/metacat";
	private static String METACAT_USERNAME = "uid=kepler,o=unaffiliated,dc=ecoinformatics,dc=org";
	private static String METACAT_PASSWORD = "kepler";
	private static String DEFAULT_ONTOLOGY = "https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl";
	
	private static AnnotationFactory instance;
	
	public static AnnotationFactory getInstance() {
		if (instance == null) {
			instance = new AnnotationFactory();
		}
		return instance;
	}
	
	public static void setUp() throws Exception {
		SMS.getInstance().getOntologyManager().importOntology(DEFAULT_ONTOLOGY);
	}
	
	public List<Annotation> generateAnnotations(int count, String[] attributes) throws Exception {
		List<Annotation> annotations = new ArrayList<Annotation>();
		
		// get seed data
		List<OntologyClass> entities = SMS.getInstance().getOntologyManager().getNamedSubclasses(Annotation.OBOE_CLASSES.get(Entity.class), true);
		List<OntologyClass> characteristics = SMS.getInstance().getOntologyManager().getNamedSubclasses(Annotation.OBOE_CLASSES.get(Characteristic.class), true);
		List<OntologyClass> standards = SMS.getInstance().getOntologyManager().getNamedSubclasses(Annotation.OBOE_CLASSES.get(Standard.class), true);
		List<OntologyClass> protocols = SMS.getInstance().getOntologyManager().getNamedSubclasses(Annotation.OBOE_CLASSES.get(Protocol.class), true);

		OntologyClass randomClass = null;
		Entity entity = null;
		Characteristic characteristic = null;
		Standard standard = null;
		Protocol protocol = null;
		
		// the id seeds
		String scope = String.valueOf(System.currentTimeMillis());
		int id = 1;
		String dataTable = "0";
		
		// iterate
		for (int i = 0; i < count; i++) {
			Annotation annotation = new Annotation();
			String emlPackageId = scope + "." + id++ + ".1";
			String uri = scope + "." + id++ + ".1";
			annotation.setEMLPackage(emlPackageId);
			annotation.setURI(uri);
			annotation.setDataTable(dataTable);
	
			// loop attributes - one observation per column
			// TODO: randomize the grouping of multiple measurements in single observation
			for (int j = 0; j < attributes.length; j++) {
				
				// handle this attribute
				String attribute = attributes[j];
				
				// select an entity using some tree traversal
				randomClass = null;
				if (entity != null) {
					// select a random subclass if they exist
					List<OntologyClass> subclasses = SMS.getInstance().getOntologyManager().getNamedSubclasses(entity, false);
					if (subclasses != null && !subclasses.isEmpty()) {
						randomClass = subclasses.get((int)Math.floor(Math.random() * subclasses.size()));
					}
				}
				if (randomClass == null) {
					// just get a random class
					randomClass = entities.get((int)Math.floor(Math.random() * entities.size()));
				}
				entity = new Entity(randomClass.getURI());

				// observation for the entity
				Observation o = new Observation();
				o.setEntity(entity );
				o.setLabel("o" + j);
				
				// measurement for the attribute
				Measurement measurement = new Measurement();
				measurement.setLabel("m" + j);
				
				// characteristic
				randomClass = null;
				if (characteristic != null) {
					// select a random subclass if they exist
					List<OntologyClass> subclasses = SMS.getInstance().getOntologyManager().getNamedSubclasses(characteristic, false);
					if (subclasses != null && !subclasses.isEmpty()) {
						randomClass = subclasses.get((int)Math.floor(Math.random() * subclasses.size()));
					}
				}
				if (randomClass == null) {
					// just get a random class
					randomClass = characteristics.get((int)Math.floor(Math.random() * characteristics.size()));
				}
				characteristic = new Characteristic(randomClass.getURI());
				
				// standard
				randomClass = null;
				if (standard != null) {
					// select a random subclass if they exist
					List<OntologyClass> subclasses = SMS.getInstance().getOntologyManager().getNamedSubclasses(standard, false);
					if (subclasses != null && !subclasses.isEmpty()) {
						randomClass = subclasses.get((int)Math.floor(Math.random() * subclasses.size()));
					}
				}
				if (randomClass == null) {
					// just get a random class
					randomClass = standards.get((int)Math.floor(Math.random() * standards.size()));
				}
				standard = new Standard(randomClass.getURI());
				
				// protocol
				randomClass = null;
				if (protocol != null) {
					// select a random subclass if they exist
					List<OntologyClass> subclasses = SMS.getInstance().getOntologyManager().getNamedSubclasses(protocol, false);
					if (subclasses != null && !subclasses.isEmpty()) {
						randomClass = subclasses.get((int)Math.floor(Math.random() * subclasses.size()));
					}
				}
				if (randomClass == null) {
					// just get a random class
					randomClass = protocols.get((int)Math.floor(Math.random() * protocols.size()));
				}
				protocol = new Protocol(randomClass.getURI());
				
				measurement.addCharacteristic(characteristic);
				measurement.setStandard(standard);
				measurement.setProtocol(protocol);
				o.addMeasurement(measurement );

				// mapping
				Mapping mapping = new Mapping();
				mapping.setMeasurement(measurement);
				mapping.setAttribute(attribute);
				
				annotation.addMapping(mapping);				
				annotation.addObservation(o);
				
				// add the ontologies used
				annotation.addOntology(entity.getOntology());
				annotation.addOntology(characteristic.getOntology());
				annotation.addOntology(standard.getOntology());
				annotation.addOntology(protocol.getOntology());
			}
			
			annotations.add(annotation);
			
		}
		
		return annotations;
	}
	
	public void upload(
			List<Annotation> annotations, 
			String emlTemplate, 
			String metacatUrl, 
			String username, 
			String password) throws Exception {
		
		// set up metacat connection
		Metacat metacat = MetacatFactory.createMetacatConnection(metacatUrl);
		metacat.login(username, password);
		
		String docid = null;
		Reader xmlReader = null;
		for (Annotation annotation: annotations) {
			
			// EML 
			docid = annotation.getEMLPackage();
			// the title
			StringBuffer title = new StringBuffer("Data Package for: ");
			title.append(annotation.getObservations().get(0).getEntity().getName());
			title.append(", ");
			title.append(annotation.getObservations().get(0).getMeasurements().get(0).getCharacteristics().get(0).getName());
			title.append(", ");
			title.append(annotation.getObservations().get(0).getMeasurements().get(0).getStandard().getName());
			title.append(", ");
			title.append(annotation.getObservations().get(0).getMeasurements().get(0).getProtocol().getName());
			// read the eml content
			BufferedReader r = 
				new BufferedReader(
						new InputStreamReader(
								this.getClass().getResourceAsStream(emlTemplate)));
			StringBuffer sb = new StringBuffer();
			String line = r.readLine();
			while (line != null) {
				sb.append(line);
				line = r.readLine();
			}
			String emlContent = sb.toString();
			emlContent = emlContent.replaceAll("_PACKAGEID_", docid);
			emlContent = emlContent.replaceAll("_TITLE_", title.toString());
			
			System.out.println(emlContent);
			if (false) // for testing
				continue;
			
			// upload the eml
			xmlReader = new StringReader(emlContent);
			metacat.insert(docid, xmlReader, null);
			metacat.setAccess(docid, "public", "4", "allow", "allowFirst");
			
			// upload the annotation
			docid = annotation.getURI();
			xmlReader = new StringReader(annotation.toString());
			metacat.insert(docid, xmlReader, null);
			metacat.setAccess(docid, "public", "4", "allow", "allowFirst");
		}
		
		metacat.logout();

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			setUp();
			List<Annotation> annotations = AnnotationFactory.getInstance().generateAnnotations(5, new String[] {"test"});
			for (Annotation annotation: annotations) {
				System.out.println(annotation);
			}
			// do the upload
			AnnotationFactory.getInstance().upload(annotations, EML_TEMPLATE, METACAT_URL, METACAT_USERNAME, METACAT_PASSWORD);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
