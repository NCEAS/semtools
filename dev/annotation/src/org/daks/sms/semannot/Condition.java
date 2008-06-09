/**
 *    '$RCSfile: Condition.java,v $'
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

package org.daks.sms.semannot;


/**
 * Objects of this class represent annotation mapping conditions
 */
public class Condition {

    /**
     * Default constructor
     */
    public Condition() {
    }

    /**
     * @param attribute the attribute
     * @param operator the operator
     * @param value the value
     */
    public Condition(String attribute, String operator, String value) {
	_attribute = attribute;
	_operator = operator;
	_value = value;
    }

    /** 
     * Set the dataset attribute
     * @param attribute the attribute
     */
    public void setAttribute(String attribute) {
	_attribute = attribute;
    }

    /** 
     * Get the dataset attribute
     * @return the attribute
     */
    public String getAttribute() {
	return _attribute;
    }

    /**
     * Set the value
     * @param value the value
     */
    public void setValue(String value) {
        _value = value;
    }

    /**
     * Get the value
     * @return the value
     */
    public String getValue() {
        return _value;
    }

    /**
     * Set the operator
     * @param operator the operator
     */
    public void setOperator(String operator) {
        _operator = operator;
    }

    /**
     * Get the operator
     * @return the operator
     */
    public String getOperator() {
        return _operator;
    }

    /**
     * Determine if a given string denotes an operator
     * @param op the string to test
     * @return true if the given string is an operator
     */
    public static boolean isOperator(String op) {
	if(op == null)
	    return false;
	if(op.equals(LESS_THAN) || op.equals(GREATER_THAN) || 
	   op.equals(EQUAL) || op.equals(LESS_THAN_EQUAL) ||
	   op.equals(GREATER_THAN_EQUAL) || op.equals(NOT_EQUAL))
	    return true;
	return false;
    }


    public static String LESS_THAN = "lt";
    public static String GREATER_THAN = "gt";
    public static String EQUAL = "eq";
    public static String LESS_THAN_EQUAL = "le";
    public static String GREATER_THAN_EQUAL = "ge";
    public static String NOT_EQUAL = "ne";


    private String _attribute;
    private String _value;
    private String _operator;

} 