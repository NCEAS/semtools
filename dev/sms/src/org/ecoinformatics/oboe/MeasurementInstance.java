package org.ecoinformatics.oboe;

import org.ecoinformatics.sms.annotation.Measurement;

public class MeasurementInstance<T> {
	private static long gMeasId=0;
	private long measId;
	private T measValue;
	private ObservationInstance observationInstance;
	private Measurement measurementType;
	
	public MeasurementInstance(Measurement _measType, ObservationInstance _obsInstance, T _measValue)
	{
		setMeasId(gMeasId++);
		this.setMeasValue(_measValue);
		this.setMeasurementType(_measType);
		this.setObservationInstance(_obsInstance);
		
	}
	public T getMeasValue() {
		return measValue;
	}
	public void setMeasValue(T measValue) {
		this.measValue = measValue;
	}
	public long getMeasId() {
		return measId;
	}
	public void setMeasId(long _measId) {
		measId = _measId;
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
