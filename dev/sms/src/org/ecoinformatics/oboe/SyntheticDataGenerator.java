package org.ecoinformatics.oboe;

import java.io.*;
import org.ecoinformatics.oboe.syntheticdata.*;
import org.ecoinformatics.oboe.Constant;

public class SyntheticDataGenerator {

	private static String L_ANNOT_SPEC_FILE_SUFFIX = "-annot-spec.txt";
	private static String L_ANNOT_FILE_SUFFIX = "-annot.xml";
	private static String L_DATA_FILE_SUFFIX = "-data.txt";
	
	private static void Generate(String inAnnotSpecFileName,
			String outAnnotFileName, 
			String outDataFileName, 
			int numOfRows)
		throws Exception
	{
		
		//1. read annotation specification files to annotation structure
		AnnotationSpecifier a = new AnnotationSpecifier();
		a.readAnnotationSpecFile(inAnnotSpecFileName);
		
		//2. write to annotation files (for consistency checking purpose, this will not be used in the generation process)
		a.WriteAnnotation(outAnnotFileName);
		
		//3. generate dataset
		DataGenerator generator = new DataGenerator(); 
		generator.setRownum(numOfRows);
		generator.setAnnotation(a.getAnnotation());
		generator.setKey2distinctfactor(a.getKey2distinctfactor());
		//generator.GenerateBottomUp(); //this method, the unique factor cannot be guaranteed
		generator.GenerateTopDown(); //need further testing
		
		//4. write dataset
		generator.WriteData(outDataFileName); 		
	}
	
	/**
	 * @param args
	 * 
	 * Example 1: 
	 * Put the following two lines in the Arguments field
	  eg13
      2000
      it will generate an annotation file together with a data file with 2000 rows 
      to follow the annotation specification in eg13
      
      Top-down generator passed test cases: 
      (1) eg1 10: (Specification file see: eg1-annot-spec.txt) 
      	  this test case checks the basic function: 
              generating single key measurement values for observation types 
              one observation type only has one key measurement
              one observation type has one key measurement and one non-key measurement 
          eg2 10 (Specification file see: eg2-annot-spec.txt)
              This is similar to "eg1 10" case. 
      (2) eg12 10 (Specification file see: eg12-annot-spec.txt) 
      	  This test case checks multi-key measurements for one observation type on the base of "eg1"
          No context
      
      (3) eg13 10 (Specification file see: eg13-annot-spec.txt)
          This test case has context
          In the base of "eg12", add one observation type which has context of other two observation types
      
	 */
	public static void main(String[] args) {
		if(args.length!=2){
			System.out.println("Usage: ./MaterializeDB <0. file name prefix> <1. number of rows> ");
			return;
		}
		
		// Get input parameters
		String inAnnotSpecFileName = Constant.localOutputUriPrefix + args[0] + L_ANNOT_SPEC_FILE_SUFFIX; 
		String outAnnotSpecFileName = Constant.localOutputUriPrefix + args[0] + L_ANNOT_FILE_SUFFIX;
		String outDataSpecFileName = Constant.localOutputUriPrefix +args[0] + "-n"+args[1]+ L_DATA_FILE_SUFFIX;
		
		int numOfRows = Integer.parseInt(args[1]);
		
		// Confirm parameters
		System.out.println("inAnnotSpecFileName="+inAnnotSpecFileName);
		System.out.println("outAnnotSpecFileName="+outAnnotSpecFileName);
		System.out.println("outDataSpecFileName="+outDataSpecFileName);
		System.out.println("numOfRows="+numOfRows+"\n");
		
		try {
			Generate(inAnnotSpecFileName,outAnnotSpecFileName, outDataSpecFileName, numOfRows);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {			
			e.printStackTrace();
		}

	}

}
