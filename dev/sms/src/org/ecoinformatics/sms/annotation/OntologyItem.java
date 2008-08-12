
/**
 *    '$RCSfile: OntologyItem.java,v $'
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

/**
 * Objects of this class represent ontology items (entities,
 * relationships, etc.)
 */
public class OntologyItem {

   /**
    * Default constructor
    */
   public OntologyItem() {
   }

   /**
    * @param ontology the ontology
    * @param name the name
    */
   public OntologyItem(Ontology ontology, String name) {
      _ontology = ontology;
      _name = name;
   }

   /** 
    * Set the ontology of this item
    * @param ontology the ontology
    */
   public void setOntology(Ontology ontology) {
      _ontology = ontology;
   }

   /** 
    * Get the ontology of this item
    * @return the ontology
    */
   public Ontology getOntology() {
      return _ontology;
   }

   /**
    * Set the name used for this item in the ontology
    * @param name the name of the item
    */
   public void setNamespace(String name) {
      _name = name;
   }

   /**
    * Get the name used to denote this item in the ontology
    * @return the name
    */
   public String getName() {
      return _name;
   }

   /**
    * Set the name used to denote this item in the ontology
    * @param name the name
    */
   public void setName(String name) {
      _name = name;
   }

   /** 
    * Get the uri associated with this item
    * @return the uri
    */
   public String getURI() {
      if(_ontology != null)
         return _ontology.getURI() + "#" + getName();
      return "#" + getName();
   }

   public String toString() {
      return getURI();
   }
   
   public boolean equals(Object obj) {
      if(!(obj instanceof OntologyItem))
         return false;
      OntologyItem item = (OntologyItem) obj;
      Ontology ont = item.getOntology();
      if(ont != null && !ont.equals(getOntology()))
         return false;
      String name = item.getName();
      if(name != null && !name.equals(getName()))
         return false;
      return true;
   }

   private Ontology _ontology;
   private String _name;
} 