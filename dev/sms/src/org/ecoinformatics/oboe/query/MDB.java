package org.ecoinformatics.oboe.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.ecoinformatics.oboe.Debugger;
import org.ecoinformatics.oboe.datastorage.PostgresDB;

/**
 * Class for materialized database 
 * 
 * @author cao
 *
 */
public class MDB extends PostgresDB{

	
// 	/**
//	 * Translate the given query to a list of SQL (for union purpose) on matererialized database 
//	 * @param query
//	 * @return A list of SQL statements (their results should be unioned)
//	 */
//	private List<String> translateQuery(OMQuery query,boolean resultWithRecord)
//	{
//		List<String> resultSQL = new ArrayList<String>();
//		
//		List<ContextChain> contextChains = query.getContextChains();
//		for(int i=0;i<contextChains.size(); i++){
//			ContextChain oneContextQuery = contextChains.get(i);
//			
//			String sql = oneContextQuery.translateQuery(this, resultWithRecord);
//			
//			resultSQL.add(sql);
//		}
//		
//		return resultSQL;
//	}
	
//	/**
//	 * Perform a query over the materialized database
//	 * 
//	 * @param query
//	 * @return
//	 * @throws Exception
//	 */
//	public Set<OboeQueryResult> query1(OMQuery query, boolean resultWithRecord) 
//		throws Exception
//	{
//		Set<OboeQueryResult> resultSet = new TreeSet<OboeQueryResult>();
//		
//		//open database connection
//		super.open();
//		
//		//translate the OM query to SQL query
//		
//		List<String> sqlList = translateQuery(query, resultWithRecord);
//		
//		Connection conn = super.getConnection();
//		
//		//The results of performing such queries should be unioned
//		for(int i=0;i<sqlList.size();i++){
//			Statement stmt = conn.createStatement();
//			ResultSet rs = stmt.executeQuery(sqlList.get(i));
//			ResultSetMetaData rsmd = rs.getMetaData();
//			int numOfCols = rsmd.getColumnCount();
//			while(rs.next()){
//				OboeQueryResult queryResult = new OboeQueryResult();
//				String datasetId = rs.getString(1);
//				queryResult.setDatasetId(datasetId);
//				if(numOfCols>=2){
//					String recordId = rs.getString(2);
//					queryResult.setRecordId(recordId);
//				}
//				resultSet.add(queryResult);
//			}
//			rs.close();
//			stmt.close();
//		}
//			
//		//close database connection
//		super.close();
//		
//		return resultSet;		
//	}
	
	
}
