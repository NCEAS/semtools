/**
 *  '$Id$'
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

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.context.ContextPanelList;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

public class ContextPage extends AbstractUIPage {

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
	private ContextPanelList contextList;
	
	// edit toggle
	JToggleButton editButton;
	

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	public ContextPage(Annotation a) {
		this.annotation = a;

		init();
		setEnabled(false);
	}

	/**
	 * initialize method does frame-specific design - i.e. adding the widgets
	 * that are displayed only in this frame (doesn't include prev/next buttons
	 * etc)
	 */
	private void init() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		final ContextPage pageRef = this;

		// edit button
		AbstractAction toggleAction = new AbstractAction("Edit") {
			public void actionPerformed(ActionEvent e) {
				if (editButton.isSelected()) {
					// active only if we have an observation
					pageRef.setEnabled(observation != null);
					editButton.setText("Save");
				}
				else {
					pageRef.setEnabled(false);
					// save annotation
					annotation = pageRef.getAnnotation();
					if (annotation != null) {
						Log.debug(40, "Persisting Annotation from Context page: " + annotation.getURI() );
						// save
						AnnotationPlugin.saveAnnotation(annotation);
					}
					editButton.setText("Edit");
				}
			}
		};
		editButton = new JToggleButton(toggleAction);
		//editButton = WidgetFactory.makeCheckBox("Edit", false);
		editButton.addActionListener(toggleAction);
		
		// help button
		JButton helpButton = Help.createHelpButton(pageRef, "What's this?");
		
		JPanel lineOnePanel = WidgetFactory.makePanel(); 
		lineOnePanel.add(WidgetFactory.makeLabel("The Observation was made where the...", false, null));
		lineOnePanel.add(Box.createHorizontalGlue());
		lineOnePanel.add(editButton);
		
		JPanel topPanel = WidgetFactory.makePanel(1);
		topPanel.add(helpButton);
			
		this.add(lineOnePanel);
		this.add(topPanel);
				
		// Context list
		JPanel contextListPanel = WidgetFactory.makePanel(6);
		
		contextList = new ContextPanelList(annotation);
		contextListPanel.add(contextList);
				
		this.add(contextListPanel);

	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		contextList.setEnabled(enabled);
		editButton.setSelected(enabled);
		if (enabled) {
			editButton.setText("Save");
		} else {
			editButton.setText("Edit");
		}
	}
	
	public void setObservation(Observation observation) {
		this.observation = observation;
	}
	
	public Annotation getAnnotation() {
		//get the latest version of the contexts
		contextList.getObservation();
		return annotation;
	}
	
	private void populateList() {
		if (this.observation == null) {
			return;
		}
		// set the existing entries
		contextList.setObservation(observation);	
	}
	
	public void handleSelectColumn() {
		observation = null;
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
		// disable editing initially until edit toggle is depressed
		//this.setEnabled(observation != null);
		this.setEnabled(false);
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
