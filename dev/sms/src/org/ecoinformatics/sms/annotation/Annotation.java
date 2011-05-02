
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
package org.ecoinformatics.sms.annotation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;


/**
 * Objects of this class represent semantic annotations of a specific
 * dataset table.
 */
public class Annotation {

	
	static {
		initialize();
	}
	
	private static void initialize() {
		OBOE_CLASSES = new HashMap<Class, OntologyClass>();
		// initialize the static classes
		ResourceBundle smsProperties = ResourceBundle.getBundle("sms");
		Class[] classes = new Class[7];
		classes[0] = Entity.class;
		classes[1] = Characteristic.class;
		classes[2] = Standard.class;
		classes[3] = Protocol.class;
		classes[4] = Relationship.class;
		classes[5] = Measurement.class;
		classes[6] = Context.class;

		for (Class objectClass: classes) {
			String uri = smsProperties.getString(objectClass.getName());
			try {
				Object obj = objectClass.newInstance();
				OntologyClass oboeClass = null;
				if (obj instanceof OntologyClass) {
					oboeClass = (OntologyClass) obj;
				} else {
					oboeClass = new OntologyClass();
				}
				oboeClass.setURI(uri);
				Annotation.OBOE_CLASSES.put(objectClass, oboeClass);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Class getClassFromOntologyClass(OntologyClass selectedSubject) {
		// find the Java class we want for the selected OntologyClass - it is the key in the map
		Iterator<Entry<Class, OntologyClass>> subjectIter = Annotation.OBOE_CLASSES.entrySet().iterator();
		while (subjectIter.hasNext()) {
			Entry<Class, OntologyClass> entry = subjectIter.next();
			if (entry.getValue().equals(selectedSubject)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
   /** 
    * Set the uri (identifier) of this annotation
    * @param uri the uri 
    */
   public void setURI(String uri) {
      _uri = uri;
   }

   /** 
    * Get the uri (identifier) of this annotation
    * @return the uri
    */
   public String getURI() {
      return _uri;
   }

   /**
    * Set the eml package identifier of this annotation
    * @param id the package id
    */
   public void setDataPackage(String id) {
      _dataPackage = id;
   }

   /**
    * Get the package identifier of this annotation
    * @return id the package id
    */
   public String getDataPackage() {
      return _dataPackage;
   }

   /** 
    * Add an observation to this annotation
    * @param t the observation to add
    */
   public void addObservation(Observation o) {
      if(o != null && !_observations.contains(o))
         _observations.add(o);
   }

   /**
    * Remove the observation from this annotation
    * @param o the observation 
    */
   public void removeObservation(Observation o) {
      removeObservation(o, false);
   }
   
   public void removeObservation(Observation o, boolean includeContext) {
	   if (includeContext) {
		   for (Observation obs: _observations) {
				// avoid concurrent modification error
				Iterator<Context> iter = new ArrayList<Context>(obs.getContexts()).iterator();
				while (iter.hasNext()) {
					Context c = iter.next();
					if (c.getObservation() != null && c.getObservation().equals(o)) {
						obs.removeContext(c);
					}
				}
			}
	   }
	   _observations.remove(o);
   }

   /**
    * Get the observation of this annotation
    * @return the set of observation
    */
   public List<Observation> getObservations() {
      return _observations;
   }
   
   /**
    * Get the observation containing this measurement
    * @return the first observation matching the measurement
    */
   public Observation getObservation(Measurement m) {
      for (Observation o: _observations) {
    	  if (o.containsMeasurement(m)) {
    		  return o;
    	  }
      }
      return null;
   }
   
   /**
    * Get the observation containing for this label 
    * @return the observation matching the label
    */
   public Observation getObservation(String label) {
      for (Observation o: _observations) {
    	  if (o.getLabel().equals(label)) {
    		  return o;
    	  }
      }
      return null;
   }
   
   
   
   /**
    * Get the observation containing this entity
    * @return the list of observations matching the entity
    */
   public List<Observation> getObservations(Entity e) {
	   List<Observation> observations = new ArrayList<Observation>();
      for (Observation o: _observations) {
    	  if (o.getEntity().equals(e)) {
    		  observations.add(o);
    	  }
      }
      return observations;
   }

   /**
    * Get the measurement type with the given label
    * 
    * TODO: inefficient, need to discuss with Ben
    * @author cao
    * @param measurementLabel
    * @return
    */
   public Measurement getMeasurement(String measurementLabel)
   {
	   for (Observation o: _observations) {
		   List<Measurement> measurements = o.getMeasurements(); 
		   for(Measurement m: measurements){
			   if (m.getLabel().equals(measurementLabel)) {
				   return m;
			   }
		   }
	   }
	   return null;	   
   }
   
   /**
    * Get the measurement[s] with the given Characteristic[s]
    * 
    * TODO: inefficient,
    * @param characteristic
    * @return
    */
   public List<Measurement> getMeasurements(List<OntologyClass> characteristics)
   {
	   List<Measurement> results = new ArrayList<Measurement>();
	   for (Observation o: _observations) {
		   List<Measurement> measurements = o.getMeasurements(); 
		   for (Measurement m: measurements) {
			   List<Characteristic> measurementCharacteristis = m.getCharacteristics();
			   for (Characteristic c: measurementCharacteristis) {
				   if (characteristics.contains(c)) {
					   results.add(m);
				   }
			   }
		   }
	   }
	   return results;	   
   }
   
   /**
    * Get the entity type with the given label
    * 
    * TODO: inefficient, need to discuss with Ben
    * @author cao
    * @param measurementLabel
    * @return
    */
   public Entity getEntity(String entityTypeName)
   {
	   for (Observation o: _observations) {
		   Entity entityType = o.getEntity();
		   if (entityType.getName().equals(entityTypeName)) {
			   return entityType;
		   }
	   }
	   return null;	   
   }
   
   /**
    * Add a mapping to this annotation
    * @param m the mapping to add
    */
   public void addMapping(Mapping m) {
      if(m != null && !_mappings.contains(m))
         _mappings.add(m);
   }

   /**
    * Remove the mapping from this annotation
    * @param m the mapping
    */
   public void removeMapping(Mapping m) {
      _mappings.remove(m);
   }

   /**
    * Get the mappings of this annotation
    * @return the set of mappings
    */
   public List<Mapping> getMappings() {
      return _mappings;
   }
   
   /**
    * Get the mapping for the given attribute
    * @return the mapping for the given attribute
    */
   public Mapping getMapping(String attribute, String dataObject) {
	   for (Mapping mapping: _mappings) {
		   if (mapping.getAttribute().equals(attribute)) {
			   if (mapping.getDataObject().equals(dataObject)) {
				   return mapping;
			   }
		   }
	   }
	   return null;
   }

   /** 
    * Add an ontology to this annotation
    * @param o the ontology to add
    */
   public void addOntology(Ontology o) {
      if (o != null && !_ontologies.containsValue(o)) {
    	  String prefix = generatePrefix(o);
          _ontologies.put(prefix, o);
      }
   }

   /**
    * Remove the ontology from this annotation
    * @param o the ontology
    */
   public void removeOntology(Ontology o) {
      _ontologies.remove(o);
   }

   /**
    * Get the ontologies of this annotation
    * @return the set of ontologies
    */
   public Map<String, Ontology> getOntologies() {
      return _ontologies;
   }
   
   public String getOntologyPrefix(Ontology ontology) {
	   if (_ontologies.containsValue(ontology)) {
		   for (Entry<String, Ontology> entry : _ontologies.entrySet()) {
			   if (entry.getValue().equals(ontology)) {
				   return entry.getKey();
			   }
		   }
	   }
	   return null;
   }
   
   public Ontology getOntology(String prefix) {
	   return _ontologies.get(prefix);
   }
   
   private String generatePrefix(Ontology ontology) {
	   String uri = ontology.getURI();
	   String prefix = uri.substring(uri.lastIndexOf("/") + 1);
	   String generatedPrefix = prefix;
	   int counter = 1;
	   // check if we have this prefix already
	   while (getOntology(generatedPrefix) != null) {
		   generatedPrefix = prefix + "." + counter;
	   }
	   return generatedPrefix;
   }
   
   private void condenseOntologies() {
	   List<Ontology> activeOntologies = new ArrayList<Ontology>();
	   Ontology ontology = null;
	   for (Observation o: getObservations()) {
		   try {
			   ontology = o.getEntity().getOntology();
			   if (ontology != null && !activeOntologies.contains(ontology)) {
				   activeOntologies.add(ontology);
			   }
		   } catch (Exception e) {
			   ontology = null;
		   }
		   for (Measurement m: o.getMeasurements()) {
			   try {
				   ontology = m.getTemplate().getOntology();
				   if (ontology != null && !activeOntologies.contains(ontology)) {
					   activeOntologies.add(ontology);
				   }
			   } catch (Exception e) {
				   ontology = null;
			   }
			   try {
				   ontology = m.getStandard().getOntology();
				   if (ontology != null && !activeOntologies.contains(ontology)) {
					   activeOntologies.add(ontology);
				   }
			   } catch (Exception e) {
				   ontology = null;
			   }
			   try {
				   ontology = m.getProtocol().getOntology();
				   if (ontology != null && !activeOntologies.contains(ontology)) {
					   activeOntologies.add(ontology);
				   }
			   } catch (Exception e) {
				   ontology = null;
			   }			   
			   for (Characteristic c: m.getCharacteristics()) {
				   try {
					   ontology = c.getOntology();
					   if (ontology != null && !activeOntologies.contains(ontology)) {
						   activeOntologies.add(ontology);
					   }
				   } catch (Exception e) {
					   ontology = null;
				   }   
			   }
		   }
		   for (Context c: o.getContexts()) {
			   try {
				   ontology = c.getRelationship().getOntology();
				   if (ontology != null && !activeOntologies.contains(ontology)) {
					   activeOntologies.add(ontology);
				   }
			   } catch (Exception e) {
				   ontology = null;
			   }
		   }
	   }
	   // remove any that aren't being used
	   _ontologies.values().retainAll(activeOntologies);
   }

   /**
    * Get the ontology classes used in this annotation
    * @return the set of ontology classes
    */
   public List<OntologyClass> getOntologyClasses() {
      List<OntologyClass> result = new ArrayList();
      for(Observation o : getObservations()) {
         Entity e = o.getEntity();
         if(e != null && !result.contains(e))
            result.add(e);
         for(Measurement m : o.getMeasurements()) {
            Standard s = m.getStandard();
            if(s != null && !result.contains(s))
               result.add(s);
            Protocol p = m.getProtocol();
            if(p != null && !result.contains(p))
               result.add(p);
            OntologyClass template = m.getTemplate();
            if(template != null && !result.contains(template))
               result.add(template);
            for(Characteristic c : m.getCharacteristics())
               if(!result.contains(c))
                  result.add(c);
            for(Entity v : m.getDomainValues())
               if(!result.contains(v))
                  result.add(v);
         }
         for(Context x : o.getContexts()) {
            OntologyClass r = x.getRelationship();
            if(r != null && !result.contains(r))
               result.add(r);
         }
      }
      return result;
   }

   /**
    * Write this annotation to an output stream
    * @param s the output stream
    */
   public void write(OutputStream s) {
	   condenseOntologies();
      AnnotationWriter.write(this, s);
   }

   /** 
    * Read an annotation from an input stream
    * @param s the input stream
    * @return an annotation
    */
   public static Annotation read(InputStream s) throws AnnotationException {
	   AnnotationReader ar = new AnnotationReader();
	   return ar.read(s);
   }

   public static String getNextMeasurementLabel(Annotation annotation, String prefix) {
	   int counter = 1;
	   String retLabel = prefix + counter++;
	   if (annotation != null) {
		   for (Observation o: annotation.getObservations()) {
			   for (Measurement m: o.getMeasurements()) {
				   String label = m.getLabel();
				   if (retLabel.equals(label)) {
					   retLabel = prefix + counter++;
				   }
			   }
		   }
	   }
	   return retLabel;
   }
   
   public static String getNextObservationLabel(Annotation annotation, String prefix) {
	   int counter = 1;
	   String retLabel = prefix + counter++;
	   if (annotation != null) {
		   for (Observation o: annotation.getObservations()) {
			   String label = o.getLabel();
			   if (retLabel.equals(label)) {
				   retLabel = prefix + counter++;
			   }
		   }
	   }
	   return retLabel;
   }
   
   /**
    * Get the XML representation of this annotation
    * @return XML string
    */
   @Override
   public String toString() {
      ByteArrayOutputStream s = new ByteArrayOutputStream();
      AnnotationWriter.write(this, s);
      return s.toString();
   }

   private String _uri;
   private String _dataPackage;
   private Map<String, Ontology> _ontologies = new HashMap<String, Ontology>();
   private List<Observation> _observations = new ArrayList();
   private List<Mapping> _mappings = new ArrayList();
   
   /**
    * Maps an Annotation Java Class to a specific OBOE OntologyClass
    */
   public static Map<Class,OntologyClass> OBOE_CLASSES;

   public static String ANNOTATION_NS =
      "http://ecoinformatics.org/sms/annotation.1.0beta1";

   public static void main(String[] args) throws Exception {
      System.out.println("args[0] = " + args[0]);
      InputStream in =
         new java.io.FileInputStream(new java.io.File(args[0]));
      Annotation a = Annotation.read(in);
      System.out.println("===============");
      System.out.println("Annotation: " + a.getURI());
      System.out.println("===============");
      System.out.println(a);
      System.out.println("===============");
      for(OntologyClass item : a.getOntologyClasses())
         System.out.println(item.getURI());
   }

} 
