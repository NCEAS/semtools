package org.ecoinformatics.sms.owlapi.example;

import org.ecoinformatics.sms.owlapi.RestrictionVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

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
 * Date: 26-Jun-2007<br><br>
 * <p/>
 * This example shows how to examine the restrictions on a class.
 */
public class Restrictions {

    public static final String DOCUMENT_IRI = "https://code.ecoinformatics.org/code/sonet/trunk/ontologies/oboe-trait.owl";
    public static final String OBOE_IRI = "http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl"; 
    
    public static void main(String[] args) {
        try {
            // Create our manager
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        	
            // Load the ontology
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(DOCUMENT_IRI));
            System.out.println("Loaded: " + ontology.getOntologyID());

//            String className = "Measurement";
            String className = "Belowground_Trait";
            //String className = "AmmoniumConcentrationFreshwaterAutomated";
            IRI restrictedClassIRI = IRI.create(ontology.getOntologyID().getOntologyIRI() + "#" + className);
//            IRI restrictedClassIRI = IRI.create(OBOE_IRI + "#" + "Measurement");

            OWLClass restrictedClass = manager.getOWLDataFactory().getOWLClass(restrictedClassIRI);

            // look up the OBOE properties for the restrictions
            OWLObjectProperty measurementFor = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(OBOE_IRI + "#measurementFor"));
            OWLObjectProperty ofEntity = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(OBOE_IRI + "#ofEntity"));
        	OWLObjectProperty ofCharacteristic = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(OBOE_IRI + "#ofCharacteristic"));
        	OWLObjectProperty usesStandard = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(OBOE_IRI + "#usesStandard"));
        	OWLObjectProperty usesProtocol = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(OBOE_IRI + "#usesProtocol"));
            
        	 // process the restrictions
            RestrictionVisitor restrictionVisitor = new RestrictionVisitor(restrictedClass, ontology);
            
            // look up what we want from them
        	Set<OWLClass> entities = restrictionVisitor.getRestrictedProperties().get(ofEntity);
        	System.out.println("entities: " + entities);
        	
        	Set<OWLClass> characteristics = restrictionVisitor.getRestrictedProperties().get(ofCharacteristic);
        	System.out.println("characteristics: " + characteristics);
        	
        	Set<OWLClass> standards = restrictionVisitor.getRestrictedProperties().get(usesStandard);
        	System.out.println("standards: " + standards);
        	
        	Set<OWLClass> protocols = restrictionVisitor.getRestrictedProperties().get(usesProtocol);
        	System.out.println("protocols: " + protocols);
        	
            
        }
        catch (OWLOntologyCreationException e) {
            System.out.println("Could not load ontology: " + e.getMessage());
        }
    }

    

}