package org.ecoinformatics.oboe.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Relationship;

public class ContextInstance{
	private ObservationInstance m_observationInstance;
	private ObservationInstance m_contextObservationInstance;
	private Context m_contextType;
	
	private OboeModel m_oboe;
	private Annotation m_annotation;
	
	public ContextInstance(ObservationInstance _observationInstance,
			Context _contextType,
			ObservationInstance _contextObservationInstance)
	{
		setObservationInstance(_observationInstance);
		m_contextType = new Context(_contextType.getObservation(),_contextType.getRelationship(),_contextType.isIdentifying());
		setContextObservationInstance(_contextObservationInstance);
	}
	
	public ObservationInstance getObservationInstance() {
		return m_observationInstance;
	}
	public void setObservationInstance(ObservationInstance _observationInstance) {
		m_observationInstance = _observationInstance;
	}
	public ObservationInstance getContextObservationInstance() {
		return m_contextObservationInstance;
	}
	public void setContextObservationInstance(ObservationInstance _contextObservationInstance) {
		m_contextObservationInstance = _contextObservationInstance;
	}
	
	public OboeModel getOboe() {
		return m_oboe;
	}
	public void setOboe(OboeModel oboe) {
		m_oboe = oboe;
	}
	public Annotation getA() {
		return m_annotation;
	}
	public void setA(Annotation annotation) {
		m_annotation = annotation;
	}
	
	public Context getContextType() {
		return m_contextType;
	}
	public void setContextType(Context _contextType) {
		m_contextType = _contextType;
	}

	public String toString()
	{
		String str = "[";
		str += "oi"+m_observationInstance.getObsId();
		str += ("->context_oi" +m_contextObservationInstance.getObsId());
		str += "(context_type ";
		if(m_contextType==null||(m_contextType.getRelationship()==null)){
			str += "null";
		}else{
			str +=m_contextType.getRelationship().getName();
		}
		str +=")]";
		return str;
	}
	
	public void toPrintStream(PrintStream p)
		throws Exception
	{
		if(m_contextType==null){
			throw new Exception("m_contextType is NULL.");
		}
			
		p.println(m_observationInstance.getObsId() + "," + m_contextObservationInstance.getObsId() +"," + m_contextType.getRelationship().getName());		
	}
	
	public void fromPrintStream(BufferedReader in) throws Exception
	{
		String line = in.readLine(); 
		String[] strArray = line.split(",");
		long oid1 = Long.parseLong(strArray[0]);
		m_observationInstance = m_oboe.GetObservationInstance(oid1);
		
		long oid2 = Long.parseLong(strArray[1]);
		m_contextObservationInstance = m_oboe.GetObservationInstance(oid2);
		
		String contextTypeRelationshipName = strArray[2];
		m_contextType = m_observationInstance.getObsType().getContext(contextTypeRelationshipName);
		
		m_oboe.AddContextInstance(this);
	}

	/**
	 * Check whether this context instance is the same to another given one or not
	 *  
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public boolean isSame(ContextInstance o) throws Exception {
		boolean cmp1 = ((m_observationInstance.getObsId()-o.getObservationInstance().getObsId())==0)?true:false;
		if(!cmp1) return (cmp1);
		
		boolean cmp2 = ((m_contextObservationInstance.getObsId()-o.getContextObservationInstance().getObsId())==0)?true:false;
		if(!cmp2) return cmp2;
		
		boolean cmp3 = true; //for both context types are true
		if(m_contextType==null&&o.getContextType()==null){
			cmp3 = true;
		}if((m_contextType!=null&&o.getContextType()==null)||(m_contextType==null&&o.getContextType()!=null)){
			cmp3 = false;
		}else{//(m_contextType!=null&&o.getContextType()!=null)
			cmp3 = m_contextType.isSame(o.getContextType());
		}
		
		return (cmp3);
	}
	
}
