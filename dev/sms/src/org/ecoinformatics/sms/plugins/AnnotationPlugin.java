/**
 * A plugin that imports the design of a Microsoft Access Database.
 * A dataTable node is added for each table found in the database.
 * Imports tables, attributes, attribute descriptions, primary keys and
 * notNullable constraints.
 * 
 * The plugin does not yet import forien keys.
 * 
 * @author Michael Finch
 * @version 1.0
 *
 */

package org.ecoinformatics.sms.plugins;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.plugins.table.AnnotationTableModel;
import org.ecoinformatics.sms.plugins.table.AnnotationTablePanel;
import org.ecoinformatics.sms.plugins.table.DataTableModelListener;
import org.ecoinformatics.sms.plugins.table.ScrollBarAdjustmentListener;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AccessionNumber;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.DataViewer;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.query.Query;
import edu.ucsb.nceas.morpho.query.ResultSet;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.SaveEvent;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

public class AnnotationPlugin
	implements PluginInterface, ServiceProvider, StateChangeListener {
		
    /** Constant int for Annotation menu position */
    public static final int ANNOTATIONMENUPOSITION = 45;
    
    /** Constant String for Annotation menu label */
    public static final String ANNOTATION_MENU_LABEL = "Annotation";
    
    public static final String ANNOTATION_CHANGE_EVENT = "ANNOTATION_SAVED_EVENT";
    
	private MorphoFrame morphoFrame = null;
	
	private GUIAction annotateAction = null;

	private GUIAction splitObservationAction;

	private GUIAction mergeObservationAction;

	private GUIAction addContextAction;

	private GUIAction removeContextAction;
	

	/**
	 * Called when the plugin is created
	 * @param morpho	The running morpho instance
	 * @see edu.ucsb.nceas.morpho.plugins.PluginInterface#initialize(edu.ucsb.nceas.morpho.Morpho)
	 */
	public void initialize(Morpho morpho) {

		try {
			ServiceController services = ServiceController.getInstance();
			services.addService(AnnotationPlugin.class, this);
			Log.debug(20, "Service added: " + this.getClass().getName());

		} catch (ServiceExistsException see) {
			Log.debug(6, "Service registration failed: " + this.getClass().getName());
			Log.debug(6, see.toString());
		}

		// initialize the actions
		annotateAction =
			new GUIAction(
				"Annotate current column...",
				null,
				new AnnotationCommand());
		annotateAction.setToolTipText(
			"Add/edit annotation or this data table attribute");
		annotateAction.setSeparatorPosition(Morpho.SEPARATOR_PRECEDING);
		annotateAction.setMenuItemPosition(0);
		annotateAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
		annotateAction.setEnabled(false);

		annotateAction.setEnabledOnStateChange(
				StateChangeEvent.SELECT_DATATABLE_COLUMN,
				true, GUIAction.EVENT_LOCAL);
		annotateAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                true, GUIAction.EVENT_LOCAL);
		annotateAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                false, GUIAction.EVENT_LOCAL);
		annotateAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                false, GUIAction.EVENT_LOCAL);
		
		mergeObservationAction =
			new GUIAction(
				"Merge Observation",
				null,
				new ObservationCommand(true));
		mergeObservationAction.setToolTipText(
			"Selected columns are for the same Observation");
		mergeObservationAction.setMenuItemPosition(5);
		mergeObservationAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
		mergeObservationAction.setEnabled(false);

		mergeObservationAction.setEnabledOnStateChange(
				StateChangeEvent.SELECT_DATATABLE_COLUMN,
				true, GUIAction.EVENT_LOCAL);
		mergeObservationAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                true, GUIAction.EVENT_LOCAL);
		mergeObservationAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                false, GUIAction.EVENT_LOCAL);
		mergeObservationAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                false, GUIAction.EVENT_LOCAL);
		
		splitObservationAction =
			new GUIAction(
				"Split Observation",
				null,
				new ObservationCommand(false));
		splitObservationAction.setToolTipText(
			"Selected columns are for different Observations");
		splitObservationAction.setMenuItemPosition(6);
		splitObservationAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
		splitObservationAction.setEnabled(false);

		splitObservationAction.setEnabledOnStateChange(
				StateChangeEvent.SELECT_DATATABLE_COLUMN,
				true, GUIAction.EVENT_LOCAL);
		splitObservationAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                true, GUIAction.EVENT_LOCAL);
		splitObservationAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                false, GUIAction.EVENT_LOCAL);
		splitObservationAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                false, GUIAction.EVENT_LOCAL);
		
		addContextAction =
			new GUIAction(
				"Add Context...",
				null,
				new ContextCommand(true));
		addContextAction.setToolTipText(
			"Define a Context relationship between this Observation and another");
		addContextAction.setMenuItemPosition(7);
		addContextAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
		addContextAction.setEnabled(false);

		addContextAction.setEnabledOnStateChange(
				StateChangeEvent.SELECT_DATATABLE_COLUMN,
				true, GUIAction.EVENT_LOCAL);
		addContextAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                true, GUIAction.EVENT_LOCAL);
		addContextAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                false, GUIAction.EVENT_LOCAL);
		addContextAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                false, GUIAction.EVENT_LOCAL);

		removeContextAction =
			new GUIAction(
				"Remove Context...",
				null,
				new ContextCommand(false));
		removeContextAction.setToolTipText(
			"Remove a Context relationship from this Observation");
		removeContextAction.setMenuItemPosition(8);
		removeContextAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
		removeContextAction.setEnabled(false);

		removeContextAction.setEnabledOnStateChange(
				StateChangeEvent.SELECT_DATATABLE_COLUMN,
				true, GUIAction.EVENT_LOCAL);
		removeContextAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                true, GUIAction.EVENT_LOCAL);
		removeContextAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                false, GUIAction.EVENT_LOCAL);
		removeContextAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                false, GUIAction.EVENT_LOCAL);
		
		// Save Annotations
	    GUIAction saveAction = new GUIAction("Save Annotations...",
	                                              null,
	                                              new SaveAnnotationCommand());
	    saveAction.setMenuItemPosition(10);
	    saveAction.setToolTipText("Save Annotations...");
		saveAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
	    saveAction.setEnabled(false);
	    saveAction.setEnabledOnStateChange(
				StateChangeEvent.SELECT_DATATABLE_COLUMN,
				true, GUIAction.EVENT_LOCAL);
	    saveAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                true, GUIAction.EVENT_LOCAL);
	    saveAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                false, GUIAction.EVENT_LOCAL);
	    saveAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                false, GUIAction.EVENT_LOCAL);
	    
	    GUIAction searchAction = new GUIAction("Search Annotations...",
                null,
                new AnnotationSearchCommand());
	    searchAction.setMenuItemPosition(15);
	    searchAction.setToolTipText("Search Annotations...");
	    searchAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
	    searchAction.setEnabled(true);
			    
		// add the custom actions
		UIController controller = UIController.getInstance();
		controller.addGuiAction(annotateAction);
		controller.addGuiAction(mergeObservationAction);
		controller.addGuiAction(splitObservationAction);
		controller.addGuiAction(addContextAction);
		controller.addGuiAction(removeContextAction);
		controller.addGuiAction(saveAction);
		controller.addGuiAction(searchAction);

		
		//register as a listener for events
		StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME, this);
		StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.SELECT_DATA_VIEWER, this);
		StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.SAVE_DATAPACKAGE, this);
		
		// initialize the annotations
		initializeAnnotations();
	}
	
	// TODO: search Metacat for annotations
	private void initializeAnnotations() {
		FileSystemDataStore fds = new FileSystemDataStore(Morpho.thisStaticInstance);
		String querySpec = getAnnotationQuery();
		Query query = new Query(querySpec, Morpho.thisStaticInstance);
		query.setSearchLocal(true);
		query.setSearchMetacat(false);
		ResultSet rs = query.execute();
		Vector<Vector> resultVector = rs.getResultsVector();
		Vector<String> docids = new Vector<String>();
		for (Vector row: resultVector) {
			String docid = (String) row.get(ResultSet.DOCIDINDEX);
			try {
				InputStream is = new FileInputStream(fds.openFile(docid));
				SMS.getInstance().getAnnotationManager().importAnnotation(is, docid);
			} catch (Exception e) {
				e.printStackTrace();
			}
			docids.add(docid);
		}
		
	}
	
	public static String getAnnotationQuery()
	  {
	  	ConfigXML config = Morpho.getConfiguration();
	  	ConfigXML profile = Morpho.thisStaticInstance.getProfile();
	    StringBuffer searchtext = new StringBuffer();
	    searchtext.append("<?xml version=\"1.0\"?>\n");
	    searchtext.append("<pathquery version=\"1.0\">\n");
	    String lastname = profile.get("lastname", 0);
	    String firstname = profile.get("firstname", 0);
	    searchtext.append("<querytitle>My Annotations (" + firstname + " " + lastname);
	    searchtext.append(")</querytitle>\n");
	    //Vector returnDoctypeList = config.get("returndoc");
	    Vector<String> returnDoctypeList = new Vector<String>();
	    returnDoctypeList.add("http://ecoinformatics.org/sms/annotation.0.9");
	    for (int i=0; i < returnDoctypeList.size(); i++) {
	      searchtext.append("<returndoctype>");
	      searchtext.append((String)returnDoctypeList.elementAt(i));
	      searchtext.append("</returndoctype>\n");
	    }
	    Vector returnFieldList = config.get("returnfield");
	    for (int i=0; i < returnFieldList.size(); i++) {
	      searchtext.append("<returnfield>");
	      searchtext.append((String)returnFieldList.elementAt(i));
	      searchtext.append("</returnfield>\n");
	    }
	    searchtext.append("<owner>" + Morpho.thisStaticInstance.getUserName() + "</owner>\n");
	    searchtext.append("<querygroup operator=\"UNION\">\n");
	    searchtext.append("<queryterm casesensitive=\"true\" ");
	    searchtext.append("searchmode=\"contains\">\n");
	    searchtext.append("<value>%</value>\n");
	    searchtext.append("</queryterm></querygroup></pathquery>");
	    return searchtext.toString();
	  }
	
	public static String getDocQuery(List<Annotation> annotations) {
		ConfigXML config = Morpho.getConfiguration();
		ConfigXML profile = Morpho.thisStaticInstance.getProfile();
		
		StringBuffer searchtext = new StringBuffer();
		searchtext.append("<?xml version=\"1.0\"?>\n");
		searchtext.append("<pathquery version=\"1.0\">\n");
		String lastname = profile.get("lastname", 0);
		String firstname = profile.get("firstname", 0);
		searchtext.append("<querytitle>Matching Docs for Annotations (" + firstname + " "
				+ lastname);
		searchtext.append(")</querytitle>\n");
		Vector<String> returnDoctypeList = config.get("returndoc");
		//Vector<String> returnDoctypeList = new Vector<String>();
		//returnDoctypeList.add("http://ecoinformatics.org/sms/annotation.0.9");
		for (int i = 0; i < returnDoctypeList.size(); i++) {
			searchtext.append("<returndoctype>");
			searchtext.append(returnDoctypeList.elementAt(i));
			searchtext.append("</returndoctype>\n");
		}
		Vector<String> returnFieldList = config.get("returnfield");
		for (int i = 0; i < returnFieldList.size(); i++) {
			searchtext.append("<returnfield>");
			searchtext.append(returnFieldList.elementAt(i));
			searchtext.append("</returnfield>\n");
		}
		searchtext.append("<owner>" + Morpho.thisStaticInstance.getUserName()
				+ "</owner>\n");
		
		//Annotation matches
		if (annotations != null && annotations.size() > 0) {
			searchtext.append("<querygroup operator=\"UNION\">\n");
		
			for (Annotation annotation: annotations) {
				searchtext.append("<queryterm casesensitive=\"true\" ");
				searchtext.append("searchmode=\"contains\">\n");
				searchtext.append("<value>");
				searchtext.append(annotation.getEMLPackage());
				searchtext.append("</value>\n");
				searchtext.append("<pathexpr>@packageId</pathexpr>\n");
				searchtext.append("</queryterm>");
			}
			searchtext.append("</querygroup>");
		}
	
		searchtext.append("</pathquery>");


		return searchtext.toString();
	}
	
	public static void saveAnnotation(Annotation annotation) {
		
		try {
			
			// about to save
			AccessionNumber accNum = new AccessionNumber(Morpho.thisStaticInstance);
			String id = annotation.getURI();
			if (id == null) {
				id = accNum.getNextId();
			} else {
				// remove the old one if present
				if (SMS.getInstance().getAnnotationManager().isAnnotation(id)) {
					SMS.getInstance().getAnnotationManager().removeAnnotation(id);
				}
				id = accNum.incRev(id);
			}
			annotation.setURI(id);
			
			//save in the manager
			SMS.getInstance().getAnnotationManager().importAnnotation(annotation, annotation.getURI());
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// TODO: handle remote locations
	public static void serializeAnnotation(String packageId) {
		FileSystemDataStore fds = new FileSystemDataStore(Morpho.thisStaticInstance);
		
		// get the annotations for this datapackage
		List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getAnnotations(packageId, null);		
		for (Annotation annotation: annotations) {	
			String id = annotation.getURI();
			// save if no file for this docid
			if (fds.status(id).equals(FileSystemDataStore.NONEXIST)) {
				//save in local store
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				annotation.write(baos);
				File annotationFile = fds.saveFile(id, new StringReader(baos.toString()));
			}
		}
	}

	public void handleStateChange(StateChangeEvent event) {

		// initialize data-table centric UI elements
		if (
				event.getChangedState().equals(StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME)
				||
				event.getChangedState().equals(StateChangeEvent.SELECT_DATA_VIEWER)
			) {
			if (!isInitialized()) {
				initPopup();
				try {
					buildAnnotationTable();
				} catch (Exception e) {
					Log.debug(5, "Could not build annotation table");
					e.printStackTrace();
				}
			}
		}
		
		if (event instanceof SaveEvent) {
			SaveEvent saveEvent = (SaveEvent) event;
			handleSaveEvent(saveEvent);
		}

	}
	
	private void handleSaveEvent(SaveEvent saveEvent) {
		// get the old and new ids for the package tha was saved
		String oldId = saveEvent.getInitialId();
		String newId = saveEvent.getFinalId();
		
		// get all the annotations for the original docid
		List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getAnnotations(oldId, null);
		for (Annotation annotation: annotations) {
			// set the updated packageId
			annotation.setEMLPackage(newId);
			// save the annotation
			saveAnnotation(annotation);
			// serialize to disk
			serializeAnnotation(newId);
		}
	}
	
	private boolean isInitialized() {

		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		if (morphoFrame != null) {
			DataViewContainerPanel dataViewContainerPanel = morphoFrame.getDataViewContainerPanel();
			if (dataViewContainerPanel != null) {
				DataViewer dataViewer = dataViewContainerPanel.getCurrentDataViewer();
				if (dataViewer != null) {
					Component[] existingComponents = dataViewer.getHeaderPanel().getComponents();
					for (Component comp: existingComponents) {
						if (comp instanceof AnnotationTablePanel) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private void initPopup() {
		morphoFrame = UIController.getInstance().getCurrentActiveWindow();

		if (morphoFrame != null) {
			DataViewContainerPanel resultPane = 
				morphoFrame.getDataViewContainerPanel();
			if (resultPane != null) {
				DataViewer dataView = resultPane.getCurrentDataViewer();
				if (dataView != null) {
					annotateAction.setEnabled(true);
					dataView.addPopupMenuItem(annotateAction, true);
					mergeObservationAction.setEnabled(true);
					dataView.addPopupMenuItem(mergeObservationAction, false);
					splitObservationAction.setEnabled(true);
					dataView.addPopupMenuItem(splitObservationAction, false);
					addContextAction.setEnabled(true);
					dataView.addPopupMenuItem(addContextAction, false);
					removeContextAction.setEnabled(true);
					dataView.addPopupMenuItem(removeContextAction, false);
				}
			}
		}
	}
	
	private void buildAnnotationTable() throws Exception {

		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		if (morphoFrame != null) {
			DataViewContainerPanel dataViewContainerPanel = morphoFrame.getDataViewContainerPanel();
			if (dataViewContainerPanel != null) {
				DataViewer dataViewer = dataViewContainerPanel.getCurrentDataViewer();
				if (dataViewer != null) {
					AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
					// package and entity
					String packageId = adp.getAccessionNumber();
					int entityIndex = dataViewer.getEntityIndex();
					String dataTable = String.valueOf(entityIndex);
					
					// look up the annotation if it exists, or make new one
					List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getAnnotations(packageId, dataTable);
					Annotation annotation = null;
					if (annotations.size() > 0) {
						annotation = annotations.get(0);
					} else {
						// create a new one
						annotation = new Annotation();
						annotation.setEMLPackage(packageId);
						annotation.setDataTable(dataTable);
						// remember it
						saveAnnotation(annotation);
					}
					
					// set up the table model
					List<String> columns = adp.getAttributeNames(entityIndex);
					
					AnnotationTableModel annotationTableModel = new AnnotationTableModel(annotation, columns);
					AnnotationTablePanel annotationTablePanel = new AnnotationTablePanel(annotationTableModel);
					StateChangeMonitor.getInstance().addStateChangeListener(ANNOTATION_CHANGE_EVENT, annotationTablePanel);

					// swap in the dataviewer's column headers...pretty nifty
					TableColumnModel columnModel = dataViewer.getDataTable().getColumnModel();
					annotationTablePanel.getAnnotationTable().setColumnModel(columnModel);
					annotationTableModel.setColumnNames(dataViewer.getColumnLabels());
										
					// add row header space to the viewer
					JLabel filler = new JLabel("Data");
					filler.setPreferredSize(AnnotationTablePanel.rowHeaderDim);
					dataViewer.getDataScrollPanel().setRowHeaderView(filler);
					
					// share the mouse listeners ?
					boolean shareSelection = true;
					if (shareSelection) {
						MouseListener[] listeners = dataViewer.getDataTable().getMouseListeners();
						for (MouseListener l: listeners) {
							annotationTablePanel.getAnnotationTable().addMouseListener(l);
						}
					} else {
						// TODO: different selection model for the annotation table	alone
					}
					
					// share the header mouse listeners
					boolean hideTableHeader = true;
					if (hideTableHeader) {
						annotationTablePanel.getAnnotationTable().setTableHeader(null);
//						annotationTablePanel.getAnnotationScrollPane().setColumnHeaderView(new JLabel("Annotations"));
//						annotationTablePanel.getAnnotationScrollPane().getColumnHeader().setPreferredSize(AnnotationTablePanel.rowHeaderDim);

					} else {
						MouseListener[] headerListeners = dataViewer.getDataTable().getTableHeader().getMouseListeners();
						for (MouseListener l: headerListeners) {
							annotationTablePanel.getAnnotationTable().getTableHeader().addMouseListener(l);
						}
					}
					
					//track the scrolling of the data - forward it to the annotation
					AdjustmentListener annotationScrollListener = 
						new ScrollBarAdjustmentListener(
								annotationTablePanel.getAnnotationScrollPane().getHorizontalScrollBar());
					dataViewer.getDataScrollPanel().getHorizontalScrollBar().addAdjustmentListener(annotationScrollListener);
					
					//track the scrolling of the annotation - forward it to the data
					AdjustmentListener dataScrollListener = 
						new ScrollBarAdjustmentListener(
								dataViewer.getDataScrollPanel().getHorizontalScrollBar());
					annotationTablePanel.getAnnotationScrollPane().getHorizontalScrollBar().addAdjustmentListener(dataScrollListener);

					// listen for data model changes
					TableModelListener dataModelListener = new DataTableModelListener(adp, annotationTableModel, dataViewer, entityIndex);
					dataViewer.getDataTable().getModel().addTableModelListener(dataModelListener);
					 
					// add to the dataviewer panel
					dataViewer.getHeaderPanel().add(BorderLayout.CENTER, annotationTablePanel);

					Log.debug(30, "Set up annotation table...\n " 
							+ "Data package: " + packageId 
							+ ", entity: " + entityIndex 
							+ ", annotation id: " + annotation.getURI()
							);

				}
			}
		}
		
	}

}
