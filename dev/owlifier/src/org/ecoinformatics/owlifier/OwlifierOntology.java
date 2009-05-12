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
import java.util.List;
import java.util.ArrayList;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 * TODO Add constructor with type (oboe/standard), then add back the
 * classify operation? 
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
	throws Exception
    {
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
	    buildOntology();
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
    public void clasify() {
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
	// save the ontology
	manager.saveOntology(ontology);
    }


    /**
     */
    private void buildOntology() throws Exception {
	OWLDataFactory factory = manager.getOWLDataFactory();
	OWLAxiom axiom = null;
	// get imports
	for(URI importURI : getImports()) {
	    axiom = factory.getOWLImportsDeclarationAxiom(ontology, importURI); 	    manager.applyChange(new AddAxiom(ontology, axiom));
	}
	// get subclasses
    }

    /**
     */
    private void buildOboeOntology() throws Exception {
    }

    /**
     * Get the list of imports
     */
    private List<URI> getImports() throws Exception {
	List<URI> result = new ArrayList();
	if(sheet == null)
	    return result;
	for(OwlifierRow r : sheet.getRows()) {
	    if(r.getBlockType() == OwlifierBlockType.IMPORT) {
		if(r.getLength() < 2) 
		    throw new Exception("ERROR: invalid IMPORT statement");
		URI uri = new URI(r.getColumn(1).getValue());
		if(!result.contains(uri))
		    result.add(uri);
	    }
	}
	return result;
    }

}