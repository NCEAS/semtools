package org.ecoinformatics.sms.annotation;

import java.util.Comparator;

public class ObservationComparator implements Comparator<Observation> {

	public int compare(Observation o1, Observation o2) {
		if (o1 == null && o2 == null) {
			return 0;
		}
		if (o1.containsObservation(o2)) {
			return 1;
		}
		else if (o2.containsObservation(o1)) {
			return -1;
		}
		// default w/o context consideration
		return o1.compareTo(o2);
	}
	
}
