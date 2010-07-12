package org.ecoinformatics.oboe.datastorage;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.ecoinformatics.oboe.CSVDataReader;
import org.ecoinformatics.oboe.query.OboeQueryResult;
import org.ecoinformatics.oboe.query.QueryMeasurement;
//import org.ecoinformatics.oboe.query.ResultSetMetaData;
import org.ecoinformatics.oboe.util.Debugger;
import org.ecoinformatics.oboe.util.Pair;

public class RawDB extends PostgresDB{
	
	public static String TB_PREFIX = "tb";
	
	public RawDB(String dbname){
		super.setDb(dbname);		
	}
	
	/**
	 * Get the data table id for this data file
	 * If the data file exists in the database already, return its id.
	 * Otherwise, insert this data file to the database and return the biggest data set id.
	 *  
	 * TODO: annotation part need to be changed.
	 * 
	 * @param dataFileName
	 * @return
	 * @throws SQLException 
	 */
	private long calDataTableId(String dataFileName) throws SQLException, Exception
	{
//		long tbid = -1L;
//		
		int pos = dataFileName.lastIndexOf("/");
		String pureDataFileName = dataFileName.trim();
		
		if(pos>=0){
			pureDataFileName = dataFileName.trim().substring(pos+1);
		} 
		
		//Pair<Long,Long> tbId_pair_annotId = getDataTableId(pureDataFileName);
		Pair<Long,String> tbId_pair_annotId = getDataTableId(pureDataFileName);
		
		long tbid = tbId_pair_annotId.getFirst();
		//If the data file does not exist in the data table yet, insert this data file into the data table, and get data table id
		if(tbid<0L){
			String insSQL = "INSERT INTO " + m_datasetAnnotTable +"(dataset_file,with_rawdata) VALUES(?,?);";
			PreparedStatement pstmt = m_conn.prepareStatement(insSQL);
			pstmt.setString(1,pureDataFileName);
			pstmt.setBoolean(2, true);
			pstmt.execute();
			tbid = super.getMaxDatasetId();
		}else{
			String updSql = "UPDATE " + m_datasetAnnotTable +" SET with_rawdata='t' WHERE did=" +tbid+";";
			Statement stmt = m_conn.createStatement();
			stmt.executeUpdate(updSql);
			stmt.close();
		}
		
		if(tbid<0L){
			throw new Exception("Did not insert the data file into the data table.");
		}
		
		return tbid;
	}
	
	/**
	 * Formulate the sql to create the data table with given attribute names and attribute columns
	 * 
	 * @param tbName
	 * @param rowStruct: attribute names
	 * @param colTypes: attribute types, the size of this list should be the same to rowStruct
	 * @return
	 */
	private String formCrtTbSQL(String tbName,ArrayList<String> rowStruct,ArrayList<String> colTypes)
	{
		String sql = "CREATE TABLE "+tbName +"(";
		for(int i=0;i<rowStruct.size();i++){
			String colName = rowStruct.get(i);
			String colType = colTypes.get(i);
			sql += colName+" "+colType;
			if(i<rowStruct.size()-1){
				sql+=",";
			}else{
				sql+=");";
			}
		}
		
		System.out.println(Debugger.getCallerPosition()+"sql="+sql);		
		return sql;
	}
	
	/**
	 * Create a data table given its name, its attribute names and types
	 * 
	 * @param tbName
	 * @param rowStruct: attribute names
	 * @param colTypes: attribute types, the size of this list should be the same to rowStruct
	 * @throws SQLException
	 */
	private void crtDataTable(String tbName,ArrayList<String> rowStruct,ArrayList<String> colTypes) 
		throws SQLException
	{
		//For the sql to create the data table
		String sql = formCrtTbSQL(tbName,rowStruct,colTypes);
		
		//Create the data table
		Statement stmt = null;
		try {
			stmt = m_conn.createStatement();
			boolean rs = stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println(Debugger.getCallerPosition()+","+e.getErrorCode()+","+e.getMessage()+","+e.getSQLState());
			if(e.getSQLState().equals("42P07")){
				String dropTbsql = "DROP TABLE " + tbName+";";
				System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+dropTbsql);
				stmt.execute(dropTbsql);
				System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+sql);
				stmt.execute(sql);
			}else{
				throw e;
			}
		}finally{
			if(stmt!=null){
				stmt.close();
			}
		}
	}
	
	/**
	 * For the SQL statement to insert into this data table
	 * This sql statement is used to prepare statementt
	 * 
	 * @param tbName
	 * @param rowStruct: attribute names
	 * @param colTypes: attribute types, the size of this list should be the same to rowStruct
	 * @return the sql statement
	 */
	private String formInsTbSQL(String tbName,ArrayList<String> rowStruct,ArrayList<String> colTypes)
	{
		String sql = "INSERT INTO "+tbName +"(";
		for(int i=0;i<rowStruct.size();i++){
			String colName = rowStruct.get(i);
			sql += colName;
			if(i<rowStruct.size()-1){
				sql+=",";
			}else{
				sql+=")";
			}
		}
		
		sql+=" VALUES(";
		for(int i=0;i<rowStruct.size();i++){
			sql += "?";
			if(i<rowStruct.size()-1){
				sql+=",";
			}else{
				sql+=")";
			}
		}
		
		sql+=";";
		
		System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		
		return sql;
	}
	
	/**
	 * Set the parameters for inserting the current row to the data table
	 * 
	 * @param pstmt
	 * @param row: attribute values
	 * @param colTypes: attribute types, the size of this list should be the same to row
	 * @throws Exception
	 */
	private void setInsParam(PreparedStatement pstmt, ArrayList<String> row,ArrayList<String> colTypes) 
		throws Exception
	{
		for(int i=0;i<colTypes.size();i++){
			String valStr = row.get(i);
			String colType = colTypes.get(i);
			//System.out.println(Debugger.getCallerPosition()+"i="+i+",colType="+colType);
			if(colType.startsWith("int")){
				pstmt.setInt(i+1, Integer.parseInt(valStr));
			}else if(colType.startsWith("bigint")){
				pstmt.setLong(i+1, Long.parseLong(valStr));
			}else if(colType.startsWith("numeric")){
				pstmt.setDouble(i+1, Double.parseDouble(valStr));
			}else{
				pstmt.setString(i+1, valStr);
			}
		}
	}
	
	/**
	 * Load the whole dataset to the given data table with given attribute names and types
	 * 
	 * @param dataset
	 * @param tbName
	 * @param rowStruct: attribute names
	 * @param colTypes: attribute types, the size of this list should be the same to rowStruct
	 * @throws SQLException
	 * @throws Exception
	 */
	private void loadData(List<ArrayList<String> > dataset,
			String tbName,ArrayList<String> rowStruct,ArrayList<String> colTypes) 
		throws SQLException, Exception 
	{
		String insTbSQL = formInsTbSQL(tbName,rowStruct,colTypes);
	
		PreparedStatement pstmt = m_conn.prepareStatement(insTbSQL);
		
		
		for(int i=0;i<dataset.size();i++){
			ArrayList<String> row = dataset.get(i);
			setInsParam(pstmt, row, colTypes);
			try{
				pstmt.execute();
			}catch(SQLException e){
				System.out.println(Debugger.getCallerPosition()+"i="+i+",row="+row);
				throw e;
			}
		}
	}
	
	/**
	 * Load the data file into data base
	 * 
	 * @param dataFileName
	 * @throws IOException
	 * @throws Exception
	 * @return the datat table name
	 */
	public String load(String dataFileName) throws IOException,Exception
	{
		ArrayList<String> rowStruct = new ArrayList<String>();
		ArrayList<String> colType = new ArrayList<String>();
		List dataset = null;
		
		//read the data from data file to data structure
		dataset = CSVDataReader.read(dataFileName, rowStruct,colType);
		
		super.open();
		
		//get the data table name
		long tbId = calDataTableId(dataFileName);
		String tbName = TB_PREFIX+tbId;
		
		//create this data table
		crtDataTable(tbName,rowStruct,colType);
		
		//inser the data into this data table
		loadData(dataset,tbName,rowStruct,colType);
		
		super.close();
		
		return tbName;
	}
	
	/**
	 * Delete the raw data table gotten from the data file name
	 * 
	 * @param dataFileName
	 * @throws IOException
	 * @throws Exception
	 */
	public void delete(String dataFileName) throws IOException,Exception
	{
		super.open();
		int pos = dataFileName.lastIndexOf("/");
		String pureDataFileName = dataFileName.trim();		
		if(pos>=0){
			pureDataFileName = dataFileName.trim().substring(pos+1);
		} 
		//Pair<Long,Long> tbId_pair_annotId = getDataTableId(pureDataFileName);
		Pair<Long,String> tbId_pair_annotId = getDataTableId(pureDataFileName);
		
		long tbId = tbId_pair_annotId.getFirst();
		if(tbId>=0){
			String tbName = TB_PREFIX+tbId;
			String dropTbsql = "DROP TABLE " + tbName+";";
			
			Statement stmt = null;
			stmt = m_conn.createStatement();
			System.out.println(Debugger.getCallerPosition()+"1. EXECUTE: "+dropTbsql);
			stmt.execute(dropTbsql);
			
			String updAnnotsql = "UPDATE "+ this.m_datasetAnnotTable +" SET with_rawdata ='f'" +" WHERE did="+tbId;
			System.out.println(Debugger.getCallerPosition()+"2. EXECUTE: "+updAnnotsql);
			stmt.execute(updAnnotsql);
			stmt.close();
		}
		
		//clean this table only
		Statement stmt = m_conn.createStatement();
		String updAnnotsql = "DELETE FROM "+ this.m_datasetAnnotTable +" WHERE annot_uri is NULL AND with_rawdata='f'";
		System.out.println(Debugger.getCallerPosition()+"4. EXECUTE: "+updAnnotsql);
		stmt.execute(updAnnotsql);
		stmt.close();
		
		super.close();
	}
	
	
	/**
	 * For the given set of characteristics, get the tables (together with the related attributes) 
	 * that have attributes for ALL the characteristics 
	 * 
	 * @param characteristics
	 * @return pair is <characteristic,attributename>
	 * @throws SQLException
	 */
	public Map<Long, List<Pair<QueryMeasurement,String>>> retrieveTbAttribute(Map<String, QueryMeasurement> cha2qm) 
	throws SQLException
	{
		Map<Long, List<Pair<QueryMeasurement,String>>> tmpTbAttribute = new TreeMap<Long, List<Pair<QueryMeasurement,String> >>();
		
		//for each characteristic, get its related dataset id (i.e., annot_id) and attribute name)
		for(String cha: cha2qm.keySet()){
			Map<Long, List<Pair<QueryMeasurement,String>>> oneTb2Attribute = retrieveOneTbAttribute(cha,cha2qm.get(cha));
			tmpTbAttribute.putAll(oneTb2Attribute);
		}
		//System.out.println(Debugger.getCallerPosition()+"tmpTbAttribute=\n"+tmpTbAttribute);
		
		//get only the table ids which has all these characteristics since they are in AND logic 
		Map<Long, List<Pair<QueryMeasurement,String> >> tbAttribute = new TreeMap<Long, List<Pair<QueryMeasurement,String> >>();
		for(Map.Entry<Long, List<Pair<QueryMeasurement,String> >> entry: tmpTbAttribute.entrySet()){
			//each data table need to have all these characteristics since they are in AND logic 
			//System.out.println(Debugger.getCallerPosition()+"entry.getValue().size()="+entry.getValue().size()+",cha2qm.size()="+cha2qm.size()+":"+cha2qm);
			
			if(entry.getValue().size()>=cha2qm.size()){
				tbAttribute.put(entry.getKey(), entry.getValue());
			}
		}
		
		//System.out.println(Debugger.getCallerPosition()+"tbAttribute=\n"+tbAttribute);
		return tbAttribute;
	}
	
	
	/**
	 * Get one table (with given tbid)'s measurement -->attribute pairs
	 * @param cha2qm
	 * @param tbid
	 * @return
	 * @throws SQLException
	 */
	public List<Pair<QueryMeasurement,String>> retrieveTbAttribute(Map<String, QueryMeasurement> cha2qm,long tbid) 
		throws SQLException
	{
		List<Pair<QueryMeasurement,String>> tbAttribute = new ArrayList<Pair<QueryMeasurement,String> >();
		
		//for each characteristic, get its related dataset id (i.e., annot_id) and attribute name)
		for(String cha: cha2qm.keySet()){
			List<Pair<QueryMeasurement,String>> oneTb2Attribute = retrieveOneTbAttribute(cha,cha2qm.get(cha),tbid);
			
			//this characteristic cannot find a related attribute
			if(oneTb2Attribute==null||oneTb2Attribute.size()==0)
				break;
			tbAttribute.addAll(oneTb2Attribute);
		}
		//System.out.println(Debugger.getCallerPosition()+"tmpTbAttribute=\n"+tmpTbAttribute);
		
		//get only the table ids which has all these characteristics since they are in AND logic 
		//Map<Long, List<Pair<QueryMeasurement,String> >> tbAttribute = new TreeMap<Long, List<Pair<QueryMeasurement,String> >>();
		//for(Map.Entry<Long, List<Pair<QueryMeasurement,String> >> entry: tmpTbAttribute.entrySet()){
			//each data table need to have all these characteristics since they are in AND logic 
			//System.out.println(Debugger.getCallerPosition()+"entry.getValue().size()="+entry.getValue().size()+",cha2qm.size()="+cha2qm.size()+":"+cha2qm);
			
		//	if(entry.getValue().size()>=cha2qm.size()){
		//		tbAttribute.put(entry.getKey(), entry.getValue());
		//	}
		//}
		
		//System.out.println(Debugger.getCallerPosition()+"tbAttribute=\n"+tbAttribute);
		return tbAttribute;
	}
	
	/**
	 * Get the table and its related attributes for one chalracteristic
	 * @param cha
	 * @return
	 * @throws SQLException
	 */
	public Map<Long, List<Pair<QueryMeasurement,String>>> retrieveOneTbAttribute(String cha,QueryMeasurement qm) 
		throws SQLException
	{
		Map<Long, List<Pair<QueryMeasurement,String>>> oneTb2Attribute = new TreeMap<Long, List<Pair<QueryMeasurement,String> >>();
		
		String sql = "SELECT DISTINCT mt.annot_id, attrname FROM measurement_type AS mt, map \n" +
					"WHERE (map.annot_id=mt.annot_id AND map.mtypelabel = mt.mtypelabel) "; 
		if(cha.contains("%")){
			sql += "AND mt.characteristic ILIKE "+cha+";";
		}else{
			sql += "AND mt.characteristic = "+cha+";";
		}

		System.out.println(Debugger.getCallerPosition()+"sql= " + sql);
		Statement stmt = m_conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next()){
			Long annotId = rs.getLong(1);
			String attrname = rs.getString(2).trim();
			
			List<Pair<QueryMeasurement,String> > oneTbAttribute = oneTb2Attribute.get(annotId);
			if(oneTbAttribute==null){
				oneTbAttribute = new ArrayList<Pair<QueryMeasurement,String> >();
				oneTb2Attribute.put(annotId, oneTbAttribute);
			}
			Pair<QueryMeasurement,String> newPair = new Pair<QueryMeasurement,String>(qm,attrname);
			oneTbAttribute.add(newPair);
		}
		rs.close();
		stmt.close();
		
		return oneTb2Attribute;
	}
	
	/**
	 * Return one table (with id tbid)'s measurement, attribute name pairs
	 * 
	 * @param cha
	 * @param qm
	 * @param tbid
	 * @return
	 * @throws SQLException
	 */
	public List<Pair<QueryMeasurement,String>> retrieveOneTbAttribute(String cha,QueryMeasurement qm,long tbid) 
		throws SQLException
	{
		//Map<Long, List<Pair<QueryMeasurement,String>>> oneTb2Attribute = new TreeMap<Long, List<Pair<QueryMeasurement,String> >>();
		
		String sql = "SELECT DISTINCT mt.annot_id, attrname FROM measurement_type AS mt, map \n" +
					"WHERE (map.annot_id=mt.annot_id AND map.mtypelabel = mt.mtypelabel AND mt.annot_id="+tbid+") "; 
		if(cha.contains("%")){
			sql += "AND mt.characteristic ILIKE "+cha+";";
		}else{
			sql += "AND mt.characteristic = "+cha+";";
		}
	
		System.out.println(Debugger.getCallerPosition()+"sql= " + sql);
		Statement stmt = m_conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		List<Pair<QueryMeasurement,String> > oneTbAttribute = new ArrayList<Pair<QueryMeasurement,String> >();
		while(rs.next()){
			Long annotId = rs.getLong(1);
			String attrname = rs.getString(2).trim();
			
			//List<Pair<QueryMeasurement,String> > oneTbAttribute = oneTb2Attribute.get(annotId);
			//if(oneTbAttribute==null){
			//	oneTbAttribute = new ArrayList<Pair<QueryMeasurement,String> >();
			//	oneTb2Attribute.put(annotId, oneTbAttribute);
			//}
			Pair<QueryMeasurement,String> newPair = new Pair<QueryMeasurement,String>(qm,attrname);
			oneTbAttribute.add(newPair);
		}
		rs.close();
		stmt.close();
		
		//return oneTb2Attribute;
		return oneTbAttribute;
	}
	
	/**
	 * Perform one data query and return the results 
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException, Exception 
	 */
	public Set<OboeQueryResult> dataQuery(String sql) throws SQLException, Exception
	{
		Set<OboeQueryResult> resultSet = new TreeSet<OboeQueryResult>();
		
		if(m_conn==null){
			open();
		}
		
		//System.out.println(Debugger.getCallerPosition()+"sql= "+sql);
		Statement stmt = m_conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		ResultSetMetaData rsmd = rs.getMetaData();
		int numOfCols = rsmd.getColumnCount();
		while(rs.next()){
			OboeQueryResult queryResult = new OboeQueryResult();
			
			Long datasetId = rs.getLong(1);
			queryResult.setDatasetId(datasetId);
			
			if(numOfCols>=2){
				String recordId = rs.getString(2);
				queryResult.setRecordId(recordId);
			}
			resultSet.add(queryResult);
		}
		rs.close();
		stmt.close();
		
		return resultSet;
	}
	
}
