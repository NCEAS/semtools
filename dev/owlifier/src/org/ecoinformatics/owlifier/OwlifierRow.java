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
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author Shawn Bowers
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
    * Check if this is an import block
    * @return true if an import block
    */
   public boolean isImportBlock() {
      return getBlockType() == OwlifierBlockType.IMPORT;
   }

   /**
    * Check if this is an entity block
    * @return true if an entity block
    */
   public boolean isEntityBlock() {
      return getBlockType() == OwlifierBlockType.ENTITY;
   }

   /**
    * Check if this is an synonym block
    * @return true if an synonym block
    */
   public boolean isSynonymBlock() {
      return getBlockType() == OwlifierBlockType.SYNONYM;
   }

   /**
    * Check if this is an overlap block
    * @return true if an overlap block
    */
   public boolean isOverlapBlock() {
      return getBlockType() == OwlifierBlockType.OVERLAP;
   }

   /**
    * Check if this is a relationship block
    * @return true if a relationship block
    */
   public boolean isRelationshipBlock() {
      return getBlockType() == OwlifierBlockType.RELATIONSHIP;
   }

   /**
    * Check if this is a transitive block
    * @return true if a transitive block
    */
   public boolean isTransitiveBlock() {
      return getBlockType() == OwlifierBlockType.TRANSITIVE;
   }

   /**
    * Check if this is a min block
    * @return true if a min block
    */
   public boolean isMinBlock() {
      return getBlockType() == OwlifierBlockType.MIN;
   }

   /**
    * Check if this is a max block
    * @return true if a max block
    */
   public boolean isMaxBlock() {
      return getBlockType() == OwlifierBlockType.MAX;
   }

   /**
    * Check if this is a exact block
    * @return true if a exact block
    */
   public boolean isExactBlock() {
      return getBlockType() == OwlifierBlockType.EXACT;
   }

   /**
    * Check if this is an inverse block
    * @return true if an inverse block
    */
   public boolean isInverseBlock() {
      return getBlockType() == OwlifierBlockType.INVERSE;
   }

   /**
    * Check if this is a sufficient block
    * @return true if a sufficient block
    */
   public boolean isSufficientBlock() {
      return getBlockType() == OwlifierBlockType.SUFFICIENT;
   }

   /**
    * Check if this is a sufficent block over an entity definition
    * @return true if a sufficient block over an entity defintion
    */
   public boolean isSufficientEntityBlock() {
      if(!isSufficientBlock())
         return false;
      if(getColumn(0) == null)
         return false;
      if(!"entity".equals(getColumn(0).getTrimmedValue()))
         return false;
      return true;
   }

   /**
    * Check if this is a sufficent block over a relationship definition
    * @return true if a sufficient block over a relationship defintion
    */
   public boolean isSufficientRelationshipBlock() {
      if(!isSufficientBlock())
         return false;
      if(getColumn(0) == null || getColumn(1) == null)
         return false;
      if(!"relationship".equals(getColumn(0).getTrimmedValue()))
         return false;
      if("not".equals(getColumn(1).getTrimmedValue()))
         return false;
      return true;
   }

   /**
    * Check if this is a sufficent block over a not-qualified relationship
    * definition
    * @return true if a sufficient block over a not-qualified relationship
    * defintion
    */
   public boolean isSufficientNotRelationshipBlock() {
      if(!isSufficientBlock())
         return false;
      if(getColumn(0) == null || getColumn(1) == null)
         return false;
      if(!"relationship".equals(getColumn(0).getTrimmedValue()))
         return false;
      if(!"not".equals(getColumn(1).getTrimmedValue()))
         return false;
      return true;
   }

   /**
    * Check if this is a description block
    * @return true if a description block
    */
   public boolean isDescriptionBlock() {
      return getBlockType() == OwlifierBlockType.DESCRIPTION;
   }

   /**
    * Check if this is a note block
    * @return true if a note block
    */
   public boolean isNoteBlock() {
      return getBlockType() == OwlifierBlockType.NOTE;
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

   /**
    * Get the entities of this row
    * @return the entities
    */
   public Set<String> getEntities() {
      Set<String> result = new HashSet();
      if(isEntityBlock())
         for(OwlifierColumn col : getColumns())
            result.add(col.getTrimmedValue());
      if(isSynonymBlock())
         for(OwlifierColumn col : getColumns())
            result.add(col.getTrimmedValue());
      if(isOverlapBlock())
         for(OwlifierColumn col : getColumns())
            result.add(col.getTrimmedValue());
      if(isRelationshipBlock())
         for(int i = 1; i < getLength(); i++)
            result.add(getColumn(i).getTrimmedValue());
      if(isTransitiveBlock())
         for(int i = 1; i < getLength(); i++)
            result.add(getColumn(i).getTrimmedValue());
      if(isMaxBlock() || isMinBlock() || isExactBlock())
         for(int i = 3; i < getLength(); i++)
            result.add(getColumn(i).getTrimmedValue());
      if(isSufficientEntityBlock())
         for(int i = 2; i < getLength(); i++)
            result.add(getColumn(i).getTrimmedValue());
      if(isSufficientRelationshipBlock())
         for(int i = 3; i < getLength(); i++)
            result.add(getColumn(i).getTrimmedValue());
      if(isSufficientNotRelationshipBlock())
         for(int i = 4; i < getLength(); i++)
            result.add(getColumn(i).getTrimmedValue());
      return result;
   }

   /**
    * Get the relationships of this row
    * @return the relationships
    */
   public Set<String> getRelationships() {
      Set<String> result = new HashSet();
      if(isRelationshipBlock() || isTransitiveBlock())
         result.add(getColumn(0).getTrimmedValue());
      if(isMinBlock() || isMaxBlock() || isExactBlock())
         result.add(getColumn(0).getTrimmedValue());
      if(isInverseBlock())
         for(OwlifierColumn col : getColumns())
            result.add(col.getTrimmedValue());
      if(isSufficientRelationshipBlock() || isSufficientNotRelationshipBlock())
         result.add(getColumn(0).getTrimmedValue());
      if(isTransitiveBlock())
         result.add(getColumn(0).getTrimmedValue());
      return result;
   }

   /**
    * Ensure this row is well formed
    * @throws java.lang.Exception
    */
   public void validate() throws Exception {
      validateRowLength();
      validateTermSpaces();
      validateNullColumns();
   }

   /**
    * Validate row lengths
    * @throws java.lang.Exception
    */
   private void validateRowLength() throws Exception {
      if(isImportBlock() && getLength() < 2)
         throw new Exception("Invalid import block: " + this);
      if(isEntityBlock() && getLength() < 1)
         throw new Exception("Invalid entity block: " + this);
      if(isSynonymBlock() && getLength() < 2)
         throw new Exception("Invalid synonym block: " + this);
      if(isOverlapBlock() && getLength() < 2)
         throw new Exception("Invalid overlap block: " + this);
      if(isRelationshipBlock() && getLength() < 3)
         throw new Exception("Invalid relationship block: " + this);
      if(isTransitiveBlock() && getLength() < 3)
         throw new Exception("Invalid transitive block: " + this);
      if(isMinBlock() && getLength() < 4)
         throw new Exception("Invalid min block: " + this);
      if(isMaxBlock() && getLength() < 4)
         throw new Exception("Invalid max block: " + this);
      if(isExactBlock() && getLength() < 4)
         throw new Exception("Invalid exact block: " + this);
      if(isInverseBlock() && getLength() < 2)
         throw new Exception("Invalid inverse block: " + this);
      if(isSufficientBlock() && getLength() < 3)
         throw new Exception("Invalid sufficient block: " + this);
      else {
         String blocktype = getColumn(0).getValue().toLowerCase();
         if("entity".equals(blocktype) && getLength() != 3)
            throw new Exception("Invalid sufficient block: " + this);
         else if("relationship".equals(blocktype)) {
            String qual = getColumn(1).getValue().trim().toLowerCase();
            if(qual.equals("not") && getLength() != 5)
               throw new Exception("Invalid sufficient block: " + this);
            if(!qual.equals("not") && getLength() != 4)
               throw new Exception("Invalid sufficient block: " + this);
         }
      }
   }

   /**
    * Validate terms for spaces
    * @throws java.lang.Exception
    */
   private void validateTermSpaces() throws Exception {
      if(!isDescriptionBlock() && !isNoteBlock())
         for(OwlifierColumn col : getColumns()) {
            String term = col.getValue().trim();
            if(term.matches(".*\\s.*"))
               throw new Exception("Invalid term: " + term);
         }
      else if(isDescriptionBlock()) {
         String term = getColumn(0).getValue().trim();
         if(term.matches(".*\\s.*"))
            throw new Exception("Invalid term: " + term);
      }

   }

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
   public Object clone() {
      OwlifierRow r = new OwlifierRow();
      r.setBlockType(this.type);
      for(OwlifierColumn c : getColumns())
         r.addColumn((OwlifierColumn) c.clone());
      return r;
   }

   @Override
   public String toString() {
      String str = getBlockType() + " ";
      for(OwlifierColumn col : getColumns())
         str += col + " ";
      return str;
   }
}