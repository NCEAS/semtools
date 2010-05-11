package org.ecoinformatics.oboe.query;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryList {
	ArrayList<Query> m_queryList = null;
	
	public QueryList(){
		m_queryList = new ArrayList<Query>(); 		
	}
	
	/**
	 * Return the query list as a string
	 */
	public String toString()
	{
		String str="";
		for(int i=0;i<m_queryList.size();i++){
			str += m_queryList.get(i).toString() + "\n";			
		}
		return str;
	}

	/**
	 * Return the size of the query list
	 * @return
	 */
	public int size()
	{
		return m_queryList.size();
	}
	
	/**
	 * Get the i-th query. i is in [0, size()-1]
	 * 
	 * @param i
	 * @return
	 */
	public Query getQuery(int i)
	{
		return m_queryList.get(i);
	}
	
	
	/**
	 * Read from a file r all the queries
	 * and put them to the query lsit
	 * @param r
	 * @throws IOException 
	 */
	private void read(BufferedReader r) throws IOException
	{
		String oneQuery = "";
		
		try {
			while((oneQuery = r.readLine())!=null){
				if(oneQuery.trim().length()>0&&oneQuery.startsWith(Constant.QUERY_COMMENT_PREFIX))
					continue;
				Query query = new Query();
				
				query.parse(oneQuery);
				m_queryList.add(query);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Read the given query file to the query list
	 * @param queryFile
	 * @throws IOException
	 */
	public void read(String queryFile) throws IOException
	{
		BufferedReader bufferedReader = null; 
		bufferedReader = new BufferedReader(new FileReader(queryFile));
		
		read(bufferedReader);
		
		if (bufferedReader != null) bufferedReader.close();		
	}
	
	/**
	 * Set a test query listt
	 */
	public void setTest()
	{
		Query query = new Query();
		query.setTest();
		m_queryList.add(query);
	}
}
