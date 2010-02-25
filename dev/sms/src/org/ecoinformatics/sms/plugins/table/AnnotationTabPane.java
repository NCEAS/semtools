package org.ecoinformatics.sms.plugins.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.pages.AnnotationPage;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;

public class AnnotationTabPane extends JTabbedPane implements StateChangeListener {
	
	public static List<String> TAB_NAMES = new ArrayList<String>();
	
	public static String FULL_ANNOTATION = "Full Annotation";
	public static String COLUMN_ANNOTATION = "Column Annotation";
	public static String CONTEXT_ANNOTATION = "Context Annotation";
	
	static {
		TAB_NAMES.add(FULL_ANNOTATION);
		TAB_NAMES.add(COLUMN_ANNOTATION);
		TAB_NAMES.add(CONTEXT_ANNOTATION);
	}
	
	private int previousTab = -1;

	private AnnotationPage annotationPage;
	
	public AnnotationTabPane(int tabPlacement) {
		super(tabPlacement);
		
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
			showColumnAnnotation();
		}
	}
	
	private void initHandling() {
		// get the annotation page no matter what
		if (getTabCount() == TAB_NAMES.size()) {
			annotationPage = (AnnotationPage) getComponentAt(TAB_NAMES.indexOf(COLUMN_ANNOTATION));
		}
	}
	
	private void handleSelectColumn() {

		// save the state of the annotation if we are on the annotation tab
		int currentTab = getSelectedIndex();
		Log.debug(40, "Column selected, Tab = " + currentTab );

		if (currentTab == TAB_NAMES.indexOf(COLUMN_ANNOTATION)) {
			persistColumnAnnotation();
		}
		
		// always set the annotation
		showColumnAnnotation();
		
	}
	
	private void handleSelectTab() {
		
		// save when moving away from the annotation tab
		int currentTab = getSelectedIndex();
		Log.debug(40, "Tab selected, Tab = " + currentTab );

		if (currentTab != TAB_NAMES.indexOf(COLUMN_ANNOTATION)) {
			if (previousTab == TAB_NAMES.indexOf(COLUMN_ANNOTATION)) {
				persistColumnAnnotation();
			}
		}
		
		// always set the latest state in the annotation tab
		showColumnAnnotation();
		
		previousTab = currentTab;
	}
	
	private void persistColumnAnnotation() {
		Annotation annotation = annotationPage.getAnnotation();
		if (annotation != null) {

			Log.debug(40, "Persisting Annotation: " + annotation.getURI() );

			// save
			AnnotationPlugin.saveAnnotation(annotation);
		}
	}
	
	private void showColumnAnnotation() {
		Annotation annotation = AnnotationPlugin.getCurrentActiveAnnotation();
		String attributeName = AnnotationPlugin.getCurrentSelectedAttribute();
		if (annotationPage != null) {
			// reset the UI
			annotationPage.reset();
		}
		// now set it
		if (annotation != null) {
			Log.debug(40, "Showing Annotation: " + annotation.getURI());
			annotationPage.setAnnotation(annotation);
			annotationPage.editAttribute(attributeName);
		}
	}
	
}
