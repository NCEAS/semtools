
/**
 *    '$Id$'
 *
 *     '$Author$'
 *       '$Date$'
 *   '$Revision$'
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
package org.ecoinformatics.sms.annotation.search;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.AnnotationException;
import org.ecoinformatics.sms.annotation.Triple;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;

public class CriteriaReader {

   /**
    * Read in a new annotation from an input stream
    * @param in the input stream
    * @return the parsed annotation
    */
   public static Criteria read(InputStream in) throws AnnotationException {
      Criteria criteria = null;
      try {
         Document doc = _getDocument(in);
         criteria = _parseQuery(doc);
      }catch(Exception e) {
         e.printStackTrace();
         throw new AnnotationException(e.getMessage());
      }
      return criteria;
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
      
      return doc;
   }

   /**
    * Parse an annotation element
    * @param doc the document
    */
   private static Criteria _parseQuery(Document doc) throws Exception {
      Element root = doc.getDocumentElement();
      Criteria criteria = new Criteria();
      List<Criteria> subCriteria = new ArrayList<Criteria>();
	  criteria.setSubCriteria(subCriteria);
	  criteria.setGroup(true);
	   
      // parse the criteria elements
      for (Element elem : _getElements(root, "AND")) {
         Criteria c = _parseCriteria(elem);
         c.setAll(true);
         criteria.getSubCriteria().add(c);
      }
      for (Element elem : _getElements(root, "OR")) {
    	  Criteria c = _parseCriteria(elem);
          c.setAll(false);
          criteria.getSubCriteria().add(c);
      }
      for (Element elem : _getElements(root, "condition")) {
          Criteria c = _parseCondition(elem);
          criteria.getSubCriteria().add(c);
      }
      for (Element elem : _getElements(root, "context")) {
    	  Criteria c = _parseContext(elem);
    	  criteria.getSubCriteria().add(c);
      }
      // remove top group if not needed
      if (criteria.getSubCriteria().size() == 1) {
    	  criteria = criteria.getSubCriteria().get(0);
      }
      return criteria;
   }
   
   /**
    * Parse 
    * @param doc the document
    */
   private static Criteria _parseCriteria(Element root) throws Exception {
	   Criteria criteria = new Criteria();
	   List<Criteria> subCriteria = new ArrayList<Criteria>();
	   criteria.setSubCriteria(subCriteria);
	   criteria.setGroup(true);
	   
	   // parse the criteria elements
		for (Element elem : _getElements(root, "AND")) {
			Criteria c = _parseCriteria(elem);
			c.setAll(true);
			criteria.getSubCriteria().add(c);
		}
		for (Element elem : _getElements(root, "OR")) {
			Criteria c = _parseCriteria(elem);
			c.setAll(false);
			criteria.getSubCriteria().add(c);
		}
		for (Element elem : _getElements(root, "condition")) {
			Criteria c = _parseCondition(elem);
			criteria.getSubCriteria().add(c);
		}
		for (Element elem : _getElements(root, "context")) {
			Criteria c = _parseContext(elem);
			criteria.getSubCriteria().add(c);
		}
		// remove top group if not needed
	      if (criteria.getSubCriteria().size() == 1) {
	    	  criteria = criteria.getSubCriteria().get(0);
	      }
		return criteria;
   }
   
   /**
    * Parse the context node
    * @root the document root
    */
   private static Criteria _parseContext(Element root) throws Exception {
	   Criteria criteria = new Criteria();
	   criteria.setContext(true);

	   //get the parts of the context relationships
	   List<Element> conditions = _getElements(root, "condition");
	   Element a = conditions.get(0);
	   Element b = conditions.get(1);
	   Element c = conditions.get(2);
	   
	   Triple contextTriple = new Triple();
	   contextTriple.a = new OntologyClass(a.getTextContent());
	   contextTriple.b = new OntologyClass(b.getTextContent());
	   contextTriple.c = new OntologyClass(c.getTextContent());

	   criteria.setContextTriple(contextTriple);
	   return criteria;
   }

   /**
    * Parse an condition element
    * @param o the observation
    * @param e the element
    */
   private static Criteria _parseCondition(Element e) throws Exception {
      Criteria criteria = new Criteria();
      criteria.setGroup(false);
      
      // subject
      Attr concept = _getAttribute(e, "concept");
      OntologyClass conceptClass = new OntologyClass(concept.getValue());
      Class subject = AnnotationPlugin.getClassFromOntologyClass(conceptClass);
      criteria.setSubject(subject);

      // condition
      Attr operator = _getAttribute(e, "operator");
      criteria.setCondition(operator.getValue());
      
      // value
      String value = e.getTextContent();
      OntologyClass valueClass = new OntologyClass(value);
      criteria.setValue(valueClass);
      
      return criteria;
   }

   /** 
    * Get the URI associated with a namespace prefix
    * @param elem the element to start searching from
    * @param prefix the prefix string to search for
    * @return the uri or null if no such prefix is used
    */
   private static String _getURI(Element elem, String prefix)
           throws Exception {
      Object parent = elem.getParentNode();
      if(elem.hasAttribute("xmlns:" + prefix))
         return elem.getAttribute("xmlns:" + prefix);
      else if(parent != null && parent instanceof Element)
         return _getURI((Element) parent, prefix);
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
      String[] s = tag.split(":");
      if(s.length == 2)
         return _getURI(elem, s[0]);
      if(elem.hasAttribute("xmlns"))
         return elem.getAttribute("xmlns");
      else if(parent != null && parent instanceof Element)
         return _getNamespace((Element) parent);
      return null;
   }


   /**
    * Get the child elements with the given case insensitive tag
    * name.
    * @param p the parent element
    * @param t the element tag name to match 
    * @return child elements with matching name
    */
   private static List<Element> _getElements(Element parent, String tag)
           throws Exception {
      ArrayList<Element> elems = new ArrayList();
      if(tag == null)
         return elems;
      tag = tag.toLowerCase();
      NodeList children = parent.getChildNodes();
      for(int i = 0; i < children.getLength(); i++) {
         Node node = children.item(i);
         if(node instanceof Element) {
            Element child = (Element) node;
            String childTag = _getElementTag(child);
            if(childTag != null && childTag.equals(tag))
               elems.add(child);
         }
      }
      return elems;
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
      if(ns == null) {
         _error("element missing '" + Annotation.ANNOTATION_NS +
                 "' namespace:" + elem.getTagName());
      }
      String[] s = elem.getTagName().split(":");
      if(s.length == 2)
         return s[1].toLowerCase();
      return s[0].toLowerCase();
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
         Attr att = (Attr) as.item(i);
         String name = att.getName().toLowerCase();
         if(name != null && name.equals(a))
            return att;
      }
      return null;
   }

   /**
    * Throw an exception with the given message
    * @param msg the message
    */
   private static void _error(String msg) throws Exception {
      throw new Exception("ERROR: " + msg);
   }
   
   public static void main(String[] args) {
	   try {
		   InputStream in = new FileInputStream(args[0]);
		   Criteria criteria = CriteriaReader.read(in);
		   System.out.println(criteria);
	   } catch (Exception e) {
		e.printStackTrace();
	}
	   
   }

} 