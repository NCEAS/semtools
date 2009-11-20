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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;

import org.ecoinformatics.sms.SMS;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.DataPackagePlugin;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.DataViewer;
import edu.ucsb.nceas.morpho.datapackage.SavePackageCommand;
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
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.UISettings;

public class AnnotationPlugin
	implements PluginInterface, ServiceProvider, StateChangeListener {
		
	private MorphoFrame morphoFrame = null;
	
	private GUIAction annotateAction = null;
	

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

		annotateAction =
			new GUIAction(
				"Annotate current column...",
				null,
				new AnnotationCommand());
		annotateAction.setToolTipText(
			"Add/edit annotation or this data table attribute");
		// I figure a position of 100 will always place this at the bottom of the Data Menu.
		annotateAction.setSeparatorPosition(Morpho.SEPARATOR_PRECEDING);
		annotateAction.setMenuItemPosition(100);
		annotateAction.setMenu(DataPackagePlugin.DATA_MENU_LABEL, DataPackagePlugin.DATAMENUPOSITION);
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

		// Save dialog box action
	    GUIAction saveAction = new GUIAction("Save Annotations...",
	                                              null,
	                                              new SaveAnnotationCommand());
	    saveAction.setMenuItemPosition(101);
	    saveAction.setToolTipText("Save Annotations...");
		saveAction.setMenu(DataPackagePlugin.DATA_MENU_LABEL, DataPackagePlugin.DATAMENUPOSITION);
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
	    
		// add the custom actions
		UIController controller = UIController.getInstance();
		controller.addGuiAction(annotateAction);
		controller.addGuiAction(saveAction);

		
		//register as a listener for data frame opening
		StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME, this);
	
		// initialize the annotations
		initializeAnnotations();
	}
	
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

	public void handleStateChange(StateChangeEvent event) {

		// alter the popup menu
		if (event.getChangedState().equals(
				StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME)) {
			morphoFrame = UIController.getInstance().getCurrentActiveWindow();

			if (morphoFrame != null) {
				DataViewContainerPanel resultPane = morphoFrame
						.getDataViewContainerPanel();

				if (resultPane != null) {
					DataViewer dataView = resultPane.getCurrentDataViewer();
					if (dataView != null) {
						annotateAction.setEnabled(true);
						dataView.addPopupMenuItem(annotateAction);
					}
				}
			}

		}

	}

}
