package org.ecoinformatics.oboe;

import org.ecoinformatics.sms.annotation.Entity;
public class EntityInstance {
	private static long gEntId=0;
	
	private long entId;
	private Entity entityType;
	
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
	
	
}
