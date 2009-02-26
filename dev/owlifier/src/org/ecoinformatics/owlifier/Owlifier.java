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

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 */
public class Owlifier {

    private static boolean guiFlag = false;
    private static boolean warnFlag = false;
    private static boolean oboeFlag = false;
    private static boolean tabFlag = false;
    private static String infile;
    private static String outfile;

    public static void main(String [] args) {
	parseArgs(args);
	
	if(guiFlag) {
	    // start in gui mode
	    System.out.println("Starting Owlifier UI");
	}
	else {
	    System.out.println("Reading file: " + infile);
	    System.out.println("Writing to file: " + outfile);
	    try {
		OwlifierSpreadsheet os = read(new FileInputStream(infile));
		System.out.println(os);
		OwlifierSpreadsheet osc = sheet.complete();
		System.out.println(osc);
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	}

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
	str += " -warn            include warnings (verbose)\n";
	str += " -tab             tab-delimited input file\n";
	str += " -csv             csv input file (default)\n";
	str += " -help            print help information\n";	
	System.out.println(str);
	System.exit(0);
    }

    /**
     */
    private static void parseArgs(String [] args) {
	if(args.length == 2 && args[0].equals("-ant"))
	    args = args[1].split("\\s");

	if(args.length == 0) 
	    printUsageAndExit();
	else if(args.length == 1) {
	    if(args[0].equals("-gui"))
		guiFlag = true;
	    else 
		printUsageAndExit();
	}
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

    private static String getSeparator() {
	if(tabFlag) 
	    return "\t";
	return ",";
    }

    /**
     */
    public static OwlifierSpreadsheet read(InputStream in) throws IOException {
	OwlifierSpreadsheet sheet = new OwlifierSpreadsheet();
	BufferedReader r = new BufferedReader(new InputStreamReader(in));
	String line = ""; 
	while((line = r.readLine()) != null) {
	    String [] vals = line.split(getSeparator());
	    if(vals.length > 0) {
		OwlifierRow row = new OwlifierRow();
		for(String val : vals) {
		    OwlifierColumn column = new OwlifierColumn();
		    if(!val.trim().equals(""))
			column.setValue(val.trim());
		    row.addColumn(column);
		}
		sheet.addRow(row);
	    }
	}
	return sheet;
    }


    
}