package org.ecoinformatics.oboe;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.ecoinformatics.oboe.query.OMQuery;
import org.ecoinformatics.oboe.syntheticdataquery.AnnotationSpecifier;
import org.ecoinformatics.oboe.syntheticdataquery.DataGenerator;
import org.ecoinformatics.oboe.syntheticdataquery.DataStatistics;
import org.ecoinformatics.oboe.util.Debugger;



public class QueryGenerator extends DataStatistics{

	private static int scale_num = 100;
	private static double m_selectivity_scale = 0.01;
	
	/**
	 * Calculate the scale of the selectivity
	 * @param accurateSelectivity
	 * @return
	 */
	private static double calSelectivityScale(double accurateSelectivity)
	{
		int scale = (int)(accurateSelectivity*scale_num);
		double roughScale = (scale*1.0)/scale_num;
		
		return roughScale;
	}
	
	private static void putSelectivity(
			Map<Double, List<String> > recordselectivity2condlist,
			double selectivityScale,
			String cond)
	{
		List<String> curSelectivityValueList = recordselectivity2condlist.get(selectivityScale);
		if(curSelectivityValueList==null){				
			curSelectivityValueList = new ArrayList<String>();
			recordselectivity2condlist.put(selectivityScale,curSelectivityValueList);
		}
		curSelectivityValueList.add(cond);
	}
	
	/**
	 * Extract query lists for the given measurements and selectivity
	 * 
	 * @param meas2_rselectivity2condlist
	 * @param recordSelectivity
	 * @return
	 */
	private static Map<String, Map<Double, List<String >>> extractQuery(
			Map<String, Map<Double, List<String >>> meas2_rselectivity2condlist,
			List<Double> recordSelectivity,int qnumPerSelectivity)
	{
		Map<String, Map<Double, List<String >>> meas2_rselectivity2querycondlist =
			new TreeMap<String, Map<Double, List<String >>>();
		
		for(String measLabel:meas2_rselectivity2condlist.keySet()){
			//get one measurement's query list
			Map<Double, List<String> > recordselectivity2querycondlist = 
				extractOneMeasQuery(meas2_rselectivity2condlist.get(measLabel),recordSelectivity,qnumPerSelectivity);
			
			//System.out.println(Debugger.getCallerPosition()+"recordselectivity2querycondlist="+recordselectivity2querycondlist);
			meas2_rselectivity2querycondlist.put(measLabel, recordselectivity2querycondlist);
		}
		
		return meas2_rselectivity2querycondlist;
	}
	
	/**
	 * Get one measurement's query list
	 * @param oneMeasureRecordSelectivity2condList
	 * @param recordSelectivity
	 * @return
	 */
	private static Map<Double, List<String> > extractOneMeasQuery(
			Map<Double, List<String >> oneMeasureRecordSelectivity2condList,
			List<Double> recordSelectivity,int qnumPerSelectivity)
	{
		Map<Double, List<String> > recordselectivity2querycondlist = new TreeMap<Double, List<String>>();
		
		for(int i=0;i<recordSelectivity.size();i++){
			double requiredSelectivity = recordSelectivity.get(i);
			System.out.println(Debugger.getCallerPosition()+"Required selectivity="+requiredSelectivity);
			List<String> requiredQueryCondList = new ArrayList<String>();
			recordselectivity2querycondlist.put(requiredSelectivity, requiredQueryCondList);
			
			while(requiredQueryCondList.size()<qnumPerSelectivity){
				List<String> condList = oneMeasureRecordSelectivity2condList.get(requiredSelectivity);
				if(condList!=null){
					for(int j=0;j<condList.size();j++){
						if(requiredQueryCondList.size()<qnumPerSelectivity){
							requiredQueryCondList.add(condList.get(j));
						}else{
							break;
						}
					}
				}
				if(requiredQueryCondList.size()<=qnumPerSelectivity){
					requiredSelectivity += m_selectivity_scale;
					System.out.println(Debugger.getCallerPosition()+"Need to access selectivity="+requiredSelectivity);
				}
			}
			
		}
		return recordselectivity2querycondlist;
	}
	
	/**
	 * Calculate the selectivity of the query
	 * @param meas2_value2count
	 * @param numberOfRows
	 * @param recordSelectivity
	 * @return
	 */
	private static Map<String, Map<Double, List<String >>> generateQuery(
			Map<String, Map<Integer, Integer>> meas2_value2count, 
			int numberOfRows)
	{
		Map<String, Map<Double, List<String >>> meas2_rselectivity2valuelist =
			new TreeMap<String, Map<Double, List<String >>>();
		
		for(String measLabel: meas2_value2count.keySet()){
			Map<Double, List<String> > recordselectivity2condlist = new TreeMap<Double, List<String>>();
			
			Map<Integer, Integer> value2count = meas2_value2count.get(measLabel); //value: count
			int accumulateCount = 0;
			for(Map.Entry<Integer,Integer> entry: value2count.entrySet()){
				int value = entry.getKey();
				int count = entry.getValue();
				
				//put (selectivityScale,=value) to the map
				double selectivity = (count*(1.0))/numberOfRows;
				double selectivityScale = calSelectivityScale(selectivity);
				putSelectivity(recordselectivity2condlist,selectivityScale,("="+value));
				
				//put (accumulateSelectivityScale,>=value) to the map
				accumulateCount += count;
				double accumulateSelectivity = (accumulateCount*(1.0))/numberOfRows;
				double accumulateSelectivityScale = calSelectivityScale(accumulateSelectivity);
				putSelectivity(recordselectivity2condlist,accumulateSelectivityScale,(">="+value));
				
			}
			meas2_rselectivity2valuelist.put(measLabel, recordselectivity2condlist);
		}
		
		return meas2_rselectivity2valuelist;
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length<3){
			//e.g., java -cp oboe.jar org.ecoinformatics.oboe.QueryGenerator syn 20 0.5
			//System.out.println("Usage: ./QueryGenerator <1. data_file_prefix> <2.file num> <3. attr slectivity double(0,1.0]> <4. list of record selectivity>");
			System.out.println("Usage: ./QueryGenerator <1. data_file_prefix> <2.file num>");
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
			recordSelectivity.add(0.05);
			recordSelectivity.add(0.01);
		}
		int qnumPerSelectivity = 10;
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
			List<String> measToTest = new ArrayList<String>();
			measToTest.add("m5");
			measToTest.add("m6");
			Map<String, Map<Integer, Integer>> meas2_value2count = generator.Statistic(dataFile, measToTest);
			
			
			//3. Compute the query and statistics
			Map<String, Map<Double, List<String >>> meas2_rselectivity2condlist = 
				generateQuery(meas2_value2count, numbOfRows);
			
//			for(String measLabel: meas2_rselectivity2condlist.keySet()){
//				Map<Double, List<String >> selectivity2valuelist = meas2_rselectivity2condlist.get(measLabel);
//				System.out.println(Debugger.getCallerPosition()+"measLabel="+measLabel+",selectivities="+selectivity2valuelist.keySet());
//				for(double selectivity: selectivity2valuelist.keySet()){
//					if(selectivity == 0.01||selectivity==0.1||(selectivity >= 0.5&&selectivity<0.6)){
//						System.out.println(Debugger.getCallerPosition()+"selectivity="+selectivity+":"+selectivity2valuelist.get(selectivity));
//					}
//				}
//			}
			
			//4. extract needed query
			Map<String, Map<Double, List<String >>> meas2_query = 
				extractQuery(meas2_rselectivity2condlist,recordSelectivity,qnumPerSelectivity);
			
			for(String measLabel: meas2_query.keySet()){
				Map<Double, List<String >> selectivity2valuelist = meas2_query.get(measLabel);
				System.out.println(Debugger.getCallerPosition()+"measLabel="+measLabel+",selectivities="+selectivity2valuelist.keySet());
				for(double selectivity: selectivity2valuelist.keySet()){
					System.out.println(Debugger.getCallerPosition()+"selectivity="+selectivity+":"+selectivity2valuelist.get(selectivity));
				}
			}
			
			OMQuery query = new OMQuery();
			query.writeQueries(a,meas2_query,Constant.localUriPrefix+"query");
		}
		
		System.out.println(Debugger.getCallerPosition()+"Finish generating queries for atrselectivity="+attrSelectivity);
	}
	
}
