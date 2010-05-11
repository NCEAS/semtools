package org.ecoinformatics.oboe.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ecoinformatics.oboe.Debugger;
import org.ecoinformatics.oboe.datastorage.PostgresDB;

/**
 * Class for materialized database 
 * 
 * @author cao
 *
 */
public class MDB extends PostgresDB{

	/**
	 * Translate the given query to sql on matererialized database 
	 * @param query
	 * @return
	 */
	private String translateQuery(Query query)
	{
		String sql = "";
		sql +="SELECT DISTINCT record_id ";
		sql +="FROM "+super.m_measInstanceTable+" AS mi," + super.m_measTypeTable + " AS mt ";
		String whereSql = query.getQueryMeasurementString();
		if(whereSql.length()>0){
			sql +="WHERE " + whereSql;
		}
		//TODO: add GROUP BY and HAVING clause
		sql+=";";
		System.out.println(Debugger.getCallerPosition()+"\n"+sql);
		return sql;
	}
	
	public OboeQueryResult query(Query query) throws Exception
	{
		OboeQueryResult queryResult = new OboeQueryResult();
		
		//open database connection
		super.open();
		
		String sql = translateQuery(query);
		
		Connection conn = super.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()){
			String id = rs.getString(1);
			System.out.println("id= "+ id);
		}
		rs.close();
		stmt.close();
			
		//close database connectioni
		super.close();
		
		return queryResult;		
	}
}
