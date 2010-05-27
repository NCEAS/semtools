package org.ecoinformatics.oboe;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVDataReader {

	/**
	 * Read data file, the structure is put in oRowStruct, and the data type is put in oAttrType 
	 * 
	 * The first row is attribute names
	 * The second row is attribute types
	 * Later rows are data rows
	 * 
	 * @param dataFileName
	 * @param oRowStruct
	 * @return
	 * @throws Exception
	 */
	public static List<ArrayList<String> > read(String dataFileName, 
			ArrayList<String> oRowStruct,
			ArrayList<String> oAttrType)
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
					oRowStruct.add("record_id");
					for(int i=0;i<attNames.length;i++){
						oRowStruct.add(attNames[i]);
					}
				}else if(lineNo==1){
					String[] attTypes = line.split(",");
					oAttrType.add("bigint");
					for(int i=0;i<attTypes.length;i++){
						oAttrType.add(attTypes[i]);
					}
				}else{
					ArrayList<String> row = new ArrayList<String>();
					String[] rowValues = line.split(",");
					if(rowValues.length!=oRowStruct.size()-1){
						throw new Exception("Data structure attribute number is different row value number.");
					}
					Integer recordId = lineNo-2;
					row.add(recordId.toString());
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
