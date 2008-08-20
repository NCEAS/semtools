
/**
 *    '$RCSfile: Annotation.java,v $'
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
package org.ecoinformatics.sms.annotation;

import java.util.List;
import java.util.ArrayList;
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
   public void setEMLPackage(String id) {
      _emlPackage = id;
   }

   /**
    * Get the eml package identifier of this annotation
    * @return id the package id
    */
   public String getEMLPackage() {
      return _emlPackage;
   }

   /**
    * Set the data table identifier of this annotation
    * @param id the table id
    */
   public void setDataTable(String id) {
      _dataTable = id;
   }

   /**
    * Get the data table identifier of this annotation
    * @return id the table id
    */
   public String getDataTable() {
      return _dataTable;
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
    * Add an ontology to this annotation
    * @param o the ontology to add
    */
   public void addOntology(Ontology o) {
      if(o != null && !_ontologies.contains(o))
         _ontologies.add(o);
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
   public List<Ontology> getOntologies() {
      return _ontologies;
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
            for(Characteristic c : m.getCharacteristics())
               if(!result.contains(c))
                  result.add(c);
            for(Entity v : m.getDomainValues())
               if(!result.contains(v))
                  result.add(v);
         }
         for(Context x : o.getContexts()) {
            Relationship r = x.getRelationship();
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
      AnnotationWriter.write(this, s);
   }

   /** 
    * Read an annotation from an input stream
    * @param s the input stream
    * @return an annotation
    */
   public static Annotation read(InputStream s) throws AnnotationException {
      return AnnotationReader.read(s);
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
   private String _emlPackage;
   private String _dataTable;
   private List<Ontology> _ontologies = new ArrayList();
   private List<Observation> _observations = new ArrayList();
   private List<Mapping> _mappings = new ArrayList();
   public static String ANNOTATION_NS =
      "http://daks.ucdavis.edu/sms-annot-1.0.0rc1";

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
