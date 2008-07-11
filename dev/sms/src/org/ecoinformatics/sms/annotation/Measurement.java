/**
 *    '$RCSfile: Measurement.java,v $'
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
 * Objects of this class represent observation ameasurements
 */
public class Measurement {

    /** 
     * Set the label of the measurement
     * @param label the measurement label
     */
    public void setLabel(String label) {
	_label = label;
    }

    /** 
     * Get the measurement label
     * @return the label
     */
    public String getLabel() {
	return _label;
    }

    /**
     * Set the measurement standard
     * @param standard the standard
     */
    public void setStandard(Standard standard) {
	_standard = standard;
    }

    /**
     * Get the measurement standard
     * @return the standard
     */
    public Standard getStandard() {
	return _standard;
    }

    /** 
     * Set the measurement precision
     * @param precision the precision
     */
    public void setPrecision(double precision) {
	_precision = precision;
    }

    /** 
     * Get the measurement precision
     * @return the precision
     */
    public double getPrecision() {
	return _precision;
    }

    /** 
     * Set the measurement value
     * @param value the value 
     */
    public void setValue(String value) {
	_value = value;
    }

    /** 
     * Get the measurement value 
     * @return the value
     */
    public String getValue() {
	return _value;
    }

    /** 
     * Add a characteristic to the measurement
     * @param characteristic the characteristic
     */
    public void addCharacteristic(Characteristic characteristic) {
	if(characteristic != null && !_characteristics.contains(characteristic))
	    _characteristics.add(characteristic);
    }

    /**
     * Remove a characteristic form this measurement
     * @param characteristic the characteristic to remove
     */
    public void removeCharacteristic(Characteristic characteristic) {
	_characteristics.remove(characteristic);
    }

    /**
     * Get the characteristics of the measurement
     * @return the set of characteristics
     */
    public List<Characteristic> getCharacteristics() {
	return _characteristics;
    }

    /**
     * Set whether this is a key for the observation
     * @param isKey if true, this is a key for the observation
     */
    public void setKey(boolean isKey) {
        _isKey = isKey;
    }

    /**
     * Get the key status of this measurement
     * @return true if this measurement is a key of the observation
     */
    public boolean isKey() {
        return _isKey;
    }

    /**
     * Set whether this is a partial key for the observation
     * @param isPartialKey if true, this is a partial key for the
     * observation
     */
    public void setPartialKey(boolean isPartialKey) {
        _isPartialKey = isPartialKey;
    }

    /**
     * Get the partial key status of this measurement
     * @return true if this measurement is a partial key of the observation
     */
    public boolean isPartialKey() {
        return _isPartialKey;
    }


    private String _label;
    private List<Characteristic> _characteristics = new ArrayList();    
    private Standard _standard;
    private double _precision = 1.0;
    private String _value;
    private boolean _isKey;
    private boolean _isPartialKey;

} 