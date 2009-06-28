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

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 *
 * @author sbowers
 */
public class OwlifierSufficientBlock extends OwlifierBlock {

   private boolean isEntityBlock;
   private boolean isNotRelationshipBlock;
   private String entity;
   private List<String> entities = new ArrayList();
   private String property;

   /**
    * Create a sufficient block
    * @param row the row representing this block
    * @throws Exception if row is not well-formed
    */
   public OwlifierSufficientBlock(OwlifierRow row) throws Exception {
      super(row);
      if(row.getLength() < 4)
         throw new Exception("Illegal sufficient block: " + row);
      // determine the type of block
      String type = row.getColumn(1).getTrimmedValue().toLowerCase();
      if(type.equals("entity")) {
         isEntityBlock = true;
         entity = row.getColumn(2).getTrimmedValue();
         for(int i = 3; i < row.getLength(); i++)
            entities.add(row.getColumn(i).getTrimmedValue());
      } else if(type.equals("relationship"))
         // check if a "not" block
         if(row.getColumn(2).getTrimmedValue().toLowerCase().equals("not")) {
            isNotRelationshipBlock = true;
            if(row.getLength() != 6)
               throw new Exception("Illegal sufficient relationship block: " + row);
            property = row.getColumn(3).getTrimmedValue();
            entity = row.getColumn(4).getTrimmedValue();
            entities.add(row.getColumn(5).getTrimmedValue());
         } else {
            if(row.getLength() != 5)
               throw new Exception("Illegal sufficient relationship block: " + row);
            property = row.getColumn(2).getTrimmedValue();
            entity = row.getColumn(3).getTrimmedValue();
            entities.add(row.getColumn(4).getTrimmedValue());
         }
      else
         throw new Exception("Illeagal sufficient block: " + row);
   }

   public String getBlockType() {
      return "Sufficient";
   }

   /**
    * Check if this is a sufficient relationship block
    * @return true if this is a sufficient relatioship block
    */
   public boolean hasProperty() {
      return !isEntityBlock;
   }

   /**
    * Get the property (relationship) of the block, if this is a sufficient
    * relationship block
    * @return the property
    */
   public String getProperty() {
      return property;
   }

   /**
    * Get the entity being defined
    * @return the entity
    */
   public String getEntity() {
      return entity;
   }

   /**
    * Get the other entities of the block. If this is a sufficient entity block,
    * then those entities defining this block. If this si a sufficient
    * relationship block, then the entity filling the range of the property
    * @return the entities
    */
   public List<String> getEntities() {
      return entities;
   }

   @Override
   public void addToOntology(OwlifierOntology ont) throws Exception {
      OWLOntologyManager m = ont.getOWLOntologyManager();
      OWLOntology o = ont.getOWLOntology();
      OWLDataFactory f = m.getOWLDataFactory();
      // get the entity class
      OWLClass c = f.getOWLClass(ont.getURI(getEntity()));
      if(isEntityBlock)
         if(getEntities().size() > 1) {
            Set<OWLClass> classes = new HashSet();
            for(String x : getEntities()) {
               OWLClass d = f.getOWLClass(ont.getURI(x));
               classes.add(d);
            }
            OWLObjectIntersectionOf i = f.getOWLObjectIntersectionOf(classes);
            OWLAxiom a = f.getOWLEquivalentClassesAxiom(c, i);
            m.applyChange(new AddAxiom(o, a));
         } else {
            OWLClass d = f.getOWLClass(ont.getURI(getEntities().get(0)));
            OWLAxiom a = f.getOWLEquivalentClassesAxiom(c, d);
            m.applyChange(new AddAxiom(o, a));
         }
      else {
         OWLObjectProperty p = f.getOWLObjectProperty(ont.getURI(getProperty()));
         OWLClass r = f.getOWLClass(ont.getURI(getEntities().get(0)));
         OWLDescription d = f.getOWLObjectSomeRestriction(p, r);
         if(isNotRelationshipBlock)
            d = f.getOWLObjectComplementOf(d);
         OWLAxiom a = f.getOWLEquivalentClassesAxiom(c, d);
         m.applyChange(new AddAxiom(o, a));
      }
   // TODO: add transitive axioms
   }
}
