package org.ecoinformatics.oboe;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.ecoinformatics.oboe.syntheticdata.AnnotationSpecifier;
import org.ecoinformatics.oboe.syntheticdata.DataGenerator;
import org.ecoinformatics.oboe.util.Debugger;

public class QueryGenerator {

	public static void main(String[] args) throws Exception {
		if(args.length<3){
			System.out.println("Usage: ./QueryGenerator <1. data_prefix> <2.file num> <3. attr slectivity double(0,1.0]> <4. list of record selectivity>");
			return;
		}
		
		String annotSpecFilePrefix = Constant.localUriPrefix + args[0];// + Constant.C_ANNOT_SPEC_FILE_SUFFIX;
		String dataFilePrefix = Constant.localUriPrefix +args[0];//+ Constant.C_DATA_FILE_SUFFIX;
		
		int filenum = Integer.parseInt(args[1]);
		double attrSelectivity = Double.parseDouble(args[2]);
		
		List<Double> recordSelectivity = new ArrayList<Double>();
		if(args.length>3){
			for(int i=1;i<args.length;i++){
				double selectivity = Double.parseDouble(args[i]);
				recordSelectivity.add(selectivity);
			}
		}else{
			recordSelectivity.add(0.5);
			recordSelectivity.add(0.2);
			recordSelectivity.add(0.1);
		}
		System.out.println("\n"+Debugger.getCallerPosition()+"annotSpecFilePrefix="+annotSpecFilePrefix);
		System.out.println(Debugger.getCallerPosition()+"dataFilePrefix="+dataFilePrefix);
		System.out.println(Debugger.getCallerPosition()+"filenum="+filenum);
		System.out.println(Debugger.getCallerPosition()+"attrSelectivity="+attrSelectivity+",recordSelectivity="+recordSelectivity+"\n");
		
		//for each file, get the data distribution for the attributes with given selectivity
		int numbOfRows = 5000;
		for(int i=0;i<1;i++){
			String annotSpecFile = annotSpecFilePrefix+"_"+(i+1)+Constant.C_ANNOT_SPEC_FILE_SUFFIX;
			String dataFile = dataFilePrefix+"_"+(i+1)+"-n5000"+ Constant.C_DATA_FILE_SUFFIX;
			
			//get data statistics
			//1. Read annotation specification files to annotation structure
			System.out.println(Debugger.getCallerPosition()+"1. Read annotation specification files ...");
			AnnotationSpecifier a = new AnnotationSpecifier();
			a.readAnnotationSpecFile(annotSpecFile);
			
			//2. Get statistics of the dataset
			System.out.println(Debugger.getCallerPosition()+"2. Get statistics of the dataset ...");
			DataGenerator generator = new DataGenerator(); 
			generator.setRownum(numbOfRows);
			generator.setAnnotation(a.getAnnotation());
			generator.setKey2distinctfactor(a.getKey2distinctfactor());
			Map<Integer, Integer> value2count = generator.Statistic(dataFile, "m5");
			System.out.println(Debugger.getCallerPosition()+"value2count size="+value2count.size()+",value2count="+value2count);
		}
		
		System.out.println(Debugger.getCallerPosition()+"Finish generating queries for atrselectivity="+attrSelectivity);
	}
	
}
