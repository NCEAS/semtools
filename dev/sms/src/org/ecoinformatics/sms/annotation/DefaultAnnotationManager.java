
/**
 *    '$RCSfile: DefaultAnnotationManager.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2007/05/25 16:13:14 $'
 *   '$Revision: 1.2 $'
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
import org.ecoinformatics.sms.ontology.OntologyClass;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public class DefaultAnnotationManager implements AnnotationManager {

   private SMS _sms;
   private HashMap<String, Annotation> _annotations = new HashMap();

   /**
    * Default constuctor
    */
   public DefaultAnnotationManager(SMS sms) {
      _sms = sms;
   }

   /**
    * Import an annotation into the manager
    * @param r the semantic annotation 
    * @param id the identifier to assign to the annotation
    */
   public void importAnnotation(InputStream is, String id) throws Exception {
      if(isAnnotation(id)) {
         String msg = "annotation id '" + id + "' already exists";
         throw new Exception(msg);
      }
      _annotations.put(id, Annotation.read(is));
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
    * Update an existing annotation in the manager
    * @param r the new semantic annotation 
    * @param id the identifier of the annotation to update
    */
   public void updateAnnotation(InputStream is, String id) throws Exception {
      if(!isAnnotation(id)) {
         String msg = "annotation id '" + id + "' does not exist";
         throw new Exception(msg);
      }
      removeAnnotation(id);
      importAnnotation(is, id);
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
      for(String id : getAnnotationIds()) {
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
    * @param eml package id
    * @param the optional data table to match
    * @return the annotations
    */
   public List<Annotation> getAnnotations(String emlPackage, String dataTable) {
      List<Annotation> results = new ArrayList<Annotation>();
      for(String id : getAnnotationIds()) {
         Annotation a = null;
         try {
            a = getAnnotation(id);
            if (a.getEMLPackage().equals(emlPackage)) {
            	if (dataTable == null || dataTable.equals(a.getDataTable())) {
            		results.add(a);
            	}
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

   /**
    * Get annotations that contain an entity in the given list and a 
    * measurement with a characteristic and standard in the given lists
    * @param entities the entity class URI's to search for
    * @param characteristics the characteristic class URI's to search for
    * @param standards the measurement standard class URI's to search for
    * @return the matching annotations
    */
   public List<Annotation> getMatchingAnnotations(List<OntologyClass> entities,
           List<OntologyClass> characteristics, List<OntologyClass> standards) {
      // find matches
      List<Annotation> results = new ArrayList();
      for(Annotation annot : getAnnotations()) {
         if(!hasMatchingEntity(annot, entities))
            continue;
         if(!hasMatchingCharacteristic(annot, characteristics))
            continue;
         if(!hasMatchingStandard(annot, standards))
            continue;
         if(!results.contains(annot))
            results.add(annot);
      }
      return results;
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
           boolean searchSubclasses) {
      if(!searchSubclasses)
         return getMatchingAnnotations(entities, characteristics, standards);
      List<OntologyClass> entSubs = new ArrayList(entities);
      List<OntologyClass> charSubs = new ArrayList(characteristics);
      List<OntologyClass> stdSubs = new ArrayList(standards);
      addSubclasses(entSubs);
      addSubclasses(charSubs);
      addSubclasses(stdSubs);
      return getMatchingAnnotations(entSubs, charSubs, stdSubs);
   }

   /**
    * Get annotations that contain the given entity, characteristic, and standard
    * @param entity the entity class to search for
    * @param characteristic the characteristic classto search for
    * @param standard the measurement standard class to search for
    * @return the matching annotations
    */
   public List<Annotation> getMatchingAnnotations(OntologyClass entity,
           OntologyClass characteristic, OntologyClass standard) {
      List<OntologyClass> entities = new ArrayList();
      if(entity != null)
         entities.add(entity);
      List<OntologyClass> characteristics = new ArrayList();
      if(characteristic != null)
         characteristics.add(characteristic);
      List<OntologyClass> standards = new ArrayList();
      if(standard != null)
         standards.add(standard);
      return getMatchingAnnotations(entities, characteristics, standards);
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
           boolean searchSubclasses) {
      List<OntologyClass> entities = new ArrayList();
      if(entity != null)
         entities.add(entity);
      List<OntologyClass> characteristics = new ArrayList();
      if(characteristic != null)
         characteristics.add(characteristic);
      List<OntologyClass> standards = new ArrayList();
      if(standard != null)
         standards.add(standard);
      return getMatchingAnnotations(entities, characteristics, standards, searchSubclasses);
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
           List<OntologyClass> characteristics, List<OntologyClass> standards) {
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
    * Get entities used in managed annotations having a measurement with the 
    * give characteristic and standard 
    * @param characteristic the characteristic to look for
    * @param standard the standard to look for
    * @return list of matching entities
    */
   public List<OntologyClass> getActiveEntities(OntologyClass characteristic,
           OntologyClass standard) {
      List<OntologyClass> characteristics = new ArrayList();
      if(characteristic != null)
         characteristics.add(characteristic);
      List<OntologyClass> standards = new ArrayList();
      if(standard != null)
         standards.add(standard);
      return getActiveEntities(characteristics, standards);
   }

   /**
    * Get entities used in managed annotations having a measurement with a 
    * characteristic and standard in the given characteristics and standards
    * @param characteristics characteristics to look for
    * @param standards standards to look for
    * @param searchSubclasses search using subclasses of the given classes
    * @param addSuperclasses add superclasses to found entities
    * @return list of matching entities
    */
   public List<OntologyClass> getActiveEntities(List<OntologyClass> characteristics,
           List<OntologyClass> standards, boolean searchSubclasses,
           boolean addSuperclasses) {
      if(characteristics == null)
         characteristics = new ArrayList();
      if(standards == null)
         standards = new ArrayList();
      List<OntologyClass> result = new ArrayList();
      if(!searchSubclasses)
         result = getActiveEntities(characteristics, standards);
      else {
         List<OntologyClass> charSubs = new ArrayList(characteristics);
         addSubclasses(charSubs);
         List<OntologyClass> stdSubs = new ArrayList(standards);
         addSubclasses(stdSubs);
         result = getActiveEntities(charSubs, stdSubs);
      }
      if(!addSuperclasses)
         return result;
      addSuperclasses(result);
      return result;
   }

   /**
    * Get entities used in managed annotations having a measurement with the 
    * give characteristic and standard 
    * @param characteristic the characteristic to look for
    * @param standard the standard to look for
    * @param searchSubclasses search using subclasses of the given classes
    * @param addSuperclasses add superclasses to found entities
    * @return list of matching entities
    */
   public List<OntologyClass> getActiveEntities(OntologyClass characteristic,
           OntologyClass standard, boolean searchSubclasses,
           boolean addSuperclasses) {
      List<OntologyClass> result = new ArrayList();
      if(!searchSubclasses)
         result = getActiveEntities(characteristic, standard);
      else {
         List<OntologyClass> charSubs = new ArrayList();
         if(characteristic != null)
            charSubs.add(characteristic);
         addSubclasses(charSubs);
         List<OntologyClass> stdSubs = new ArrayList();
         if(standard != null)
            stdSubs.add(standard);
         addSubclasses(stdSubs);
         result = getActiveEntities(charSubs, stdSubs);
      }
      if(!addSuperclasses)
         return result;
      addSuperclasses(result);
      return result;
   }

   /**
    * Get characteristics used in managed annotations having a measurement 
    * with an entity and standard in the given entities and standards
    * @param entities entities to look for
    * @param standards standards to look for
    * @return list of matching characteristics
    */
   public List<OntologyClass> getActiveCharacteristics(
           List<OntologyClass> entities, List<OntologyClass> standards) {
      // check if both args are missing
      if((entities == null || entities.isEmpty()) &&
              (standards == null || standards.isEmpty()))
         return getActiveCharacteristics();
      List<OntologyClass> results = new ArrayList();
      for(Annotation a : getAnnotations())
         for(Observation o : a.getObservations()) {
            if(entities != null && !entities.isEmpty() && 
                    !overlaps(entities, getAllEntities(o))) 
               continue;
            for(Measurement m : o.getMeasurements()) {
               if(standards != null && !standards.isEmpty() &&
                       !standards.contains(m.getStandard()))
                  continue;
               // found a match
               for(Characteristic c : m.getCharacteristics())
                  if(!results.contains(c))
                     results.add(c);
            }
         }
      return results;
   }

   /**
    * Get characteristics used in managed annotations having a measurement 
    * with an entity and standard in the given entities and standards
    * @param entities entities to look for
    * @param standards standards to look for
    * @param searchSubclasses search using subclasses of the given classes
    * @param addSuperclasses add superclasses to found entities
    * @return list of matching characteristics
    */
   public List<OntologyClass> getActiveCharacteristics(
           List<OntologyClass> entities, List<OntologyClass> standards,
           boolean searchSubclasses, boolean addSuperclasses) {
      List<OntologyClass> result = new ArrayList();
      if(entities == null)
         entities = new ArrayList();
      if(standards == null)
         standards = new ArrayList();
      if(!searchSubclasses)
         result = getActiveCharacteristics(entities, standards);
      else {
         List<OntologyClass> entSubs = new ArrayList(entities);
         addSubclasses(entSubs);
         List<OntologyClass> stdSubs = new ArrayList(standards);
         addSubclasses(stdSubs);
         result = getActiveCharacteristics(entSubs, stdSubs);
      }
      if(!addSuperclasses)
         return result;
      addSuperclasses(result);
      return result;
   }

   /**
    * Get characteristics used in managed annotations with a measurement having 
    * the given entity and standard 
    * @param entity the entity to look for
    * @param standard the standard to look for
    * @return list of matching characteristics
    */
   public List<OntologyClass> getActiveCharacteristics(OntologyClass entity,
           OntologyClass standard) {
      List<OntologyClass> entities = new ArrayList();
      if(entity != null)
         entities.add(entity);
      List<OntologyClass> standards = new ArrayList();
      if(standard != null)
         standards.add(standard);
      return getActiveCharacteristics(entities, standards);
   }

   /**
    * Get characteristics used in managed annotations with a measurement having 
    * the given entity and standard 
    * @param entity the entity to look for
    * @param standard the standard to look for
    * @param searchSubclasses search using subclasses of the given classes
    * @param addSuperclasses add superclasses to found entities
    * @return list of matching characteristics
    */
   public List<OntologyClass> getActiveCharacteristics(OntologyClass entity,
           OntologyClass standard, boolean searchSubclasses,
           boolean addSuperclasses) {
      List<OntologyClass> result = new ArrayList();
      if(!searchSubclasses)
         result = getActiveCharacteristics(entity, standard);
      else {
         List<OntologyClass> entSubs = new ArrayList();
         if(entity != null)
            entSubs.add(entity);
         addSubclasses(entSubs);
         List<OntologyClass> stdSubs = new ArrayList();
         if(standard != null)
            stdSubs.add(standard);
         addSubclasses(stdSubs);
         result = getActiveCharacteristics(entSubs, stdSubs);
      }
      if(!addSuperclasses)
         return result;
      addSuperclasses(result);
      return result;
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
           List<OntologyClass> entities, List<OntologyClass> characteristics) {
      // check if both args are missing
      if((entities == null || entities.isEmpty()) &&
              (characteristics == null || characteristics.isEmpty()))
         return getActiveStandards();
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
      return results;
   }

   /**
    * Get standards used in managed annotations having a measurement 
    * with an entity and characteristic in the given entities and 
    * characteristics
    * @param entities entities to look for
    * @param characteristics characteristics to look for
    * @param searchSubclasses search using subclasses of the given classes
    * @param addSuperclasses add superclasses to found entities
    * @return list of matching standards
    */
   public List<OntologyClass> getActiveStandards(
           List<OntologyClass> entities, List<OntologyClass> characteristics,
           boolean searchSubclasses, boolean addSuperclasses) {
      List<OntologyClass> result = new ArrayList();
      if(entities == null)
         entities = new ArrayList();
      if(characteristics == null)
         characteristics = new ArrayList();
      if(!searchSubclasses)
         result = getActiveStandards(entities, characteristics);
      else {
         List<OntologyClass> entSubs = new ArrayList(entities);
         addSubclasses(entSubs);
         List<OntologyClass> charSubs = new ArrayList(characteristics);
         addSubclasses(charSubs);
         result = getActiveStandards(entSubs, charSubs);
      }
      if(!addSuperclasses)
         return result;
      addSuperclasses(result);
      return result;
   }

   /**
    * Get standards used in managed annotations with an observation having
    * the given entity and measurement characteristic
    * @param entity the entity to look for
    * @param characteristic the characteristic to look for
    * @return list of matching standards
    */
   public List<OntologyClass> getActiveStandards(OntologyClass entity,
           OntologyClass characteristic) {
      List<OntologyClass> entities = new ArrayList();
      if(entity != null)
         entities.add(entity);
      List<OntologyClass> characteristics = new ArrayList();
      if(characteristic != null)
         characteristics.add(characteristic);
      return getActiveStandards(entities, characteristics);
   }

   /**
    * Get standards used in managed annotations with an observation having
    * the given entity and measurement characteristic
    * @param entity the entity to look for
    * @param characteristic the characteristic to look for
    * @param searchSubclasses search using subclasses of the given classes
    * @param addSuperclasses add superclasses to found entities
    * @return list of matching standards
    */
   public List<OntologyClass> getActiveStandards(OntologyClass entity,
           OntologyClass characteristic, boolean searchSubclasses,
           boolean addSuperclasses) {
      List<OntologyClass> result = new ArrayList();
      if(!searchSubclasses)
         result = getActiveStandards(entity, characteristic);
      else {
         List<OntologyClass> entSubs = new ArrayList();
         if(entity != null)
            entSubs.add(entity);
         addSubclasses(entSubs);
         List<OntologyClass> charSubs = new ArrayList();
         if(characteristic != null)
            charSubs.add(characteristic);
         addSubclasses(charSubs);
         result = getActiveStandards(entSubs, charSubs);
      }
      if(!addSuperclasses)
         return result;
      addSuperclasses(result);
      return result;
   }

   /**
    * Helper method to check if an annotation has a matching entity
    * @param a the annotation
    * @param entities entities to check
    * @return true if annotation contains an entity in given list
    */
   private boolean hasMatchingEntity(Annotation a, List<OntologyClass> entities) {
      if(entities == null || entities.size() == 0)
         return true;
      for(Observation o : a.getObservations())
         if(entities.contains(o.getEntity()))
            return true;
      for(Observation o : a.getObservations())
         for(Measurement m : o.getMeasurements())
            for(Entity v : m.getDomainValues())
               if(entities.contains(v))
                  return true;
      return false;
   }

   /**
    * Helper method to check if an annotation has a matching characteristic
    * @param a the annotation
    * @param chars the characteristics to check
    * @return true if annotation contains a characteristic in given list
    */
   private boolean hasMatchingCharacteristic(Annotation a, List<OntologyClass> chars) {
      if(chars == null || chars.size() == 0)
         return true;
      for(Observation o : a.getObservations())
         for(Measurement m : o.getMeasurements())
            for(Characteristic c : m.getCharacteristics())
               if(chars.contains(c))
                  return true;
      return false;
   }

   /**
    * Helper method to check if an annotation has a matching standard
    * @param a the annotation
    * @param standards the standards to check
    * @return true if annotation contains a standard in the given list
    */
   private boolean hasMatchingStandard(Annotation a, List<OntologyClass> standards) {
      if(standards == null || standards.size() == 0)
         return true;
      for(Observation o : a.getObservations())
         for(Measurement m : o.getMeasurements())
            if(standards.contains(m.getStandard()))
               return true;
      return false;
   }

   /** 
    * Helper method to add all superclasses of the given classes
    * @param classes the class list being added to
    */
   private void addSuperclasses(List<OntologyClass> classes) {
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
   private void addSubclasses(List<OntologyClass> classes) {
      OntologyManager ontMgr = _sms.getOntologyManager();
      List<OntologyClass> subclasses = new ArrayList();
      for(OntologyClass c : classes)
         for(OntologyClass s : ontMgr.getNamedSubclasses(c))
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
   private List<Entity> getAllEntities(Observation o) {
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
   private boolean overlaps(List first, List second) {
      if(first == null || second == null)
         return false;
      for(Object x : first) 
         if(second.contains(x))
            return true;
      return false;
   }
   
} 