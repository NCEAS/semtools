/**
 *    '$RCSfile: Ontology.java,v $'
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
import java.util.Vector;
import java.util.Enumeration;

/**
 * An annotation ontology is associated with a single uri, and
 * represents the set of classes and properties associated with that
 * uri.
 *
 * @author Shawn Bowers
 */

public class Ontology {

    /**
     * Default constructor. The constructor is called from the
     * ontology catalog.
     * @param uri the uri of this ontology 
     */
    protected Ontology(String uri, OntologyManager manager) {
	_uri = uri;
	_manager = manager;
    }

    /**
     * Returns the uri of the ontology
     * @return the uri of the ontology
     */
    public String getURI() {
	return _uri;
    }

    /**
     * Returns the named classes defined in this ontology
     * TODO: cache the named properties
     * @return the named classes
     */
    public Vector getNamedClasses() { 
	Vector results = new Vector();
	Enumeration classes = _manager.getNamedClasses().elements();
	while(classes.hasMoreElements()) {
	    OntologyClass c = (OntologyClass)classes.nextElement();
	    if(c.getOntology().equals(this))
		results.add(c);
	}
	return results;
    }

    /**
     * Returns the named properties defined in the ontology
     * TODO: cache the named properties
     * @return the named properties
     */
    public Vector getNamedProperties() {
	Vector results = new Vector();
	Enumeration properties = _manager.getNamedProperties().elements();
	while(properties.hasMoreElements()) {
	    OntologyProperty p = (OntologyProperty)properties.nextElement();
	    if(p.getOntology().equals(this))
		results.add(p);
	}
	return results;
    }

    /**
     * Returns true if the class is in the ontology
     * @return true if a class
     */
    public boolean isNamedClass(String name) {
	Enumeration classes = getNamedClasses().elements();
	while(classes.hasMoreElements()) {
	    OntologyClass c = (OntologyClass)classes.nextElement();
	    if(c.getName().equals(name))
		return true;
	}	    
	return false;
    }

    /**
     * Returns the class with the given name.
     * @return the ontology class
     */
    public OntologyClass getNamedClass(String name) throws Exception {
	Enumeration classes = getNamedClasses().elements();
	while(classes.hasMoreElements()) {
	    OntologyClass c = (OntologyClass)classes.nextElement();
	    if(c.getName().equals(name))
		return c;
	}	    
	String msg = "class '" + name + "' is not defined in this ontology" + 
	    " (" + getURI() + ")";
	throw new Exception(msg);
    }

    /**
     * Returns true if the property is in the ontology
     * @return true if a property
     */
    public boolean isNamedProperty(String name) {
	Enumeration properties = getNamedProperties().elements();
	while(properties.hasMoreElements()) {
	    OntologyProperty p = (OntologyProperty)properties.nextElement();
	    if(p.getName().equals(name))
		return true;
	}	    
	return false;
    }

    /**
     * Returns the property with the given name.
     * @return the ontology property
     */
    public OntologyProperty getNamedProperty(String name) throws Exception {
	Enumeration properties = getNamedProperties().elements();
	while(properties.hasMoreElements()) {
	    OntologyProperty p = (OntologyProperty)properties.nextElement();
	    if(p.getName().equals(name))
		return p;
	}	    
	String msg = "property '" + name + "' is not defined in this ontology" +
	    " (" + getURI() + ")";
	throw new Exception(msg);
    }

    /** 
     * Returns a string representation of the ontology
     * @return the string
     */
    public String toString() {
	return getURI();
    }

    /** 
     * Returns true if the given ontology has the same uri
     */
    public boolean equals(Object obj) {
	if(obj instanceof Ontology) {
	    Ontology ont = (Ontology)obj;
	    if(ont.getURI().equals(getURI()))
	       return true;
	}
	return false;
    }


    ////////////////////////////////////////////////////////////////////////
    // PRIVATE DATA

    /** the uri of the ontology */
    private String _uri;

    private OntologyManager _manager; 

}
