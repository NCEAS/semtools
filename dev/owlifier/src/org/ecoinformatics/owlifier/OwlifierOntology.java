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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
      ontology = manager.createOntology(IRI.create(uri));
      // build up the ontology
      if(oboe)
         buildOboeOntology();
      else
         buildStandardOntology();
   }

   /** 
    * Get the OWL-API Ontology Manager
    * @return the ontology manager
    */
   public OWLOntologyManager getOWLOntologyManager() {
      return manager;
   }

   /**
    * Get the OWL-API Ontology
    * @return the ontology
    */
   public OWLOntology getOWLOntology() {
      return ontology;
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
    * Check if the given item is a class
    * @param item the item
    * @return true if the item is a class
    * @throws java.lang.Exception
    */
   public boolean isClass(String item) throws Exception {
      for(OwlifierRow row : getSpreadsheet().getRows()) {
         OwlifierBlock b = row.getBlock();
         if(b instanceof OwlifierEntityBlock)
            if(((OwlifierEntityBlock) b).getBlockType().equals(item))
               return true;
         if(b instanceof OwlifierSynonymBlock)
            if(((OwlifierSynonymBlock) b).getSynonyms().contains(item))
               return true;
         if(b instanceof OwlifierExactBlock)
            if(((OwlifierExactBlock) b).getEntities().contains(item))
               return true;
         if(b instanceof OwlifierMaxBlock)
            if(((OwlifierMaxBlock) b).getEntities().contains(item))
               return true;
         if(b instanceof OwlifierMinBlock)
            if(((OwlifierMinBlock) b).getEntities().contains(item))
               return true;
         if(b instanceof OwlifierExactBlock)
            if(((OwlifierExactBlock) b).getEntities().contains(item))
               return true;
         if(b instanceof OwlifierOverlapBlock)
            if(((OwlifierOverlapBlock) b).getOverlap().contains(item))
               return true;
         if(b instanceof OwlifierRelationshipBlock)
            if(((OwlifierRelationshipBlock) b).getEntities().contains(item))
               return true;
         if(b instanceof OwlifierTransitiveBlock)
            if(((OwlifierTransitiveBlock) b).getEntities().contains(item))
               return true;
         if(b instanceof OwlifierSufficientBlock) {
            if(((OwlifierSufficientBlock) b).getEntity().equals(item))
               return true;
            else if(((OwlifierSufficientBlock) b).getEntities().contains(item))
               return true;
         }
      }
      return false;
   }

   /**
    * Check if the item is a property (relationship)
    * @param item the item
    * @return true if the item is a property (relationship)
    */
   public boolean isProperty(String item) throws Exception {
      for(OwlifierRow row : getSpreadsheet().getRows()) {
         OwlifierBlock b = row.getBlock();
         if(b instanceof OwlifierExactBlock)
            if(((OwlifierExactBlock) b).getRelationship().equals(item))
               return true;
         if(b instanceof OwlifierMaxBlock)
            if(((OwlifierMaxBlock) b).getRelationship().equals(item))
               return true;
         if(b instanceof OwlifierMinBlock)
            if(((OwlifierMinBlock) b).getRelationship().equals(item))
               return true;
         if(b instanceof OwlifierExactBlock)
            if(((OwlifierExactBlock) b).getRelationship().equals(item))
               return true;
         if(b instanceof OwlifierRelationshipBlock)
            if(((OwlifierRelationshipBlock) b).getRelationship().equals(item))
               return true;
         if(b instanceof OwlifierTransitiveBlock)
            if(((OwlifierTransitiveBlock) b).getRelationship().equals(item))
               return true;
         if(b instanceof OwlifierInverseBlock) {
            if(((OwlifierInverseBlock) b).getRelationship1().equals(item))
               return true;
            if(((OwlifierInverseBlock) b).getRelationship2().equals(item))
               return true;
         }
         if(b instanceof OwlifierSufficientBlock) {
            OwlifierSufficientBlock s = (OwlifierSufficientBlock)b;
            if(s.hasProperty() && s.getProperty().equals(item))
               return true;
         }
      }
      return false;
   }

   /**
    * Construct the ontology
    */
   private void buildStandardOntology() throws Exception {
      // add the non-description blocks
      for(OwlifierRow row : getSpreadsheet().getRows()) {
         OwlifierBlock block = row.getBlock();
         if(!(block instanceof OwlifierDescriptionBlock) &&
               !(block instanceof OwlifierOverlapBlock))
            block.addToOntology(this);
      }
      // add the description blocks
      for(OwlifierRow row : getSpreadsheet().getRows()) {
         OwlifierBlock block = row.getBlock();
         if(block instanceof OwlifierDescriptionBlock)
            block.addToOntology(this);
      }

      // TODO:
      // 1. assert disjoint axioms
      // 2. remove overlaps blocks
      // 3. combine per entity the sufficient block axioms

   }

   /**
    */
   private void buildOboeOntology() throws Exception {
   }

   /**
    */
   public URI getURI(String str) {
      return URI.create(uri + "#" + str);
   }
//
//   /**
//    * Get the list of imports
//    */
//   private List<URI> getImports() throws Exception {
//      List<URI> result = new ArrayList();
//      for(OwlifierRow r : sheet.getRows())
//         if(r.getBlockType() == OwlifierBlockType.IMPORT) {
//            URI importURI = new URI(r.getColumn(1).getTrimmedValue());
//            if(!result.contains(importURI))
//               result.add(importURI);
//         }
//      return result;
//   }
//
//   /**
//    * Get the list of entities
//    */
//   private Set<String> getClasses() {
//      return sheet.getClasses();
//   }
//
//   private Set<String> getObjectProperties() {
//      return sheet.getObjectProperties();
//   }
//
//   /**
//    * Get the subclass relationships
//    */
//   private Map<String, Set<String>> getSubclasses() {
//      Map<String, Set<String>> result = new HashMap();
//      for(OwlifierRow r : sheet.getRows(OwlifierBlockType.ENTITY))
//         for(int i = 0; i + 1 < r.getLength(); i++) {
//            String parentEntity = r.getColumn(i).getTrimmedValue();
//            String childEntity = r.getColumn(i + 1).getTrimmedValue();
//            Set<String> children = new HashSet();
//            if(result.containsKey(parentEntity))
//               children = result.get(parentEntity);
//            else
//               result.put(parentEntity, children);
//            children.add(childEntity);
//         }
//      return result;
//   }
//
//   /**
//    * Get synonym classes
//    */
//   private Set<Set<String>> getSynonyms() {
//      Set<Set<String>> result = new HashSet();
//      for(OwlifierRow r : sheet.getRows(OwlifierBlockType.SYNONYM)) {
//         Set<String> synonyms = new HashSet();
//         for(int i = 0; i < r.getLength(); i++) {
//            String entity = r.getColumn(i).getTrimmedValue();
//            synonyms.add(entity);
//         }
//         result.add(synonyms);
//      }
//      return result;
//   }
//
//   /**
//    * Get description comments
//    * @return descriptions
//    */
//   private Map<String, Set<String>> getDescriptions() {
//      Map<String, Set<String>> result = new HashMap();
//      for(OwlifierRow r : sheet.getRows(OwlifierBlockType.DESCRIPTION)) {
//         String entity = r.getColumn(0).getTrimmedValue();
//         Set<String> descs = new HashSet();
//         if(result.containsKey(entity))
//            descs = result.get(entity);
//         else
//            result.put(entity, descs);
//         descs.add(r.getColumn(1).getTrimmedValue());
//      }
//      return result;
//   }
//
//   /**
//    * Get relationship blocks
//    * @return relationship definitions
//    */
//   private List<List<String>> getRelationships() {
//      // first element of contained list is the relname, rest are class names
//      List<List<String>> result = new ArrayList();
//      for(OwlifierRow r : sheet.getRows(OwlifierBlockType.RELATIONSHIP)) {
//         List<String> propRow = new ArrayList();
//         result.add(propRow);
//         String rel = r.getColumn(0).getTrimmedValue();
//         propRow.add(rel);
//         for(int i = 1; i < r.getLength(); i++)
//            propRow.add(r.getColumn(i).getTrimmedValue());
//      }
//      return result;
//   }
//
//   /**
//    * Get transitive blocks
//    * @return transitive definitions
//    */
//   private List<List<String>> getTransitives() {
//      // first element of contained list is the relname, rest are class names
//      List<List<String>> result = new ArrayList();
//      for(OwlifierRow r : sheet.getRows(OwlifierBlockType.TRANSITIVE)) {
//         List<String> propRow = new ArrayList();
//         result.add(propRow);
//         // add the property
//         propRow.add(r.getColumn(0).getTrimmedValue());
//         for(int i = 1; i < r.getLength(); i++)
//            propRow.add(r.getColumn(i).getTrimmedValue());
//      }
//
//      return result;
//   }
//
//   /**
//    * Get max property blocks
//    * @return max properties
//    */
//   private List<List<String>> getMaxProperties() {
//      // first element is the property, then integer cardinality, then classes
//      List<List<String>> result = new ArrayList();
//      for(OwlifierRow r : sheet.getRows(OwlifierBlockType.MAX)) {
//         List<String> maxRow = new ArrayList();
//         result.add(maxRow);
//         // add the property
//         maxRow.add(r.getColumn(0).getTrimmedValue());
//         // add the integer
//         maxRow.add(r.getColumn(1).getTrimmedValue());
//         // add the classes
//         for(int i = 2; i < r.getLength(); i++)
//            maxRow.add(r.getColumn(i).getTrimmedValue());
//      }
//      return result;
//   }
//
//   /**
//    * Get min property blocks
//    * @return min properties
//    */
//   private List<List<String>> getMinProperties() {
//      // first element is the property, then integer cardinality, then classes
//      List<List<String>> result = new ArrayList();
//      for(OwlifierRow r : sheet.getRows(OwlifierBlockType.MIN)) {
//         List<String> minRow = new ArrayList();
//         result.add(minRow);
//         // add the property
//         minRow.add(r.getColumn(0).getTrimmedValue());
//         // add the integer
//         minRow.add(r.getColumn(1).getTrimmedValue());
//         // add the classes
//         for(int i = 2; i < r.getLength(); i++)
//            minRow.add(r.getColumn(i).getTrimmedValue());
//      }
//      return result;
//   }
//
//   /**
//    * Get exact property blocks
//    * @return exact properties
//    */
//   private List<List<String>> getExactProperties() {
//      // first element is the property, then integer cardinality, then classes
//      List<List<String>> result = new ArrayList();
//      for(OwlifierRow r : sheet.getRows(OwlifierBlockType.MIN)) {
//         List<String> exactRow = new ArrayList();
//         result.add(exactRow);
//         // add the property
//         exactRow.add(r.getColumn(0).getTrimmedValue());
//         // add the integer
//         exactRow.add(r.getColumn(1).getTrimmedValue());
//         // add the classes
//         for(int i = 2; i < r.getLength(); i++)
//            exactRow.add(r.getColumn(i).getTrimmedValue());
//      }
//      return result;
//   }
//
//   /**
//    * Get inverse properties
//    * @return inverse properties
//    */
//   private Map<String, Set<String>> getInverses() {
//      Map<String, Set<String>> result = new HashMap();
//      for(OwlifierRow r : sheet.getRows(OwlifierBlockType.INVERSE)) {
//         String prop = r.getColumn(0).getTrimmedValue();
//         Set<String> inverses = new HashSet();
//         if(result.containsKey(prop))
//            inverses = result.get(prop);
//         else
//            result.put(prop, inverses);
//         inverses.add(r.getColumn(1).getTrimmedValue());
//      }
//      return result;
//   }
//
//   /**
//    * Get sufficient blocks
//    * @return sufficient definitions
//    */
//   private Map<String, List<Set<String>>> getSufficients() {
//      // first element: entity name
//      // first list has 3 elems: relationships, not-relationships, entities
//      // first elem of relationships and not-relationships: relationship name
//      Map<String, List<Set<String>>> result = new HashMap();
//      // build up result for each entity
//      for(String ent : getSufficientEntities()) {
//         List<Set<String>> entry = new ArrayList();
//         result.put(ent, entry);
//         entry.add(0, new HashSet()); // the sufficient relationships
//         entry.add(1, new HashSet()); // the sufficient not-relationshps
//         entry.add(2, new HashSet()); // the sufficient entities
//      }
//      // fill in the entries
//      for(OwlifierRow r : sheet.getRows(OwlifierBlockType.SUFFICIENT))
//         if(r.isSufficientRelationshipBlock()) {
//            String rel = r.getColumn(1).getTrimmedValue();
//            for(int i = 2; i < r.getLength() + 1; i++) {
//               String ent1 = r.getColumn(i).getTrimmedValue();
//               String ent2 = r.getColumn(i + 1).getTrimmedValue();
//               Set<String> suffRel = result.get(ent1).get(0);
//               suffRel.add(rel);
//               suffRel.add(ent2);
//            }
//         } else if(r.isSufficientNotRelationshipBlock()) {
//            // TODO
//         } else if(r.isEntityBlock()) {
//            // TODO
//         }
//
//      return result;
//   }
//
//   private Set<String> getSufficientEntities() {
//      Set<String> result = new HashSet();
//      // TODO
//      return result;
//   }
}