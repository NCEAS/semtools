package org.ecoinformatics.oboe;

import java.io.*;

import org.ecoinformatics.oboe.syntheticdataquery.*;
import org.ecoinformatics.oboe.Constant;

public class SyntheticDataGenerator {

	
	
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
      
      (4) eg12-1 10 (Specification file see: eg12-1-annot-spec.txt) 
          Base: eg12
          Test: context chain
	 */
	public static void main(String[] args) {
		if(args.length!=3){
			System.out.println("Usage: ./SyntheticDataGenerator <0.random seed> <1. file name prefix> <2. number of rows> ");
			return;
		}
		int randomSeed = Integer.parseInt(args[0]);
		
		// Get input parameters
		String inAnnotSpecFileName = Constant.localUriPrefix + args[1] + Constant.C_ANNOT_SPEC_FILE_SUFFIX; 
		String outAnnotSpecFileName = Constant.localUriPrefix + args[1] + Constant.C_ANNOT_FILE_SUFFIX;
		String outDataSpecFileName = Constant.localUriPrefix +args[1] + "-rs"+args[0]+"-n"+args[2]+ Constant.C_DATA_FILE_SUFFIX;
		
		int numOfRows = Integer.parseInt(args[2]);
		
		// Confirm parameters
		System.out.println("randomSeed="+randomSeed);
		System.out.println("inAnnotSpecFileName="+inAnnotSpecFileName);
		System.out.println("outAnnotSpecFileName="+outAnnotSpecFileName);
		System.out.println("outDataSpecFileName="+outDataSpecFileName);
		System.out.println("numOfRows="+numOfRows+"\n");
		
		// Generate data to satisfy the annotation specifications
		try {
			DataGenerator.Generate(inAnnotSpecFileName,outAnnotSpecFileName, outDataSpecFileName, numOfRows,true,randomSeed);			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {			
			e.printStackTrace();
		}

	}

}
