/**
 *    '$RCSfile: OntologyProperty.java,v $'
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
import java.util.Enumeration;
import java.util.Vector;


/**
 *
 * @author Shawn Bowers
 */

public abstract class OntologyProperty {

    /**
     * Default constructor
     */
    protected OntologyProperty(Ontology ontology, String propName, 
			       OntologyManager manager) 
    {
	_ontology = ontology;
	_propName = propName;
	_manager = manager;
    }

    /**
     * Returns the name of this property
     * @return the name
     */
    public String getName() {
	return _propName;
    }

    /**
     * Returns the name of this property
     * @return the name
     */
    public String getURI() {
	return getOntology().getURI() + getName();
    }

    /**
     * Returns the ontology that this property is defined in
     * @return the ontology
     */
    public Ontology getOntology() {
	return _ontology;
    }

    /**
     * Returns all subproperties in this ontology of this property
     * @return the subproperty of this class
     */
    public Vector getNamedSubproperties() throws Exception {
	Vector results = new Vector();
	Enumeration props = _manager.getNamedSubproperties(this).elements();
	while(props.hasMoreElements()) {
	    OntologyProperty p = (OntologyProperty)props.nextElement();
	    if(p.getOntology().equals(getOntology()))
	       results.add(p);
	}
	return results;
    }

    /**
     * Returns all superproperties in this ontology of this property
     * @return the superproperty of this class
     */
    public Vector getNamedSuperproperties() throws Exception {
	Vector results = new Vector();
	Enumeration props = _manager.getNamedSuperproperties(this).elements();
	while(props.hasMoreElements()) {
	    OntologyProperty p = (OntologyProperty)props.nextElement();
	    if(p.getOntology().equals(getOntology()))
	       results.add(p);
	}
	return results;
    }


    /**
     * Returns the set of classes defining the domain of this property.
     */
    public Vector getDomainClasses() {
	return new Vector();
    }

    /**
     * Returns a string representation of the property
     * @return the string
     */
    public String toString() {
	return getURI();
    }

    ////////////////////////////////////////////////////////////////////////
    // PRIVATE DATA

    /* the property name */
    private String _propName;

    /* the ontology */
    private Ontology _ontology;

    /* the ontology manager */
    protected OntologyManager _manager;

}
