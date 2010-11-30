
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
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.Map.Entry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 */
public class DbAnnotationManager extends DefaultAnnotationManager {

	public static Log log = LogFactory.getLog(DbAnnotationManager.class);
	
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
	
	public static List<String> getURIs(List<OntologyClass> classes) {
		List<String> uris = new ArrayList<String>();
		for (OntologyClass c: classes) {
			uris.add(c.getURI());
		}
		return uris;
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
				// template
				dbMeasurement.setTemplate((m.getTemplate() == null) ? null : m.getTemplate().getURI());
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
			OntologyClass relationship = c.getRelationship();
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
	   
	   // for path splits
	   Map<String, List<String>> aliases = new HashMap<String, List<String>>();
	   
	   // get the expression string
	   Expression expression = criteriaAsExpression(criteria, criteria.isSame(), aliases);
	   
	   List<Annotation> results = performAnnotationQuery(expression, aliases);
	   
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
	   
	   // for path splits
	   Map<String, List<String>> aliases = new HashMap<String, List<String>>();
	   
	   // using "or" instead of "and" for more results, even though these are not ranked yet.
	   Expression expression = createExpression(entities, characteristics, standards, protocols, contexts, Criteria.IS, false, aliases);
	   
	   // do the query
	   List<Annotation> results = performAnnotationQuery(expression, aliases);
	   
	   return results;
   }
   
   private List<Annotation> performAnnotationQuery(Expression expression, Map<String, List<String>> aliases) {
	   List<Annotation> results = new ArrayList<Annotation>();
	   ObjectContext context = getDataContext();
	   SelectQuery query = new SelectQuery(DbAnnotation.class, expression);
	   // register the path alias for splitting
	   for (Entry<String, List<String>> alias: aliases.entrySet()) {
		   query.aliasPathSplits(alias.getKey(), alias.getValue().toArray(new String[0]));
	   }
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
    * Get characteristics used in managed annotations
    * @return list of characteristics
    */
   public List<OntologyClass> getActiveCharacteristics() {
      List<OntologyClass> results = new ArrayList<OntologyClass>();
		
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
    * Get measurement standards used in managed annotations
    * @return list of measurement standards
    */
   public List<OntologyClass> getActiveStandards() {
      List<OntologyClass> results = new ArrayList<OntologyClass>();
		
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
    * Get measurement protocols used in managed annotations
    * @return list of measurement protocols
    */
   public List<OntologyClass> getActiveProtocols() {
      List<OntologyClass> results = new ArrayList<OntologyClass>();
		
      ObjectContext context = getDataContext();
		// look up the observation entities
		SelectQuery query = new SelectQuery(DbMeasurement.class);
		List<DbMeasurement> values = context.performQuery(query);
		if (values != null) {
			for (DbMeasurement dbm: values) {
				OntologyClass c = null;
				try {
					c = new Protocol(dbm.getProtocol());
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
    * Get protocols used in managed annotations having a measurement 
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
         return getActiveProtocols();
      
      if (searchSubclasses) {
    	  addSubclasses(entities);
    	  addSubclasses(characteristics);
    	  addSubclasses(standards);
      }
      
      List<OntologyClass> results = new ArrayList<OntologyClass>();
      
      Expression entityExpression = null;
      Expression charExpression = null;
      Expression stdExpression = null;
      Expression expression = null;
      
      if (entities != null && !entities.isEmpty()) {
    	  entityExpression = ExpressionFactory.inExp("observation.entity", getURIs(entities));
    	  expression = entityExpression;
      }
      if (characteristics != null && !characteristics.isEmpty()) {
    	  charExpression = ExpressionFactory.inExp("characteristics.type", getURIs(characteristics));
    	  expression = charExpression;
      }
      if (entityExpression != null && charExpression != null) {
    	  expression = entityExpression.andExp(charExpression);
      }
      // add standards
      if (standards != null && !standards.isEmpty()) {
    	  stdExpression = ExpressionFactory.inExp("standard", getURIs(standards));
    	  if (expression != null) {
        	  expression = expression.andExp(stdExpression);
    	  } else {
    		  expression = stdExpression;
    	  }
      }
      
      ObjectContext context = getDataContext();
	  SelectQuery query = new SelectQuery(DbMeasurement.class, expression);
      List<DbMeasurement> values = context.performQuery(query);
      if (values != null) {
    	  for (DbMeasurement dbm: values) {
    		  OntologyClass c = null;
    		  try {
    			  c = new Protocol(dbm.getProtocol());
    		  } catch (Exception e) {
    			  // TODO Auto-generated catch block
    			  e.printStackTrace();
    		  }
    		  if(c !=null && !results.contains(c)) {
    			  results.add(c);
    		  }
    	  }
      }
      
      if (addSuperclasses) {
    	  addSuperclasses(results);
      }
      
      return results;
   }
   
   /**
    * Get measurement protocols used in managed annotations
    * @return list of measurement protocols
    */
   public List<OntologyClass> getActiveMeasurements() {
      List<OntologyClass> results = new ArrayList<OntologyClass>();
		
      ObjectContext context = getDataContext();
		// look up the observation entities
		SelectQuery query = new SelectQuery(DbMeasurement.class);
		List<DbMeasurement> values = context.performQuery(query);
		if (values != null) {
			for (DbMeasurement dbm: values) {
				OntologyClass c = null;
				try {
					c = new OntologyClass(dbm.getTemplate());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.warn("Problem parsing measurement template: " + e.getMessage());
				}
				if(c !=null && !results.contains(c)) {
					results.add(c);
				}
			}
		}
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
              (standards == null || standards.isEmpty()) &&
              (protocols == null || protocols.isEmpty())) {
         return getActiveEntities();
      }
      
      // include subclasses?
      if (searchSubclasses) {
    	  addSubclasses(characteristics);
    	  addSubclasses(standards);
    	  addSubclasses(protocols);
      }
      
      List<OntologyClass> results = new ArrayList<OntologyClass>();
      
      Expression charExpression = null;
      Expression stdExpression = null;
      Expression protocolExpression = null;
      Expression expression = null;
      
      if(characteristics != null && !characteristics.isEmpty()) {
    	  charExpression = ExpressionFactory.inExp("measurements.characteristics.type", getURIs(characteristics));
    	  expression = charExpression;
      }
      if (standards != null && !standards.isEmpty()) {
    	  stdExpression = ExpressionFactory.inExp("measurements.standard", getURIs(standards));
    	  expression = stdExpression;
      }
      if (charExpression != null && stdExpression != null) {
    	  expression = charExpression.andExp(stdExpression);
      }
      // add protocol
      if (protocols != null && !protocols.isEmpty()) {
    	  protocolExpression = ExpressionFactory.inExp("measurements.protocol", getURIs(protocols));
    	  if (expression != null) {
        	  expression = expression.andExp(protocolExpression);
    	  } else {
    		  expression = protocolExpression;
    	  }
      }

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
      
      // should we augment the list with the superclasses?
      if (addSuperclasses) {
    	  addSuperclasses(results);
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
      
      if (searchSubclasses) {
    	  addSubclasses(entities);
    	  addSubclasses(standards);
    	  addSubclasses(protocols);
      }
      List<OntologyClass> results = new ArrayList<OntologyClass>();
      Expression expression = null;
	  Expression entityExpression = null;
      Expression charExpression = null;
	  Expression protocolExpression = null;

      if (entities != null && !entities.isEmpty()) {
    	  entityExpression = ExpressionFactory.inExp("measurement.observation.entity", getURIs(entities));
    	  expression = entityExpression;
      }
      if ((standards == null || !standards.isEmpty())) {
    	  charExpression = ExpressionFactory.inExp("measurement.standard", getURIs(standards));
    	  expression = charExpression;
      }
      if (entityExpression != null && charExpression != null) {
    	  expression = entityExpression.andExp(charExpression);
      }
      // add protocol
      if ((protocols == null || !protocols.isEmpty())) {
    	  protocolExpression = ExpressionFactory.inExp("measurement.protocol", getURIs(protocols));
    	  if (expression != null) {
    		  expression = expression.andExp(protocolExpression);
    	  } else {
    		  expression = protocolExpression;
    	  }
      }
      
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
      List<OntologyClass> results = new ArrayList<OntologyClass>();
      
      Expression entityExpression = null;
      Expression charExpression = null;
      Expression protocolExpression = null;
      Expression expression = null;
      
      if (entities != null && !entities.isEmpty()) {
    	  entityExpression = ExpressionFactory.inExp("observation.entity", getURIs(entities));
    	  expression = entityExpression;
      }
      if (characteristics != null && !characteristics.isEmpty()) {
    	  charExpression = ExpressionFactory.inExp("characteristics.type", getURIs(characteristics));
    	  expression = charExpression;
      }
      if (entityExpression != null && charExpression != null) {
    	  expression = entityExpression.andExp(charExpression);
      }
      //add protocol
      if (protocols != null && !protocols.isEmpty()) {
    	  protocolExpression = ExpressionFactory.inExp("protocol", getURIs(protocols));
    	  if (expression != null) {
    		  expression = expression.andExp(protocolExpression);
    	  }
    	  else {
    		  expression = protocolExpression;
    	  }
      }
      
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
      if (addSuperclasses) {
    	  addSuperclasses(results);
      }
      return results;
   }

   
   /** 
    * This method is called recursively on the subcriteria, building up a weighted return
    * @param a the Annotation to be evaluated for matching
    * @param criteria potentially complex 
    * @return
    */
   private Expression criteriaAsExpression(Criteria criteria, boolean sameObservation, Map<String, List<String>> aliases) {
	   
	   Expression expression = null;
	   
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
			String operator = criteria.getCondition();

			// generate the expression for this single criteria (that may include multiple aspects)
			expression = 
				createExpression(
					entities, 
					characteristics, 
					standards, 
					protocols, 
					contexts, 
					operator, 
					sameObservation, // same observation
					aliases); 
			
	   }
	   else {
		   // iterate through the subcriteria
		   if (criteria.getSubCriteria() != null) {
			   Iterator<Criteria> iter = criteria.getSubCriteria().iterator();
			   while (iter.hasNext()) {
				   Criteria subcriteria = iter.next();
				   // recurse here
				   Expression subExpression = criteriaAsExpression(subcriteria, criteria.isSame(), aliases);
				   if (expression == null) {
					   expression = subExpression;
				   } else {
					   if (criteria.isAll()) {
						   expression = expression.andExp(subExpression);
					   } else {
						   expression = expression.orExp(subExpression);
					   }
				   }
			   }
		   }
	   }
	   
	   return expression;
   }

   private Expression createExpression(
		   List<OntologyClass> entities,
           List<OntologyClass> characteristics, 
           List<OntologyClass> standards,
           List<OntologyClass> protocols, 
           List<Triple> contexts, 
           String equalityOperator, 
           boolean sameObservation,
           Map<String, List<String>> aliases) {
	   
	   Expression terms = null;
	   addSubclasses(entities);
	   addSubclasses(characteristics);
	   addSubclasses(standards);
	   addSubclasses(protocols);
	   
	   //what are we splitting on?
	   String splitPath = "observations";
	   if (sameObservation) {
		   // observations.1...
		   splitPath = "measurements";
	   }
	   // split on measurements
	   List<String> existingAliases = aliases.get(splitPath);
	   int nextAlias = 0;
	   if (existingAliases != null) {
		   nextAlias = Integer.valueOf(existingAliases.get(existingAliases.size()-1));
	   } else {
		   existingAliases = new ArrayList<String>();
	   }
	   nextAlias++;
	   existingAliases.add(String.valueOf(nextAlias));
	   aliases.put(splitPath, existingAliases);
	   
	   String alias = nextAlias + ".measurements";
	   if (sameObservation) {
		   alias = "observations." + nextAlias;
	   }
	   
	   Expression entityExpression = createExpressionForClasses(entities, "observations.entity", equalityOperator);
	   Expression characterExpression = createExpressionForClasses(characteristics, alias + ".characteristics.type", equalityOperator);
	   Expression standardExpression = createExpressionForClasses(standards, alias + ".standard", equalityOperator);
	   Expression protocolExpression = createExpressionForClasses(protocols, alias + ".protocol", equalityOperator);
	   Expression contextExpression = createContextExpression(contexts, false);
	   
	   //TODO handle "and"
	   if (entityExpression != null) {
		   if (terms == null) {
			   terms = entityExpression;
		   } else {
			   terms = terms.orExp(entityExpression);
		   }
	   }
	   if (characterExpression != null) {
		   if (terms == null) {
			   terms = characterExpression;
		   } else {
			   terms = terms.orExp(characterExpression);
		   }
	   }
	   if (standardExpression != null) {
		   if (terms == null) {
			   terms = standardExpression;
		   } else {
			   terms = terms.orExp(standardExpression);
		   }
	   }
	   if (protocolExpression != null) {
		   if (terms == null) {
			   terms = protocolExpression;
		   } else {
			   terms = terms.orExp(protocolExpression);
		   }
	   }
	   if (contextExpression != null) {
		   if (terms == null) {
			   terms = contextExpression;
		   } else {
			   terms = terms.orExp(contextExpression);
		   }
	   }
	   
	   return terms;
	   
   }
   
   private Expression createExpressionForClasses(
		   List<OntologyClass> classes, 
		   String path, 
		   String operator) {
	   
	   Expression expression = null;
	   List<String> values = new ArrayList<String>();
	   if (classes != null && !classes.isEmpty()) {
		   Iterator<OntologyClass> iter = classes.iterator();
		   while (iter.hasNext()) {
			   OntologyClass oc = iter.next();
			   values.add(oc.getURI());
		   }
		   expression = ExpressionFactory.inExp(path, values);
		   // negate the expression if not ==
		   if (operator.equals(Criteria.ISNOT)) {
			   expression = expression.notExp();
		   }
		   
		   return expression;
	   }
	   return null;
   }
   
   private Expression createContextExpression(List<Triple> contextTriples, boolean matchAll) {
	   Expression expression = null;
	   
	   if (contextTriples != null && !contextTriples.isEmpty()) {
		   Iterator<Triple> iter = contextTriples.iterator();
		   while (iter.hasNext()) {
			   Expression contextExpression = ExpressionFactory.expTrue();
			   Triple contextTriple = iter.next();
			   if (contextTriple.a != null) {
				   contextExpression.andExp(ExpressionFactory.matchExp("observations.contexts.observation.entity", contextTriple.a.getURI()));
			   }
			   if (contextTriple.b != null) {
				   contextExpression.andExp(ExpressionFactory.matchExp("observations.contexts.relationship", contextTriple.b.getURI()));
			   }
			   if (contextTriple.c != null) {
				   contextExpression.andExp(ExpressionFactory.matchExp("observations.contexts.observationB.entity", contextTriple.c.getURI()));
			   }
			   // match every triple, or just any?
			   if (matchAll) {
				   if (expression == null) {
					   expression = ExpressionFactory.expTrue();
				   }
				   expression.andExp(contextExpression);
			   } else {
				   if (expression == null) {
					   expression = ExpressionFactory.expFalse();
				   }
				   expression.orExp(contextExpression);
			   }
		   }
	   }
	   return expression;
   }

   public static void main(String[] args) {
		try {
	
			//String annot1 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/er-2008-ex1-annot.xml";
			String annot1 = "http://fred.msi.ucsb.edu:8080/knb/metacat/benriver.216.6";

	        URL url = new URL(annot1);
	
	        // get annotation manager
	        AnnotationManager annotationManager = SMS.getInstance().getAnnotationManager();
	        Annotation annotation = Annotation.read(url.openStream());
	        annotationManager.importAnnotation(annotation, url.toString());
			
	        // look up the db annotation directly
			//DbAnnotation dbAnnotation = ((DbAnnotationManager)annotationManager).getDbAnnotation(annotation);
	 	   //ObjectContext context = DataContext.createDataContext();
	 	   ObjectContext context = ((DbAnnotationManager)annotationManager).getDataContext();
	 	   
	 	   //test
	 	   DbCharacteristic dbChar = queryChar("https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl#Macrocystis", context);
	 	   
	        DbAnnotation dbAnnotation = query(null, context);
	        
			// print it out
			System.out.println("Annotation: " + dbAnnotation);
			System.out.println();
			List<DbObservation> dbObservations = dbAnnotation.getObservations();
			for (DbObservation dbo: dbObservations) {
				System.out.println("Observation: " + dbo);
				System.out.println();
				for (DbMeasurement dbm: dbo.getMeasurements()) {
					System.out.println("Measurement: " + dbm);
					for (DbCharacteristic dbc: dbm.getCharacteristics()) {
						System.out.println(dbc);
					}
				}
//				List<DbContext> contexts = dbo.getContexts();
//				System.out.println("Context count: " + contexts.size());
//				for (DbContext dbc: contexts) {
//					//System.out.println(dbc);
//					System.out.println("--------");
//					System.out.println(dbc.getObservation());
//					System.out.println(dbc.getRelationship());
//					System.out.println(dbc.getObservationB());
//				}
				System.out.println();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
   
   private static DbAnnotation query(String param, ObjectContext context) {
		
		DbAnnotation dbAnnotation = null;

		String queryString = 
	 		   "(observations.entity = 'https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl#Macrocystis')"
	 		   + " and " +
	 		   "(observations.measurements|characteristics.type = 'https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl#DryMass')"
//	 		   + " and " +
//	 		   "(observations.measurements.characteristics.type = 'https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl#WetMass')"
	 		   ;
		List characteristics = new ArrayList();
		characteristics.add("https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl#DryMass");
		characteristics.add("https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl#WetMass");
		
		// look up the annotation
		Expression exp = ExpressionFactory.expTrue(); //ExpressionFactory.inExp("observations.entity", "https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl#Macrocystis");
		int splitCounter = 0;
//		exp = exp.andExp(
//				ExpressionFactory.matchAllExp("observations|measurements.characteristics.type", characteristics)
//				);
		exp = exp.andExp(ExpressionFactory.inExp("" + splitCounter++ + ".measurements.characteristics.type", characteristics.get(0)));
		exp = exp.andExp(ExpressionFactory.inExp("" + splitCounter++ + ".measurements.characteristics.type", characteristics.get(1)));

		
		final Expression expression = exp; //Expression.fromString(queryString);
		Map<String, String> params = new HashMap<String, String>();
		params.put("param", param);
		SelectQuery query = new SelectQuery(DbAnnotation.class, expression);
		query.aliasPathSplits("observations", "0", "1");
		List values = context.performQuery(query);
		if (values != null && !values.isEmpty()) {
			dbAnnotation = (DbAnnotation) values.get(0);
		}
		return dbAnnotation;
   }
   
   private static DbCharacteristic queryChar(String param, ObjectContext context) throws Exception {
	   
		DbCharacteristic dbCharacteristic = null;
		List<OntologyClass> entities = new ArrayList<OntologyClass>();
		entities.add(new Entity(param));
		final Expression expression = ExpressionFactory.inExp("measurement.observation.entity", getURIs(entities));
		
		SelectQuery query = new SelectQuery(DbCharacteristic.class, expression);
		
		List values = context.performQuery(query);
		if (values != null && !values.isEmpty()) {
			dbCharacteristic = (DbCharacteristic) values.get(0);
		}
		return dbCharacteristic;
  }
   
} 