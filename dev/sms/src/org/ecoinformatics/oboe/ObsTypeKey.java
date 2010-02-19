package org.ecoinformatics.oboe;

public class ObsTypeKey {
	String obsTypeLabel;
	String keyValue;
	
	public ObsTypeKey(String _obsTypeLabel, String _keyValue)
	{
		this.setObsTypeLabel(_obsTypeLabel);
		this.setKeyValue(_keyValue);
	}

	public String getObsTypeLabel() {
		return obsTypeLabel;
	}

	public void setObsTypeLabel(String _obsTypeLabel) {
		this.obsTypeLabel = _obsTypeLabel;
	}

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}
}
