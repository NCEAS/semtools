package org.ecoinformatics.oboe.model;

public class ObsTypeKey implements Comparable<ObsTypeKey>{
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

	public void setKeyValue(String _keyValue) {
		this.keyValue = _keyValue;
	}

	public int compareTo(ObsTypeKey other) {
		int comp1 = obsTypeLabel.compareTo(other.getObsTypeLabel());
		if(comp1!=0)
			return comp1;
		else{
			int comp2 = keyValue.compareTo(other.getKeyValue());
			return comp2;
		}
	}
	
	public String toString()
	{
		String str = obsTypeLabel+":"+keyValue;
		return str;
	}
}
