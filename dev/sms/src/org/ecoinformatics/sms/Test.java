
package org.ecoinformatics.sms;

import org.ecoinformatics.sms.*;
import org.ecoinformatics.sms.ontology.*;
import java.util.List;

public class Test {

   public static void main(String[] args) {
      try {
         // get the args
         String uri1 = args[0];
         String uri2 = args[1];

         // get a ontology manager
         SMS sms = new SMS();
         OntologyManager ontologyManager = sms.getOntologyManager();
         ontologyManager.importOntology(uri1);
         ontologyManager.importOntology(uri2);
         Ontology ont1 = ontologyManager.getOntology(uri1);
         Ontology ont2 = ontologyManager.getOntology(uri2);

         // print the loaded ontologies
         System.out.println("loaded ontologies: ");
         System.out.println(ontologyManager.getOntologyIds().toString());

         // print the loaded classes
         System.out.println("loaded classes: ");
         List<OntologyClass> classes = ontologyManager.getNamedClasses();
         System.out.println(classes);

         // print all subclasses
         for(OntologyClass c : classes)
            System.out.println("loaded subclasses of '" + c + "': " +
               ontologyManager.getNamedSubclasses(c));

         // print subclasses of oboe:Entity in ont2
         System.out.println("subclasses of oboe:Entity:");
         OntologyClass ent = ontologyManager.getNamedClass(ont1, "Entity");
         if(ent == null) {
            System.out.println("couldn't find oboe:Entity!");
            return;
         }
         for(OntologyClass c : ontologyManager.getNamedSubclasses(ent, ont2)) {
            System.out.println("   " + c.toString());
         }
      }catch(Exception e) {
         System.out.println("Error: " + e.getMessage());
         e.printStackTrace();
      }
   }

}
