package org.ecoinformatics.sms.renderer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.ObservationComparator;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Relationship;
import org.ecoinformatics.sms.annotation.Standard;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.orthogonal.mxOrthogonalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

public class AnnotationGraph {
	
	public static Component createAnnotationGraph(Annotation annotation, boolean showAll) {
		
		// the graph
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		
		//the cell style
		mxStylesheet stylesheet = graph.getStylesheet();
		Map<String, Object> styleMap = new HashMap<String, Object>();
		styleMap.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		styleMap.put(mxConstants.STYLE_OPACITY, 50);
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
		//edgeStyleMap.put(mxConstants.STYLE_ELBOW, mxConstants.ELBOW_HORIZONTAL);
		edgeStyleMap.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
		//edgeStyleMap.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_LOOP);
		edgeStyleMap.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_BOTTOM);
		stylesheet.putCellStyle("Edge", edgeStyleMap);

		graph.setStylesheet(stylesheet);

		
		// for positioning
		double x = 20;
		double y = 20;
		double width = 80;
		double height = 30;
		double observationOffset = 0;
		String style = "defaultVertex;Rounded";
		String observationStyle = "defaultVertex;Rounded;Observation";
		String measurementStyle = "defaultVertex;Rounded;Measurement";
		String observationEdgeStyle = "defaultEdge;Edge";
		String edgeStyle = null;


		
		// keep track of the observation cells
		Map<Observation, mxCell> observationMap = new HashMap<Observation, mxCell>();
		int observationCount = 0;
		List<Observation> sortedObservations = annotation.getObservations();
		boolean sort = true;
		boolean includeContext = true;
		if (sort) {
			if (includeContext) {
				Collections.sort(sortedObservations, new ObservationComparator());
			} else {
				Collections.sort(sortedObservations);
			}
		}
		
		for (Observation observation: sortedObservations) {
			// start at the top again
			y = 5;
			//observationOffset = observationCount++ * height;
			observationOffset = 0;
			y += observationOffset;

			graph.getModel().beginUpdate();
			try {
				
				Object observationCell = parent; //graph.addCell(new mxCell(observation), parent);
				
				// add observation
				Object observationNode = 
					graph.insertVertex(observationCell, null, observation, x, y, width, height, observationStyle);

				observationMap.put(observation, (mxCell)observationNode);
				
				if (!showAll) {
					continue;
				}
				
				for (Measurement measurement: observation.getMeasurements()) {
					
					// shift down
					y = (height*1.5) + observationOffset;
					
					// add measurement
					String measurementString = measurement.getLabel();
					Mapping mapping = measurement.getMapping();
					if (mapping != null) {
						measurementString = mapping.getAttribute();
					}
					Object measurementNode = 
						graph.insertVertex(observationCell, null, measurementString, x, y, width, height, measurementStyle);
					
					graph.insertEdge(observationCell, null, "", observationNode, measurementNode, edgeStyle);
					
					// shift down
					//y = (height*4) + observationOffset;
					y = y + (height*1.3) + observationOffset;

					// add characteristic, if available
					Characteristic characteristic = null;
					try {
						characteristic = measurement.getCharacteristics().get(0);
					} catch (Exception e) {}
					Object characteristicNode = 
						graph.insertVertex(observationCell, null, characteristic, x, y, width, height, style);
					graph.insertEdge(observationCell, null, "", measurementNode, characteristicNode, edgeStyle);
					
					// shift over
					x += (width*.75);
					// shift down
					y = y + (height*.75) + observationOffset;
					
					// add standard
					Standard standard = measurement.getStandard();
					Object standardNode = 
						graph.insertVertex(observationCell, null, standard, x, y, width, height, style);
					graph.insertEdge(observationCell, null, "", measurementNode, standardNode, edgeStyle);
					
					// shift over
					x += (width*.75);
					// shift down
					y = y + (height*.75) + observationOffset;
					
					// add protocol
					Protocol protocol = measurement.getProtocol();
					Object protocolNode = 
						graph.insertVertex(observationCell, null, protocol, x, y, width, height, style);
					graph.insertEdge(observationCell, null, "", measurementNode, protocolNode, edgeStyle);
					
					// shift over
					x += (width*.75);
					
				}
				// shift over
				//x += width;
			}
			finally {
				graph.getModel().endUpdate();
			}
		}
		
		// process context edges/observation cells
		Object[] observationCells = graph.getChildVertices(parent);
		List<Object> observationEdges = new ArrayList<Object>();
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
					Object edge = graph.insertEdge(parent, null, relationship, observationCell, targetObservationCell, observationEdgeStyle);
					observationEdges.add(edge);
				}	
			}
		}
		graph.orderCells(true, observationEdges.toArray());
		
		//add to the page
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setEnabled(false);
		
		if (!showAll) {
			layoutGraph(graphComponent);
		}
		
		JPanel panel = WidgetFactory.makePanel(9);
		panel.add(graphComponent);
		
		return panel;
		
	}

	public static void layoutGraph(Component c) {
		
		if (c instanceof mxGraphComponent) {
			mxGraphComponent graphComponent = (mxGraphComponent) c;
			mxGraph graph = graphComponent.getGraph();
			Object parent = graph.getDefaultParent();
			
			//mxGraphLayout layout = new mxFastOrganicLayout(graph);
			//mxGraphLayout layout = new mxCircleLayout(graph);
			//mxGraphLayout layout = new mxOrganicLayout(graph);
			//mxGraphLayout layout = new mxCompactTreeLayout(graph);

			mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
			//mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.NORTH);
			layout.setDisableEdgeStyle(false);
			layout.execute(parent);
			
		}
	}
}
