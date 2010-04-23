/**
 *    '$Id$'
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
 *
 * @author Shawn Bowers
 */
public abstract class OntologyProperty {

    /* the property name */
    private String _propName;
    /* the ontology */
    private Ontology _ontology;

    /**
     * Default constructor
     */
    protected OntologyProperty(Ontology ontology, String propName) {
        _ontology = ontology;
        _propName = propName;
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
        return getOntology().getURI() + "#" + getName();
    }

    /**
     * Returns the ontology that this property is defined in
     * @return the ontology
     */
    public Ontology getOntology() {
        return _ontology;
    }

    /**
     * Returns a string representation of the property
     * @return the string
     */
    @Override
    public String toString() {
        return getURI();
    }
}
