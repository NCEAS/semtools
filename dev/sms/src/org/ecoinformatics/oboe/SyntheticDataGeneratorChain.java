package org.ecoinformatics.oboe;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

import org.ecoinformatics.oboe.syntheticdataquery.*;
import org.ecoinformatics.oboe.util.Debugger;
import org.ecoinformatics.oboe.Constant;

public class SyntheticDataGeneratorChain {

	private static Map<String, Double> setAnnotSpecFile()
	{
		Map<String, Double> annotSpecFile2Ratio= new TreeMap<String,Double>();
//		annotSpecFile2Ratio.put("synchain_0.001-annot-spec.txt",0.001);
//		annotSpecFile2Ratio.put("synchain_0.01-annot-spec.txt",0.01);
//		annotSpecFile2Ratio.put("synchain_0.05-annot-spec.txt",0.05);
//		annotSpecFile2Ratio.put("synchain_0.1-annot-spec.txt",0.1);
//		annotSpecFile2Ratio.put("synchain_0.2-annot-spec.txt",0.2);
//		annotSpecFile2Ratio.put("synchain_0.5-annot-spec.txt",0.5);
//		annotSpecFile2Ratio.put("synchain_other-annot-spec.txt",-1.0); //cover all the others
		annotSpecFile2Ratio.put("synchain_0.001",0.001);
		annotSpecFile2Ratio.put("synchain_0.01",0.01);
		annotSpecFile2Ratio.put("synchain_0.05",0.05);
		annotSpecFile2Ratio.put("synchain_0.1",0.1);
		annotSpecFile2Ratio.put("synchain_0.2",0.2);
		annotSpecFile2Ratio.put("synchain_0.5",0.5);
		annotSpecFile2Ratio.put("synchain_other",-1.0); //cover all the others
		return annotSpecFile2Ratio;
	}
	
	private static Map<String, Integer> calFileNum(Map<String, Double> annotSpecFile2Ratio,int totalFnum)
	{
		Map<String, Integer> annotSpecFile2fnum= new TreeMap<String,Integer>();
		
		int remainedFnum = totalFnum;
		for(String annotSpecFname : annotSpecFile2Ratio.keySet()){
			double ratio = annotSpecFile2Ratio.get(annotSpecFname);
			if(ratio>0.0){
				int fnum = (int)(totalFnum*ratio);
				annotSpecFile2fnum.put(annotSpecFname, fnum);
				remainedFnum -=fnum;
			}else{
				annotSpecFile2fnum.put(annotSpecFname, remainedFnum);
			}
		}
		
		//TODO: HP check the total number of files, should be correct.
		return annotSpecFile2fnum;
	}
	
	/**
	 */
	public static void main(String[] args) {
		if(args.length!=2){
			System.out.println("Usage: ./SyntheticDataGeneratorChain <0. total number of files> <1. number of rows per file> ");
			return;
		}
		
		int totalFnum = Integer.parseInt(args[0]);
		int numOfRows = Integer.parseInt(args[1]);
		
		Map<String, Double> annotSpecFile2Ratio = setAnnotSpecFile();
		Map<String, Integer> annotSpecFile2fnum = calFileNum(annotSpecFile2Ratio,totalFnum);
		System.out.println(Debugger.getCallerPosition()+"annotSpecFile2fnum="+annotSpecFile2fnum);
		
		System.out.println("numOfRows="+numOfRows+"\n");
		
		// Generate data to satisfy the annotation specifications
		try {
			for(String annotSpecFname : annotSpecFile2fnum.keySet()){
				int fnum = annotSpecFile2fnum.get(annotSpecFname);
				String inAnnotSpecFileName = Constant.localUriPrefix + annotSpecFname + Constant.C_ANNOT_SPEC_FILE_SUFFIX; 
				String outAnnotSpecFileName = Constant.localUriPrefix + annotSpecFname + Constant.C_ANNOT_FILE_SUFFIX;
				// Confirm parameters
				System.out.println("inAnnotSpecFileName="+inAnnotSpecFileName);
				System.out.println("outAnnotSpecFileName="+outAnnotSpecFileName);
				
				for(int i=0;i<fnum;i++){
					// Get input parameters
					String outDataSpecFileName = Constant.localUriPrefix +annotSpecFname + "-n"+args[1]+"-d"+(i+1)+ Constant.C_DATA_FILE_SUFFIX;
					System.out.println("outDataSpecFileName="+outDataSpecFileName);
					
					boolean needWriteAnnotFile = false;
					if(i==0) needWriteAnnotFile = true;
					
					DataGenerator.Generate(inAnnotSpecFileName,outAnnotSpecFileName, outDataSpecFileName, numOfRows,needWriteAnnotFile);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {			
			e.printStackTrace();
		}

	}

}
