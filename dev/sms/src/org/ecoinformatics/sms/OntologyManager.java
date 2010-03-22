/**
 *    '$RCSfile: OntologyManager.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2007/05/21 20:25:10 $'
 *   '$Revision: 1.1 $'
 *
 *  For Details: http://daks.ucdavis.edu
 *
 * Copyright (c) 2005 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package org.ecoinformatics.sms;

import java.util.List;
import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.ontology.OntologyProperty;
import org.ecoinformatics.sms.ontology.OntologyObjectProperty;

/**
 * An ontology manager maintains a "knowledge base" of OWL ontologies.
 * @author Shawn Bowers
 */
public interface OntologyManager {

    /**
     * Adds an OWL ontology to the manager. The uri is assumed also to be the 
     * namespace of the ontology.
     * @param uri the uri and location of the OWL file ontology
     */
    public void importOntology(String uri) throws Exception;
    
    /**
     * Adds an OWL ontology to the manager. 
     * 
     * The URL is required for loading the ontology, 
     * The URI may or may not be same as the URL and is optional.
     * This allows ontologies to be loaded from different sources but to have the same
     * consistent URI
     * @param url the url (physical location) of the OWL file ontology
     * @param uri the uri (optional) of the ontology
     */
    public void importOntology(String url, String uri) throws Exception;

    /**
     * Removes the ontology from the manager
     * @param uri the location of the OWL ontology file
     */
    public void removeOntology(String uri);

    /**
     * Checks if the uri denotes an ontology in the catalog
     * @return true if the uri is an ontology in the catalog
     */
    public boolean isOntology(String uri);

    /**
     * Returns the ontology associated with the uri
     * @return the ontology
     */
    public Ontology getOntology(String uri);

   /**
     * Get the label for the ontology, if one exists
     * @param ont the ontology
     * @return the label
     */
    public String getOntologyLabel(Ontology ont);

   /**
     * Get the label of a named class, if one exists
     * @param c the class
     * @return the label
     */
    public List<String> getNamedClassLabels(OntologyClass c);
    
    /** 
     * Returns the uri's of ontologies currently managed by this manager
     * @return the ontology uri's
     */
    public List<String> getOntologyIds();

    /**
     * Returns the class with the given name in the ontology
     * @param o the ontology
     * @param name the class name
     * @return the class (if it exists)
     */
    public OntologyClass getNamedClass(Ontology o, String name);

   /**
     * Get the label of a named class, if one exists
     * @param c the class
     * @return the label
     */
    public String getNamedClassLabel(OntologyClass c);

    
    /**
     * Gets the named classes for ontologies being managed by this manager
     * @returns the named classes
     */
    public List<OntologyClass> getNamedClasses();
    
    /**
     * Gets the named classes for given ontology
     * @returns the named classes for the given ontology
     */
    public List<OntologyClass> getNamedClasses(Ontology ontology);

    /**
     * Gets classes for the given property restriction on the given class
     * @param p restrictions for this property
     * @param c the class that has the restrictions
     * @return list of classes for the restriction
     */
	public List<OntologyClass> getNamedClassesForPropertyRestriction(OntologyProperty p, OntologyClass c);
	
    /**
     * Returns the set of named subclasses of the given ontology class.
     * @param c the superclass to search for
     * @return the subclasses
     */
    public List<OntologyClass> getNamedSubclasses(OntologyClass c, boolean deep);

    /**
     * Returns the set of named subclasses of the given class within the given
     * given ontology
     * @param o the ontology to search
     * @param c the super class 
     * @return the subclasses within the given ontology
     */
    public List<OntologyClass> getNamedSubclasses(OntologyClass c, Ontology o);

    /**
     * Returns true if the first class is a subclass of the second
     * @param sub the subclass
     * @param sup the superclass
     * @returns result of subclass check
     */
    public boolean isSubClass(OntologyClass sub, OntologyClass sup, boolean deep);

    /**
     * Returns true if classes are equivalent
     * @param c1 the first class
     * @param c2 the second class
     * @returns result of equivalence check
     */
    public boolean isEquivalentClass(OntologyClass c1, OntologyClass c2);

    /**
     * Gets the superclasses of the given ontology class whose ontology
     * is managed by this manager
     * @param o the ontology
     * @returns the named classes
     */
    public List<OntologyClass> getNamedSuperclasses(OntologyClass c);

    /**
     * Get the superclases of the given ontology class that are also within 
     * the given ontology
     * @param c the subclass 
     * @param o the ontology to search
     * @return the superclasses within the given ontology
     */
    public List<OntologyClass> getNamedSuperclasses(OntologyClass c, Ontology o);

    /**
     * Gets the named properties for ontologies being managed by this
     * manager
     * @returns the named properties
     */
    public List<OntologyProperty> getNamedProperties();

    /**
     * Gets the named subproperties of the given property whose
     * ontology is managed by this manager
     * @returns the named properties
     */
    public List<OntologyProperty> getNamedSubproperties(OntologyProperty p);

    /**
     * Gets the named superproperties of the given property whose
     * ontology is managed by this manager
     * @returns the named properties
     */
    public List<OntologyProperty> getNamedSuperproperties(OntologyProperty p);

    /**
     * Returns true if the given class is the domain of the property.
     * @param p the property
     * @param c the class
     */
    public boolean hasDomain(OntologyProperty p, OntologyClass c);

    /**
     * Returns true if the given class is the range of the property.
     * @param p the property
     * @param c the class
     */
    public boolean hasRange(OntologyProperty p, OntologyClass c);

    /**
     * Returns the domain classes of the property.
     * @param p the property
     * @return the domain classes
     */
    public List<OntologyClass> getDomain(OntologyProperty p);

    /**
     * Returns the range classes of the property.
     * @param p the property
     * @return the domain classes
     */
    public List<OntologyClass> getRange(OntologyProperty p);
}
