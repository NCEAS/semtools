package org.ecoinformatics.sms.plugins.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.pages.AnnotationPage;

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
	
	public AnnotationTabPane(int tabPlacement) {
		super(tabPlacement);
		
		// add a change listener
		this.addChangeListener(new ChangeListener() {
		    // This method is called whenever the selected tab changes
		    public void stateChanged(ChangeEvent e) {
		        handleSelectTab();
		    }
		});
	}
	
	public void handleStateChange(StateChangeEvent event) {
		
		if (event.getChangedState().equals(StateChangeEvent.SELECT_DATATABLE_COLUMN)) {
			handleSelectColumn();
		}
	}
	
	private void handleSelectColumn() {
		// get the annotation page if it exists
		AnnotationPage annotationPage = null;
		if (getTabCount() == TAB_NAMES.size()) {
			annotationPage = (AnnotationPage) getComponentAt(TAB_NAMES.indexOf(COLUMN_ANNOTATION));
		}
		
		// save the state of the annotation if we are on the annotation tab
		int currentTab = getSelectedIndex();
		if (currentTab == TAB_NAMES.indexOf(COLUMN_ANNOTATION)) {
			persistColumnAnnotation(annotationPage);
		}
		
		// always set the annotation
		showColumnAnnotation(annotationPage);
		
	}
	
	private void handleSelectTab() {
		
		// get the annotation page if it exists
		AnnotationPage annotationPage = null;
		if (getTabCount() == TAB_NAMES.size()) {
			annotationPage = (AnnotationPage) getComponentAt(TAB_NAMES.indexOf(COLUMN_ANNOTATION));
		}
		
		// save when moving away from the annotation tab
		int currentTab = getSelectedIndex();
		if (currentTab != TAB_NAMES.indexOf(COLUMN_ANNOTATION)) {
			if (previousTab == TAB_NAMES.indexOf(COLUMN_ANNOTATION)) {
				persistColumnAnnotation(annotationPage);
			}
		}
		
		// always set the latest state in the annotation tab
		showColumnAnnotation(annotationPage);
		
		previousTab = currentTab;
	}
	
	private void persistColumnAnnotation(AnnotationPage annotationPage) {
		Annotation annotation = annotationPage.getAnnotation();
		if (annotation != null) {
			// reset the UI
			annotationPage.reset();
			// save
			AnnotationPlugin.saveAnnotation(annotation);
		}
	}
	
	private void showColumnAnnotation(AnnotationPage annotationPage) {
		if (annotationPage != null) {
			Annotation annotation = AnnotationPlugin.getCurrentActiveAnnotation();
			String attributeName = AnnotationPlugin.getCurrentSelectedAttribute();
			annotationPage.setAnnotation(annotation);
			annotationPage.editAttribute(attributeName);
		}
	}
	
}
