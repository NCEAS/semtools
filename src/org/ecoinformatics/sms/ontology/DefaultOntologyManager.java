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
import java.io.Reader;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.mindswap.pellet.jena.OWLReasoner;
import org.mindswap.pellet.jena.ModelReader;
import org.mindswap.pellet.jena.JenaUtils;
import org.mindswap.pellet.utils.ATermUtils;
import aterm.ATermAppl;


/**
 * An ontology catalog maintains a "knowledge base" of OWL
 * ontologies. This implementation uses the Pellet application for
 * OWL-DL reasoning.  An ontology is denoted by a uri, which is also
 * assumed to be the location of the assocated OWL file.
 *
 * @author Shawn Bowers
 */

public class DefaultOntologyManager implements OntologyManager {

    /**
     * Default constructor.
     */
    public DefaultOntologyManager() {
	_reasoner = new OWLReasoner();
    }

    /**
     * Adds the given owl file to the catalog, returning the
     * associated annotation ontology. The OWL file uri is assumed to
     * also be the namespace of the ontology.
     *
     * TODO: Infer the uri of the ontology from the file
     *
     * @param uri the uri and location of the OWL file ontology
     */
    public void importOntology(String uri) throws Exception {
	Ontology ont = new Ontology(uri, this);
	// make sure the ontology isn't already loaded
	if(_ontologies.contains(ont))
	    return;
	// create reader and jena model for the uri
	ModelReader modelReader = new ModelReader();
	Model model = modelReader.read(uri);
	// load the model into the reasoner
	_reasoner.load(model);
	// add the ontology
	_ontologies.add(ont);
    }

    /**
     * Removes the ontology associated with the uri from the manager
     * @param uri the uri
     */
    public void removeOntology(String uri) throws Exception {
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
	Enumeration e = _ontologies.elements();
	while(e.hasMoreElements()) {
	    Ontology o = (Ontology)e.nextElement();
	    if(o.getURI().equals(uri))
		return o;
	}
	return null;
    }

    /** 
     * Returns the ontologies being managed by the catalog.
     * @return the ontologies
     */
    public Vector getOntologyIds() { 
	Vector results = new Vector();
	Enumeration ontos = _ontologies.elements();
	while(ontos.hasMoreElements()) {
	    Ontology o = (Ontology)ontos.nextElement();
	    results.add(o.getURI());
	}
	return results;
    }

    /**
     * Returns the named classes for ontologies defined in the
     * catalog. That is, for classes whose namespaces match a uri in
     * the catalog.
     * @returns the classes
     */
    public Vector getNamedClasses() {
	Vector results = new Vector();
	Iterator iter = _reasoner.getClasses().iterator();
	while(iter.hasNext()) {
	    Resource r = (Resource)iter.next();
	    String uri = r.getNameSpace();
	    Ontology o = getOntology(uri);
	    if(o != null)
		results.add(new OntologyClass(o, r.getLocalName(), this));
	}
	return results;
    }

    /**
     * Returns the set of named sub classes of the given ontology class.
     * @return the subclasses
     */
    public Vector getNamedSubclasses(OntologyClass c) {
	Vector results = new Vector();
	Resource res = getClassResource(c);
	// check if this is a valid resource
	if(res == null)
	    return results;
	Iterator xs = _reasoner.getSubClasses(res, true).iterator();
	while(xs.hasNext()) {
	    Iterator ys = ((Collection)xs.next()).iterator();
	    while(ys.hasNext()) {
		Resource r = (Resource)ys.next();
		results.add(getOntologyClass(r));
	    }
	}
	return results;
    }


    /**
     * Returns true if the first class is a subclass of the second
     * @param sub the subclass
     * @param sup the superclass
     * @returns result of subclass check
     */
    public boolean isSubClass(OntologyClass sub, OntologyClass sup) {
	Resource r1 = getClassResource(sub);
	Resource r2 = getClassResource(sup);
	return _reasoner.isSubClassOf(r1, r2);
    }

    /**
     * Returns true if classes are equivalent
     * @param c1 the first class
     * @param c2 the second class
     * @returns result of equivalence check
     */
    public boolean isEquivalentClass(OntologyClass c1, OntologyClass c2) {
	Resource r1 = getClassResource(c1);
	Resource r2 = getClassResource(c2);
	return _reasoner.isEquivalentClass(r1, r2);
    }


    /**
     * Gets the superclasses of the given ontology class whose ontology
     * is managed by this manager
     * @param o the ontology
     * @returns the named classes
     */
    public Vector getNamedSuperclasses(OntologyClass c) throws Exception {
	Vector results = new Vector();
	Resource res = getClassResource(c);
	// check if this is a valid resource
	if(res == null)
	    return results;
	Iterator xs = _reasoner.getSuperClasses(res, true).iterator();
	while(xs.hasNext()) {
	    Iterator ys = ((Collection)xs.next()).iterator();
	    while(ys.hasNext()) {
		Resource r = (Resource)ys.next();
		results.add(getOntologyClass(r));
	    }
	}
	return results;
    }

    /**
     * Returns all named properties defined in the catalog
     * @returns the properties
     */
    public Vector getNamedProperties() {
	Vector results = new Vector();
	Iterator iter = _reasoner.getKB().getObjectProperties().iterator();
	while(iter.hasNext()) {
	    Resource res = _reasoner.toJenaResource((ATermAppl)iter.next());
	    Ontology ont = getOntology(res.getNameSpace());
	    if(ont != null) {
		OntologyProperty p = 
		    new OntologyObjectProperty(ont, res.getLocalName(), this);
		results.add(p);
	    }
	}
	iter = _reasoner.getKB().getDataProperties().iterator();
	while(iter.hasNext()) {
	    Resource res = _reasoner.toJenaResource((ATermAppl)iter.next());
	    Ontology ont = getOntology(res.getNameSpace());
	    if(ont != null) {
		OntologyProperty p = 
		    new OntologyDatatypeProperty(ont, res.getLocalName(), this);
		results.add(p);
	    }
	}
	return results;
    }

    /**
     * Gets the named subproperties of the given property whose
     * ontology is managed by this manager
     * @returns the named properties
     */
    public Vector getNamedSubproperties(OntologyProperty p) 
	throws Exception 
    {
	return null;
    }

    /**
     * Gets the named superproperties of the given property whose
     * ontology is managed by this manager
     * @returns the named properties
     */
    public Vector getNamedSuperproperties(OntologyProperty p) 
	throws Exception
    {
	return null;
    }

    /**
     * Returns true if the given class is the domain of the property.
     * @param p the property
     * @param c the class
     */
    public boolean hasDomain(OntologyProperty p, OntologyClass c) {
	Resource rp = getPropertyResource(p);
	Resource rc = getClassResource(c);
	return _reasoner.hasDomain(rp, rc);
    }


    /**
     * Returns true if the given class is a range of the property.
     * @param p the property
     * @param c the class
     */
    public boolean hasRange(OntologyProperty p, OntologyClass c) {
	Resource rp = getPropertyResource(p);
	Resource rc = getClassResource(c);
	return _reasoner.hasRange(rp, rc);
    }

    /**
     * Returns the domain classes of the property.
     * @param p the property
     * @return the domain classes
     */
    public Vector getDomain(OntologyProperty p) {
	Vector result = new Vector();
	ATermAppl a = ATermUtils.makeTermAppl(p.toString());
	Iterator iter = _reasoner.getKB().getDomains(a).iterator();
	while(iter.hasNext()) {
	    ATermAppl r = (ATermAppl)iter.next();
	    if(_reasoner.getKB().isClass(r)) {
		Resource res = _reasoner.toJenaResource(r);
		Ontology ont = getOntology(res.getNameSpace());
		if(ont != null) {
		    OntologyClass c = 
			new OntologyClass(ont, res.getLocalName(), this);
		    result.add(c);
		}
	    }
	}
	return result;
    }

    /**
     * Returns the range classes of the property.
     * @param p the property
     * @return the domain classes
     */
    public Vector getRange(OntologyProperty p) {
	Vector result = new Vector();
	ATermAppl a = ATermUtils.makeTermAppl(p.toString());
	Iterator iter = _reasoner.getKB().getRanges(a).iterator();
	while(iter.hasNext()) {
	    ATermAppl r = (ATermAppl)iter.next();
	    if(_reasoner.getKB().isClass(r)) {
		Resource res = _reasoner.toJenaResource(r);
		Ontology ont = getOntology(res.getNameSpace());
		if(ont != null) {
		    OntologyClass c = 
			new OntologyClass(ont, res.getLocalName(), this);
		    result.add(c);
		}
	    }
	}
	return result;
    }


    /**
     * Classify all ontologies in the catalog.
     */
    public void classify() {
	_reasoner.classify();
    }


    ////////////////////////////////////////////////////////////////////////
    // PROTECTED METHODS

    /**
     * Creates a new ontology having the given uri.
     * @param uri the uri
     * @return the ontology
     */
    protected Ontology createOntology(String uri) {
	Ontology ont = new Ontology(uri, this);
	_ontologies.add(ont);
	return ont;
    }


    /**
     * Returns the resource associated with the given property
     * @param prop the property
     * @return the resource
     */
    protected Resource getPropertyResource(OntologyProperty prop) {
  	Iterator iter = _reasoner.getKB().getObjectProperties().iterator();
  	while(iter.hasNext()) {
  	    Resource res = _reasoner.toJenaResource((ATermAppl)iter.next());
  	    Ontology ont = new Ontology(res.getNameSpace(), this);
  	    if(prop.getOntology().equals(ont) && 
	       res.getLocalName().equals(prop.getName()))
  		return res;
  	}
  	iter = _reasoner.getKB().getDataProperties().iterator();
  	while(iter.hasNext()) {
  	    Resource res = _reasoner.toJenaResource((ATermAppl)iter.next());
  	    Ontology ont = new Ontology(res.getNameSpace(), this);
  	    if(prop.getOntology().equals(ont) && 
	       res.getLocalName().equals(prop.getName()))
  		return res;
  	}
  	return null;
    }

    /**
     * Generates an OntologyClass from a class resource. If the
     * resource isn't defined as a class, returns null
     * @param res the resource
     * @return the ontology class
     */
    protected OntologyClass getOntologyClass(Resource res) {
	ATermAppl t = _reasoner.node2term(res);
	if(_reasoner.getKB().isClass(t)) {
	    Ontology o = new Ontology(res.getNameSpace(), this);
	    return new OntologyClass(o, res.getLocalName(), this);
	}
	return null;
    }

    /**
     * Returns the set of resources defined as the range of the given property
     * @param prop the property
     * @return the range resources
     */
    protected Vector getPropertyRangeResources(OntologyProperty prop) {
	Vector results = new Vector();
 	Resource res = getPropertyResource(prop);
	if(res == null)
	    return results;
	ATermAppl t = _reasoner.node2term(res);
	Iterator iter = _reasoner.getKB().getRanges(t).iterator();
	while(iter.hasNext()) {
	    Resource r = _reasoner.toJenaResource((ATermAppl)iter.next());
	    results.add(r);
	}
	return results;
    }

    /**
     * Returns the set of resources defined as the range of the given property
     * @param prop the property
     * @return the range resources
     */
    protected Vector getPropertyRangeClasses(OntologyProperty prop) {
	Vector results = new Vector();
	Enumeration xs = getPropertyRangeResources(prop).elements();
	while(xs.hasMoreElements()) {
	    Resource res = (Resource)xs.nextElement();
	    ATermAppl t = _reasoner.node2term(res);
	    if(_reasoner.getKB().isClass(t))
		results.add(getOntologyClass(res));
	}
	return results;
    }

    /**
     * Returns the set of resources defined as the domain of the given property
     * @param prop the property
     * @return the domain resources
     */
    protected Vector getPropertyDomainResources(OntologyProperty prop) {
	Vector results = new Vector();
 	Resource res = getPropertyResource(prop);
	if(res == null)
	    return results;
	ATermAppl t = _reasoner.node2term(res);
	Iterator iter = _reasoner.getKB().getDomains(t).iterator();
	while(iter.hasNext()) {
	    Resource r = _reasoner.toJenaResource((ATermAppl)iter.next());
	    results.add(r);
	}
	return results;
    }
    

    /**
     * Returns the resource associated with the given class
     * @param c the class
     * @return the resource
     */
    protected Resource getClassResource(OntologyClass c) {
	Iterator iter = _reasoner.getClasses().iterator();
	while(iter.hasNext()) {
	    Resource res = (Resource)iter.next();
	    Ontology ont = new Ontology(res.getNameSpace(), this);
	    if(c.getOntology().equals(ont) && 
	       c.getName().equals(res.getLocalName()))
		return res;
	}
	return null;
    }


    ////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS


    ////////////////////////////////////////////////////////////////////////
    // PRIVATE DATA

    /* the reasoner */
    private OWLReasoner _reasoner;

    /* the ontology store */ 
    private Vector _ontologies = new Vector();


}
