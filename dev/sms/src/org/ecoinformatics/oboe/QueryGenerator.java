package org.ecoinformatics.oboe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.ecoinformatics.oboe.query.OMQuery;
import org.ecoinformatics.oboe.syntheticdataquery.AnnotationSpecifier;
import org.ecoinformatics.oboe.syntheticdataquery.DataGenerator;
import org.ecoinformatics.oboe.syntheticdataquery.DataStatistics;
import org.ecoinformatics.oboe.util.Debugger;
import org.ecoinformatics.oboe.util.ListComparator;



public class QueryGenerator extends DataStatistics{

	private static int m_value_scale_num = 10000;
	private static double m_value_selectivity_scale = (1.0/m_value_scale_num);
	
	private static String m_annotSpecFilePrefix;
	private static String m_dataFilePrefix;
	
	private static int m_qnum_per_value_selectivity = 10;
	/**
	 * Calculate the scale of the selectivity
	 * @param accurateSelectivity
	 * @return
	 */
	private static double calSelectivityScale(double accurateSelectivity)
	{
		int scale = (int)(accurateSelectivity*m_value_scale_num);
		double roughScale = (scale*1.0)/m_value_scale_num;
		
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
	 * put te selectibity conditions for meultiple measurements
	 * @param recordselectivity2condlist
	 * @param selectivityScale
	 * @param cond
	 */
	private static void putSelectivityForMultiMeas(
			Map<Double, List<List<String> > > recordselectivity2condlist,
			double selectivityScale,
			List<String> cond)
	{
		List<List<String> > curSelectivityValueList = recordselectivity2condlist.get(selectivityScale);
		if(curSelectivityValueList==null){				
			curSelectivityValueList = new ArrayList<List<String> >();
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
	 * @throws Exception 
	 */
	private static Map<String, Map<Double, List<String >>> extractQuery(
			Map<String, Map<Double, List<String >>> meas2_rselectivity2condlist,
			List<Double> recordSelectivity,int qnumPerSelectivity) throws Exception
	{
		Map<String, Map<Double, List<String >>> meas2_rselectivity2querycondlist =
			new TreeMap<String, Map<Double, List<String >>>();
		
		for(String measLabel:meas2_rselectivity2condlist.keySet()){
			//get one measurement's query condition list
			Map<Double, List<String> > recordselectivity2querycondlist = 
				extractOneMeasQuery(meas2_rselectivity2condlist.get(measLabel),recordSelectivity,qnumPerSelectivity);
			
			//System.out.println(Debugger.getCallerPosition()+"recordselectivity2querycondlist="+recordselectivity2querycondlist);
			meas2_rselectivity2querycondlist.put(measLabel, recordselectivity2querycondlist);
		}
		
		return meas2_rselectivity2querycondlist;
	}
	
	/**
	 * Extract the queries for multiple meas (with logic AND)
	 * 
	 * @param measlist2_rselectivity2condlist
	 * @param recordSelectivity
	 * @param qnumPerSelectivity
	 * @return
	 * @throws Exception
	 */
	private static Map<List<String>, Map<Double, List<List<String> >>> extractQueryForMultipleMeas(
			//Map<List<String>, Map<Double, List<List<String> >>> measlist2_rselectivity2condlist,
			Map<List<String>, Object> measlist2_rselectivity2condlistobj,
			List<Double> recordSelectivity,int qnumPerSelectivity) throws Exception
	{
		
		Map<List<String>, Map<Double, List<List<String> >>> measlist2_rselectivity2querycondlist =
			new TreeMap<List<String>, Map<Double, List<List<String> >>>(new ListComparator());
		
		for(List<String> measLabelList: measlist2_rselectivity2condlistobj.keySet()){
			//get one measurement's query condition list
			Map<Double, List<List<String> > > recordselectivity2querycondlist = 
				//extractOneMeasQuery
				extractOneMeasQueryForMultipleMeas(//measlist2_rselectivity2querycondlist.get(measLabelList),
						(Map<Double, List<List<String>>>)measlist2_rselectivity2condlistobj.get(measLabelList),
						recordSelectivity,qnumPerSelectivity);
			
			//System.out.println(Debugger.getCallerPosition()+"recordselectivity2querycondlist="+recordselectivity2querycondlist);
			measlist2_rselectivity2querycondlist.put(measLabelList, recordselectivity2querycondlist);
		}
		
		return measlist2_rselectivity2querycondlist;
	}
	
	/**
	 * Get one measurement's query list
	 * @param oneMeasureRecordSelectivity2condList
	 * @param recordSelectivity
	 * @return
	 * @throws Exception 
	 */
	private static Map<Double, List<String> > extractOneMeasQuery(
			Map<Double, List<String >> oneMeasureRecordSelectivity2condList,
			List<Double> recordSelectivity,int qnumPerSelectivity) throws Exception
	{
		Map<Double, List<String> > recordselectivity2querycondlist = new TreeMap<Double, List<String>>();
		

		for(int i=0;i<recordSelectivity.size();i++){
			//from each needed record selectivity, generate the "qnumPerSelectivity" query conditions 
			double origRequiredSelectivity = recordSelectivity.get(i);
			
			System.out.println(Debugger.getCallerPosition()+"Required selectivity="+origRequiredSelectivity);
			List<String> requiredQueryCondList = new ArrayList<String>();
			recordselectivity2querycondlist.put(origRequiredSelectivity, requiredQueryCondList);
			
			//when there is not enough query conditions, loop
			double requiredSelectivity = origRequiredSelectivity;
			int accumulateLevel = 1;
			while(requiredQueryCondList.size()<qnumPerSelectivity && requiredSelectivity<1.0){				
				List<String> condList = oneMeasureRecordSelectivity2condList.get(requiredSelectivity);
				if(condList!=null){
					for(int j=0;j<condList.size();j++){
						if(requiredQueryCondList.size()<qnumPerSelectivity){
							requiredQueryCondList.add(condList.get(j));
							if(origRequiredSelectivity==0.001){
								System.out.println(Debugger.getCallerPosition()+"real sel.="+requiredSelectivity+":"+condList.get(j));
							}
						}else{
							break;
						}
					}
				}
				if(requiredQueryCondList.size()<=qnumPerSelectivity){
					requiredSelectivity = origRequiredSelectivity+(accumulateLevel*m_value_selectivity_scale);
					if(origRequiredSelectivity==0.001){
						System.out.println(Debugger.getCallerPosition()+"accumulateLevel="+accumulateLevel+",Access sel.="+requiredSelectivity+",orig sel.="+origRequiredSelectivity
								+",requiredQueryCondList.size()="+requiredQueryCondList.size());
					}
					
					if(accumulateLevel>0) accumulateLevel = (-1)*accumulateLevel;
					else accumulateLevel = ((-1)*accumulateLevel)+1;
				}
			}
			
			if(requiredQueryCondList.size()<qnumPerSelectivity){
				throw new Exception("orig sel.="+origRequiredSelectivity+",query size="+requiredQueryCondList.size());
			}
			
		}
		return recordselectivity2querycondlist;
	}
	
	/**
	 * Get one measurement list (AND logic)'s query list
	 * @param oneMeasureRecordSelectivity2condList
	 * @param recordSelectivity
	 * @param qnumPerSelectivity
	 * @return
	 * @throws Exception
	 */
	private static Map<Double, List<List<String> > > extractOneMeasQueryForMultipleMeas(
			Map<Double, List<List<String> >> oneMeasureRecordSelectivity2condList,
			List<Double> recordSelectivity,int qnumPerSelectivity) throws Exception
	{
		Map<Double, List<List<String>> > recordselectivity2querycondlist = new TreeMap<Double, List<List<String>>>();
		

		for(int i=0;i<recordSelectivity.size();i++){
			//System.out.println(Debugger.getCallerPosition()+"i="+i);
			//from each needed record selectivity, generate the "qnumPerSelectivity" query conditions
			try{
				double origRequiredSelectivity = recordSelectivity.get(i);
				
				System.out.println(Debugger.getCallerPosition()+"Required selectivity="+origRequiredSelectivity);
				List<List<String> > requiredQueryCondList = new ArrayList<List<String> >();
				recordselectivity2querycondlist.put(origRequiredSelectivity, requiredQueryCondList);
				
				//when there is not enough query conditions, loop
				double requiredSelectivity = origRequiredSelectivity;
				int accumulateLevel = 1;
				while(requiredQueryCondList.size()<qnumPerSelectivity && requiredSelectivity<1.0){				
					List<List<String> > condList = oneMeasureRecordSelectivity2condList.get(requiredSelectivity);
					if(condList!=null){
						for(int j=0;j<condList.size();j++){
							if(requiredQueryCondList.size()<qnumPerSelectivity){
								requiredQueryCondList.add(condList.get(j));
								if(origRequiredSelectivity==0.001){
									System.out.println(Debugger.getCallerPosition()+"real sel.="+requiredSelectivity+":"+condList.get(j));
								}
							}else{
								break;
							}
						}
					}
					if(requiredQueryCondList.size()<=qnumPerSelectivity){
						requiredSelectivity = origRequiredSelectivity+(accumulateLevel*m_value_selectivity_scale);
						if(origRequiredSelectivity==0.001){
							System.out.println(Debugger.getCallerPosition()+"accumulateLevel="+accumulateLevel+",Access sel.="+requiredSelectivity+",orig sel.="+origRequiredSelectivity
									+",requiredQueryCondList.size()="+requiredQueryCondList.size());
						}
						
						if(accumulateLevel>0) accumulateLevel = (-1)*accumulateLevel;
						else accumulateLevel = ((-1)*accumulateLevel)+1;
					}
				}
				
				if(requiredQueryCondList.size()<qnumPerSelectivity){
					throw new Exception("orig sel.="+origRequiredSelectivity+",query size="+requiredQueryCondList.size());
				}
			}catch(Exception e){
				throw e;
			}
			
		}
		
		//System.out.println(Debugger.getCallerPosition()+"recordselectivity2querycondlist="+recordselectivity2querycondlist);
		return recordselectivity2querycondlist;
	}
	
	/**
	 * Calculate the selectivity of the query
	 * @param meas2_value2count
	 * @param numberOfRows
	 * @param recordSelectivity
	 * @return
	 * @throws Exception 
	 */
	private static Map<String, Map<Double, List<String >>> generateQuery(
			Map<String, Map<Integer, Integer>> meas2_value2count, 
			Map<String,Integer> meas2_totalRecordNumInTb) 
		throws Exception
	{
		Map<String, Map<Double, List<String >>> meas2_rselectivity2valuelist =
			new TreeMap<String, Map<Double, List<String >>>();
		
		for(String measLabel: meas2_value2count.keySet()){
			try{
				Map<Double, List<String> > recordselectivity2condlist = new TreeMap<Double, List<String>>();
				
				Map<Integer, Integer> value2count = meas2_value2count.get(measLabel); //value: count
				int accumulateCount = 0;
				
				//get the total number of rows for tables having this measurement label
				int numberOfRowsThisMeas = meas2_totalRecordNumInTb.get(measLabel);
				if(numberOfRowsThisMeas==0) throw new Exception("numberOfRowsThisMeas is 0");
				
				for(Map.Entry<Integer,Integer> entry: value2count.entrySet()){
					int value = entry.getKey(); 
					int count = entry.getValue();
					
					//put (selectivityScale,=value) to the map
					double selectivity = (count*(1.0))/numberOfRowsThisMeas;
					if(selectivity>1.0) throw new Exception("selectivity>1.0");
					
					//double selectivityScale = calSelectivityScale(selectivity);
					//putSelectivity(recordselectivity2condlist,selectivityScale,("="+value));
					
					//put (accumulateSelectivityScale,>=value) to the map
					accumulateCount += count;
					double accumulateSelectivity = (accumulateCount*(1.0))/numberOfRowsThisMeas;
					if(accumulateSelectivity>1.0) throw new Exception("accumulateSelectivity>1.0");
					double accumulateSelectivityScale = calSelectivityScale(accumulateSelectivity);
					
					//values are ordered ascendingly, so,for this selectivity, the condition should be "<= value"
					putSelectivity(recordselectivity2condlist,accumulateSelectivityScale,("<="+value));
					
				}
				System.out.println(Debugger.getCallerPosition()+"[x]");
				meas2_rselectivity2valuelist.put(measLabel, recordselectivity2condlist);
			}catch(Exception e){
				throw e;
			}
		}
		
		return meas2_rselectivity2valuelist;
	}
	
	private static //Map<List<String>, Map<Double, List<List<String> >>> 
		Map<List<String>, Object> 
		generateQueryForMultipleMeas(
			//Map<List<String>, Map<List<Integer>, Integer>> meas2_value2count, 
			Map<List<String>, Object> meas2_value2countobj,
			Map<List<String>,Integer> meas2_totalRecordNumInTb) 
		throws Exception
	{
		//Map<List<String>, Map<List<Integer>, Integer>> meas2_value2count = 
		//	(Map<List<String>, Map<List<Integer>, Integer>>) meas2_value2countobj;
		//meas list ==> (selectivity ==> value list)
		//Map<List<String>, Map<Double, List<List<String> >>> meas2_rselectivity2valuelist =
			//new TreeMap<List<String>, Map<Double, List<List<String> >>>(new ListComparator());
		Map<List<String>, Object> meas2_rselectivity2valuelist =
			new TreeMap<List<String>, Object>(new ListComparator());
		
		//System.out.println(Debugger.getCallerPosition()+"[a]");
		
		for(List<String> measLabelList: meas2_value2countobj.keySet()){
			Map<Double, List<List<String>> > recordselectivity2condlist = new TreeMap<Double, List<List<String> >>();
			//System.out.println(Debugger.getCallerPosition()+"[b]measLabelList="+measLabelList);
			
			Map<List<Integer>, Integer> valuelist2count = (Map<List<Integer>, Integer>)meas2_value2countobj.get(measLabelList); //value: count
			//System.out.println(Debugger.getCallerPosition()+"[b]valuelist2count="+valuelist2count);
			int accumulateCount = 0;
			
			//get the total number of rows for tables having this measurement label
			int numberOfRowsThisMeas = meas2_totalRecordNumInTb.get(measLabelList);
			if(numberOfRowsThisMeas==0) throw new Exception("numberOfRowsThisMeas is 0");
			
			for(Map.Entry<List<Integer>,Integer> entry: valuelist2count.entrySet()){
				List<Integer> valueList = entry.getKey(); 
				int count = entry.getValue();
				//System.out.println(Debugger.getCallerPosition()+"[c]valueList="+valueList+",count="+count);
				//put (selectivityScale,=value) to the map
				double selectivity = (count*(1.0))/numberOfRowsThisMeas;
				if(selectivity>1.0) throw new Exception("selectivity>1.0");
				
				//double selectivityScale = calSelectivityScale(selectivity);
				//putSelectivity(recordselectivity2condlist,selectivityScale,("="+value));
				
				//put (accumulateSelectivityScale,>=value) to the map
				accumulateCount += count;
				double accumulateSelectivity = (accumulateCount*(1.0))/numberOfRowsThisMeas;
				if(accumulateSelectivity>1.0) throw new Exception("accumulateSelectivity>1.0");
				double accumulateSelectivityScale = calSelectivityScale(accumulateSelectivity);
				
				//values are ordered ascendingly, so,for this selectivity, the condition should be "<= value"
				List<String> condition = new ArrayList<String>();
				for(int j=0;j<valueList.size();j++){
					condition.add("<="+valueList.get(j));
				}
				putSelectivityForMultiMeas(recordselectivity2condlist,accumulateSelectivityScale,condition);
				
			}
			//System.out.println(Debugger.getCallerPosition()+"[d],recordselectivity2condlist="+recordselectivity2condlist);
			
			meas2_rselectivity2valuelist.put(measLabelList, recordselectivity2condlist);
		}
		
		return meas2_rselectivity2valuelist;
	}
	
	
	
	private static void selectivityQueryForMultipleMeas(List<List<String> > measToGenerateQueryFor, 
			List<Double> recordSelectivity,int filenum,
			String queryFilePrefix) 
			throws FileNotFoundException, IOException, Exception
	{
		//Map<List<String>, Map<List<Integer>, Integer>> measlist2_value2count =  
		//	new TreeMap<List<String>, Map<List<Integer>,Integer>>(new ListComparator());
		Map<List<String>, Object> measlist2_value2count =  
			new TreeMap<List<String>, Object>(new ListComparator());
		
		Map<List<String>,Integer> meas2_totalRecordNumInTb = 
			new  TreeMap<List<String>,Integer> (new ListComparator());
		
		for(int i=0;i<filenum;i++){
			//String annotSpecFile = m_annotSpecFilePrefix+"_"+(i+1)+Constant.C_ANNOT_SPEC_FILE_SUFFIX;
			String annotSpecFile = m_annotSpecFilePrefix+Constant.C_ANNOT_SPEC_FILE_SUFFIX;
			String dataFile = m_dataFilePrefix+"-n5000"+ "-d"+(i+1)+Constant.C_DATA_FILE_SUFFIX;
			
			System.out.println(Debugger.getCallerPosition()+"i="+i+",dataFile="+dataFile);
			
			//get data statistics
			//1. Read annotation specification files to annotation structure
			//System.out.println(Debugger.getCallerPosition()+"1. Read annotation specification files ...");
			AnnotationSpecifier a = new AnnotationSpecifier();
			a.readAnnotationSpecFile(annotSpecFile);
			
			//2. Get statistics of the dataset
			//System.out.println(Debugger.getCallerPosition()+"2. Get statistics of the dataset ...");
			DataGenerator generator = new DataGenerator(); 
			generator.setAnnotation(a.getAnnotation());
			generator.setKey2distinctfactor(a.getKey2distinctfactor());
			
			//System.out.println(Debugger.getCallerPosition()+"[1]measlist2_value2count="+measlist2_value2count);
			generator.StatisticForMultipleCondition(dataFile, measToGenerateQueryFor, measlist2_value2count,meas2_totalRecordNumInTb);
			//System.out.println(Debugger.getCallerPosition()+"[2]measlist2_value2count="+measlist2_value2count);
		}
		
		//3. Compute the query and statistics
		//System.out.println(Debugger.getCallerPosition()+"[3]");
		//Map<List<String>, Map<Double, List<List<String> >>> measlist2_rselectivity2condlist = 
		Map<List<String>, Object> measlist2_rselectivity2condlist =
			generateQueryForMultipleMeas(measlist2_value2count, meas2_totalRecordNumInTb);
		//Map<String, Map<Double, List<String >>> meas2_rselectivity2condlist = 
		//	generateQuery(meas2_value2count, meas2_totalRecordNumInTb);
		
//		for(String measLabel: meas2_rselectivity2condlist.keySet()){
//			Map<Double, List<String >> selectivity2valuelist = meas2_rselectivity2condlist.get(measLabel);
//			System.out.println(Debugger.getCallerPosition()+"measLabel="+measLabel+",selectivities="+selectivity2valuelist.keySet());
//			for(double selectivity: selectivity2valuelist.keySet()){
//				if(selectivity == 0.01||selectivity==0.1||(selectivity >= 0.5&&selectivity<0.6)){
//					System.out.println(Debugger.getCallerPosition()+"selectivity="+selectivity+":"+selectivity2valuelist.get(selectivity));
//				}
//			}
//		}
		
		//4. extract needed query
		Map<List<String>, Map<Double, List<List<String> >>> measlist2_query = 
			extractQueryForMultipleMeas(measlist2_rselectivity2condlist,recordSelectivity,m_qnum_per_value_selectivity);
		
		for(List<String> measLabelList: measlist2_query.keySet()){
			Map<Double, List<List<String> >> selectivity2valuelist = measlist2_query.get(measLabelList);
			System.out.println(Debugger.getCallerPosition()+"measLabel="+measLabelList+",selectivities="+selectivity2valuelist.keySet());
			for(double selectivity: selectivity2valuelist.keySet()){
				System.out.println(Debugger.getCallerPosition()+"selectivity="+selectivity+":"+selectivity2valuelist.get(selectivity));
			}
		}
		
		OMQuery query = new OMQuery();
		//query.writeQueries(measlist2_query,Constant.localUriPrefix+"query");
		//query.writeQueriesForMultipleMeas(measlist2_query,Constant.localUriPrefix+"query");		
		query.writeQueriesForMultipleMeas(measlist2_query,Constant.localUriPrefix+queryFilePrefix);
		
	}
	
	private static void allfileSelectivityQuery(List<String > measToGenerateQueryFor, 
			List<Double> recordSelectivity,int filenum) 
			throws FileNotFoundException, IOException, Exception
	{
		Map<String, Map<Integer, Integer>> meas2_value2count =  new TreeMap<String, Map<Integer,Integer>>();
		Map<String,Integer> meas2_totalRecordNumInTb = new  TreeMap<String,Integer>();
		
		for(int i=0;i<filenum;i++){
			String annotSpecFile = m_annotSpecFilePrefix+"_"+(i+1)+Constant.C_ANNOT_SPEC_FILE_SUFFIX;
			String dataFile = m_dataFilePrefix+"_"+(i+1)+"-n5000"+ Constant.C_DATA_FILE_SUFFIX;
			
			//get data statistics
			//1. Read annotation specification files to annotation structure
			//System.out.println(Debugger.getCallerPosition()+"1. Read annotation specification files ...");
			AnnotationSpecifier a = new AnnotationSpecifier();
			a.readAnnotationSpecFile(annotSpecFile);
			
			//2. Get statistics of the dataset
			//System.out.println(Debugger.getCallerPosition()+"2. Get statistics of the dataset ...");
			DataGenerator generator = new DataGenerator(); 
			//generator.setRownum(numbOfRows);
			generator.setAnnotation(a.getAnnotation());
			generator.setKey2distinctfactor(a.getKey2distinctfactor());
			
			generator.Statistic(dataFile, measToGenerateQueryFor, meas2_value2count,meas2_totalRecordNumInTb);
		}
		
		//3. Compute the query and statistics
		Map<String, Map<Double, List<String >>> meas2_rselectivity2condlist = 
			generateQuery(meas2_value2count, meas2_totalRecordNumInTb);
		
//		for(String measLabel: meas2_rselectivity2condlist.keySet()){
//			Map<Double, List<String >> selectivity2valuelist = meas2_rselectivity2condlist.get(measLabel);
//			System.out.println(Debugger.getCallerPosition()+"measLabel="+measLabel+",selectivities="+selectivity2valuelist.keySet());
//			for(double selectivity: selectivity2valuelist.keySet()){
//				if(selectivity == 0.01||selectivity==0.1||(selectivity >= 0.5&&selectivity<0.6)){
//					System.out.println(Debugger.getCallerPosition()+"selectivity="+selectivity+":"+selectivity2valuelist.get(selectivity));
//				}
//			}
//		}
		
		//4. extract needed query
		Map<String, Map<Double, List<String >>> meas2_query = 
			extractQuery(meas2_rselectivity2condlist,recordSelectivity,m_qnum_per_value_selectivity);
		
		for(String measLabel: meas2_query.keySet()){
			Map<Double, List<String >> selectivity2valuelist = meas2_query.get(measLabel);
			System.out.println(Debugger.getCallerPosition()+"measLabel="+measLabel+",selectivities="+selectivity2valuelist.keySet());
			for(double selectivity: selectivity2valuelist.keySet()){
				System.out.println(Debugger.getCallerPosition()+"selectivity="+selectivity+":"+selectivity2valuelist.get(selectivity));
			}
		}
		
		OMQuery query = new OMQuery();
		query.writeQueries(meas2_query,Constant.localUriPrefix+"query");
		
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length<3){
			//e.g., java -cp oboe.jar org.ecoinformatics.oboe.QueryGenerator syn 20 0.5
			System.out.println("Usage: ./QueryGenerator <1. data_file_prefix> <2.file num> <3. query file prefix> [<4. attribute slectivity double(0,1.0]>] ");
			return;
		}
		
		m_annotSpecFilePrefix = Constant.localUriPrefix + args[0];
		m_dataFilePrefix = Constant.localUriPrefix +args[0];
		
		int filenum = Integer.parseInt(args[1]);
		String queryFilePrefix = args[2];
		
		double attrSelectivity = 0.5;
		if(args.length>3){
			attrSelectivity = Double.parseDouble(args[3]);
		}
		
		List<Double> recordSelectivity = new ArrayList<Double>();
		recordSelectivity.add(0.5);
		recordSelectivity.add(0.2);
		recordSelectivity.add(0.1);
		//recordSelectivity.add(0.05);
		recordSelectivity.add(0.01);
		//recordSelectivity.add(0.005);
		recordSelectivity.add(0.001);
		
		System.out.println("\n"+Debugger.getCallerPosition()+"annotSpecFilePrefix="+m_annotSpecFilePrefix);
		System.out.println(Debugger.getCallerPosition()+"dataFilePrefix="+m_dataFilePrefix);
		System.out.println(Debugger.getCallerPosition()+"filenum="+filenum);
		System.out.println(Debugger.getCallerPosition()+"attrSelectivity="+attrSelectivity+",recordSelectivity="+recordSelectivity+"\n");
		
		//Get the measurements that we need to generate query for
		//List<String> measToGenerateQueryFor = new ArrayList<String>();
		List<List<String> > measToGenerateQueryFor = new ArrayList<List<String> >();
		setParam(measToGenerateQueryFor,attrSelectivity);
		System.out.println(Debugger.getCallerPosition()+"measToGenerateQueryFor="+measToGenerateQueryFor);
		
		//for the 1st file, get the data distribution for the attributes with given selectivity
		//allfileSelectivityQuery(measToGenerateQueryFor,recordSelectivity,filenum);
		selectivityQueryForMultipleMeas(measToGenerateQueryFor,recordSelectivity,filenum,queryFilePrefix);
		
		System.out.println(Debugger.getCallerPosition()+"Finish generating queries for atrselectivity="+attrSelectivity);
	}
	
	private static void setParam(List<List<String> > measToGenerateQueryFor, double selectivity)
	{
//		for(int i=0;i<m_measurements.length;i++){
	//		measToGenerateQueryFor.add(m_measurements[i]);
	//	}
		
		/*measToGenerateQueryFor.add("m1"); 
		measToGenerateQueryFor.add("m3"); 
		measToGenerateQueryFor.add("m5"); //for testing purpose
		measToGenerateQueryFor.add("m7"); 
		measToGenerateQueryFor.add("m9");
		measToGenerateQueryFor.add("m11"); 
		*/
		
		if(selectivity==0.001){
		
			List<String> oneLogicAndList001 = new ArrayList<String>();
			oneLogicAndList001.add("m701");
			oneLogicAndList001.add("m702");
			measToGenerateQueryFor.add(oneLogicAndList001); //0.001			
		}else if(selectivity==0.01){
			List<String> oneLogicAndList01 = new ArrayList<String>();
			oneLogicAndList01.add("m101");
			oneLogicAndList01.add("m102");
			measToGenerateQueryFor.add(oneLogicAndList01); //0.01
		}else if(selectivity==0.05){
			List<String> oneLogicAndList05 = new ArrayList<String>();
			oneLogicAndList05.add("m201");
			oneLogicAndList05.add("m202");
			measToGenerateQueryFor.add(oneLogicAndList05); //0.05
		}else if(selectivity==0.1){
			List<String> oneLogicAndList1 = new ArrayList<String>();
			oneLogicAndList1.add("m301");
			oneLogicAndList1.add("m302");
			measToGenerateQueryFor.add(oneLogicAndList1); //0.1
		}else if(selectivity==0.2){
			List<String> oneLogicAndList2 = new ArrayList<String>();
			oneLogicAndList2.add("m401");
			oneLogicAndList2.add("m402");
			measToGenerateQueryFor.add(oneLogicAndList2); //0.2
		}else if(selectivity==0.5){
			List<String> oneLogicAndList5 = new ArrayList<String>();
			oneLogicAndList5.add("m501");
			oneLogicAndList5.add("m502");
			measToGenerateQueryFor.add(oneLogicAndList5); //0.5
		}
	}
	
}
