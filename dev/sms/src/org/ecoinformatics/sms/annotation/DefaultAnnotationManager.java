/**
 *    '$RCSfile: DefaultAnnotationManager.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2007/05/25 16:13:14 $'
 *   '$Revision: 1.2 $'
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
package org.ecoinformatics.sms.annotation;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.AnnotationManager;
import org.ecoinformatics.sms.ontology.OntologyClass;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public class DefaultAnnotationManager implements AnnotationManager {

    private SMS _sms;
    private HashMap<String, Annotation> _annotations = new HashMap();

    /**
     * Default constuctor
     */
    public DefaultAnnotationManager(SMS sms) {
        _sms = sms;
    }

    /**
     * Import an annotation into the manager
     * @param r the semantic annotation 
     * @param id the identifier to assign to the annotation
     */
    public void importAnnotation(InputStream is, String id) throws Exception {
        if(isAnnotation(id)) {
            String msg = "annotation id '" + id + "' already exists";
            throw new Exception(msg);
        }
        _annotations.put(id, Annotation.read(is));
    }

    /**
     * Export an annotation from the manager
     * @param the identiifer of the annotation
     */
    public void exportAnnotation(String id, OutputStream os) throws Exception {
        if(!isAnnotation(id)) {
            String msg = "annotation id '" + id + "' does not exist";
            throw new Exception(msg);
        }
        getAnnotation(id).write(os);
    }

    /**
     * Update an existing annotation in the manager
     * @param r the new semantic annotation 
     * @param id the identifier of the annotation to update
     */
    public void updateAnnotation(InputStream is, String id) throws Exception {
        if(!isAnnotation(id)) {
            String msg = "annotation id '" + id + "' does not exist";
            throw new Exception(msg);
        }
        removeAnnotation(id);
        importAnnotation(is, id);
    }

    /**
     * Remove an annotation from the manager
     * @param id the identifier of the annotation
     */
    public void removeAnnotation(String id) throws Exception {
        if(!isAnnotation(id)) {
            String msg = "annotation id '" + id + "' does not exist";
            throw new Exception(msg);
        }
        _annotations.remove(id);
    }

    /**
     * Check if the identifier is assigned to an annotation in the
     * manager
     * @return true if the id is assigned to an annotation in the manager
     * @param id the annotation identifier
     */
    public boolean isAnnotation(String id) {
        return _annotations.containsKey(id);
    }

    /**
     * Get an annotation from the manager
     * @param the identiifer of the annotation
     */
    public Annotation getAnnotation(String id) throws Exception {
        if(!isAnnotation(id)) {
            String msg = "annotation id '" + id + "' does not exist";
            throw new Exception(msg);
        }
        return _annotations.get(id);
    }

    /**
     * Get the annotation identifiers from the manager
     * @return the set of annotation identifiers
     */
    public List<String> getAnnotationIds() {
        return new ArrayList(_annotations.keySet());
    }

    /**
     * Get annotations that contain an entity in the given list and a 
     * measurement with a characteristic and standard in the given lists
     * @param entities the entity class URI's to search for
     * @param characteristics the characteristic class URI's to search for
     * @param standards the measurement standard class URI's to search for
     * @return the matching annotations
     */
    public List<Annotation> getMatchingAnnotations(List<OntologyClass> entities,
            List<OntologyClass> characteristics, List<OntologyClass> standards) {
        List<Annotation> results = new ArrayList();
        for(Annotation annot : getAnnotations()) {
            boolean match = false;
            for(Observation o : annot.getObservations()) {
                if(entities != null && entities.size() != 0) {
                    if(o.getEntity() == null)
                        continue;
                    if(!entities.contains(o.getEntity()))
                        continue;
                }
                if((characteristics == null || characteristics.size() == 0) &&
                        (standards == null || standards.size() == 0)) {
                    match = true;
                    break;
                }
                for(Measurement m : o.getMeasurements()) {
                    if(standards != null && standards.size() != 0)
                        if(m.getStandard() == null)
                            continue;
                        else if(!standards.contains(m.getStandard()))
                            continue;
                    if(characteristics == null || characteristics.size() == 0) {
                        match = true;
                        break;
                    }
                    for(Characteristic c : m.getCharacteristics())
                        if(characteristics.contains(c)) {
                            match = true;
                            break;
                        }
                }
            }
            if(match)
                results.add(annot);

        }
        return results;
    }

    /**
     * Get entities used in managed annotations
     * @return list of entities
     */
    public List<OntologyClass> getActiveEntities() {
        List<OntologyClass> results = new ArrayList();
        for(Annotation a : getAnnotations())
            for(OntologyClass c : a.getOntologyClasses())
                if(c instanceof Entity && !results.contains(c))
                    results.add(c);
        return results;
    }

    /**
     * Get characteristics used in managed annotations
     * @return list of characteristics
     */
    public List<OntologyClass> getActiveCharacteristics() {
        List<OntologyClass> results = new ArrayList();
        for(Annotation a : getAnnotations())
            for(OntologyClass c : a.getOntologyClasses())
                if(c instanceof Characteristic && !results.contains(c))
                    results.add(c);
        return results;
    }

    /**
     * Get measurement standards used in managed annotations
     * @return list of measurement standards
     */
    public List<OntologyClass> getActiveStandards() {
        List<OntologyClass> results = new ArrayList();
        for(Annotation a : getAnnotations())
            for(OntologyClass c : a.getOntologyClasses())
                if(c instanceof Standard && !results.contains(c))
                    results.add(c);
        return results;
    }

    /**
     * Get entities used in managed annotations having a measurement with a 
     * characteristic and standard in the given characteristics and standards
     * @param characteristics characteristics to look for
     * @param standards
     * @return list of matching entities
     */
    public List<OntologyClass> getActiveEntities(
            List<OntologyClass> characteristics, List<OntologyClass> standards) {
        // check if both args are missing
        if((characteristics == null || characteristics.isEmpty()) &&
                (standards == null || standards.isEmpty()))
            return getActiveEntities();
        List<OntologyClass> results = new ArrayList();
System.out.println(">>> HERE");
        for(Annotation a : getAnnotations())
            for(Observation o : a.getObservations()) {
                Entity e = o.getEntity();
                if(results.contains(e))
                    continue;
                boolean found = false;
                for(Measurement m : o.getMeasurements()) {
                    if(standards != null && !standards.contains(m.getStandard()))
                        continue;
                    if(characteristics == null || characteristics.isEmpty()) {
                        found = true;
                        break;
                    }
                    for(Characteristic c : m.getCharacteristics())
                        if(characteristics.contains(c)) {
                            found = true;
                            break;
                        }
                    if(found)
                        break;
                }
                if(found)
                    results.add(e);
            }
        return results;
    }

    /**
     * Get characteristics used in managed annotations having a measurement 
     * with an entity and standard in the given entities and standards
     * @param entities entities to look for
     * @param standards standards to look for
     * @return list of matching characteristics
     */
    public List<OntologyClass> getActiveCharacteristics(
            List<OntologyClass> entities, List<OntologyClass> standards) {
        // check if both args are missing
        if((entities == null || entities.isEmpty()) &&
                (standards == null || standards.isEmpty()))
            return getActiveCharacteristics();
        List<OntologyClass> results = new ArrayList();
        for(Annotation a : getAnnotations())
            for(Observation o : a.getObservations()) {
                if(entities != null && !entities.contains(o.getEntity()))
                    continue;
                for(Measurement m : o.getMeasurements()) {
                    if(standards != null && !standards.contains(m.getStandard()))
                        continue;
                    // found a match
                    for(Characteristic c : m.getCharacteristics())
                        if(!results.contains(c))
                            results.add(c);
                }
            }
        return results;
    }

    /**
     * Get standards used in managed annotations having a measurement 
     * with an entity and characteristic in the given entities and 
     * characteristics
     * @param entities entities to look for
     * @param characteristics characteristics to look for
     * @return list of matching standards
     */
    public List<OntologyClass> getActiveStandards(
            List<OntologyClass> entities, List<OntologyClass> characteristics) {
        // check if both args are missing
        if((entities == null || entities.isEmpty()) &&
                (characteristics == null || characteristics.isEmpty()))
            return getActiveStandards();
        List<OntologyClass> results = new ArrayList();
        for(Annotation a : getAnnotations())
            for(Observation o : a.getObservations()) {
                if(entities != null && !entities.contains(o.getEntity()))
                    continue;
                for(Measurement m : o.getMeasurements()) {
                    boolean found = false;
                    if(characteristics != null) {
                        for(Characteristic c : m.getCharacteristics())
                            if(characteristics.contains(c)) {
                                found = true;
                                break;
                            }
                    } else
                        found = true;
                    Standard s = m.getStandard();
                    if(found && !results.contains(s))
                        results.add(s);
                }
            }
        return results;
    }

    /**
     * Helper method to get all annotations
     * @return annotations
     */
    private List<Annotation> getAnnotations() {
        List<Annotation> results = new ArrayList();
        for(String id : getAnnotationIds()) {
            Annotation a = null;
            try {
                a = getAnnotation(id);
                results.add(a);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }
} 