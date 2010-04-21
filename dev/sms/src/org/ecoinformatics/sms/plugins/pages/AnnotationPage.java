/**
 *  '$RCSfile: UsageRights.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Ben Leinfelder
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.18 $'
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

import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
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
	
	private JLabel attributeLabel;

	// observation
	private JTextField observationLabel;
	private JLabel observationLabelLabel;
	private JCheckBox observationIsDistinct;
	
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
		setFieldsEnabled(false);
	}

	private void setFieldsEnabled(boolean enabled) {		
		this.simpleAnnotationPanel.setEnabled(enabled);
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
		JButton helpButton = Help.createHelpButton(pageRef, "What's this?");
		
		// actually show the help
		if (showAll) {
			this.add(descPanel);
		}
		
		// Attribute Label
		JPanel attributeLabelPanel = WidgetFactory.makePanel(1);
		attributeLabelPanel.add(WidgetFactory.makeLabel("Attribute:", true));
		attributeLabel = WidgetFactory.makeLabel("?", true);
		attributeLabelPanel.add(attributeLabel);
		attributeLabelPanel.add(helpButton);
		this.add(attributeLabelPanel);
		
		//add the main panel here
		simpleAnnotationPanel = new SimpleAnnotationPanel(true, showAll);
		this.add(simpleAnnotationPanel);
		
		// Measurement Label
		JPanel measurementPanel = WidgetFactory.makePanel(2);
		measurementPanel.setLayout(new GridLayout(1,2));
		
		JPanel measurementLabelPanel = WidgetFactory.makePanel(2);
		measurementLabelLabel = WidgetFactory.makeLabel("Measurement:", false);
		measurementLabelPanel.add(measurementLabelLabel);
		measurementLabel = WidgetFactory.makeOneLineShortTextField("");
		measurementLabel.setEnabled(false);
		measurementLabelPanel.add(measurementLabel);
		measurementLabelPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		// measurement key
		measurementIsKey = WidgetFactory.makeCheckBox("Is Key?", false);
		measurementLabelPanel.add(measurementIsKey);
		measurementLabelPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		
		measurementPanel.add(measurementLabelPanel);
		measurementPanel.add(WidgetFactory.makeHTMLLabel(Help.MEASUREMENT_ISKEY_HELP, 2));
		if (showAll) {
			this.add(measurementPanel);
			this.add(WidgetFactory.makeDefaultSpacer());
		}
		
		// Observation Label
		JPanel observationPanel = WidgetFactory.makePanel(2);
		observationPanel.setLayout(new GridLayout(1,2));
		
		JPanel labelPanel = WidgetFactory.makePanel(2);
		observationLabelLabel = WidgetFactory.makeLabel("Observation:", false);
		labelPanel.add(observationLabelLabel);
		observationLabel = WidgetFactory.makeOneLineShortTextField("");
		observationLabel.setEnabled(false);
		labelPanel.add(observationLabel);
		labelPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		// Observation distinct
		observationIsDistinct = WidgetFactory.makeCheckBox("Is Distinct?", false);
		labelPanel.add(observationIsDistinct);
		labelPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		
		observationPanel.add(labelPanel);
		observationPanel.add(WidgetFactory.makeHTMLLabel(Help.OBSERVATION_ISKEY_HELP, 2));
		if (showAll) {
			this.add(observationPanel);
			this.add(WidgetFactory.makeDefaultSpacer());
		}
	
	}

	public void editAttribute(String attributeName) {
		this.currentAttributeName = attributeName;
		this.setFieldsEnabled(currentAttributeName != null);
		
		try {
			// what are we editing:
			attributeLabel.setText(currentAttributeName);

			// is there a measurement mapping for the attribute?
			currentMapping = annotation.getMapping(currentAttributeName);
			if (currentMapping == null) {
				return;
			}
			
			// get the current measurement from the mapping
			currentMeasurement = currentMapping.getMeasurement();
			
			// is there an observation that uses that measurement?
			currentObservation = annotation.getObservation(currentMeasurement);

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
		String label = observationLabel.getText();
		if (label != null && label.length() > 0) {
			currentObservation.setLabel(label);
		}
		
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
		
	}

	public String getCurrentAttributeName() {
		return currentAttributeName;
	}

}
