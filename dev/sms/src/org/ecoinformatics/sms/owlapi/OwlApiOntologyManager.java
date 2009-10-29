/**
 * 
 */
package org.ecoinformatics.sms.owlapi;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ecoinformatics.sms.OntologyManager;
import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.ontology.OntologyObjectProperty;
import org.ecoinformatics.sms.ontology.OntologyProperty;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
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
		String className = o.getURI() + "#" + name;
		OntologyClass ontologyClass = new OntologyClass();
		try {
			OWLOntology ontology = manager.getOntology(new URI(o.getURI()));
			Iterator<OWLClass> classIter = ontology.getReferencedClasses().iterator();
			while (classIter.hasNext()) {
				OWLClass owlClass = classIter.next();
				String owlClassName = owlClass.getURI().toString();
				if (owlClassName.equals(className)) {
					ontologyClass.setName(name);
					ontologyClass.setOntology(o);
					return ontologyClass;
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ontologyClass;
		
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedClassLabel(org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public String getNamedClassLabel(OntologyClass c) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getNamedClassLabels(org.ecoinformatics.sms.ontology.OntologyClass)
	 */
	public List<String> getNamedClassLabels(OntologyClass c) {
		// TODO Auto-generated method stub
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
					
					OntologyClass ontologyClass = new OntologyClass();
					String name = owlClass.getURI().toString();
					name = name.substring(name.indexOf("#") + 1);
					ontologyClass.setName(name);
					ontologyClass.setOntology(o);
					
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
		
		// iterate over all ontologies
		Set<OWLOntology> ontologies = manager.getOntologies();
		Iterator<OWLOntology> ontologyIter = ontologies.iterator();
		while (ontologyIter.hasNext()) {
			OWLOntology ontology = ontologyIter.next();
			Ontology o = new Ontology(ontology.getURI().toString());
			
			// iterate over classes in the ontology
			ontology.getReferencedClasses();
			Iterator<OWLClass> classIter = ontology.getReferencedClasses().iterator();
			while (classIter.hasNext()) {
				OWLClass owlClass = classIter.next();
				
				// match the class 
				if (owlClass.getURI().toString().equals(c.getURI())) {
					
					// iterate over the subclasses of the class
					Set<OWLDescription> subClasses = owlClass.getSubClasses(ontology);
					Iterator<OWLDescription> subClassIter = subClasses.iterator();
					while (subClassIter.hasNext()) {
						OWLDescription subclass = subClassIter.next();
						OntologyClass ontologyClass = new OntologyClass();
						
						// add the subclass when appropriate
						if (!subclass.isAnonymous()) {
							String name = subclass.asOWLClass().getURI().toString();
							name = name.substring(name.indexOf("#") + 1);
							ontologyClass.setName(name);
							ontologyClass.setOntology(o);
							// include in the return list
							classes.add(ontologyClass);
						}
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

		OWLOntology ontology = null;
		try {
			ontology = manager.getOntology(new URI(o.getURI()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		// iterate over classes in the ontology
		ontology.getReferencedClasses();
		Iterator<OWLClass> classIter = ontology.getReferencedClasses().iterator();
		while (classIter.hasNext()) {
			OWLClass owlClass = classIter.next();
			
			// match the class 
			if (owlClass.getURI().toString().equals(c.getURI())) {
				
				// iterate over the subclasses of the class
				Set<OWLDescription> subClasses = owlClass.getSubClasses(ontology);
				Iterator<OWLDescription> subClassIter = subClasses.iterator();
				while (subClassIter.hasNext()) {
					OWLDescription subclass = subClassIter.next();
					OntologyClass ontologyClass = new OntologyClass();
					
					// add the subclass when appropriate
					if (!subclass.isAnonymous()) {
						String name = subclass.asOWLClass().getURI().toString();
						name = name.substring(name.indexOf("#") + 1);
						ontologyClass.setName(name);
						ontologyClass.setOntology(o);
						// include in the return list
						classes.add(ontologyClass);
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
		
		// iterate over all ontologies
		Set<OWLOntology> ontologies = manager.getOntologies();
		Iterator<OWLOntology> ontologyIter = ontologies.iterator();
		while (ontologyIter.hasNext()) {
			OWLOntology ontology = ontologyIter.next();
			Ontology o = new Ontology(ontology.getURI().toString());
			
			// iterate over classes in the ontology
			ontology.getReferencedClasses();
			Iterator<OWLClass> classIter = ontology.getReferencedClasses().iterator();
			while (classIter.hasNext()) {
				OWLClass owlClass = classIter.next();
				
				// match the class 
				if (owlClass.getURI().toString().equals(c.getURI())) {
					
					// iterate over the superclasses of the class
					Set<OWLDescription> superClasses = owlClass.getSuperClasses(ontology);
					Iterator<OWLDescription> superClassIter = superClasses.iterator();
					while (superClassIter.hasNext()) {
						OWLDescription superclassDesc = superClassIter.next();
						OntologyClass ontologyClass = new OntologyClass();
						
						// add the superclass when appropriate
						if (!superclassDesc.isAnonymous()) {
							OWLClass superclass = superclassDesc.asOWLClass();
							String name = superclass.getURI().toString();
							name = name.substring(name.indexOf("#") + 1);
							ontologyClass.setName(name);
							ontologyClass.setOntology(o);
							// include in the return list
							classes.add(ontologyClass);
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
		
		OWLOntology ontology;
		try {
			ontology = manager.getOntology(new URI(o.getURI()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
			
		// iterate over classes in the ontology
		ontology.getReferencedClasses();
		Iterator<OWLClass> classIter = ontology.getReferencedClasses().iterator();
		while (classIter.hasNext()) {
			OWLClass owlClass = classIter.next();
			
			// match the class 
			if (owlClass.getURI().toString().equals(c.getURI())) {
				
				// iterate over the superclasses of the class
				Set<OWLDescription> superClasses = owlClass.getSuperClasses(ontology);
				Iterator<OWLDescription> superClassIter = superClasses.iterator();
				while (superClassIter.hasNext()) {
					OWLDescription superclass = superClassIter.next();
					OntologyClass ontologyClass = new OntologyClass();
					
					// add the superclass when appropriate
					if (!superclass.isAnonymous()) {
						String name = superclass.asOWLClass().getURI().toString();
						name = name.substring(name.indexOf("#") + 1);
						ontologyClass.setName(name);
						ontologyClass.setOntology(o);
						// include in the return list
						classes.add(ontologyClass);
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
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#getRange(org.ecoinformatics.sms.ontology.OntologyObjectProperty)
	 */
	public List<OntologyClass> getRange(OntologyObjectProperty p) {
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
		// TODO Auto-generated method stub

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
