package org.ecoinformatics.oboe;

import java.util.ArrayList;

public class TestData {
	
	/**
	 * This test data comes from er-2008-ex1-data.txt
	 * @param oRowStruct
	 * @return
	 */
	public static ArrayList setTestData1(ArrayList<String> oRowStruct)
	{
		//tree,spp,yr,dbh
		oRowStruct.add("tree");
		oRowStruct.add("spp");
		oRowStruct.add("yr");
		oRowStruct.add("dbh");
		
		ArrayList dataset = new ArrayList();
		//1,piru,2007,35.8
		ArrayList row1  = new ArrayList();
		row1.add(1);
		row1.add("piru");
		row1.add(2007);
		row1.add(35.8);
		dataset.add(row1);
		
		//1,piru,2008,36.2
		ArrayList row2  = new ArrayList();
		row2.add(1);
		row2.add("piru");
		row2.add(2008);
		row2.add(36.2);
		dataset.add(row2);
		
		//2,piru,2008,33.2
		ArrayList row3  = new ArrayList();
		row3.add(2);
		row3.add("piru");
		row3.add(2008);
		row3.add(33.2);
		dataset.add(row3);
		
		return dataset;
	}
}
