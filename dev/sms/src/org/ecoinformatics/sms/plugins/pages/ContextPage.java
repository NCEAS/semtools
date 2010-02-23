/**
 *  '$RCSfile: UsageRights.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Ben Leinfelder
 *    Release: @release@
 *
 *   '$Author$'
 *     '$Date$'
 * '$Revision$'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ecoinformatics.sms.plugins.pages;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Relationship;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.commands.ContextCommand;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.utilities.OrderedMap;

public class ContextPage extends AbstractUIPage implements StateChangeListener {

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	private final String pageID = null;
	private final String pageNumber = "0";
	private final String title = "Context Editor";
	private final String subtitle = "";

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *
		
	// annotation
	private Annotation annotation;
	private Observation observation;
	
	// context options
	private CustomList contextList;
	private JLabel contextListLabel;
	

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	public ContextPage() {
		init();
	}

	/**
	 * initialize method does frame-specific design - i.e. adding the widgets
	 * that are displayed only in this frame (doesn't include prev/next buttons
	 * etc)
	 */
	private void init() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel desc = WidgetFactory
				.makeHTMLLabel(
						"<b>Edit Context Relationships</b> "
								+ "Add, Remove, Edit Observation Contexts",
						2);
		this.add(desc);
				
		// Context list
		JPanel contextListPanel = WidgetFactory.makePanel(5);
		contextListLabel = WidgetFactory.makeLabel("Context:", false);
		contextListPanel.add(contextListLabel);
		String[] colNames = new String[] {"Observation", "Relationship", "Observation"};
		Object[] editors = null; // no direct editing
		contextList = WidgetFactory.makeList(
				colNames, 
				editors, 
				3, //displayRows, 
				true, //showAddButton, 
				true, //showEditButton, 
				false, //showDuplicateButton, 
				true, //showDeleteButton, 
				false, //showMoveUpButton, 
				false //showMoveDownButton
				);
		
		// add the data later
		
		// add the custom add action
		GUIAction addAction = new GUIAction("Add Context", null, new ContextCommand(true));
		contextList.setCustomAddAction(addAction);
		
		// remove action
		contextList.setCustomDeleteAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				doRemove();
			}
		});

		
		contextListPanel.add(contextList);
		contextListPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(contextListPanel);

		this.add(WidgetFactory.makeDefaultSpacer());

	}
	
	private void doRemove() {
		List rowList = contextList.getSelectedRowList();
		Context selectedContext = (Context) rowList.get(3);
		// get the existing context
		List<Context> existingContexts = observation.getContexts();
		for (Context context: existingContexts) {
			if (selectedContext.equals(context)) {
				observation.removeContext(context);
				AnnotationPlugin.saveAnnotation(annotation);
				// fire change event
				StateChangeEvent annotationEvent = new StateChangeEvent(this, AnnotationPlugin.ANNOTATION_CHANGE_EVENT);
				StateChangeMonitor.getInstance().notifyStateChange(annotationEvent);
				// done
				break;
			}
		}

		
	}
	
	public void setObservation(Observation observation) {
		this.observation = observation;
	}
	
	private void populateList() {
		contextList.removeAllRows();
		if (this.observation == null) {
			return;
		}
		//set the existing entries
		List<Context> existingContexts = observation.getContexts();
		for (Context context: existingContexts) {
			Relationship relationship = context.getRelationship();
			Observation target = context.getObservation();
			
			List<Object> rowList = new ArrayList<Object>();
			rowList.add(observation);
			rowList.add(relationship);
			rowList.add(target);
			rowList.add(context);
			contextList.addRow(rowList );			
		}
	}
	
	public void handleStateChange(StateChangeEvent event) {
		if (event.getChangedState().equals(StateChangeEvent.SELECT_DATATABLE_COLUMN)) {
			handleSelectColumn();
		}
		else if (event.getChangedState().equals(AnnotationPlugin.ANNOTATION_CHANGE_EVENT)) {
			this.populateList();
		}
		
	}
	
	private void handleSelectColumn() {
		observation = null;
		annotation = AnnotationPlugin.getCurrentActiveAnnotation();
		String attributeName = AnnotationPlugin.getCurrentSelectedAttribute();
		if (attributeName != null && annotation != null) {
			Mapping mapping = annotation.getMapping(attributeName);
			if (mapping != null) {
				Measurement measurement = mapping.getMeasurement();
				observation = annotation.getObservation(measurement);
			}
		}
		this.setObservation(observation);
		this.populateList();
	}

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	/**
	 * The action to be executed when the page is displayed. May be empty
	 */
	public void onLoadAction() {
	}

	/**
	 * The action to be executed when the "Prev" button is pressed. May be empty
	 * 
	 */
	public void onRewindAction() {

	}

	/**
	 * The action to be executed when the "Next" button (pages 1 to
	 * last-but-one) or "Finish" button(last page) is pressed. May be empty, but
	 * if so, must return true
	 * 
	 * @return boolean true if wizard should advance, false if not (e.g. if a
	 *         required field hasn't been filled in)
	 */
	public boolean onAdvanceAction() {
		return true;
	}

	/**
	 * gets the OrderedMap object that contains all the key/value paired
	 * settings for this particular wizard page
	 * 
	 * @return data the OrderedMap object that contains all the key/value paired
	 *         settings for this particular wizard page
	 */
	private OrderedMap returnMap = new OrderedMap();

	public OrderedMap getPageData() {
		return getPageData(null);
	}

	/**
	 * gets the Map object that contains all the key/value paired settings for
	 * this particular wizard page
	 * 
	 * @param rootXPath
	 *            the root xpath to prepend to all the xpaths returned by this
	 *            method
	 * @return data the Map object that contains all the key/value paired
	 *         settings for this particular wizard page
	 */
	public OrderedMap getPageData(String rootXPath) {
		for (Object rowObj: contextList.getListOfRowLists()) {
			List<String> row = (List<String>) rowObj;
			String uri = row.get(0);
			String url = row.get(1);
			returnMap.put(uri, url);
		}
		return returnMap;
	}

	/**
	 * gets the unique ID for this wizard page
	 * 
	 * @return the unique ID String for this wizard page
	 */
	public String getPageID() {
		return pageID;
	}

	/**
	 * gets the title for this wizard page
	 * 
	 * @return the String title for this wizard page
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * gets the subtitle for this wizard page
	 * 
	 * @return the String subtitle for this wizard page
	 */
	public String getSubtitle() {
		return subtitle;
	}

	/**
	 * Returns the ID of the page that the user will see next, after the "Next"
	 * button is pressed. If this is the last page, return value must be null
	 * 
	 * @return the String ID of the page that the user will see next, or null if
	 *         this is te last page
	 */
	public String getNextPageID() {
		return nextPageID;
	}

	/**
	 * Returns the serial number of the page
	 * 
	 * @return the serial number of the page
	 */
	public String getPageNumber() {
		return pageNumber;
	}

	public boolean setPageData(OrderedMap data, String _xPathRoot) {
		return true;
	}

}
