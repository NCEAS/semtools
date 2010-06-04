package org.ecoinformatics.oboe.query;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

import org.ecoinformatics.oboe.util.Debugger;

public class OboeQueryResultContainer {
	ArrayList<Set<OboeQueryResult> > m_queryResult = null;
	
	public OboeQueryResultContainer(){
		m_queryResult = new ArrayList<Set<OboeQueryResult> >(); 		
	}
	
	public void add(Set<OboeQueryResult> queryResult){
		m_queryResult.add(queryResult);
	}
	
	private void write(PrintStream o)
	{
		System.out.println(Debugger.getCallerPosition()+"Not finished...");
		System.exit(0);
	}

	/**
	 * Write the query results to a file
	 * @param queryResultFile
	 * @throws FileNotFoundException 
	 */
	public void write(String queryResultFile) throws FileNotFoundException
	{
		if(queryResultFile==null||(queryResultFile.trim().length()==0))
			return;
		
		PrintStream resultPrintStream = new PrintStream(queryResultFile);
		write(resultPrintStream);
		resultPrintStream.close();
		
		System.out.println("Result is written to "+queryResultFile);		
		
	}
}
