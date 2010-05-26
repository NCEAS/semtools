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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.ui.SimpleAnnotationPanel;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

public class AnnotationPage extends AbstractUIPage {

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	private final String pageID = DataPackageWizardInterface.USAGE_RIGHTS;
	private final String pageNumber = "9";
	private final String title = "Attribute Annotation Editor";
	private final String subtitle = "";

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *
	
	private SimpleAnnotationPanel simpleAnnotationPanel = null;
	
	private String currentAttributeName = null;
	
	private boolean showAll = true;
	
	// edit toggle
	JToggleButton editButton;
	
	private JLabel attributeLabel;

	// observation
	private JTextField observationLabel;
	private JLabel observationLabelLabel;
	private JCheckBox observationIsDistinct;
	private JComboBox existingObservationList;
	
	// measurement
	private JTextField measurementLabel;
	private JLabel measurementLabelLabel;
	private JCheckBox measurementIsKey;

	private Annotation annotation = null;
	private Observation currentObservation;
	private Mapping currentMapping;
	private Measurement currentMeasurement;
	private Characteristic currentCharacteristic;
	private Standard currentStandard;
	private Protocol currentProtocol;
	
	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	public AnnotationPage(boolean showAll) {
		this.showAll = showAll;
		init();
		setEnabled(false);
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		simpleAnnotationPanel.setEnabled(enabled);
		existingObservationList.setEnabled(enabled);
		observationIsDistinct.setEnabled(enabled);
		measurementIsKey.setEnabled(enabled);
		editButton.setSelected(enabled);
		if (enabled) {
			editButton.setText("Apply");
		} else {
			editButton.setText("Edit");
		}

	}
	
	/**
	 * initialize method does frame-specific design - i.e. adding the widgets
	 * that are displayed only in this frame (doesn't include prev/next buttons
	 * etc)
	 */
	private void init() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel descPanel = WidgetFactory.makePanel(3);
		JLabel desc = WidgetFactory
				.makeHTMLLabel(
						"<b>Select the Measurement template for the given attribute</b>, " +
						"<br>or select the Observation Entity, Characteristic, Standard and Protocol for the selected attribute."
						+ "<br>The Ontology Browser can be used to locate classes in specific ontologies."
						+ "<br>Clicking the field will launch the browser for that annotation class."
						, 3);
		descPanel.add(desc);
		
		// help button
		final AnnotationPage pageRef = this;
		JButton helpButton = Help.createHelpButton(pageRef, "Help");
		
		// edit button
		AbstractAction toggleAction = new AbstractAction("Edit") {
			public void actionPerformed(ActionEvent e) {
				if (editButton.isSelected()) {
					// active only if we have an observation
					pageRef.setEnabled(currentAttributeName != null);
					editButton.setText("Apply");
				}
				else {
					pageRef.setEnabled(false);
					// save the current state
					annotation = pageRef.getAnnotation();
					if (annotation != null) {
						Log.debug(40, "Persisting Annotation: " + annotation.getURI() );
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
		
		// actually show the help
		if (showAll) {
			this.add(descPanel);
		}
		
		// Attribute Label
		JPanel attributeLabelPanel = WidgetFactory.makePanel(2);
		attributeLabel = WidgetFactory.makeLabel("<none selected>", true);
		
		attributeLabelPanel.add(WidgetFactory.makeLabel("Selected Attribute:", false, null));
		attributeLabelPanel.add(attributeLabel);
		//attributeLabelPanel.add(WidgetFactory.makeLabel("(select a column to begin)", false, null));
		attributeLabelPanel.add(Box.createHorizontalGlue());
		// Observation distinct
		observationIsDistinct = WidgetFactory.makeCheckBox("Is Distinct?", false);
		attributeLabelPanel.add(observationIsDistinct);
		// measurement key
		measurementIsKey = WidgetFactory.makeCheckBox("Is Key?", false);
		attributeLabelPanel.add(measurementIsKey);
		// help
		attributeLabelPanel.add(helpButton);
		attributeLabelPanel.add(editButton);
		
		this.add(attributeLabelPanel);
		
		//add the main panel here
		simpleAnnotationPanel = new SimpleAnnotationPanel(true, showAll);
		this.add(simpleAnnotationPanel);
		this.add(WidgetFactory.makeDefaultSpacer());
		
		// same as other observation?
		JPanel sameObservationPanel = WidgetFactory.makePanel(2);
		sameObservationPanel.add(WidgetFactory.makeLabel("Measurements were taken on the same sample or individual as:", false, null));
		ItemListener observationListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Observation obs = (Observation) existingObservationList.getSelectedItem();
				if (obs != null) {
					simpleAnnotationPanel.setObservationEntity(obs.getEntity());
					observationIsDistinct.setSelected(obs.isDistinct());
					observationLabel.setText(obs.getLabel());
				}
			}
		};
		existingObservationList = WidgetFactory.makePickList(null, false, 0, observationListener);
		sameObservationPanel.add(existingObservationList);
		sameObservationPanel.setBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray), 
						null, 
						TitledBorder.LEFT, 
						TitledBorder.TOP,
						WizardSettings.WIZARD_CONTENT_FONT,
						WizardSettings.WIZARD_CONTENT_TEXT_COLOR));
		this.add(sameObservationPanel);
		
		// Measurement Label
		JPanel measurementLabelPanel = WidgetFactory.makePanel();
		measurementLabelLabel = WidgetFactory.makeLabel("Measurement:", false);
		measurementLabelPanel.add(measurementLabelLabel);
		measurementLabel = WidgetFactory.makeOneLineTextField("");
		measurementLabel.setEnabled(false);
		measurementLabelPanel.add(measurementLabel);

		if (showAll) {
//			this.add(measurementLabelPanel);
//			this.add(WidgetFactory.makeDefaultSpacer());
		}
		
		// Observation Label
		JPanel labelPanel = WidgetFactory.makePanel();
		observationLabelLabel = WidgetFactory.makeLabel("Observation:", false);
		labelPanel.add(observationLabelLabel);
		observationLabel = WidgetFactory.makeOneLineTextField("");
		observationLabel.setEnabled(false);
		labelPanel.add(observationLabel);
		if (showAll) {
//			this.add(labelPanel);
//			this.add(WidgetFactory.makeDefaultSpacer());
		}
		this.add(Box.createVerticalGlue());
	
	}

	public void editAttribute(String attributeName) {
		this.currentAttributeName = attributeName;
		// initially the panel is not in edit mode
		//this.setEnabled(currentAttributeName != null);
		this.setEnabled(false);
		
		try {
			// what are we editing:
			attributeLabel.setText(currentAttributeName);
			
			try {
				List<Observation> existingObservations = annotation.getObservations();
				existingObservationList.removeAllItems();
				existingObservationList.addItem(null);
				for (Observation o: existingObservations) {
					existingObservationList.addItem(o);
				}
				
			} catch (Exception e) {
			}

			// is there a measurement mapping for the attribute?
			currentMapping = annotation.getMapping(currentAttributeName);
			if (currentMapping == null) {
				return;
			}
			
			// get the current measurement from the mapping
			currentMeasurement = currentMapping.getMeasurement();
			
			// is there an observation that uses that measurement?
			currentObservation = annotation.getObservation(currentMeasurement);
			
			// select it in the existing list
			existingObservationList.setSelectedItem(currentObservation);
			
			// try to set the text field values
			try {
				boolean distinct = currentObservation.isDistinct();
				this.observationIsDistinct.setSelected(distinct);
			} catch (Exception e) {
			}
			try {
				String label = currentObservation.getLabel();
				this.observationLabel.setText(label);
			} catch (Exception e) {
			}
			try {
				boolean key = currentMeasurement.isKey();
				this.measurementIsKey.setSelected(key);
			} catch (Exception e) {
			}
			try {
				String label = currentMeasurement.getLabel();
				this.measurementLabel.setText(label);
			} catch (Exception e) {
			}
			try {
				this.simpleAnnotationPanel.setObservationEntity(currentObservation.getEntity());
			} catch (Exception e) {
			}
			try {
				currentCharacteristic = currentMeasurement.getCharacteristics().get(0);
				this.simpleAnnotationPanel.setObservationCharacteristic(currentCharacteristic);
			} catch (Exception e) {
			}
			try {
				currentStandard = currentMeasurement.getStandard();
				this.simpleAnnotationPanel.setObservationStandard(currentStandard);
			} catch (Exception e) {
			}
			try {
				currentProtocol = currentMeasurement.getProtocol();
				this.simpleAnnotationPanel.setObservationProtocol(currentProtocol);
			} catch (Exception e) {
			}
		} catch (Exception e) {
			e.printStackTrace();
			// we don't care about this right now
		}

	}
	
	public void applyChanges() {
		editButton.doClick();
	}
	
	public void setAnnotation(Annotation a) {
		this.annotation = a;
	}

	public Annotation getAnnotation() {
		
		if (currentAttributeName == null) {
			return annotation;
		}

		// create a measurement if there wasn't one already
		if (currentMeasurement == null) {
			currentMeasurement = new Measurement();
			currentMeasurement.setLabel(
					Annotation.getNextMeasurementLabel(annotation, "m"));
			Log.debug(40, "Adding Measurement: " + currentMeasurement);
		}
		
		// edit the existing values
		try {
			if (currentCharacteristic == null) {
				currentCharacteristic = new Characteristic(
						simpleAnnotationPanel.getObservationCharacteristic().getURI());
			} else {
				currentCharacteristic.setURI(simpleAnnotationPanel.getObservationCharacteristic().getURI());
			}
			annotation.addOntology(currentCharacteristic.getOntology());
		} catch (Exception e) {
			currentCharacteristic = null;
			Log.debug(40, "Ignoring Characteristic: " + e.getMessage());
			//e.printStackTrace();
		}
		currentMeasurement.getCharacteristics().clear();
		currentMeasurement.addCharacteristic(currentCharacteristic);
		try {
			if (currentStandard == null) {
				currentStandard = new Standard(simpleAnnotationPanel.getObservationStandard().getURI());
			} else {
				currentStandard.setURI(simpleAnnotationPanel.getObservationStandard().getURI());
			}
			annotation.addOntology(currentStandard.getOntology());
		} catch (Exception e) {
			currentStandard = null;
			Log.debug(40, "Ignoring Standard: " + e.getMessage());
			//e.printStackTrace();
		}
		currentMeasurement.setStandard(currentStandard);
		try {
			if (currentProtocol == null) {
				currentProtocol = new Protocol(simpleAnnotationPanel.getObservationProtocol().getURI());
			} else {
				currentProtocol.setURI(simpleAnnotationPanel.getObservationProtocol().getURI());
			}
			annotation.addOntology(currentProtocol.getOntology());
		} catch (Exception e) {
			currentProtocol = null;
			Log.debug(40, "Ignoring Protocol: " + e.getMessage());
			//e.printStackTrace();
		}
		currentMeasurement.setProtocol(currentProtocol);

		// measurement label
		String mLabel = measurementLabel.getText();
		if (mLabel != null && mLabel.length() > 0) {
			currentMeasurement.setLabel(mLabel);
		}
		
		boolean key = measurementIsKey.isSelected();
		currentMeasurement.setKey(key);

		// make an entity from the form value
		Entity entity = null;
		try {
			entity = new Entity(simpleAnnotationPanel.getObservationEntity().getURI());
			annotation.addOntology(entity.getOntology());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.debug(40, "Ignoring Entity: " + e.getMessage());
			//e.printStackTrace();
		}
		
		// check that there is anything to set
		if (
				currentCharacteristic == null
				&&
				currentProtocol == null
				&&
				currentStandard == null
				&&
				currentObservation == null
				&&
				entity == null
				
		) {
			// don't save this at all
			return annotation;
		}
		
		// check if it is meant to be part of an existing observation
		Observation selectedObservation = (Observation) existingObservationList.getSelectedItem();
		// if one is selected, we might do something with it.
		if (selectedObservation != null) {
			// use the selected one
			if (currentObservation == null) {
				currentObservation = selectedObservation;
				currentObservation.addMeasurement(currentMeasurement);
			}
			// if they are different, swap them out
			else if (!selectedObservation.equals(currentObservation)) {
				currentObservation.removeMeasurement(currentMeasurement);
				selectedObservation.addMeasurement(currentMeasurement);
				// remove "empty observation"
				if (currentObservation.getMeasurements().size() < 1) {
					annotation.removeObservation(currentObservation, true);
				}
				currentObservation = selectedObservation;
			}
		}
		// nothing selected - could be new or they are splitting off
		if (selectedObservation == null) {
			// if they want to split off the observation make sure that happens
			if (currentObservation != null) {
				currentObservation.removeMeasurement(currentMeasurement);
				// remove "empty observation"
				if (currentObservation.getMeasurements().size() < 1) {
					annotation.removeObservation(currentObservation, true);
				}
				currentObservation = null;
			}
		}
		
		// the observation
		if (currentObservation == null) {
			currentObservation = new Observation();
			currentObservation.setLabel(
					Annotation.getNextObservationLabel(annotation, "o"));
			currentObservation.addMeasurement(currentMeasurement);
			annotation.addObservation(currentObservation);
			Log.debug(40, "Adding Observation: " + currentObservation);
		}
				
		// observation label
//		String label = observationLabel.getText();
//		if (label != null && label.length() > 0) {
//			currentObservation.setLabel(label);
//		}
		
		boolean distinct = observationIsDistinct.isSelected();
		currentObservation.setDistinct(distinct);
		
		// set the new values for existing classes
		currentObservation.setEntity(entity);
		
		// a mapping for this attribute
		if (currentMapping == null) {
			currentMapping = new Mapping();
			annotation.addMapping(currentMapping);
			Log.debug(40, "Adding Mapping: " + currentMapping);
		}
		currentMapping.setAttribute(currentAttributeName);
		currentMapping.setMeasurement(currentMeasurement);

		return annotation;
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

		// check uniqueness of label
		String label = observationLabel.getText();
		if (label != null) {
			Observation obs = annotation.getObservation(label);
			if (obs != null && !obs.equals(currentObservation)) {
				//not unique
				WidgetFactory.hiliteComponent(observationLabelLabel);
				return false;
			}
		}
		WidgetFactory.unhiliteComponent(observationLabelLabel);
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
	
	public void reset() {
			
		currentAttributeName = null;

		currentCharacteristic = null;
		currentMapping = null;
		currentMeasurement = null;
		currentObservation = null;
		currentProtocol = null;
		currentStandard = null;
		
		// attribute
		attributeLabel.setText(null);
		
		// observation
		observationLabel.setText(null);
		observationIsDistinct.setSelected(false);
		
		// measurement
		measurementLabel.setText(null);
		measurementIsKey.setSelected(false);
		
		// classes
		simpleAnnotationPanel.reset();
		
		// existing observations
		existingObservationList.removeAllItems();
		
	}

	public String getCurrentAttributeName() {
		return currentAttributeName;
	}

}
