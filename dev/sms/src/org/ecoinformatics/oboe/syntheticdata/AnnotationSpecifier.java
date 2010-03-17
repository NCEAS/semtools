package org.ecoinformatics.oboe.syntheticdata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.ecoinformatics.sms.annotation.*;
import org.ecoinformatics.sms.ontology.Ontology;

public class AnnotationSpecifier{

	private String separatorMain = ":";
	private String separatorMinor = " ";
	private String measurementContextSeparatorMain = ",";
	private String distinctStr = "distinct";
	private String keyStr = "key";
	private String identifyStr = "identifying";
	
	private float defaultDistinctFactor = (float)0.5;
	private Annotation annotation = null;
	private Map<String, Float> key2distinctfactor = null;
	private Ontology m_defaultOnto = null;
	
	public AnnotationSpecifier()
	{
		key2distinctfactor = new TreeMap<String, Float>();
		annotation = new Annotation();
		m_defaultOnto = new Ontology("oboe", "testOntology");
	}
	
	public Map<String, Float> getKey2distinctfactor() {
		return key2distinctfactor;
	}

	public void setKey2distinctfactor(Map<String, Float> key2distinctfactor) {
		this.key2distinctfactor = key2distinctfactor;
	}
	
	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}
	
	public void WriteAnnotation(String outAnnotFileName) throws IOException
	{
		FileOutputStream annotOutputStream = new FileOutputStream(outAnnotFileName);
				
		annotation.write(annotOutputStream);
		annotOutputStream.close();
		System.out.println("Annotation is written to file: " + outAnnotFileName);
		
		System.out.println("\nkey2distinctfactor:" + key2distinctfactor);
	}

	public void readAnnotationSpecFile(String fname) 
		throws FileNotFoundException,IOException, Exception
	{
		BufferedReader bufferedReader = null; 
		bufferedReader = new BufferedReader(new FileReader(fname));
		
		readAnnotationSpecFile(bufferedReader);
		
		if (bufferedReader != null) bufferedReader.close();
	}
	
	private void readAnnotationSpecFile(BufferedReader r)
		throws Exception
	{
		String line = null;
		try {
			while((line = r.readLine())!=null){
				System.out.println(line);
				extractAnnotation(line);				
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void extractAnnotation(String line)
		throws Exception
	{
		String[] oneAnnotate = line.split(separatorMain);
		
		if(oneAnnotate.length<=1)
			return; 
		
		Observation obsType = null;
		if(oneAnnotate.length>=2){
			obsType = extractObs(oneAnnotate[0]);
			annotation.addObservation(obsType);
			float factor = extractMeasurements(obsType,oneAnnotate[1]);
			if(factor<0){
				factor = defaultDistinctFactor;
			}
			key2distinctfactor.put(obsType.getLabel(),factor);
		}
		
		if(oneAnnotate.length>=3){
			extractContexts(obsType, oneAnnotate[2]);
		}		
	}
	
	/**
	 * 
	 * @param str: in the form of "o2 e2 distinct" or "o3 e3"
	 * @return
	 * @throws Exception
	 */
	private Observation extractObs(String str)
		throws Exception
	{
		Observation obs = new Observation();
		String[] obsAnnotate = str.split(separatorMinor);
		
		if(obsAnnotate.length>=2){
			String obsLabel = obsAnnotate[0]; 
			obs.setLabel(obsLabel);			
			
			String entityLabel = obsAnnotate[1];
			Entity entity = new Entity();
			entity.setName(entityLabel);
			entity.setOntology(m_defaultOnto);
			obs.setEntity(entity);			
		}
		
		if(obsAnnotate.length==2){
			obs.setDistinct(false);
		}else if(obsAnnotate.length==3){
			String isDistinct = obsAnnotate[2];
			if(isDistinct.trim().equals(distinctStr)){
				obs.setDistinct(true);
			}else{
				obs.setDistinct(false);
			}
		}else{
			throw new Exception("Invalid observation specification " + str);
		}
		
		return obs;
	}
	
	/**
	 * 
	 * @param obs
	 * @param str: in the form of "m4 key,m5 key,0.5"
	 */
	private float extractMeasurements(Observation obs, String str)
		throws Exception
	{
		float factor = (float)(-1.0);
		String[] measurements = str.split(measurementContextSeparatorMain);
		
		//1. error processing
		if(obs==null){
			return factor;
		}
				
		if((obs!=null)&&(measurements.length==0)){
			throw new Exception("Observation specifier is not NULL, but no measurements.");			
		}
		
		//2. extract the duplication factor
		int numberOfMeasurements = measurements.length;
		String lastStr = measurements[measurements.length-1];
		factor = Float.parseFloat(lastStr);
		if(factor>=0.0 && factor <=1.0){
			--numberOfMeasurements;
		}

		//3. extract all the measuremetns
		if(numberOfMeasurements==0){
			throw new Exception("Invalid measurement type specification, number of measurements = 0: "+str);
		}
		
		for(int i=0;i<numberOfMeasurements;i++){
			Measurement m = extractOneMeasurement(measurements[i]); 
			obs.addMeasurement(m);
		}
		
		return factor;
	}
	
	/**
	 * 
	 * @param str: in the form of "m1 key" or "m1"
	 * @return
	 */
	private Measurement extractOneMeasurement(String str)
		throws Exception
	{
		String[] oneMeasurement = str.split(separatorMinor); 
		Measurement m = null;
		
		if(oneMeasurement.length==0||oneMeasurement.length>2){
			throw new Exception("Invalid oneMeasurement: "+str);
		}
		if(oneMeasurement.length>=1){
			String measurmentLabel = oneMeasurement[0];
			m = new Measurement();
			m.setLabel(measurmentLabel);
		}
		
		if(oneMeasurement.length==2){
			String isKey = oneMeasurement[1];
			if(isKey.trim().equals(keyStr)){
				m.setKey(true);
			}else{
				m.setKey(false);
			}
		}else{
			m.setKey(false);
		}
		
		return m;		
	}
	
	/**
	 * 
	 * @param str: in the form of "o1 identifying,o2 identifying" or "o1 identifying,o2"
	 * @return
	 */
	private void extractContexts(Observation obs, String str)
		throws Exception
	{
		String[] contexts = str.split(measurementContextSeparatorMain);
		
		
		//1. error processing
		if(obs==null){
			return;
		}
				
		if((obs!=null)&&(contexts.length==0)){
			throw new Exception("Observation specifier is not NULL, but no contexts.");			
		}
		
		//2. extract all the contexts
		for(int i=0;i<contexts.length;i++){
			Context c = extractOneContext(contexts[i]); 
			obs.addContext(c);
		}
	}
	
	/**
	 * 
	 * @param str: in the form of "o1 identifying" or "o1"
	 */
	private Context extractOneContext(String str)
		throws Exception
	{
		
		String[] oneContext = str.split(separatorMinor); 
		Context c = null;
		
		if(oneContext.length==0||oneContext.length>2){
			throw new Exception("Invalid oneContext: "+str);
		}
		if(oneContext.length>=1){
			String contextObsTypeLabel = oneContext[0];
			Observation contextObsType = annotation.getObservation(contextObsTypeLabel);
			c = new Context();
			c.setObservation(contextObsType);			
		}
		
		if(oneContext.length==2){
			String isIdentifying = oneContext[1];
			if(isIdentifying.trim().equals(identifyStr)){
				c.setIdentifying(true);
			}else{
				c.setIdentifying(false);
			}
		}else{
			c.setIdentifying(false);
		}
		
		return c;		
	}
}
