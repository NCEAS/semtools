package org.ecoinformatics.sms.renderer;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Relationship;
import org.ecoinformatics.sms.annotation.Standard;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class AnnotationGraph {
	
	public static Component createAnnotationGraph(Annotation annotation) {
		
		// the graph
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		int x = 20;
		int y = 20;
		int width = 80;
		int height = 30;
		
		Map<Observation, mxCell> observationMap = new HashMap<Observation, mxCell>();
		
		for (Observation observation: annotation.getObservations()) {
			// start at the left again
			x = 20;
			graph.getModel().beginUpdate();
			try {
				
				Object observationCell = graph.addCell(new mxCell(observation), parent);
				
				observationMap.put(observation, (mxCell) observationCell);

				// add observation
				Object observationNode = 
					graph.insertVertex(observationCell, null, observation.getLabel(), x, y, width, height);
				
				// shift down
				//y += (height*2);
				
				// of entity
				Object entityNode = 
					graph.insertVertex(observationCell, null, observation.getEntity(), x, y + (height*2), width, height);
				graph.insertEdge(observationCell, null, "ofEntity", observationNode, entityNode);

				for (Measurement measurement: observation.getMeasurements()) {
					
					// shift over to right
					x = (width*3);
					
					// add measurement
					Object measurementNode = 
						graph.insertVertex(observationCell, null, measurement.getLabel(), x, y, width, height);
					
					graph.insertEdge(observationCell, null, "hasMeasurement", observationNode, measurementNode);
					
					// shift over to right
					x = (width*6);

					// add characteristic, if available
					Characteristic characteristic = null;
					try {
						characteristic = measurement.getCharacteristics().get(0);
					} catch (Exception e) {}
					Object characteristicNode = 
						graph.insertVertex(observationCell, null, characteristic, x, y, width, height);
					graph.insertEdge(observationCell, null, "ofCharacteristic", measurementNode, characteristicNode);
					
					// shift down
					y += (height*2);
					
					// add standard
					Standard standard = measurement.getStandard();
					Object standardNode = 
						graph.insertVertex(observationCell, null, standard, x, y, width, height);
					graph.insertEdge(observationCell, null, "usesStandard", measurementNode, standardNode);
					
					// shift down
					y += (height*2);
					
					// add protocol
					Protocol protocol = measurement.getProtocol();
					Object protocolNode = 
						graph.insertVertex(observationCell, null, protocol, x, y, width, height);
					graph.insertEdge(observationCell, null, "usesProtocol", measurementNode, protocolNode);
					
					// shift down
					y += (height*2);
					
				}
				// shift down
				y += height;
			}
			finally {
				graph.getModel().endUpdate();
			}
		}
		
		// process context
		Object[] observationCells = graph.getChildCells(parent);
		for (int i = 0; i < observationCells.length; i++) {
			mxCell observationCell = (mxCell) observationCells[i];
			Observation observation = (Observation) observationCell.getValue();
			if (observation.getContexts() != null) {
				for (Context context: observation.getContexts()) {
					Observation targetObservation = context.getObservation();
					Relationship relationship = context.getRelationship();
					mxCell targetObservationCell = observationMap.get(targetObservation);
					// add the context from one observation node to the other observation node
					// here we know the Observation is the first child in the observationCell
					// TODO: figure out how to effectively do sub-graphs so that i can connect individual observation cells
					graph.insertEdge(parent, null, relationship, observationCell.getChildAt(0), targetObservationCell.getChildAt(0));
				}	
			}
		}
		
		//add to the page
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		
		return graphComponent;
		
	}

}
