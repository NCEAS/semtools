package org.ecoinformatics.oboe;

import org.ecoinformatics.sms.annotation.Entity;
public class EntityInstance {
	private long _entId;
	private Entity entityType;
	
	public long getEntId() {
		return _entId;
	}
	public void setEntId(long entId) {
		_entId = entId;
	}
	public Entity getEntityType() {
		return entityType;
	}
	public void setEntityType(Entity entityType) {
		this.entityType = entityType;
	}
	
	
}
