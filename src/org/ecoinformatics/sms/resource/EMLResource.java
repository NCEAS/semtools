/**
 *    '$RCSfile: EMLResource.java,v $'
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


/**
 * Representation of an EML dataset.
 *
 * @author Shawn Bowers
 */

public class EMLResource extends Resource {

    /**
     * Default constructor
     */
    public EMLResource(String uri) {
	super(uri);
	setType("EML");
    }


    /**
     * Returns true if the string is an attribute name for this resource
     * @param a the attribute name
     * @return true if valid attribute name
     */
    public boolean isAttribute(String a) {
	return true;
    }

    /**
     * Returns the attribute of this resource with the given name
     * @param a the attribute name
     * @return the resource attribute
     */
    public ResourceAttribute getAttribute(String a) {
	return new ResourceAttribute(this, a);
    }


    ////////////////////////////////////////////////////////////////////////
    // PRIVATE DATA

}
