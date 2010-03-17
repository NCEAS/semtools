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
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author sbowers
 */
public class OwlifierImportBlock extends OwlifierBlock {

   /** the import namespace */
   private String namespace;
   /** the uri */
   private String uriString;

   /**
    * Create an import block
    * @param row the row representing this block
    * @throws Exception if row is not well-formed
    */
   public OwlifierImportBlock(OwlifierRow row) throws Exception {
      super(row);
      // TODO
   }

   public String getBlockType() {
      return "Import";
   }

   @Override
   public void addToOntology(OwlifierOntology ont) throws Exception { 
      OWLOntologyManager m = ont.getOWLOntologyManager();
      OWLOntology o = ont.getOWLOntology();
      URI uri = new URI(getURIString());
      OWLDataFactory f = m.getOWLDataFactory();
      OWLImportsDeclaration importAxiom = f.getOWLImportsDeclaration(IRI.create(uri));
      m.applyChange(new AddImport(o, importAxiom));
   }

   /**
    * Set the namespace name
    * @param namespace the namespace name
    */
   public void setNamespace(String namespace) {
      this.namespace = namespace;
   }

   /**
    * Get the namespace name
    * @return the namespace name
    */
   public String getNamespace() {
      return namespace;
   }

   /**
    * Set the namespace uri
    * @param uriString the namespace uri
    */
   public void setURIString(String uriString) {
      this.uriString = uriString;
   }

   /**
    * Get the namespace uri
    * @return the namespace uri
    */
   public String getURIString() {
      return uriString;
   }

}
