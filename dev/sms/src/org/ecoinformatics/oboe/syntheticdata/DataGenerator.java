package org.ecoinformatics.oboe.syntheticdata;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Context;

import java.io.FileNotFoundException;
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

	private String separator = ",";
	
	private Annotation annotation = null;
	private List dataset = null; 
	private int rownum = 0;
	private Random randnumGenerator; 
	private Map<String, Float> m_key2distinctfactor = null;
	private Map<String, List> m_obsType2obsData = null;
	private Map<Observation, Set> m_obsType2contexObsType= null;
	
	public DataGenerator()
	{
		randnumGenerator = new Random(0);
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
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}
	
	public int getRownum() {
		return rownum;
	}

	public void setRownum(int rownum) {
		this.rownum = rownum;
	}

	private void calObsType2ContextObsType()
	{
		for(Observation obsType: annotation.getObservations()){
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
	
	public void Generate() throws Exception
	{
		//1. validae the input 
		boolean valid = ValidateInput();
		if(!valid){
			throw new Exception("Input is invalid.");
		}
		
		//2. get the order of observation types for which to generate data
		List<Observation> contextObsList = compOrderedObsList();
		
		//3. Generate data for observation types
		for(Observation obsType: contextObsList){
			generateOneObsData(obsType);
		}
	}
	
	public void WriteData(String outDataFileName) throws FileNotFoundException
	{
		PrintStream dataPrintStream = new PrintStream(outDataFileName);
		WriteDataset(dataPrintStream);
		dataPrintStream.close();
		
		System.out.println("Data is written to "+outDataFileName);		
	}
	
	private List compOrderedObsList()
		throws Exception
	{
		//Map<Observation, Set> tmpObsType2contexObsType= new TreeMap<Observation,Set>();
		//tmpObsType2contexObsType.putAll(m_obsType2contexObsType);
		
		List<Observation> contextObsList = new ArrayList<Observation> ();
		List<Observation> thisLoopChange = new ArrayList<Observation>();
		thisLoopChange.add(null);
		
		int looptime = 0;
		while((thisLoopChange.size()>0||looptime>annotation.getObservations().size())&&m_obsType2contexObsType.size()>0){
			thisLoopChange.clear();
			
			//get the entries we need to remove for this loop
			Set<String> todelObs = new HashSet<String>();
			Iterator<Entry<Observation,Set> > iter = m_obsType2contexObsType.entrySet().iterator();
			while(iter.hasNext()){
				Entry<Observation,Set> entry = iter.next();
				if(entry.getValue().size()==0){
					thisLoopChange.add(entry.getKey());
					todelObs.add(entry.getKey().getLabel());
				}
			}
			
			//remove the entries
			for(Observation obs: thisLoopChange){
				m_obsType2contexObsType.remove(obs);
			}
			
			//remove these observations' reference from other observation
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
		
		if(looptime>annotation.getObservations().size()){
			throw new Exception("cannot get correct order to generate data.\n");
		}
		
		//since the map is removed gradually, need to recalculate it.
		calObsType2ContextObsType();
		
		return contextObsList;
	}
	/**
	 * make sure one observation should have higher distinct factor than its context observations 
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
	 		for(int rowno = 0;rowno<rownum;rowno++){
	 			ArrayList<Integer> obsrow = GenerateARow(obsType.getMeasurements().size());
				obsData.add(obsrow);
	 		}
		}else{//2. Has key measurements
			//generate the list of distinct key rows
			ArrayList<ArrayList<Integer>> distinctKeyRowList = new ArrayList();
			int distinctKeyRowNum = (int)(rownum*factor);
			
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
			for(int rowno=0;rowno<rownum;rowno++){
				ArrayList<Integer> obsrow = new ArrayList<Integer>();
				
				int distinctRowIdx = (rowno<distinctKeyRowNum)?rowno:randnumGenerator.nextInt(distinctKeyRowNum);
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
			Observation obs = annotation.getObservation(obsLabel);
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
			obsrow.add(randnumGenerator.nextInt(2*rownum));
		}
		return obsrow;
	}
	
	private int CalColNum()
	{
		int colNum = 0;
		for(Observation obsType: annotation.getObservations()){
			colNum +=(obsType.getMeasurements().size());
		}
		
		return colNum;
		
	}
	

	
	private void WriteDataset(PrintStream dataPrintStream)
	{
		flattenData();
			
		for(int i=0;i<dataset.size();i++){
			ArrayList row = (ArrayList)(dataset.get(i));			
			for(int j=0;j<row.size();j++){
				String val = row.get(j).toString();
				dataPrintStream.append(val);
				if(j<row.size()-1){
					dataPrintStream.append(separator);
				}				
			}
			dataPrintStream.append("\n");
			dataPrintStream.flush();
		}		
	}
	
	private void flattenData()
	{
		if(dataset==null) dataset = new ArrayList();
		else dataset.clear();
		
		for(int i=0;i<rownum;i++){
			List<Integer> row = new ArrayList();
			Iterator iter = m_obsType2obsData.entrySet().iterator();
			while(iter.hasNext()){
				Entry<Integer,List> entry = (Entry<Integer,List>)iter.next();
				List rowpart = (List)entry.getValue().get(i);
				row.addAll(rowpart);
				
			}
			dataset.add(row);
		}
	}

}
