
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

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.OntologyManager;
import org.ecoinformatics.sms.AnnotationManager;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.ontology.OntologyClass;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public class DefaultAnnotationManager implements AnnotationManager {

   protected SMS _sms;
   protected HashMap<String, Annotation> _annotations = new HashMap();

   /**
    * Default constuctor
    */
   public DefaultAnnotationManager(SMS sms) {
      _sms = sms;
   }

   /**
    * Import an annotation into the manager
    * NOTE: the source is currently ignored in this implementation
    * @param r the semantic annotation 
    * @param source location of the annotation
    */
   public void importAnnotation(InputStream is, String source) throws Exception {
      importAnnotation(Annotation.read(is), source);
   }
   
   /**
    * Import an annotation into the manager
    * NOTE: the source is currently ignored in this implementation
    * @param r the semantic annotation 
    * @param source location of the annotation
    */
   public void importAnnotation(Annotation ann, String source) throws Exception {
      _annotations.put(ann.getURI(), ann);
   }

   /**
    * Export an annotation from the manager
    * @param the identiifer of the annotation
    */
   public void exportAnnotation(String id, OutputStream os) throws Exception {
      if(!isAnnotation(id)) {
         String msg = "annotation id '" + id + "' does not exist";
         throw new Exception(msg);
      }
      getAnnotation(id).write(os);
   }

   /**
    * Remove an annotation from the manager
    * @param id the identifier of the annotation
    */
   public void removeAnnotation(String id) throws Exception {
      if(!isAnnotation(id)) {
         String msg = "annotation id '" + id + "' does not exist";
         throw new Exception(msg);
      }
      _annotations.remove(id);
   }

   /**
    * Check if the identifier is assigned to an annotation in the
    * manager
    * @return true if the id is assigned to an annotation in the manager
    * @param id the annotation identifier
    */
   public boolean isAnnotation(String id) {
      return _annotations.containsKey(id);
   }

   /**
    * Get an annotation from the manager
    * @param the identiifer of the annotation
    */
   public Annotation getAnnotation(String id) throws Exception {
      if(!isAnnotation(id)) {
         String msg = "annotation id '" + id + "' does not exist";
         throw new Exception(msg);
      }
      return _annotations.get(id);
   }

   /**
    * Ensure that the annotation is valid.
    * @param a the annotation
    * @throws java.lang.Exception
    */
   public void validateAnnotation(Annotation a) throws Exception {
      String msg = "ERROR: annotation";
      OntologyManager mgr = _sms.getOntologyManager();
      if(a == null)
         throw new Exception(msg + " is null");
      for(OntologyClass c : a.getOntologyClasses()) {
         if(c == null)
            throw new Exception(msg + " contains null class");
         if(c.getOntology() == null)
            throw new Exception(msg + " class with null ontology: " +
                    c.toString());
         String ont_uri = c.getOntology().getURI();
         if(!mgr.isOntology(ont_uri))
            throw new Exception(msg + " class with unknown ontology: " +
                    ont_uri);
         if(c.getName() == null)
            throw new Exception(msg + " class with unknown name: " +
                    c.toString());
         String name = c.getName();
         if(mgr.getNamedClass(c.getOntology(), name) == null)
            throw new Exception(msg + " with unknown class: " + c.toString());
      }
   }

   /**
    * Get the annotation identifiers from the manager
    * @return the set of annotation identifiers
    */
   public List<String> getAnnotationIds() {
      return new ArrayList(_annotations.keySet());
   }

   /**
    * Get the annotations fromt he manager
    * @return the annotations
    */
   public List<Annotation> getAnnotations() {
      List<Annotation> results = new ArrayList();
      for(String id : _annotations.keySet()) {
         Annotation a = null;
         try {
            a = getAnnotation(id);
            results.add(a);
         }catch(Exception e) {
            e.printStackTrace();
         }
      }
      return results;
   }
   
   /**
    * Get the annotations matching the EML package and optionally the data table
    * Use null for datatable to include all annotations
    * @param package id
    * @param the optional data table to match
    * @return the annotations
    */
   public List<Annotation> getAnnotations(String dataPackage) {
      List<Annotation> results = new ArrayList<Annotation>();
      for(String id : _annotations.keySet()) {
         Annotation a = null;
         try {
            a = getAnnotation(id);
            if (a.getDataPackage().equals(dataPackage)) {
            	results.add(a);
            }
         }catch(Exception e) {
            e.printStackTrace();
         }
      }
      return results;
   }

   /**
    * Get all ontology classes used for a specific annotation
    * @param a the annotation
    * @return the ontology classes used in an annotation
    */
   public List<OntologyClass> getOntologyClasses(Annotation a) {
      return a.getOntologyClasses();
   }

   /**
    * Get all ontology classes used for a specific annotation
    * @param a the annotation
    * @param addSubclasses if true, add all subclasses to result
    * @param addSuperclasses if true, add all superclasses to result
    * @return the ontology classes used in an annotation
    */
   public List<OntologyClass> getOntologyClasses(Annotation a,
           boolean addSubclasses, boolean addSuperclasses) {
      List<OntologyClass> result = getOntologyClasses(a);
      List<OntologyClass> subclasses = new ArrayList(result);
      addSubclasses(subclasses);
      List<OntologyClass> superclasses = new ArrayList(result);
      addSuperclasses(superclasses);
      for(OntologyClass c : subclasses)
         if(!result.contains(c))
            result.add(c);
      for(OntologyClass c : superclasses)
         if(!result.contains(c))
            result.add(c);
      return result;
   }
   
   public List<Annotation> getMatchingAnnotations(Criteria criteria) {
	   //let the fun begin!
	   List<Annotation> results = new ArrayList<Annotation>();
	   for(Annotation annotation : getAnnotations()) {
		   int match = matchesCriteria(annotation, criteria);
		   if (match < 0) {
			   continue;
		   }
		   results.add(annotation);
	   }
	   return results;
   }


   /**
    * Get annotations that contain an entity in the given list and a 
    * measurement with a characteristic and standard in the given lists
    * @param entities the entity class URI's to search for
    * @param characteristics the characteristic class URI's to search for
    * @param standards the measurement standard class URI's to search for
    * @return the matching annotations
    */
   public List<Annotation> getMatchingAnnotations(List<OntologyClass> entities,
           List<OntologyClass> characteristics, List<OntologyClass> standards,
           List<OntologyClass> protocols, List<Triple> contexts) {
	   int criteria = 5;
	   if (entities == null || entities.isEmpty()) {
		   criteria--;
	   }
	   if (characteristics == null || characteristics.isEmpty()) {
		   criteria--;
	   }
	   if (standards == null || standards.isEmpty()) {
		   criteria--;
	   }
	   if (protocols == null || protocols.isEmpty()) {
		   criteria--;
	   }
	   if (contexts == null || contexts.isEmpty()) {
		   criteria--;
	   }
      // find matches
      List<Annotation> rankedResults = new ArrayList<Annotation>();
      SortedMap<Integer, List<Annotation>> rankedResultMap = new TreeMap<Integer, List<Annotation>>();
      for(Annotation annot : getAnnotations()) {
    	 // decremented for each type of match
    	 int rank = criteria;
    	 int weight = -1;
         weight = hasMatchingEntity(annot, entities);
         if(weight < 0) {
            //continue;
         }
         rank -= weight;
         weight = hasMatchingCharacteristic(annot, characteristics);
         if (weight < 0) {
            //continue;
         }
         rank -= weight;
         weight = hasMatchingStandard(annot, standards);
         if (weight < 0) {
            //continue;
         }
         rank -= weight;
         weight = hasMatchingProtocol(annot, protocols);
         if (weight < 0) {
             //continue;
         }
         rank -= weight;
         weight = hasMatchingContext(annot, contexts);
         if (weight < 0) {
             //continue;
         }
         rank -= weight;
         // criteria were given and it didn't match any of the criteria
         if (rank >= (criteria*2) && criteria != 0) {
        	 continue;
         }
         // put the result in the correct bucket
         List<Annotation> results = rankedResultMap.get(rank);
         if (results == null) {
        	 results = new ArrayList<Annotation>();
        	 rankedResultMap.put(rank, results);
         }
         if(!results.contains(annot)) {
            results.add(annot);
         }
      }
      // combine the results in order from the map
      for (List<Annotation> results: rankedResultMap.values()) {
    	  rankedResults.addAll(results);
      }
      return rankedResults;
   }

   /**
    * Get annotations that contain an entity in the given list and a 
    * measurement with a characteristic and standard in the given lists
    * @param entities the entity classes to search for
    * @param characteristics the characteristic classes to search for
    * @param standards the measurement standard classes to search for
    * @param searchSubclasses if true, search subclasses of the given classes
    * @return the matching annotations
    */
   public List<Annotation> getMatchingAnnotations(List<OntologyClass> entities,
           List<OntologyClass> characteristics, List<OntologyClass> standards,
           List<OntologyClass> protocols, List<Triple> contexts, boolean searchSubclasses) {
      if(!searchSubclasses)
         return getMatchingAnnotations(entities, characteristics, standards, protocols, contexts);
      List<OntologyClass> entSubs = new ArrayList(entities);
      List<OntologyClass> charSubs = new ArrayList(characteristics);
      List<OntologyClass> stdSubs = new ArrayList(standards);
      List<OntologyClass> protSubs = new ArrayList(protocols);
      // TODO: add subclasses to the triples
      List<Triple> contextSubs = new ArrayList<Triple>(contexts);
      addSubclasses(entSubs);
      addSubclasses(charSubs);
      addSubclasses(stdSubs);
      addSubclasses(protSubs);
      return getMatchingAnnotations(entSubs, charSubs, stdSubs, protSubs, contextSubs);
   }

   /**
    * Get annotations that contain the given entity, characteristic, and standard
    * @param entity the entity class to search for
    * @param characteristic the characteristic classto search for
    * @param standard the measurement standard class to search for
    * @return the matching annotations
    */
   public List<Annotation> getMatchingAnnotations(OntologyClass entity,
           OntologyClass characteristic, OntologyClass standard, OntologyClass protocol,
           Triple context) {
      List<OntologyClass> entities = new ArrayList();
      if(entity != null)
         entities.add(entity);
      List<OntologyClass> characteristics = new ArrayList();
      if(characteristic != null)
         characteristics.add(characteristic);
      List<OntologyClass> standards = new ArrayList();
      if(standard != null)
         standards.add(standard);
      List<OntologyClass> protocols = new ArrayList();
      if(protocol != null)
         protocols.add(protocol);
      List<Triple> contexts = new ArrayList();
      if(context != null)
         contexts.add(context);
      return getMatchingAnnotations(entities, characteristics, standards, protocols, contexts);
   }

   /**
    * Get annotations that contain the given entity, characteristic, and standard
    * @param entity the entity class to search for
    * @param characteristic the characteristic classto search for
    * @param standard the measurement standard class to search for
    * @param searchSubclasses if true, search subclasses of the given classes
    * @return the matching annotations
    */
   public List<Annotation> getMatchingAnnotations(OntologyClass entity,
           OntologyClass characteristic, OntologyClass standard, 
           OntologyClass protocol, Triple context, boolean searchSubclasses) {
      List<OntologyClass> entities = new ArrayList();
      if(entity != null)
         entities.add(entity);
      List<OntologyClass> characteristics = new ArrayList();
      if(characteristic != null)
         characteristics.add(characteristic);
      List<OntologyClass> standards = new ArrayList();
      if(standard != null)
         standards.add(standard);
      List<OntologyClass> protocols = new ArrayList();
      if(protocol != null)
         protocols.add(protocol);
      List<Triple> contexts = new ArrayList();
      if(context != null)
         contexts.add(context);
      return getMatchingAnnotations(entities, characteristics, standards, protocols, contexts, searchSubclasses);
   }

   /**
    * Get entities used in managed annotations
    * @return list of entities
    */
   public List<OntologyClass> getActiveEntities() {
      List<OntologyClass> results = new ArrayList();
      for(Annotation a : getAnnotations())
         for(OntologyClass c : a.getOntologyClasses())
            if(c instanceof Entity && !results.contains(c))
               results.add(c);
      return results;
   }

   /**
    * Get entities used in managed annotations
    * @param addSuperclasses if true, include all superclasses of active 
    * entities
    * @return list of entities
    */
   public List<OntologyClass> getActiveEntities(boolean addSuperclasses) {
      List<OntologyClass> results = getActiveEntities();
      if(!addSuperclasses)
         return results;
      addSuperclasses(results);
      return results;
   }

   /**
    * Get characteristics used in managed annotations
    * @return list of characteristics
    */
   public List<OntologyClass> getActiveCharacteristics() {
      List<OntologyClass> results = new ArrayList();
      for(Annotation a : getAnnotations())
         for(OntologyClass c : a.getOntologyClasses())
            if(c instanceof Characteristic && !results.contains(c))
               results.add(c);
      return results;
   }

   /**
    * Get characteristics used in managed annotations
    * @param addSuperclasses if true, include all superclasses of active 
    * characteristics
    * @return list of characteristics
    */
   public List<OntologyClass> getActiveCharacteristics(
           boolean addSuperclasses) {
      List<OntologyClass> results = getActiveCharacteristics();
      if(!addSuperclasses)
         return results;
      addSuperclasses(results);
      return results;
   }

   /**
    * Get measurement standards used in managed annotations
    * @return list of measurement standards
    */
   public List<OntologyClass> getActiveStandards() {
      List<OntologyClass> results = new ArrayList();
      for(Annotation a : getAnnotations())
         for(OntologyClass c : a.getOntologyClasses())
            if(c instanceof Standard && !results.contains(c))
               results.add(c);
      return results;
   }

   /**
    * Get standards used in managed annotations
    * @param addSuperclasses if true, include all superclasses of active 
    * standards
    * @return list of standards
    */
   public List<OntologyClass> getActiveStandards(boolean addSuperclasses) {
      List<OntologyClass> results = getActiveStandards();
      if(!addSuperclasses)
         return results;
      addSuperclasses(results);
      return results;
   }

   /**
    * Get entities used in managed annotations having a measurement with a 
    * characteristic and standard in the given characteristics and standards
    * @param characteristics characteristics to look for
    * @param standards
    * @return list of matching entities
    */
   public List<OntologyClass> getActiveEntities(
           List<OntologyClass> characteristics, 
           List<OntologyClass> standards,
           List<OntologyClass> protocols,
           boolean searchSubclasses,
           boolean addSuperclasses) {
      // check if both args are missing
      if((characteristics == null || characteristics.isEmpty()) &&
              (standards == null || standards.isEmpty()))
         return getActiveEntities();
      List<OntologyClass> results = new ArrayList();
      for(Annotation a : getAnnotations())
         for(Observation o : a.getObservations()) {
            boolean found = false;
            for(Measurement m : o.getMeasurements()) {
               if(standards != null && !standards.isEmpty() && !standards.contains(m.getStandard()))
                  continue;
               if(characteristics == null || characteristics.isEmpty()) {
                  found = true;
                  break;
               }
               for(Characteristic c : m.getCharacteristics())
                  if(characteristics.contains(c)) {
                     found = true;
                     break;
                  }
               if(found)
                  break;
            }
            if(found)
               for(Entity e : getAllEntities(o))
                  if(!results.contains(e))
                     results.add(e);
         }
      return results;
   }


   /**
    * Get characteristics used in managed annotations having a measurement 
    * with an entity and standard in the given entities and standards
    * @param entities entities to look for
    * @param standards standards to look for
    * @return list of matching characteristics
    */
   public List<OntologyClass> getActiveCharacteristics(
           List<OntologyClass> entities, 
           List<OntologyClass> standards, 
           List<OntologyClass> protocols,
           boolean searchSubclasses,
           boolean addSuperclasses) {
      // check if both args are missing
      if((entities == null || entities.isEmpty()) &&
              (standards == null || standards.isEmpty()) && 
              (protocols == null || protocols.isEmpty()))
         return getActiveCharacteristics();
      
      // add subclasses?
      if (searchSubclasses) {
    	  addSuperclasses(entities);
    	  addSuperclasses(standards);
    	  addSuperclasses(protocols);
      }
      // TODO: handle protocol
      List<OntologyClass> results = new ArrayList();
      for(Annotation a : getAnnotations())
         for(Observation o : a.getObservations()) {
            if(entities != null && !entities.isEmpty() && 
                    !overlaps(entities, getAllEntities(o))) 
               continue;
            for(Measurement m : o.getMeasurements()) {
               if (standards != null && !standards.isEmpty() &&
                       !standards.contains(m.getStandard()))
                  continue;
               // found a match
               for(Characteristic c : m.getCharacteristics())
                  if(!results.contains(c))
                     results.add(c);
            }
         }
      if (addSuperclasses) {
    	  addSuperclasses(results);
      }
      return results;
   }

   /**
    * Get standards used in managed annotations having a measurement 
    * with an entity and characteristic in the given entities and 
    * characteristics
    * @param entities entities to look for
    * @param characteristics characteristics to look for
    * @return list of matching standards
    */
   public List<OntologyClass> getActiveStandards(
           List<OntologyClass> entities, 
           List<OntologyClass> characteristics,
           List<OntologyClass> protocols,
           boolean searchSubclasses, 
           boolean addSuperclasses) {
      // check if both args are missing
      if((entities == null || entities.isEmpty()) &&
              (characteristics == null || characteristics.isEmpty()) &&
              	(protocols == null || protocols.isEmpty()))
         return getActiveStandards();
      if (searchSubclasses) {
    	  addSubclasses(entities);
    	  addSubclasses(characteristics);
    	  addSubclasses(protocols);
      }
      List<OntologyClass> results = new ArrayList();
      for(Annotation a : getAnnotations())
         for(Observation o : a.getObservations()) {
            if(entities != null && !entities.isEmpty() && 
                    !overlaps(entities, getAllEntities(o))) 
               continue;
            for(Measurement m : o.getMeasurements()) {
               boolean found = false;
               if(characteristics != null && !characteristics.isEmpty()) {
                  for(Characteristic c : m.getCharacteristics())
                     if(characteristics.contains(c)) {
                        found = true;
                        break;
                     }
               }else
                  found = true;
               Standard s = m.getStandard();
               if(found && !results.contains(s))
                  results.add(s);
            }
         }
      if (addSuperclasses) {
    	  addSuperclasses(results);
      }
      return results;
   }
   
   /**
    * Get protocols used in managed annotations
    * @return list of protocols
    */
   public List<OntologyClass> getActiveProtocols() {
      List<OntologyClass> results = new ArrayList();
      for(Annotation a : getAnnotations())
         for(OntologyClass c : a.getOntologyClasses())
            if(c instanceof Protocol && !results.contains(c))
               results.add(c);
      return results;
   }
   
   /**
    * Get Protocols used in managed annotations having a measurement 
    * with an entity and characteristic in the given entities and 
    * characteristics
    * @param entities entities to look for
    * @param characteristics characteristics to look for
    * @return list of matching protocols
    */
   public List<OntologyClass> getActiveProtocols(
           List<OntologyClass> entities, 
           List<OntologyClass> characteristics,
           List<OntologyClass> standards,
           boolean searchSubclasses, 
           boolean addSuperclasses) {
      // check if both args are missing
      if((entities == null || entities.isEmpty()) &&
              (characteristics == null || characteristics.isEmpty()) &&
              (standards == null || standards.isEmpty()))
         return getActiveStandards();
      if (searchSubclasses) {
    	  addSubclasses(entities);
    	  addSubclasses(characteristics);
    	  addSubclasses(standards);
      }
      List<OntologyClass> results = new ArrayList();
      for(Annotation a : getAnnotations())
         for(Observation o : a.getObservations()) {
            if(entities != null && !entities.isEmpty() && 
                    !overlaps(entities, getAllEntities(o))) 
               continue;
            for(Measurement m : o.getMeasurements()) {
               boolean found = false;
               if(characteristics != null && !characteristics.isEmpty()) {
                  for(Characteristic c : m.getCharacteristics())
                     if(characteristics.contains(c)) {
                        found = true;
                        break;
                     }
               }else
                  found = true;
               Protocol p = m.getProtocol();
               if(found && !results.contains(p))
                  results.add(p);
            }
         }
      if (addSuperclasses) {
    	  addSuperclasses(results);
      }
      return results;
   }
   
   /**
    * Get measurements used in managed annotations
    * @return list of measurements
    */
   public List<OntologyClass> getActiveMeasurements() {
      List<OntologyClass> results = new ArrayList();
      for(Annotation a : getAnnotations())
         for(OntologyClass c : a.getOntologyClasses()) {
        	 if(c instanceof Entity) {
        		continue; 
        	 }
        	 if(c instanceof Characteristic) {
         		continue; 
         	 }
        	 if(c instanceof Standard) {
         		continue; 
         	 }
        	 if(c instanceof Protocol) {
         		continue; 
         	 }
        	 if(!results.contains(c)) {
        		 results.add(c);
        	}
         }
      return results;
   }
   
   /** 
    * This method is called recursively on the subcriteria, building up a weighted return
    * @param a the Annotation to be evaluated for matching
    * @param criteria potentially complex 
    * @return
    */
   private int matchesCriteria(Annotation a, Criteria criteria) {
	   
	   //simple first case
	   if (!criteria.isGroup()) {
		   // everything in a list
			List<OntologyClass> characteristics = new ArrayList<OntologyClass>();
			List<OntologyClass> standards = new ArrayList<OntologyClass>();
			List<OntologyClass> protocols = new ArrayList<OntologyClass>();
			List<OntologyClass> entities = new ArrayList<OntologyClass>();
			List<Triple> contexts = new ArrayList<Triple>();
			
			// try context
			if (criteria.isContext()) {
				Triple context = criteria.getContextTriple();
				if (context != null) {
					contexts.add(context);
					return hasMatchingContext(a, contexts);
				}
			} else {
				// what criteria was given?
				Class type = criteria.getType();
				OntologyClass value = criteria.getSubject();
				if (value == null) {
					return 0;
				}
				if (type != null && type.equals(Entity.class)) {
					entities.add(value);
					return hasMatchingEntity(a, entities);
				}
				if (type != null && type.equals(Characteristic.class)) {
					characteristics.add(value);
					return hasMatchingCharacteristic(a, characteristics);
				}
				if (type != null && type.equals(Standard.class)) {
					standards.add(value);
					return hasMatchingStandard(a, standards);
				}
				if (type != null && type.equals(Protocol.class)) {
					protocols.add(value);
					return hasMatchingProtocol(a, protocols);
				}
				// expand the measurement template if given
				if (type != null && type.equals(Measurement.class)) {
					List<OntologyClass> classes = null;
					// entity
					classes = Measurement.lookupRestrictionClasses(value, Entity.class);
					if (classes != null) {
						entities.addAll(classes);
					}
					// characteristic
					classes = Measurement.lookupRestrictionClasses(value, Characteristic.class);
					if (classes != null) {
						characteristics.addAll(classes);
					}
					// standard
					classes = Measurement.lookupRestrictionClasses(value, Standard.class);
					if (classes != null) {
						standards.addAll(classes);
					}
					// protocol
					classes = Measurement.lookupRestrictionClasses(value, Protocol.class);
					if (classes != null) {
						protocols.addAll(classes);
					}
				}
			}
	   }
	   else {
		   // iterate through the subcriteria, keeping track of the match weight
		   int weight = 0;
		   if (criteria.getSubCriteria() != null) {
			   for (Criteria subcriteria: criteria.getSubCriteria()) {
				   // recurse here
				   int match = matchesCriteria(a, subcriteria);
				   // if we require perfect matches, then stop if we missed one
				   if (match < 0 && criteria.isAll()) {
					   return -1;
				   }
				   // otherwise keep going
				   weight += match;
			   }
		   }
		   return weight;
	   }
	   
	   return -1;
   }
   
   /**
    * Helper method to check if an annotation has a matching context
    * @param a the annotation
    * @param contexts list of context triples to check
    * @return int 
    * 	-1 for no matches
    * 	0 if no criteria are given to match
    * 	1 for a match
    */
   private int hasMatchingContext(Annotation a, List<Triple> contexts) {
      if(contexts == null || contexts.size() == 0) {
         return 0;
      }
      // construct all the general triples in this annotation
      for(Observation o : a.getObservations()) {
         for(Context c : o.getContexts()) {
        	 Triple triple = new Triple();
        	 triple.a = o.getEntity();
        	 triple.b = c.getRelationship();
        	 triple.c = c.getObservation().getEntity();
        	 // is there a match in the given list?
        	 boolean match = triple.hasMatch(contexts);
        	 if (match) {
        		 return 1;
        	 }
         }
      }
      return -1;
   }

   /**
    * Helper method to check if an annotation has a matching entity
    * @param a the annotation
    * @param entities entities to check
    * @return int 
    * 	-1 for no matches
    * 	0 if no criteria are given to match
    * 	1 for a match
    */
   private int hasMatchingEntity(Annotation a, List<OntologyClass> entities) {
      if(entities == null || entities.size() == 0)
         return 0;
      for(Observation o : a.getObservations())
         if(entities.contains(o.getEntity()))
            return 1;
      for(Observation o : a.getObservations())
         for(Measurement m : o.getMeasurements())
            for(Entity v : m.getDomainValues())
               if(entities.contains(v))
                  return 1;
      return -1;
   }

   /**
    * Helper method to check if an annotation has a matching characteristic
    * @param a the annotation
    * @param chars the characteristics to check
    * @return int 
    * 	-1 for no matches
    * 	0 if no criteria are given to match
    * 	1 for a match
    */
   private int hasMatchingCharacteristic(Annotation a, List<OntologyClass> chars) {
	   if(chars == null || chars.size() == 0)
         return 0;
      for(Observation o : a.getObservations())
         for(Measurement m : o.getMeasurements())
            for(Characteristic c : m.getCharacteristics())
               if(chars.contains(c))
                  return 1;
      return -1;
   }

   /**
    * Helper method to check if an annotation has a matching standard
    * @param a the annotation
    * @param standards the standards to check
    * @return int 
    * 	-1 for no matches
    * 	0 if no criteria are given to match
    * 	1 for a match
    */
   private int hasMatchingStandard(Annotation a, List<OntologyClass> standards) {
	  if(standards == null || standards.size() == 0)
         return 0;
      for(Observation o : a.getObservations())
         for(Measurement m : o.getMeasurements())
            if(standards.contains(m.getStandard()))
               return 1;
      return -1;
   }

   /**
    * Helper method to check if an annotation has a matching standard
    * @param a the annotation
    * @param standards the standards to check
    * @return int 
    * 	-1 for no matches
    * 	0 if no criteria are given to match
    * 	1 for a match
    */
   private int hasMatchingProtocol(Annotation a, List<OntologyClass> protocols) {
      if(protocols == null || protocols.size() == 0)
         return 0;
      for(Observation o : a.getObservations())
         for(Measurement m : o.getMeasurements())
            if(protocols.contains(m.getProtocol()))
               return 1;
      return -1;
   }
   
   /** 
    * Helper method to add all superclasses of the given classes
    * @param classes the class list being added to
    */
   protected void addSuperclasses(List<OntologyClass> classes) {
	   if (classes == null) {
		   return;
	   }
      OntologyManager ontMgr = _sms.getOntologyManager();
      List<OntologyClass> superclasses = new ArrayList();
      for(OntologyClass c : classes)
         for(OntologyClass s : ontMgr.getNamedSuperclasses(c))
            if(!superclasses.contains(s))
               superclasses.add(s);
      for(OntologyClass c : superclasses)
         if(!classes.contains(c))
            classes.add(c);
   }

   /** 
    * Helper method to add all subclasses of the given classes
    * @param classes the class list being added to
    */
   protected void addSubclasses(List<OntologyClass> classes) {
	   if (classes == null) {
		   return;
	   }
      OntologyManager ontMgr = _sms.getOntologyManager();
      List<OntologyClass> subclasses = new ArrayList();
      for(OntologyClass c : classes)
         for(OntologyClass s : ontMgr.getNamedSubclasses(c, true))
            if(!subclasses.contains(s))
               subclasses.add(s);
      for(OntologyClass c : subclasses)
         if(!classes.contains(c))
            classes.add(c);
   }

   /** 
    * Helper method to get all entities of an observation
    * @param o the observation
    */
   protected List<Entity> getAllEntities(Observation o) {
      List<Entity> result = new ArrayList();
      if(o.getEntity() != null)
         result.add(o.getEntity());
      for(Measurement m : o.getMeasurements())
         for(Entity d : m.getDomainValues())
            if(!result.contains(d))
               result.add(d);
      return result;
   }

   /**
    * Helper method to check if two lists overlap
    * @param first first list
    * @param second second list
    * @return
    */
   protected boolean overlaps(List first, List second) {
      if(first == null || second == null)
         return false;
      for(Object x : first) 
         if(second.contains(x))
            return true;
      return false;
   }
   
} 