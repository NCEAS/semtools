package org.ecoinformatics.oboe.datastorage;

import org.ecoinformatics.oboe.query.OboeQueryResult;
import org.ecoinformatics.oboe.util.Debugger;
import org.ecoinformatics.oboe.util.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresDB {
	protected static int RECORD_ID_LEN = 16;
	protected Connection m_conn = null;
	
	final private String m_url = "jdbc:postgresql://localhost:5432/";
	private String m_db = "oboe";
	final private String m_user = "oboe";
	final private String m_password = "nceas";
	
	final protected String m_datasetAnnotTable = "data_annotation";
	final protected String m_obsTypeTable = "observation_type";
	final protected String m_contextTypeTable = "context_type";
	final protected String m_measTypeTable = "measurement_type";
	final protected String m_mapTable = "map";	

	final protected String m_maxDatasetIdSql = "SELECT last_value FROM did_seq;";
	
	//Table: test (col1 char(16), col2 char(16); 
	//the selection can work here. 
	//select col1, avg(cast (col2 as numeric)) from test 
	//where col2 ~ '^[-]?[0-9]+' AND col2 !~ '[a-zA-Z]+' 
	//and CAST (col2 AS numeric)>=5 
	//group by col1 having (avg(cast (col2 as numeric))>10.0);
	
	final public String m_DIGIT_RE = "'^[-]?[0-9]+'"; //this need to be more complete, how to make it allow leading and trailing spaces
	final public String m_STRING_RE = "'[a-zA-Z]+'"; //this need to be more complete
	
	public String getDb() {
		return m_db;
	}

	public void setDb(String mDb) {
		m_db = mDb;
	}
	
	public String getMeasTypeTable() {
		return m_measTypeTable;
	}
	
	public String getObsTypeTable() {
		return m_obsTypeTable;
	}
	
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
		if(m_conn==null){
			try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException cnfe) {
				System.err.println(Debugger.getCallerPosition()+"Couldn't find driver class:");
				System.out.println(Debugger.getCallerPosition()+"Couldn't find driver class:");
				throw cnfe;
			}
			
			try {
				m_conn = DriverManager.getConnection(m_url+m_db, m_user, m_password);
	        } catch (SQLException se) {
	            System.out.println(Debugger.getCallerPosition()+"Couldn't connect: print out a stack trace and exit.");
	            throw se;
	        }
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
		
		//1. get this entity name's observation type label
		Map<Long,String> annotId_otypelabel = retrieveObsTypeLabel(entityTypeName);
	
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
	 * This function does not use a lot of space 
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
					first = false;
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
			//System.out.println(Debugger.getCallerPosition()+"annotId2Otypeattr="+annotId2Otypeattr);
		}
		rs.close();
		stmt.close();
		
		//3. from the temporary structure annotId2Otypeattr, get the result
		for(Map.Entry<Long, Map> entry: annotId2Otypeattr.entrySet()){
			Long annotId = entry.getKey();
			List<String> contextOtypeLabel = annotId_contextotypelabel.get(annotId);
			Set<String> tmpOtypeLabelSet = (Set<String>)entry.getValue().keySet();
			
			//this temporary otype label set must contain all the context type label
			//System.out.println(Debugger.getCallerPosition()+"tmpOtypeLabelSet="+tmpOtypeLabelSet+",entry="+tmpOtypeLabelSet);
			
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
			" WHERE is_identifying='t' AND otypelabel = '" + otypeLabel.trim() + "' AND annot_id="+annotId+";";
		
		System.out.println(Debugger.getCallerPosition()+"sql= "+sql);
		Statement stmt = m_conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
	
		while(rs.next()){
			String contextOtypelabel = rs.getString(1).trim();
			contextOTypeLabels.add(contextOtypelabel);
		}
		rs.close();
		stmt.close();
		
		return contextOTypeLabels;
	}
	
	
	/**
	 * Get the direct context obs type labels for dataset with id annotId
	 * 
	 * @param annotId
	 * @param contextTbName
	 * @return
	 * @throws SQLException
	 */
	public Map<String,List<String>> retrieveDirectContextTypeLabels(Long annotId, String contextTbName, String identifying) 
		throws SQLException
	{
		//List<String> contextOTypeLabels = new ArrayList<String>();
		
		Map<String,List<String>> directOtypelabel2cotypelabel = new TreeMap<String, List<String> >();
		
		String sql = "SELECT otypeLabel,context_otypelabel FROM " + contextTbName + 
			" WHERE is_identifying="+identifying+" AND annot_id="+annotId+";";
		
		System.out.println(Debugger.getCallerPosition()+"sql= "+sql);
		Statement stmt = m_conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
	
		while(rs.next()){
			String otypelabel = rs.getString(1).trim();
			String contextOtypelabel = rs.getString(2).trim();
			
			List<String> contextOTypeLabels = directOtypelabel2cotypelabel.get(otypelabel);
			if(contextOTypeLabels==null){
				contextOTypeLabels = new ArrayList<String>();
				directOtypelabel2cotypelabel.put(otypelabel, contextOTypeLabels);
			}
			contextOTypeLabels.add(contextOtypelabel);
		}
		rs.close();
		stmt.close();
		
		return directOtypelabel2cotypelabel;
	}
	
	public void insertAllCT(long tbId, Map<String,List<String>> newOneTbOtypelabel2cotypelabel, 
			String newContxtTbName,boolean identifying) 
		throws SQLException
	{
		
		final String insertAllContextType ="INSERT INTO " +newContxtTbName + " VALUES(?,?,?,?,?);";
		PreparedStatement pstmtContext = m_conn.prepareStatement(insertAllContextType);
		
		for(String obsTypeLabel: newOneTbOtypelabel2cotypelabel.keySet()){
			List<String> contextObsTypeList = newOneTbOtypelabel2cotypelabel.get(obsTypeLabel);
			for(String contextOTypeLabel: contextObsTypeList){
				pstmtContext.setLong(1, tbId);
				pstmtContext.setString(2, obsTypeLabel);
				pstmtContext.setString(3, contextOTypeLabel);
				pstmtContext.setString(4, org.ecoinformatics.oboe.Constant.DEFAULT_RELATIONSHIP);
				pstmtContext.setBoolean(5, identifying);
				
				pstmtContext.execute();
			}
		}
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
		String sql = "SELECT annot_id, otypelabel FROM " + m_obsTypeTable + " AS ot";
		if(entityTypeName.contains("%")||entityTypeName.contains("_"))
			sql+=" WHERE ename ILIKE " + entityTypeName +";";
		else
			sql+=" WHERE ename =" + entityTypeName +";";
	
		System.out.println(Debugger.getCallerPosition()+"sql= "+sql);
		Map<Long,String> annotId_otypelabel = new HashMap<Long, String>();
		Statement stmt = m_conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
	
		while(rs.next()){
			Long annotId = rs.getLong(1);
			String otypelabel = rs.getString(2).trim();
			
			annotId_otypelabel.put(annotId,otypelabel);
		}
		rs.close();
		stmt.close();
		
		return annotId_otypelabel;
	}

	/**
	 * Perform an SQL query
	 * @param db
	 * @param sql
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public Set<OboeQueryResult> executeSQL (String sql, boolean withRecordId) 
		throws SQLException,Exception
	{
		Set<OboeQueryResult> resultSet = new TreeSet<OboeQueryResult>();
		
		//1. get connection
		Connection conn = getConnection();
		if(conn==null){
			open();
			conn = getConnection();
		}
		
		//2. execute sql
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		//3. extract results
		while(rs.next()){
			OboeQueryResult queryResult = new OboeQueryResult();
			
			Long datasetId = rs.getLong(1);
			queryResult.setDatasetId(datasetId);
			
			if(withRecordId){
				String recordId = rs.getString(2);
				queryResult.setRecordId(recordId);
			}
			
			resultSet.add(queryResult);
		}
		
		rs.close();
		stmt.close();
		
		return resultSet;
	}
		
	/**
	 * Get the data table id-annot url pair, which is for the given data file
	 * 
	 * @param dataFileName
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	protected Pair<Long,String> getDataTableId(String dataFileName) throws SQLException, Exception
	{
		Pair<Long,String> tbid2annoturl = new Pair<Long,String>(-1L,"");
		
		//long tbid = -1L;
		
		int pos = dataFileName.lastIndexOf("/");
		String pureDataFileName = dataFileName.trim();		
		if(pos>=0)pureDataFileName = dataFileName.trim().substring(pos+1);
		 
		String sql = "SELECT did, annot_uri FROM "+m_datasetAnnotTable+" WHERE dataset_file ILIKE '"+pureDataFileName+"%';";
		
		System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		
		//Check whether this data file exist in the data table or not, if it exists already, directly get the id
		Statement stmt = m_conn.createStatement();		
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next()){
			long tbid = rs.getLong(1);
			String annotiurl= rs.getString(2);
			if(annotiurl!=null) annotiurl = annotiurl.trim();
			tbid2annoturl.setFirst(tbid);
			tbid2annoturl.setSecond(annotiurl);
			break;
		}
		rs.close();
		stmt.close();
		
		//return tbid;
		//return tbid2annotid;
		return tbid2annoturl;
	}
	
	/**
	 * clean the MDB to remove all the contents with annotId
	 * 
	 * @param annotId
	 * @throws SQLException
	 */
	protected void cleanMDBType(long annotId) throws SQLException
	{
		Statement stmt = null;
		stmt = m_conn.createStatement();
			
		String cleanMapsql = "DELETE FROM "+ this.m_mapTable +" WHERE annot_id="+annotId;
		System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+cleanMapsql);
		stmt.execute(cleanMapsql);
		
		String cleaanCTsql = "DELETE FROM "+ this.m_contextTypeTable +" WHERE annot_id="+annotId;
		System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+cleaanCTsql);
		stmt.execute(cleaanCTsql);
		
		String cleaanMTsql = "DELETE FROM "+ this.m_measTypeTable +" WHERE annot_id="+annotId;
		System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+cleaanMTsql);
		stmt.execute(cleaanMTsql);
		
		String cleaanOTsql = "DELETE FROM "+ this.m_obsTypeTable +" WHERE annot_id="+annotId;
		System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+cleaanOTsql);
		stmt.execute(cleaanOTsql);
		
	}
	
	/**
	 * Get all the raw data table ids
	 * @return
	 * @throws Exception 
	 */
	public List<Long> getAllTbIds() throws Exception
	{
		List<Long> tbidList = new ArrayList<Long>();
		
		String sql = "SELECT did FROM data_annotation;";
		
		if(m_conn==null){
			open();
		}
		
		Statement stmt = m_conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next()){
			Long tbid = rs.getLong(1);
			tbidList.add(tbid);
		}
		rs.close();
		stmt.close();
		
		return tbidList;
	}
	
}
