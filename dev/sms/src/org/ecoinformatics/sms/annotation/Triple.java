package org.ecoinformatics.sms.annotation;

import java.util.List;

import org.ecoinformatics.sms.ontology.OntologyClass;

/**
 * Utility class for storing OntologyClass triples
 * Used for expressing generic Context relationships like:
 * <Entity> <Relationship> <Entity> (Plant isWithin Plot)
 * @author leinfelder
 *
 */
public class Triple {
	
	public OntologyClass a, b, c;
	
	public Triple() {}

	public Triple(OntologyClass a, OntologyClass b, OntologyClass c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public boolean hasMatch(List<Triple> triples) {
		for (Triple t: triples) {
			boolean aMatch = false;
			boolean bMatch = false;
			boolean cMatch = false;
			
			// check each item
			aMatch = (t.a == null || t.a.equals(a));
			bMatch = (t.b == null || t.b.equals(b));
			cMatch = (t.c == null || t.c.equals(c));
			// a hit?
			if (aMatch && bMatch && cMatch) {
				return true;
			}
		}
		return false;
	}

}
