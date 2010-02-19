package org.ecoinformatics.oboe;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ecoinformatics.sms.annotation.*;

public class ObservationInstance {
	private static long gObsId = 0;
	private long obsId;
	private Observation obsType;
	private EntityInstance entityInstance;
	
	public ObservationInstance(Observation _obsType, EntityInstance _entityInstance)
	{
		this.setObsId(gObsId++);
		this.setObsType(_obsType);
		this.setEntity(_entityInstance);
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
	
	
}
