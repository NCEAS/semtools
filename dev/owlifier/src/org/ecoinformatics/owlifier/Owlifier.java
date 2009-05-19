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

import java.net.URI;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * @author Shawn Bowers
 */
public class Owlifier {

   private static boolean guiFlag = false;
   private static boolean warnFlag = false;
   private static boolean oboeFlag = false;
   private static boolean tabFlag = false;
   private static boolean classifyFlag = false;
   private static String infile;
   private static String outfile;

   public static void main(String[] args) {
      try {
         parseArgs(args);
         if(guiFlag)
            throw new Exception("-gui not supported");
         else {
            OwlifierSpreadsheet sheet = read(new FileInputStream(infile));
            if(sheet == null)
               throw new Exception("Unable to read: " + infile);
            sheet.complete();
            if(warnFlag)
               sheet.validate();
            URI uri = new File(outfile).toURL().toURI();
            OwlifierOntology ont =
                  new OwlifierOntology(sheet, uri, oboeFlag);
            if(classifyFlag)
               ont.classify();
            ont.writeAsRDFXML();
         }
      } catch(Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Read an owlifier spreadsheet
    * @param in the spreadsheet
    * @return the parsed spreadsheet representation
    */
   public static OwlifierSpreadsheet read(InputStream in) throws IOException {
      OwlifierSpreadsheet sheet = new OwlifierSpreadsheet();
      BufferedReader r = new BufferedReader(new InputStreamReader(in));
      OwlifierBlockType currBlockType = null;
      String line = "";
      while((line = r.readLine()) != null) {
         String[] vals = line.split(getSeparator());
         if(vals.length > 0 && !isEmptyRow(vals)) {
            OwlifierRow row = new OwlifierRow();
            String strType = vals[0].trim();
            if(!strType.equals(""))
               currBlockType = getBlockType(strType);
            if(currBlockType == null) {
               String msg = "ERROR: invalid block type '" + vals[0] + "'";
               throw new IOException(msg);
            }
            row.setBlockType(currBlockType);
            for(int i = 1; i < vals.length; i++) {
               String val = vals[i].trim();
               OwlifierColumn column = new OwlifierColumn();
               if(!val.equals(""))
                  column.setValue(val);
               row.addColumn(column);
            }
            sheet.addRow(row);
         }
      }
      return sheet;
   }

   /**
    */
   private static void printUsageAndExit() {
      String str = "";
      str += "usage: java owlifier -gui | " +
            "[options] -in spreadsheet -out ontology\n";
      str += "\n";
      str += "where:\n";
      str += " -gui             start owlifier ui\n";
      str += " -in              the owlifier spreadsheet\n";
      str += " -out             the generated owl ontology\n";
      str += "\n";
      str += "and options are:\n";
      str += " -oboe            output as oboe ontology\n";
      str += " -classify        classify ontology before writing\n";
      str += " -warn            include warnings (verbose)\n";
      str += " -tab             tab-delimited input file\n";
      str += " -csv             csv input file (default)\n";
      str += " -help            print help information\n";
      System.out.println(str);
      System.exit(0);
   }

   /**
    * Parse command line arguments
    */
   private static void parseArgs(String[] args) {
      if(args.length == 2 && args[0].equals("-ant"))
         args = args[1].split("\\s");

      if(args.length == 0)
         printUsageAndExit();
      else if(args.length == 1)
         if(args[0].equals("-gui"))
            guiFlag = true;
         else
            printUsageAndExit();
      else if(args.length > 1) {
         // find the flags and the -in and -out
         int x_in = -1, x_out = -1;
         for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-gui"))
               printUsageAndExit();
            if(args[i].contains("-warn"))
               warnFlag = true;
            if(args[i].contains("-oboe"))
               oboeFlag = true;
            if(args[i].contains("-tab"))
               tabFlag = true;
            if(args[i].contains("-classify"))
               classifyFlag = true;
            if(args[i].equals("-in"))
               x_in = i;
            if(args[i].equals("-out"))
               x_out = i;
         }
         if(x_in == -1 || x_out == -1)
            printUsageAndExit();
         if(args.length < x_in || args.length < x_out)
            printUsageAndExit();
         infile = args[x_in + 1];
         outfile = args[x_out + 1];
      }
   }

   /**
    * The spreadsheet cell separator
    * @return the separator string (e.g., "," or "\t")
    */
   private static String getSeparator() {
      if(tabFlag)
         return "\t";
      return ",";
   }

   /**
    * Check if a given row is empty
    * @param row the row to check
    * @return true if empty
    */
   private static boolean isEmptyRow(String[] row) {
      for(String val : row)
         if(!val.trim().equals(""))
            return false;
      return true;
   }

   /**
    * Get the type of block from the block string
    * @param type the given bock type
    * @return the parsed block type
    */
   private static OwlifierBlockType getBlockType(String type) {
      type = type.trim().toLowerCase();
      if(type.equals("import"))
         return OwlifierBlockType.IMPORT;
      else if(type.equals("entity"))
         return OwlifierBlockType.ENTITY;
      else if(type.equals("synonym"))
         return OwlifierBlockType.SYNONYM;
      else if(type.equals("overlap"))
         return OwlifierBlockType.OVERLAP;
      else if(type.equals("relationship"))
         return OwlifierBlockType.RELATIONSHIP;
      else if(type.equals("transitive"))
         return OwlifierBlockType.TRANSITIVE;
      else if(type.equals("max"))
         return OwlifierBlockType.MAX;
      else if(type.equals("min"))
         return OwlifierBlockType.MIN;
      else if(type.equals("exact"))
         return OwlifierBlockType.EXACT;
      else if(type.equals("inverse"))
         return OwlifierBlockType.INVERSE;
      else if(type.equals("sufficient"))
         return OwlifierBlockType.SUFFICIENT;
      else if(type.equals("description"))
         return OwlifierBlockType.DESCRIPTION;
      else if(type.equals("note"))
         return OwlifierBlockType.NOTE;
      return null;
   }
}