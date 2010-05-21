package org.ecoinformatics.oboe.datastorage;

import org.ecoinformatics.oboe.Debugger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresDB {
	protected static int RECORD_ID_LEN = 16;
		
	private String m_url = "jdbc:postgresql://localhost:5432/oboe";
	private String m_user = "oboe";
	private String m_password = "nceas";
	
	protected Connection m_conn = null;
		
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
