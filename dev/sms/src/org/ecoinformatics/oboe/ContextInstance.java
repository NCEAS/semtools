package org.ecoinformatics.oboe;

import org.ecoinformatics.sms.annotation.Context;

public class ContextInstance {
	private ObservationInstance observationInstance;
	private ObservationInstance contextObservationInstance;
	private Context contextType;
	
	
	public ObservationInstance getObservationInstance() {
		return observationInstance;
	}
	public void setObservationInstance(ObservationInstance observationInstance) {
		this.observationInstance = observationInstance;
	}
	public ObservationInstance getContextObservationInstance() {
		return contextObservationInstance;
	}
	public void setContextObservationInstance(
			ObservationInstance contextObservationInstance) {
		this.contextObservationInstance = contextObservationInstance;
	}
	public Context getContextType() {
		return contextType;
	}
	public void setContextType(Context contextType) {
		this.contextType = contextType;
	}

	
}
