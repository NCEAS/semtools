package org.ecoinformatics.oboe.query;

public class OboeQueryResult implements Comparable<OboeQueryResult>{
	private Long m_datasetId;
	private String m_recordId;
	
	public Long getDatasetId() {
		return m_datasetId;
	}
	
	public void setDatasetId(Long mDatasetId) {
		m_datasetId = mDatasetId;
	}
	
	public String getRecordId() {
		return m_recordId;
	}
	
	public void setRecordId(String mRecordId) {
		if(mRecordId!=null)
			m_recordId = mRecordId.trim();
		else{
			m_recordId = mRecordId;
		}
	}

	public String toString()
	{
		String str="("+m_datasetId+","+m_recordId+")";
		return str;
	}
	
	public int compareTo(OboeQueryResult o) {	
		int cmp1= m_datasetId.compareTo(o.getDatasetId());
		if(cmp1==0){
			if(m_recordId==null||o.getRecordId()==null)
				return 0;
			cmp1 = m_recordId.compareTo(o.getRecordId());			
		}
		return cmp1;
	}
}
