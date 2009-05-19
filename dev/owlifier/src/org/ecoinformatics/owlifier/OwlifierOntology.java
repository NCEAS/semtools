/**
 * Copyright (c) 2009 The Regents of the University of California.
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
package org.ecoinformatics.owlifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLObjectProperty;

/**
 * 
 * @author Shawn Bowers
 */
public class OwlifierOntology {

   private OwlifierSpreadsheet sheet;
   private boolean oboe;
   private URI uri;
   private OWLOntologyManager manager;
   private OWLOntology ontology;

   /**
    * Create an owlifier ontology from an owlifier spreadsheet
    * @param sheet the spreadsheet
    * @param uri the ontology uri
    * @param oboe if true, model as an oboe extension ontology
    */
   public OwlifierOntology(OwlifierSpreadsheet sheet, URI uri, boolean oboe)
         throws Exception {
      this.sheet = sheet;
      this.uri = uri;
      this.oboe = oboe;
      // set up the manager and create the ontology
      manager = OWLManager.createOWLOntologyManager();
      ontology = manager.createOntology(uri);
      // build up the ontology
      if(oboe)
         buildOboeOntology();
      else
         buildStandardOntology();
   }

   /**
    * Get the owlifier spreadsheet backing this ontology
    * @return the spreadsheet
    */
   public OwlifierSpreadsheet getSpreadsheet() {
      return sheet;
   }

   /**
    * Check if this is an OBOE extension ontology
    * @return true if this is an OBOE extension ontology
    */
   public boolean isOboeExtension() {
      return oboe;
   }

   /**
    * Get the uri assigned to the ontology
    * @return the uri
    */
   public URI getURI() {
      return uri;
   }

   /**
    * TODO Classify the ontology
    */
   public void classify() {
   }

   /**
    * TODO Check whether the ontology is consistent
    */
   public void checkConsistency() throws Exception {
   }

   /**
    * Write the ontology to RDF/XML OWL representation
    */
   public void writeAsRDFXML() throws Exception {
      manager.saveOntology(ontology);
   }

   /**
    */
   private void buildStandardOntology() throws Exception {
      OWLDataFactory factory = manager.getOWLDataFactory();
      OWLAxiom axiom = null;
      OWLClass class1 = null, class2 = null;
      OWLObjectProperty rel = null;
      // get imports
      for(URI importURI : getImports()) {
         axiom = factory.getOWLImportsDeclarationAxiom(ontology, importURI);
         manager.applyChange(new AddAxiom(ontology, axiom));
      }
      // get entities
      for(String entity : getEntities()) {
         class1 = factory.getOWLClass(getURI(entity));
         axiom = factory.getOWLDeclarationAxiom(class1);
         manager.applyChange(new AddAxiom(ontology, axiom));
      }
      // get relationships
      for(String relationship : getRelationships()) {
         rel = factory.getOWLObjectProperty(getURI(relationship));
         axiom = factory.getOWLDeclarationAxiom(rel);
         manager.applyChange(new AddAxiom(ontology, axiom));
      }
      // get subclasses
      Map<String, List<String>> subclasses = getSubclasses();
      for(String parentEntity : subclasses.keySet()) {
         class1 = factory.getOWLClass(getURI(parentEntity));
         for(String childEntity : subclasses.get(parentEntity)) {
            class2 = factory.getOWLClass(getURI(childEntity));
            axiom = factory.getOWLSubClassAxiom(class1, class2);
            manager.applyChange(new AddAxiom(ontology, axiom));
         }
      }
      // get synonyms
      for(List<String> synonyms : getSynonyms()) {
         Set<OWLClass> classes = new HashSet();
         for(String entity : synonyms) {
            OWLClass c = factory.getOWLClass(getURI(entity));
            if(!classes.contains(c))
               classes.add(c);
         }
         axiom = factory.getOWLEquivalentClassesAxiom(classes);
         manager.applyChange(new AddAxiom(ontology, axiom));
      }
      // get description comments
      Map<String, List<String>> descriptions = getDescriptions();
      for(String entity : descriptions.keySet())
         for(String desc : descriptions.get(entity)) {
            class1 = factory.getOWLClass(getURI(entity));
            OWLAnnotation a = factory.getCommentAnnotation(desc, "en");
            axiom = factory.getOWLEntityAnnotationAxiom(class1, a);
            manager.applyChange(new AddAxiom(ontology, axiom));
         }
   // TODO get disjoint classes

   }

   /**
    */
   private void buildOboeOntology() throws Exception {
   }

   /**
    */
   private URI getURI(String str) {
      return URI.create(uri + "#" + str);
   }

   /**
    * Get the list of imports
    */
   private List<URI> getImports() throws Exception {
      List<URI> result = new ArrayList();
      for(OwlifierRow r : sheet.getRows())
         if(r.getBlockType() == OwlifierBlockType.IMPORT) {
            URI importURI = new URI(r.getColumn(1).getValue());
            if(!result.contains(importURI))
               result.add(importURI);
         }
      return result;
   }

   /**
    * Get the list of entities
    */
   private Set<String> getEntities() {
      return sheet.getEntities();
   }

   private Set<String> getRelationships() {
      return sheet.getRelationships();
   }

   /**
    * Get the subclass relationships
    */
   private Map<String, List<String>> getSubclasses() {
      Map<String, List<String>> result = new HashMap();
      for(OwlifierRow r : sheet.getRows())
         if(r.getBlockType() == OwlifierBlockType.ENTITY)
            for(int i = 1; i + 1 < r.getLength(); i++) {
               String parentEntity = r.getColumn(i).getValue();
               String childEntity = r.getColumn(i + 1).getValue();
               if(result.containsKey(parentEntity)) {
                  List<String> children = result.get(parentEntity);
                  if(!children.contains(childEntity))
                     children.add(childEntity);
               } else {
                  List<String> children = new ArrayList();
                  children.add(childEntity);
                  result.put(parentEntity, children);
               }
            }
      return result;
   }

   /**
    * Get synonym classes
    */
   private List<List<String>> getSynonyms() {
      List<List<String>> result = new ArrayList();
      for(OwlifierRow r : sheet.getRows())
         if(r.getBlockType() == OwlifierBlockType.SYNONYM) {
            List<String> synonyms = new ArrayList();
            for(int i = 0; i < r.getLength(); i++) {
               String entity = r.getColumn(i).getValue();
               if(!synonyms.contains(entity))
                  synonyms.add(entity);
            }
            result.add(synonyms);
         }
      return result;
   }

   /**
    * Get description comments
    */
   private Map<String, List<String>> getDescriptions() {
      Map<String, List<String>> result = new HashMap();
      for(OwlifierRow r : sheet.getRows())
         if(r.getBlockType() == OwlifierBlockType.DESCRIPTION) {
            String entity = r.getColumn(0).getValue();
            List<String> descs = new ArrayList();
            if(result.containsKey(entity))
               descs = result.get(entity);
            else
               result.put(entity, descs);
            descs.add(r.getColumn(1).getValue());
         }
      return result;
   }
}