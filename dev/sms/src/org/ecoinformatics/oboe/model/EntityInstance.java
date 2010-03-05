package org.ecoinformatics.oboe.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import org.ecoinformatics.oboe.OboeModel;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Entity;
public class EntityInstance {
	private static long gEntId=0;
	
	private long entId;
	private Entity entityType;
	
	private OboeModel oboe;
	private Annotation a;
	
	public EntityInstance(Entity _entityType)
	{
		setEntId(gEntId++);
		setEntityType(_entityType);
	}
	
	public long getEntId() {
		return entId;
	}
	public void setEntId(long _entId) {
		entId = _entId;
	}
	public Entity getEntityType() {
		return entityType;
	}
	public void setEntityType(Entity entityType) {
		this.entityType = entityType;
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
		String str = "[ei";
		str += entId;
		str +="("+ entityType.getName()+")";
		str +="]";
		return str;
	}
	
	public void toPrintStream(PrintStream p)
	{
		p.println(entId + "," + entityType.getName());		
	}
	
	public void fromPrintStream(BufferedReader in) throws IOException{
		String line = in.readLine(); 
		String[] strArray = line.split(",");
		entId = Long.parseLong(strArray[0]);
		
		String entTypeName = strArray[1];
		entityType = a.getEntity(entTypeName);
		
		oboe.AddEntityInstance(this);
	}
}
