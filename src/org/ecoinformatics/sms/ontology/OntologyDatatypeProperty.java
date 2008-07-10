/**
 *    '$RCSfile: OntologyDatatypeProperty.java,v $'
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


/**
 *
 * @author Shawn Bowers
 */

public class OntologyDatatypeProperty extends OntologyProperty {

    /**
     * Default constructor
     */
    protected OntologyDatatypeProperty(Ontology ontology, String propName, 
				       OntologyManager manager) 
    {
	super(ontology, propName, manager);
    }

    /**
     */
    public String getDatatype() {
	return null;
    }

    /**
     * Returns true if the given object property has the same name as
     * this object property
     * @return true if the given object has the same name
     */
    public boolean equals(Object obj) {
	if(obj instanceof OntologyDatatypeProperty) {
	    if(getName().equals(((OntologyDatatypeProperty)obj).getName()))
	       return true;
	}
	return false;
    }

}
