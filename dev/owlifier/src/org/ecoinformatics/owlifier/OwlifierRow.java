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
 *
 * @author Shawn Bowers
 */
public class OwlifierRow {

   /** string delimeter for printing rows */
   protected String DELIMETER = " | ";
   private List<OwlifierColumn> row = new ArrayList();
   private OwlifierBlock block;

   public OwlifierBlock getBlock() throws Exception {
      String type = getColumn(0).getTrimmedValue().toLowerCase();
      if(type.equals("entity"))
         return new OwlifierEntityBlock(this);
      else if(type.equals("import"))
         return new OwlifierImportBlock(this);
      else if(type.equals("note"))
         return new OwlifierNoteBlock(this);
      else if(type.equals("synonym"))
         return new OwlifierSynonymBlock(this);
      else if(type.equals("overlap"))
         return new OwlifierOverlapBlock(this);
      else if(type.equals("relationship"))
         return new OwlifierRelationshipBlock(this);
      else if(type.equals("transitive"))
         return new OwlifierTransitiveBlock(this);
      else if(type.equals("min"))
         return new OwlifierMinBlock(this);
      else if(type.equals("max"))
         return new OwlifierMaxBlock(this);
      else if(type.equals("exact"))
         return new OwlifierExactBlock(this);
      else if(type.equals("inverse"))
         return new OwlifierInverseBlock(this);
      else if(type.equals("sufficient"))
         return new OwlifierSufficientBlock(this);
      else if(type.equals("description"))
         return new OwlifierDescriptionBlock(this);

      else
         throw new Exception("Undefined block type: " + type);
   }

//   /**
//    * Check if this is a sufficent block over an entity definition
//    * @return true if a sufficient block over an entity defintion
//    */
//   public boolean isSufficientEntityBlock() {
//      if(!isSufficientBlock())
//         return false;
//      if(getColumn(0) == null)
//         return false;
//      if(!"entity".equals(getColumn(0).getTrimmedValue()))
//         return false;
//      return true;
//   }
//
//   /**
//    * Check if this is a sufficent block over a relationship definition
//    * @return true if a sufficient block over a relationship defintion
//    */
//   public boolean isSufficientRelationshipBlock() {
//      if(!isSufficientBlock())
//         return false;
//      if(getColumn(0) == null || getColumn(1) == null)
//         return false;
//      if(!"relationship".equals(getColumn(0).getTrimmedValue()))
//         return false;
//      if("not".equals(getColumn(1).getTrimmedValue()))
//         return false;
//      return true;
//   }

//   /**
//    * Check if this is a sufficent block over a not-qualified relationship
//    * definition
//    * @return true if a sufficient block over a not-qualified relationship
//    * defintion
//    */
//   public boolean isSufficientNotRelationshipBlock() {
//      if(!isSufficientBlock())
//         return false;
//      if(getColumn(0) == null || getColumn(1) == null)
//         return false;
//      if(!"relationship".equals(getColumn(0).getTrimmedValue()))
//         return false;
//      if(!"not".equals(getColumn(1).getTrimmedValue()))
//         return false;
//      return true;
//   }
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

//   /**
//    * Ensure this row is well formed
//    * @throws java.lang.Exception
//    */
//   public void validate() throws Exception {
//      validateTermSpaces();
//      validateNullColumns();
//   }
//
//
//   /**
//    * Validate terms for spaces
//    * @throws java.lang.Exception
//    */
//   private void validateTermSpaces() throws Exception {
//      if(!isDescriptionBlock() && !isNoteBlock())
//         for(OwlifierColumn col : getColumns()) {
//            String term = col.getValue().trim();
//            if(term.matches(".*\\s.*"))
//               throw new Exception("Invalid term: " + term);
//         }
//      else if(isDescriptionBlock()) {
//         String term = getColumn(0).getValue().trim();
//         if(term.matches(".*\\s.*"))
//            throw new Exception("Invalid term: " + term);
//      }
//
//   }
//
   /**
    * Validate for missing (null) columns
    * @throws java.lang.Exception
    */
   private void validateNullColumns() throws Exception {
      for(int i = 0; i < getLength(); i++) {
         String term = getColumn(i).getValue().trim();
         if("".equals(term))
            for(int j = i + 1; j < getLength(); j++) {
               String nextTerm = getColumn(j).getValue().trim();
               if(!"".equals(nextTerm))
                  throw new Exception("Invalid row: " + this);
            }

      }
   }

   @Override
   public OwlifierRow clone() {
      OwlifierRow r = new OwlifierRow();
      for(OwlifierColumn c : r.getColumns())
         r.addColumn((OwlifierColumn) c.clone());
      return r;
   }

   @Override
   public String toString() {
      String str = "";
      for(int i = 0; i < getLength(); i++) {
         str += getColumn(i).toString();
         if(i + 1 < getLength())
            str += DELIMETER;
      }
      return str;
   }
}