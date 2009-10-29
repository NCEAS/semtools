
package org.ecoinformatics.sms;

import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.annotation.Annotation;
import java.net.URL;
import java.net.URLConnection;

public class Test2 {

   public static void main(String[] args) {
      try {

//         String gceURI = "https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-gce.owl";
//         String oboeURI = "https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe.owl";
//         String unitURI = "https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-unit.owl";
         String gceURI = "http://linus.nceas.ucsb.edu/sms/metacat/oboegce.3";
         String oboeURI = "http://linus.nceas.ucsb.edu/sms/metacat/oboe.1";
         String unitURI = "http://linus.nceas.ucsb.edu/sms/metacat/oboeunit.1";

         SMS sms = new SMS(null);

         // get the ontology manager
         OntologyManager ontMgr = sms.getOntologyManager();
         ontMgr.importOntology(oboeURI);
         ontMgr.importOntology(unitURI);
         ontMgr.importOntology(gceURI);

         // print the loaded ontologies and their labels
         System.out.println("\n*** Loaded ontologies: ***");
         for(String ontId : ontMgr.getOntologyIds()) {
            Ontology o = ontMgr.getOntology(ontId);
            String lbl = ontMgr.getOntologyLabel(o);
            System.out.println("   '" + lbl + "' (" + ontId + ") ");
         }
         /*
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
          */
         // /*
         String annURI1 = "http://linus.nceas.ucsb.edu/sms/metacat/annotation.3";
         String annURI2 = "http://linus.nceas.ucsb.edu/sms/metacat/annotation.4";
         String annURI3 = "http://linus.nceas.ucsb.edu/sms/metacat/annotation.6";
         String annURI4 = "http://linus.nceas.ucsb.edu/sms/metacat/annotation.7";
         String annURI5 = "http://linus.nceas.ucsb.edu/sms/metacat/annotation.8";
         String annURI6 = "http://linus.nceas.ucsb.edu/sms/metacat/annotation.9";
         String annURI7 = "http://linus.nceas.ucsb.edu/sms/metacat/annotation.10";
         String annURI8 = "http://linus.nceas.ucsb.edu/sms/metacat/annotation.11";
         // */
         /*
         String annURI1 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/plt-gced-0409-1-1-annot.xml";
         String annURI2 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/plt-gcem-0211b-2-1-annot.xml";
         String annURI3 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/plt-gcem-0511a-1-0-annot.xml";
         String annURI4 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/plt-gcem-0511b-2-0-annot.xml";
         String annURI5 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/inv-gcem-0501a1-1-0-annot.xml";
         String annURI6 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/inv-gcem-0501a2-1-0-annot.xml";         
         String annURI7 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/inv-gcem-0511-2-0-annot.xml";
         String annURI8 = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/ins-gcem-0511-1-0-annot.xml";
          */
         AnnotationManager annMgr = sms.getAnnotationManager();
         addAnnotation(annURI1, annMgr);
         addAnnotation(annURI2, annMgr);
         addAnnotation(annURI3, annMgr);
         addAnnotation(annURI4, annMgr);
         addAnnotation(annURI5, annMgr);
         addAnnotation(annURI6, annMgr);
         addAnnotation(annURI7, annMgr);
         addAnnotation(annURI8, annMgr);
         
         // print the loaded annotations 
         System.out.println("\n*** Loaded annotations: ***");
         for(Annotation a : annMgr.getAnnotations()) {
            try {
               annMgr.validateAnnotation(a);
            }catch(Exception e) {
               e.printStackTrace();
               System.exit(-1);
            }
            System.out.println("   '" + a.getURI() + "'");
         }

      /*         
      System.out.println("\n*** Annotation classes: ***");
      for(Annotation a : annMgr.getAnnotations()) {
      System.out.println("   '" + a.getURI() + "'");
      for(OntologyClass c : annMgr.getOntologyClasses(a, false, false))
      System.out.println("      " + c.getURI());
      }
      
      
      
      // search for a matching annotation
      Ontology gceOnt = ontMgr.getOntology(gceURI);
      OntologyClass entity = ontMgr.getNamedClass(gceOnt, "JuncusRoemerianus");
      //         OntologyClass entity = ontMgr.getNamedClass(gceOnt, "Organism");
      OntologyClass characteristic = ontMgr.getNamedClass(gceOnt, "Biomass");
      
      System.out.println("\nEntity: " + entity);
      System.out.println("\nCharacteristic: " + characteristic);
      
      // get query results         
      System.out.println("\n*** Search for matching annotation: ***");
      for(Annotation a : annMgr.getMatchingAnnotations(null, characteristic, null, true))
      System.out.println("   match: " + a.getURI());
      
      System.out.println("\n*** Search for active characteristics: ***");
      for(OntologyClass c : annMgr.getActiveCharacteristics())
      System.out.println("   characteristic: " + ontMgr.getNamedClassLabel(c));
      
      System.out.println("\n*** Search for active characteristics of entity: '" +
      ontMgr.getNamedClassLabel(entity) + "' ***");
      for(OntologyClass c : annMgr.getActiveCharacteristics(entity, null, true, true))
      System.out.println("   characteristic: " + ontMgr.getNamedClassLabel(c));
      
      System.out.println("\n*** Search for active domain of entity='" + 
      ontMgr.getNamedClassLabel(entity) + "' characteristic='" + 
      ontMgr.getNamedClassLabel(characteristic) + "': ***");
      java.util.List<OntologyClass> ents = new java.util.ArrayList();
      ents.add(entity);
      java.util.List<OntologyClass> chars = new java.util.ArrayList();
      chars.add(characteristic);
      for(OntologyClass c : annMgr.getActiveStandards(ents, chars, true, true))
      System.out.println("   standard: " + ontMgr.getNamedClassLabel(c));
      
      System.out.println("\n*** Search for active domain of characteristic='" +
      ontMgr.getNamedClassLabel(characteristic) + "': ***");
      chars.add(characteristic);
      for(OntologyClass c : annMgr.getActiveEntities(chars, null, true, true))
      System.out.println("   entity: " + ontMgr.getNamedClassLabel(c));
       */
      }catch(Exception e) {
         System.out.println("Error: " + e.getMessage());
         e.printStackTrace();
      }

   }

   private static void addAnnotation(String urlStr, AnnotationManager mgr)
           throws Exception {
      URL url = new URL(urlStr);
      URLConnection connection = url.openConnection();
      mgr.importAnnotation(connection.getInputStream(), urlStr);
   }

}
