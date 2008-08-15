package org.ecoinformatics.sms;

import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.annotation.Annotation;
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
            System.out.println("\nLoaded ontologies: ");
            for(String ontId : ontologyManager.getOntologyIds())
                System.out.println("   " + ontId);

            // print the loaded classes
            System.out.println("\nLoaded classes: ");
            for(OntologyClass c : ontologyManager.getNamedClasses())
                System.out.println("   " + c);


            // print all subclasses
            System.out.println("\nSubclasses: ");
            for(OntologyClass c : ontologyManager.getNamedClasses()) {
                System.out.println("   subclasses of '" + c + "': ");
                for(OntologyClass s : ontologyManager.getNamedSubclasses(c))
                    System.out.println("      " + s);
            }

            // print subclasses of oboe:Entity in ont2
            System.out.println("\nSubclasses of oboe.Entity:");
            OntologyClass ent = ontologyManager.getNamedClass(ont1, "Entity");
            if(ent == null) {
                System.out.println("couldn't find oboe.Entity!");
                return;
            }
            for(OntologyClass c : ontologyManager.getNamedSubclasses(ent, ont2))
                System.out.println("   " + c.toString());

            // get a test annotation
            String metacat = "http://linus.nceas.ucsb.edu/sms/metacat/";
            String annot1 = metacat + "annot.5.1";
            URL url = new URL(annot1);
            URLConnection connection = url.openConnection();

            // get annotation manager
            AnnotationManager annotationManager = sms.getAnnotationManager();
            annotationManager.importAnnotation(connection.getInputStream(), "annot1");

            // build up simple query
            Ontology gceOnt = new Ontology(metacat + "ont.7.1");
            List<OntologyClass> entities = new ArrayList();
            entities.add(new OntologyClass(gceOnt, "SpatialLocation"));
            entities.add(new OntologyClass(gceOnt, "Plant"));
            entities.add(new OntologyClass(gceOnt, "CentralSubplot"));
            entities.add(new OntologyClass(gceOnt, "FooBar"));
            List<OntologyClass> chars = new ArrayList();
            chars.add(new OntologyClass(gceOnt, "DryWeight"));
            List<OntologyClass> stds = new ArrayList();
            stds.add(new OntologyClass(gceOnt, "GramsPerSquareMeter"));

            // get query results         
            System.out.println("\nSearch for matching annotation:");
            for(Annotation a : annotationManager.getMatchingAnnotations(entities, chars, stds))
                System.out.println("   match: " + a.getURI());

            System.out.println("\nActive Entities: ");
            // get the active entity domain
            List<OntologyClass> entityActiveDom = new ArrayList();
            for(OntologyClass c : annotationManager.getActiveEntities()) {
                if(!entityActiveDom.contains(c))
                    entityActiveDom.add(c);
                for(OntologyClass s : ontologyManager.getNamedSuperclasses(c))
                    if(!entityActiveDom.contains(s))
                        entityActiveDom.add(s);
            }
            for(OntologyClass c : entityActiveDom)
                System.out.println("   " + c);

            System.out.println("\nActive Characteristics: ");
            // get the active entity domain
            List<OntologyClass> charActiveDom = new ArrayList();
            for(OntologyClass c : annotationManager.getActiveCharacteristics()) {
                if(!charActiveDom.contains(c))
                    charActiveDom.add(c);
                for(OntologyClass s : ontologyManager.getNamedSuperclasses(c))
                    if(!charActiveDom.contains(s))
                        charActiveDom.add(s);
            }
            for(OntologyClass c : charActiveDom)
                System.out.println("   " + c);

            System.out.println("\nActive Standards: ");
            // get the active entity domain
            List<OntologyClass> stdActiveDom = new ArrayList();
            for(OntologyClass c : annotationManager.getActiveStandards()) {
                if(!stdActiveDom.contains(c))
                    stdActiveDom.add(c);
                for(OntologyClass s : ontologyManager.getNamedSuperclasses(c))
                    if(!stdActiveDom.contains(s))
                        stdActiveDom.add(s);
            }
            for(OntologyClass c : stdActiveDom)
                System.out.println("   " + c);

            // build up a simple active domain query
            chars = new ArrayList();
            OntologyClass biomass = new OntologyClass(gceOnt, "Biomass");
            chars.add(biomass);
            for(OntologyClass c : ontologyManager.getNamedSubclasses(biomass))
                if(!chars.contains(c))
                    chars.add(c);
            // get the results
            System.out.println("\nSearch for active entity domains of 'Biomass':");
            entityActiveDom = new ArrayList();
            for(OntologyClass c : annotationManager.getActiveEntities(chars, null)) {
                if(!entityActiveDom.contains(c))
                    entityActiveDom.add(c);
                for(OntologyClass s : ontologyManager.getNamedSuperclasses(c))
                    if(!entityActiveDom.contains(s))
                        entityActiveDom.add(s);
            }
            for(OntologyClass c : entityActiveDom)
                System.out.println("    " + c);

        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
