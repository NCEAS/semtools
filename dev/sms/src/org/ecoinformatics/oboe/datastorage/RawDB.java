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
import org.ecoinformatics.oboe.Debugger;
import org.ecoinformatics.oboe.query.OboeQueryResult;
//import org.ecoinformatics.oboe.query.ResultSetMetaData;
import org.ecoinformatics.oboe.util.Pair;

public class RawDB extends PostgresDB{
	
	public String TB_PREFIX = "tb";
	
	
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
		long tbid = -1L;
		
		int pos = dataFileName.lastIndexOf("/");
		String pureDataFileName = dataFileName.trim();
		
		if(pos>=0){
			pureDataFileName = dataFileName.trim().substring(pos+1);
		} 
		
		String sql = "SELECT did FROM "+super.m_datasetAnnotTable+" WHERE dataset_file='"+pureDataFileName+"';";
		
		System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		
		//Check whether this data file exist in the data table or not, if it exists already, directly get the id
		Statement stmt = m_conn.createStatement();		
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next()){
			tbid = rs.getLong(1);
			break;
		}
		rs.close();
		stmt.close();
		
		//If the data file does not exist in the data table yet, insert this data file into the data table, and get data table id
		if(tbid<0L){
			String insSQL = "INSERT INTO " + m_datasetAnnotTable +"(dataset_file) VALUES(?);";
			PreparedStatement pstmt = m_conn.prepareStatement(insSQL);
			pstmt.setString(1,pureDataFileName);
			pstmt.execute();
			tbid = super.getMaxDatasetId();
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
			pstmt.execute();
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
	 * For the given set of characteristics, get the tables (together with the related attributes) 
	 * that have attributes for ALL the characteristics 
	 * 
	 * @param characteristics
	 * @return pair is <characteristic,attributename>
	 * @throws SQLException
	 */
	public Map<Long, List<Pair>> retrieveTbAttribute(Set<String> characteristics) throws SQLException
	{
		Map<Long, List<Pair>> tmpTbAttribute = new TreeMap<Long, List<Pair>>();
		
		//for each characteristic, get its related dataset id (i.e., annot_id) and attribute name)
		for(String cha: characteristics){
			String sql = "SELECT DISTINCT annot_id, attrname FROM measurement_type AS mt, map " +
					"WHERE map.mtypelabel = mt.mtypelabel AND mt.characteristic="+cha+";";
			
			Statement stmt = m_conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
		
			while(rs.next()){
				Long annotId = rs.getLong(1);
				String attrname = rs.getString(2);
				
				List<Pair> oneTbAttribute = tmpTbAttribute.get(annotId);
				if(oneTbAttribute==null){
					oneTbAttribute = new ArrayList<Pair>();
					tmpTbAttribute.put(annotId, oneTbAttribute);
				}
				Pair newPair = new Pair(cha,attrname);
				oneTbAttribute.add(newPair);
			}
			rs.close();
			stmt.close();
		}
		
		//get only the table ids which has all these characteristics since they are in AND logic 
		Map<Long, List<Pair>> tbAttribute = new TreeMap<Long, List<Pair>>();
		for(Map.Entry<Long, List<Pair>> entry: tmpTbAttribute.entrySet()){
			//each data table need to have all these characteristics since they are in AND logic 
			if(entry.getValue().size()==characteristics.size()){
				tbAttribute.put(entry.getKey(), entry.getValue());
			}
		}
		return tbAttribute;
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
