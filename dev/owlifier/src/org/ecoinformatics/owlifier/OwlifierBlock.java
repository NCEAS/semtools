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

/**
 * Base class for owlifier blocks
 * @author sbowers
 */
public abstract class OwlifierBlock {

   /** the row of this block */
   private OwlifierRow row;

   /**
    * Create a block from a row
    * @param row
    */
   public OwlifierBlock(OwlifierRow row) {
      this.row = row;
   }

   /**
    * Get the row this block was created from
    * @return
    */
   public OwlifierRow getRow() {
      return row;
   }

   /**
    * Get the type of this block
    * @return the type
    */
   public abstract String getBlockType();

   /**
    * Add this block to the ontology
    * @param ont the ontology
    * @throws java.lang.Exception
    */
   public abstract void addToOntology(OwlifierOntology ont) throws Exception;

   @Override
   public String toString() {
      if(row != null)
         return row.toString();
      return "";
   }
}
