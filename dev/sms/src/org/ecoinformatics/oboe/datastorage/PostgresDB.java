package org.ecoinformatics.oboe.datastorage;

import org.ecoinformatics.oboe.Debugger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresDB {
	protected static int RECORD_ID_LEN = 16;
		
	private String m_url = "jdbc:postgresql://localhost:5432/oboe";
	private String m_user = "oboe";
	private String m_password = "nceas";
	
	protected Connection m_conn = null;
	
	protected String m_datasetAnnotTable = "data_annotation";

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
	
	

		
	
}
