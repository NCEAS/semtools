package edu.ucsb.nceas.metacat.plugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.annotation.search.CriteriaReader;
import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.ontology.bioportal.OntologyBean;
import org.ecoinformatics.sms.ontology.bioportal.OntologyService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.metacat.DBQuery;
import edu.ucsb.nceas.metacat.DBTransform;
import edu.ucsb.nceas.metacat.DBUtil;
import edu.ucsb.nceas.metacat.DocumentIdQuery;
import edu.ucsb.nceas.metacat.DocumentImpl;
import edu.ucsb.nceas.metacat.QuerySpecification;
import edu.ucsb.nceas.metacat.client.InsufficientKarmaException;
import edu.ucsb.nceas.metacat.client.Metacat;
import edu.ucsb.nceas.metacat.client.MetacatFactory;
import edu.ucsb.nceas.metacat.properties.PropertyService;
import edu.ucsb.nceas.metacat.shared.HandlerException;
import edu.ucsb.nceas.metacat.util.MetacatUtil;
import edu.ucsb.nceas.metacat.util.SystemUtil;
import edu.ucsb.nceas.utilities.FileUtil;
import edu.ucsb.nceas.utilities.XMLUtilities;

public class SemtoolsPlugin implements MetacatHandlerPlugin {

	private static List<String> supportedActions = new ArrayList<String>();
	
	private static boolean readAll = true;
	
	public static Log log = LogFactory.getLog(SemtoolsPlugin.class);
	
	static {
		//note: order matters - it drives the switch/case handling
		supportedActions.add("semquery");
		supportedActions.add("initialize");
		supportedActions.add("registerontology");
		supportedActions.add("unregisterontology");
		supportedActions.add("ontologies");
		supportedActions.add("getactivedomain");

		initializeOntologies();
		initializeAnnotations();
	}
	
	/**
	 * Look up annotations stored on this Metacat instance
	 */
	private static void initializeAnnotations() {
		try {
			String metacatURL = SystemUtil.getServletURL();
			Metacat mc = MetacatFactory.createMetacatConnection(metacatURL);
			DBUtil dbutil = new DBUtil();
			Vector<String> annotationDocids = dbutil.getAllDocidsByType(Annotation.ANNOTATION_NS, false);
			for (String annotationDocid: annotationDocids) {
				try {
					InputStream in = null;
					String documentPath = null;
					if (readAll) {
						// circumvents permission checking by reading directly from Metacat
						DocumentImpl docImpl = new DocumentImpl(annotationDocid);
						String docString = docImpl.toString();
						in = new ByteArrayInputStream(docString.getBytes());
						String documentDir = PropertyService.getProperty("application.documentfilepath");
						documentPath = documentDir + FileUtil.getFS() + annotationDocid;
						documentPath = new File(documentPath).toURI().toURL().toString();
					} else {						
						try {
							// uses the client and only allows public read
							in = mc.read(annotationDocid);
							documentPath = metacatURL + "/" + annotationDocid;
						} catch (InsufficientKarmaException ike) {
							// public read permission is not granted
							log.warn(ike.getMessage());
							continue;
						} 
					}
					// get the annotation and import it
					Annotation annotation = Annotation.read(in);
					if (!SMS.getInstance().getAnnotationManager().isAnnotation(annotation.getURI())) {
						SMS.getInstance().getAnnotationManager().importAnnotation(annotation, documentPath);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					//e.printStackTrace();
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private static void initializeOntologies() {
		// clear the ontologies that are loaded
		for (String uri:SMS.getInstance().getOntologyManager().getOntologyIds()) {
			if (SMS.getInstance().getOntologyManager().isOntology(uri)) {
				SMS.getInstance().getOntologyManager().removeOntology(uri);
			}
		}
		
		// TODO: load them from BioPortal
		// load the configured ontologies
		Hashtable<String, String> ontologyURIs = new Hashtable<String, String>();
		ontologyURIs.put(
				"http://ecoinformatics.org/oboe/oboe.1.0/oboe.owl", 
				"http://ecoinformatics.org/oboe/oboe.1.0/oboe.owl");
		ontologyURIs.put(
				"https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl", 
				"https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl");
		ontologyURIs.put(
				"https://code.ecoinformatics.org/code/sonet/trunk/ontologies/oboe-trait.owl", 
				"https://code.ecoinformatics.org/code/sonet/trunk/ontologies/oboe-trait.owl");
		
		// map them first
		try {
			SMS.getInstance().getOntologyManager().mapOntologies(ontologyURIs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}
		// load them in the manager
		for (Entry<String, String> entry: ontologyURIs.entrySet()) {
			String uri = entry.getKey();
			String url = entry.getValue();
			try {
				SMS.getInstance().getOntologyManager().importOntology(url, uri);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
	}
	
	/**
	 * delegates to the appropriate handling method
	 */
	public boolean handleAction(String action,
			Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId) throws HandlerException {

		switch (supportedActions.indexOf(action.toLowerCase())) {
		case 0:
			semquery(params, request, response, username, groups, sessionId);
			break;
		case 1:
			initializeOntologies();
			initializeAnnotations();
			break;
		case 2:
			registerOntology(params, request, response, username, groups, sessionId);
			break;
		case 3:
			unregisterOntology(params, request, response, username, groups, sessionId);
			break;
		case 4:
			ontologies(params, request, response, username, groups, sessionId);
			break;
		case 5:
			getActiveDomain(params, request, response, username, groups, sessionId);
			break;	
		default:
			break;
		}
		
		return true;
	}

	private void registerOntology(Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId) throws HandlerException {
		
		StringBuffer sb = new StringBuffer();
		String id = null;
		String uri = null;
		String url = null;
		try {
			if (params.containsKey("id")) {
				id = params.get("id")[0];
				OntologyBean ontologyBean = OntologyService.getInstance().getOntologyBean(id);
				uri = ontologyBean.getUrn();
				url = OntologyService.getInstance().getOntology(id);
			} else {
				uri = params.get("uri")[0];
				url = params.get("url")[0];
			}
			Hashtable<String, String> ontologyURIs = new Hashtable<String, String>();
			ontologyURIs.put(uri, url);
			SMS.getInstance().getOntologyManager().mapOntologies(ontologyURIs);
			SMS.getInstance().getOntologyManager().importOntology(url, uri);
			
			sb.append("<success>");
	        sb.append("<action>registerOntology</action>");
	        sb.append("<param name='uri'>" + uri + "</param>");
	        sb.append("<param name='url'>" + url + "</param>");
	        sb.append("</success>");
		} catch (Exception e) {
			e.printStackTrace();
			sb.append("<error>");
	        sb.append("<message>" + e.getMessage() + "</message>");
	        sb.append("<action>registerOntology</action>");
	        sb.append("<param name='uri'>" + uri + "</param>");
	        sb.append("<param name='url'>" + url + "</param>");
	        sb.append("</error>");
		}
        
        try {
        	response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            out.println(sb.toString());
            out.close();
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
		}
	}
	
	private void unregisterOntology(Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId) throws HandlerException {
		
		StringBuffer sb = new StringBuffer();
		String id = null;
		String uri = null;
		try {
			if (params.containsKey("id")) {
				id = params.get("id")[0];
				OntologyBean ontologyBean = OntologyService.getInstance().getOntologyBean(id);
				uri = ontologyBean.getUrn();
			} else {
				uri = params.get("uri")[0];
			}
			SMS.getInstance().getOntologyManager().removeOntology(uri);
			
			sb.append("<success>");
	        sb.append("<action>unregisterOntology</action>");
	        sb.append("<param name='uri'>" + uri + "</param>");
	        sb.append("</success>");
		} catch (Exception e) {
			e.printStackTrace();
			sb.append("<error>");
	        sb.append("<message>" + e.getMessage() + "</message>");
	        sb.append("<action>unregisterOntology</action>");
	        sb.append("<param name='uri'>" + uri + "</param>");
	        sb.append("</error>");
		}
        
        try {
        	response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            out.println(sb.toString());
            out.close();
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
		}
		
	}
	
	private void ontologies(Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId) throws HandlerException {
		
		StringBuffer sb = new StringBuffer();

		try {
			List<String> ontologies = SMS.getInstance().getOntologyManager().getOntologyIds();
			
			sb.append("<success>");
	        sb.append("<action>ontologies</action>");
			for (String uri: ontologies) {
				Ontology ontology = SMS.getInstance().getOntologyManager().getOntology(uri);
				String label = SMS.getInstance().getOntologyManager().getOntologyLabel(ontology);
		        sb.append("<ontology>");
		        sb.append("<uri>" + uri + "</uri>");
		        sb.append("<label>" + label + "</label>");
		        sb.append("</ontology>");
			}
	        sb.append("</success>");
		} catch (Exception e) {
			e.printStackTrace();
			sb.append("<error>");
	        sb.append("<message>" + e.getMessage() + "</message>");
	        sb.append("<action>ontologies</action>");
	        sb.append("</error>");
		}
        
        try {
        	response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            out.println(sb.toString());
            out.close();
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
		}
	}
	
	private void semquery(Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId) throws HandlerException {
		try {
			// get the semantic query xml
			String semquery = params.get("query")[0];
			
			//parse it 
			Criteria criteria = CriteriaReader.read(semquery);
			
			List<Annotation> matches = SMS.getInstance().getAnnotationManager().getMatchingAnnotations(criteria);
			
			// look up annotation matches - for the data package ids
			Vector<String> docids = new Vector<String>();
			for (Annotation annotation: matches) {
				String docid = annotation.getEMLPackage();
				docid = docid.substring(0, docid.lastIndexOf("."));
				// check permissions here
				boolean readAccess = DocumentImpl.hasReadPermission(username, groups, docid);
				if (readAccess) {
					docids.add(docid);
				} else {
					log.warn("user: " + username + " does not have read permission for docid: " + docid);
				}
			}
			
			// HACKALERT: we don't have any matches, so we fake it out
			if (docids.isEmpty()) {
				docids.add("INVALID");
			}
			
			// just need a valid pathquery - this is not actually used
			String squery = DocumentIdQuery.createDocidQuery(docids.toArray(new String[0]));
			String[] queryArray = new String[1];
	        queryArray[0] = squery;
	        params.put("query", queryArray);
	        
	        // now perform an squery with the given docids
	        String[] actionArray = new String[1];
	        actionArray[0] = "squery";
	        params.put("action", actionArray);
	        
	        DBQuery dbQuery = new DBQuery(docids);
	        QuerySpecification qspec = 
	        	new QuerySpecification(
	        			squery, 
	        			PropertyService.getProperty("xml.saxparser"), 
	        			PropertyService.getProperty("document.accNumSeparator"));
	        // use null out so it does not print response yet
			//dbQuery.findDocuments(response, out, params, username, groups, sessionId);
	        StringBuffer results = dbQuery.createResultDocument(
	        		qspec.getNormalizedXMLQuery(), 
	        		qspec, 
	        		null, 
	        		username, 
	        		groups, 
	        		true);
	        
	        // augment the results with annotation information
	        String modifiedResults = augmentQuery(matches, results);
	        
	        // style the results
	        PrintWriter out = null;
	        String qformat = params.get("qformat")[0];
	        if (!qformat.equals(MetacatUtil.XMLFORMAT)) {
	        	// html
	        	response.setContentType("text/html");
		        out = response.getWriter();
		        // transform via stylesheet
		        DBTransform transform = new DBTransform();
		        transform.transformXMLDocument(
		        		modifiedResults, 
		        		"-//NCEAS//resultset//EN",
	                    "-//W3C//HTML//EN", 
	                    qformat, 
	                    out, 
	                    params,
	                    sessionId);
	        } else {
	        	// xml
	        	response.setContentType("text/xml");
		        out = response.getWriter();
		        // send to output as plain xml
	        	out.print(modifiedResults);
	        }
	        // done
	        out.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new HandlerException(e.getMessage());
		}
	}
	
	private void getActiveDomain(Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId) throws HandlerException {
		
		// which domain are we looking up
		String domainClass = params.get("class")[0];
		
		// harvest parameters
		String[] entityParam = params.get("entity");
		String[] characteristicParam = params.get("characteristic");
		String[] standardParam = params.get("standard");
		String[] protocolParam = params.get("protocol");
		String[] measurementParam = params.get("measurement");
		
		List<OntologyClass> activeEntities = new ArrayList<OntologyClass>();
		List<OntologyClass> activeCharacteristics = new ArrayList<OntologyClass>();
		List<OntologyClass> activeStandards = new ArrayList<OntologyClass>();
		List<OntologyClass> activeProtocols = new ArrayList<OntologyClass>();
		List<OntologyClass> activeMeasurements = new ArrayList<OntologyClass>();
		
		
		// Entity
		if (entityParam != null) {
			for (String classString: entityParam) {
				OntologyClass c = null;
				try {
					c = new OntologyClass(classString);
					activeEntities.add(c);
				} catch (Exception e) {
					log.error("Problem parsing parameter", e);
				}
			}
		}
		// Characteristic
		if (characteristicParam != null) {
			for (String classString: characteristicParam) {
				OntologyClass c = null;
				try {
					c = new OntologyClass(classString);
					activeCharacteristics.add(c);
				} catch (Exception e) {
					log.error("Problem parsing parameter", e);
				}
			}
		}
		// Standard
		if (standardParam != null) {
			for (String classString: standardParam) {
				OntologyClass c = null;
				try {
					c = new OntologyClass(classString);
					activeStandards.add(c);
				} catch (Exception e) {
					log.error("Problem parsing parameter", e);
				}
			}
		}
		// Protocol
		if (protocolParam != null) {
			for (String classString: protocolParam) {
				OntologyClass c = null;
				try {
					c = new OntologyClass(classString);
					activeProtocols.add(c);
				} catch (Exception e) {
					log.error("Problem parsing parameter", e);
				}
			}
		}
		// Measurement
		// TODO: implement measurement expansion
		
		String returnString = "";
		
		if (domainClass.equals(Entity.class.getName())) {
			// construct the selection fields for existing annotations
			// Entity
			StringBuffer entityOptions = new StringBuffer();
			List<OntologyClass> entities = SMS.getInstance().getAnnotationManager().getActiveEntities(activeCharacteristics, activeStandards);
			for (OntologyClass oc: entities) {
				entityOptions.append("<option ");
				entityOptions.append("title='");
				entityOptions.append(oc.getURI());
				entityOptions.append("' ");
				entityOptions.append("value='");
				entityOptions.append(oc.getURI());
				entityOptions.append("'");
				entityOptions.append(">");
				entityOptions.append(oc.getName());
				entityOptions.append("</option>");
			}
			returnString = entityOptions.toString();
		}
		if (domainClass.equals(Characteristic.class.getName())) {
			// Characteristic
			StringBuffer characteristicOptions = new StringBuffer();
			List<OntologyClass> characteristics = SMS.getInstance().getAnnotationManager().getActiveCharacteristics(activeEntities, activeStandards);
			for (OntologyClass oc: characteristics) {
				characteristicOptions.append("<option ");
				characteristicOptions.append("title='");
				characteristicOptions.append(oc.getURI());
				characteristicOptions.append("' ");
				characteristicOptions.append("value='");
				characteristicOptions.append(oc.getURI());
				characteristicOptions.append("'");
				characteristicOptions.append(">");
				characteristicOptions.append(oc.getName());
				characteristicOptions.append("</option>");
			}
			returnString = characteristicOptions.toString();
		}
		if (domainClass.equals(Standard.class.getName())) {
			// Standard
			StringBuffer standardOptions = new StringBuffer();
			List<OntologyClass> standards = SMS.getInstance().getAnnotationManager().getActiveStandards(activeEntities, activeCharacteristics);
			for (OntologyClass oc: standards) {
				standardOptions.append("<option ");
				standardOptions.append("title='");
				standardOptions.append(oc.getURI());
				standardOptions.append("' ");
				standardOptions.append("value='");
				standardOptions.append(oc.getURI());
				standardOptions.append("'");
				standardOptions.append(">");
				standardOptions.append(oc.getName());
				standardOptions.append("</option>");
			}
			returnString = standardOptions.toString();
		}
		if (domainClass.equals(Protocol.class.getName())) {
			// Protocol TODO: filtering
			StringBuffer protocolOptions = new StringBuffer();
			List<OntologyClass> protocols = SMS.getInstance().getAnnotationManager().getActiveProtocols();
			for (OntologyClass oc: protocols) {
				protocolOptions.append("<option ");
				protocolOptions.append("title='");
				protocolOptions.append(oc.getURI());
				protocolOptions.append("' ");
				protocolOptions.append("value='");
				protocolOptions.append(oc.getURI());
				protocolOptions.append("'");
				protocolOptions.append(">");
				protocolOptions.append(oc.getName());
				protocolOptions.append("</option>");
			}
			returnString = protocolOptions.toString();
		}
		if (domainClass.equals(Measurement.class.getName())) {
			// Measurement TODO: filtering
			StringBuffer measurementOptions = new StringBuffer();
			List<OntologyClass> measurements = SMS.getInstance().getAnnotationManager().getActiveMeasurements();
			for (OntologyClass oc: measurements) {
				measurementOptions.append("<option ");
				measurementOptions.append("title='");
				measurementOptions.append(oc.getURI());
				measurementOptions.append("' ");
				measurementOptions.append("value='");
				measurementOptions.append(oc.getURI());
				measurementOptions.append("'");
				measurementOptions.append(">");
				measurementOptions.append(oc.getName());
				measurementOptions.append("</option>");
			}
			returnString = measurementOptions.toString();
		}
		
		response.setContentType("text/html");
        PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        out.print(returnString);
        out.close();
		
	}

	private String augmentQuery(List<Annotation> matches, StringBuffer results) throws Exception {
		
		//make a map for the annotation matches - easy to reference when augmenting the search results with annotation info
		// TODO: handle multiple annotations per package
		Map<String, Annotation> matchMap = new HashMap<String, Annotation>();
		for (Annotation annotation: matches) {
			matchMap.put(annotation.getEMLPackage(), annotation);
		}
		// read existing results into DOM
		StringReader xmlReader = new StringReader(results.toString());
		Document document = XMLUtilities.getXMLReaderAsDOMDocument(xmlReader);
		NodeList documentNodes = XMLUtilities.getNodeListWithXPath(document.getDocumentElement(), "//document");
		if (documentNodes != null) {
			for (int i = 0; i < documentNodes.getLength(); i++) {
				// get the document element
				Node documentNode = documentNodes.item(i);
				// get the document/docid element
				Node docidNode = XMLUtilities.getTextNodeWithXPath(documentNode, "docid");
				String docid = docidNode.getTextContent();
				// get the annotation for this document
				Annotation annotation = matchMap.get(docid);
				if (annotation == null) {
					continue;
				}
				// add the annotation to the search results
				StringReader annotatationStringReader = new StringReader(annotation.toString());
				// TODO: subset of the annotation?
				Node annotationNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(annotatationStringReader);
				Node importedAnnnotationNode = document.importNode(annotationNode, true);
				documentNode.appendChild(importedAnnnotationNode);
			}
		}
		
		// return a string
		String modifiedResults = XMLUtilities.getDOMTreeAsString(document.getDocumentElement());
		
		return modifiedResults;
		
	}

	public boolean handlesAction(String action) {
		return supportedActions.contains(action.toLowerCase());
	}

}
