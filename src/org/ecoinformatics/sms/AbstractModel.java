/**
 *    '$RCSfile: AbstractModel.java,v $'
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

package org.ecoinformatics.sms;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/**
 * Helper class for MVC
 */
public abstract class AbstractModel {

    /**
     * Default constructor.
     */
    public AbstractModel() {
	_propChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Add a property change listener
     * @param listener the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	_propChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property change listener
     * @param listener the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	_propChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Fire a property change
     * @param propertyName name of changed property
     * @param oldValue the old object value
     * @param newValue the new object value
     */
    public void firePropertyChange(String propertyName, Object oldValue, 
				   Object newValue) 
    {
	_propChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }


    //////////////////////////////////////////////////////////////////////
    // PRIVATE DATA

    private PropertyChangeSupport _propChangeSupport;

}