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


import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.DefaultAnnotationManager;
import org.ecoinformatics.sms.ontology.DefaultOntologyManager;
import org.ecoinformatics.sms.operations.KeywordSearch;
import org.ecoinformatics.sms.operations.KeywordSearchResult;
import org.ecoinformatics.sms.operations.KeywordSearchResultSet;
import org.ecoinformatics.sms.gui.KeywordSearchApp;
import org.ecoinformatics.sms.gui.AnnotationPanel;
//import org.apache.log4j.Logger;
//import org.apache.log4j.Level;
import java.util.Vector;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.Writer;
import javax.swing.JFrame;

/**
 * @author Shawn Bowers
 */
public class SMS {

    /**
     * Default onstructor
     */
    public SMS() {
	_initialize();
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


    /**
     * Perform a keyword search operation over the given list of
     * keyword terms
     * @param query the list of query terms
     * @return a ranked list of keyword search results
     */
    public KeywordSearchResultSet keywordSearch(String[] query) 
	throws Exception 
    {
 	KeywordSearch op = new KeywordSearch(query, this);
 	return op.doSearch();
    }

    /**
     */
    public static void main(String [] args) {

	// turn off logging
	// Logger logger = Logger.getRootLogger(); 
	// logger.setLevel(Level.OFF);
	SMS sms = new SMS();
 	try {
	    if(args.length == 0)
      {
		    sms.loadConfigs();
 	    } else if(args.length == 2) {
 		// keyword search operation
 		if(args[0].equals("-q"))
		    sms.keywordQueryMode(args[1].split("\\s"));
	    }
	} catch(Exception e) {
	    e.printStackTrace();
 	}
    }
    

    ////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS

    private void keywordQueryMode(String[] query) throws Exception {
	loadConfigs();
	System.out.println(">>> Starting keyword search ...");
	System.out.println(keywordSearch(query));
    }

    
    private void loadConfigs() throws Exception {
	System.out.println(">>> Loading annotations ... ");
	SMSProperties props = SMSProperties.getInstance();
	Vector names = props.getAnnotationNames();
	for(String name : (Vector<String>)names) {
	    String file = props.getAnnotationFile(name);
	    System.out.println(">>>\t Loading '" + name + "' ");
	    File fin = new File(file);
	    FileInputStream in = new FileInputStream(fin);
	    getAnnotationManager().importAnnotation(in, name);
	    //Writer w = sms.getAnnotationManager().exportAnnotation(name);
	    //System.out.println("writer: " + w);
	}
    }

    /**
     * Initialization
     */
    private void _initialize() {
	_ontologyManager = new DefaultOntologyManager();	
	_annotationManager = new DefaultAnnotationManager(this);	
    }


    ////////////////////////////////////////////////////////////////////////
    // PRIVATE DATA

    private AnnotationManager _annotationManager;
    private OntologyManager _ontologyManager;
    private ResourceManager _ResourceManager;

}
