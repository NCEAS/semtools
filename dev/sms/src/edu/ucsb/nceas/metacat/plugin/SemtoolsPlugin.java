package edu.ucsb.nceas.metacat.plugin;

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

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.annotation.search.CriteriaReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import edu.ucsb.nceas.metacat.DBQuery;
import edu.ucsb.nceas.metacat.DBTransform;
import edu.ucsb.nceas.metacat.DBUtil;
import edu.ucsb.nceas.metacat.DocumentIdQuery;
import edu.ucsb.nceas.metacat.QuerySpecification;
import edu.ucsb.nceas.metacat.client.Metacat;
import edu.ucsb.nceas.metacat.client.MetacatFactory;
import edu.ucsb.nceas.metacat.properties.PropertyService;
import edu.ucsb.nceas.metacat.shared.HandlerException;
import edu.ucsb.nceas.metacat.util.MetacatUtil;
import edu.ucsb.nceas.metacat.util.SystemUtil;
import edu.ucsb.nceas.utilities.XMLUtilities;

public class SemtoolsPlugin implements MetacatHandlerPlugin {

	private static List<String> supportedActions = new ArrayList<String>();
	static {
		//note: order matters - it drives the switch/case handling
		supportedActions.add("semquery");
		supportedActions.add("initialize");
		supportedActions.add("registerontology");
		supportedActions.add("unregisterontology");

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
					InputStream in = mc.read(annotationDocid);
					Annotation annotation = Annotation.read(in);
					if (!SMS.getInstance().getAnnotationManager().isAnnotation(annotation.getURI())) {
						SMS.getInstance().getAnnotationManager().importAnnotation(annotation, metacatURL + "/" + annotationDocid);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// load them in the manager
		for (Entry<String, String> entry: ontologyURIs.entrySet()) {
			String uri = entry.getKey();
			String url = entry.getValue();
			try {
				SMS.getInstance().getOntologyManager().importOntology(url, uri);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		default:
			break;
		}
		
		return true;
	}

	private void registerOntology(Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId) throws HandlerException {
		
		StringBuffer sb = new StringBuffer();
		String uri = null;
		String url = null;
		try {
			uri = params.get("uri")[0];
			url = params.get("url")[0];
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
			e.printStackTrace();
		}
	}
	
	private void unregisterOntology(Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId) throws HandlerException {
		
		StringBuffer sb = new StringBuffer();
		String uri = null;
		try {
			uri = params.get("uri")[0];
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
			e.printStackTrace();
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
				docids.add(docid);
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
	        //response.setContentType("text/html");
	        PrintWriter out = response.getWriter();
	        String qformat = params.get("qformat")[0];
	        if (!qformat.equals(MetacatUtil.XMLFORMAT)) {
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
		        // send to output as plain xml
	        	out.print(modifiedResults);
	        }
	        out.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException(e.getMessage());
		}
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
		
		// return a string
		String modifiedResults = XMLUtilities.getDOMTreeAsString(document.getDocumentElement());
		
		return modifiedResults;
		
	}

	public boolean handlesAction(String action) {
		return supportedActions.contains(action.toLowerCase());
	}

}
