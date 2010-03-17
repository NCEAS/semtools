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

import java.util.Set;
import java.util.HashSet;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author sbowers
 */
public class OwlifierSynonymBlock extends OwlifierBlock {

   /** the set of synonymous entities */
   private Set<String> synonyms;

   /**
    * Create a synonym block
    * @param row the row representing this block
    * @throws Exception if row is not well-formed
    */
   public OwlifierSynonymBlock(OwlifierRow row) throws Exception {
      super(row);
      if(row.getLength() < 3)
         throw new Exception("Illegal synonym block: " + row);
      synonyms = new HashSet();
      for(int i = 1; i < row.getLength(); i++)
         synonyms.add(row.getColumn(i).getTrimmedValue());
   }

   public String getBlockType() {
      return "Synonym";
   }

   @Override
   public void addToOntology(OwlifierOntology ont) throws Exception {
      OWLOntologyManager m = ont.getOWLOntologyManager();
      OWLOntology o = ont.getOWLOntology();
      OWLDataFactory f = m.getOWLDataFactory();
      // add each entity as a class declaration
      Set<OWLClass> classes = new HashSet();
      for(String entity : getSynonyms()) {
         OWLClass c = f.getOWLClass(IRI.create(ont.getURI(entity)));
         OWLAxiom a = f.getOWLDeclarationAxiom(c);
         m.applyChange(new AddAxiom(o, a));
         classes.add(c);
      }
      OWLAxiom a = f.getOWLEquivalentClassesAxiom(classes);
      m.applyChange(new AddAxiom(o, a));
   }

   /**
    * Get the set of synonomous entities in the block
    * @return the entities
    */
   public Set<String> getSynonyms() {
      return synonyms;
   }
}
