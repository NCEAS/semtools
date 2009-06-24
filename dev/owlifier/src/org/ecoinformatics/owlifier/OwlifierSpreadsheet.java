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
 * A representation of an owlifier spreadsheet
 * @author Shawn Bowers
 */
public class OwlifierSpreadsheet {

   private List<OwlifierRow> rows = new ArrayList();

   /**
    * Add the row to the end of this spreadsheet
    * @param row the row
    */
   public void addRow(OwlifierRow row) {
      if(row != null)
         rows.add(row);
   }

   /**
    * Insert the row at the specified position in this row. Shifts
    * the row currently at that position (if any) and any subsequent
    * rows to the right (adds one to their indices). The index must
    * be between 0 and number of rows minus one (inclusive).
    * @param index the index at which the column is to be inserted
    * @param row the row to be inserted
    */
   public void addRowAt(int index, OwlifierRow row) {
      if(row != null && index >= 0 && index < getLength())
         rows.add(index, row);
   }

   /**
    * Replace the row at the specified position in this
    * spreadsheet. The index must be between 0 and the number of rows
    * minus one (inclusive).
    * @param index the index at which the row is to be replace
    * @param row the new row
    */
   public void replaceRow(int index, OwlifierRow row) {
      if(row != null && index >= 0 && index < getLength())
         rows.set(index, row);
   }

   /**
    * Remove the row at the specified position in this spreadsheet
    * @param index the index at which the row is to be removed
    */
   public void removeRow(int index) {
      if(index >= 0 && index < getLength())
         rows.remove(index);
   }

   /**
    * Get the number of rows in this spreadsheet
    * @return the length
    */
   public int getLength() {
      return rows.size();
   }

   /**
    * Get the spreadsheet as a list of rows
    * @return the spreadsheet as a list
    */
   public List<OwlifierRow> getRows() {
      return rows;
   }

   /**
    * Get the row at the specified position in this spreadsheet
    * @return the row
    */
   public OwlifierRow getRow(int index) {
      return rows.get(index);
   }

   /**
    * Fill in the missing columns implied from previous columns in
    * the spreadsheet
    */
   public void complete() {
      for(int i = 0; i < getLength() - 1; i++) {
         OwlifierRow r1 = getRow(i);
         OwlifierRow r2 = getRow(i + 1);
         for(int j = 0; j < r1.getLength(); j++) {
            OwlifierColumn c = (OwlifierColumn) r1.getColumn(j).clone();
            if(r2.getLength() < j)
               r2.addColumn(c);
            else if(r2.getColumn(j).getValue() == null)
               r2.replaceColumn(j, c);
            else
               break;
         }
      }
   }

   /**
    * TODO Ensure that the spreadsheet is well-formed, assuming the
    * spreadsheet has been completed
    */
   public void validate() throws Exception {
//      String msg = "";
//      // validate each row
//      for(OwlifierRow row : getRows())
//         row.getBlock();
//      Set<String> allrels = getObjectProperties();
//      // check if entity and relationship have same name
//      for(String entity : getClasses())
//         if(allrels.contains(entity)) {
//            msg = "Entity and relationship with same name: " + entity;
//            throw new Exception(msg);
//         }
//      // check if transitive and relationship with same name
//      Set<String> rels = new HashSet();
//      for(OwlifierRow row : getRows())
//         if(row.isRelationshipBlock())
//            rels.add(row.getColumn(0).getTrimmedValue());
//      for(OwlifierRow row : getRows())
//         if(row.isTransitiveBlock()) {
//            String trel = row.getColumn(0).getTrimmedValue();
//            if(rels.contains(trel)) {
//               msg = "'" + trel + "' used in transitive and relationship block";
//               throw new Exception(msg);
//            }
//         }
//      // check if each min, max, or exact property is defined in either
//      // a transtive or relationship block
//      for(OwlifierRow row : getRows())
//         if(row.isTransitiveBlock())
//            rels.add(row.getColumn(0).getTrimmedValue());
//      for(OwlifierRow row : getRows())
//         if(row.isMaxBlock() || row.isMinBlock() || row.isExactBlock())
//            if(!rels.contains(row.getColumn(0).getTrimmedValue()))
//               throw new Exception("Undefined property in: " + row);
   }

   @Override
   public Object clone() {
      OwlifierSpreadsheet s = new OwlifierSpreadsheet();
      for(OwlifierRow r : getRows())
         s.addRow((OwlifierRow) r.clone());
      return s;
   }

   @Override
   public String toString() {
      String str = "";
      for(OwlifierRow row : getRows())
         str += row + "\n";
      return str;
   }
}