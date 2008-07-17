package org.ecoinformatics.sms;

import org.ecoinformatics.sms.*;
import org.ecoinformatics.sms.ontology.*;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        try {
            String uri = args[0];
            SMS sms = new SMS();
            OntologyManager ontologyManager = sms.getOntologyManager();
            ontologyManager.importOntology(uri);
            //ontologyManager.classify();
            System.out.println("loaded ontologies: " + ontologyManager.getOntologyIds().toString());
            List<OntologyClass> classes = ontologyManager.getNamedClasses();
            System.out.println("classes: " + classes);
            for(OntologyClass c : classes)
                System.out.println("subclasses of '" + c + "': " + ontologyManager.getNamedSubclasses(c));
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
