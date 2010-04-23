/**
 *    '$RCSfile: SMSProperties.java,v $'
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

package org.ecoinformatics.sms;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.io.File;
import java.io.FileInputStream;


public class SMSProperties {
    
    /**
     * Private constructor
     */
    private SMSProperties() {}

    /**
     * Returns a singleton instance
     * @return the instance
     */
    public static SMSProperties getInstance() {
	if(_instance == null) {
	    _instance = new SMSProperties();
	    _instance._init();
	}
	return _instance;
    }

    public Vector getAnnotationNames() {
	return new Vector(_annotations.keySet());
    }

    public String getAnnotationFile(String annotationName) {
	return (String)_annotations.get(annotationName);
    }

    //////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS

    /**
     * Initialize the properties file
     */
    private void _init() {
	// load the properties file
	_properties = new Properties();
	try {
	    File propFile = new File("config/sms.properties");
	    _properties.load(new FileInputStream(propFile));
	} catch(Exception e) {
	    e.printStackTrace();
	}
	_buildAnnotationFiles();
    }

    private void _buildAnnotationFiles() {
	Enumeration ps = _properties.propertyNames();
	while(ps.hasMoreElements()) {
	    String name = (String)ps.nextElement();
	    String[] args = name.split("\\.");
	    if(args.length == 3 && args[0].equals("sms") && 
	       args[1].equals("annotation")) 
		_annotations.put(args[2], _properties.getProperty(name));
	}
    }


    //////////////////////////////////////////////////////////////////////
    // PRIVATE DATA

    private static SMSProperties _instance;
    private Properties _properties;
    private Hashtable _annotations = new Hashtable();
    

}
