package org.ecoinformatics.oboe.query;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.ecoinformatics.oboe.Debugger;

public class OboeQueryResultContainer {
	ArrayList<OboeQueryResult> m_queryResult = null;
	
	public OboeQueryResultContainer(){
		m_queryResult = new ArrayList<OboeQueryResult>(); 		
	}
	
	public void add(OboeQueryResult queryResult){
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
