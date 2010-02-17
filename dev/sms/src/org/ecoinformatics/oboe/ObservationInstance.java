package org.ecoinformatics.oboe;

public class ObservationInstance {
	private long _obsId;
	private EntityInstance entityInstance;
	
	public long get_obsId() {
		return _obsId;
	}
	public void set_obsId(long obsId) {
		_obsId = obsId;
	}
	public EntityInstance getEntity() {
		return entityInstance;
	}
	public void setEntity(EntityInstance pEntityInstance) {
		this.entityInstance = pEntityInstance;
	}
	
}
