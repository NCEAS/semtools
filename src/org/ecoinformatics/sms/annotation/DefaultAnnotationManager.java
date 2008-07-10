/**
 *    '$RCSfile: DefaultAnnotationManager.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2007/05/25 16:13:14 $'
 *   '$Revision: 1.2 $'
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

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.AnnotationManager;

import java.util.Hashtable;
import java.util.Vector;
import java.io.Reader;
import java.io.Writer;
import java.io.InputStream;
import java.io.OutputStream;


/**
 */
public class DefaultAnnotationManager implements AnnotationManager {


    /**
     * Default constuctor
     */
    public DefaultAnnotationManager(SMS sms) {
	_sms = sms;
    }

    /**
     * Import an annotation into the manager
     * @param r the semantic annotation 
     * @param id the identifier to assign to the annotation
     */
    public void importAnnotation(InputStream is, String id) throws Exception {
	if(isAnnotation(id)) {
	    String msg = "annotation id '" + id + "' already exists";
	    throw new Exception(msg);
	}
	_annotations.put(id, Annotation.read(is)); 
    }


    /**
     * Export an annotation from the manager
     * @param the identiifer of the annotation
     */
    public void exportAnnotation(String id, OutputStream os) throws Exception {
	if(!isAnnotation(id)) {
	    String msg = "annotation id '" + id + "' does not exist";
	    throw new Exception(msg);
	}
	   getAnnotation(id).write(os);
    }

    /**
     * Update an existing annotation in the manager
     * @param r the new semantic annotation 
     * @param id the identifier of the annotation to update
     */
    public void updateAnnotation(InputStream is, String id) throws Exception {
	if(!isAnnotation(id)) {
	    String msg = "annotation id '" + id + "' does not exist";
	    throw new Exception(msg);
	}
	removeAnnotation(id);
	importAnnotation(is, id);
    }

    /**
     * Remove an annotation from the manager
     * @param id the identifier of the annotation
     */
    public void removeAnnotation(String id) throws Exception {
	if(!isAnnotation(id)) {
	    String msg = "annotation id '" + id + "' does not exist";
	    throw new Exception(msg);
	}
	_annotations.remove(id);
    }

    /**
     * Check if the identifier is assigned to an annotation in the
     * manager
     * @return true if the id is assigned to an annotation in the manager
     * @param id the annotation identifier
     */
    public boolean isAnnotation(String id) {
	return _annotations.containsKey(id);
    }

    /**
     * Get an annotation from the manager
     * @param the identiifer of the annotation
     */
    public Annotation getAnnotation(String id) throws Exception {
	if(!isAnnotation(id)) {
	    String msg = "annotation id '" + id + "' does not exist";
	    throw new Exception(msg);
	}
	return (Annotation)_annotations.get(id);
    }

    /**
     * Get the annotation identifiers from the manager
     * @return the set of annotation identifiers
     */
    public Vector<String> getAnnotationIds() {
	return new Vector(_annotations.keySet());	
    }


    //////////////////////////////////////////////////////////////////////
    // PRIVATE DATA
    
    private SMS _sms;
    private Hashtable _annotations = new Hashtable();

}