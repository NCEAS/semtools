package org.ecoinformatics.oboe;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVDataReader {

	public static ArrayList read(String dataFileName, ArrayList<String> oRowStruct)
		throws Exception
	{
		ArrayList dataset = new ArrayList();
		try{
			BufferedReader reader = new BufferedReader( new FileReader( dataFileName ));

			int lineNo = 0;
			String line;
			while( (line = reader.readLine()) != null ) {
				
				if(lineNo==0){
					String[] attNames = line.split(",");
					for(int i=0;i<attNames.length;i++){
						oRowStruct.add(attNames[i]);
					}
				}else{
					ArrayList<String> row = new ArrayList<String>();
					String[] rowValues = line.split(",");
					if(rowValues.length!=oRowStruct.size()){
						throw new Exception("Data structure attribute number is different row value number.");
					}
					for(int i=0;i<rowValues.length;i++){
						row.add(rowValues[i]);
					}
					dataset.add(row);
				}
				lineNo++;
			}
			
			reader.close();
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		return dataset;
	}
}
