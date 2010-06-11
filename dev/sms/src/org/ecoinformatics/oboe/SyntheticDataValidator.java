package org.ecoinformatics.oboe;

import java.io.FileNotFoundException;

import org.ecoinformatics.oboe.syntheticdata.AnnotationSpecifier;
import org.ecoinformatics.oboe.syntheticdata.DataGenerator;
import org.ecoinformatics.oboe.util.Debugger;

public class SyntheticDataValidator {

	/**
	 * Validate whether the input data file satisfy the annotation specification
	 * 
	 * @param inAnnotSpecFileName
	 * @param inDataFileName
	 * @param numOfRows
	 * @throws Exception
	 */
	private static void Validate(String inAnnotSpecFileName,
			String inDataFileName, 
			int numOfRows)
		throws Exception
	{
		
		//1. Read annotation specification files to annotation structure
		System.out.println(Debugger.getCallerPosition()+"1. Read annotation specification files ...");
		AnnotationSpecifier a = new AnnotationSpecifier();
		a.readAnnotationSpecFile(inAnnotSpecFileName);
		
		//2. Validate dataset
		System.out.println(Debugger.getCallerPosition()+"2. Validate dataset ...");
		DataGenerator generator = new DataGenerator(); 
		generator.setRownum(numOfRows);
		generator.setAnnotation(a.getAnnotation());
		generator.setKey2distinctfactor(a.getKey2distinctfactor());
		boolean rc = generator.Validate(inDataFileName);
		if(rc){
			System.out.println("\n"+Debugger.getCallerPosition()+"********* VALID *************\n");
		}else{
			System.out.println("\n"+Debugger.getCallerPosition()+"********* INVALID *************\n");
		}
	}
	
	/**
	 * Tested: 
	 * (1) eg1 10 correct
	 * (2) eg12 10 correct
	 * (3) eg13 10 correct
	 * (4) eg12-1 10 correct
	 * (5) eg2 10 correct
	 * @param args
	 */
	public static void main(String[] args)
	{
		if(args.length!=2){
			System.out.println("Usage: ./SyntheticDataValidator <0. file name prefix> <1. number of rows> ");
			return;
		}
		
		// Get input parameters
		String inAnnotSpecFileName = Constant.localUriPrefix + args[0] + Constant.C_ANNOT_SPEC_FILE_SUFFIX;
		String inDataFileName = Constant.localUriPrefix +args[0] + "-n"+args[1]+ Constant.C_DATA_FILE_SUFFIX;
		
		int numOfRows = Integer.parseInt(args[1]);
		
		// Confirm parameters
		System.out.println(Debugger.getCallerPosition()+"inAnnotSpecFileName="+inAnnotSpecFileName);
		System.out.println(Debugger.getCallerPosition()+"inDataFileName="+inDataFileName);
		System.out.println(Debugger.getCallerPosition()+"numOfRows="+numOfRows+"\n");
		
		// Validate whether the input data file satisfy the annotation specification
		try {
			Validate(inAnnotSpecFileName, inDataFileName, numOfRows);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
}
