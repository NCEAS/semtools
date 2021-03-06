
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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Objects of this class represent observations
 */
public class Observation implements Comparable {

   /** 
    * Set the label of this observation
    * @param label the label
    */
   public void setLabel(String label) {
      _label = label;
   }

   /** 
    * Get the label of this observation
    * @return the label
    */
   public String getLabel() {
      return _label;
   }

   /**
    * Set the entity type of this observation
    * @param entity the entity type
    */
   public void setEntity(Entity entity) {
      _entity = entity;
   }

   /**
    * Get the entity type of this observation
    * @return the entity type
    */
   public Entity getEntity() {
      return _entity;
   }

   /** 
    * Add a measurement to this observation
    * @param measurement the measurement
    */
   public void addMeasurement(Measurement measurement) {
      if(measurement != null && !_measurements.contains(measurement))
         _measurements.add(measurement);
   }

   /**
    * Remove a measurement form this observation
    * @param measurement the measurement to remove
    */
   public void removeMeasurement(Measurement measurement) {
      _measurements.remove(measurement);
   }

   /**
    * Get the measurements assigned to this observation
    * @return the set of measurement
    */
   public List<Measurement> getMeasurements() {
      return _measurements;
   }

   /** 
    * Add a context to this observation
    * @param context the context
    */
   public void addContext(Context context) {
      if(context != null && !_contexts.contains(context))
         _contexts.add(context);
   }

   /**
    * Remove a context form this observation
    * @param context the context to remove
    */
   public void removeContext(Context context) {
      _contexts.remove(context);
   }

   /**
    * Get the contexts assigned to this observation
    * @return the set of contexts
    */
   public List<Context> getContexts() {
      return _contexts;
   }

   /**
    * Get the context type given this observation type's relationship name 
    * (it should be unique, otherwise, there would be error) //TODO: check this with ben
    * 
    * @author cao
    * @param contextRelationshipName
    * @return
    */
   public Context getContext(String contextRelationshipName)
   {
	   for(Context c: _contexts){
		   if(c.getRelationship().getName().equals(contextRelationshipName)){
			   return c;
		   }
	   }
	   return null;
   }
   /**
    * Set whether this is a distinct observation
    * @param isDistinct if true, this is a distinct observation
    */
   public void setDistinct(boolean isDistinct) {
      _isDistinct = isDistinct;
   }

   /**
    * Get the distinct status of this observation
    * @return true if this is a distinct observation
    */
   public boolean isDistinct() {
      return _isDistinct;
   }
   
   /**
    * Get the distinct status of this observation
    * @return true if this is a distinct observation
    */
   public boolean containsMeasurement(Measurement measurement) {
      for (Measurement m: _measurements) {
    	  if (m.equals(measurement)) {
    		  return true;
    	  }
      }
      return false;
   }
   
   /**
    * Check for matching Observation in Context list
    * @return true if contains a context with the observation
    */
   public boolean containsObservation(Observation obs) {
      for (Context c: _contexts) {
    	  Observation targetObs = c.getObservation();
    	  if (targetObs == null) {
    		  continue;
    	  }
    	  if (targetObs.equals(obs)) {
    		  return true;
    	  }
      }
      return false;
   }
   
   /**
    * Get the measurement types which are marked with "key yes" for this observation type
    * 
    * @author cao
    * @return a list of measurement types marked with "key yes"
    */
   public List<Measurement> getKeyMeasurements()
   {
	   List<Measurement> keyMeasurementList = new ArrayList<Measurement>();
	   for(Measurement mType: _measurements){
		   if(mType.isKey()){
			   keyMeasurementList.add(mType);
		   }
	   }
	   return keyMeasurementList;
   }
   
   public String toString() {
	   return getFullString();
   }
   
   protected String getFullString() {
	   StringBuffer desc = new StringBuffer();
	   if (_entity != null) {
		   desc.append(_entity.toString());
	   }
	   if (_measurements != null) {
		   desc.append(" [");
		   Iterator<Measurement> iter = _measurements.iterator();
		   while (iter.hasNext()) {
			   Measurement m = iter.next();
			   Mapping mapping = m.getMapping();
			   if (mapping != null) {
				   desc.append(mapping.getAttribute());
				   if (iter.hasNext()) {
					   desc.append(", ");
				   }
			   }
		   }
		   desc.append("]");
	   }
	   // TODO: probably don't need label if we have the attributes
//	   if (_label != null) {
//		   desc.append(" (" + _label + ")"); 
//	   }
	   return desc.toString();
   }
   
   /**
    * do an alpha sort by label - really we only care about equality
    */
   public int compareTo(Object o) {
	   if (o instanceof Observation) {
		   Observation obs = (Observation) o;
		   if (obs.getLabel() != null) {
			   return this.getLabel().compareTo(obs.getLabel());
		   }
	   }
	   return 0;
	}

   private String _label;
   private Entity _entity;
   private List<Measurement> _measurements = new ArrayList();
   private List<Context> _contexts = new ArrayList();
   private boolean _isDistinct;
}