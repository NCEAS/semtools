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
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.util.SimpleURIMapper;

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
			OWLOntology ontology = manager.getOntology(new URI(ontologyClass.getOntology().getURI()));
			Iterator<OWLClass> classIter = ontology.getReferencedClasses().iterator();
			while (classIter.hasNext()) {
				OWLClass owlClass = classIter.next();
				String owlClassURI = owlClass.getURI().toString();
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
		OWLClass owlClass = this.getOWLClass(c);
		OWLOntology ontology = null;
		try {
			ontology = manager.getOntology(new URI(c.getOntology().getURI()));
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer labels = new StringBuffer();
		for (OWLAnnotation annotation : owlClass.getAnnotations(ontology)) {
			labels.append(annotation.getAnnotationValue().toString());
		}

		return labels.toString();
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedClassLabels(org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public List<String> getNamedClassLabels(OntologyClass c) {
		return null;
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
				Ontology o = new Ontology(ontology.getURI().toString());
				Iterator<OWLClass> classIter = ontology.getReferencedClasses().iterator();
				while (classIter.hasNext()) {
					OWLClass owlClass = classIter.next();
					OntologyClass ontologyClass = new OntologyClass(owlClass.getURI().toString());
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
			
			OWLOntology ontology = manager.getOntology(new URI(o.getURI()));
			Iterator<OWLClass> classIter = ontology.getReferencedClasses().iterator();
			while (classIter.hasNext()) {
				OWLClass owlClass = classIter.next();
				OntologyClass ontologyClass = new OntologyClass(owlClass.getURI().toString());
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
				Ontology o = new Ontology(ontology.getURI().toString());
				
				// iterate over the subclasses of the class in this ontology
				Set<OWLDescription> subClasses = owlClass.getSubClasses(ontology);
				Iterator<OWLDescription> subClassIter = subClasses.iterator();
				while (subClassIter.hasNext()) {
					OWLDescription subclass = subClassIter.next();
					
					
					// add the subclass when appropriate
					if (!subclass.isAnonymous()) {
						OntologyClass ontologyClass = null;
						try {
							ontologyClass = new OntologyClass(subclass.asOWLClass().getURI().toString());
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
				ontology = manager.getOntology(new URI(o.getURI()));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
					
			// iterate over the subclasses of the class for the given ontology
			Set<OWLDescription> subClasses = owlClass.getSubClasses(ontology);
			Iterator<OWLDescription> subClassIter = subClasses.iterator();
			while (subClassIter.hasNext()) {
				OWLDescription subclass = subClassIter.next();
				
				
				// add the subclass when appropriate
				if (!subclass.isAnonymous()) {
					OntologyClass ontologyClass = null;
					try {
						ontologyClass = new OntologyClass(subclass.asOWLClass().getURI().toString());
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
				Set<OWLDescription> superClasses = owlClass.getSuperClasses(ontology);
				Iterator<OWLDescription> superClassIter = superClasses.iterator();
				while (superClassIter.hasNext()) {
					OWLDescription superclassDesc = superClassIter.next();
					
					// add the superclass when appropriate
					if (!superclassDesc.isAnonymous()) {
						OWLClass superclass = superclassDesc.asOWLClass();
						OntologyClass ontologyClass = null;
						try {
							ontologyClass = new OntologyClass(superclass.getURI().toString());
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
				ontology = manager.getOntology(new URI(o.getURI()));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			// iterate over the superclasses of the class
			Set<OWLDescription> superClasses = owlClass.getSuperClasses(ontology);
			Iterator<OWLDescription> superClassIter = superClasses.iterator();
			while (superClassIter.hasNext()) {
				OWLDescription superclass = superClassIter.next();
				
				// add the superclass when appropriate
				if (!superclass.isAnonymous()) {
					OntologyClass ontologyClass = null;
					try {
						ontologyClass = new OntologyClass(superclass.asOWLClass().getURI().toString());
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
			owlOnt = manager.getOntology(new URI(uri));
			ont.setURI(owlOnt.getURI().toString());
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
			ids.add(ontology.getURI().toString());
		}
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getOntologyLabel(org.ecoinformatics.sms.ontology.Ontology)
	 */
	public String getOntologyLabel(Ontology ont) {
		OWLOntology owlOnt = null;
		try {
			owlOnt = manager.getOntology(new URI(ont.getURI()));
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuffer labels = new StringBuffer();
		for (OWLOntologyAnnotationAxiom annotation : owlOnt.getAnnotations(owlOnt)) {
			labels.append(annotation.getAnnotation().toString());
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
        manager.loadOntologyFromPhysicalURI(new URI(uri));
		//manager.loadOntology(new URI(uri));

	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#importOntology(java.lang.String, java.lang.String)
	 */
	public void importOntology(String url, String uri) throws Exception {
		if (uri != null) {
            SimpleURIMapper mapper = new SimpleURIMapper(new URI(uri), new URI(url));
            manager.addURIMapper(mapper);
            manager.loadOntology(new URI(uri));
		} else {
            manager.loadOntologyFromPhysicalURI(new URI(url));
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
			return manager.contains(new URI(uri));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#isSubClass(org.ecoinformatics.sms.ontology.OntologyClass, org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public boolean isSubClass(OntologyClass sub, OntologyClass sup) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#removeOntology(java.lang.String)
	 */
	public void removeOntology(String uri) {
		try {
			manager.removeOntology(new URI(uri));
		} catch (URISyntaxException e) {
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
