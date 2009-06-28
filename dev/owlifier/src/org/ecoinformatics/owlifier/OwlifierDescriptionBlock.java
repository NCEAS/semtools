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
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 *
 * @author sbowers
 */
public class OwlifierDescriptionBlock extends OwlifierBlock {

   private String item;
   private String description;

   /**
    * Create a max block
    * @param row the row representing this block
    * @throws Exception if row is not well-formed
    */
   public OwlifierDescriptionBlock(OwlifierRow row) throws Exception {
      super(row);
      if(row.getLength() != 3)
         throw new Exception("Illegal description block: " + row);
      item = row.getColumn(1).getTrimmedValue();
      description = row.getColumn(2).getTrimmedValue();
   }

   public String getBlockType() {
      return "Description";
   }

   public String getDescription() {
      return description;
   }

   public String getItem() {
      return item;
   }

   @Override
   public void addToOntology(OwlifierOntology ont) throws Exception {
      OWLOntologyManager m = ont.getOWLOntologyManager();
      OWLOntology o = ont.getOWLOntology();
      OWLDataFactory f = m.getOWLDataFactory();
      OWLAnnotation n = f.getCommentAnnotation(getDescription());
      if(ont.isClass(getItem())) {
         OWLClass c = f.getOWLClass(ont.getURI(getItem()));
         OWLAxiom a = f.getOWLEntityAnnotationAxiom(c, n);
         m.applyChange(new AddAxiom(o, a));
      }
      if(ont.isProperty(getItem())) {
         OWLObjectProperty p = f.getOWLObjectProperty(ont.getURI(getItem()));
         OWLAxiom a = f.getOWLEntityAnnotationAxiom(p, n);
         m.applyChange(new AddAxiom(o, a));
      }
   }
}
