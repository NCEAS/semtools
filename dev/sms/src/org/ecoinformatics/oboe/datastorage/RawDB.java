package org.ecoinformatics.oboe.datastorage;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.ecoinformatics.oboe.CSVDataReader;
import org.ecoinformatics.oboe.Debugger;

public class RawDB extends PostgresDB{
	
	private String TB_PREFIX = "tb";
	
	
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
		
		String sql = "SELECT did FROM "+super.m_datasetAnnotTable+" WHERE dataset_file='"+dataFileName.trim()+"';";
		
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
			pstmt.setString(1,dataFileName.trim());
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
		Statement stmt = m_conn.createStatement();		
		boolean rs = stmt.execute(sql);
		stmt.close();
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
			if(colType.startsWith("int")){
				pstmt.setLong(i, Integer.parseInt(valStr));
			}else if(colType.startsWith("bigint")){
				pstmt.setLong(i, Long.parseLong(valStr));
			}else if(colType.startsWith("numeric")){
				pstmt.setDouble(i, Double.parseDouble(valStr));
			}else{
				pstmt.setString(i, valStr);
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
}
