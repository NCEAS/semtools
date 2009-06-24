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

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
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
    */
   private void buildStandardOntology() throws Exception {
      for(OwlifierRow row : getSpreadsheet().getRows()) {
         OwlifierBlock block = row.getBlock();
         block.addToOntology(this);
      }
//      OWLDataFactory factory = manager.getOWLDataFactory();
//      OWLAxiom axiom = null;
//      OWLClass class1 = null, class2 = null;
//      OWLObjectProperty rel = null;
//      // get classes
//      for(String entity : getClasses()) {
//         class1 = factory.getOWLClass(getURI(entity));
//         axiom = factory.getOWLDeclarationAxiom(class1);
//         manager.applyChange(new AddAxiom(ontology, axiom));
//      }
//      // get subclasses
//      Map<String, Set<String>> subclasses = getSubclasses();
//      for(String parentEntity : subclasses.keySet()) {
//         class1 = factory.getOWLClass(getURI(parentEntity));
//         for(String childEntity : subclasses.get(parentEntity)) {
//            class2 = factory.getOWLClass(getURI(childEntity));
//            axiom = factory.getOWLSubClassAxiom(class2, class1);
//            manager.applyChange(new AddAxiom(ontology, axiom));
//         }
//      }
//      // get relationships
//      for(String prop : getObjectProperties()) {
//         rel = factory.getOWLObjectProperty(getURI(prop));
//         axiom = factory.getOWLDeclarationAxiom(rel);
//         manager.applyChange(new AddAxiom(ontology, axiom));
//      }
//      // get description comments
//      Map<String, Set<String>> descriptions = getDescriptions();
//      for(String item : descriptions.keySet())
//         for(String desc : descriptions.get(item)) {
//            OWLAnnotation a = factory.getCommentAnnotation(desc, "en");
//            if(getClasses().contains(item)) {
//               class1 = factory.getOWLClass(getURI(item));
//               axiom = factory.getOWLEntityAnnotationAxiom(class1, a);
//            } else if(getObjectProperties().contains(item)) {
//               rel = factory.getOWLObjectProperty(getURI(item));
//               axiom = factory.getOWLEntityAnnotationAxiom(rel, a);
//            }
//            manager.applyChange(new AddAxiom(ontology, axiom));
//         }
//      // min cardinality
//      for(List<String> maxRow : getMaxProperties()) {
//         rel = factory.getOWLObjectProperty(getURI(maxRow.get(0)));
//         int n;
//         try {
//            n = (new Integer(maxRow.get(1))).intValue();
//         } catch(Exception e) {
//            throw new Exception("Invalid max cardinality: " + maxRow.get(1));
//         }
//         for(int i = 2; i + 1 < maxRow.size(); i++) {
//            class1 = factory.getOWLClass(getURI(maxRow.get(i)));
//            class2 = factory.getOWLClass(getURI(maxRow.get(i + 1)));
//            OWLDescription d =
//                  factory.getOWLObjectMaxCardinalityRestriction(rel, n, class2);
//            axiom = factory.getOWLSubClassAxiom(class1, d);
//            manager.applyChange(new AddAxiom(ontology, axiom));
//         }
//      }
//      // min cardinality
//      for(List<String> minRow : getMinProperties()) {
//         rel = factory.getOWLObjectProperty(getURI(minRow.get(0)));
//         int n;
//         try {
//            n = (new Integer(minRow.get(1))).intValue();
//         } catch(Exception e) {
//            throw new Exception("Invalid min cardinality: " + minRow.get(1));
//         }
//         for(int i = 2; i + 1 < minRow.size(); i++) {
//            class1 = factory.getOWLClass(getURI(minRow.get(i)));
//            class2 = factory.getOWLClass(getURI(minRow.get(i + 1)));
//            OWLDescription d =
//                  factory.getOWLObjectMinCardinalityRestriction(rel, n, class2);
//            axiom = factory.getOWLSubClassAxiom(class1, d);
//            manager.applyChange(new AddAxiom(ontology, axiom));
//         }
//      }
//      // exact cardinality
//      for(List<String> exactRow : getMinProperties()) {
//         rel = factory.getOWLObjectProperty(getURI(exactRow.get(0)));
//         int n;
//         try {
//            n = (new Integer(exactRow.get(1))).intValue();
//         } catch(Exception e) {
//            throw new Exception("Invalid exact cardinality: " + exactRow.get(1));
//         }
//         for(int i = 2; i + 1 < exactRow.size(); i++) {
//            class1 = factory.getOWLClass(getURI(exactRow.get(i)));
//            class2 = factory.getOWLClass(getURI(exactRow.get(i + 1)));
//            OWLDescription d =
//                  factory.getOWLObjectExactCardinalityRestriction(rel, n, class2);
//            axiom = factory.getOWLSubClassAxiom(class1, d);
//            manager.applyChange(new AddAxiom(ontology, axiom));
//         }
//      }
//      // inverse
//      Map<String, Set<String>> inverses = getInverses();
//      for(String prop : inverses.keySet()) {
//         rel = factory.getOWLObjectProperty(getURI(prop));
//         for(String inv : inverses.get(prop)) {
//            OWLObjectProperty irel = factory.getOWLObjectProperty(getURI(inv));
//            axiom = factory.getOWLInverseObjectPropertiesAxiom(rel, irel);
//            manager.applyChange(new AddAxiom(ontology, axiom));
//         }
//
//      }
//      // sufficient relationship
//      for(List<String> propRow : getSufficientRelationships()) {
//         rel = factory.getOWLObjectProperty(getURI(propRow.get(0)));
//         // add the property defintion
//         for(int i = 1; i + 1 < propRow.size(); i++) {
//            class1 = factory.getOWLClass(getURI(propRow.get(i)));
//            class2 = factory.getOWLClass(getURI(propRow.get(i + 1)));
//            OWLDescription d = factory.getOWLObjectSomeRestriction(rel, class2);
//            axiom = factory.getOWLEquivalentClassesAxiom(class1, d);
//            manager.applyChange(new AddAxiom(ontology, axiom));
//         }
//      }
//      // sufficient not-relationship
//      for(List<String> propRow : getSufficientNotRelationships()) {
//         rel = factory.getOWLObjectProperty(getURI(propRow.get(0)));
//         // add the property defintion
//         for(int i = 1; i + 1 < propRow.size(); i++) {
//            class1 = factory.getOWLClass(getURI(propRow.get(i)));
//            class2 = factory.getOWLClass(getURI(propRow.get(i + 1)));
//            OWLDescription d = factory.getOWLObjectSomeRestriction(rel, class2);
//            OWLDescription d2 = factory.getOWLObjectComplementOf(d);
//            axiom = factory.getOWLEquivalentClassesAxiom(class1, d2);
//            manager.applyChange(new AddAxiom(ontology, axiom));
//         }
//      }

      // sufficient blocks ...
      // TODO get disjoint classes
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