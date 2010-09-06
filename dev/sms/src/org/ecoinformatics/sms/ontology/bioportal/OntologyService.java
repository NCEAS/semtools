package org.ecoinformatics.sms.ontology.bioportal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OntologyService {
	
	private static OntologyService instance;
	private static String BIOPORTAL_URL_PREFIX = "http://rest.bioontology.org/bioportal";

	public static OntologyService getInstance() {
		if (instance == null) {
			instance = new OntologyService();
		}
		return instance;
	}
	
	public List<OntologyBean> getOntologyBeans() throws Exception {
		String uri = BIOPORTAL_URL_PREFIX + "/ontologies";
		return processOntologyBeanResponse(uri);
		
	}
	
	private List<OntologyBean> processOntologyBeanResponse(String uri) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

		Document doc = docBuilder.parse(uri);

		//Normalize text representation
		doc.getDocumentElement().normalize();

		NodeList listOfSearchResults = doc.getElementsByTagName("ontologyBean");
		int totalSearchResults = listOfSearchResults.getLength();
		System.out.println("Total Results: "+ totalSearchResults);
		
		List<OntologyBean> results = new ArrayList<OntologyBean>();
		for (int i = 0; i < totalSearchResults; i++) {
			OntologyBean ontologyBean = new OntologyBean();
			Element beanNode = (Element) listOfSearchResults.item(i);
			
			try {
				String id = beanNode.getElementsByTagName("id").item(0).getTextContent();
				ontologyBean.setId(id);
			} catch (Exception e) {}
			try {
				String ontologyId = beanNode.getElementsByTagName("ontologyId").item(0).getTextContent();
				ontologyBean.setOntologyId(ontologyId);
			} catch (Exception e) {}
			try {
				String urn = beanNode.getElementsByTagName("urn").item(0).getTextContent();
				ontologyBean.setUrn(urn);
			} catch (Exception e) {}
			try {
				String displayLabel = beanNode.getElementsByTagName("displayLabel").item(0).getTextContent();
				ontologyBean.setDisplayLabel(displayLabel);
			} catch (Exception e) {}
			try {
				String description = beanNode.getElementsByTagName("description").item(0).getTextContent();
				ontologyBean.setDescription(description);
			} catch (Exception e) {}
			try {	
				String versionNumber = beanNode.getElementsByTagName("versionNumber").item(0).getTextContent();
				ontologyBean.setVersionNumber(versionNumber);
			} catch (Exception e) {}
			try {	
				String contactName = beanNode.getElementsByTagName("contactName").item(0).getTextContent();
				ontologyBean.setContactName(contactName);
			} catch (Exception e) {}				
			
			results.add(ontologyBean);
		}
		return results;
	}
	
	public String getOntology(String id) {
		String uri = BIOPORTAL_URL_PREFIX + "/ontologies/download/" + id;
		return uri;
	}
	
	public OntologyBean getOntologyBean(String forId) throws Exception {
		String uri = BIOPORTAL_URL_PREFIX + "/ontologies/" + forId;
		
		// just one
		return processOntologyBeanResponse(uri).get(0);
	}

	public static void main(String args[]) {
		try {
			List<OntologyBean> beans = OntologyService.getInstance().getOntologyBeans();
			for (OntologyBean bean: beans) {
				System.out.println(bean.getDisplayLabel());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
