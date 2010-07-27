/**
 * 
 */
package org.ecoinformatics.sms.owlapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import org.ecoinformatics.sms.OntologyManager;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.ontology.OntologyProperty;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author leinfelder
 *
 */
public class ReasoningOntologyManager extends OwlApiOntologyManager {
	
	private Map<IRI, PelletReasoner> reasoners = new HashMap<IRI, PelletReasoner>();

	public List<OntologyClass> getNamedClassesForPropertyRestriction(List<OntologyProperty> properties, OntologyClass c) {
		List<OntologyClass> classes = new ArrayList<OntologyClass>();

		OWLClass restrictedClass = getOWLClass(c);
		
		OntologyProperty baseProperty = properties.remove(0);
		OWLObjectProperty property = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(baseProperty.getURI()));
        //OWLObjectInverseOf inverseOfProperty = manager.getOWLDataFactory().getOWLObjectInverseOf(property);
        //OWLObjectSomeValuesFrom someCombined = manager.getOWLDataFactory().getOWLObjectSomeValuesFrom(inverseOfProperty, restrictedClass);
        OWLObjectSomeValuesFrom someCombined = manager.getOWLDataFactory().getOWLObjectSomeValuesFrom(property, restrictedClass);

        // add the nested property chain parts
		for (OntologyProperty p: properties) {
			OWLObjectProperty remainingProperty = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(p.getURI()));
	        //OWLObjectInverseOf inverseOfRemainingProperty = manager.getOWLDataFactory().getOWLObjectInverseOf(remainingProperty);
	        //someCombined = manager.getOWLDataFactory().getOWLObjectSomeValuesFrom(inverseOfRemainingProperty, someCombined);
	        someCombined = manager.getOWLDataFactory().getOWLObjectSomeValuesFrom(remainingProperty, someCombined);
		}

		PelletReasoner reasoner = reasoners.get(IRI.create(c.getOntology().getURI()));
        NodeSet<OWLClass> esc = reasoner.getSuperClasses(someCombined, true);
        Set<OWLClass> owlClasses = esc.getFlattened();			
//        Node<OWLClass> esc = reasoner.getEquivalentClasses(someCombined);
//        Set<OWLClass> owlClasses = esc.getEntities();

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
	 * @see org.ecoinformatics.sms.OntologyManager#importOntology(java.lang.String)
	 */
	public void importOntology(String uri) throws Exception {
		super.importOntology(uri);
		initReasoner(IRI.create(uri));

	}

	/* (non-Javadoc)
	 * @see org.ecoinformatics.sms.OntologyManager#importOntology(java.lang.String, java.lang.String)
	 */
	public void importOntology(String url, String uri) throws Exception {
		super.importOntology(url, uri);
		initReasoner(IRI.create(uri));	
	}
	
	private void initReasoner(IRI ontologyIRI) {
		
		PelletReasoner reasoner = reasoners.get(ontologyIRI);
		if (reasoner == null || !reasoner.getManager().contains(ontologyIRI)) {
			OWLOntology ontology = manager.getOntology(ontologyIRI);
			ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
            OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
            //create a new reasoner, and initialize it in a thread
	        final PelletReasoner newReasoner = PelletReasonerFactory.getInstance().createReasoner(ontology, config);
	        // run the reasoning in a new thread - can take a while
	        Executors.newSingleThreadExecutor().execute(new Runnable() {
				public void run() {
					newReasoner.prepareReasoner();
				}
	        });
	        reasoners.put(ontologyIRI, newReasoner);
        }
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			OntologyManager man = new ReasoningOntologyManager();
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
