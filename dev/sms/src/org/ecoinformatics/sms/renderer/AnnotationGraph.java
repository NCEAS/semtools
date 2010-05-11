package org.ecoinformatics.sms.renderer;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Relationship;
import org.ecoinformatics.sms.annotation.Standard;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

public class AnnotationGraph {
	
	public static Component createAnnotationGraph(Annotation annotation) {
		
		// the graph
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		
		//the cell style
		mxStylesheet stylesheet = graph.getStylesheet();
		Map<String, Object> styleMap = new HashMap<String, Object>();
		styleMap.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		//styleMap.put(mxConstants.STYLE_OPACITY, 50);
		//styleMap.put(mxConstants.STYLE_FONTCOLOR, "#774400");
		stylesheet.putCellStyle("Rounded", styleMap);
		
		Map<String, Object> observationStyleMap = new HashMap<String, Object>();
		observationStyleMap.put(mxConstants.STYLE_FILLCOLOR, "#EEEEEE");
		stylesheet.putCellStyle("Observation", observationStyleMap);
		
		Map<String, Object> measurementStyleMap = new HashMap<String, Object>();
		measurementStyleMap.put(mxConstants.STYLE_FILLCOLOR, "#DDDDDD");
		stylesheet.putCellStyle("Measurement", measurementStyleMap);
		
		// the edge style
		Map<String, Object> edgeStyleMap = new HashMap<String, Object>();
		edgeStyleMap.put(mxConstants.STYLE_ELBOW, mxConstants.ELBOW_VERTICAL);
		edgeStyleMap.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
		stylesheet.putCellStyle("Edge", edgeStyleMap);

		graph.setStylesheet(stylesheet);

		
		// for positioning
		int x = 20;
		int y = 20;
		int width = 80;
		int height = 30;
		int observationOffset = 0;
		String style = "defaultVertex;Rounded";
		String observationStyle = "defaultVertex;Rounded;Observation";
		String measurementStyle = "defaultVertex;Rounded;Measurement";
		String edgeStyle = "defaultEdge;Edge";

		
		// keep track of the observation cells
		Map<Observation, mxCell> observationMap = new HashMap<Observation, mxCell>();
		int observationCount = 0;
		for (Observation observation: annotation.getObservations()) {
			// start at the left again
			x = 20;
			observationOffset = observationCount++ * width;
			x += observationOffset;

			graph.getModel().beginUpdate();
			try {
				
				Object observationCell = parent; //graph.addCell(new mxCell(observation), parent);
				
				// add observation
				Object observationNode = 
					graph.insertVertex(observationCell, null, observation, x, y, width, height, observationStyle);

				observationMap.put(observation, (mxCell)observationNode);
				
				// of entity
//				Object entityNode = 
//					graph.insertVertex(observationCell, null, observation.getEntity(), x, y + (height*2), width, height, style);
//				graph.insertEdge(observationCell, null, "ofEntity", observationNode, entityNode, edgeStyle);

				for (Measurement measurement: observation.getMeasurements()) {
					
					// shift over to right
					x = (width*3) + observationOffset;
					
					// add measurement
					Object measurementNode = 
						graph.insertVertex(observationCell, null, measurement.getLabel(), x, y, width, height, measurementStyle);
					
					graph.insertEdge(observationCell, null, "hasMeasurement", observationNode, measurementNode, null);
					
					// shift over to right
					x = (width*6) + observationOffset;

					// add characteristic, if available
					Characteristic characteristic = null;
					try {
						characteristic = measurement.getCharacteristics().get(0);
					} catch (Exception e) {}
					Object characteristicNode = 
						graph.insertVertex(observationCell, null, characteristic, x, y, width, height, style);
					graph.insertEdge(observationCell, null, "ofCharacteristic", measurementNode, characteristicNode, null);
					
					// shift down
					y += (height*2);
					
					// add standard
					Standard standard = measurement.getStandard();
					Object standardNode = 
						graph.insertVertex(observationCell, null, standard, x, y, width, height, style);
					graph.insertEdge(observationCell, null, "usesStandard", measurementNode, standardNode, null);
					
					// shift down
					y += (height*2);
					
					// add protocol
					Protocol protocol = measurement.getProtocol();
					Object protocolNode = 
						graph.insertVertex(observationCell, null, protocol, x, y, width, height, style);
					graph.insertEdge(observationCell, null, "usesProtocol", measurementNode, protocolNode, null);
					
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
		
		// process context edges/observation cells
		Object[] observationCells = graph.getChildVertices(parent);
		for (int i = 0; i < observationCells.length; i++) {
			mxCell observationCell = (mxCell) observationCells[i];
			Object cellValue = observationCell.getValue();
			if (!(cellValue instanceof Observation)) {
				continue;
			}
			Observation observation = (Observation) observationCell.getValue();
			if (observation.getContexts() != null) {
				for (Context context: observation.getContexts()) {
					Observation targetObservation = context.getObservation();
					Relationship relationship = context.getRelationship();
					Object targetObservationCell = observationMap.get(targetObservation);
					// add the context from one observation node to the other observation node
					graph.insertEdge(parent, null, relationship, observationCell, targetObservationCell, edgeStyle);
				}	
			}
		}
		
		//add to the page
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		
		JPanel panel = WidgetFactory.makePanel(5);
		panel.add(graphComponent);
		
		return panel;
		
	}

	public static void layoutGraph(Component c) {
		
		if (c instanceof mxGraphComponent) {
			mxGraphComponent graphComponent = (mxGraphComponent) c;
			mxGraph graph = graphComponent.getGraph();
			Object parent = graph.getDefaultParent();
			
			//mxGraphLayout layout = new mxFastOrganicLayout(graph);
			mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
			layout.setDisableEdgeStyle(false);
			layout.execute(parent);
			
		}
	}
}
