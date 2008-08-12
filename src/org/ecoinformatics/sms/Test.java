
package org.ecoinformatics.sms;

import org.ecoinformatics.sms.ontology.*;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.OntologyItem;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.URLConnection;

public class Test {

   public static void main(String[] args) {
      try {
         // get the args
         String uri1 = args[0];
         String uri2 = args[1];

         
         SMS sms = new SMS();


         // get a ontology manager
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
         
         // get a test annotation
         String metacat = "http://linus.nceas.ucsb.edu/sms/metacat/";
         String annot1 = metacat + "annot.5.1";
         URL url = new URL(annot1); 
         URLConnection connection = url.openConnection();
         
         // get annotation manager
         AnnotationManager annotationManager = sms.getAnnotationManager();
         annotationManager.importAnnotation(connection.getInputStream(), "annot1");

         // build up simple query
         List<String> entities = new ArrayList();
         entities.add(metacat + "ont.7.1#SpatialLocation");
         entities.add(metacat + "ont.7.1#Plant");
         entities.add(metacat + "ont.7.1#CentralSubplot");
         entities.add(metacat + "ont.7.1#FooBar");
         List<String> chars = new ArrayList();
         chars.add(metacat + "ont.7.1#DryWeight");
         List<String> stds = new ArrayList();
         stds.add(metacat + "ont.7.1#GramsPerSquareMeter");

         // get query results         
         for(Annotation a : annotationManager.getMatchingAnnotations(entities, chars, stds))
            System.out.println("match: " + a.getURI());
         
      }catch(Exception e) {
         System.out.println("Error: " + e.getMessage());
         e.printStackTrace();
      }
   }

}
