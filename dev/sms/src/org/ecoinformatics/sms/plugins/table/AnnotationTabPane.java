package org.ecoinformatics.sms.plugins.table;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.pages.AnnotationPage;
import org.ecoinformatics.sms.plugins.pages.ContextPage;
import org.ecoinformatics.sms.renderer.AnnotationGraph;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;

public class AnnotationTabPane extends JTabbedPane implements StateChangeListener {
	
	public static List<String> TAB_NAMES = new ArrayList<String>();
	
	public static String COLUMN_ANNOTATION = Language.getInstance().getMessage("tab.ColumnAnnotation.name");
	public static String CONTEXT_ANNOTATION = Language.getInstance().getMessage("tab.ContextAnnotation.name");
	public static String GRAPH_ANNOTATION = Language.getInstance().getMessage("tab.GraphAnnotation.name");
	public static String FULL_ANNOTATION = Language.getInstance().getMessage("tab.FullAnnotation.name");

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
	
	private AnnotationTablePanel annotationTablePanel;

	
	public AnnotationTabPane(Annotation a, int tabPlacement) {
		super(tabPlacement);
		this.setModel(new AnnotationTabPaneModel(this));
		this.annotation = a;
		
		// add a change listener
		this.addChangeListener(new ChangeListener() {
		    // This method is called whenever the selected tab changes
		    public void stateChanged(ChangeEvent e) {
		    	initHandling();
		    	showAnnotation();
		    }
		});
	}
	
	public boolean hasChanged() {
		boolean hasChanged = false;
		if (getSelectedIndex() == AnnotationTabPane.TAB_NAMES.indexOf(AnnotationTabPane.CONTEXT_ANNOTATION)) {
			if (contextPage != null) {
				if (contextPage.isEnabled()) {
					hasChanged = true;
				}
			}
		}
		if (getSelectedIndex() == AnnotationTabPane.TAB_NAMES.indexOf(AnnotationTabPane.COLUMN_ANNOTATION)) {
			if (annotationPage != null) {
				if (annotationPage.isEnabled()) {
					hasChanged = true;
				}
			}
		}
		return hasChanged;
	}
	
	public void applyChanges() {
		if (getSelectedIndex() == AnnotationTabPane.TAB_NAMES.indexOf(AnnotationTabPane.CONTEXT_ANNOTATION)) {
			if (contextPage != null) {
				if (contextPage.isEnabled()) {
					contextPage.applyChanges();
				}
			}
		}
		if (getSelectedIndex() == AnnotationTabPane.TAB_NAMES.indexOf(AnnotationTabPane.COLUMN_ANNOTATION)) {
			if (annotationPage != null) {
				if (annotationPage.isEnabled()) {
					annotationPage.applyChanges();
				}
			}
		}
	}
	
	public void handleStateChange(StateChangeEvent event) {
		//check if this is for our frame
		MorphoFrame thisAncestor = GUIAction.getMorphoFrameAncestor(this);
		if (!GUIAction.isLocalEvent(event, thisAncestor)) {
			return;
		}
		
		initHandling();
		
		if (event.getChangedState().equals(StateChangeEvent.SELECT_DATATABLE_COLUMN)) {
			showAnnotation();
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
			annotationTablePanel = (AnnotationTablePanel) getComponentAt(TAB_NAMES.indexOf(FULL_ANNOTATION));
		}
	}
	
	private void showAnnotation() {
		// refresh the annotation column view
		String attributeName = AnnotationPlugin.getCurrentSelectedAttribute();
		String dataObject = AnnotationPlugin.getCurrentSelectedEntity();

		// get the latest
		annotation = AnnotationPlugin.getCurrentActiveAnnotation();
		
		// column annotation
		if (getSelectedIndex() == TAB_NAMES.indexOf(COLUMN_ANNOTATION)) {
			if (annotationPage != null) {
				// reset the UI
				annotationPage.reset();
				// now set it
				if (annotation != null) {
					Log.debug(40, "Showing Annotation: " + annotation.getURI());
					annotationPage.setAnnotation(annotation);
					annotationPage.editAttribute(attributeName, dataObject);
				}
			}
		}
		
		// the context view
		if (getSelectedIndex() == TAB_NAMES.indexOf(CONTEXT_ANNOTATION)) {
			if (contextPage != null) {
				contextPage.setAnnotation(annotation);
				contextPage.handleSelectColumn();
			}
		}
		
		// the table view
		if (getSelectedIndex() == TAB_NAMES.indexOf(FULL_ANNOTATION)) {
			if (annotationTablePanel != null) {
				annotationTablePanel.refreshTable();
			}
		}
		
		// the graph
		if (getSelectedIndex() == TAB_NAMES.indexOf(GRAPH_ANNOTATION)) {
			if (annotationGraph != null) {
				annotationGraph = AnnotationGraph.createAnnotationGraph(annotation, true);
				setComponentAt(TAB_NAMES.indexOf(GRAPH_ANNOTATION), annotationGraph);
			}
		}
	}
	
	/**
	 * Use this selection model subclass to 'intercept' the tab selection event.
	 * If there are unsaved changes, then we can continue (discard them), not continue, or maybe even 
	 * save them TBD
	 * @author leinfelder
	 *
	 */
	class AnnotationTabPaneModel extends DefaultSingleSelectionModel {
		private AnnotationTabPane pane;
		
		public AnnotationTabPaneModel(AnnotationTabPane p) {
			this.pane = p;
		}
		
		public void setSelectedIndex(int index) {
			if (canContinue()) {
				super.setSelectedIndex(index);
			}
		}
		
		private boolean canContinue() {
			 if (pane.hasChanged()) {
				int response = JOptionPane.showConfirmDialog(
						pane, 
						Language.getInstance().getMessage("ApplyChanges.prompt"),
						Language.getInstance().getMessage("ApplyChanges.title"),
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.CANCEL_OPTION) {
					return false;
				}
				if (response == JOptionPane.YES_OPTION) {
					pane.applyChanges();
				}
				// NO_OPTION does nothing but continue
			 }
			 return true;
		 }
	}
	
}
