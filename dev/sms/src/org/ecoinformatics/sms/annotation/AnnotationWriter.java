
/**
 *    '$Id$'
 *
 *     '$Author$'
 *       '$Date$'
 *   '$Revision$'
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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import org.ecoinformatics.sms.ontology.Ontology;

public class AnnotationWriter {

   /**
    * Write an annotation to an output stream
    * @param annotation the annotation
    * @param out the output stream
    */
   public static void write(Annotation annotation, OutputStream out) {
      PrintStream s = new PrintStream(out);
      s.print("<?xml version=\"1.0\"?>\n");
      _writeAnnotation(annotation, s);
   }

   /**
    * Helper method to write the annotation
    * @param a the annotation
    * @param s the output stream
    */
   private static void _writeAnnotation(Annotation a, PrintStream s) {
      s.print("<sms:annotation");
      if(a.getURI() != null)
         s.print(" xmlns=\"" + a.getURI() + "\"");
      s.print(" xmlns:sms=\"" + Annotation.ANNOTATION_NS + "\"");
      for(Ontology o : a.getOntologies())
         s.print(" xmlns:" + o.getPrefix() + "=\"" + o.getURI() + "\"");
      if(a.getEMLPackage() != null)
         s.print(" emlPackage=\"" + a.getEMLPackage() + "\"");
      if(a.getDataTable() != null)
         s.print(" dataTable=\"" + a.getDataTable() + "\"");
      s.print(">\n");
      for(Observation o : a.getObservations())
         _writeObservation(o, s);
      for(Mapping m : a.getMappings())
         _writeMapping(m, s);
      s.print("</sms:annotation>\n");
   }

   /**
    * Helper method to write an observation
    * @param o the observation
    * @param s the output stream
    */
   private static void _writeObservation(Observation o, PrintStream s) {
      s.print(_indent1 + "<sms:observation");
      if(o.getLabel() != null)
         s.print(" label=\"" + o.getLabel() + "\"");
      if(o.isDistinct())
         s.print(" distinct=\"yes\"");
      s.print(">\n");
      Entity e = o.getEntity();
    
      if(e != null && e.getOntology() != null)
    	  s.print(_indent2 + "<sms:entity id=\"" +
           e.getOntology().getPrefix() + ":" +
           e.getName() + "\"/>\n");
           
      for(Measurement m : o.getMeasurements())
         _writeMeasurement(m, s);
      for(Context c : o.getContexts())
         _writeContext(c, s);
      s.print(_indent1 + "</sms:observation>\n");
   }

   /**
    * Helper method to write a mapping
    * @param m the mapping
    * @param s the output stream
    */
   private static void _writeMapping(Mapping m, PrintStream s) {
      s.print(_indent1 + "<sms:map");
      if(m.getAttribute() != null)
         s.print(" attribute=\"" + m.getAttribute() + "\"");
      Measurement r = m.getMeasurement();
      if(r != null && r.getLabel() != null)
         s.print(" measurement=\"" + r.getLabel() + "\"");
      if(m.getValue() != null)
         s.print(" value=\"" + m.getValue() + "\"");
      if(m.getConditions().size() > 0) {
         s.print(" if=\"");
         for(Iterator i = m.getConditions().iterator(); i.hasNext();) {
            Condition c = (Condition) i.next();
            s.print(c.getAttribute() + " " + c.getOperator() + " " +
                    _quoteValue(c.getValue()));
            if(i.hasNext())
               s.print(", ");
         }
         s.print("\"");
      }
      s.print("/>\n");
   }

   /**
    * Returns a quoted value for values with spaces
    * @return quoted string (if original has spaces)
    */
   private static String _quoteValue(String str) {
      if(str.matches(".*\\s.*"))
         return "'" + str + "'";
      return str;
   }

   /**
    * Helper method to write a measurement
    * @param m the measurement
    * @param s the output stream
    */
   private static void _writeMeasurement(Measurement m, PrintStream s) {
      s.print(_indent2 + "<sms:measurement");
      if(m.getLabel() != null)
         s.print(" label=\"" + m.getLabel() + "\"");
      s.print(" precision=\"" + m.getPrecision() + "\"");
      if(m.getValue() != null)
         s.print(" value=\"" + m.getValue() + "\"");
      if(m.isKey())
         s.print(" key=\"yes\"");
      s.print(">\n");
      for(Characteristic c : m.getCharacteristics())
          if(c.getOntology() != null)
             s.print(_indent3 + "<sms:characteristic id=\"" +
                     c.getOntology().getPrefix() + ":" +
                     c.getName() + "\"/>\n");
      Standard d = m.getStandard();
      if(d != null && d.getOntology() != null)
         s.print(_indent3 + "<sms:standard id=\"" +
                 d.getOntology().getPrefix() + ":" +
                 d.getName() + "\"/>\n");
      Protocol p = m.getProtocol();
      if(p != null && p.getOntology() != null)
         s.print(_indent3 + "<sms:protocol id=\"" +
                 p.getOntology().getPrefix() + ":" +
                 p.getName() + "\"/>\n");
      if(m.getDomainValues().size() > 1) {
         s.print(_indent3 + "<sms:domain>\n");
         for(Entity v : m.getDomainValues())
            s.print(_indent4 + "<sms:entity id=\"" +
                    v.getOntology().getPrefix() + ":" +
                    v.getName() + "\">\n");
         s.print(_indent3 + "</sms:domain>\n");
      }
      s.print(_indent2 + "</sms:measurement>\n");
   }

   /**
    * Helper method to write an observation context
    * @param c the context
    * @param s the output stream
    */
   private static void _writeContext(Context c, PrintStream s) {
		Observation o = c.getObservation();
		if (o == null) {
			return;
		}
		s.print(_indent2 + "<sms:context");
		if (o.getLabel() != null)
			s.print(" observation=\"" + o.getLabel() + "\"");
		if (c.isIdentifying())
			s.print(" identifying=\"yes\"");
		s.print(">\n");
		Relationship r = c.getRelationship();
		if (r != null && r.getOntology() != null)
			s.print(_indent3 + "<sms:relationship id=\""
					+ r.getOntology().getPrefix() + ":" + r.getName()
					+ "\"/>\n");
		s.print(_indent2 + "</sms:context>\n");
	}
   /* the indent levels */

   private static String _indent1 = "   ";
   private static String _indent2 = _indent1 + _indent1;
   private static String _indent3 = _indent1 + _indent2;
   private static String _indent4 = _indent1 + _indent3;
} 