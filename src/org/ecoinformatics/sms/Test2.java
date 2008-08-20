
package org.ecoinformatics.sms;

import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.annotation.Annotation;
//import java.util.List;
//import java.util.ArrayList;
import java.net.URL;
import java.net.URLConnection;

public class Test2 {

   public static void main(String[] args) {
      try {
         String uri = "http://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-gce.owl";

         SMS sms = new SMS();

         // get the ontology manager
         OntologyManager ontMgr = sms.getOntologyManager();
         ontMgr.importOntology(uri);

         // print the loaded ontologies and their labels
         System.out.println("\n*** Loaded ontologies: ***");
         for(String ontId : ontMgr.getOntologyIds()) {
            Ontology o = ontMgr.getOntology(ontId);
            String lbl = ontMgr.getOntologyLabel(o);
            System.out.println("   '" + lbl + "' (" + ontId + ") ");
         }

         // print the loaded classes and their labels
         System.out.println("\n*** Loaded classes: ***");
         for(OntologyClass c : ontMgr.getNamedClasses()) {
            String lbl = ontMgr.getNamedClassLabel(c);
            System.out.println("   '" + lbl + "' (" + c + ")");
         }

         // print the subclasses of loaded classes
         System.out.println("\n*** Subclasses: ***");
         for(OntologyClass c : ontMgr.getNamedClasses()) {
            System.out.println("   " + c.getURI() + ":");
            for(OntologyClass s : ontMgr.getNamedSubclasses(c))
               System.out.println("      " + s.getURI());
         }

         uri = "http://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/plt-gced-0409-1-1-annot.xml";
         URL url = new URL(uri);
         URLConnection connection = url.openConnection();

         // get annotation manager
         AnnotationManager annMgr = sms.getAnnotationManager();
         annMgr.importAnnotation(connection.getInputStream(), "annot1");

         // print the loaded annotations 
         System.out.println("\n*** Loaded annotations: ***");
         for(Annotation a : annMgr.getAnnotations()) {
            System.out.println("   '" + a.getURI() + "'");
            a.write(System.out);
         }

         System.out.println("\n*** Annotation classes: ***");
         for(Annotation a : annMgr.getAnnotations()) {
            System.out.println("   '" + a.getURI() + "'");
            for(OntologyClass c : annMgr.getOntologyClasses(a, false, false))
               System.out.println("      " + c.getURI());
         }

         
         
      // print the loaded classes, and their labels

      // print all subclasses
      //System.out.println("\n*** Subclasses: ***");
      //for(OntologyClass c : ontologyManager.getNamedClasses()) {
      //    System.out.println("   subclasses of '" + c + "': ");
      //    for(OntologyClass s : ontologyManager.getNamedSubclasses(c))
      //        System.out.println("      " + s);
      //}



      }catch(Exception e) {
         System.out.println("Error: " + e.getMessage());
         e.printStackTrace();
      }
   }

}
