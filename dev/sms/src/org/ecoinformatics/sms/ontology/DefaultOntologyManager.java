/**
 *    '$RCSfile: DefaultOntologyManager.java,v $'
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
package org.ecoinformatics.sms.ontology;

import org.ecoinformatics.sms.OntologyManager;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntClass;
import org.mindswap.pellet.jena.PelletReasonerFactory;

/**
 * An ontology catalog maintains a "knowledge base" of OWL
 * ontologies. This implementation uses the Pellet application for
 * OWL-DL reasoning.  An ontology is denoted by a uri, which is also
 * assumed to be the location of the assocated OWL file.
 *
 * @author Shawn Bowers
 */
public class DefaultOntologyManager implements OntologyManager {

    // map from ontologies to jena ont models
    private Map<Ontology, OntModel> _models = new HashMap();

    /**
     * Constructor
     */
    public DefaultOntologyManager() {
        // do nothing
    }

    /**
     * Adds an OWL ontology to the manager. The uri is assumed also to be the 
     * namespace of the ontology.
     * @param uri the uri and location of the OWL file ontology
     */
    public void importOntology(String uri) throws Exception {
        // make sure the ontology isn't already loaded
        if(isOntology(uri))
            return;
        // load via pellet and jena
        OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
        model.read(uri);
        // add to the 
        _models.put(new Ontology(uri), model);
    }
    
    /**
     * Adds an OWL ontology to the manager. 
     * This particular implementation does not support URL != URI use
     * @param url the url and location of the OWL file ontology
     * @param uri the uri and location of the OWL file ontology
     */
    public void importOntology(String url, String uri) throws Exception {
    	this.importOntology(url);
    }

    /**
     * Removes the ontology associated with the uri from the manager
     * @param uri the uri
     */
    public void removeOntology(String uri) {
        // TODO
    }

    /**
     * Checks if the uri denotes an ontology in the catalog
     * @return true if the uri is an ontology in the catalog
     */
    public boolean isOntology(String uri) {
        return getOntology(uri) != null;
    }

    /**
     * Returns the ontology associated with the uri
     * @return the ontology
     */
    public Ontology getOntology(String uri) {
        for(Ontology ont : _models.keySet())
            if(ont.getURI().equals(uri))
                return ont;
        return null;
    }
    
    /**
     * Get the label for the ontology, if one exists
     * @param ont the ontology
     * @return the label
     */
    public String getOntologyLabel(Ontology ont) {
       OntModel m = getModel(ont);
       if(m == null)
          return null;
       if(m.getOntology(ont.getURI()) == null)
          return "null (getOntology failed)";
       return m.getOntology(ont.getURI()).getLabel(null);
    }

    /** 
     * Returns the ontologies being managed by the catalog.
     * @return the ontologies
     */
    public List<String> getOntologyIds() {
        List<String> results = new ArrayList();
        for(Ontology ont : _models.keySet())
            results.add(ont.getURI());
        return results;
    }

    /**
     * Returns the class with the given name in the ontology
     * @param o the ontology
     * @param name the class name
     * @return the class (if it exists)
     */
    public OntologyClass getNamedClass(Ontology o, String name) {
        OntModel model = getModel(o);
        if(model == null)
            return null;
        OntClass oc = model.getOntClass(o.getURI() + "#" + name);
        if(oc == null)
            return null;
        return new OntologyClass(o, name);
    }

    /**
     * Get the first label of a named class, if one exists
     * @param c the class
     * @return the label
     */
    public String getNamedClassLabel(OntologyClass c) {
       OntModel model = getModel(c.getOntology());
       if(model == null)
          return null;
       OntClass oc = model.getOntClass(c.getURI());
       if(oc == null)
          return null;
       return oc.getLabel(null);
    }

   /**
     * Get the label of a named class, if one exists
     * @param c the class
     * @return the label
     */
    public List<String> getNamedClassLabels(OntologyClass c) {
       List<String> result = new ArrayList();
       OntModel model = getModel(c.getOntology());
       if(model == null)
          return result;
       OntClass oc = model.getOntClass(c.getURI());
       if(oc == null)
          return null;
       for(RDFNode label : (List<RDFNode>)oc.listLabels(null).toList()) 
          if(!result.contains(label))
             result.add(label.toString());
       return result;
    }

    
    /**
     * Returns the named classes for ontologies in the catalog
     * catalog
     * @returns the named ontology classes
     */
    public List<OntologyClass> getNamedClasses() {
        List<OntologyClass> results = new ArrayList();
        for(OntModel model : getModels())
            for(OntClass c : (List<OntClass>) model.listClasses().toList()) {
                String uri = c.getNameSpace();
                if(uri != null && uri.endsWith("#"))
                    uri = uri.substring(0, uri.length() - 1);
                Ontology ont = getOntology(uri);
                OntologyClass oc = new OntologyClass(ont, c.getLocalName());
                if(ont != null && !results.contains(oc))
                    results.add(oc);
            }
        return results;
    }
    
    /**
     * Returns the named classes for given ontology
     * catalog
     * @returns the named ontology classes
     */
    public List<OntologyClass> getNamedClasses(Ontology ont) {
        List<OntologyClass> results = new ArrayList();
        OntModel model = getModel(ont);
            for(OntClass c : (List<OntClass>) model.listClasses().toList()) {
                String uri = c.getNameSpace();
                if(uri != null && uri.endsWith("#"))
                    uri = uri.substring(0, uri.length() - 1);
                OntologyClass oc = new OntologyClass(ont, c.getLocalName());
                if(ont != null && !results.contains(oc))
                    results.add(oc);
            }
        return results;
    }

    /**
     * Returns the set of named subclasses of the given ontology class.
     * @param c the superclass to search for
     * @return the subclasses
     */
    public List<OntologyClass> getNamedSubclasses(OntologyClass c) {
        List<OntologyClass> results = new ArrayList();
        if(c == null)
            return results;
        for(String uri : getOntologyIds()) {
            Ontology ont = getOntology(uri);
            if(ont == null)
                continue;
            OntModel model = getModel(ont);
            if(model == null)
                continue;
            OntClass oc = model.getOntClass(c.toString());
            if(oc == null)
                continue;
            // change 'false' below to 'true' to get direct subclasses only
            for(Resource r : (List<OntClass>) oc.listSubClasses(false).toList()) {
                String r_uri = r.getNameSpace();
                if(r_uri != null && r_uri.endsWith("#"))
                    r_uri = r_uri.substring(0, r_uri.length() - 1);
                Ontology subont = getOntology(r_uri);
                OntologyClass subc = new OntologyClass(subont, r.getLocalName());
                if(subont != null && !results.contains(subc))
                    results.add(subc);
            }
        }
        return results;
    }

    /**
     * Returns the set of named subclasses of the given class within the given
     * given ontology
     * @param c the super class 
     * @param o the ontology to search
     * @return the subclasses within the given ontology
     */
    public List<OntologyClass> getNamedSubclasses(OntologyClass c, Ontology o) {
        List<OntologyClass> results = new ArrayList();
        if(c == null)
            return results;
        OntModel model = getModel(o);
        if(model == null)
            return results;
        OntClass oc = model.getOntClass(c.toString());
        if(oc == null)
            return results;
        // change 'false' below to 'true' to get direct subclasses only
        for(Resource r : (List<OntClass>) oc.listSubClasses(false).toList()) {
            String r_uri = r.getNameSpace();
            if(r_uri != null && r_uri.endsWith("#"))
                r_uri = r_uri.substring(0, r_uri.length() - 1);
            if(!o.getURI().equals(r_uri))
                continue;
            OntologyClass subc = new OntologyClass(o, r.getLocalName());
            if(!results.contains(subc))
                results.add(subc);
        }
        return results;
    }

    /**
     * Returns true if the first class is a subclass of the second
     * @param sub the subclass
     * @param sup the superclass
     * @returns result of subclass check
     */
    public boolean isSubClass(OntologyClass sub, OntologyClass sup) {
        // TODO
        return false;
    }

    /**
     * Returns true if classes are equivalent
     * @param c1 the first class
     * @param c2 the second class
     * @returns result of equivalence check
     */
    public boolean isEquivalentClass(OntologyClass c1, OntologyClass c2) {
        // TODO
        return false;
    }

    /**
     * Get the superclasses of the given ontology class whose ontology
     * is managed by this manager
     * @param c the subclass
     * @returns the named classes
     */
    public List<OntologyClass> getNamedSuperclasses(OntologyClass c) {
        List<OntologyClass> results = new ArrayList();
        if(c == null)
            return results;
        for(String uri : getOntologyIds()) {
            Ontology ont = getOntology(uri);
            if(ont == null)
                continue;
            OntModel model = getModel(ont);
            if(model == null)
                continue;
            OntClass oc = model.getOntClass(c.toString());
            if(oc == null)
                continue;
            // change 'false' below to 'true' to get direct subclasses only
            for(Resource r : (List<OntClass>) oc.listSuperClasses(false).toList()) {
                String r_uri = r.getNameSpace();
                if(r_uri != null && r_uri.endsWith("#"))
                    r_uri = r_uri.substring(0, r_uri.length() - 1);
                Ontology supont = getOntology(r_uri);
                OntologyClass supc = new OntologyClass(supont, r.getLocalName());
                if(supont != null && !results.contains(supc))
                    results.add(supc);
            }
        }
        return results;
    }

    /**
     * Get the superclases of the given ontology class that are also within 
     * the given ontology
     * @param c the subclass 
     * @param o the ontology to search
     * @return the superclasses within the given ontology
     */
    public List<OntologyClass> getNamedSuperclasses(OntologyClass c, Ontology o) {
        List<OntologyClass> results = new ArrayList();
        if(c == null)
            return results;
        OntModel model = getModel(o);
        if(model == null)
            return results;
        OntClass oc = model.getOntClass(c.toString());
        if(oc == null)
            return results;
        // change 'false' below to 'true' to get direct subclasses only
        for(Resource r : (List<OntClass>) oc.listSuperClasses(false).toList()) {
            String r_uri = r.getNameSpace();
            if(r_uri != null && r_uri.endsWith("#"))
                r_uri = r_uri.substring(0, r_uri.length() - 1);
            if(!o.getURI().equals(r_uri))
                continue;
            OntologyClass supc = new OntologyClass(o, r.getLocalName());
            if(!results.contains(supc))
                results.add(supc);
        }
        return results;
    }

    /**
     * Returns all named properties defined in the catalog
     * @returns the properties
     */
    public List<OntologyProperty> getNamedProperties() {
        // TODO
        return null;
    }

    /**
     * Gets the named subproperties of the given property whose
     * ontology is managed by this manager
     * @returns the named properties
     */
    public List<OntologyProperty> getNamedSubproperties(OntologyProperty p) {
        // TODO
        return null;
    }

    /**
     * Gets the named superproperties of the given property whose
     * ontology is managed by this manager
     * @returns the named properties
     */
    public List<OntologyProperty> getNamedSuperproperties(OntologyProperty p) {
        // TODO
        return null;
    }

    /**
     * Returns true if the given class is the domain of the property.
     * @param p the property
     * @param c the class
     */
    public boolean hasDomain(OntologyProperty p, OntologyClass c) {
        // TODO
        return false;
    }

    /**
     * Returns true if the given class is a range of the property.
     * @param p the property
     * @param c the class
     */
    public boolean hasRange(OntologyProperty p, OntologyClass c) {
        // TODO
        return false;
    }

    /**
     * Returns the domain classes of the property.
     * @param p the property
     * @return the domain classes
     */
    public List<OntologyClass> getDomain(OntologyProperty p) {
        // TODO
        return null;
    }

    /**
     * Returns the range classes of the property.
     * @param p the property
     * @return the domain classes
     */
    public List<OntologyClass> getRange(OntologyProperty p) {
        // TODO
        return null;
    }

    
    /** 
     * Get the Jena OntModels being managed
     * @return the ontology models
     */
    private List<OntModel> getModels() {
        List<OntModel> results = new ArrayList();
        for(Ontology ont : _models.keySet()) {
            OntModel m = _models.get(ont);
            if(m != null)
                results.add(m);
        }
        return results;
    }

    /**
     * Return the Jena OntModel for the given ontology
     * @param o the ontology
     * @return the ontology model
     */
    private OntModel getModel(Ontology o) {
       Ontology onto = getOntology(o.getURI());
       return _models.get(onto);
    }
}
