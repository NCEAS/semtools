/**
 * Copyright (c) 2009 The Regents of the University of California.
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

package org.ecoinformatics.owlifier;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class OwlifierRow {

    private List<OwlifierColumn> row = new ArrayList();
    private OwlifierBlockType type;

    /**
     * Set the type of this block
     * @param type the type of this block
     */
    public void setBlockType(OwlifierBlockType type) {
	this.type = type;
    }
    
    /**
     * Get the type of this block
     * @return the block type
     */
    public OwlifierBlockType getBlockType() {
	return type;
    }

    /**
     * Add the column to the end of this row
     * @param column the column 
     */
    public void addColumn(OwlifierColumn column) {
	if(column != null)
	    row.add(column);
    }

    /**
     * Insert the column at the specified position in this row. Shifts
     * the column currently at that position (if any) and any
     * subsequent elements to the right (adds one to their
     * indices). The index must be between 0 and row-length minus one
     * (inclusive).
     * @param index the index at which the column is to be inserted
     * @param column the column to be inserted
     */
    public void addColumnAt(int index, OwlifierColumn column) {
	if(column != null && index >= 0 && index < getLength())
	    row.add(index, column);
    }

    /** 
     * Replace the column at the specified position in this row. The
     * index must be between 0 and row-length minus one (inclusive). 
     * @param index the index at which the column is to be replace
     * @param column the new column 
     */
    public void replaceColumn(int index, OwlifierColumn column) {
	if(column != null && index >= 0 && index < getLength())
	    row.set(index, column);
    }

    /** 
     * Remove the column at the specified position in this row. The
     * index must be between 0 and row-length minus one (inclusive). 
     * @param index the index at which the column is to be removed
     */
    public void removeColumn(int index) {
	if(index >= 0 && index < getLength())
	    row.remove(index);
    }

    /**
     * Get the number of columns in this row
     * @return the length
     */
    public int getLength() {
	return row.size();
    }

    /**
     * Get the row as a list of columns
     * @return the row as a list
     */
    public List<OwlifierColumn> getColumns() {
	return row;
    }

    /**
     * Get the column at the specified position in this row.
     * @return the column
     */
    public OwlifierColumn getColumn(int index) {
	return row.get(index);
    }

    public Object clone() {
	OwlifierRow r = new OwlifierRow();
	r.setBlockType(this.type);
	for(OwlifierColumn c : getColumns()) 
	    r.addColumn((OwlifierColumn)c.clone());
	return r;
    }

}