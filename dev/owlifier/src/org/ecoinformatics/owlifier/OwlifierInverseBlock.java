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

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLObjectProperty;

/**
 *
 * @author sbowers
 */
public class OwlifierInverseBlock extends OwlifierBlock {

   private String relationship1;
   private String relationship2;

   /**
    * Create a inverse block
    * @param row the row representing this block
    * @throws Exception if row is not well-formed
    */
   public OwlifierInverseBlock(OwlifierRow row) throws Exception {
      super(row);
      if(row.getLength() != 3)
         throw new Exception("Illegal inverse block: " + row);
      relationship1 = row.getColumn(1).getTrimmedValue();
      relationship2 = row.getColumn(2).getTrimmedValue();
   }

   public String getRelationship1() {
      return relationship1;
   }

   public String getRelationship2() {
      return relationship2;
   }

   public String getBlockType() {
      return "Inverse";
   }

   @Override
   public void addToOntology(OwlifierOntology ont) throws Exception {
      OWLOntologyManager m = ont.getOWLOntologyManager();
      OWLOntology o = ont.getOWLOntology();
      OWLDataFactory f = m.getOWLDataFactory();
      // create the properties and add them
      OWLObjectProperty r1 = f.getOWLObjectProperty(ont.getURI(getRelationship1()));
      OWLObjectProperty r2 = f.getOWLObjectProperty(ont.getURI(getRelationship2()));
      OWLAxiom a = f.getOWLDeclarationAxiom(r1);
      m.applyChange(new AddAxiom(o, a));
      a = f.getOWLDeclarationAxiom(r2);
      m.applyChange(new AddAxiom(o, a));
      // create the inverse axiom
      a = f.getOWLInverseObjectPropertiesAxiom(r1, r2);
      m.applyChange(new AddAxiom(o, a));
   }
}
