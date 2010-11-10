
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
package org.ecoinformatics.sms;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Triple;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.ontology.OntologyClass;

import java.util.List;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public interface AnnotationManager {

   /**
    * Import an annotation into the manager
    * @param r the semantic annotation 
    * @param source location of the annotation
    */
   public void importAnnotation(InputStream is, String source) throws Exception;
   
   /**
    * Import an annotation into the manager
    * @param r the semantic annotation 
    * @param source location of the annotation
    */
   public void importAnnotation(Annotation annot, String source) throws Exception;


   /**
    * Export an annotation from the manager
    * @param the identiifer of the annotation
    * @param w the writer to write to
    */
   public void exportAnnotation(String id, OutputStream os) throws Exception;

   /**
    * Remove an annotation from the manager
    * @param id the identifier of the annotation
    */
   public void removeAnnotation(String id) throws Exception;

   /**
    * Check if the identifier is assigned to an annotation in the
    * manager
    * @return true if the id is assigned to an annotation in the manager
    * @param id the annotation identifier
    */
   public boolean isAnnotation(String id);

   /**
    * Get an annotation from the manager
    * @param the identiifer of the annotation
    */
   public Annotation getAnnotation(String id) throws Exception;

   
   /**
    * Ensure that the annotation is valid.
    * @param a the annotation
    * @throws java.lang.Exception
    */
   public void validateAnnotation(Annotation a) throws Exception;
   
   /**
    * Get the annotation identifiers from the manager
    * @return the set of annotation identifiers
    */
   public List<String> getAnnotationIds();

   /**
    * Get the annotations from the manager
    * @return the annotations
    */
   public List<Annotation> getAnnotations();
   
   /**
    * Get the annotations for a given EML package
    * @param the EML package id
    * @param the [optional] dataTable to match
    * @return annotations for the eml package
    */
   public List<Annotation> getAnnotations(String emlPackage, String dataTable);

   /**
    * Get all ontology classes used for a specific annotation
    * @param a the annotation
    * @return the ontology classes used in an annotation
    */
   public List<OntologyClass> getOntologyClasses(Annotation a);

   /**
    * Get all ontology classes used for a specific annotation
    * @param a the annotation
    * @param addSubclasses if true, add all subclasses to result
    * @param addSuperclasses if true, add all superclasses to result
    * @return the ontology classes used in an annotation
    */
   public List<OntologyClass> getOntologyClasses(Annotation a,
           boolean addSubclasses, boolean addSuperclasses);

   /**
    * Get Annotations that match the given Criteria
    * @param criteria Criteria to match
    * @return list of matching annotations
    */
   public List<Annotation> getMatchingAnnotations(Criteria criteria);
   
   /**
    * Get annotations that contain an entity in the given list and a 
    * measurement with a characteristic and standard in the given lists
    * @param entities the entity classes to search for
    * @param characteristics the characteristic classes to search for
    * @param standards the measurement standard classes to search for
    * @return the matching annotations
    */
   public List<Annotation> getMatchingAnnotations(List<OntologyClass> entities,
           List<OntologyClass> characteristics, List<OntologyClass> standards,
           List<OntologyClass> protocols, List<Triple> contexts);

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
           List<OntologyClass> protocols, List<Triple> contexts, boolean searchSubclasses);

   /**
    * Get annotations that contain the given entity, characteristic, and standard
    * @param entity the entity class to search for
    * @param characteristic the characteristic classto search for
    * @param standard the measurement standard class to search for
    * @return the matching annotations
    */
   public List<Annotation> getMatchingAnnotations(OntologyClass entity,
           OntologyClass characteristic, OntologyClass standard,
           OntologyClass protocol, Triple context);

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
           OntologyClass protocol, Triple context, boolean searchSubclasses);

   /**
    * Get entities used in managed annotations
    * @return list of entities
    */
   public List<OntologyClass> getActiveEntities();

   /**
    * Get entities used in managed annotations
    * @param addSuperclasses if true, include all superclasses of active 
    * entities
    * @return list of entities
    */
   public List<OntologyClass> getActiveEntities(boolean addSuperclasses);

   /**
    * Get entities used in managed annotations having a measurement with a 
    * characteristic and standard in the given characteristics and standards
    * @param characteristics characteristics to look for
    * @param standards standards to look for
    * @return list of matching entities
    */
   public List<OntologyClass> getActiveEntities(List<OntologyClass> characteristics,
           List<OntologyClass> standards, List<OntologyClass> protocols);

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
           List<OntologyClass> standards, boolean searchSubclasses, boolean addSuperclasses);

   /**
    * Get entities used in managed annotations having a measurement with the 
    * give characteristic and standard 
    * @param characteristic the characteristic to look for
    * @param standard the standard to look for
    * @return list of matching entities
    */
   public List<OntologyClass> getActiveEntities(OntologyClass characteristic,
           OntologyClass standard);

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
           OntologyClass standard, boolean searchSubclasses, boolean addSuperclasses);

   /**
    * Get characteristics used in managed annotations
    * @return list of characteristics
    */
   public List<OntologyClass> getActiveCharacteristics();

   /**
    * Get characteristics used in managed annotations
    * @param addSuperclasses if true, include all superclasses of active 
    * characteristics
    * @return list of characteristics
    */
   public List<OntologyClass> getActiveCharacteristics(boolean addSuperclasses);

   /**
    * Get characteristics used in managed annotations having a measurement 
    * with an entity and standard in the given entities and standards
    * @param entities entities to look for
    * @param standards standards to look for
    * @return list of matching characteristics
    */
   public List<OntologyClass> getActiveCharacteristics(
           List<OntologyClass> entities, List<OntologyClass> standards, List<OntologyClass> protocols);

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
           boolean searchSubclasses, boolean addSuperclasses);

   /**
    * Get characteristics used in managed annotations with a measurement having 
    * the given entity and standard 
    * @param entity the entity to look for
    * @param standard the standard to look for
    * @return list of matching characteristics
    */
   public List<OntologyClass> getActiveCharacteristics(OntologyClass entity,
           OntologyClass standard);

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
           boolean addSuperclasses);

   /**
    * Get measurement standards used in managed annotations
    * @return list of measurement standards
    */
   public List<OntologyClass> getActiveStandards();

   /**
    * Get standards used in managed annotations
    * @param addSuperclasses if true, include all superclasses of active 
    * standards
    * @return list of standards
    */
   public List<OntologyClass> getActiveStandards(boolean addSuperclasses);

   /**
    * Get standards used in managed annotations having a measurement 
    * with an entity and characteristic in the given entities and 
    * characteristics
    * @param entities entities to look for
    * @param characteristics characteristics to look for
    * @return list of matching standards
    */
   public List<OntologyClass> getActiveStandards(
           List<OntologyClass> entities, List<OntologyClass> characteristics, List<OntologyClass> protocols);

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
           boolean searchSubclasses, boolean addSuperclasses);

   /**
    * Get standards used in managed annotations with an observation having
    * the given entity and measurement characteristic
    * @param entity the entity to look for
    * @param characteristic the characteristic to look for
    * @return list of matching standards
    */
   public List<OntologyClass> getActiveStandards(OntologyClass entity,
           OntologyClass characteristic);

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
           boolean addSuperclasses);
   
   /**
    * Get Protocols used in managed annotations
    * @return list of protocols
    */
   public List<OntologyClass> getActiveProtocols();
   
   /**
    * Get protocols used in managed annotations having a measurement 
    * with an entity and characteristic in the given entities and 
    * characteristics
    * @param entities entities to look for
    * @param characteristics characteristics to look for
    * @return list of matching protocols
    */
   public List<OntologyClass> getActiveProtocols(
           List<OntologyClass> entities, List<OntologyClass> characteristics, List<OntologyClass> standards);

   
   /**
    * Get measurements used in managed annotations
    * @return list of measurements
    */
   public List<OntologyClass> getActiveMeasurements();

}