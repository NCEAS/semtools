package org.ecoinformatics.oboe.syntheticdata;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.oboe.util.ObservationUtil;
import org.ecoinformatics.oboe.Constant;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.Comparator;


class ListComparator implements Comparator
{

	public int compare(Object o1, Object o2) {
		
		ArrayList list1 = (ArrayList)o1;
		ArrayList list2 = (ArrayList)o2;
		if(list1.size()!=list2.size()){
			return (list1.size()-list2.size());
		}else{
			for(int i=0;i<list1.size();i++){
				if(!list1.get(i).equals(list2.get(i))){
					Integer v1 = (Integer)(list1.get(i));
					Integer v2 = (Integer)(list2.get(i));
					return (v1.compareTo(v2));
				}
			}
		}
		return 0;
	}
}

public class DataGenerator {

	private Random m_randnumGenerator; 
	
	private Annotation m_annotation = null; //userinput
	private int m_rownum = 0; //user input
	private Map<String, Float> m_key2distinctfactor = null; //user input
	
	private List m_dataset = null; 
	private List<String> m_rowStruct = null;
	private Map<String, List> m_obsType2obsData = null;
	private Map<Observation, Set> m_obsType2contexObsType= null;
	
	public DataGenerator()
	{
		m_randnumGenerator = new Random(0);
		m_obsType2obsData = new TreeMap<String, List>();
		m_obsType2contexObsType= new TreeMap<Observation,Set>();
	}
	
	public Map<String, Float> getKey2distinctfactor() {
		return m_key2distinctfactor;
	}

	public void setKey2distinctfactor(Map<String, Float> key2distinctfactor) {
		this.m_key2distinctfactor = key2distinctfactor;
	}

	public Annotation getAnnotation() {
		return m_annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.m_annotation = annotation;
	}
	
	public int getRownum() {
		return m_rownum;
	}

	public void setRownum(int rownum) {
		this.m_rownum = rownum;
	}

	/**
	 * Get all the measurement in a set ordered by the measurement label
	 * 
	 * @return
	 */
	private Set<Measurement> calMeasurementSet()
	{
		Set<Measurement> measurementSet = new TreeSet<Measurement>();
		
		for(Observation obsType: m_annotation.getObservations()){
			List<Measurement> curObsMeasurementList = obsType.getMeasurements();
			measurementSet.addAll(curObsMeasurementList);				
		}
		//System.out.println("All measurements: " + measurementSet);
		return measurementSet;		
	}
	
	private void calObsType2ContextObsType()
	{
		for(Observation obsType: m_annotation.getObservations()){
			Set<String> contextObservationSet = getContextKeyObservation(obsType);
			m_obsType2contexObsType.put(obsType,contextObservationSet);		
		}
	}
	
	public boolean ValidateInput()
	{
		boolean valid = true;
		
		//1. first compute the map from the observation type to its context observation types
		calObsType2ContextObsType();
		
		//2. check the validity of the relationships of the factors 
		Iterator<Entry<Observation,Set> > iter = m_obsType2contexObsType.entrySet().iterator();
		while(iter.hasNext()){
			Entry<Observation,Set> entry = iter.next();
			float keyDistinctFactor = this.m_key2distinctfactor.get(entry.getKey().getLabel());
			for(Object contextObsLabel: entry.getValue()){
				float contextObsdistinctFactor = m_key2distinctfactor.get(contextObsLabel.toString());
				if(contextObsdistinctFactor > keyDistinctFactor){
					System.out.println("Invalid: entry="+entry +", contextObsLabel="+contextObsLabel+", contextObsdistinctFactor="+contextObsdistinctFactor);
					valid = false;
				}
			}
		}
		
		//3. check the annotation validity
		//TODO this need to be done in the Annotation class
		
		return valid;
	}
	
	/**
	 * Generate data satisfying the annotation specifications. 
	 * This is a bottom-up method, 
	 * It first calculates the instances for the most independent observations types
	 * Then calculate the instances for the least independent observation types
	 * 
	 * This method cannot guarantee the generated key measurements satisfy the uniqueness factor. 
	 * (1) For observations without context key measurements
	 *     The number of unique key-measurements values/total number of measurement values =  the uniqueness factor.
	 * (2) For observations with context key measurements
	 *     The number of unique key-measurements values/total number of measurement values >= the uniqueness factor
	 *     
	 * @deprecated
	 * @throws Exception
	 * 
	 */
	public void GenerateBottomUp() throws Exception
	{
		//1. validae the input 
		boolean valid = ValidateInput();
		if(!valid){
			throw new Exception("Input is invalid.");
		}
		
		//2. get the order of observation types for which to generate data: 
		//   from most independent observation types to most dependent observation types 
		System.out.println("m_obsType2contexObsType="+m_obsType2contexObsType);
		List<Observation> contextObsList = compOrderedObsList();
		System.out.println("contextObsList="+contextObsList);
		
		//3. Generate data for observation types according to the order
		for(Observation obsType: contextObsList){
			generateOneObsData(obsType);
		}
		
		flattenData();
	}
	
	/**
	 * Generate data satisfying the annotation specifications. 
	 * This is a top-down method
	 * It calculates the instances for the most dependent observations types
	 * Then calculate the instances for the least dependent observation types
	 * 
	 * Given situations:
	 * o1 (m1)
	 * o2 (m2) --> o1 (m1) --> (m1, m2)
	 * o3 (m3) --> o1 (m1) --> (m1, m3)
	 * o4 (m4) --> o1, o2 ,o3 --> (m1, m2, m3)
	 * 
	 * We should start from o4 (m1, m2, m3, m4) --> from (m1, m3) + m2 + m4
	 * 
	 * order to generate instances for 
	 * (m1, m2, m3, m4) --> (m1, m2, m3) unique 80% + m4
	 * (m1, m2, m3) 80% unique -- > (m1, m3) 70% + (m1,m2) 60% + (m1)50%  (ordered according to the unique rate descendingly)
	 * (m1, m3) 70% --> (m1) 50% 
	 * (m1, m2) 60% --> (m1) 50%
	 * (m1) 50% --> null. 
	 * 
	 * if S1 is a subset of S2, the valid input:
	 * unique rate of S1 <= unique rate of S2; otherwise, it does not work.  
	 * @throws Exception
	 */
	public void GenerateTopDown() throws Exception
	{
		//1. validae the input 
		boolean valid = ValidateInput();
		if(!valid){
			throw new Exception("Input is invalid.");
		}
		
		//2. get the order of observation types for which to generate data: 
		//   from most independent observation types to most dependent observation types 
		List<Observation> contextObsList = compOrderedObsList();
		//System.out.println("m_obsType2contexObsType="+m_obsType2contexObsType);
		//System.out.println("contextObsList="+contextObsList);
		
		Map<String,List> measurement2ValueList = new TreeMap<String, List>();
		
		//3. Generate rows with unique values
		for(Observation obsType: contextObsList){
			generateUniqueRow(obsType,measurement2ValueList);			
		}
		//System.out.println("measurement2uniqueRowValueSet="+measurement2ValueList);
		
		//4. Generate rows with non-unique values
		for(int i=contextObsList.size()-1; i>=0; i--){
			Observation obsType = contextObsList.get(i);
			generateNonUniqueRow(obsType,measurement2ValueList); 
		}
		
		//4. Generate values for the measurement types that does not have any uniqueness restriction
		for(Observation obsType: contextObsList){
			generateNonKeyCols(obsType,measurement2ValueList);
		}
		
		boolean rc = Validate(measurement2ValueList);
		System.out.println("\nVALID value = "+rc);
		
		flattenDataTopDown(measurement2ValueList);
	}
	
	
	
	/**
	 * Output the generated synthetic data to the given file
	 * 
	 * @param outDataFileName
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public void WriteData(String outDataFileName) throws FileNotFoundException, Exception
	{
		PrintStream dataPrintStream = new PrintStream(outDataFileName);
		WriteDataset(dataPrintStream);
		dataPrintStream.close();
		
		System.out.println("Data is written to "+outDataFileName);		
	}
	
	/**
	 * Read data from the given data file
	 * 
	 * @param inDataFileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public Map<String, List> readData(String inDataFileName) throws FileNotFoundException,IOException, Exception
	{
		
		BufferedReader bufferedReader = null; 
		bufferedReader = new BufferedReader(new FileReader(inDataFileName));
		
		Map<String, List> measurement2valueList = readData(bufferedReader);
		
		if (bufferedReader != null) bufferedReader.close();		
		
		return measurement2valueList;
	}
	
	/**
	 * Read data from the given buffered reader
	 * 
	 * @param r
	 * @throws Exception
	 */
	private Map<String, List> readData(BufferedReader r)
		throws Exception
	{
		String line = null;
		
		// Calculate the set of the measurements for this annotation
		Set<Measurement> measurementSet = calMeasurementSet();
		
		// Get the total number of column number
		int colnum = measurementSet.size();
		
		// Read data file, and make sure each row contains exactly colnum values
		try {
			int lineno = 0;
			while((line = r.readLine())!=null){
				//if((lineno)<=10){
				//	System.out.println(line);
				//}
				if(lineno>0){ //skip the first row because it's the row structure
					extractOneRow(line,colnum);
				}
				++lineno;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		
		// Structure data 
		Map<String, List> measurement2valueList = structureData(measurementSet);
		
		return measurement2valueList;
	}
	
	/**
	 * From one line in the dataset, extract one row, it need to contain "colnum" values
	 * 
	 * @param line
	 * @param colnum
	 * @throws Exception
	 */
	private void extractOneRow(String line,int colnum)
		throws Exception
	{
		if(m_dataset==null)
			m_dataset = new ArrayList();
		
		String[] oneRow = line.split(Constant.C_DATASET_SEPARATOR);
		if(oneRow.length!=colnum){
			throw new Exception("Line ="+line +" does not contain "+colnum+" columns");
		}
		List<Integer> row = new ArrayList<Integer>();
		for(int i=0;i<oneRow.length;i++){
			row.add(Integer.parseInt(oneRow[i]));
		}
		m_dataset.add(row);
	}
	
	/**
	 * 
	 * @param inDataFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public boolean Validate(String inDataFile) throws FileNotFoundException, IOException, Exception
	{
		// Read data		
		Map<String, List> measurement2ValueList = readData(inDataFile);
		
		if(measurement2ValueList==null||measurement2ValueList.size()==0)
			return true;
		
		// Validate data
		boolean rc = Validate(measurement2ValueList);
		return rc;
	}
	
	
	/**
	 * Internally validate all the observation key measurements
	 * @param measurement2ValueList
	 * @return
	 * @throws Exception
	 */
	private boolean Validate(Map<String, List> measurement2ValueList) throws Exception
	{
		boolean rc = true;
		
		for(Observation obsType: this.m_annotation.getObservations()){
			float factor = m_key2distinctfactor.get(obsType.getLabel());
			List<Measurement> keyMeasurementList = ObservationUtil.getAllKeyMeasurement(obsType);
			Collections.sort(keyMeasurementList);
			
			boolean rcTmp = ValidateObsKeyMeasurements(factor,keyMeasurementList, measurement2ValueList);
			if(!rcTmp){
				rc = false;
				System.out.println("obsType ="+ obsType.getLabel() +" is INVALID");
			}
		}
		return rc;
	}
	
	/**
	 * Validate the data of one observation type
	 * @param factor
	 * @param keyMeasrementLists
	 * @param measurement2ValueList
	 * @return
	 * @throws Exception
	 */
	private boolean ValidateObsKeyMeasurements(float factor,
			List<Measurement> keyMeasurementList,
			Map<String, List> measurement2ValueList) throws Exception
	{
		if(keyMeasurementList.size()==0){
			return true;
		}
		Set<List<Integer>> distinctKeyRowSet = new TreeSet<List<Integer>>(new ListComparator());
		
		for(int rowno=0;rowno<this.m_rownum;rowno++){
			List<Integer> newrow = new ArrayList<Integer>();
			for(Measurement m: keyMeasurementList){
				List valueList = measurement2ValueList.get(m.getLabel());
				if(rowno>=valueList.size()){
					throw new Exception("rowno = " + rowno +">= valueList size="+valueList.size());
				}
				int colVal = (Integer)valueList.get(rowno);
				newrow.add(colVal);
			}
			
			distinctKeyRowSet.add(newrow);
		}
		
		int shouldDistinctKeyRowNum = (int)(m_rownum*factor);
		
		//System.out.println("shouldDistinctKeyRowNum="+shouldDistinctKeyRowNum);
		//System.out.println("distinctKeyRowSet="+distinctKeyRowSet);
		if(distinctKeyRowSet.size()==shouldDistinctKeyRowNum){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * Calculate a list of observation types which are ordered according their dependence to others
	 * The most dependent one are ordered earlier than the ones depending on others.
	 * E.g., 	o1 e1 distinct:m1 key,0.2
				o2 e2 distinct:m2 key,m3,0.3
				o3 e3:m4 key,m5 key,0.5:o1 identifying
				o4 e4 distinct:m6 key,m7,0.6:o1 identifying,o2 identifying
				Since o1 and o2 does not depend on other observation types
				they are ordered at an earlier position 
				o3 depends on o1, it is ordered after o1 and o2
				o4 depends on o2, it is ordered after o1, o2, and o3 
	 * @return
	 * @throws Exception
	 */
	private List compOrderedObsList()
		throws Exception
	{
			
		List<Observation> contextObsList = new ArrayList<Observation> ();
		List<Observation> thisLoopChange = new ArrayList<Observation>();
		thisLoopChange.add(null);
		
		int looptime = 0;
		while((thisLoopChange.size()>0||looptime>m_annotation.getObservations().size())&&m_obsType2contexObsType.size()>0){
			thisLoopChange.clear();
			
			//Step 1: get the observation types that do not have any context observation types. 
			Set<String> todelObs = new HashSet<String>();
			Iterator<Entry<Observation,Set> > iter = m_obsType2contexObsType.entrySet().iterator();
			while(iter.hasNext()){
				Entry<Observation,Set> entry = iter.next();
				
				if(entry.getValue().size()==0){
					//this observation does not have context observation types
					thisLoopChange.add(entry.getKey());
					todelObs.add(entry.getKey().getLabel());
				}
			}
			
			//Step 2: remove the observation types that we calculated in the first step from the map 
			for(Observation obs: thisLoopChange){
				m_obsType2contexObsType.remove(obs);
			}
			
			//Step 3: from the map, remove the context references that other observations types have to these "toDELete" observation types.
			if(todelObs.size()>0){
				for(String todel: todelObs){
					Iterator<Entry<Observation,Set> > iter2 = m_obsType2contexObsType.entrySet().iterator();
					while(iter2.hasNext()){
						Entry<Observation,Set> entry = iter2.next();
						entry.getValue().remove(todel);
					}
				}
			}
			contextObsList.addAll(thisLoopChange);		
			looptime ++;
		}
		
		if(looptime>m_annotation.getObservations().size()){
			throw new Exception("cannot get correct order to generate data.\n");
		}
		
		//since the map is removed gradually, need to recalculate it.
		calObsType2ContextObsType();
		
		return contextObsList;
	}
	
	/**
	 * For the non-key measurement columns, randomly generate values 
	 * 
	 * @param obsType
	 * @param ioMeasurement2ValueList
	 * @throws Exception
	 */
	private void generateNonKeyCols(Observation obsType, 
			Map<String,List> ioMeasurement2ValueList)
		throws Exception
	{
		List<Measurement> measurementList = obsType.getMeasurements();
		for(int j=0;j<measurementList.size();j++){
			Measurement m = measurementList.get(j);
			
			//get this measurement's value list
			List measColValueList =  ioMeasurement2ValueList.get(m.getLabel());
			
			//when it is null, it means that we did not generate any values for this measurement yet
			//these measurements are generally non-key measurements
			if(measColValueList==null){
				measColValueList = new ArrayList<Integer>();
				while(measColValueList.size()<this.m_rownum){
					int val = this.m_randnumGenerator.nextInt(2*m_rownum);
					measColValueList.add(val);
				}
				//after we generate the random values for this measurement, put it into the map
				ioMeasurement2ValueList.put(m.getLabel(), measColValueList);
			}else{
				//when we already generate some values for this measurement, they need to have "m_rownum" values.
				if(measColValueList.size()!=m_rownum){
					throw new Exception("Key measurement has less than m_rownum values!");
				}
			}
		}		
	}
	
	
	/**
	 * Generate non-unique measurement values for observation type "obsType"
	 * test case 1: (m1, m2) 
	 * test case 2: (m1, m2) exists, now generate (m1, m3)
	 * test case 3: (m1, m2) exists, (m2, m4) exists, now generate (m1, m2, m3)
	 * @param obsType
	 * @param ioMeasurement2uniqueRowValueSet
	 * @throws Exception
	 */
	private void generateNonUniqueRow(Observation obsType, 
			Map<String,List> ioMeasurement2uniqueRowValueSet)
		throws Exception
	{

		List<Measurement> keyMeasurementList = ObservationUtil.getAllKeyMeasurement(obsType);//obsType.getKeyMeasurements();

		if(keyMeasurementList.size()==0){
			return;
		}
		
		//1. get the minimum exist unique column size
		int oldMinRowNum = -1; 
		int oldMaxRowNum = -1;
		for(int j=0;j<keyMeasurementList.size();j++){
			Measurement m = keyMeasurementList.get(j);
			List distinctKeyColList =  ioMeasurement2uniqueRowValueSet.get(m.getLabel());
			if(oldMinRowNum==-1){
				oldMinRowNum = oldMaxRowNum = distinctKeyColList.size();
			}else{
				if(oldMinRowNum>distinctKeyColList.size()){					
					oldMinRowNum = distinctKeyColList.size();
				}
				if(oldMaxRowNum<distinctKeyColList.size()){
					oldMaxRowNum = distinctKeyColList.size();
				}
			}
		}
		//System.out.println("oldMinRowNum="+oldMinRowNum +", oldMaxRowNum="+oldMaxRowNum);
		
		//2. calculate the new rows which are the same with some old existing rows
		ArrayList<ArrayList<Integer>> newRowList = new ArrayList();
		int newRowNum = this.m_rownum - oldMinRowNum;
		if(oldMinRowNum<0){
			newRowNum = this.m_rownum;
		}
		
		while(newRowList.size()<newRowNum){
			
			ArrayList<Integer> newRow = null;
			
			
			//some column row value exists (should not be changed because they are key values), some column row value does not exist
			//based on the existing values get the row index			
			if((newRowList.size()+oldMinRowNum) < oldMaxRowNum){
				//retrieve the row index between [0, oldMinRowNum) with the same existing column values 
				int curRowNo = newRowList.size()+oldMinRowNum;
				int index = searchRowIndex(keyMeasurementList,ioMeasurement2uniqueRowValueSet,curRowNo,oldMinRowNum);
				if(index<0){
					throw new Exception("Cannot find existing index, somewhere is wrong.");
				}
				//these are rows with existing values
				newRow = retrieveExistRow(keyMeasurementList,index,ioMeasurement2uniqueRowValueSet);
			}else{//i.e., (newRowList.size()+oldMinRowNum) >= oldMaxRowNum
				//generate a random number in [0, oldMaxRowNum)
				int index = this.m_randnumGenerator.nextInt(oldMaxRowNum);
				
				//get a new row for the key measurements from the existing row values col[index] 
				
				if(index<oldMinRowNum){
					//these are rows with existing values
					newRow = retrieveExistRow(keyMeasurementList,index,ioMeasurement2uniqueRowValueSet);
				}else{
					//these are rows which contain some old existing values (need to be consistent) and some newly added ones
					newRow = newRowList.get(index-oldMinRowNum); 
				}				
			}
			
			
			newRowList.add(newRow);
			//generate a random number in [0, oldMaxRowNum)
			//int index = this.m_randnumGenerator.nextInt(oldMaxRowNum);
			
			//some column row value exists (should not be changed because they are key values), some column row value does not exist
			//based on the existing values get the row index
			//int fullRowMaxIndex = oldMinRowNum+newRowList.size();
			//if((index>=oldMinRowNum) && (fullRowMaxIndex < oldMaxRowNum))
			//if((index>=oldMinRowNum))
			//{
				//retrieve the row index between [0, oldMinRowNum) with the same existing column values 
			//	index = searchRowIndex(keyMeasurementList,ioMeasurement2uniqueRowValueSet,newRowList.size(),oldMinRowNum);
			//	if(index<0){
			//		throw new Exception("Cannot find existing index, somewhere is wrong.");
			//	}
			//}
		}
		
		//3. from the new rows, write back the measurement value list
		updMeasurementList(ioMeasurement2uniqueRowValueSet,keyMeasurementList,newRowList,oldMinRowNum);		
	}
	
	/**
	 * based on the knows column values for row "curRowNo", search from the past rows to get the index of row
	 * such that this row has all the column values
	 * 
	 * E.g., for three measurements
	 * m1 m2 m3 
	 * 1 3 2
	 * 1 4 2
	 * 1 4 3
	 * 2 5 2
	 * 2 4 2
	 * 1 ? 2 
	 * ? ? 2 
	 * for curRowNo=5, the existing values for columns (m1 and m3) are 1 and 2
	 * Then, the previous existing rows with id=0 (1 3 2) or id=1 (1 4 2) can be a candidate to be copied here.
	 * Then, the return value can be 0 or 1
	 * 
	 * for curRowNo = 6, the existing values for columns (m3) is 2
	 * Then, the previous existing rows with id = 0,1,3,4 all satisfy this condition.
	 * The return value can be one of them. 
	 * 
	 * @param keyMeasurementList
	 * @param measurement2uniqueRowValueSet
	 * @param curRowNo
	 * @param oldMinRowNum
	 * @return
	 */
	private int searchRowIndex(List<Measurement> keyMeasurementList,
			Map<String,List> measurement2uniqueRowValueSet,
			int curRowNo,
			int oldMinRowNum)
	{
		int index = -1;
		
		Set<Integer> availableRowIdSet = new HashSet<Integer>();
		
		for(int j=0;j<keyMeasurementList.size();j++){
			Measurement m = keyMeasurementList.get(j);
			List distinctKeyColList =  measurement2uniqueRowValueSet.get(m.getLabel());
			
			if(distinctKeyColList.size()>curRowNo){ //this column has enough value;
				int colVal = (Integer)distinctKeyColList.get(curRowNo);
				Set<Integer> thisColRowId = calRowIdwithColVal(distinctKeyColList,colVal);
				
				//get the row id-s that is smaller than the oldMinRowNum
				Set<Integer> validColRowId = new HashSet<Integer>(); 
				for(int rowid: thisColRowId){
					if(rowid<oldMinRowNum){
						validColRowId.add(rowid);
					}
				}
				
				//get the intersection of all the valid row ids for the columns with existing values
				if(availableRowIdSet.size()==0){
					availableRowIdSet.addAll(validColRowId);
				}else{
					//get the rowId intersection of the existing set and the new set
					availableRowIdSet.retainAll(validColRowId);					
				}
			}
		}
		
		//get the random row id index.
		if(availableRowIdSet.size()==0){
			return index;
		}else{
			int tmpIdx = this.m_randnumGenerator.nextInt(availableRowIdSet.size());
			int i=0;
			for(Integer x:availableRowIdSet){
				if(i==tmpIdx){
					index = x;
					break;
				}else{
					i++;
				}
			}
		}
		
		return index;
	}
	
	/**
	 * From a list of values get the indexes such that valList[index] = testVal
	 * 
	 * @param valList
	 * @param testVal
	 * @return
	 */
	private Set<Integer> calRowIdwithColVal(List valList, int testVal)
	{
		Set<Integer> thisColRowId = new HashSet<Integer>();
		for(int i=0;i<valList.size();i++){
			if(testVal== ((Integer)valList.get(i)).intValue()){
				thisColRowId.add(i);
			}
		}
		return thisColRowId;
	}
	
	/**
	 * get a row for the given measurements, whose value is the value at the related measurement list[index]
	 * 
	 * FIXME: clarify more the explanation
	 * @param keyMeasurementList
	 * @param index
	 * @param ioMeasurement2RowValueList
	 * @return
	 * @throws Exception
	 */
	private ArrayList<Integer> retrieveExistRow(List<Measurement> keyMeasurementList,int index,
			Map<String,List> ioMeasurement2RowValueList)
			throws Exception
	{
		ArrayList<Integer> resultObsKeyRow = new ArrayList<Integer>();
		for(int j=0;j<keyMeasurementList.size();j++){					
			Measurement m = keyMeasurementList.get(j);
			List keyColList =  ioMeasurement2RowValueList.get(m.getLabel());
			
			if(keyColList==null||keyColList.size()<=index){
				throw new Exception("keyColList==null||keyColList.size()<=index");
			}
			int oldColVal = (Integer)keyColList.get(index);
			resultObsKeyRow.add(oldColVal);
		}
		
		return resultObsKeyRow;
	}
	
	/**
	 * Use the rowList to update measurement's row value list
	 * 
	 * @param ioMeasurement2uniqueRowValueSet
	 * @param keyMeasurementList
	 * @param rowList
	 * @throws Exception
	 */
	private void updMeasurementList(Map<String,List> ioMeasurement2RowValueList, 
			List<Measurement> keyMeasurementList,
			ArrayList<ArrayList<Integer>> rowList,
			int startIdx)
		throws Exception
	{
		for(int i=0;i<rowList.size();i++){
			ArrayList<Integer> obsKeyRow = rowList.get(i);
			for(int j=0;j<keyMeasurementList.size();j++){					
				int newColVal = obsKeyRow.get(j);
					
				Measurement m = keyMeasurementList.get(j);
				List colValueList =  ioMeasurement2RowValueList.get(m.getLabel());
				if(colValueList!=null){ 
					//the unique value list already exists, 
					//need to see whether (1) exist ones should be the same, or (2) add the new ones 
					if((startIdx+i) < colValueList.size()){//
						int oldColVal = (Integer)colValueList.get(startIdx+i);
						if(newColVal!=oldColVal){
							throw new Exception("newColVal!=oldColVal for m="+m.getLabel() +" row i="+(startIdx+i));
						}
					}else if((startIdx+i)== colValueList.size()){
						colValueList.add(newColVal);
					}else{
						throw new Exception("Missing unique vlaues for measurement when I am at m="+m.getLabel() +" row i="+i);
					}
				}else{//the unique value list does not exist yet,add this list
					colValueList = new ArrayList();					
					colValueList.add(newColVal);	
					ioMeasurement2RowValueList.put(m.getLabel(), colValueList);
				}
			}
		}
	}
	
	/**
	 * Generate unique measurement values for observation type "obsType"
	 * 
	 * @param obsType
	 * @param ioMeasurement2uniqueRowValueSet
	 */
	private void generateUniqueRow(Observation obsType, 
			Map<String,List> ioMeasurement2uniqueRowValueSet)
		throws Exception
	{
		float factor = m_key2distinctfactor.get(obsType.getLabel());
		List<Measurement> keyMeasurementList = ObservationUtil.getAllKeyMeasurement(obsType);
		
		//no key measurements
		if(keyMeasurementList.size()==0){
			return;
		}
		
		ArrayList<ArrayList<Integer>> distinctKeyRowList = new ArrayList();
		Set<ArrayList<Integer>> distinctKeyRowSetIndex = new HashSet<ArrayList<Integer>>();
		int distinctKeyRowNum = (int)(m_rownum*factor);
		
		//1. generate "distinctKeyRowNum" rows of results
		while(distinctKeyRowList.size()<distinctKeyRowNum){
			ArrayList<Integer> obsKeyRow = new ArrayList<Integer>();
			//System.out.println("distinctKeyRowList.size()="+distinctKeyRowList.size());
			for(int j=0;j<keyMeasurementList.size();j++){
				Measurement m = keyMeasurementList.get(j);
				List distinctKeyColList =  ioMeasurement2uniqueRowValueSet.get(m.getLabel());
				int colVal = -1;
				//this key measurement already has unique data
				if(distinctKeyColList!=null){
					//when this unique value already exists, just get this unique value
					int index = distinctKeyRowList.size();
					if(distinctKeyRowList.size() >=distinctKeyColList.size()){						
						//otherwise, randomly get an existing value 
						index = this.m_randnumGenerator.nextInt(distinctKeyColList.size());			
					}
					colVal = (Integer)distinctKeyColList.get(index);						
				}else{
					//this key measurement does not have existing unique data yet
					colVal = m_randnumGenerator.nextInt(2*m_rownum);
				}
				obsKeyRow.add(colVal); //add this column value to this key row					
			}
			
			//if this key row is unique compared with existing row value, add it; otherwise, do nothing
			if(!distinctKeyRowSetIndex.contains(obsKeyRow)){
				distinctKeyRowList.add(obsKeyRow);
				distinctKeyRowSetIndex.add(obsKeyRow);
				//updDistinctKeyRowList(distinctKeyRowList,obsKeyRow);
			}
		}
		
		//2. based on this unique measurement key set, update the related measurement value list
		updMeasurementList(ioMeasurement2uniqueRowValueSet,keyMeasurementList,distinctKeyRowList,0);		
	}
	
	/**
	 * if this key row is unique compared with existing row value, add it; otherwise, do nothing
	 * FIXME: this is linear scan, too slow, maybe use set to do the checking in the caller (did not call this)
	 * @param ioDistinctKeyRowList
	 * @param obsKeyRow
	 * @deprecated
	 */
	private void updDistinctKeyRowList(ArrayList<ArrayList<Integer>> ioDistinctKeyRowList, ArrayList<Integer> newRow)
		throws Exception
	{
		for(int i=0;i<ioDistinctKeyRowList.size();i++){
			ArrayList<Integer> oldRow = ioDistinctKeyRowList.get(i);
			if(oldRow.size()!=newRow.size()){
				throw new Exception("oldRow size is not equal to newRow size.");
			}
			
			//check whether the oldRow equals to the newRow or not.
			boolean sameRow = isSameRow(oldRow,newRow); 
			if(sameRow)
				return;
		}
		
		//no existing rows are the same with this one
		ioDistinctKeyRowList.add(newRow);		
	}
	
	/**
	 * Check whether two rows are the same or not. 
	 * If they are the same, return true;
	 * otherwise, return false. 
	 * 
	 * The caller need to make sure that their sizes are the same. 
	 * FIXME: move this to a utility package
	 * 
	 * @param row1
	 * @param row2
	 * @deprecated
	 * @return
	 */
	boolean isSameRow(ArrayList<Integer> row1, ArrayList<Integer> row2)
	{	
		for(int j=0;j<row1.size();j++){
			if(row1.get(j)!=row2.get(j))
				return false;
		}
		return true;
	}
	
	/**
	 * Make sure one observation should have higher distinct factor than its context observations
	 * 
	 * @deprecated For bottom up method
	 * @param obsType
	 * @throws Exception
	 */
	private void generateOneObsData(Observation obsType) throws Exception
	{
		float factor = m_key2distinctfactor.get(obsType.getLabel());
		List<Measurement> measurements = obsType.getMeasurements();
		List<Measurement> keyMeasurements = obsType.getKeyMeasurements();
		ArrayList<ArrayList<Integer>> obsData = new ArrayList();
		
		//1. No key measurements, it's easy
	 	if(keyMeasurements==null||keyMeasurements.size()==0){
	 		for(int rowno = 0;rowno<m_rownum;rowno++){
	 			ArrayList<Integer> obsrow = GenerateARow(obsType.getMeasurements().size());
				obsData.add(obsrow);
	 		}
		}else{//2. Has key measurements
			//generate the list of distinct key rows
			ArrayList<ArrayList<Integer>> distinctKeyRowList = new ArrayList();
			int distinctKeyRowNum = (int)(m_rownum*factor);
			
			if((obsType.getContexts()==null)||(obsType.getContexts().size()==0)){
				Set<ArrayList<Integer>> distinctKeyRowSet = new TreeSet<ArrayList<Integer>>(new ListComparator());			
				while(distinctKeyRowSet.size()<distinctKeyRowNum){				
					ArrayList<Integer> obsKeyRow = GenerateARow(keyMeasurements.size());
					distinctKeyRowSet.add(obsKeyRow);
				}
				distinctKeyRowList.addAll(distinctKeyRowSet);
				
			}else{
				Set<ArrayList<Integer>> distinctKeyRowSet = new TreeSet<ArrayList<Integer>>(new ListComparator());			
				for(int i=0;i<distinctKeyRowNum;i++){
					ArrayList<Integer> directObsKeyRow = GenerateARow(keyMeasurements.size());
					List contextKeyRow = getContextKeyRow(obsType, i);
					if(contextKeyRow==null||contextKeyRow.size()==0){
						throw new Exception("contextKeyRow is null or size is zero."); 
					}
					directObsKeyRow.addAll(contextKeyRow);
					while(distinctKeyRowSet.contains(contextKeyRow)){
						directObsKeyRow = GenerateARow(keyMeasurements.size());						
						directObsKeyRow.addAll(contextKeyRow);
					}
					distinctKeyRowList.add(directObsKeyRow); //put it to the list following other context columns order
					distinctKeyRowSet.add(directObsKeyRow); //set as an index to facilitate quick search
				}
				
			}

			//generate a row
			for(int rowno=0;rowno<m_rownum;rowno++){
				ArrayList<Integer> obsrow = new ArrayList<Integer>();
				
				int distinctRowIdx = (rowno<distinctKeyRowNum)?rowno:m_randnumGenerator.nextInt(distinctKeyRowNum);
				obsrow.addAll(distinctKeyRowList.get(distinctRowIdx));
				
				ArrayList<Integer> extraColVals = GenerateARow(measurements.size()-keyMeasurements.size());
				if(extraColVals!=null){
					obsrow.addAll(extraColVals);
				}
				obsData.add(obsrow);
			}
			
		}
	 	
	 	m_obsType2obsData.put(obsType.getLabel(), obsData);
	}
	
	
	private List getContextKeyRow(Observation obsType,int rowno)
		throws Exception
	{
		List contextKeyRow = new ArrayList();
		
		//1. get all the distinct context observation types
		Set<String> contextObservationSet = m_obsType2contexObsType.get(obsType); 
		
		//2. for each observation type, get its key measurement values at rowno
		for(String obsLabel: contextObservationSet){
			List obsData = m_obsType2obsData.get(obsLabel);
			if(obsData==null||obsData.size()<rowno){
				throw new Exception("I am constructing the key row from the context, but the depending context observation data do not exist yet.");				
			}
			
			//get the row data
			List row = (List)obsData.get(rowno);
			
			//get the key row measurements
			Observation obs = m_annotation.getObservation(obsLabel);
			if(obs.getKeyMeasurements()==null||obs.getKeyMeasurements().size()==0){
				throw new Exception("I am constructing the key row from the context, but the depending context observation DOES NOT have key measurements.");				
			}
			if(row.size()<obs.getKeyMeasurements().size()){
				throw new Exception("I am constructing the key row from the context, row.size()<obs.getKeyMeasurements().size().");
			}
			
			//get the key row data
			for(int colno = 0; colno<obs.getKeyMeasurements().size();colno++){
				contextKeyRow.add(row.get(colno));
			}			
		}
		
		//3. return the result row
		return contextKeyRow;
	}
	
	/**
	 * Recursively get the identifying context observation types of the given obsTye
	 * @param obsType
	 * @return
	 */
	private Set<String> getContextKeyObservation(Observation obsType){
		Set<String> contextObservationSet = new TreeSet<String>();
		for(Context c: obsType.getContexts()){
			if(c.isIdentifying()){				
				contextObservationSet.add(c.getObservation().getLabel());
				Observation contextObservationType =  c.getObservation();
				Set<String> newSet = getContextKeyObservation(contextObservationType);
				contextObservationSet.addAll(newSet);
			}
		}
		return contextObservationSet;
	}
	
	private ArrayList<Integer> GenerateARow (int colnum)
	{
		if(colnum<=0)
			return null;
		
		ArrayList<Integer> obsrow = new ArrayList<Integer>();
		for(int i=0;i<colnum;i++){
			obsrow.add(m_randnumGenerator.nextInt(2*m_rownum));
		}
		return obsrow;
	}
	
	private int CalColNum()
	{
		int colNum = 0;
		for(Observation obsType: m_annotation.getObservations()){
			colNum +=(obsType.getMeasurements().size());
		}
		
		return colNum;
		
	}
	
	private void WriteDataset(PrintStream dataPrintStream)
		throws Exception
	{	
		System.out.println("m_rowStruct="+m_rowStruct);
		
		
		
		//1. write data structure information
		for(int i=0;i<m_rowStruct.size();i++){
			dataPrintStream.append(m_rowStruct.get(i));
			if(i<m_rowStruct.size()-1){
				dataPrintStream.append(Constant.C_DATASET_SEPARATOR);
			}
		}
		dataPrintStream.append("\n");
		
		
		//2. write data information
		for(int i=0;i<m_dataset.size();i++){
			ArrayList row = (ArrayList)(m_dataset.get(i));		
			if(m_rowStruct.size()!=row.size()){
				throw new Exception("m_rowStruct.size["+m_rowStruct.size()+"]!=row.size["+row.size()+"]!");
			}
			for(int j=0;j<row.size();j++){
				String val = row.get(j).toString();
				dataPrintStream.append(val);
				if(j<row.size()-1){
					dataPrintStream.append(Constant.C_DATASET_SEPARATOR);
				}				
			}
			dataPrintStream.append("\n");
			dataPrintStream.flush();
		}		
	}
	
	private void flattenData()
	{
		if(m_dataset==null) m_dataset = new ArrayList();
		else m_dataset.clear();
		
		for(int i=0;i<m_rownum;i++){
			List<Integer> row = new ArrayList();
			Iterator iter = m_obsType2obsData.entrySet().iterator();
			while(iter.hasNext()){
				Entry<Integer,List> entry = (Entry<Integer,List>)iter.next();
				List rowpart = (List)entry.getValue().get(i);
				row.addAll(rowpart);
				
			}
			m_dataset.add(row);
		}
	}
	
	/**
	 * Put the data in the map to m_dataset
	 * 
	 * @param measurement2ValueList, TreeMap: each entity is <Measurement label string: list of values for this measurement>
	 */
	private void flattenDataTopDown(Map<String, List> measurement2ValueList)
		throws Exception
	{
		//declare space for m_dataset
		if(m_dataset==null) m_dataset = new ArrayList();
		else m_dataset.clear();
		
		if(m_rowStruct==null) m_rowStruct = new ArrayList<String>();
		else m_rowStruct.clear();
		
		Iterator<Entry<String, List >> iter = measurement2ValueList.entrySet().iterator();
		int colno = 0;
		while(iter.hasNext()){
			//1. get the measurement label, the value list for this measurement		
			Entry<String, List > entry = iter.next();
			String measLabel = entry.getKey();
			
			m_rowStruct.add(measLabel);
			List<Integer> valueList = entry.getValue();
			
			//System.out.println(measLabel+":"+valueList);
			//System.out.println("measLabel="+measLabel+" valueList="+valueList);
			//2. make sure that this value list contains exactly the needed number of rows
			if(valueList.size()!=m_rownum){
				throw new Exception("For measurement " + measLabel +" there is no enough values.");
			}
			
			if(colno==0){
				for(int i=0;i<m_rownum;i++){
					List<Integer> row = new ArrayList<Integer>();
					int colVal = valueList.get(i);
					row.add(colVal);
					m_dataset.add(row);
				}
			}else{
				//3. put this measurement's values to the dataset
				for(int i=0;i<m_rownum;i++){
					Object rowObj = m_dataset.get(i);
					if(rowObj==null){
						throw new Exception("Measurement" + measLabel +" row " + i + "does not EXIST.");
					}
					List<Integer> row = (List) rowObj;
					
					//when the row exist, make sure the existing number of columns matches this column no.
					if(row.size()!=colno){
						throw new Exception("Measurement" + measLabel +" row " + i + "does not match the existing data.");
					}
					
					int colVal = valueList.get(i);
					row.add(colVal);
				}
			}
			
			//4. maintain the column no for error checking
			++colno;
		}
		
	}
	
	/**
	 * Opposite to flatten data
	 * 
	 * @param measurementSet
	 * @return
	 */
	private Map<String, List > structureData(Set<Measurement> measurementSet)
	{
		Map<String, List> measurement2ValueList = new TreeMap<String, List>();
		Map<Integer, String> colno2measLabel = new HashMap<Integer,String>();
		
		// Construct the structure
		int colno = 0;
		for(Measurement m: measurementSet){
			List valueList = new ArrayList();
			measurement2ValueList.put(m.getLabel().trim(), valueList);
			colno2measLabel.put(colno,m.getLabel().trim());
			colno++;
		}
		
		//refill the data
		for(int i=0;i<m_dataset.size();i++){
			List row = (List)m_dataset.get(i);
			for(int j=0;j<row.size();j++){
				int colval = (Integer)(row.get(j));
				String colMeasLabel = colno2measLabel.get(j);
				List colValueList = measurement2ValueList.get(colMeasLabel);
				colValueList.add(colval);
			}
		}
		
		//System.out.println("measurement2ValueList="+measurement2ValueList);
		
		return measurement2ValueList;
	}

}
