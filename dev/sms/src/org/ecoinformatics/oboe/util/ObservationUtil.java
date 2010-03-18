package org.ecoinformatics.oboe.util;

import java.util.List;
import java.util.ArrayList;

import org.ecoinformatics.sms.annotation.*;

public class ObservationUtil{

	/**
	 * Get the given observation type's key measurements, 
	 * it contains
	 * (1) its own key measurements
	 * (2) its context key measurements recursively
	 *  
	 * @param obsType
	 * @return
	 * @throws Exception
	 */
	public static List<Measurement> getAllKeyMeasurement(Observation obsType)
		throws Exception
	{
		List<Measurement> keyMeasurements = obsType.getKeyMeasurements();
		if(keyMeasurements==null){
			keyMeasurements = new ArrayList<Measurement>();
		}
		
		List<Context> contextList = obsType.getContexts();
		for(Context c: contextList){
			Observation contextObsType = c.getObservation();
			if(c.isIdentifying()){
				List<Measurement> contextObsTypeKeyMeasurementList = getAllKeyMeasurement(contextObsType); //contextObsType.getKeyMeasurements();
					
				if(contextObsTypeKeyMeasurementList==null||contextObsTypeKeyMeasurementList.size()==0){
					throw new Exception("An identifying observation type does not have key measurements. ");
				}
				keyMeasurements.addAll(contextObsTypeKeyMeasurementList);
			}
		}

		return keyMeasurements; 
	}

}
