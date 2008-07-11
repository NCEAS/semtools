/**
 *    '$RCSfile: Ontology.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2008-06-02 19:51:10 $'
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

package org.ecoinformatics.sms.annotation;


/**
 * Objects of this class represent ontologies used in semantic annotations
 */
public class Ontology {

    /**
     * Default constructor
     */
    public Ontology() {
    }

    /**
     * @param namespace the namespace name
     * @param uri the ontology uri
     */
    public Ontology(String namespace, String uri) {
	_namespace = namespace;
	_uri = uri;
    }

    /** 
     * Set the uri (identifier) of this ontology
     * @param uri the uri 
     */
    public void setURI(String uri) {
	_uri = uri;
    }

    /** 
     * Get the uri (identifier) of this ontology
     * @return the uri
     */
    public String getURI() {
	return _uri;
    }

    /**
     * Set the namespace used to denote this ontology in the annotation
     * @param namespace the namespace name
     */
    public void setNamespace(String namespace) {
        _namespace = namespace;
    }

    /**
     * Get the namespace name used to denote this ontology in the
     * annotation
     * @return the namespace name
     */
    public String getNamespace() {
        return _namespace;
    }

  
    public boolean equals(Object obj) {
	if(!(obj instanceof Ontology))
	    return false;
	Ontology ont = (Ontology)obj;
	String uri = ont.getURI();
	if(uri != null && !uri.equals(getURI()))
	    return false;
	return true;
    }

  
    private String _uri;
    private String _namespace;

} 