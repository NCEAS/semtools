package org.ecoinformatics.oboe.syntheticdataquery;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.ecoinformatics.oboe.Constant;
import org.ecoinformatics.sms.annotation.*;
import org.ecoinformatics.sms.ontology.Ontology;

public class AnnotationSpecifier{

	private String m_separatorMain = ":";
	private String m_separatorMinor = " ";
	private String m_measurementContextSeparatorMain = ",";
	private String m_comment = "#";
	private String m_distinctStr = "distinct";
	private String m_keyStr = "key";
	private String m_identifyStr = "identifying";
	
	private float m_defaultDistinctFactor = (float)0.5;
	private Annotation m_annotation = null;
	private Map<String, Float> m_key2distinctfactor = null;
	private Ontology m_defaultOnto = null;
	
	public AnnotationSpecifier()
	{
		m_key2distinctfactor = new TreeMap<String, Float>();
		m_annotation = new Annotation();
		m_defaultOnto = new Ontology(Constant.ANNOTATION_DEFAULT_RELATIONSHIP_URI, Constant.ANNOTATION_DEFAULT_ONTOLOGY);
	}
	
	public Map<String, Float> getKey2distinctfactor() {
		return m_key2distinctfactor;
	}

	public void setKey2distinctfactor(Map<String, Float> m_key2distinctfactor) {
		this.m_key2distinctfactor = m_key2distinctfactor;
	}
	
	public Annotation getAnnotation() {
		return m_annotation;
	}

	public void setAnnotation(Annotation m_annotation) {
		this.m_annotation = m_annotation;
	}
	
	/**
	 * Get the entity type name of a given measurement label
	 * 
	 * @param measurementLabel
	 * @return
	 */
	public String getEntityTypeName(String measurementLabel)
	{
		Measurement meas = m_annotation.getMeasurement(measurementLabel);
		Observation obs = m_annotation.getObservation(meas);
		
		String entityTypeName = obs.getEntity().getName();
		return entityTypeName;
	}
	/**
	 * Set the default mappings from each column to each measurement
	 */
	public void setDefaultMapping()
	{
		for(Observation obs: m_annotation.getObservations()){
			for(Measurement meas: obs.getMeasurements()){
				Mapping m = new Mapping();
				m.setAttribute(meas.getLabel());
				m.setMeasurement(meas);
				m_annotation.addMapping(m);
			}
		}
	}
	
	
	/**
	 * Write the annotation (read from annotation specification file) to an annotation file (for further checking)
	 * 
	 * @param outAnnotFileName
	 * @throws IOException
	 */
	public void WriteAnnotation(String outAnnotFileName) throws IOException
	{
		FileOutputStream annotOutputStream = new FileOutputStream(outAnnotFileName);
		
		int pos = outAnnotFileName.lastIndexOf("/");
		if(pos<0)
			m_annotation.setDataPackage(outAnnotFileName);
		else
			m_annotation.setDataPackage(outAnnotFileName.substring(pos+1));
		Ontology o =new Ontology();
		o.setPrefix(Constant.ANNOTATION_DEFAULT_RELATIONSHIP_URI);
		o.setURI(null);
		m_annotation.addOntology(o);
		
		// Form mapping for each measurement	
		setDefaultMapping();
		
		// Write the annotation to an annotation file
		m_annotation.write(annotOutputStream);
		annotOutputStream.close();
		System.out.println("key2distinctfactor:" + m_key2distinctfactor);
		
		System.out.println("\nm_annotation is written to file: " + outAnnotFileName+"\n");
	}

	/**
	 * From a m_annotation specification file, read the m_annotation rules to the m_annotation structure
	 * 
	 * @param fname
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public void readAnnotationSpecFile(String fname) 
		throws FileNotFoundException,IOException, Exception
	{
		BufferedReader bufferedReader = null; 
		bufferedReader = new BufferedReader(new FileReader(fname));
		
		readAnnotationSpecFile(bufferedReader);
		
		if (bufferedReader != null) bufferedReader.close();
	}
	
	/**
	 * From a buffered reader, read m_annotation files to the m_annotation structure
	 * 
	 * @param r
	 * @throws Exception
	 */
	private void readAnnotationSpecFile(BufferedReader r)
		throws Exception
	{
		//1. Extract all the annotations and set default mappings
		String line = null;
		try {
			while((line = r.readLine())!=null){				
				if(!line.startsWith(m_comment)){
					//System.out.println(line);
					extractAnnotation(line);	
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	
	/**
	 * Extract annotation from a line in the specification file
	 * 
	 * @param line
	 * @throws Exception
	 */
	private void extractAnnotation(String line)
		throws Exception
	{
		//System.out.println("Extract annotation from: " +line);
		String[] oneAnnotate = line.split(m_separatorMain);
		
		if(oneAnnotate.length<=1)
			return; 
		
		Observation obsType = null;
		if(oneAnnotate.length>=2){
			obsType = extractObs(oneAnnotate[0]);
			
			m_annotation.addObservation(obsType);
			float factor = extractMeasurements(obsType,oneAnnotate[1]);
			if(factor<0){
				factor = m_defaultDistinctFactor;
			}
			m_key2distinctfactor.put(obsType.getLabel(),factor);
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
		String[] obsAnnotate = str.split(m_separatorMinor);
		
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
			if(isDistinct.trim().equals(m_distinctStr)){
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
		String[] measurements = str.split(m_measurementContextSeparatorMain);
		
		//1. error processing
		if(obs==null){
			return factor;
		}
				
		if((obs!=null)&&(measurements.length==0)){
			throw new Exception("Observation specifier is not NULL, but no measurements.");			
		}
		
		//2. extract the duplication factor
		int numberOfMeasurements = measurements.length;
		if(measurements.length>1){
			String lastStr = measurements[measurements.length-1];
			factor = Float.parseFloat(lastStr);
			if(factor>=0.0 && factor <=1.0){
				--numberOfMeasurements;
			}
		}
		

		//3. extract all the measuremetns
		if(numberOfMeasurements==0){
			throw new Exception("Invalid measurement type specification, number of measurements = 0: "+str);
		}
		
		for(int i=0;i<numberOfMeasurements;i++){
			Measurement m = extractOneMeasurement(measurements[i]);
			Ontology o = new Ontology();
			o.setPrefix(Constant.ANNOTATION_DEFAULT_ONTOLOGY);
			Characteristic cha = new Characteristic();
			cha.setOntology(o);
			//cha.setURI("chauri");
			cha.setName(m.getLabel()+"cha");			
			m.addCharacteristic(cha);
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
		String[] oneMeasurement = str.split(m_separatorMinor); 
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
			if(isKey.trim().equals(m_keyStr)){
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
		String[] contexts = str.split(m_measurementContextSeparatorMain);
		
		
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
		
		String[] oneContext = str.split(m_separatorMinor); 
		Context c = null;
		
		if(oneContext.length==0||oneContext.length>2){
			throw new Exception("Invalid oneContext: "+str);
		}
		if(oneContext.length>=1){
			String contextObsTypeLabel = oneContext[0];
			Observation contextObsType = m_annotation.getObservation(contextObsTypeLabel);
			c = new Context();
			c.setObservation(contextObsType);			
		}
		
		if(oneContext.length==2){
			String isIdentifying = oneContext[1];
			if(isIdentifying.trim().equals(m_identifyStr)){
				c.setIdentifying(true);
			}else{
				c.setIdentifying(false);
			}
		}else{
			c.setIdentifying(false);
		}
		
		setDefaultContexRelationship(c);
		
		return c;		
	}
	
	/**
	 * Set a default relationship of this context
	 * 
	 * @param c
	 * @throws Exception
	 */
	private void setDefaultContexRelationship(Context c) throws Exception
	{
		Relationship relationship = new Relationship();
		relationship.setURI(Constant.ANNOTATION_DEFAULT_RELATIONSHIP_URI);
		relationship.setName(Constant.ANNOTATION_DEFAULT_RELATIONSHIP_NAME);
		c.setRelationship(relationship);
	}
}
