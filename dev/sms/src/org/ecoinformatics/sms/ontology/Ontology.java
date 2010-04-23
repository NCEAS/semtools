/**
 *    '$RCSfile: Ontology.java,v $'
 *
 *     '$Author$'
 *       '$Date$'
 *   '$Revision$'
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
 * An annotation ontology is associated with a single uri, and
 * represents the set of classes and properties associated with that
 * uri.
 *
 * @author Shawn Bowers
 */
public class Ontology {

    /* uri of the ontology */
    private String _uri;
    /* optional prefix */
    private String _prefix;

    /**
     */
    public Ontology() {
        // do nothing
    }

    /**
     * Create an ontology reference from a uri
     * @param uri the uri of this ontology 
     */
    public Ontology(String uri) {
        _uri = uri;
    }

    /**
     * Create an ontology reference from a prefix and uri
     * @param prefix the uri of this ontology
     * @param uri the uri of this ontology
     */
    public Ontology(String prefix, String uri) {
        _prefix = prefix;
        _uri = uri;
    }
    
    /**
     * Set the uri of the ontology
     * @param uri
     */
    public void setURI(String uri) {
        _uri = uri;
    }

    /**
     * Returns the uri of the ontology
     * @return the uri of the ontology
     */
    public String getURI() {
        return _uri;
    }

    /**
     * Set the prefix of for the ontology uri
     * @param prefix
     */
    public void setPrefix(String prefix) {
        _prefix = prefix;
    }
    
    /**
     * Get the prefix for the ontology uri
     * @return the prefix string
     */
    public String getPrefix() {
    	if (_prefix == null) {
    		//use the last part of the uri
    		String generatedPrefix = _uri.substring(_uri.lastIndexOf("/")+1);
    		return generatedPrefix;
    	}
        return _prefix;
    }
    
    /**
     * Get a string representation of the ontology
     * @return the ontology uri
     */
    public String toString() {
        return getURI();
    }

    /** 
     * Check if two ontologies are the equal
     * @return true if ontologies have same uri
     */
    public boolean equals(Object obj) {
        if(obj instanceof Ontology) {
            Ontology ont = (Ontology) obj;
            if(ont.getURI().equals(getURI()))
                return true;
        }
        return false;
    }
}
