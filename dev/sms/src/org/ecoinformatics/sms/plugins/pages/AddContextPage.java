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

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Relationship;
import org.ecoinformatics.sms.plugins.ui.OntologyClassField;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.utilities.OrderedMap;

public class AddContextPage extends AbstractUIPage {

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	private final String pageID = null;
	private final String pageNumber = "0";
	private final String title = "Context Annotation Editor";
	private final String subtitle = "";

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *
		
	// context options
	private JComboBox observationList;
	private JLabel observationListLabel;
	private JCheckBox observationIsIdentifying;
	private OntologyClassField contextRelationship;
	
	// providing observation
	private JLabel observationLabel;
	private JLabel observationLabelLabel;

	private Context currentContext;
	private Observation currentObservation;
	private Relationship relationship;
	private List<Observation> existingObservations;

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	public AddContextPage(boolean madLib) {
		if (madLib) {
			initMadlib();
		} else {
			init();
		}
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
						"<b>Select the Observation providing context</b> "
								+ "The Ontology Browser can be used to navigate specific ontologies.",
						2);
		this.add(desc);
		this.add(WidgetFactory.makeDefaultSpacer());
		
		this.add(WidgetFactory.makeDefaultSpacer());
		
		// Observation
		JPanel contextPanel = WidgetFactory.makePanel(1);
		observationLabelLabel = WidgetFactory.makeLabel("Observation:", false);
		contextPanel.add(observationLabelLabel);
		observationLabel = WidgetFactory.makeLabel("", true, null);
		observationLabel.setEnabled(false);
		contextPanel.add(observationLabel);
		contextPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(contextPanel);

		this.add(WidgetFactory.makeDefaultSpacer());
		
		// Relationship
		JPanel relationshipPanel = WidgetFactory.makePanel(1);
		relationshipPanel.add(WidgetFactory.makeLabel("Relationship:",
				false));
		contextRelationship = OntologyClassField.makeLabel("<relationship>", false, null);
		relationshipPanel.add(contextRelationship);
		relationshipPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
				0, 8 * WizardSettings.PADDING));
		// Relationship is identifying
		observationIsIdentifying = WidgetFactory.makeCheckBox("Is Identifying?", false);
		relationshipPanel.add(observationIsIdentifying);
		relationshipPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(relationshipPanel);
		
		this.add(WidgetFactory.makeDefaultSpacer());

		// Observation Context
		JPanel contextTargetPanel = WidgetFactory.makePanel(1);
		observationListLabel = WidgetFactory.makeLabel("Observation:", true);
		contextTargetPanel.add(observationListLabel);
		observationList = WidgetFactory.makePickList(null, false, 0, null);
		contextTargetPanel.add(observationList);
		contextTargetPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(contextTargetPanel);

		this.add(WidgetFactory.makeDefaultSpacer());

	}

	private void initMadlib() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel desc = WidgetFactory
				.makeHTMLLabel(
						"<b>Select the Observation providing context</b> "
								+ "The Ontology Browser can be used to navigate specific ontologies.",
						2);
		this.add(desc);
		this.add(WidgetFactory.makeDefaultSpacer());
				
		// Observation
		JPanel contextPanel = WidgetFactory.makePanel(2);
		observationLabelLabel = WidgetFactory.makeLabel("The Observation was made where the ", false, null);
		contextPanel.add(observationLabelLabel);
		
		// entity
		observationLabel = WidgetFactory.makeLabel("", true, null);
		observationLabel.setEnabled(false);
		contextPanel.add(observationLabel);
		
		contextPanel.add(WidgetFactory.makeLabel(" was ", false, null));

		// context
		contextRelationship = OntologyClassField.makeLabel("<relationship>", false, null);
		contextRelationship.setFilterClass(Annotation.OBOE_CLASSES.get(Relationship.class));
		contextPanel.add(contextRelationship);
		
		contextPanel.add(WidgetFactory.makeLabel(" the ", false, null));
		
		// other entity
		observationList = WidgetFactory.makePickList(null, false, 0, null);
		contextPanel.add(observationList);
		
		contextPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(contextPanel);

		this.add(WidgetFactory.makeDefaultSpacer());
		
		// Relationship
		JPanel relationshipPanel = WidgetFactory.makePanel(1);
		
		// Relationship is identifying
		observationIsIdentifying = WidgetFactory.makeCheckBox("Relationship is Identifying?", false);
		relationshipPanel.add(observationIsIdentifying);
		relationshipPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(relationshipPanel);
		

		this.add(WidgetFactory.makeDefaultSpacer());

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

	public Observation getSelectedObservation() {
		return (Observation) observationList.getSelectedItem();
	}

	public Relationship getRelationship() {
		try {
			String uri = contextRelationship.getOntologyClass().getURI();
			relationship = new Relationship(uri);
		} catch (Exception e) {
			relationship = null;
		}
		return relationship;
	}

	public boolean getIsIdentifying() {
		return observationIsIdentifying.isSelected();
	}

	public void setObservations(List<Observation> observations) {
		this.existingObservations = observations;
		this.observationList.removeAllItems();
		for (Observation o: existingObservations) {
			observationList.addItem(o);
		}
		
	}

	// for editing the context
	public void setContext(Context context) {
		this.currentContext = context;
		if (context != null) {
			this.contextRelationship.setOntologyClass(context.getRelationship());
			this.observationIsIdentifying.setSelected(context.isIdentifying());
			this.observationList.setSelectedItem(context.getObservation());
		}
		
	}
	
	public Context getContext() {
		return this.currentContext;
	}
	
	public void setObservation(Observation contextObservation) {
		this.currentObservation = contextObservation;
		this.observationLabel.setText(currentObservation.toString());
	}
}
