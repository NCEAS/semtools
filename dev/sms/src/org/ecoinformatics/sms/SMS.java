/**
 *    '$RCSfile: SMS.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2007/05/25 16:13:14 $'
 *   '$Revision: 1.7 $'
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
package org.ecoinformatics.sms;

import org.ecoinformatics.sms.annotation.DefaultAnnotationManager;
import org.ecoinformatics.sms.annotation.persistent.manager.DbAnnotationManager;

/**
 * @author Shawn Bowers
 */
public class SMS {

   /* TODO: 
    *   1. Add an OBOE_URI hook. 
    *   2. Revise annotation syntax:
    *      a. use uri's to identify data
    *      b. nest mappings under measurements
    *      c. ??? 
    *   3. Support part-of expansions
    */ 
   
	public static final String DEFAULT_ONTOLOGY_MANAGER_CLASS = "org.ecoinformatics.sms.ontology.DefaultOntologyManager";
	public static final String OWL_ONTOLOGY_MANAGER_CLASS = "org.ecoinformatics.sms.owlapi.OwlApiOntologyManager";

    private AnnotationManager _annotationManager;
    private OntologyManager _ontologyManager;

    private static SMS instance = null;
    
    /**
     * Default constructor
     */
    private SMS(String ontologyManagerClassName) {
    	// determine the implementation to use
    	String className = OWL_ONTOLOGY_MANAGER_CLASS;
    	if (ontologyManagerClassName != null) {
    		className = ontologyManagerClassName;
    	}
    	
    	// instantiate the ontology manager
		try {
			_ontologyManager = (OntologyManager) Class.forName(className).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}

        //_annotationManager = new DefaultAnnotationManager(this);
        _annotationManager = new DbAnnotationManager(this);

    }
    
    public static SMS getInstance() {
    	if (instance == null) {
    		instance = new SMS(null);
    	}
    	return instance;
    }

    /**
     * Set the current annotation manager
     * @param m the new annotation manager
     */
    public void setAnnotationManager(AnnotationManager m) {
        _annotationManager = m;
    }

    /**
     * Gets the current annotation manager
     * @return the annotation manager
     */
    public AnnotationManager getAnnotationManager() {
        return _annotationManager;
    }

    /**
     * Sets the current ontology manager
     * @param m the ontology manager
     */
    public void setOntologyManager(OntologyManager m) {
        _ontologyManager = m;
    }

    /**
     * Gets the ontology manager
     * @return the ontology manager
     */
    public OntologyManager getOntologyManager() {
        return _ontologyManager;
    }
}