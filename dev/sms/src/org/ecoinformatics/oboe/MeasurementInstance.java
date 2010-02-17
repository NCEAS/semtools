package org.ecoinformatics.oboe;

import org.ecoinformatics.sms.annotation.Measurement;

public class MeasurementInstance {
	private long _obsId;
	private ObservationInstance observationInstance;
	private Measurement measurementType;
	
	public long getObsId() {
		return _obsId;
	}
	public void setObsId(long obsId) {
		_obsId = obsId;
	}
	public ObservationInstance getObservationInstance() {
		return observationInstance;
	}
	public void setObservationInstance(ObservationInstance observationInstance) {
		this.observationInstance = observationInstance;
	}
	public Measurement getMeasurementType() {
		return measurementType;
	}
	public void setMeasurementType(Measurement measurementType) {
		this.measurementType = measurementType;
	}
	
	
}
