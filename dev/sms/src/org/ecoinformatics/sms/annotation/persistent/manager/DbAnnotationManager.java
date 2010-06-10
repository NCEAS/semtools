
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
package org.ecoinformatics.sms.annotation.persistent.manager;

import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.conf.DefaultConfiguration;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.SelectQuery;
import org.apache.commons.lang.StringUtils;
import org.ecoinformatics.sms.SMS;
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
import org.ecoinformatics.sms.annotation.Relationship;
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
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
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
	public DbAnnotation getDbAnnotation(Annotation annotation) {
				
		DbAnnotation dbAnnotation = null;

		ObjectContext context = getDataContext();
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
	public DbAnnotation createDbAnnotation(Annotation annotation, String source) {
						
		// look up the annotation
		DbAnnotation dbAnnotation = getDbAnnotation(annotation);
		if (dbAnnotation != null) {
			return dbAnnotation;
		}
		
		ObjectContext context = getDataContext();
		
		// otherwise, create the object
		dbAnnotation = context.newObject(DbAnnotation.class);
		dbAnnotation.setIdentifier(annotation.getURI());
		dbAnnotation.setSource(source);
		dbAnnotation.setEmlPackage(annotation.getEMLPackage());
		dbAnnotation.setDataTable(annotation.getDataTable());
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
			// contexts, recursively.
			// if we don't want to expand them fully, then set to false.
			expandContexts(o, dbObservation, context, true);
		}
		
		context.commitChanges();
		
		return dbAnnotation;
		
	}
	
	/**
	 * Adds contexts - optionally added recursively so that synthetic transitive contexts are stored
	 * Context is transitive in the sense that:
	 * A rel B; 
	 * B rel C; 
	 * therefore: A rel C
	 * @param o the Observation to expand contexts
	 * @param dbObservation the dbObservation that will have these expanded contexts
	 * @param context the data object context (for new objects and transactions)
	 * @param recursive - should this be done recursively (such that transitive context is captured)
	 */
	private void expandContexts(Observation o, DbObservation dbObservation, ObjectContext context, boolean recursive) {
		if (o == null) {
			return;
		}
		List<Context> contexts = o.getContexts();
		if (contexts == null || contexts.isEmpty()) {
			return;
		}
		for (Context c: contexts) {
			DbContext dbContext = context.newObject(DbContext.class);
			
			// the relationship
			Relationship relationship = c.getRelationship();
			dbContext.setRelationship((relationship == null) ? null : relationship.getURI());

			// the target observation
			Observation observationB = c.getObservation();
			DbObservation dbObservationB = context.newObject(DbObservation.class);
			dbObservationB.setEntity((observationB == null || observationB.getEntity() == null) ? null : observationB.getEntity().getURI());
			dbContext.setObservationB(dbObservationB);
			
			//System.out.println("Adding context " + o + " --- " + c.getMadlib());
			
			//do one or the other, otherwise you duplicate
			//dbContext.setObservation(dbObservation);
			dbObservation.addToContexts(dbContext);
			
			// call again to capture transitive context relationships
			if (recursive) {
				expandContexts(observationB, dbObservation, context, recursive);
			}
		}
	}
	
	private ObjectContext getDataContext() {
		ObjectContext context = null;
		try {
			context = BaseContext.getThreadObjectContext();
		} catch (Exception e) {
			context = DataContext.createDataContext();
			BaseContext.bindThreadObjectContext(context);
		}
		return context;
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
		   // add to the db index
		   DbAnnotation dbAnnotation = createDbAnnotation(annotation, source);
		   // remove from the working set
		   _annotations.remove(annotation.getURI());
	   } else {
		   // add to the working set only
		   super.importAnnotation(annotation, source);
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
      ObjectContext context = getDataContext();
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
	   ObjectContext context = getDataContext();
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
		ObjectContext context = getDataContext();
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
	   ObjectContext context = getDataContext();
	   // add ids from the index
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
	   // get the working set first
	   List<Annotation> results = super.getAnnotations(emlPackage, dataTable);
	   
	   ObjectContext context = getDataContext();
	   
		// look up the annotations
		final Expression expression = 
			Expression.fromString(
					"emlPackage = $emlPackage and dataTable = $dataTable");
		Map<String, String> params = new HashMap<String, String>();
		params.put("emlPackage", emlPackage);
		if (dataTable != null) {
			params.put("dataTable", dataTable);
		}
		SelectQuery query = new SelectQuery(DbAnnotation.class, expression.expWithParameters(params));
		List<DbAnnotation> values = context.performQuery(query);
		if (values != null) {
			for (DbAnnotation dbAnnotation: values) {
				try {
					results.add(getAnnotation(dbAnnotation));
				} catch (AnnotationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
      return results;
   }

   public List<Annotation> getMatchingAnnotations(Criteria criteria) {
	   //let the fun begin!
	   List<Annotation> results = new ArrayList<Annotation>();

	   // get the expression string
	   String expressionString = criteriaAsExpression(criteria);
	   
	   Expression expression = Expression.fromString(expressionString.toString());
	   ObjectContext context = getDataContext();
	   SelectQuery query = new SelectQuery(DbAnnotation.class, expression);
	   List<DbAnnotation> values = context.performQuery(query);
	   for (DbAnnotation dbAnnotation: values) {
		   try {
			   results.add(getAnnotation(dbAnnotation));
			} catch (AnnotationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	   
	   List<Annotation> results = new ArrayList<Annotation>();
	   
	   // using "or" instead of "and" for more results, even though these are not ranked yet.
	   String combined = createExpresstionString(entities, characteristics, standards, protocols, contexts, "=", "or");
	   
	   Expression expression = Expression.fromString(combined.toString());
	   ObjectContext context = getDataContext();
	   SelectQuery query = new SelectQuery(DbAnnotation.class, expression);
	   List<DbAnnotation> values = context.performQuery(query);
	   for (DbAnnotation dbAnnotation: values) {
		   try {
			   results.add(getAnnotation(dbAnnotation));
			} catch (AnnotationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
      }
     
      return results;
   }

   /**
    * Get entities used in managed annotations
    * @return list of entities
    */
   public List<OntologyClass> getActiveEntities() {
	      List<OntologyClass> results = new ArrayList();
		
	      ObjectContext context = getDataContext();
	      
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
		
      ObjectContext context = getDataContext();
      
		// look up the observation entities
		SelectQuery query = new SelectQuery(DbCharacteristic.class);
		List<DbCharacteristic> values = context.performQuery(query);
		if (values != null) {
			for (DbCharacteristic dbc: values) {
				OntologyClass c = null;
				try {
					c = new Characteristic(dbc.getType());
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
		
      ObjectContext context = getDataContext();
		// look up the observation entities
		SelectQuery query = new SelectQuery(DbMeasurement.class);
		List<DbMeasurement> values = context.performQuery(query);
		if (values != null) {
			for (DbMeasurement dbm: values) {
				OntologyClass c = null;
				try {
					c = new Standard(dbm.getStandard());
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
      
      String charExpression = createExpressionString(characteristics, "measurements.characteristics.type", "=", "or");
      String stdExpression = createExpressionString(characteristics, "measurements.standard", "=", "or");
      String combined = StringUtils.join(new String[] {charExpression, stdExpression}, " or ");
     
      Expression expression = Expression.fromString(combined);
     
      ObjectContext context = getDataContext();
	  SelectQuery query = new SelectQuery(DbObservation.class, expression);
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
      
      String entityExpression = createExpressionString(entities, "measurement.observation.entity", "=", "or");
      String charExpression = createExpressionString(standards, "measurement.standard", "=", "or");
      String combined = StringUtils.join(new String[] {entityExpression, charExpression}, " or ");
     
      Expression expression = Expression.fromString(combined);
      
      ObjectContext context = getDataContext();
      
	  SelectQuery query = new SelectQuery(DbCharacteristic.class, expression);
      List<DbCharacteristic> values = context.performQuery(query);
      if (values != null) {
    	  for (DbCharacteristic dbc: values) {
    		  OntologyClass c = null;
    		  try {
    			  c = new Characteristic(dbc.getType());
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
      
      String entityExpression = createExpressionString(entities, "observation.entity", "=", "or");
      String charExpression = createExpressionString(characteristics, "characteristics.type", "=", "or");
      String combined = StringUtils.join(new String[] {entityExpression, charExpression}, " or ");
     
      Expression expression = Expression.fromString(combined);
      ObjectContext context = getDataContext();
	  SelectQuery query = new SelectQuery(DbMeasurement.class, expression);
      List<DbMeasurement> values = context.performQuery(query);
      if (values != null) {
    	  for (DbMeasurement dbm: values) {
    		  OntologyClass c = null;
    		  try {
    			  c = new Standard(dbm.getStandard());
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
   private String criteriaAsExpression(Criteria criteria) {
	   
	   StringBuffer expression = new StringBuffer();
	   
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
				}
			} else {
				// what criteria were given?
				Class subject = criteria.getSubject();
				OntologyClass value = criteria.getValue();
				if (value == null) {
					return null;
				}
				if (subject != null && subject.equals(Entity.class)) {
					entities.add(value);
				}
				if (subject != null && subject.equals(Characteristic.class)) {
					characteristics.add(value);
				}
				if (subject != null && subject.equals(Standard.class)) {
					standards.add(value);
				}
				if (subject != null && subject.equals(Protocol.class)) {
					protocols.add(value);
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
			// now construct the expression from the given class lists
			String operator = "=";
			if (criteria.getCondition().equals(Criteria.ISNOT)) {
				operator = "!=";
			}
			String conditionalOperator = "or";

			expression.append(
					createExpresstionString(entities, characteristics, standards, protocols, contexts, operator, conditionalOperator));
			
	   }
	   else {
		   // iterate through the subcriteria
		   if (criteria.getSubCriteria() != null) {
			   String operator = "or";
			   if (criteria.isAll()) {
				   operator = "and";
			   }
			   Iterator<Criteria> iter = criteria.getSubCriteria().iterator();
			   while (iter.hasNext()) {
				   Criteria subcriteria = iter.next();
				   // recurse here
				   String subExpression = criteriaAsExpression(subcriteria);
				   expression.append(subExpression);
				   if (iter.hasNext()) {
					   expression.append(" " + operator + " ");
				   }   
			   }
		   }
	   }
	   
	   return expression.toString();
   }

   private String createExpresstionString(List<OntologyClass> entities,
           List<OntologyClass> characteristics, List<OntologyClass> standards,
           List<OntologyClass> protocols, List<Triple> contexts, String relationalOperator, String conditionalOperator) {
	   
	   List<String> terms = new ArrayList<String>();
	   addSubclasses(entities);
	   addSubclasses(characteristics);
	   addSubclasses(standards);
	   addSubclasses(protocols);
	   
	   String entityString = createExpressionString(entities, "observations.entity", relationalOperator, "or");
	   String characterString = createExpressionString(characteristics, "observations.measurements.characteristics.type", relationalOperator, "or");
	   String standardString = createExpressionString(standards, "observations.measurements.standard", relationalOperator, "or");
	   String protocolString = createExpressionString(protocols, "observations.measurements.protocol", relationalOperator, "or");
	   String contextString = createContextExpressionString(contexts, "or");
	   
	   if (entityString != null) {
		   terms.add(entityString);
	   }
	   if (characterString != null) {
		   terms.add(characterString);
	   }
	   if (standardString != null) {
		   terms.add(standardString);
	   }
	   if (protocolString != null) {
		   terms.add(protocolString);
	   }
	   if (contextString != null) {
		   terms.add(contextString);
	   }
	   
	   String combined = StringUtils.join(
			   terms, 
			   " " + conditionalOperator + " ");
	   
	   if (combined != null && combined.length() == 0) {
		   combined = null;
	   }
	   
	   return combined;
	   
   }
   
   private String createExpressionString(List<OntologyClass> classes, String path, String relationalOperator, String conditionalOperator) {
	   StringBuffer expression = new StringBuffer();
	   if (classes != null && !classes.isEmpty()) {
		   expression.append("(");
		   Iterator<OntologyClass> iter = classes.iterator();
		   while (iter.hasNext()) {
			   OntologyClass oc = iter.next();
			   expression.append(path + " " + relationalOperator + " '");
			   expression.append(oc.getURI());
			   expression.append("'");
			   if (iter.hasNext()) {
				   expression.append(" " + conditionalOperator + " ");
			   }
		   }
		   expression.append(")");
		   return expression.toString();
	   }
	   return null;
   }
   
   private String createContextExpressionString(List<Triple> contextTriples, String operator) {
	   StringBuffer expression = new StringBuffer();
	   if (contextTriples != null && !contextTriples.isEmpty()) {
		   expression.append("(");
		   Iterator<Triple> iter = contextTriples.iterator();
		   while (iter.hasNext()) {
			   Triple contextTriple = iter.next();
			   
			   List<String> terms = new ArrayList<String>();
			   StringBuffer aTerm = new StringBuffer();
			   StringBuffer bTerm = new StringBuffer();
			   StringBuffer cTerm = new StringBuffer();

			   if (contextTriple.a != null) {
				   aTerm.append("observations.contexts.observation.entity = '");
				   aTerm.append(contextTriple.a.getURI());
				   aTerm.append("'");
				   terms.add(aTerm.toString());
			   }
			   if (contextTriple.b != null) {
				   bTerm.append("observations.contexts.relationship = '");
				   bTerm.append(contextTriple.b.getURI());
				   bTerm.append("'");
				   terms.add(bTerm.toString());
			   }
			   if (contextTriple.c != null) {
				   cTerm.append("observations.contexts.observationB.entity = '");
				   cTerm.append(contextTriple.c.getURI());
				   cTerm.append("'");
				   terms.add(cTerm.toString());
			   }
			   
			   String combined = StringUtils.join(terms, " and ");
			   
			   expression.append("(");
			   expression.append(combined);
			   expression.append(")");

			   if (iter.hasNext()) {
				   expression.append(" " + operator + " ");
			   }
		   }
		   expression.append(")");
		   return expression.toString();
	   }
	   return null;
   }

   public static void main(String[] args) {
		try {
	
			//String annot1 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/er-2008-ex1-annot.xml";
			String annot1 = "file:///Users/leinfelder/.semtools/profiles/benriver/data/benriver/20.52";

	        URL url = new URL(annot1);
	
	        // get annotation manager
	        AnnotationManager annotationManager = SMS.getInstance().getAnnotationManager();
	        Annotation annotation = Annotation.read(url.openStream());
	        annotationManager.importAnnotation(annotation, url.toString());
			
	        // look up the db annotation directly
			//DbAnnotation dbAnnotation = ((DbAnnotationManager)annotationManager).getDbAnnotation(annotation);
	 	   //ObjectContext context = DataContext.createDataContext();
	 	   ObjectContext context = ((DbAnnotationManager)annotationManager).getDataContext();
	        DbAnnotation dbAnnotation = query("%", context);
	        
			// print it out
			System.out.println("Annotation: " + dbAnnotation);
			System.out.println();
			List<DbObservation> dbObservations = dbAnnotation.getObservations();
			for (DbObservation dbo: dbObservations) {
				System.out.println("Observation: " + dbo);
				System.out.println();
//				for (DbMeasurement dbm: dbo.getMeasurements()) {
//					System.out.println(dbm);
//				}
				List<DbContext> contexts = dbo.getContexts();
				System.out.println("Context count: " + contexts.size());
				for (DbContext dbc: contexts) {
					//System.out.println(dbc);
					System.out.println("--------");
					System.out.println(dbc.getObservation());
					System.out.println(dbc.getRelationship());
					System.out.println(dbc.getObservationB());
				}
				System.out.println();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
   
   private static DbAnnotation query(String param, ObjectContext context) {
		
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