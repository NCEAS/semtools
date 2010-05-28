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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.AnnotationException;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.commands.AnnotationCommand;
import org.ecoinformatics.sms.plugins.commands.CompoundAnnotationSearchCommand;
import org.ecoinformatics.sms.plugins.commands.OntologyManagementCommand;
import org.ecoinformatics.sms.plugins.commands.RemoveCommand;
import org.ecoinformatics.sms.plugins.commands.SaveAnnotationCommand;
import org.ecoinformatics.sms.plugins.commands.ViewAnnotationCommand;
import org.ecoinformatics.sms.plugins.pages.AnnotationPage;
import org.ecoinformatics.sms.plugins.pages.ContextPage;
import org.ecoinformatics.sms.plugins.table.AnnotationTabPane;
import org.ecoinformatics.sms.plugins.table.AnnotationTableModel;
import org.ecoinformatics.sms.plugins.table.AnnotationTablePanel;
import org.ecoinformatics.sms.plugins.table.DataTableModelListener;
import org.ecoinformatics.sms.plugins.table.ScrollBarAdjustmentListener;
import org.ecoinformatics.sms.renderer.AnnotationGraph;
import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AccessionNumber;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.DataViewer;
import edu.ucsb.nceas.morpho.datastore.DataStoreInterface;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.query.Query;
import edu.ucsb.nceas.morpho.query.ResultSet;
import edu.ucsb.nceas.morpho.util.DeleteEvent;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.SaveEvent;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.utilities.XMLUtilities;

public class AnnotationPlugin
	implements PluginInterface, ServiceProvider, StateChangeListener {
		
    /** Constant int for Annotation menu position */
    public static final int ANNOTATIONMENUPOSITION = 45;
    
    /** Constant String for Annotation menu label */
    public static final String ANNOTATION_MENU_LABEL = "Annotation";
    
    public static final String ANNOTATION_CHANGE_EVENT = "ANNOTATION_SAVED_EVENT";
    
    /** Constants for ConfigXML entries **/
    public static final String ONTOLOGY_TAG_NAME = "ontologies";
    
    public static final String LOGICAL_URI_TAG_NAME = "logicalURI";

    public static final String PHYSICAL_URI_TAG_NAME = "physicalURI";
    
    public static final String ANNOTATION_LOCATION = AbstractDataPackage.LOCAL;
    //public static final String ANNOTATION_LOCATION = AbstractDataPackage.METACAT;
    //public static final String ANNOTATION_LOCATION = AbstractDataPackage.BOTH;
    
    private MorphoFrame morphoFrame = null;
	
	private GUIAction annotateAction = null;

	private GUIAction removeObservationAction;

	private GUIAction removeMeasurementAction;
	

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

		int menuPosition = 0;
		
		// initialize the actions
		annotateAction =
			new GUIAction(
				"Annotate current column...",
				null,
				new AnnotationCommand());
		annotateAction.setToolTipText(
			"Add/edit annotation or this data table attribute");
		annotateAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
		annotateAction.setMenuItemPosition(menuPosition++);
		annotateAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
		annotateAction.setEnabled(false);

		annotateAction.setEnabledOnStateChange(
				StateChangeEvent.SELECT_DATATABLE_COLUMN,
				true, GUIAction.EVENT_LOCAL);
		
		removeObservationAction =
			new GUIAction(
				"Remove Observation",
				null,
				new RemoveCommand(Observation.class));
		removeObservationAction.setToolTipText(
			"Remove this Observation");
		removeObservationAction.setMenuItemPosition(menuPosition++);
		removeObservationAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
		removeObservationAction.setEnabled(false);

		removeObservationAction.setEnabledOnStateChange(
				StateChangeEvent.SELECT_DATATABLE_COLUMN,
				true, GUIAction.EVENT_LOCAL);
		
		removeMeasurementAction =
			new GUIAction(
				"Remove Measurement",
				null,
				new RemoveCommand(Measurement.class));
		removeMeasurementAction.setToolTipText(
			"Remove this Measurement");
		removeMeasurementAction.setMenuItemPosition(menuPosition++);
		removeMeasurementAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
		removeMeasurementAction.setEnabled(false);

		removeMeasurementAction.setEnabledOnStateChange(
				StateChangeEvent.SELECT_DATATABLE_COLUMN,
				true, GUIAction.EVENT_LOCAL);
	    
		// view Annotation
	    GUIAction viewAction = 
	    	new GUIAction(
	    			"View Annotation",
	    			null,
	    			new ViewAnnotationCommand());
	    viewAction.setMenuItemPosition(menuPosition++);
	    viewAction.setToolTipText("View Annotation");
	    viewAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
	    viewAction.setSeparatorPosition(Morpho.SEPARATOR_PRECEDING);
	    viewAction.setEnabled(false);
	    viewAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                true, GUIAction.EVENT_LOCAL);
	    viewAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                false, GUIAction.EVENT_LOCAL);
	    viewAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                false, GUIAction.EVENT_LOCAL);
	    
	    // Save Annotations
	    GUIAction saveAction = 
	    	new GUIAction(
	    			"Save Annotations",
	    			null,
	    			new SaveAnnotationCommand());
	    saveAction.setMenuItemPosition(menuPosition++);
	    saveAction.setToolTipText("Save Annotations");
		saveAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
		saveAction.setSeparatorPosition(Morpho.SEPARATOR_PRECEDING);
	    saveAction.setEnabled(false);
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
                new CompoundAnnotationSearchCommand());
	    searchAction.setMenuItemPosition(menuPosition++);
	    searchAction.setToolTipText("Search Annotations...");
	    searchAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
	    searchAction.setEnabled(true);
	    
	    GUIAction manageOntologyAction = new GUIAction("Manage Ontologies...",
                null,
                new OntologyManagementCommand());
	    manageOntologyAction.setMenuItemPosition(menuPosition++);
	    manageOntologyAction.setSeparatorPosition(Morpho.SEPARATOR_PRECEDING);
	    manageOntologyAction.setToolTipText("Manage Ontologies...");
	    manageOntologyAction.setMenu(ANNOTATION_MENU_LABEL, ANNOTATIONMENUPOSITION);
	    manageOntologyAction.setEnabled(true);
			    
		// add the custom actions
		UIController controller = UIController.getInstance();
		controller.addGuiAction(annotateAction);
		controller.addGuiAction(removeObservationAction);
		controller.addGuiAction(removeMeasurementAction);
		controller.addGuiAction(viewAction);
		controller.addGuiAction(saveAction);
		controller.addGuiAction(searchAction);
		controller.addGuiAction(manageOntologyAction);
		
		//register as a listener for events
		StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME, this);
		StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.SELECT_DATA_VIEWER, this);
		StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.SAVE_DATAPACKAGE, this);
		StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.DELETE_DATAPACKAGE, this);
		
		//initialize the ontologies
		initializeOntologies();
		
		// initialize the annotations
		initializeAnnotations(null, ANNOTATION_LOCATION);
		
	}
	
	/**
	 * This method adds a window adaptor to clean up annotations in memory 
	 * that should not be saved when the data package is re-opened.
	 * Similarly, it removes listeners that should no longer be active 
	 * (as they are listening to closed packages).
	 * We need to get a reference to the gui component so that it is available 
	 * in the windowClosed event, otherwise it is null.
	 */
	private void initializeWindowAdapter() {
		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		if (morphoFrame != null) {
			final DataViewContainerPanel dataViewContainerPanel = morphoFrame.getDataViewContainerPanel();
			WindowAdapter windowListener = new WindowAdapter() {
				public void windowClosed(WindowEvent we) {
					AbstractDataPackage adp = morphoFrame.getAbstractDataPackage();
					String docid = adp.getAccessionNumber();
					// only clear annotations if we can look them up again
					String location = adp.getLocation();
					if (!location.equals("")) {
						clearAnnotations(docid);
						initializeAnnotations(docid, location);
					}
					removeStateChangeListeners(dataViewContainerPanel);
					morphoFrame.removeWindowListener(this);		
				}
			};
			morphoFrame.addWindowListener(windowListener);
		}
	}
	
	// load annotations from a given location
	private static void initializeAnnotations(String forDocid, String location) {
		Log.debug(30, "initializing annotations for docid: " + forDocid);
		FileSystemDataStore fds = new FileSystemDataStore(Morpho.thisStaticInstance);
		MetacatDataStore mds = new MetacatDataStore(Morpho.thisStaticInstance);
		String querySpec = getAnnotationQuery(forDocid);
		Query query = new Query(querySpec, Morpho.thisStaticInstance);
		// search BOTH by default
		query.setSearchLocal(true);
		query.setSearchMetacat(true);
		if (location.equals(AbstractDataPackage.LOCAL)) {
			query.setSearchLocal(true);
			query.setSearchMetacat(false);
		}
		if (location.equals(AbstractDataPackage.METACAT)) {
			query.setSearchLocal(false);
			query.setSearchMetacat(true);
		}
		ResultSet rs = query.execute();
		Vector<Vector> resultVector = rs.getResultsVector();
		for (Vector row: resultVector) {
			String docid = (String) row.get(ResultSet.DOCIDINDEX);
			String isLocal = (String) row.get(ResultSet.ISLOCALINDEX);
			String isMetacat = (String) row.get(ResultSet.ISMETACATINDEX);
			File fileSource = null;
			
			try {
				if (isLocal.equals(QueryRefreshInterface.LOCALCOMPLETE)) {
					fileSource = fds.openFile(docid);
					Log.debug(30, "loading annotation from LOCAL source: " + docid);
				}
				else if (isMetacat.equals(QueryRefreshInterface.NETWWORKCOMPLETE)) {
					fileSource = mds.openFile(docid);
					Log.debug(30, "loading annotation from METACAT source: " + docid);
				}
				InputStream is = new FileInputStream(fileSource);
				Annotation annotation = Annotation.read(is);
				// if we are filtering, skip any docs that don't match
				if (forDocid != null) {
					String docidFromAnnotation = annotation.getEMLPackage();
					if (!forDocid.equals(docidFromAnnotation)) {
						// not what we are looking for
						continue;
					}
				}
				Log.debug(30, "importing annotation: " + docid);
				SMS.getInstance().getAnnotationManager().importAnnotation(annotation, fileSource.toURI().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Remove annotations that exist for a given doc id
	 * @param forDocid
	 */
	private static void clearAnnotations(String forDocid) {
		// remove the current existing annotations for EML
		List<Annotation> existingAnnotations = SMS.getInstance().getAnnotationManager().getAnnotations(forDocid, null);
		for (Annotation existingAnnotation: existingAnnotations) {
			Log.debug(30, "removing annotation: " + existingAnnotation.getURI());
			try {
				SMS.getInstance().getAnnotationManager().removeAnnotation(existingAnnotation.getURI());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void initializeOntologies() {
		// clear the ontologies that are loaded
		for (String uri:SMS.getInstance().getOntologyManager().getOntologyIds()) {
			if (SMS.getInstance().getOntologyManager().isOntology(uri)) {
				SMS.getInstance().getOntologyManager().removeOntology(uri);
			}
		}
		// load the configured ontologies
		Hashtable<String, String> ontologyURIs = Morpho.getConfiguration().getHashtable(ONTOLOGY_TAG_NAME, LOGICAL_URI_TAG_NAME, PHYSICAL_URI_TAG_NAME);
		for (Entry<String, String> entry: ontologyURIs.entrySet()) {
			String uri = entry.getKey();
			String url = entry.getValue();
			try {
				SMS.getInstance().getOntologyManager().importOntology(url, uri);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// initialize the static classes
		Hashtable<String, String> oboeClasses = Morpho.getConfiguration().getHashtable("oboeClasses", "className", "classURI");
		Annotation.OBOE_CLASSES.clear();
		for (Entry<String, String> entry: oboeClasses.entrySet()) {
			String name = entry.getKey();
			String uri = entry.getValue();
			try {
				Object objectClass = Class.forName(name).newInstance();
				OntologyClass oboeClass = null;
				if (objectClass instanceof OntologyClass) {
					oboeClass = (OntologyClass) objectClass;
				} else {
					oboeClass = new OntologyClass();
				}
				oboeClass.setURI(uri);
				Annotation.OBOE_CLASSES.put(objectClass.getClass(), oboeClass);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//SMS.getInstance().getOntologyManager().importOntology("http://ecoinformatics.org/oboe/oboe.1.0beta");
		//SMS.getInstance().getOntologyManager().importOntology("http://ecoinformatics.org/oboe/oboe-units.1.0beta");
		//SMS.getInstance().getOntologyManager().importOntology("https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-sbc.owl");
		//SMS.getInstance().getOntologyManager().importOntology("https://code.ecoinformatics.org/code/semtools/trunk/dev/oboe/oboe-gce.owl");
	}
	
	public static String getAnnotationQuery(String forDocid)
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
	    //searchtext.append("<owner>" + Morpho.thisStaticInstance.getUserName() + "</owner>\n");
	    searchtext.append("<querygroup operator=\"INTERSECT\">\n");
	    searchtext.append("<queryterm casesensitive=\"true\" ");
	    searchtext.append("searchmode=\"contains\">\n");
	    searchtext.append("<value>%</value>\n");
	    searchtext.append("</queryterm>");
	    if (forDocid != null) {
		    searchtext.append("<queryterm casesensitive=\"true\" ");
		    searchtext.append("searchmode=\"contains\">\n");
		    searchtext.append("<value>" + forDocid + "</value>\n");
			searchtext.append("<pathexpr>@emlPackage</pathexpr>\n");
		    searchtext.append("</queryterm>");
	    }
	    searchtext.append("</querygroup></pathquery>");
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
	
	public static boolean saveAnnotation(Annotation annotation) {
		
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
				//id = accNum.incRev(id);
			}
			annotation.setURI(id);
			
			//save in the manager
			// TODO: source should actually be a source
			SMS.getInstance().getAnnotationManager().importAnnotation(annotation, null);
			
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void deleteAnnotations(String packageId, String location) {
		FileSystemDataStore fds = new FileSystemDataStore(Morpho.thisStaticInstance);
		MetacatDataStore mds = new MetacatDataStore(Morpho.thisStaticInstance);
		
		List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getAnnotations(packageId, null);
		for (Annotation annotation: annotations) {
			try {
				String annotationId = annotation.getURI();
				// remove from manager
				SMS.getInstance().getAnnotationManager().removeAnnotation(annotationId);
				// remove from storage
				if (location.equals(AbstractDataPackage.LOCAL) || location.equals(AbstractDataPackage.BOTH)) {
					fds.deleteFile(annotationId);
				}
				if (location.equals(AbstractDataPackage.METACAT) || location.equals(AbstractDataPackage.BOTH)) {
					mds.deleteFile(annotationId);
				}
			} catch (Exception e) {
				Log.debug(5, "Error removing annotation: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
	}
	
	public static void setAccess(String packageId, String annotationId) {
		// TODO: better way to get this dp?
		AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
		if (!adp.getAccessionNumber().equals(packageId)) {
			return;
		}
		String accessXML = null;
		
		// get the access node
		Node accessNode = adp.getSubtree("access", 0);
		try {
			accessXML = XMLUtilities.getDOMTreeAsString(accessNode);
			Log.debug(30, "Access XML: \n" + accessXML);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		MetacatDataStore mds = new MetacatDataStore(Morpho.thisStaticInstance);
		mds.setAccess(annotationId, accessXML);
		
	}
	
	// TODO: handle remote locations
	public static boolean serializeAnnotation(String packageId, String location) {
		FileSystemDataStore fds = new FileSystemDataStore(Morpho.thisStaticInstance);
		MetacatDataStore mds = new MetacatDataStore(Morpho.thisStaticInstance);
		AccessionNumber accNum = new AccessionNumber(Morpho.thisStaticInstance);

		// we can' save to nowhere
		if (location == null || location.length() == 0) {
			Log.debug(30, "Ignoring save event because location is empty");
			return false;
		}
		
		// get the annotations for this datapackage
		List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getAnnotations(packageId, null);		
		for (Annotation annotation: annotations) {	
			String id = annotation.getURI();
			String originalId = id;
			ByteArrayOutputStream baos = null;
			File annotationFile = null;
			
			// local save
			if (location.equals(AbstractDataPackage.LOCAL) || location.equals(AbstractDataPackage.BOTH)) {
				try {
					
					// find the next available id
					while (!fds.status(id).equals(DataStoreInterface.NONEXIST)) {
						id = accNum.incRev(id);
					}
					// set it in the annotation
					annotation.setURI(id);
					// save to local store
				
					//save in local store
					baos = new ByteArrayOutputStream();
					annotation.write(baos);
					annotationFile = fds.saveFile(id, new StringReader(baos.toString()));
					
				}
				catch (Exception e) {
					Log.debug(5, 
							"Error saving annotation: " + id
							+ "\nMessage: " + e.getMessage()
							);
					e.printStackTrace();
					return false;
				}
			}
			
			// network
			if (location.equals(AbstractDataPackage.METACAT) || location.equals(AbstractDataPackage.BOTH)) {
				try {
					String metacatStatus = mds.status(id);
					// resolve id conflict first
					while (metacatStatus.equals(DataStoreInterface.CONFLICT)) {
						id = accNum.incRev(id);
						metacatStatus = mds.status(id);
					}
					annotation.setURI(id);
					baos = new ByteArrayOutputStream();
					annotation.write(baos);
					// check if we will update or make new file
					if (metacatStatus.equals(DataStoreInterface.UPDATE)) {
						annotationFile = mds.saveFile(id, new StringReader(baos.toString()));
					}
					else if (metacatStatus.equals(DataStoreInterface.NONEXIST)) {
						annotationFile = mds.newFile(id, new StringReader(baos.toString()));
					}
					// set permissions for the annotation file
					setAccess(packageId, id);
				} catch (Exception e) {
					Log.debug(5, "Error saving annotation to network: " + id
							+ "\nMessage: " + e.getMessage()
							);
					e.printStackTrace();
					return false;
				}
			}
			
			// manage the annotation
			try {
				// remove old one if it exists
				if (SMS.getInstance().getAnnotationManager().isAnnotation(originalId)) {
					SMS.getInstance().getAnnotationManager().removeAnnotation(originalId);
				}
				// [re]import new one, possibily with source
				String source = null;
				if (annotationFile != null) {
					source = annotationFile.toURI().toString();
				}
				SMS.getInstance().getAnnotationManager().importAnnotation(annotation, source);
			} catch (Exception e) {
				Log.debug(5, "Error while reimporting saved annotation: " + id + "\nMessage: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Consolidates the annotation retrieval code that has been used in
	 * most of the annotation editing classes.
	 * returns the currently selected attribute
	 * @return currently selected attributeName
	 */
	public static String getCurrentSelectedAttribute() {

		String attributeName = null;

		//now go on with the current selection
		MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();

		DataViewContainerPanel resultPane = null;
		if (morphoFrame != null) {
			resultPane = morphoFrame.getDataViewContainerPanel();
		}

		AbstractDataPackage adp = null;
		if (resultPane != null) {
			adp = resultPane.getAbstractDataPackage();
		}

		if (adp == null) {
			Log.debug(16, " Abstract Data Package is null in "
					+ AnnotationPlugin.class.getName());
			return attributeName;
		}

		// make sure resultPanel is not null
		if (resultPane != null) {
			DataViewer dataView = resultPane.getCurrentDataViewer();
			if (dataView != null) {

				JTable table = dataView.getDataTable();
				int viewIndex = table.getSelectedColumn();
				if (viewIndex < 0) {
					return attributeName;
				}
				int entityIndex = dataView.getEntityIndex();

				// attribute
				int attributeIndex = table.getColumnModel().getColumn(viewIndex).getModelIndex();
				attributeName = adp.getAttributeName(entityIndex, attributeIndex);
			}
		}
		return attributeName;
	}
	
	/**
	 * Consolidates the annotation retrieval code that has been used in
	 * most of the annotation editing classes.
	 * and returns the currently selected attribute
	 * @return current annotation for the current DP/entity
	 */
	public static Annotation getCurrentActiveAnnotation() {

		Annotation annotation = null;

		//now go on with the current selection
		MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();

		DataViewContainerPanel resultPane = null;
		if (morphoFrame != null) {
			resultPane = morphoFrame.getDataViewContainerPanel();
		}

		AbstractDataPackage adp = null;
		if (resultPane != null) {
			adp = resultPane.getAbstractDataPackage();
		}

		if (adp == null) {
			Log.debug(16, " Abstract Data Package is null in "
					+ AnnotationPlugin.class.getName());
			return annotation;
		}

		// make sure resultPanel is not null
		if (resultPane != null) {
			DataViewer dataView = resultPane.getCurrentDataViewer();
			if (dataView != null) {

				int entityIndex = dataView.getEntityIndex();
				
				// package and entity
				String packageId = adp.getAccessionNumber();
				String dataTable = String.valueOf(entityIndex);
				
				// look up the annotation if it exists, or make new one
				List<Annotation> annotations = 
					SMS.getInstance().getAnnotationManager().getAnnotations(packageId, dataTable);
				Log.debug(30, "Annotations for doc: " + annotations.size());
				if (annotations.size() > 0) {
					annotation = annotations.get(0);
				} else {
					// create a new one
					annotation = new Annotation();
					annotation.setEMLPackage(packageId);
					annotation.setDataTable(dataTable);
				}
				
			}
		}
		return annotation;
	}
	

	public void handleStateChange(StateChangeEvent event) {

		// initialize data-table centric UI elements
		if (event.getChangedState().equals(StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME)) {
			if (isInitialized()) {
				// listen for the closed window event
				initializeWindowAdapter();
			}
		}
		
		// just build the annotation table and pop up - no window listener
		if (event.getChangedState().equals(StateChangeEvent.SELECT_DATA_VIEWER)) {
			if (!isInitialized()) {
				// make the pop up for the data table
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
		if (event instanceof DeleteEvent) {
			DeleteEvent deleteEvent = (DeleteEvent) event;
			handleDeleteEvent(deleteEvent);
		}

	}
	
	private void handleSaveEvent(SaveEvent saveEvent) {
		// get the old and new ids for the package tha was saved
		String oldId = saveEvent.getInitialId();
		String newId = saveEvent.getFinalId();
		String location = saveEvent.getLocation();
		boolean duplicate = saveEvent.isDuplicate();
		boolean syncronize = saveEvent.isSynchronize();
		boolean success = true;
		
		// for synch, look up the annotation from the original source
		if (syncronize) {
			// going from METACAT->LOCAL
			if (location.equals(AbstractDataPackage.LOCAL)) {
				// load the remote annotations for that package
				initializeAnnotations(oldId, AbstractDataPackage.METACAT);
			}
			if (location.equals(AbstractDataPackage.METACAT)) {
				// load the local annotations for that package
				initializeAnnotations(oldId, AbstractDataPackage.LOCAL);
			}
		}
		
		// get all the annotations for the original docid
		List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getAnnotations(oldId, null);
		for (Annotation annotation: annotations) {
			if (duplicate) {
				// prompt?
				boolean saveDuplicate = false;
				int response = 
					JOptionPane.showConfirmDialog(
							null, 
							"Would you like to copy the existing Annotation information to the new package?\n" +
							"id = " + annotation.getURI(),
							"Duplicate Annotation?", JOptionPane.YES_NO_OPTION);
				saveDuplicate = (response == JOptionPane.YES_OPTION);
				if (!saveDuplicate) {
					continue;
				}
				
				try {
					// copy the annotation
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					annotation.write(baos);
					Annotation annotationCopy = Annotation.read(new ByteArrayInputStream(baos.toByteArray()));
					// make an id for the annotation
					AccessionNumber an = new AccessionNumber(Morpho.thisStaticInstance);
					String annotationId = an.getNextId();
					// set the data package association and the id
					annotationCopy.setEMLPackage(newId);
					annotationCopy.setURI(annotationId);
					// save the copy
					saveAnnotation(annotationCopy);
					// serialize the copy
					success = serializeAnnotation(newId, location);
				} catch (AnnotationException e) {
					Log.debug(5, "Error saving Annotation duplicate");
					e.printStackTrace();
				}
			}
			else {
				// set the updated packageId
				annotation.setEMLPackage(newId);
				// save the annotation
				saveAnnotation(annotation);
				// serialize to disk
				success = serializeAnnotation(newId, location);
			}
		}
	}
	
	private void handleDeleteEvent(DeleteEvent deleteEvent) {
	
		// get the information from the event
		String docid = deleteEvent.getId();
		String location = deleteEvent.getLocation();
		
		// delete the associated annotations
		deleteAnnotations(docid, location);
	}
	
	/**
	 * the AnnotationTabPane listens for GUI events and saves/shows the annotation as needed
	 * When the frame is closed, this listner should stop listening (a new listener is made if 
	 * the frame is re-opened)
	 * @param dataViewContainerPanel reference to the morpho data viewer
	 * @return true if the listeners were found/removed
	 */
	private void removeStateChangeListeners(DataViewContainerPanel dataViewContainerPanel) {

		if (dataViewContainerPanel != null) {
			DataViewer dataViewer = dataViewContainerPanel.getCurrentDataViewer();
			if (dataViewer != null) {
				Component[] existingComponents = dataViewer.getHeaderPanel().getComponents();
				for (Component comp: existingComponents) {
					if (comp instanceof AnnotationTabPane) {
						
						// get a reference to the tab pane to remove references from
						AnnotationTabPane tabPane = (AnnotationTabPane) comp;
						// tabPane listens for column selection
						StateChangeMonitor.getInstance().removeStateChangeListener(StateChangeEvent.SELECT_DATATABLE_COLUMN, tabPane);
						// tab pane listens for annotation change
						StateChangeMonitor.getInstance().removeStateChangeListener(ANNOTATION_CHANGE_EVENT, tabPane);
						
						// remove listeners that are the tab contents
						for (int i = 0; i < tabPane.getTabCount(); i++) {
							Component tabContents = tabPane.getComponent(i);
							if (tabContents instanceof StateChangeListener) {
								StateChangeMonitor.getInstance().removeStateChangeListener((StateChangeListener)tabContents);
							}
						}
					}
				}
			}
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
						if (comp instanceof AnnotationTabPane) {
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
					dataView.addPopupMenuItem(UIController.getGUIActionCloneUsedByMorphoFrame(annotateAction, morphoFrame), true);
					// separator
					dataView.addPopupMenuItem(UIController.getGUIActionCloneUsedByMorphoFrame(removeObservationAction, morphoFrame), false);
					dataView.addPopupMenuItem(UIController.getGUIActionCloneUsedByMorphoFrame(removeMeasurementAction, morphoFrame), false);
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
					String location = adp.getLocation();
					
					// load annotations from the correct location
					if (!location.equals("")) {
						initializeAnnotations(packageId, location);
					}
					
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
					Insets tabInsets = UIManager.getInsets("TabbedPane.tabAreaInsets");
					Dimension fillerDim = new Dimension(AnnotationTablePanel.rowHeaderDim);
					fillerDim.width += tabInsets.left;
					filler.setPreferredSize(fillerDim);
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

					// the TAB pane
					AnnotationTabPane tabPane = new AnnotationTabPane(annotation, JTabbedPane.TOP);

					// add the column view
					AnnotationPage madLib = new AnnotationPage(false);
					tabPane.addTab(AnnotationTabPane.TAB_NAMES.get(0), madLib);
										
					// add the context tab
					ContextPage contextTab = new ContextPage(annotation);
					tabPane.addTab(AnnotationTabPane.TAB_NAMES.get(1), contextTab);
					
					// add the full table view to the tab pane
					tabPane.addTab(AnnotationTabPane.TAB_NAMES.get(2), annotationTablePanel);
					
					// add the graph
					Component graphComponent = AnnotationGraph.createAnnotationGraph(annotation, true);
					tabPane.addTab(AnnotationTabPane.TAB_NAMES.get(3), graphComponent);
					
					// tabPane listens for column selection
					StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.SELECT_DATATABLE_COLUMN, tabPane);
					// tab pane listens for annotation change
					StateChangeMonitor.getInstance().addStateChangeListener(ANNOTATION_CHANGE_EVENT, tabPane);
					
					// add the tab pane to the panel
					dataViewer.getHeaderPanel().add(BorderLayout.CENTER, tabPane);
					
					// intercept column selection using the tab pane
					AnnotationListSelectionModel annotationSelectionListener = new AnnotationListSelectionModel(tabPane);
					dataViewer.getDataTable().getColumnModel().setSelectionModel(annotationSelectionListener);
					
					Log.debug(30, "Set up annotation table...\n " 
							+ "Data package: " + packageId 
							+ ", entity: " + entityIndex 
							+ ", annotation id: " + annotation.getURI()
							);

				}
			}
		}
		
	}
	
	class AnnotationListSelectionModel extends DefaultListSelectionModel {
		private AnnotationTabPane pane;
		
		public AnnotationListSelectionModel(AnnotationTabPane p) {
			this.pane = p;
		}
		
		 public void setSelectionInterval(int index0, int index1) {
			 if (canContinue()) {
				 super.setSelectionInterval(index0, index1);
			 }
		 }
		 
		 public void addSelectionInterval(int index0, int index1) {
			 if (canContinue()) {
				 super.addSelectionInterval(index0, index1);
			 }
		 }
		 
		 private boolean canContinue() {
			 if (pane.hasChanged()) {
				int response = JOptionPane.showConfirmDialog(
						pane, 
						"Apply changes before continuing?", 
						"Unsaved changes", 
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
