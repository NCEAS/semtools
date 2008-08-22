
package org.ecoinformatics.sms;

import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.annotation.Annotation;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.URLConnection;

public class Test2 {

   public static void main(String[] args) {
      try {
         String ontUri = "http://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-gce.owl";
         
         SMS sms = new SMS();

         // get the ontology manager
         OntologyManager ontMgr = sms.getOntologyManager();
         ontMgr.importOntology(ontUri);

         ontMgr.importOntology("http://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe.owl");
         ontMgr.importOntology("http://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-unit.owl");
         ontMgr.importOntology("http://linus.nceas.ucsb.edu/sms/metacat/oboeunit.1.1");

         
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

         String annUri = "http://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/plt-gced-0409-1-1-annot.xml";
         URL url = new URL(annUri);
         URLConnection connection = url.openConnection();

         AnnotationManager annMgr = sms.getAnnotationManager();
         annMgr.importAnnotation(connection.getInputStream(), "annot1");

         // print the loaded annotations 
         System.out.println("\n*** Loaded annotations: ***");
         for(Annotation a : annMgr.getAnnotations())
            System.out.println("   '" + a.getURI() + "'");

         System.out.println("\n*** Annotation classes: ***");
         for(Annotation a : annMgr.getAnnotations()) {
            System.out.println("   '" + a.getURI() + "'");
            for(OntologyClass c : annMgr.getOntologyClasses(a, false, false))
               System.out.println("      " + c.getURI());
         }

         // search for a matching annotation
         Ontology gceOnt = ontMgr.getOntology(ontUri);
         OntologyClass entity = ontMgr.getNamedClass(gceOnt, "JuncusRoemerianus");
         OntologyClass characteristic = ontMgr.getNamedClass(gceOnt, "Biomass");

         System.out.println("\nEntity: " + entity);
         System.out.println("\nCharacteristic: " + characteristic);

         // get query results         
         System.out.println("\n*** Search for matching annotation: ***");
         for(Annotation a : annMgr.getMatchingAnnotations(null, characteristic, null, true))
            System.out.println("   match: " + a.getURI());

         System.out.println("\n*** Search for active characteristic entities: ***");
         for(OntologyClass c : annMgr.getActiveCharacteristics())
            System.out.println("   entity: " + ontMgr.getNamedClassLabel(c));
         
         System.out.println("\n*** Search for active domain of '" + ontMgr.getNamedClassLabel(characteristic) + "': ***");
         for(OntologyClass c : annMgr.getActiveStandards(null, characteristic, true, true))
            System.out.println("   standard: " + ontMgr.getNamedClassLabel(c));
         

      }catch(Exception e) {
         System.out.println("Error: " + e.getMessage());
         e.printStackTrace();
      }
   }

}
