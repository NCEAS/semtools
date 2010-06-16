package org.ecoinformatics.oboe;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

import org.ecoinformatics.oboe.util.Debugger;
import org.ecoinformatics.oboe.util.MyRandom;
import org.ecoinformatics.oboe.syntheticdataquery.DataStatistics;

public class AnnotSpecGenerator extends DataStatistics{
	private static int maximum_attribute_num_per_tb = 16;
	private static int maximum_attribute_no = 100;
	//private static int meas_num_per_obs = 2;
	private static double ratio = 0.5;
	
	
	private static List<List<Integer> > generateTableAttributes(int numAnnotSpecFiles)
	{
		
		List<List<Integer> > tableAttributes = new ArrayList<List<Integer> >();
		
		for(int i=0;i<numAnnotSpecFiles;i++){
			List<Integer> ontTbAtt = new ArrayList<Integer>();
			tableAttributes.add(ontTbAtt);
		}
	
		for(int i=0;i<probability.length;i++){
			int j=0;
			//put the measurement no. with selectivity control
			for(;j<(int)(probability[i]*numAnnotSpecFiles);j++){
				List<Integer> ontTbAtt = tableAttributes.get(j);
				ontTbAtt.add(i+1);
			}
			if(j==0){
				//TODO: HP for 0.05 and 0.01				
			}
			
			//put the measurements without selectivity control
			for(;j<numAnnotSpecFiles;j++){
				List<Integer> oneTbAtt = tableAttributes.get(j);
				
				int mno = MyRandom.getRand(probability.length+1,maximum_attribute_no);
				while(oneTbAtt.contains(mno)){
					mno = MyRandom.getRand(probability.length+1,maximum_attribute_no);
				}
				oneTbAtt.add(mno);
			}
		}
		
		return tableAttributes;
	}
	
	
	/**
	 * write the annotation specification of one table.
	 * TODO: HP this need to be changed to make it more general
	 * @param dataPrintStream
	 * @param oneTbAtt
	 */
	private static void writeOneAnnotSpec(PrintStream dataPrintStream,List<Integer> oneTbAtt)
	{
		for(int attrno=0;attrno<oneTbAtt.size();attrno++){
			int oid = attrno+1;
			int mid1 = oneTbAtt.get(attrno);
			//int mid2 = oneTbAtt.get(meas_num_per_obs*attrno+1);
			dataPrintStream.append("o"+oid+" e"+oid+" distinct:m"+mid1+" key,"+ratio+"\n");
		}
	}
	/**
	 * Based on the table attributes, write the specification to the 
	 * @param annotSpecFilePrefix
	 * @param tableAttributes
	 * @throws FileNotFoundException 
	 */
	private static void writeAnnotSpecFile(String annotSpecFilePrefix,List<List<Integer> > tableAttributes) 
		throws FileNotFoundException
	{
		for(int i=1;i<=tableAttributes.size();i++){
			String outAnnotSpecFname = annotSpecFilePrefix+"_"+i+Constant.C_ANNOT_SPEC_FILE_SUFFIX;
			PrintStream dataPrintStream = new PrintStream(outAnnotSpecFname);
			writeOneAnnotSpec(dataPrintStream,tableAttributes.get(i-1));
			dataPrintStream.close();
			
			System.out.println(Debugger.getCallerPosition()+"annot"+(i)+": "+tableAttributes.get(i-1)+"is written to "+outAnnotSpecFname);			
		}
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length!=2){
			System.out.println("Usage: ./QueryGenerator <1. annot_spec_file_prefix> <2. number of annot_spec>");
			return;
		}
		
		String annotSpecFilePrefix = Constant.localUriPrefix+args[0];
		int numAnnotSpecFiles = Integer.parseInt(args[1]);
		
		//generate the attribute names for the annotations  
		List<List<Integer> > tableAttributes = generateTableAttributes(numAnnotSpecFiles);
		//for(int i=0;i<tableAttributes.size();i++){
		//	System.out.println(Debugger.getCallerPosition()+"annot"+(i+1)+": "+tableAttributes.get(i));
		//}
		
		//write annotation specification files
		writeAnnotSpecFile(annotSpecFilePrefix,tableAttributes);
		
		System.out.println(Debugger.getCallerPosition()+"Finish generating annotation specification files.");
		
		
		//
	}
}
