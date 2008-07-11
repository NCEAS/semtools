/**
 *    '$RCSfile: Resource.java,v $'
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

package org.ecoinformatics.sms.resource;

import java.util.Vector;

/**
 * Representation of a resource such as a dataset. Resources have
 * identifiers and types.
 *
 * @author Shawn Bowers
 */

public abstract class Resource {

    /**
     * Default constructor
     */
    public Resource(String uri) {
	_uri = uri;
    }

    /**
     * Returns the identifier for the resource
     * @return the id
     */
    public String getURI() {
	return _uri;
    }

    /**
     * Returns true if the string is an attribute name for this resource
     * @param a the attribute name
     * @return true if valid attribute name
     */
    public abstract boolean isAttribute(String a);

    /**
     * Returns the set of attributes defined for this resource
     * @return the set of attributes
     */
    public Vector getAttributes() {
	return _attributes;
    }

    /**
     * Returns the attribute of this resource with the given name
     * @param a the attribute name
     * @return the resource attribute
     */
    public abstract ResourceAttribute getAttribute(String a);

    /**
     * Returns the type of this resource.
     * @return the type
     */
    public String getType() {
	return _type;
    }

    /**
     * Sets the type of this resource;
     * @param t the type
     */
    protected void setType(String t) {
	_type = t;
    }

    /**
     */
    public String toString() {
	return getURI();
    }


    ////////////////////////////////////////////////////////////////////////
    // PRIVATE DATA

    private String _uri;
    private Vector _attributes = new Vector();
    private String _type;

}
