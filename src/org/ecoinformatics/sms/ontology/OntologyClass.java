/**
 *    '$RCSfile: OntologyClass.java,v $'
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
 * An OntologyClass is a simple representation of an
 * ontology class, encapsulating the name of the class and other basic
 * operations.
 *
 * @author Shawn Bowers
 */

public class OntologyClass {

    /**
     * Default constructor.
     * @param uri the uri of the ontology
     */
    protected OntologyClass(Ontology ontology, String className, 
			    OntologyManager manager) 
    {
	_ontology = ontology;
	_className = className;
	_manager = manager;
    }

    /**
     * Returns the name of the class
     * @return the class name
     */
    public String getName() {
	return _className;
    }

    /**
     * Returns the name of the class
     * @return the class name
     */
    public String getURI() {
	return getOntology().getURI() + getName();
    }


    /**
     * Returns the classes ontology
     * @return the ontology
     */
    public Ontology getOntology() {
	return _ontology;
    }

    /**
     * Returns all subclasses in this ontology of this class
     * @return the subclasses of this class
     */
    public Vector getNamedSubclasses() throws Exception {
	Vector results = new Vector();
	Enumeration classes = _manager.getNamedSubclasses(this).elements();
	while(classes.hasMoreElements()) {
	    OntologyClass c = (OntologyClass)classes.nextElement();
	    if(c.getOntology().equals(this.getOntology()))
		results.add(c);
	}
	return results;
    }

    /**
     * Returns all superclasses in this ontology of this class
     * @return the superclasses of this class
     */
    public Vector getNamedSuperclasses() throws Exception {
	Vector results = new Vector();
	Enumeration classes = _manager.getNamedSuperclasses(this).elements();
	while(classes.hasMoreElements()) {
	    OntologyClass c = (OntologyClass)classes.nextElement();
	    if(c.getOntology().equals(this.getOntology()))
		results.add(c);
	}
	return results;
    }

    /**
     * Returns all properties in this ontology of this class
     * @return the properties of this class
     */
    public Vector getNamedProperties() {
	return null;
    }

    /**
     * Checks if two annotation ontology classes have the same name.
     */
    public boolean equals(Object obj) {
	if(!(obj instanceof OntologyClass))
	    return false;
	OntologyClass c = (OntologyClass)obj;
	if(c.getURI().equals(getURI()))
	    return true;
	return false;
    }

    /**
     * Returns the resolved name of this class, which consists of the
     * corresponding ontology uri followed by the class name
     * @return the string representation of this object
     */
    public String toString() {
	return getURI();
    }

    ////////////////////////////////////////////////////////////////////////
    // PRIVATE DATA

    /* the ontology catalog */ 
    private OntologyManager _manager;

    /* the ontology */ 
    private Ontology _ontology;

    /* the name of the class */
    private String _className;

}
