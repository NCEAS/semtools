package org.ecoinformatics.oboe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Context;

public class ContextInstance {
	private ObservationInstance observationInstance;
	private ObservationInstance contextObservationInstance;
	private Context contextType;
	
	private OboeModel oboe;
	private Annotation a;
	
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
	
	public void toPrintStream(PrintStream p)
		throws Exception
	{
		if(contextType==null){
			throw new Exception("contextType is NULL.");
		}
			
		p.println(observationInstance.getObsId() + "," + contextObservationInstance.getObsId() +", " + contextType.getRelationship().getName());		
	}
	
	public void fromPrintStream(BufferedReader in) throws IOException{
		String line = in.readLine(); 
		String[] strArray = line.split(",");
		long oid1 = Long.parseLong(strArray[0]);
		observationInstance = oboe.GetObservationInstance(oid1);
		
		long oid2 = Long.parseLong(strArray[1]);
		contextObservationInstance = oboe.GetObservationInstance(oid2);
		
		String contextTypeRelationshipName = strArray[2];
		contextType = observationInstance.getObsType().getContext(contextTypeRelationshipName);
		
		oboe.AddContextInstance(this);
	}
	
}
