package org.ecoinformatics.oboe;

import java.io.*;

import org.ecoinformatics.oboe.model.ObservationInstance;
import org.ecoinformatics.sms.annotation.*;

import org.ecoinformatics.sms.annotation.Measurement;

public class MeasurementInstance<T> implements Comparable<MeasurementInstance> {
	private static long gMeasId=0;
	private Long measId;
	private String measValue;
	private ObservationInstance observationInstance;
	private Measurement measurementType;
	
	private OboeModel oboe;
	private Annotation a;
	
	public MeasurementInstance(Measurement _measType, ObservationInstance _obsInstance, String _measValue)
	{
		setMeasId(gMeasId++);
		this.setMeasValue(_measValue);
		this.setMeasurementType(_measType);
		this.setObservationInstance(_obsInstance);
		
	}
	public String getMeasValue() {
		return measValue;
	}
	public void setMeasValue(String measValue) {
		this.measValue = measValue;
	}
	public long getMeasId() {
		return measId;
	}
	
	public void setMeasId(Long measId) {
		this.measId = measId;
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
	public OboeModel getOboe() {
		return oboe;
	}
	public void setOboe(OboeModel oboe) {
		this.oboe = oboe;
	}
	public Annotation getA() {
		return a;
	}
	public void setA(Annotation a) {
		this.a = a;
	}
	
	public int compareTo(MeasurementInstance other) {
		return (measId.compareTo(other.getMeasId()));	
	}
	
	public String toString()
	{
		String str="[mi";
		str += measId.toString();		
		str += "("+measurementType.getLabel()+","+measurementType.getCharacteristics()+")";
		str += " val="+measValue.toString();
		str += " -> oi";
		if(observationInstance!=null)
			str +=observationInstance.getObsId();
		else
			str +="("+observationInstance+")";
		str +="]";
		
		return str;
	}
	
	public void toPrintStream(PrintStream p)
	{
		p.println(measId + "," + measValue +","+observationInstance.getObsId() + ", "+measurementType.getLabel());
	}
	
	public void fromPrintStream(BufferedReader in) throws IOException{
		String line = in.readLine(); 
		String[] strArray = line.split(",");
		measId = Long.parseLong(strArray[0]);
		
		measValue = strArray[1];
		Long oiId = Long.parseLong(strArray[2]);
		observationInstance = oboe.GetObservationInstance(oiId);
		
		String measurementLabel= strArray[3];
		measurementType = a.getMeasurement(measurementLabel);
		
		oboe.AddMeasurementInstance(this);
	}
}
