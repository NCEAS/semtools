package org.ecoinformatics.sms.owlapi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

/**
 * Visits existential restrictions and collects the properties which are
 * restricted
 */
public class RestrictionVisitor extends
		OWLClassExpressionVisitorAdapter {

	private OWLOntology ontology;

	private boolean processInherited = true;

	private Set<OWLClass> processedClasses;

	private Map<OWLObjectPropertyExpression, Set<OWLClass>> restrictedProperties;

	public RestrictionVisitor(OWLClass restrictedClass, OWLOntology ontology) {
		this.ontology = ontology;
		processedClasses = new HashSet<OWLClass>();
		restrictedProperties = new HashMap<OWLObjectPropertyExpression, Set<OWLClass>>();
		processRestrictions(restrictedClass);
	}
	
	public void reset() {
		processedClasses.clear();
		restrictedProperties.clear();
	}

	public void setProcessInherited(boolean processInherited) {
		this.processInherited = processInherited;
	}

	public Map<OWLObjectPropertyExpression, Set<OWLClass>> getRestrictedProperties() {
		return restrictedProperties;
	}

	public void visit(OWLClass desc) {
		if (processInherited && !processedClasses.contains(desc)) {
			// If we are processing inherited restrictions then
			// we recursively visit named supers. Note that we
			// need to keep track of the classes that we have processed
			// so that we don't get caught out by cycles in the taxonomy
			processedClasses.add(desc);
		
			for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(desc)) {
				ax.getSuperClass().accept(this);
			}
		}
	}
	
	// recursively handle intersections
	public void visit(OWLObjectIntersectionOf intersection) {
		Set<OWLClassExpression> classExpressions = intersection.asConjunctSet();
		for (OWLClassExpression exp: classExpressions) {
			exp.accept(this);
		}
	}
	
	// recursively handle unions
	public void visit(OWLObjectUnionOf union) {
		Set<OWLClassExpression> classExpressions = union.asDisjunctSet();
		for (OWLClassExpression exp: classExpressions) {
			exp.accept(this);
		}
	}

	public void visit(OWLObjectAllValuesFrom desc) {
		// This method gets called when a class expression is an
		// existential (allValuesFrom) restriction and it asks us to visit it
		OWLObjectPropertyExpression property = desc.getProperty();
		OWLClassExpression filler = desc.getFiller();
		Set<OWLClass> classes = filler.getClassesInSignature();
		addProperty(property, classes);
		// recurse
		filler.accept(this);

	}

	public void visit(OWLObjectSomeValuesFrom desc) {
		// This method gets called when a class expression is an
		// existential (someValuesFrom) restriction and it asks us to visit it
		OWLObjectPropertyExpression property = desc.getProperty();
		OWLClassExpression filler = desc.getFiller();
		Set<OWLClass> classes = filler.getClassesInSignature();
		addProperty(property, classes);
		// recurse
		filler.accept(this);
	}
	
	private void addProperty( OWLObjectPropertyExpression property, Set<OWLClass> classes) {
		Set<OWLClass> existingClasses = restrictedProperties.get(property);
		if (existingClasses != null) {
			classes.addAll(existingClasses);
		}
		restrictedProperties.put(property, classes);
	}
	
	private void processRestrictions(OWLClass restrictedClass) {
				
        // process equiv classes
        for (OWLEquivalentClassesAxiom ax : ontology.getEquivalentClassesAxioms(restrictedClass)) {
        	for (OWLClassExpression cls: ax.getClassExpressions()) {
	        	System.out.println("Equiv class: " + cls);	
	            // Ask our class to accept a visit from the RestrictionVisitor - if it is an
	            // existential restriction then our restriction visitor will answer it - if not our
	            // visitor will ignore it
	        	cls.accept(this);
        	}
        }
        
        // In this case, restrictions are used as (anonymous) superclasses, so to get the restrictions on
        // the class we need to obtain the subclass axioms for the restricted class
        for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(restrictedClass)) {
        	OWLClassExpression cls = ax.getSuperClass();
        	System.out.println("[Inferred] Superclass: " + cls);	
            // Ask our superclass to accept a visit from the RestrictionVisitor - if it is an
            // existential restriction then our restriction visitor will answer it - if not our
            // visitor will ignore it
        	cls.accept(this);
        }
        
        if (false) {
	        // go up the chain since there seem to be problems doing this automatically
	        for (OWLClassExpression superClassEx: restrictedClass.getSuperClasses(ontology)) {
	        	if (superClassEx.isAnonymous()) {
	        		continue;
	        	}
	        	OWLClass superCls = superClassEx.asOWLClass();
	        	processRestrictions(superCls);
	        }
        }
        
        // Our RestrictionVisitor has now collected all of the properties that have been restricted in existential
        // restrictions - print them out.
        System.out.println("Restricted properties for " + restrictedClass + ": " + this.getRestrictedProperties().size());
        for (OWLObjectPropertyExpression prop : this.getRestrictedProperties().keySet()) {
            System.out.println("    " + prop);
        }
    }

}