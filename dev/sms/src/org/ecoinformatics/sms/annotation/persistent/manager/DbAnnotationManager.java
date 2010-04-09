
/**
 *    '$RCSfile: DefaultAnnotationManager.java,v $'
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
package org.ecoinformatics.sms.annotation.persistent.manager;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.conf.DefaultConfiguration;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.SelectQuery;
import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.OntologyManager;
import org.ecoinformatics.sms.AnnotationManager;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.AnnotationException;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.DefaultAnnotationManager;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.annotation.Triple;
import org.ecoinformatics.sms.annotation.persistent.DbAnnotation;
import org.ecoinformatics.sms.annotation.persistent.DbCharacteristic;
import org.ecoinformatics.sms.annotation.persistent.DbContext;
import org.ecoinformatics.sms.annotation.persistent.DbMeasurement;
import org.ecoinformatics.sms.annotation.persistent.DbObservation;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.ontology.OntologyClass;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 */
public class DbAnnotationManager extends DefaultAnnotationManager {


   /**
    * Default constuctor
    */
   public DbAnnotationManager(SMS sms) {
	   super(sms);
      
      // configure db mapping
	   DefaultConfiguration conf = new DefaultConfiguration();			
	   conf.addClassPath("org/ecoinformatics/sms/annotation/persistent/config");
	   Configuration.initializeSharedConfiguration(conf);
   }

	/**
	 * Given a DbAnnotation object, return an Annotation object
	 * @param dbAnnotation
	 * @return Annotation pointed to by the DB entry
	 * @throws AnnotationException 
	 * @throws IOException 
	 */
	public static Annotation getAnnotation(DbAnnotation dbAnnotation) throws AnnotationException, IOException {
		String source = dbAnnotation.getSource();
		URL sourceURL = new URL(source);
		InputStream is = sourceURL.openStream();
		Annotation a = Annotation.read(is);
		
		return a;
	}
	
	/**
	 * Look up the DB annotation using id
	 * @param annotation
	 * @return dbAnnotation object for the given Annotation, if it exists
	 */
	public static DbAnnotation getDbAnnotation(Annotation annotation) {
		
		ObjectContext context = DataContext.createDataContext();
		
		DbAnnotation dbAnnotation = null;

		// look up the annotation if it exists
		final Expression expression = Expression.fromString("identifier = $identifier");
		Map<String, String> params = new HashMap<String, String>();
		params.put("identifier", annotation.getURI());
		SelectQuery query = new SelectQuery(DbAnnotation.class, expression.expWithParameters(params));
		List values = context.performQuery(query);
		if (values != null && !values.isEmpty()) {
			dbAnnotation = (DbAnnotation) values.get(0);
		}
		return dbAnnotation;
	}
	
	/**
	 * Creates (and inserts) a dbAnnotation object
	 * @param annotation
	 * @param source
	 * @return
	 */
	public static DbAnnotation createDbAnnotation(Annotation annotation, String source) {
				
		ObjectContext context = DataContext.createDataContext();
		
		// look up the annotation
		DbAnnotation dbAnnotation = getDbAnnotation(annotation);
		if (dbAnnotation != null) {
			return dbAnnotation;
		}
		
		// otherwise, create the object
		dbAnnotation = context.newObject(DbAnnotation.class);
		dbAnnotation.setIdentifier(annotation.getURI());
		dbAnnotation.setSource(source);
		// observations
		for (Observation o: annotation.getObservations()) {
			DbObservation dbObservation = context.newObject(DbObservation.class);
			// entity
			dbObservation.setEntity((o.getEntity() == null) ? null : o.getEntity().getURI());
			dbAnnotation.addToObservations(dbObservation);
			// measurements
			for (Measurement m: o.getMeasurements()) {
				DbMeasurement dbMeasurement = context.newObject(DbMeasurement.class);
				// standard
				dbMeasurement.setStandard((m.getStandard() == null) ? null : m.getStandard().getURI());
				// protocol
				dbMeasurement.setProtocol((m.getProtocol() == null) ? null : m.getProtocol().getURI());
				dbObservation.addToMeasurements(dbMeasurement);
				// characteristics
				for (Characteristic c: m.getCharacteristics()) {
					DbCharacteristic dbCharacteristic = context.newObject(DbCharacteristic.class);
					dbCharacteristic.setType(c.getURI());
					dbMeasurement.addToCharacteristics(dbCharacteristic);
				}
			}
			// contexts
			for (Context c: o.getContexts()) {
				DbContext dbContext = context.newObject(DbContext.class);
				dbContext.setObservation(dbObservation);
				dbContext.setRelationship((c.getRelationship() == null) ? null : c.getRelationship().getURI());
				// TODO: follow the observation b's contexts all the way down...
				DbObservation dbObservationB = context.newObject(DbObservation.class);
				dbObservationB.setEntity((c.getObservation() == null || c.getObservation().getEntity() == null) ? null : c.getObservation().getEntity().getURI());
				dbContext.setObservationB(dbObservationB);
				
				dbObservation.addToContexts(dbContext);
			}
		}
		
		context.commitChanges();
		
		return dbAnnotation;
		
	}
	
   /**
    * Import an annotation into the manager
    * @param r the semantic annotation 
    * @param id the identifier to assign to the annotation
    */
   public void importAnnotation(InputStream is, String source) throws Exception {
      importAnnotation(Annotation.read(is), source);
   }
   
   /**
    * Import an annotation into the manager
    * @param r the semantic annotation 
    * @param id the identifier to assign to the annotation
    */
   public void importAnnotation(Annotation annotation, String source) throws Exception {
	   if (source != null) {
		   DbAnnotation dbAnnotation = createDbAnnotation(annotation, source);
	   } else {
		   _annotations.put(annotation.getURI(), annotation);
	   }
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
      
      // remove from the working set
      super.removeAnnotation(id);
      
      //remove from the index
      ObjectContext context = DataContext.createDataContext();
		
		// look up the annotation 
		final Expression expression = Expression.fromString("identifier = $identifier");
		Map<String, String> params = new HashMap<String, String>();
		params.put("identifier", id);
		SelectQuery query = new SelectQuery(DbAnnotation.class, expression.expWithParameters(params));
		List values = context.performQuery(query);
		if (values != null && !values.isEmpty()) {
			DbAnnotation dbAnnotation = (DbAnnotation) values.get(0);
			context.deleteObject(dbAnnotation);
			context.commitChanges();
		}
   }

   /**
    * Check if the identifier is assigned to an annotation in the
    * manager
    * @return true if the id is assigned to an annotation in the manager
    * @param id the annotation identifier
    */
   public boolean isAnnotation(String id) {
	   
	   // we have it in the working set already
	   if (super.isAnnotation(id)) {
		   return true;
	   }
	   
	   // is it in the index?
	   ObjectContext context = DataContext.createDataContext();
		
		// look up the annotation 
		final Expression expression = Expression.fromString("identifier = $identifier");
		Map<String, String> params = new HashMap<String, String>();
		params.put("identifier", id);
		SelectQuery query = new SelectQuery(DbAnnotation.class, expression.expWithParameters(params));
		List values = context.performQuery(query);
		if (values != null && !values.isEmpty()) {
			return true;
		}
      return false;
   }

   /**
    * Get an annotation from the manager
    * @param the identifier of the annotation
    */
   public Annotation getAnnotation(String id) throws Exception {
      if(!isAnnotation(id)) {
         String msg = "annotation id '" + id + "' does not exist";
         throw new Exception(msg);
      }
      // check working set first
		Annotation annotation = super.getAnnotation(id);
		if (annotation != null) {
			return annotation;
		}

		// look it up in the index
      ObjectContext context = DataContext.createDataContext();
		
		// look up the annotation 
		final Expression expression = Expression.fromString("identifier = $identifier");
		Map<String, String> params = new HashMap<String, String>();
		params.put("identifier", id);
		SelectQuery query = new SelectQuery(DbAnnotation.class, expression.expWithParameters(params));
		List values = context.performQuery(query);
		if (values != null && !values.isEmpty()) {
			DbAnnotation dbAnnotation = (DbAnnotation) values.get(0);
			// construct it from source
			annotation = getAnnotation(dbAnnotation);
		}
		
		return annotation;
   }

   /**
    * Get the annotation identifiers from the manager
    * @return the set of annotation identifiers
    */
   public List<String> getAnnotationIds() {
	   // get the working set first
	   List<String> results = super.getAnnotationIds();
	   // add ids from the index
	   ObjectContext context = DataContext.createDataContext();
	   SelectQuery query = new SelectQuery(DbAnnotation.class);
	   List<DbAnnotation> values = context.performQuery(query);
	   for (DbAnnotation a: values) {
		   results.add(a.getIdentifier());
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
	   //TODO: add parameters to the dbObject
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

		ObjectContext context = DataContext.createDataContext();
		
		// look up the observation entities
		SelectQuery query = new SelectQuery(DbObservation.class);
		List<DbObservation> values = context.performQuery(query);
		if (values != null) {
			for (DbObservation dbo: values) {
				OntologyClass c = null;
				try {
					c = new Entity(dbo.getEntity());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(c !=null && !results.contains(c)) {
					results.add(c);
				}
			}
		}
		
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
				Class subject = criteria.getSubject();
				OntologyClass value = criteria.getValue();
				if (value == null) {
					return 0;
				}
				if (subject != null && subject.equals(Entity.class)) {
					entities.add(value);
					return hasMatchingEntity(a, entities);
				}
				if (subject != null && subject.equals(Characteristic.class)) {
					characteristics.add(value);
					return hasMatchingCharacteristic(a, characteristics);
				}
				if (subject != null && subject.equals(Standard.class)) {
					standards.add(value);
					return hasMatchingStandard(a, standards);
				}
				if (subject != null && subject.equals(Protocol.class)) {
					protocols.add(value);
					return hasMatchingProtocol(a, protocols);
				}
				// expand the measurement template if given
				if (subject != null && subject.equals(Measurement.class)) {
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

   public static void main(String[] args) {
		try {
	
			String annot1 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/er-2008-ex1-annot.xml";
	        URL url = new URL(annot1);
	
	        // get annotation manager
	        AnnotationManager annotationManager = SMS.getInstance().getAnnotationManager();
	        Annotation annotation = Annotation.read(url.openStream());
	        annotationManager.importAnnotation(annotation, url.toString());
			
	        // look up the db annotation directly
			//DbAnnotation dbAnnotation = getDbAnnotation(annotation);
	        DbAnnotation dbAnnotation = query("%Centimeter");
	        
			// print it out
			System.out.println(dbAnnotation);
			List<DbObservation> dbObservations = dbAnnotation.getObservations();
			for (DbObservation dbo: dbObservations) {
				System.out.println(dbo);
				for (DbMeasurement dbm: dbo.getMeasurements()) {
					System.out.println(dbm);
				}
				for (DbContext dbc: dbo.getContexts()) {
					System.out.println(dbc);
					System.out.println("--------");
					System.out.println(dbc.getObservation());
					System.out.println(dbc.getRelationship());
					System.out.println(dbc.getObservationB());
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
   
   private static DbAnnotation query(String param) {
	   ObjectContext context = DataContext.createDataContext();
		
		DbAnnotation dbAnnotation = null;

		// look up the annotation
		final Expression expression = 
			Expression.fromString(
					"observations.measurements.standard like $param ");
		Map<String, String> params = new HashMap<String, String>();
		params.put("param", param);
		SelectQuery query = new SelectQuery(DbAnnotation.class, expression.expWithParameters(params));
		List values = context.performQuery(query);
		if (values != null && !values.isEmpty()) {
			dbAnnotation = (DbAnnotation) values.get(0);
		}
		return dbAnnotation;
   }
   
} 