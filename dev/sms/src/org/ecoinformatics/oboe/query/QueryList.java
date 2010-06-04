package org.ecoinformatics.oboe.query;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ecoinformatics.oboe.util.Debugger;

public class QueryList {
	List<OMQuery> m_queryList = null; //this is for averaging the results
	
	public QueryList(){
		m_queryList = new ArrayList<OMQuery>(); 		
	}
	
	/**
	 * Return the query list as a string
	 */
	public String toString()
	{
		String str="";
		for(int i=0;i<m_queryList.size();i++){
			str += "i="+i+":"+m_queryList.get(i).toString() + "\n";			
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
	public OMQuery getQuery(int i)
	{
		return m_queryList.get(i);
	}
	
	
	/**
	 * Read from a file r all the queries
	 * and put them to the query list
	 * @param r
	 * @throws Exception 
	 */
	private void read(BufferedReader r) 
		throws Exception
	{
		String oneLine="";
		try {
			while((oneLine = r.readLine())!=null){
				oneLine = oneLine.trim();
				
				if(oneLine.length()==0)
					continue;
				
				if(oneLine.startsWith(Constant.QUERY_COMMENT_PREFIX))
					continue;
				
				System.out.println(Debugger.getCallerPosition()+"oneLine="+oneLine);
				
				//put the lines within one query to oneQuery list and parse it later
				if(oneLine.contains(Constant.QUERY_START)){
					OMQuery curQuery = new OMQuery();
					List<String> oneQuery = new ArrayList<String>();
					
					while((oneLine = r.readLine())!=null){
						oneLine = oneLine.trim();
						if(oneLine.length()==0)
							continue;
						if(oneLine.startsWith(Constant.QUERY_COMMENT_PREFIX))
							continue;
						
						if(oneLine.contains(Constant.QUERY_END))
							break;
						
						oneQuery.add(oneLine);
					}
					
					curQuery.parse(oneQuery);
					m_queryList.add(curQuery);
				}
				//finish parsing the lines for one query
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Read the given query file to the query list
	 * @param queryFile
	 * @throws IOException,Exception 
	 */
	public void read(String queryFile) throws IOException,Exception
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
		OMQuery query = new OMQuery();
		query.setTest();
		m_queryList.add(query);
	}
}
