/**
 *    '$RCSfile: Mapping.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2008-06-02 19:51:10 $'
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

package org.ecoinformatics.sms.annotation;

import java.util.List;
import java.util.ArrayList;


/**
 * Objects of this class represent mappings to measurements
 */
public class Mapping {

    /** 
     * Set the dataset attribute being mapped
     * @param attribute the mapped attribute
     */
    public void setAttribute(String attribute) {
	_attribute = attribute;
    }

    /** 
     * Get the attribute of this mapping
     * @return the attribute
     */
    public String getAttribute() {
	return _attribute;
    }

    /**
     * Set the measurement being mapped to
     * @param measurement the measurement
     */
    public void setMeasurement(Measurement measurement) {
	_measurement = measurement;
    }

    /**
     * Get the measurement being mapped to
     * @return the measurement
     */
    public Measurement getMeasurement() {
	return _measurement;
    }

    /** 
     * Set an optional value as the measurement value
     * @param value the value to use in the measurement
     */
    public void setValue(String value) {
	_value = value;
    }

    /** 
     * Get the value to use for the measurement value
     * @return the value
     */
    public String getValue() {
	return _value;
    }

    /** 
     * Add a condition to this mapping
     * @param condition the condition
     */
    public void addCondition(Condition condition) {
	if(condition != null && !_conditions.contains(condition))
	    _conditions.add(condition);
    }

    /**
     * Remove a condition form this mapping
     * @param condition the condition to remove
     */
    public void removeCondition(Condition condition) {
	_conditions.remove(condition);
    }

    /**
     * Get the conditions assigned to this mapping
     * @return the set of conditions
     */
    public List<Condition> getConditions() {
	return _conditions;
    }

    public String toString(){
    	String str = "attr=" + _attribute +", meas="+_measurement + ", value="+_value +", conditions="+_conditions;
    	return str;
    }

    private String _attribute;
    private Measurement _measurement;
    private String _value;
    private List<Condition> _conditions = new ArrayList();

} 