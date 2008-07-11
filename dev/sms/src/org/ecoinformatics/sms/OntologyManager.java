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


import java.util.Vector;
import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.ontology.OntologyProperty;


/**
 * An ontology manager maintains a "knowledge base" of OWL ontologies.
 *
 * @author Shawn Bowers
 */

public interface OntologyManager {

    /**
     * Adds the given owl file reference (uri) to the manager.
     * @param uri the location of the OWL ontology file
     */
    public void importOntology(String uri) throws Exception;

    /**
     * Removes the ontology from the manager
     * @param uri the location of the OWL ontology file
     */
    public void removeOntology(String uri) throws Exception;

    /**
     * Checks if the uri denotes an ontology in the catalog
     * @return true if the uri is an ontology in the catalog
     */
    public boolean isOntology(String uri);

    /**
     * Returns the ontology associated with the uri
     * @return the ontology
     */
    public Ontology getOntology(String uri) throws Exception;

    /** 
     * Returns the uri's of ontologies currently managed by this manager
     * @return the ontology uri's
     */
    public Vector getOntologyIds();

    /**
     * Gets the named classes for ontologies being managed by this manager
     * @returns the named classes
     */
    public Vector getNamedClasses();

    /**
     * Gets the subclasses of the given ontology class whose ontology
     * is managed by this manager
     * @param o the ontology
     * @returns the named classes
     */
    public Vector getNamedSubclasses(OntologyClass c) throws Exception;

    /**
     * Returns true if the first class is a subclass of the second
     * @param sub the subclass
     * @param sup the superclass
     * @returns result of subclass check
     */
    public boolean isSubClass(OntologyClass sub, OntologyClass sup);

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
    public Vector getNamedSuperclasses(OntologyClass c) throws Exception;

    /**
     * Gets the named properties for ontologies being managed by this
     * manager
     * @returns the named properties
     */
    public Vector getNamedProperties();

    /**
     * Gets the named subproperties of the given property whose
     * ontology is managed by this manager
     * @returns the named properties
     */
    public Vector getNamedSubproperties(OntologyProperty p) throws Exception;

    /**
     * Gets the named superproperties of the given property whose
     * ontology is managed by this manager
     * @returns the named properties
     */
    public Vector getNamedSuperproperties(OntologyProperty p) throws Exception;

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
    public Vector getDomain(OntologyProperty p);

    /**
     * Returns the range classes of the property.
     * @param p the property
     * @return the domain classes
     */
    public Vector getRange(OntologyProperty p);

    /**
     * Runs an OWL-DL classifier over the managed ontologies. 
     */
    public void classify();

}
