package org.ecoinformatics.oboe.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ecoinformatics.sms.annotation.*;

public class ObservationInstance implements Comparable<ObservationInstance>{
	private Long obsId;
	private Observation obsType;
	private EntityInstance entityInstance;
	private Long m_recordId;
	private Set<Long> m_compressedRecordIds;
	
	private OboeModel oboe;
	private Annotation a;
	
	public ObservationInstance(Observation _obsType, EntityInstance _entityInstance)
	{
		this.setObsId((oboe.gOldMaxObsId)++);
		this.setObsType(_obsType);
		this.setEntity(_entityInstance);
		m_compressedRecordIds = new TreeSet<Long>();
	}
	
	public long getObsId() {
		return obsId;
	}
	public void setObsId(long _obsId) {
		obsId = _obsId;
	}
	
	public Observation getObsType() {
		return obsType;
	}
	
	public void setObsType(Observation _obsType) {
		obsType = _obsType;
	}
	
	public EntityInstance getEntity() {
		return entityInstance;
	}
	public void setEntity(EntityInstance _entityInstance) {
		this.entityInstance = _entityInstance;
	}
	
	public Long getRecordId() {
		return m_recordId;
	}

	public void setRecordId(Long mRecordId) {
		m_recordId = mRecordId;
	}
	
	public Set<Long> getCompressedRecordIds() {
		return m_compressedRecordIds;
	}

	public void setCompressedRecordIds(Set<Long> mCompressedRecordIds) {
		m_compressedRecordIds = mCompressedRecordIds;
	}
	
	public void addRecordId(long rid){
		m_compressedRecordIds.add(rid);
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
	
	public String toString()
	{
		String str="[oi";
		str += obsId.toString();
		str += "->ei";
		if(entityInstance!=null)
			str +=entityInstance.getEntId()+"("+entityInstance.getEntityType().getName()+")";
		else
			str +=entityInstance;
		str += ", ot="+obsType.getLabel();
		str +="]";
		
		return str;
	}

	public int compareTo(ObservationInstance other) {
		return (obsId.compareTo(other.getObsId()));	
	}
	
	public void toPrintStream(PrintStream p)
	{
		p.println(obsId + "," + entityInstance.getEntId() +","+obsType.getLabel());
	}
	
	public void fromPrintStream(BufferedReader in) throws IOException{
		String line = in.readLine(); 
		String[] strArray = line.split(",");
		obsId = Long.parseLong(strArray[0]);
		
		Long eiId = Long.parseLong(strArray[1]);
		EntityInstance ei = oboe.GetEntityInstance(eiId);
		
		oboe.AddObservationInstance(this);
	}
}
