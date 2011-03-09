package edu.ucsb.nceas.metacat.plugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
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
import org.ecoinformatics.sms.annotation.search.data.Materializer;
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
import edu.ucsb.nceas.metacat.MetaCatServlet;
import edu.ucsb.nceas.metacat.QuerySpecification;
import edu.ucsb.nceas.metacat.client.InsufficientKarmaException;
import edu.ucsb.nceas.metacat.client.Metacat;
import edu.ucsb.nceas.metacat.client.MetacatFactory;
import edu.ucsb.nceas.metacat.properties.PropertyService;
import edu.ucsb.nceas.metacat.service.SessionService;
import edu.ucsb.nceas.metacat.shared.HandlerException;
import edu.ucsb.nceas.metacat.util.AuthUtil;
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
		supportedActions.add("getcart");

		initializeOntologies();
		initializeAnnotations();
	}
	
	private static void clearAnnotations() throws Exception {
		for (String annotationId: SMS.getInstance().getAnnotationManager().getAnnotationIds()) {
			SMS.getInstance().getAnnotationManager().removeAnnotation(annotationId);
		}
	}
	
	/**
	 * Look up annotations stored on this Metacat instance
	 */
	private static void initializeAnnotations() {
		try {
			// clear the existing annotations
			clearAnnotations();
			// load them again
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
				"http://ecoinformatics.org/oboe-ext/sbclter.1.0/oboe-sbclter.owl", 
				"http://ecoinformatics.org/oboe-ext/sbclter.1.0/oboe-sbclter.owl");
		
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
		case 6:
			getCart(params, request, response, username, groups, sessionId);
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
            Writer out = new OutputStreamWriter(response.getOutputStream(), MetaCatServlet.DEFAULT_ENCODING);
            out.write(sb.toString());
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
            Writer out = new OutputStreamWriter(response.getOutputStream(), MetaCatServlet.DEFAULT_ENCODING);
            out.write(sb.toString());
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
            Writer out = new OutputStreamWriter(response.getOutputStream(), MetaCatServlet.DEFAULT_ENCODING);
            out.write(sb.toString());
            out.close();
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
		}
	}
	
	private void getCart(Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId) throws HandlerException {
		try {

			// annotation matches - may be empty after processing
			Map<Annotation, String> matches = new HashMap<Annotation, String>();

			// get the packages in the cart if the user is logged in
			boolean loggedIn = AuthUtil.isUserLoggedIn(request);
			if (loggedIn) {
				String[] docids = SessionService.getInstance().getRegisteredSession(sessionId).getDocumentCart().getDocids();
				if (docids != null) {
					for (String dataPackage: docids) {
						List<Annotation> m = SMS.getInstance().getAnnotationManager().getAnnotations(dataPackage);
						for (Annotation a: m) {
							matches.put(a, null);
						}
					}
				}
			}
			
			// process matches if we have them
			handleAnnotationMatches(params, request, response, username, groups, sessionId, matches, null);
			
		} catch (Exception e) {
			throw new HandlerException(e.getMessage());
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
			
			// do the data filtering
			Map<Annotation, String> matchesMap = new HashMap<Annotation, String>();
			for (Annotation a: matches) {
				matchesMap.put(a, null);
			}
			String allData = Materializer.getInstance(Materializer.METACAT_CONFIGURATION).filterDataMatches(matchesMap, criteria);
			
			// handle the matches for this query
			handleAnnotationMatches(params, request, response, username, groups, sessionId, matchesMap, allData);
			
		} catch (Exception e) {
			throw new HandlerException(e.getMessage());
		}
	}
	
	private void handleAnnotationMatches(Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId, Map<Annotation, String> matches, String allData) throws HandlerException {
		try {
			
			// look up annotation matches - for the data package ids
			Vector<String> docids = new Vector<String>();
			for (Annotation annotation: matches.keySet()) {
				String docid = annotation.getDataPackage();
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
	        String modifiedResults = augmentQuery(matches, allData, results);
	        
	        // style the results
	        Writer out = null;
	        String qformat = params.get("qformat")[0];
	        if (!qformat.equals(MetacatUtil.XMLFORMAT)) {
	        	// html
	        	response.setContentType("text/html");
		        out = new OutputStreamWriter(response.getOutputStream(), MetaCatServlet.DEFAULT_ENCODING);
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
		        out = new OutputStreamWriter(response.getOutputStream(), MetaCatServlet.DEFAULT_ENCODING);
		        // send to output as plain xml
	        	out.write(modifiedResults);
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
		String domainClassString = params.get("class")[0];
		OntologyClass domainClass = null;
		try {
			domainClass = Annotation.OBOE_CLASSES.get(Class.forName(domainClassString));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
					log.warn("Problem parsing parameter", e);
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
					log.warn("Problem parsing parameter", e);
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
					log.warn("Problem parsing parameter", e);
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
					log.warn("Problem parsing parameter", e);
				}
			}
		}
		// Measurement (expansion
		if (measurementParam != null) {
			for (String classString: measurementParam) {
				OntologyClass c = null;
				try {
					c = new OntologyClass(classString);
					// use the ontology to expand the Measurement
					activeEntities.addAll(Measurement.lookupRestrictionClasses(c, Entity.class));
					activeCharacteristics.addAll(Measurement.lookupRestrictionClasses(c, Characteristic.class));
					activeStandards.addAll(Measurement.lookupRestrictionClasses(c, Standard.class));
					activeProtocols.addAll(Measurement.lookupRestrictionClasses(c, Protocol.class));
					// also include the template measurement
					activeMeasurements.add(c);
				} catch (Exception e) {
					log.warn("Problem parsing parameter", e);
				}
			}
		}		
		
		String returnString = "";
		List<OntologyClass> classes = null;

		if (domainClassString.equals(Entity.class.getName())) {
			classes = SMS.getInstance().getAnnotationManager().getActiveEntities(activeCharacteristics, activeStandards, activeProtocols, true, false);
		}
		if (domainClassString.equals(Characteristic.class.getName())) {
			// Characteristic
			classes = SMS.getInstance().getAnnotationManager().getActiveCharacteristics(activeEntities, activeStandards, activeProtocols, true, false);
		}
		if (domainClassString.equals(Standard.class.getName())) {
			classes = SMS.getInstance().getAnnotationManager().getActiveStandards(activeEntities, activeCharacteristics, activeProtocols, true, false);
		}
		if (domainClassString.equals(Protocol.class.getName())) {
			// Protocol
			classes = SMS.getInstance().getAnnotationManager().getActiveProtocols(activeEntities, activeCharacteristics, activeStandards, true, false);
		}
		if (domainClassString.equals(Measurement.class.getName())) {
			// Measurement TODO: filtering
			classes = SMS.getInstance().getAnnotationManager().getActiveMeasurements();
		}
		
		// construct the selection fields for existing annotations
		StringBuffer options = new StringBuffer();		
		options.append("<ul>");
		options.append(
				buildTree(
						domainClass, 
						classes));
		options.append("</ul>");
		returnString = options.toString();
		
		response.setContentType("text/html");
        Writer out = null;
		try {
            out = new OutputStreamWriter(response.getOutputStream(), MetaCatServlet.DEFAULT_ENCODING);
            out.write(returnString);
            out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String buildTree(OntologyClass node, List<OntologyClass> activeClasses) {
		StringBuffer sb = new StringBuffer();
		sb.append("<li ");
		sb.append("id='");
		sb.append(node.getName());
		sb.append("'");
		sb.append(">");
		sb.append("<a href='javascript:donothing()' ");
		sb.append("onclick='select(this)' ");
		sb.append("title='");
		sb.append(node.getURI());
		sb.append("'" );
		if (activeClasses.contains(node)) {
			sb.append("class='bold' ");
		}
		sb.append(">");
		sb.append(node.getName());
		sb.append("</a>");
		List<OntologyClass> children = SMS.getInstance().getOntologyManager().getNamedSubclasses(node, false);
		if (!children.isEmpty()) {
			sb.append("<ul>");
			for (OntologyClass oc: children) {
				sb.append(buildTree(oc, activeClasses));
			}
			sb.append("</ul>");
		}
		sb.append("</li>");

		return sb.toString();
	}

	private String augmentQuery(Map<Annotation, String> matches, String allData, StringBuffer results) throws Exception {
		
		//make a map for the annotation matches - easy to reference when augmenting the search results with annotation info
		// TODO: handle multiple annotations per package
		Map<String, Annotation> matchMap = new HashMap<String, Annotation>();
		for (Annotation annotation: matches.keySet()) {
			matchMap.put(annotation.getDataPackage(), annotation);
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
				
				// add the data to the search results
				String data = matches.get(annotation);
				if (data != null) {
					StringReader dataStringReader = new StringReader("<data><![CDATA[" + data + "]]></data>");
					Node dataNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(dataStringReader);
					Node importedDataNode = document.importNode(dataNode, true);
					documentNode.appendChild(importedDataNode);
				}
			}
			if (allData != null) {
				Node resultsNode = XMLUtilities.getNodeWithXPath(document.getDocumentElement(), "//resultset");
				StringReader dataStringReader = new StringReader("<data><![CDATA[" + allData + "]]></data>");
				Node dataNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(dataStringReader);
				Node importedDataNode = document.importNode(dataNode, true);
				resultsNode.appendChild(importedDataNode);
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
