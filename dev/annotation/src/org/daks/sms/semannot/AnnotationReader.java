/**
 *    '$RCSfile: AnnotationReader.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2008-06-02 19:51:10 $'
 *   '$Revision: 1.1 $'
 *
 *  For Details: http://daks.ucdavis.edu
 *
 * Copyright (c) 2005 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */

package org.daks.sms.semannot;

import java.io.InputStream;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;


public class AnnotationReader {

    /**
     * Read in a new annotation from an input stream
     * @param in the input stream
     * @return the parsed annotation
     */
    public static Annotation read(InputStream in) throws AnnotationException {
	_annotation = new Annotation();
	try {
	    Document doc = _getDocument(in);
	    _parseAnnotation(doc);
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new AnnotationException(e.getMessage());
	}
	return _annotation;
    }
    
    /**
     * Create the xml document
     * @param in the input stream
     * @return the XML document object
     */
    private static Document _getDocument(InputStream in) throws Exception {
	// create the document builder factory
	DocumentBuilderFactory factory = 
	    DocumentBuilderFactory.newInstance();
	// set document builder parameters
	factory.setCoalescing(true);
	factory.setExpandEntityReferences(true);
	factory.setIgnoringComments(true);
	factory.setIgnoringElementContentWhitespace(true);
	// create the document from the input
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document doc = builder.parse(new InputSource(in));
	// make sure the annotation root element exists
	_ensureAnnotationRoot(doc); 
	return doc;
    }

    /**
     * Parse an annotation element
     * @param doc the document
     */
    private static void _parseAnnotation(Document doc) throws Exception {
	Element root = doc.getDocumentElement();
	// parse emlPackage and dataTable
 	_parseEMLResource(root);
 	// parse the observations
 	for(Element elem : _getElements(root, "observation")) 
 	    _parseObservation(elem);	    
	// parse the context relationships	
	_parseContext(root);
 	// parse the mappings
 	for(Element elem : _getElements(root, "map")) 
	    _parseMapping(elem);	    
	_validate();
    }

    /**
     * Validate the annotation
     */
    private static void _validate() throws Exception {
	// check measurement partial keys
	for(Observation o : _annotation.getObservations()) {
	    boolean hasIdentifyingRel = false;
	    for(Context c : o.getContexts()) {
		if(c.isIdentifying()) {
		    hasIdentifyingRel = true;
		    break;
		}
	    }
	    for(Measurement m : o.getMeasurements()) {
		if(m.isKey() && hasIdentifyingRel) {
		    _error("observation with identifying context " + 
			   "relationship and key: " +  m.getLabel());
		    return;
		}
		else if(m.isPartialKey() && !hasIdentifyingRel) {
		    _error("observation without identifying context " + 
			   "relationship with partial key: " + m.getLabel());
		    return;
		}
	    }
	}
    }


    /**
     * Parse the context of observations
     * @root the document root
     */
    private static void _parseContext(Element root) throws Exception {
	// create a hashmap of label -> observation
	HashMap<String,Observation> map = new HashMap();
	for(Observation o : _annotation.getObservations())  {
	    String label = o.getLabel();
	    if(map.containsKey(label))
		_error("multiple observations with label '" + label + "'");
	    map.put(label, o);
	}
	// build up the context relationships
	for(Element obs : _getElements(root, "observation")) {
	    for(Element ctx : _getElements(obs, "context")) {
		// get the parent observation
		Attr att1 = _getAttribute(obs, "label");
		String ol = att1.getValue().trim();
		Observation o = map.get(ol);
		// get the contextual observation
		Attr att2 = _getAttribute(ctx, "observation");
		if(att2 == null)
		    _error("context missing observation attribute");
		String cl = att2.getValue().trim();
		Observation c = map.get(cl);
		if(c == null) 
		    _error("context observation '" + cl + "' not found");
		Context x = new Context();
		x.setObservation(c);
		o.addContext(x);
		// get the identifying flag (optional)
		Attr att3 = _getAttribute(ctx, "identifying");
		if(att3 != null) {
		    String flag = att3.getValue().trim();
		    if(flag.toLowerCase().equals("yes"))
			x.setIdentifying(true);
		    else if(!flag.toLowerCase().equals("no"))
			_error("invalid 'identifying' value: " + flag);
		}
		// get the relationship (optional)
		List<Element> rels = _getElements(ctx, "relationship");
		if(rels.size() > 1)
		    _error("context with multiple relationships");
		else if(rels.size() == 1)
		    _parseRelationship(x, rels.get(0)); 
	    }
	}
    }
    
    /**
     * Parse the annotation mappings
     * @param e a map element
     */
    private static void _parseMapping(Element e) throws Exception {
	// create a hash of label -> measurement
	HashMap<String,Measurement> map = new HashMap();
	for(Observation o : _annotation.getObservations()) 
	    for(Measurement meas : o.getMeasurements()) {
		String label = meas.getLabel();
		if(map.containsKey(label))
		    _error("multiple measurements with label: " + label);
		map.put(label, meas);
	    }
	Mapping m = new Mapping();
	_annotation.addMapping(m);
	// get the attribute name
	Attr att1 = _getAttribute(e, "attribute");
	if(att1 == null)
	    _error("map element missing dataset attribute name");
	m.setAttribute(att1.getValue().trim());
	// get the measurement label
	Attr att2 = _getAttribute(e, "measurement");
	if(att2 == null)
	    _error("map element missing measurement");
	String label = att2.getValue().trim();
	Measurement meas = map.get(label);
	if(meas == null)
	    _error("no measurement with label: " + label);
	m.setMeasurement(meas);
	// get the value (optional)
	Attr att3 = _getAttribute(e, "value");
	if(att3 != null)
	    m.setValue(att3.getValue().trim());
	// get the conditions (optional)
	Attr att4 = _getAttribute(e, "if");
	if(att4 != null)
	    for(Condition c : _getConditions(att4.getValue().trim()))
		m.addCondition(c);
    }
    
    /**
     * Parse the emlpackage and dataTable attribues
     * @param e the element
     */
    private static void _parseEMLResource(Element e) throws Exception {
 	// get the emlPackage id
 	Attr att1 = _getAttribute(e, "emlPackage");
 	if(att1 == null) 
 	    _error("annotation element missing 'emlPackage' attribute");
	_annotation.setEMLPackage(att1.getValue().trim());
 	// get the dataTable index
 	Attr att2 = _getAttribute(e, "dataTable");
 	if(att2 == null) 
 	    _error("annotation element missing 'dataTable' attribute");
	_annotation.setDataTable(att2.getValue().trim());
	// get the annotation uri (optional)
	Attr att3 = _getAttribute(e, "xmlns");
	if(att3 != null)
	    _annotation.setURI(att3.getValue().trim());
     }

     /**
      * Parse an observation element
      * @param e the observation element
      */
    private static void _parseObservation(Element e) throws Exception {
 	// create and add the observation 
 	Observation o = new Observation();
	_annotation.addObservation(o);
	// get the label
 	Attr att1 = _getAttribute(e, "label");
 	if(att1 == null) 
 	    _error("observation element missing 'label' attribute");	    
 	o.setLabel(att1.getValue().trim());
	// get the distinct flag (optional)
	Attr att2 = _getAttribute(e, "distinct");
	if(att2 != null) {
	    String flag = att2.getValue().trim();
	    if(flag.toLowerCase().equals("yes"))
		o.setDistinct(true);
	    else if(!flag.toLowerCase().equals("no"))
		_error("invalid 'distinct' value: " + flag);
	}	
 	// get the optional entities
	List<Element> elems = _getElements(e, "entity");
	if(elems.size() > 1)
	    _error("observation with multiple entities");
	else if(elems.size() == 1)
	    _parseEntity(o, elems.get(0));
	// get the optional measurements
	for(Element measurement : _getElements(e, "measurement"))
	    _parseMeasurement(o, measurement);
     }

     /**
      * Parse a measurement element
      * @param elem the measurement elem
      * @param observation the containing observation type
      */
    private static void _parseMeasurement(Observation o, Element e)
 	throws Exception 
    {
	Measurement m = new Measurement();
	o.addMeasurement(m);
	// get the label (optional)
	Attr att1 = _getAttribute(e, "label");
	if(att1 != null) 
	    m.setLabel(att1.getValue().trim());
	// get the precision (optional)
	Attr att2 = _getAttribute(e, "precision");
	if(att2 != null) {
	    double val = Double.parseDouble(att2.getValue().trim());
	    m.setPrecision(val);
	}
	// get the value (optional)
	Attr att3 = _getAttribute(e, "value");
	if(att3 != null)
	    m.setValue(att3.getValue().trim());
	// get the key (optional)
	Attr att4 = _getAttribute(e, "key");
	if(att4 != null) {
	    String flag = att4.getValue().trim();
	    if(flag.toLowerCase().equals("yes"))
		m.setKey(true);
	    else if(!flag.toLowerCase().equals("no"))
		_error("invalid 'key' value: " + flag);
	}	
	// get the partial key (optional)
	Attr att5 = _getAttribute(e, "partialKey");
	if(att5 != null) {
	    String flag = att5.getValue().trim();
	    if(flag.toLowerCase().equals("yes"))
		m.setPartialKey(true);
	    else if(!flag.toLowerCase().equals("no"))
		_error("invalid 'key' value: " + flag);
	}	
	if(m.isKey() && m.isPartialKey())
	    _error("measurement is both a key and a partial key");
	// get the standard
	List<Element> stds = _getElements(e, "standard");
	if(stds.size() > 1)
	    _error("measurement with multiple standards");
	else if(stds.size() == 1)
	    _parseStandard(m, stds.get(0));
 	// get the optional characteristics
	for(Element characteristic : _getElements(e, "characteristic"))
	    _parseCharacteristic(m, characteristic);
    }

    /**
     * Parse an entity element
     * @param o the observation
     * @param e the element
     */
    private static void _parseEntity(Observation o, Element e) throws Exception 
    {
	Entity entity = new Entity();
	// get the id attribute
	Attr att1 = _getAttribute(e, "id");
	if(att1 == null)
	    _error("entity missing 'id' attribute");
	_getQName(e, entity, att1.getValue().trim());
	o.setEntity(entity);
    }

     /**
      * Parse a measurement standard element
      * @param m the measurement
      * @param e the element
      */
    private static void _parseRelationship(Context c, Element e)
 	throws Exception 
    {
	Relationship relationship = new Relationship();
	// get the id attribute
	Attr att = _getAttribute(e, "id");
	if(att == null)
	    _error("relationship missing 'id' attribute");
	_getQName(e, relationship, att.getValue().trim());
	c.setRelationship(relationship);
     }

     /**
      * Parse a measurement standard element
      * @param m the measurement
      * @param e the element
      */
    private static void _parseStandard(Measurement m, Element e)
 	throws Exception 
    {
	Standard standard = new Standard();
	// get the id attribute
	Attr att = _getAttribute(e, "id");
	if(att == null)
	    _error("entity missing 'id' attribute");
	_getQName(e, standard, att.getValue().trim());
	m.setStandard(standard);
     }

     /**
      * Parse a characteristic element. 
      * @param m the measurement
      * @param e the element
      */
    private static void _parseCharacteristic(Measurement m, Element e)
 	throws Exception 
    {
	Characteristic characteristic = new Characteristic();
	// get the id attribute
	Attr att = _getAttribute(e, "id");
	if(att == null)
	    _error("entity missing 'id' attribute");
	_getQName(e, characteristic, att.getValue().trim());
	m.addCharacteristic(characteristic);
     }


    /**
     * Parse the condition expression of a value element
     * @param cond the condition attribute
     * @param m the containing measurement value type
     */
    private static List<Condition> _getConditions(String str) 
 	throws Exception 
    {
	List<Condition> result = new ArrayList();
	str = str.trim();
	while(!str.equals("")) {
	    Condition c = new Condition();
	    str = _parseCondAtt(c, str);
	    str = _parseCondOp(c, str);
	    str = _parseCondConst(c, str);
	    if(!result.contains(c))
		result.add(c);
	}
	return result;
    }

    /**
     * Parse the attribute from the condition string
     * @param c the condition 
     * @param str the current state of the condition string
     * @return the new string with the attribute removed
     */
    private static String _parseCondAtt(Condition c, String str) 
	throws Exception 
    {
	str = str.trim();
	String attribute = "";
	int i = 0;
	for(; i < str.length(); i++) {
	    if(Character.isWhitespace(str.charAt(i))) 
		break;
	    else
		attribute += str.charAt(i);
	}
	if(i == 0 || i == str.length())
	    _error("invalid condition expression '" + str + "'");
	c.setAttribute(attribute.trim());
	return str.substring(i+1, str.length());
    }

    /**
     * Parse the operator from the condition string
     * @param c the condition 
     * @param str the current state of the condition string
     * @return the new string with the operator removed
     */
    private static String _parseCondOp(Condition c, String str) 
	throws Exception
    {
	str = str.trim();
	String op = "";
	int i = 0;
	for(; i < str.length(); i++) {
	    if(Character.isWhitespace(str.charAt(i))) 
		break;
	    else
		op += str.charAt(i);
	}
	if(i == 0 || i == str.length() || !Condition.isOperator(op))
	    _error("invalid condition expression '" + str + "'");
	c.setOperator(op);
	return str.substring(i+1, str.length());
    }

    /**
     * Parse the constant from the condition string
     * @param c the condition 
     * @param str the current state of the condition string
     * @return the new string with the constant removed
     */
    private static String _parseCondConst(Condition c, String str) 
	throws Exception
    {
	str = str.trim();
	if(str.startsWith("'"))
	    return _parseStringConst(c, str);
	else
	    return _parseNumericConst(c, str);
    }

    /**
     * Parse the string constant from the condition string
     * @param c the condition expression
     * @param str the current state of the condition string
     * @return the new string with the constant removed
     */
    private static String _parseStringConst(Condition c, String str)
	throws Exception
    {
	String constant = "";
	int i = 1;	
	// get the ending quote
	for(; i < str.length(); i++) {
	    if(str.charAt(i) == '\'') 
		break;
	    else
		constant += str.charAt(i);
	}
	// make sure we found a quote
	if(i == str.length())
	    _error("invalid condition expression '" + str + "'");
	// set the value
	c.setValue(constant);
	// set up the remainder of the string
	str = str.substring(i+1, str.length()).trim();
	if(!str.equals("") && !str.startsWith(","))
	    _error("invalid condition expression '" + str + "'");
	if(str.startsWith(","))
	    str = str.substring(1, str.length());
	return str;
    }	
    
    /**
     * Parse the numeric constant from the condition string
     * @param c the condition 
     * @param str the current state of the condition string
     * @return the new string with the constant removed
     */
    private static String _parseNumericConst(Condition c, String str)
	throws Exception 
    {
	String constant = "";
	int i = 0;
	for(; i < str.length(); i++) {
	    if(str.charAt(i) == ',')
		break;
	    constant += str.charAt(i);
	}
	// set the constant
	c.setValue(constant.trim());
	// set up the remainder of the string
	str = str.substring(i, str.length()).trim();
	if(!str.equals("") && !str.startsWith(","))
	    _error("invalid condition expression '" + str + "'");
	if(str.startsWith(","))
	    str = str.substring(1, str.length());
	return str;
    }

    /** 
     * Parse an ontology id string. These are QNames of the form
     * "prefix:name" where prefix is the ontology label and name is
     * the local name in the ontology.
     * @param elem the current XML element
     * @param item the ontology item to populate
     * @param str the attribute string
     */
    private static void _getQName(Element elem, OntologyItem item, String str) 
	throws Exception
    {
  	// break string into label and name parts
  	String[] s = str.split(":");
  	if(s.length != 2)
  	    _error("invalid id '" + str + "'");
  	String prefix = s[0];
  	String local = s[1];
	Ontology ont = _getOntology(elem, prefix);
	item.setOntology(ont);
	item.setName(local);
    }

    /**
     * Gets the ontology associated with the given XML element and
     * namespace prefix. This method has a side effect, which is to
     * add the ontology to the annotation if it has not been
     * previously added.
     */
    private static Ontology _getOntology(Element elem, String prefix) 
	throws Exception
    {
	String uri = _getURI(elem, prefix);
	for(Ontology ont : _annotation.getOntologies()) {
	    if(ont.getURI().equals(uri))
		return ont;
	}
	Ontology ont = new Ontology(prefix, uri);
	_annotation.addOntology(ont);
	return ont;
    }

    /** 
      * Get the URI associated with a namespace prefix
      * @param elem the element to start searching from
      * @param prefix the prefix string to search for
      * @return the uri or null if no such prefix is used
      */
    private static String _getURI(Element elem, String prefix)
	throws Exception 
    {
	Object parent = elem.getParentNode();
 	if(elem.hasAttribute("xmlns:" + prefix))
 	    return elem.getAttribute("xmlns:" + prefix);
 	else if(parent != null && parent instanceof Element)
 	    return _getURI((Element)parent, prefix);
	else
	    _error("could not find prefix: " + prefix);
 	return null;
    }


    /** 
      * Get the namespace associated with an element 
      * @param elem the element to start searching from
      * @return the namespace uri or null if no namespace
      */
    private static String _getNamespace(Element elem) throws Exception {
	Object parent = elem.getParentNode();
	String tag = elem.getTagName();
	String [] s = tag.split(":");
	if(s.length == 2)
	    return _getURI(elem, s[0]);
 	if(elem.hasAttribute("xmlns"))
 	    return elem.getAttribute("xmlns");
 	else if(parent != null && parent instanceof Element)
 	    return _getNamespace((Element)parent);
 	return null;
    }


    /**
     * Get the element tag of the given element. The element is
     * checked to ensure it is a member of the annotation namespace,
     * and if not, an exception is thrown.
     * @param elem the element 
     * @return the element name without an appended namespace
     */
    private static String _getElementTag(Element elem) throws Exception {
	String ns = _getNamespace(elem);
	if(ns == null || !ns.equals(_SMS_SEM_ANNOT_NS)) 
	    _error("element missing '" + _SMS_SEM_ANNOT_NS + "' namespace:" + 
		   elem.getTagName());
	String [] s = elem.getTagName().split(":");
	if(s.length == 2) 
	    return s[1].toLowerCase();
	return s[0].toLowerCase();
    }

    /**
     * Get the child elements with the given case insensitive tag
     * name.
     * @param p the parent element
     * @param t the element tag name to match 
     * @return child elements with matching name
     */
    private static List<Element> _getElements(Element parent, String tag) 
	throws Exception 
    {
	ArrayList<Element> elems = new ArrayList();
	if(tag == null)
	    return elems;
	tag = tag.toLowerCase();
	NodeList children = parent.getChildNodes();
	for(int i = 0; i < children.getLength(); i++) {
	    Node node = children.item(i);
	    if(node instanceof Element) {
		Element child = (Element)node;
		String childTag = _getElementTag(child);
		if(childTag != null && childTag.equals(tag))
		    elems.add(child);
	    }
	}
	return elems;
    }
    
    /**
     * Get the element attribute with the given case insensitive
     * attribute name
     * @param e the element to search
     * @param a the attribute name to match 
     * @retun attribute with matching name or null
     */
    private static Attr _getAttribute(Element e, String a) {
	if(a == null)
	    return null;
	a = a.toLowerCase();
  	NamedNodeMap as = e.getAttributes();
  	// get the values for the relevant attributes
  	for(int i = 0; i < as.getLength(); i++) {
  	    Attr att = (Attr)as.item(i);
  	    String name = att.getName().toLowerCase();
  	    if(name != null && name.equals(a))
  		return att;
  	}
  	return null;
    }

    /**
     * Get a list of space-separated strings from a single string
     * @param str the string
     * @return a list of strings
     */
    private static List<String> _getStringList(String str) {
	ArrayList<String> result = new ArrayList();
	if(str == null)
	    return result;
	String [] s = str.split("\\s");
	for(int i = 0; i < s.length; i++)
	    if(!result.contains(s[i]))
		result.add(s[i]);
	return result;
    }
	    
    /**
     * Make sure that the document root is an annotation element (has
     * the tag "annotation")
     */
    private static void _ensureAnnotationRoot(Document doc) throws Exception {
 	Element root = doc.getDocumentElement();
 	if(root == null || root.getTagName() == null)
 	    _error("missing document element");
 	String tagName = _getElementTag(root);
 	if(tagName == null || !tagName.equals("annotation"))
 	    _error("expecting 'annotation' found '" + tagName + "'"); 
    }

    /**
     * Throw an exception with the given message
     * @param msg the message
     */
    private static void _error(String msg) throws Exception {
	throw new Exception("ERROR: " + msg);
    }


    /* the annotation */
    private static Annotation _annotation;
    /* the annotation namespace */
    private static String _SMS_SEM_ANNOT_NS = 
	"http://daks.ucdavis.edu/sms-semannot-1.0.0rc1";

} 