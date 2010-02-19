package org.ecoinformatics.oboe;

import org.ecoinformatics.sms.annotation.Context;

public class ContextInstance {
	private ObservationInstance observationInstance;
	private ObservationInstance contextObservationInstance;
	private Context contextType;
	
	public ContextInstance(ObservationInstance _observationInstance,
			Context _contextType,
			ObservationInstance _contextObservationInstance)
	{
		this.setObservationInstance(_contextObservationInstance);
		this.setContextType(_contextType);
		this.setContextObservationInstance(_contextObservationInstance);
	}
	
	public ObservationInstance getObservationInstance() {
		return observationInstance;
	}
	public void setObservationInstance(ObservationInstance _observationInstance) {
		this.observationInstance = _observationInstance;
	}
	public ObservationInstance getContextObservationInstance() {
		return contextObservationInstance;
	}
	public void setContextObservationInstance(ObservationInstance _contextObservationInstance) {
		this.contextObservationInstance = _contextObservationInstance;
	}
	public Context getContextType() {
		return contextType;
	}
	public void setContextType(Context _contextType) {
		this.contextType = contextType;
	}

	public String toString()
	{
		String str = "(";
		str += observationInstance.getObsId();
		str += ", ct=";
		if(contextType==null||(contextType.getRelationship()==null)){
			str += "null";
		}else{
			contextType.getRelationship().getName();
		}
		
		str += (" coi=" +contextObservationInstance.getObsId());
		str +=")";
		return str;
	}
	
}
