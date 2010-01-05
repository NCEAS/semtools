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

/**
 * An OntologyClass is a simple representation of an
 * ontology class, encapsulating the name of the class and other basic
 * operations.
 *
 * @author Shawn Bowers
 */
public class OntologyClass {

    /* the ontology */
    private Ontology _ontology;
    /* the name of the class */
    private String _className;

    /**
     * 
     */
    public OntologyClass() {
    }

    /**
     * Default constructor.
     * @param uri the uri of the ontology
     */
    public OntologyClass(Ontology ontology, String className) {
        _ontology = ontology;
        _className = className;
    }
    
    public OntologyClass(String uri) throws Exception {
    	if (uri.indexOf("#") > -1) {
	        _ontology = new Ontology(uri.substring(0, uri.indexOf("#")));
	        _className = uri.substring(uri.indexOf("#") + 1);
    	} else {
    		throw new Exception("URI does not refer to a valid class");
    	}
    }

    /**
     * Set the local name of the class
     * @param className the name of the class
     */
    public void setName(String className) {
        _className = className;
    }
    
    /**
     * Return the name of the class
     * @return the class name
     */
    public String getName() {
        return _className;
    }

    /**
     * Return the name of the class
     * @return the class name
     */
    public String getURI() {
        return getOntology().getURI() + "#" + getName();
    }
    
    /**
     * Set the name of the class
     * @return the class name
     * @throws Exception 
     */
    public void setURI(String uri) throws Exception {
    	if (uri.indexOf("#") > -1) {
    		_ontology = new Ontology(uri.substring(0, uri.indexOf("#")));
    		_className = uri.substring(uri.indexOf("#") + 1);
    	} else {
    		throw new Exception("URI does not refer to a valid class");
    	}
    }

    /**
     * Set the ontology of the class
     * @param ontology the ontology
     */
    public void setOntology(Ontology ontology) {
        _ontology = ontology;
    }
    
    /**
     * Return the ontology of the class
     * @return the ontology
     */
    public Ontology getOntology() {
        return _ontology;
    }
    
    /**
     * Checks if two annotation ontology classes have the same name.
     */
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof OntologyClass))
            return false;
        OntologyClass c = (OntologyClass) obj;
        if(c.getURI().equals(getURI()))
            return true;
        return false;
    }

    /**
     * Returns the resolved name of this class, which consists of the
     * corresponding ontology uri followed by the class name
     * @return the string representation of this object
     */
    @Override
    public String toString() {
        //return getURI();
        return getName();
    }
}
