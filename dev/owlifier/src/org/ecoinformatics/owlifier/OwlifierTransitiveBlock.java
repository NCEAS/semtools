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
import java.util.ArrayList;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author sbowers
 */
public class OwlifierTransitiveBlock extends OwlifierBlock {

   private String relationship;
   private List<String> entities;

   /**
    * Create a transitive block
    * @param row the row representing this block
    * @throws Exception if row is not well-formed
    */
   public OwlifierTransitiveBlock(OwlifierRow row) throws Exception {
      super(row);
      if(row.getLength() < 2)
         throw new Exception("Illegal relationship block: " + row);
      relationship = row.getColumn(1).getTrimmedValue();
      entities = new ArrayList();
      for(int i = 2; i < row.getLength(); i++)
         entities.add(row.getColumn(i).getTrimmedValue());
   }

   public String getBlockType() {
      return "Transitive";
   }

   /**
    * Get the relationship name
    * @return the relationship name
    */
   public String getRelationship() {
      return relationship;
   }

   /**
    * Get the list of entities
    * @return the entity list
    */
   public List<String> getEntities() {
      return entities;
   }

   @Override
   public void addToOntology(OwlifierOntology ont) throws Exception {
      OWLOntologyManager m = ont.getOWLOntologyManager();
      OWLOntology o = ont.getOWLOntology();
      OWLDataFactory f = m.getOWLDataFactory();
      OWLObjectProperty r = f.getOWLObjectProperty(IRI.create(ont.getURI(getRelationship())));
      OWLAxiom a = f.getOWLTransitiveObjectPropertyAxiom(r);
      m.applyChange(new AddAxiom(o, a));
      // add the domain and range constraints
      for(int i = 0; i < getEntities().size() - 1; i++) {
         OWLClass c1 = f.getOWLClass(IRI.create(ont.getURI(getEntities().get(i))));
         OWLClass c2 = f.getOWLClass(IRI.create(ont.getURI(getEntities().get(i + 1))));
         OWLClassExpression d = f.getOWLObjectSomeValuesFrom(r, c2);
         a = f.getOWLSubClassOfAxiom(c1, d);
         m.applyChange(new AddAxiom(o, a));
      }
   }
}
