/**
 *    '$RCSfile: DefaultOntologyManager.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2007/05/21 20:25:10 $'
 *   '$Revision: 1.1 $'
 *
 *  For Details: http://daks.ucdavis.edu
 *
 * Copyright (c) 2005 The Regents of the University of California.
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
package org.ecoinformatics.sms.ontology;

import org.ecoinformatics.sms.OntologyManager;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.ontology.OntModel;
import org.mindswap.pellet.jena.PelletReasonerFactory;

/**
 * An ontology catalog maintains a "knowledge base" of OWL
 * ontologies. This implementation uses the Pellet application for
 * OWL-DL reasoning.  An ontology is denoted by a uri, which is also
 * assumed to be the location of the assocated OWL file.
 *
 * @author Shawn Bowers
 */
public class DefaultOntologyManager implements OntologyManager {

    // map from ontologies to jena ont models
    private Map<Ontology, OntModel> _models = new HashMap();

    /**
     * Constructor.
     */
    public DefaultOntologyManager() {
    }

    /**
     * Adds an OWL ontology to the manager. The uri is assumed also to be the 
     * namespace of the ontology.
     * @param uri the uri and location of the OWL file ontology
     */
    public void importOntology(String uri) throws Exception {
        // make sure the ontology isn't already loaded
        if(isOntology(uri))
            return;
        Ontology ont = new Ontology(uri);
        // load via pellet and jena
        OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
        model.read(uri);
        // add to the 
        _models.put(ont, model);
    }

    /**
     * Removes the ontology associated with the uri from the manager
     * @param uri the uri
     */
    public void removeOntology(String uri) {
        // TODO
    }

    /**
     * Checks if the uri denotes an ontology in the catalog
     * @return true if the uri is an ontology in the catalog
     */
    public boolean isOntology(String uri) {
        return getOntology(uri) != null;
    }

    /**
     * Returns the ontology associated with the uri
     * @return the ontology
     */
    public Ontology getOntology(String uri) {
        for(Ontology ont : _models.keySet())
            if(ont.getURI().equals(uri))
                return ont;
        return null;
    }

    /** 
     * Returns the ontologies being managed by the catalog.
     * @return the ontologies
     */
    public List<String> getOntologyIds() {
        List<String> results = new ArrayList();
        for(Ontology ont : _models.keySet())
            results.add(ont.getURI());
        return results;
    }

    /**
     * Returns the named classes for ontologies defined in the
     * catalog. That is, for classes whose namespaces match a uri in
     * the catalog.
     * @returns the classes
     */
    public List<OntologyClass> getNamedClasses() {
        List<OntologyClass> results = new ArrayList();
        for(OntModel model : _models.values())
            for(Resource c : (List<Resource>) model.listClasses().toList()) {
                String uri = c.getNameSpace();
                Ontology ont = getOntology(uri);
                OntologyClass oc = new OntologyClass(ont, c.getLocalName());
                if(ont != null && !results.contains(oc))
                    results.add(oc);
            }
        return results;
    }

    /**
     * Returns the set of named sub classes of the given ontology class.
     * @return the subclasses
     */
    public List<OntologyClass> getNamedSubclasses(OntologyClass c) {
        return null;
    }

    /**
     * Returns true if the first class is a subclass of the second
     * @param sub the subclass
     * @param sup the superclass
     * @returns result of subclass check
     */
    public boolean isSubClass(OntologyClass sub, OntologyClass sup) {
        return false;
    }

    /**
     * Returns true if classes are equivalent
     * @param c1 the first class
     * @param c2 the second class
     * @returns result of equivalence check
     */
    public boolean isEquivalentClass(OntologyClass c1, OntologyClass c2) {
        return false;
    }

    /**
     * Gets the superclasses of the given ontology class whose ontology
     * is managed by this manager
     * @param o the ontology
     * @returns the named classes
     */
    public List<OntologyClass> getNamedSuperclasses(OntologyClass c) {
        return null;
    }

    /**
     * Returns all named properties defined in the catalog
     * @returns the properties
     */
    public List<OntologyProperty> getNamedProperties() {
        return null;
    }

    /**
     * Gets the named subproperties of the given property whose
     * ontology is managed by this manager
     * @returns the named properties
     */
    public List<OntologyProperty> getNamedSubproperties(OntologyProperty p) {
        return null;
    }

    /**
     * Gets the named superproperties of the given property whose
     * ontology is managed by this manager
     * @returns the named properties
     */
    public List<OntologyProperty> getNamedSuperproperties(OntologyProperty p) {
        return null;
    }

    /**
     * Returns true if the given class is the domain of the property.
     * @param p the property
     * @param c the class
     */
    public boolean hasDomain(OntologyProperty p, OntologyClass c) {
        return false;
    }

    /**
     * Returns true if the given class is a range of the property.
     * @param p the property
     * @param c the class
     */
    public boolean hasRange(OntologyProperty p, OntologyClass c) {
        return false;
    }

    /**
     * Returns the domain classes of the property.
     * @param p the property
     * @return the domain classes
     */
    public List<OntologyClass> getDomain(OntologyProperty p) {
        return null;
    }

    /**
     * Returns the range classes of the property.
     * @param p the property
     * @return the domain classes
     */
    public List<OntologyClass> getRange(OntologyObjectProperty p) {
        return null;
    }
}
