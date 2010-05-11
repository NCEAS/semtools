package org.ecoinformatics.sms.plugins.table;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.pages.AnnotationPage;
import org.ecoinformatics.sms.plugins.pages.ContextPage;
import org.ecoinformatics.sms.renderer.AnnotationGraph;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;

public class AnnotationTabPane extends JTabbedPane implements StateChangeListener {
	
	public static List<String> TAB_NAMES = new ArrayList<String>();
	
	public static String COLUMN_ANNOTATION = "Column Annotation";
	public static String CONTEXT_ANNOTATION = "Context Annotation";
	public static String GRAPH_ANNOTATION = "Graph Annotation";
	public static String FULL_ANNOTATION = "Full Annotation";

	
	static {
		TAB_NAMES.add(COLUMN_ANNOTATION);
		TAB_NAMES.add(CONTEXT_ANNOTATION);
		TAB_NAMES.add(FULL_ANNOTATION);
		TAB_NAMES.add(GRAPH_ANNOTATION);
	}
	
	private Annotation annotation;
	
	private AnnotationPage annotationPage;
	
	private ContextPage contextPage;
	
	private Component annotationGraph;

	
	public AnnotationTabPane(Annotation a, int tabPlacement) {
		super(tabPlacement);
		this.annotation = a;
		
		// add a change listener
		this.addChangeListener(new ChangeListener() {
		    // This method is called whenever the selected tab changes
		    public void stateChanged(ChangeEvent e) {
		    	initHandling();
		        handleSelectTab();
		    }
		});
	}
	
	public void handleStateChange(StateChangeEvent event) {
		initHandling();
		
		if (event.getChangedState().equals(StateChangeEvent.SELECT_DATATABLE_COLUMN)) {
			handleSelectColumn();
		}
		if (event.getChangedState().equals(AnnotationPlugin.ANNOTATION_CHANGE_EVENT)) {
			showAnnotation();
		}
	}
	
	private void initHandling() {
		// get the annotation page no matter what
		if (getTabCount() == TAB_NAMES.size()) {
			annotationPage = (AnnotationPage) getComponentAt(TAB_NAMES.indexOf(COLUMN_ANNOTATION));
			contextPage = (ContextPage) getComponentAt(TAB_NAMES.indexOf(CONTEXT_ANNOTATION));
			annotationGraph = getComponentAt(TAB_NAMES.indexOf(GRAPH_ANNOTATION));
		}
	}
	
	private void handleSelectColumn() {
		// show the annotation
		showAnnotation();
	}
	
	private void handleSelectTab() {
		// show the latest state in the tabs
		showAnnotation();
	}
	
	private void showAnnotation() {
		// refresh the annotation column view
		String attributeName = AnnotationPlugin.getCurrentSelectedAttribute();
		if (annotationPage != null) {
			// reset the UI
			annotationPage.reset();
			// now set it
			if (annotation != null) {
				Log.debug(40, "Showing Annotation: " + annotation.getURI());
				annotationPage.setAnnotation(annotation);
				annotationPage.editAttribute(attributeName);
			}
		}
		
		// refresh the context view
		if (contextPage != null) {
			contextPage.handleSelectColumn();
		}
		
		// the graph
		if (annotationGraph != null) {
			annotationGraph = AnnotationGraph.createAnnotationGraph(annotation, true);
			setComponentAt(TAB_NAMES.indexOf(GRAPH_ANNOTATION), annotationGraph);
		}
	}
	
}
