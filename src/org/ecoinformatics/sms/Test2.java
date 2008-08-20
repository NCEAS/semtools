
package org.ecoinformatics.sms;

import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
//import org.ecoinformatics.sms.annotation.Annotation;
//import java.util.List;
//import java.util.ArrayList;
//import java.net.URL;
//import java.net.URLConnection;
public class Test2 {

   public static void main(String[] args) {
      try {
         String uri = "http://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-gce.owl";

         SMS sms = new SMS();

         // get a ontology manager
         OntologyManager ontMgr = sms.getOntologyManager();
         ontMgr.importOntology(uri);
         Ontology ont = ontMgr.getOntology(uri);

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
