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

package org.ecoinformatics.sms.plugins;

import javax.swing.BoxLayout;
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

	public AnnotationPage() {
		nextPageID = DataPackageWizardInterface.GEOGRAPHIC;
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
						"<b>Select Observation Entity, Characteristic and Standard for the selected attribute.</b> "
								+ "The Ontology Browser can be used to navigate specific ontologies.",
						2);
		this.add(desc);
		this.add(WidgetFactory.makeDefaultSpacer());
		
		// Attribute Label
		JPanel attributeLabelPanel = WidgetFactory.makePanel(1);
		attributeLabelPanel.add(WidgetFactory.makeLabel("Attribute:", false));
		attributeLabel = WidgetFactory.makeLabel("?", false);
		attributeLabelPanel.add(attributeLabel);
		attributeLabelPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(attributeLabelPanel);
		this.add(WidgetFactory.makeDefaultSpacer());
		
		// Measurement Label
		JPanel measurementLabelPanel = WidgetFactory.makePanel(1);
		measurementLabelLabel = WidgetFactory.makeLabel("Measurement Label:", true);
		measurementLabelPanel.add(measurementLabelLabel);
		measurementLabel = WidgetFactory.makeOneLineTextField("<label>");
		measurementLabelPanel.add(measurementLabel);
		measurementLabelPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		// measurement key
		measurementIsKey = WidgetFactory.makeCheckBox("Is Key?", false);
		measurementLabelPanel.add(measurementIsKey);
		measurementLabelPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(measurementLabelPanel);
		this.add(WidgetFactory.makeDefaultSpacer());
		
		//this.add(WidgetFactory.makeDefaultSpacer());

		//add the main panel here
		simpleAnnotationPanel = new SimpleAnnotationPanel();
		this.add(simpleAnnotationPanel);

		this.add(WidgetFactory.makeDefaultSpacer());
		this.add(WidgetFactory.makeDefaultSpacer());
		
		// Observation Label
		JPanel labelPanel = WidgetFactory.makePanel(1);
		observationLabelLabel = WidgetFactory.makeLabel("Observation Label:", true);
		labelPanel.add(observationLabelLabel);
		observationLabel = WidgetFactory.makeOneLineTextField("<label>");
		labelPanel.add(observationLabel);
		labelPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		// Observation distinct
		observationIsDistinct = WidgetFactory.makeCheckBox("Is Distinct?", false);
		labelPanel.add(observationIsDistinct);
		labelPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(labelPanel);
		this.add(WidgetFactory.makeDefaultSpacer());
		

	}

	public void setAnnotation(Annotation a, String attributeName) {
		this.annotation = a;
		
		try {
			// what are we editing:
			attributeLabel.setText(attributeName);

			// is there a measurement mapping for the attribute?
			currentMapping = annotation.getMapping(attributeName);
			currentMeasurement = currentMapping.getMeasurement();
			currentCharacteristic = currentMeasurement.getCharacteristics()
					.get(0);
			currentStandard = currentMeasurement.getStandard();
			currentProtocol = currentMeasurement.getProtocol();

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
				String entity = currentObservation.getEntity().getURI();
				this.simpleAnnotationPanel.setObservationEntity(entity);
			} catch (Exception e) {
			}
			try {
				String charString = currentCharacteristic.getURI();
				this.simpleAnnotationPanel.setObservationCharacteristic(charString);
			} catch (Exception e) {
			}
			try {
				String standard = currentStandard.getURI();
				this.simpleAnnotationPanel.setObservationStandard(standard);
			} catch (Exception e) {
			}
			try {
				String protocol = currentProtocol.getURI();
				this.simpleAnnotationPanel.setObservationProtocol(protocol);
			} catch (Exception e) {
			}
		} catch (Exception e) {
			// we don't care about this right now
		}

	}

	public Annotation getAnnotation(String attributeName) {

		// create a measurement if there wasn't one already
		if (currentMeasurement == null) {
			currentMeasurement = new Measurement();
			currentMeasurement.setLabel("measurement_"
					+ System.currentTimeMillis());
		}
		
		// edit the existing values
		try {
			if (currentCharacteristic == null) {
				currentCharacteristic = new Characteristic(
						simpleAnnotationPanel.getObservationCharacteristic());
			} else {
				currentCharacteristic.setURI(simpleAnnotationPanel.getObservationCharacteristic());
			}
			annotation.addOntology(currentCharacteristic.getOntology());
		} catch (Exception e) {
			currentCharacteristic = null;
			Log.debug(30, "Ignoring: " + e.getMessage());
			//e.printStackTrace();
		}
		currentMeasurement.getCharacteristics().clear();
		currentMeasurement.addCharacteristic(currentCharacteristic);
		try {
			if (currentStandard == null) {
				currentStandard = new Standard(simpleAnnotationPanel.getObservationStandard());
			} else {
				currentStandard.setURI(simpleAnnotationPanel.getObservationStandard());
			}
			annotation.addOntology(currentStandard.getOntology());
		} catch (Exception e) {
			currentStandard = null;
			Log.debug(30, "Ignoring: " + e.getMessage());
			//e.printStackTrace();
		}
		currentMeasurement.setStandard(currentStandard);
		try {
			if (currentProtocol == null) {
				currentProtocol = new Protocol(simpleAnnotationPanel.getObservationProtocol());
			} else {
				currentProtocol.setURI(simpleAnnotationPanel.getObservationProtocol());
			}
			annotation.addOntology(currentProtocol.getOntology());
		} catch (Exception e) {
			currentProtocol = null;
			Log.debug(30, "Ignoring: " + e.getMessage());
			//e.printStackTrace();
		}
		currentMeasurement.setProtocol(currentProtocol);

		// measurement label
		String mLabel = measurementLabel.getText();
		if (mLabel != null) {
			currentMeasurement.setLabel(mLabel);
		}
		
		boolean key = measurementIsKey.isSelected();
		currentMeasurement.setKey(key);

		// a measurement mapping for this attribute
		if (currentMapping == null) {
			currentMapping = new Mapping();
			annotation.addMapping(currentMapping);
		}
		currentMapping.setAttribute(attributeName);
		currentMapping.setMeasurement(currentMeasurement);

		// make an entity from the form value
		Entity entity = null;
		try {
			entity = new Entity(simpleAnnotationPanel.getObservationEntity());
			annotation.addOntology(entity.getOntology());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// look for existing Observations of this given entity
//		 List<Observation> existingObservations = annotation.getObservations(entity);
//		 if (!existingObservations.isEmpty()) {
//			 int useExisting =
//				 JOptionPane.showConfirmDialog(
//						 UIController.getInstance().getCurrentActiveWindow(),
//						 "Use existing observation? Entity: " + entity,
//						 "Existing Observation", 
//						 JOptionPane.YES_NO_OPTION);
//			 if (useExisting == JOptionPane.YES_OPTION) {
//				 currentObservation = existingObservations.get(0);
//				 currentObservation.addMeasurement(currentMeasurement);
//			 }
//		 }

		// the observation
		if (currentObservation == null) {
			currentObservation = new Observation();
			currentObservation.setLabel("observation_"
					+ System.currentTimeMillis());
			currentObservation.addMeasurement(currentMeasurement);
			annotation.addObservation(currentObservation);
		}
		
		// observation label
		String label = observationLabel.getText();
		if (label != null) {
			currentObservation.setLabel(label);
		}
		
		boolean distinct = observationIsDistinct.isSelected();
		currentObservation.setDistinct(distinct);
		
		// set the new values for existing classes
		currentObservation.setEntity(entity);

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
}
