package org.ecoinformatics.sms.owlapi.example;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.net.URI;
import java.util.Set;
/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 11-Jan-2007<br><br>
 */
public class URIMapping {

    public static void main(String[] args) {
        try {
            // A simple example of how to load and save an ontology
            // We first need to obtain a copy of an OWLOntologyManager, which, as the
            // name suggests, manages a set of ontologies.  An ontology is unique within
            // an ontology manager.  To load multiple copies of an ontology, multiple managers
            // would have to be used.
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            
            // We have to have a physical URI - it may be the same as the logical
            // Or we can have a physical that maps to a logical
            // We may not "know" the logical URI until we load the ontology from the physical URI
			IRI physicalURI = IRI.create(args[0]);
			IRI logicalURI = null;
			if (args.length > 1) {
				logicalURI = IRI.create(args[1]);
			}
			
			// Now ask the manager to load the ontology based on what we have
			if (logicalURI != null) {
	            SimpleIRIMapper mapper = new SimpleIRIMapper(logicalURI, physicalURI);
	            manager.addIRIMapper(mapper);
	            manager.loadOntology(logicalURI);
			} else {
	            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(physicalURI);
	            logicalURI = ontology.getOntologyID().getOntologyIRI();
			}
            
			// For debugging
            Set<OWLOntology> ontologies = manager.getOntologies();
            
            // Look it up using the logical URI from this point on
            OWLOntology ontology = manager.getOntology(logicalURI);
            
            System.out.println("Ontology: " + ontology.getOntologyID().getOntologyIRI());
            System.out.println("Loaded from: " + manager.getOntologyDocumentIRI(ontology));
            System.out.println("--------------------------------");
            
            // Print out all of the classes which are referenced in the ontology
            for(OWLClass cls : ontology.getClassesInSignature()) {
                printClasses(cls, ontology, 0);
            }
            
            // Now save a copy to another location in OWL/XML format (i.e. disregard the
            // format that the ontology was loaded in).
            // (To save the file on windows use a URL such as  "file:/C:\\windows\\temp\\MyOnt.owl")
            IRI physicalURI2 = IRI.create("file:/tmp/MyOnt2.owl");
            manager.saveOntology(ontology, new OWLXMLOntologyFormat(), physicalURI2);
            // Remove the ontology from the manager
            manager.removeOntology(ontology);
        }
        catch (OWLOntologyCreationException e) {
            System.out.println("The ontology could not be created: " + e.getMessage());
        }
        catch (OWLOntologyStorageException e) {
            System.out.println("The ontology could not be saved: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void printClasses(OWLClass cls, OWLOntology ontology, int level) {
    	if (level == 0) {
    		System.out.println(cls);
    	}
        for(OWLClassExpression cls2 : cls.getSubClasses(ontology)) {
        	for (int i = 0; i < level; i++) {
        		System.out.print("\t");
        	}
            System.out.println(cls2);
            if (!cls.isAnonymous()) {
            	printClasses(cls2.asOWLClass(), ontology, level+1);
            }
        }
    }
}