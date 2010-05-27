package org.ecoinformatics.oboe.datastorage;

import org.ecoinformatics.oboe.Debugger;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ecoinformatics.oboe.util.Pair;

public class PostgresDB {
	protected static int RECORD_ID_LEN = 16;
		
	private String m_url = "jdbc:postgresql://localhost:5432/oboe";
	private String m_user = "oboe";
	private String m_password = "nceas";
	
	protected Connection m_conn = null;
	
	protected String m_datasetAnnotTable = "data_annotation";
	protected String m_obsTypeTable = "observation_type";
	protected String m_contextTypeTable = "context_type";
	protected String m_measTypeTable = "measurement_type";

	protected String m_maxDatasetIdSql = "SELECT last_value FROM did_seq;";
	
	//Table: test (col1 char(16), col2 char(16); 
	//the selection can work here. 
	//select col1, avg(cast (col2 as numeric)) from test 
	//where col2 ~ '^[-]?[0-9]+' AND col2 !~ '[a-zA-Z]+' 
	//and CAST (col2 AS numeric)>=5 
	//group by col1 having (avg(cast (col2 as numeric))>10.0);
	
	public String m_DIGIT_RE = "'^[-]?[0-9]+'"; //this need to be more complete, how to make it allow leading and trailing spaces
	public String m_STRING_RE = "'[a-zA-Z]+'"; //this need to be more complete
	
	/**
	 * Return the maximum data set id
	 * 
	 * @return
	 * @throws SQLException
	 */
	protected long getMaxDatasetId() throws SQLException
	{
		Statement stmt = m_conn.createStatement();
		
		ResultSet rs = stmt.executeQuery(m_maxDatasetIdSql);
		long maxDatasetId = 1;
		while(rs.next()){
			maxDatasetId = rs.getLong(1);
			break;
		}
		
		return maxDatasetId;
	}
	
	/**
	 * Open the postgres connection 
	 * 
	 * @throws Exception
	 */
	public void open()
		throws Exception
	{
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException cnfe) {
			System.err.println(Debugger.getCallerPosition()+"Couldn't find driver class:");
			System.out.println(Debugger.getCallerPosition()+"Couldn't find driver class:");
			throw cnfe;
		}
		
		try {
			m_conn = DriverManager.getConnection(m_url, m_user, m_password);
        } catch (SQLException se) {
            System.out.println(Debugger.getCallerPosition()+"Couldn't connect: print out a stack trace and exit.");
            throw se;
        }
	}
	
	public Connection getConnection()
	{
		return m_conn;
	}
	
	/**
	 * Close the postgres connection 
	 */
	public void close() throws Exception
	{
		if(m_conn!=null){
			try {
				m_conn.close();
			} catch (SQLException e) {				
				e.printStackTrace();
				throw e;
			}
		}
	}
	
	/**
	 * Given an entity type name, it may contain some special character, e.g., '%', '_', etc. 
	 * First clean this entity type name, then find the dataset (i.e., annotation) id with 
	 * the key characteristics' attributes 
	 * 
	 * This is similar to MaterializeDb.GetObsTypeKeys()
	 * 
	 * @param entityTypeName
	 * @return
	 * @throws SQLException 
	 */
	public Map<Long, List<String>> calKeyAttr(String entityTypeName) throws SQLException
	{
		Map<Long, List<String>> annotId2AttrList= new HashMap<Long, List<String>>();
		
		System.out.println(Debugger.getCallerPosition()+"entityTypeName="+entityTypeName);
		//1. clean the entity type name
		//entityTypeName.replaceAll("%", "");
		//entityTypeName.replaceAll("_", "");
		//System.out.println(Debugger.getCallerPosition()+"entityTypeName="+entityTypeName);
		
		//1. get this entity name's observation type label
		Map<Long,String> annotId_otypelabel = retrieveObsTypeLabel(entityTypeName);
	
		//TODO: If the candidate set is big, this is a place that I should save a lot of time 
		//2. get the context observation types
		Map<Long, List<String> > annotId_contextotypelabel = new HashMap<Long, List<String>>();
		for(Map.Entry<Long, String> entry: annotId_otypelabel.entrySet()){
			Long annotId = entry.getKey();
			String otypeLabel = entry.getValue();
			
			List<String> contextOTypeLabel =  retrieveContextOTypeLabel(annotId,otypeLabel);
			annotId_contextotypelabel.put(annotId, contextOTypeLabel);
		}
		
		//3. From the annotationId-context observation type labels, get all the list of <annotId-list of key attributes>
		annotId2AttrList= retrieveKeyAttrList(annotId_contextotypelabel);
		
		return annotId2AttrList;
	}
	

	/**
	 * From the annotationId-context observation type labels, 
	 * Get all the list of <annotId-list of key attributes>
	 * 
	 * TODO: this function may use a lot of space, how much? need to test 
	 * @param annotId
	 * @param otypelabel
	 * @return
	 * @throws SQLException 
	 */
	private Map<Long, List<String> > retrieveKeyAttrList(Map<Long, List<String> > annotId_contextotypelabel) throws SQLException
	{
		Map<Long, List<String> > annotId2AttrList= new HashMap<Long, List<String>>();
		
		//1. form SQL to get the attributes with the given annot_id
		String sql = "SELECT DISTINCT mt.annot_id as annot_id, mt.otypelabel as otypelabel, attrname " +
				"FROM map, " + m_measTypeTable +" AS mt " +
				"WHERE mt.annot_id = map.annot_id AND mt.mtypelabel=map.mtypelabel AND mt.isKey = 't' "; 
		
		if(annotId_contextotypelabel!=null&&annotId_contextotypelabel.keySet().size()>0){
			sql += "AND mt.annot_id IN (";
			boolean first = true;
			for(Long annotId : annotId_contextotypelabel.keySet()){
				if(!first){
					sql+=","+annotId;
				}else{
					sql+=annotId;
				}
			}
			sql +=")";
		}
		sql +=";";
		
		//2. execute the sql
		Map<Long, Map> annotId2Otypeattr = new HashMap<Long, Map>();
		Statement stmt = m_conn.createStatement();
		System.out.println(Debugger.getCallerPosition()+"sql= "+sql);
		ResultSet rs = stmt.executeQuery(sql);
	
		while(rs.next()){
			Long annotId = rs.getLong(1);
			String otypelabel = rs.getString(2).trim();
			String attrname = rs.getString(3).trim();
			Map otypeattr = annotId2Otypeattr.get(annotId);
			if(otypeattr==null){
				otypeattr = new HashMap<String, List>();
				annotId2Otypeattr.put(annotId, otypeattr);
			}
			List<String> attrList = (List<String>)otypeattr.get(otypelabel);
			if(attrList==null){
				attrList  = new ArrayList<String>();
				otypeattr.put(otypelabel, attrList);
			}
			attrList.add(attrname);
			System.out.println(Debugger.getCallerPosition()+"annotId2Otypeattr="+annotId2Otypeattr);
		}
		rs.close();
		stmt.close();
		
		//3. from the temporary structure annotId2Otypeattr, get the result
		for(Map.Entry<Long, Map> entry: annotId2Otypeattr.entrySet()){
			Long annotId = entry.getKey();
			List<String> contextOtypeLabel = annotId_contextotypelabel.get(annotId);
			Set<String> tmpOtypeLabelSet = (Set<String>)entry.getValue().keySet();
			
			//this temporary otype label set must contain all the context type label
			System.out.println(Debugger.getCallerPosition()+"tmpOtypeLabelSet="+tmpOtypeLabelSet+",entry="+tmpOtypeLabelSet);
			
			if(contextOtypeLabel!=null&&contextOtypeLabel.size()>0){
				List<String> attrList = new ArrayList<String>();
				for(String tmpOtypeLabel:tmpOtypeLabelSet){
					if(contextOtypeLabel.contains(tmpOtypeLabel)){
						attrList.addAll((List<String>)entry.getValue().get(tmpOtypeLabel));
					}
				}
				annotId2AttrList.put(annotId, attrList);
			}
		}
		return annotId2AttrList;
	}
	
	/**
	 * context type: 
	 * if the context chain is o1->o2->o3, the materialization should be 
	 * o1, o2
	 * o1, o3
	 * o2, o3
	 * 
	 * @param annotId
	 * @param otypeLabel
	 * @return
	 * @throws SQLException
	 */
	private List<String> retrieveContextOTypeLabel(Long annotId,String otypeLabel) throws SQLException
	{
		List<String> contextOTypeLabels = new ArrayList<String>();
		
		contextOTypeLabels.add(otypeLabel);
		String sql = "SELECT context_otypelabel FROM " + m_contextTypeTable + 
			"WHERE is_identifying='t' AND otypelabel = " + otypeLabel + " AND annot_id="+annotId+";";
		
		Statement stmt = m_conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
	
		while(rs.next()){
			String contextOtypelabel = rs.getString(1);
			contextOTypeLabels.add(contextOtypelabel);
		}
		rs.close();
		stmt.close();
		
		return contextOTypeLabels;
	}
	
	/**
	 * Given the entity type, retrie
	 * ve <annot_id, otypelabel> such that the obs.entity name is the given one
	 * and the is_distinct is yes
	 *  
	 * @param entityTypeName
	 * @return
	 * @throws SQLException
	 */
	private Map<Long,String> retrieveObsTypeLabel(String entityTypeName) throws SQLException
	{
		String sql = "SELECT annot_id, otypelabel FROM " + m_obsTypeTable + " AS ot " +
				"WHERE is_distinct='t' AND ename = " + entityTypeName +";";
		
		Map<Long,String> annotId_otypelabel = new HashMap<Long, String>();
		Statement stmt = m_conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
	
		while(rs.next()){
			Long annotId = rs.getLong(1);
			String otypelabel = rs.getString(2);
			
			annotId_otypelabel.put(annotId,otypelabel);
		}
		rs.close();
		stmt.close();
		
		return annotId_otypelabel;
	}

		
	
}
