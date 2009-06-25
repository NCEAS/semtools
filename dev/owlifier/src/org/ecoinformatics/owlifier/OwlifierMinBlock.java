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
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 *
 * @author sbowers
 */
public class OwlifierMinBlock extends OwlifierBlock {

   public int minCardinality;
   public String relationship;
   public List<String> entities;

   /**
    * Create a min block
    * @param row the row representing this block
    * @throws Exception if row is not well-formed
    */
   public OwlifierMinBlock(OwlifierRow row) throws Exception {
      super(row);
      if(row.getLength() < 5)
         throw new Exception("Illegal min block: " + row);
      relationship = row.getColumn(1).getTrimmedValue();
      try {
         String val = row.getColumn(2).getTrimmedValue();
         minCardinality = (new Integer(val)).intValue();
      } catch(Exception e) {
         throw new Exception("Illegal min block: " + row);
      }
      entities = new ArrayList();
      for(int i = 3; i < row.getLength(); i++)
         entities.add(row.getColumn(i).getTrimmedValue());

   }

   public String getBlockType() {
      return "Min";
   }


    /**
    * Get the relationship being constrained
    * @return the relationship
    */
   public String getRelationship() {
      return relationship;
   }

   /**
    * Get the min cardinality value
    * @return the relationship
    */
   public int getMinCardinality() {
      return minCardinality;
   }

   /**
    * Get the entities being constrained/related
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
      OWLObjectProperty r = f.getOWLObjectProperty(ont.getURI(getRelationship()));
      for(int i = 0; i < entities.size() - 1; i++) {
         OWLClass c1 = f.getOWLClass(ont.getURI(entities.get(i)));
         OWLClass c2 = f.getOWLClass(ont.getURI(entities.get(i+1)));
         OWLDescription d =
               f.getOWLObjectMinCardinalityRestriction(r, getMinCardinality(), c2);
         OWLAxiom a = f.getOWLSubClassAxiom(c1, d);
         m.applyChange(new AddAxiom(o, a));
      }
   }
}
