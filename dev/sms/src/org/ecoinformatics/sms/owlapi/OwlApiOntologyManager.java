/**
 * 
 */
package org.ecoinformatics.sms.owlapi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ecoinformatics.sms.OntologyManager;
import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.ontology.OntologyProperty;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * @author leinfelder
 *
 */
public class OwlApiOntologyManager implements OntologyManager {
	
	public static Log log = LogFactory.getLog(OwlApiOntologyManager.class);
	
	private static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getDomain(org.ecoinformatics.sms.ontology.OntologyProperty)
	 */
	public List<OntologyClass> getDomain(OntologyProperty p) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedClass(org.ecoinformatics.sms.ontology.Ontology, java.lang.String)
	 */
	public OntologyClass getNamedClass(Ontology o, String name) {
			
		OntologyClass ontologyClass = new OntologyClass(o, name);
		OWLClass owlClass = this.getOWLClass(ontologyClass);
		if (owlClass == null) {
			ontologyClass = null;
		}

		return ontologyClass;
		
	}
	
	/**
	 * Utility method for retrieving OWL API classes
	 * @param o
	 * @param name
	 * @return
	 */
	private OWLClass getOWLClass(OntologyClass ontologyClass) {
		String classURI = ontologyClass.getURI();
		try {
			OWLOntology ontology = manager.getOntology(IRI.create(ontologyClass.getOntology().getURI()));
			Iterator<OWLClass> classIter = ontology.getClassesInSignature().iterator();
			while (classIter.hasNext()) {
				OWLClass owlClass = classIter.next();
				String owlClassURI = owlClass.getIRI().toURI().toString();
				if (owlClassURI.equals(classURI)) {
					return owlClass;
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.error("No class found for: " + ontologyClass.getURI());
		return null;
			
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedClassLabel(org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public String getNamedClassLabel(OntologyClass c) {
		List<String> labels =getNamedClassLabels(c);
		StringBuffer sb = new StringBuffer();
		for (String label : labels) {
			sb.append(label);
		}

		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedClassLabels(org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public List<String> getNamedClassLabels(OntologyClass c) {
		OWLClass owlClass = this.getOWLClass(c);
		OWLOntology ontology = null;
		try {
			ontology = manager.getOntology(IRI.create(c.getOntology().getURI()));
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> labels = new ArrayList<String>();
		for (OWLAnnotation annotation : owlClass.getAnnotations(ontology)) {
			labels.add(annotation.getValue().toString());
		}

		return labels;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedClasses()
	 */
	public List<OntologyClass> getNamedClasses() {
		List<OntologyClass> allClasses = new ArrayList<OntologyClass>();
		try {
			Set<OWLOntology> ontologies = manager.getOntologies();
			Iterator<OWLOntology> ontologyIter = ontologies.iterator();
			while (ontologyIter.hasNext()) {
				OWLOntology ontology = ontologyIter.next();
				Ontology o = new Ontology(ontology.getOntologyID().getOntologyIRI().toString());
				Iterator<OWLClass> classIter = ontology.getClassesInSignature().iterator();
				while (classIter.hasNext()) {
					OWLClass owlClass = classIter.next();
					OntologyClass ontologyClass = new OntologyClass(owlClass.getIRI().toString());
					if (!allClasses.contains(ontologyClass)) {
						allClasses.add(ontologyClass);
					}
						
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allClasses;
	}
	
	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedClasses()
	 */
	public List<OntologyClass> getNamedClasses(Ontology o) {
		List<OntologyClass> allClasses = new ArrayList<OntologyClass>();
		try {
			
			OWLOntology ontology = manager.getOntology(IRI.create(o.getURI()));
			Iterator<OWLClass> classIter = ontology.getClassesInSignature().iterator();
			while (classIter.hasNext()) {
				OWLClass owlClass = classIter.next();
				OntologyClass ontologyClass = new OntologyClass(owlClass.getIRI().toString());
				if (!allClasses.contains(ontologyClass)) {
					allClasses.add(ontologyClass);
				}		
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allClasses;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedProperties()
	 */
	public List<OntologyProperty> getNamedProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedSubclasses(org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public List<OntologyClass> getNamedSubclasses(OntologyClass c) {
		List<OntologyClass> classes = new ArrayList<OntologyClass>();
		
		OWLClass owlClass = getOWLClass(c);
		if (owlClass != null) {
		
			// iterate over all ontologies
			Set<OWLOntology> ontologies = manager.getOntologies();
			Iterator<OWLOntology> ontologyIter = ontologies.iterator();
			while (ontologyIter.hasNext()) {
				OWLOntology ontology = ontologyIter.next();
				Ontology o = new Ontology(ontology.getOntologyID().getOntologyIRI().toString());
				
				// iterate over the subclasses of the class in this ontology
				Set<OWLClassExpression> subClasses = owlClass.getSubClasses(ontology);
				Iterator<OWLClassExpression> subClassIter = subClasses.iterator();
				while (subClassIter.hasNext()) {
					OWLClassExpression subclass = subClassIter.next();
					
					
					// add the subclass when appropriate
					if (!subclass.isAnonymous()) {
						OntologyClass ontologyClass = null;
						try {
							ontologyClass = new OntologyClass(subclass.asOWLClass().getIRI().toString());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// include in the return list
						classes.add(ontologyClass);
					}
				}
			}
		}
		
		return classes;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedSubclasses(org.ecoinformatics.sms.ontology.OntologyClass, org.ecoinformatics.sms.ontology.Ontology)
	 */
	public List<OntologyClass> getNamedSubclasses(OntologyClass c, Ontology o) {
		List<OntologyClass> classes = new ArrayList<OntologyClass>();

		OWLClass owlClass = getOWLClass(c);
		if (owlClass != null) {
			OWLOntology ontology = null;
			try {
				ontology = manager.getOntology(IRI.create(o.getURI()));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
					
			// iterate over the subclasses of the class for the given ontology
			Set<OWLClassExpression> subClasses = owlClass.getSubClasses(ontology);
			Iterator<OWLClassExpression> subClassIter = subClasses.iterator();
			while (subClassIter.hasNext()) {
				OWLClassExpression subclass = subClassIter.next();
				
				
				// add the subclass when appropriate
				if (!subclass.isAnonymous()) {
					OntologyClass ontologyClass = null;
					try {
						ontologyClass = new OntologyClass(subclass.asOWLClass().getIRI().toString());
						// include in the return list
						classes.add(ontologyClass);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		return classes;
	}
	
	public List<OntologyClass> getNamedClassesForPropertyRestriction(OntologyProperty p, OntologyClass c) {
		List<OntologyClass> classes = new ArrayList<OntologyClass>();

		OWLClass restrictedClass = getOWLClass(c);
		OWLOntology ontology = manager.getOntology(IRI.create(c.getOntology().getURI()));
        OWLObjectProperty property = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(p.getURI()));
        
		RestrictionVisitor visitor = new RestrictionVisitor(restrictedClass, ontology);
		
		Set<OWLClass> owlClasses = visitor.getRestrictedProperties().get(property);
		if (owlClasses != null) {
			for (OWLClass owlClass: owlClasses) {
				OntologyClass ontologyClass = null;
				try {
					ontologyClass = new OntologyClass(owlClass.getIRI().toString());
					// include in the return list
					classes.add(ontologyClass);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return classes;
	}
	

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedSubproperties(org.ecoinformatics.sms.ontology.OntologyProperty)
	 */
	public List<OntologyProperty> getNamedSubproperties(OntologyProperty p) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedSuperclasses(org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public List<OntologyClass> getNamedSuperclasses(OntologyClass c) {
		List<OntologyClass> classes = new ArrayList<OntologyClass>();
		
		//get the class
		OWLClass owlClass = getOWLClass(c);
		if (owlClass != null) {
			// iterate over all ontologies
			Set<OWLOntology> ontologies = manager.getOntologies();
			Iterator<OWLOntology> ontologyIter = ontologies.iterator();
			while (ontologyIter.hasNext()) {
				OWLOntology ontology = ontologyIter.next();
				
				// iterate over the superclasses of the class in this ontology
				Set<OWLClassExpression> superClasses = owlClass.getSuperClasses(ontology);
				Iterator<OWLClassExpression> superClassIter = superClasses.iterator();
				while (superClassIter.hasNext()) {
					OWLClassExpression superclassDesc = superClassIter.next();
					
					// add the superclass when appropriate
					if (!superclassDesc.isAnonymous()) {
						OWLClass superclass = superclassDesc.asOWLClass();
						OntologyClass ontologyClass = null;
						try {
							ontologyClass = new OntologyClass(superclass.getIRI().toString());
							// include in the return list
							classes.add(ontologyClass);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		return classes;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedSuperclasses(org.ecoinformatics.sms.ontology.OntologyClass, org.ecoinformatics.sms.ontology.Ontology)
	 */
	public List<OntologyClass> getNamedSuperclasses(OntologyClass c, Ontology o) {
		List<OntologyClass> classes = new ArrayList<OntologyClass>();
		
		OWLClass owlClass = getOWLClass(c);
		if (owlClass != null) {
			OWLOntology ontology;
			try {
				ontology = manager.getOntology(IRI.create(o.getURI()));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			// iterate over the superclasses of the class
			Set<OWLClassExpression> superClasses = owlClass.getSuperClasses(ontology);
			Iterator<OWLClassExpression> superClassIter = superClasses.iterator();
			while (superClassIter.hasNext()) {
				OWLClassExpression superclass = superClassIter.next();
				
				// add the superclass when appropriate
				if (!superclass.isAnonymous()) {
					OntologyClass ontologyClass = null;
					try {
						ontologyClass = new OntologyClass(superclass.asOWLClass().getIRI().toString());
						// include in the return list
						classes.add(ontologyClass);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		return classes;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedSuperproperties(org.ecoinformatics.sms.ontology.OntologyProperty)
	 */
	public List<OntologyProperty> getNamedSuperproperties(OntologyProperty p) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getOntology(java.lang.String)
	 */
	public Ontology getOntology(String uri) {
		OWLOntology owlOnt = null;
		Ontology ont = new Ontology();

		try {
			owlOnt = manager.getOntology(IRI.create(uri));
			ont.setURI(owlOnt.getOntologyID().getOntologyIRI().toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ont;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getOntologyIds()
	 */
	public List<String> getOntologyIds() {
		List<String> ids = new ArrayList<String>();
		
		Set<OWLOntology> ontologies = manager.getOntologies();
		Iterator<OWLOntology> ontologyIter = ontologies.iterator();
		while (ontologyIter.hasNext()) {
			OWLOntology ontology = ontologyIter.next();
			ids.add(ontology.getOntologyID().getOntologyIRI().toString());
		}
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getOntologyLabel(org.ecoinformatics.sms.ontology.Ontology)
	 */
	public String getOntologyLabel(Ontology ont) {
		OWLOntology owlOnt = null;
		try {
			owlOnt = manager.getOntology(IRI.create(ont.getURI()));
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuffer labels = new StringBuffer();
		for (OWLAnnotation annotation : owlOnt.getAnnotations()) {
			OWLAnnotationValue value = annotation.getValue();
			labels.append(value.toString());
		}

		return labels.toString();
		
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getRange(org.ecoinformatics.sms.ontology.OntologyProperty)
	 */
	public List<OntologyClass> getRange(OntologyProperty p) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#hasDomain(org.ecoinformatics.sms.ontology.OntologyProperty, org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public boolean hasDomain(OntologyProperty p, OntologyClass c) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#hasRange(org.ecoinformatics.sms.ontology.OntologyProperty, org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public boolean hasRange(OntologyProperty p, OntologyClass c) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#importOntology(java.lang.String)
	 */
	public void importOntology(String uri) throws Exception {
		// load from physical URI - this is an assumption for OntologyManager
        manager.loadOntologyFromOntologyDocument(IRI.create(uri));
		//manager.loadOntology(new URI(uri));

	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#importOntology(java.lang.String, java.lang.String)
	 */
	public void importOntology(String url, String uri) throws Exception {
		if (uri != null) {
            OWLOntologyIRIMapper mapper = new SimpleIRIMapper(new URI(uri), IRI.create(url));
            manager.addIRIMapper(mapper);
            manager.loadOntology(IRI.create(uri));
		} else {
            manager.loadOntologyFromOntologyDocument(IRI.create(url));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#isEquivalentClass(org.ecoinformatics.sms.ontology.OntologyClass, org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public boolean isEquivalentClass(OntologyClass c1, OntologyClass c2) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#isOntology(java.lang.String)
	 */
	public boolean isOntology(String uri) {
		// TODO Auto-generated method stub
		try {
			return manager.contains(IRI.create(uri));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#isSubClass(org.ecoinformatics.sms.ontology.OntologyClass, org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public boolean isSubClass(OntologyClass sub, OntologyClass sup) {
		List<OntologyClass> subclasses = this.getNamedSubclasses(sup);
		return subclasses.contains(sub);
		
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#removeOntology(java.lang.String)
	 */
	public void removeOntology(String uri) {
		try {
			manager.removeOntology(manager.getOntology(IRI.create(uri)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			OntologyManager man = new OwlApiOntologyManager();
			if (args.length > 1) {
				man.importOntology(args[0], args[1]);
			} else {
				man.importOntology(args[0]);
			}
			List<OntologyClass> allClasses = man.getNamedClasses();
			for (OntologyClass oc: allClasses) {
				log.warn(oc);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		

	}

}
