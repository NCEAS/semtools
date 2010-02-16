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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.ecoinformatics.sms.SMS;
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

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.DataViewer;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.HelpDialog;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.utilities.OrderedMap;

public class AnnotationPage extends AbstractUIPage implements StateChangeListener {

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
	
	private String MEASUREMENT_HELP_TEXT = "The Measurement 'Is Key' when it identifies the uniqueness of an Observation instance. Multiple measurements may be combined as a compound key.";
	private String OBSERVATION_HELP_TEXT = "The Observation 'Is Distinct' when the observation Entity identifies the uniqueness of the observation Instance.";

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

		JPanel descPanel = WidgetFactory.makePanel(5);
		descPanel.setLayout(new GridLayout(2,1));
		JLabel desc = WidgetFactory
				.makeHTMLLabel(
						"<b>Select the Observation Entity, Characteristic, Standard and Protocol for the selected attribute.</b> "
						+ "<br>The Ontology Browser can be used to locate classes in specific ontologies."
						+ "<br>Clicking the spyglass will launch the browser for that annotation class."
						, 3);
		descPanel.add(desc);
		
		// help button
	    Command command = new Command() {
	      private JDialog helpDialog = null;
	      public void execute(ActionEvent ae) {

	        if(helpDialog == null) {
	        	String title = "Help with Semantic Annotations";
	        	String helpText = 
	        		"<html> <body>"
	        		+ "<p><b>Measurement (Is Key)</b></p>"
	        		+ "<p>The measurement 'iskey' attribute in the annotation syntax is a constraint, much like (primary) key constraints in relational databases." +
	        		"<br>A measurement 'isKey = yes' constraint states that a particular measurement (characteristic-value pair) determines the identity of the corresponding entity instance." +
	        		"<br>For example, if we have a plot entity with a label characteristic measurement, and state that this measurement is a key, then each occurrence of the label value in a row of the dataset relates to the same entity instance (see example below)." +
	        		"<br>For instance, each site label '1' would denote a unique site instance, each site label '2' would denote a different site instance, and so on." +
	        		"<br>If multiple measurements in an observation have an 'isKey = yes' constraint, then each measurement serves as a partial key, i.e., the identity of the entity instance is determined by a combination of the corresponding characteristic-value pairs (and not by just one of the pairs).</p>" +
	        		"<p><b>Observation (Is Distinct)</b></p>" +
	        		"<p>The observation 'isDistinct=yes' constraint is somewhat similar, but promoted to the level of the observation." +
	        		"<br>It states that if two of the observation instances are of the same entity instance, then the these observation instances are the same instance (i.e., there is only one 'distinct' or 'unique' observation).</p>"
	        		+ "</body> </html>";
	          helpDialog = new HelpDialog(title, helpText);
	        }
	        Point loc = getLocationOnScreen();
	        int wd = getWidth();
	        int ht = getHeight();
	        int dwd = HelpDialog.HELP_DIALOG_SIZE.width;
	        int dht = HelpDialog.HELP_DIALOG_SIZE.height;
	        helpDialog.setLocation( (int)loc.getX() + wd/2 - dwd/2, (int)loc.getY() + ht/2 - dht/2);
	        helpDialog.setSize(HelpDialog.HELP_DIALOG_SIZE);
	        helpDialog.setVisible(true);
	        helpDialog.toFront();

	      }
	    };
	    
	    GUIAction helpAction = new GUIAction("More information about Semantic Annotation", null, command);
		JButton helpButton = new HyperlinkButton(helpAction);
		
		//this.add(WidgetFactory.makePanel(1).add(helpButton));
		descPanel.add(helpButton);
		this.add(descPanel);
		
		// Attribute Label
		JPanel attributeLabelPanel = WidgetFactory.makePanel(1);
		attributeLabelPanel.add(WidgetFactory.makeLabel("Attribute:", false));
		attributeLabel = WidgetFactory.makeLabel("?", false);
		attributeLabelPanel.add(attributeLabel);
		attributeLabelPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(attributeLabelPanel);
		this.add(WidgetFactory.makeDefaultSpacer());				
		
		//add the main panel here
		simpleAnnotationPanel = new SimpleAnnotationPanel(true);
		this.add(simpleAnnotationPanel);

		//this.add(WidgetFactory.makeDefaultSpacer());
		
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
		measurementPanel.add(WidgetFactory.makeHTMLLabel(MEASUREMENT_HELP_TEXT , 2));
		this.add(measurementPanel);
		this.add(WidgetFactory.makeDefaultSpacer());
		
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
		observationPanel.add(WidgetFactory.makeHTMLLabel(OBSERVATION_HELP_TEXT , 2));

		this.add(observationPanel);
		this.add(WidgetFactory.makeDefaultSpacer());
		
	
	}

	public void setAnnotation(Annotation a, String attributeName) {
		this.annotation = a;
		this.currentAttributeName = attributeName;
		
		try {
			// what are we editing:
			attributeLabel.setText(attributeName);

			// is there a measurement mapping for the attribute?
			currentMapping = annotation.getMapping(attributeName);
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
				String entity = currentObservation.getEntity().getURI();
				this.simpleAnnotationPanel.setObservationEntity(currentObservation.getEntity());
			} catch (Exception e) {
			}
			try {
				currentCharacteristic = currentMeasurement.getCharacteristics().get(0);
				String charString = currentCharacteristic.getURI();
				this.simpleAnnotationPanel.setObservationCharacteristic(currentCharacteristic);
			} catch (Exception e) {
			}
			try {
				currentStandard = currentMeasurement.getStandard();
				String standard = currentStandard.getURI();
				this.simpleAnnotationPanel.setObservationStandard(currentStandard);
			} catch (Exception e) {
			}
			try {
				currentProtocol = currentMeasurement.getProtocol();
				String protocol = currentProtocol.getURI();
				this.simpleAnnotationPanel.setObservationProtocol(currentProtocol);
			} catch (Exception e) {
			}
		} catch (Exception e) {
			e.printStackTrace();
			// we don't care about this right now
		}

	}

	public Annotation getAnnotation(String attributeName) {

		// create a measurement if there wasn't one already
		if (currentMeasurement == null) {
			currentMeasurement = new Measurement();
			currentMeasurement.setLabel(
					Annotation.getNextMeasurementLabel(annotation, "m"));
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
			Log.debug(30, "Ignoring: " + e.getMessage());
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
			Log.debug(30, "Ignoring: " + e.getMessage());
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
			Log.debug(30, "Ignoring: " + e.getMessage());
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
			entity = new Entity(simpleAnnotationPanel.getObservationEntity().getURI());
			annotation.addOntology(entity.getOntology());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.debug(30, "Ignoring: " + e.getMessage());
			//e.printStackTrace();
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
			currentObservation.setLabel(
					Annotation.getNextObservationLabel(annotation, "o"));
			currentObservation.addMeasurement(currentMeasurement);
			annotation.addObservation(currentObservation);
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

	public void handleStateChange(StateChangeEvent event) {
		if (event.getChangedState().equals(StateChangeEvent.SELECT_DATATABLE_COLUMN)) {
			handleSelectColumn();
		}
		
	}
	
	private void handleSelectColumn() {
		
		//save what we have before moving forward?
		if (currentAttributeName != null) {
			annotation = this.getAnnotation(currentAttributeName);
			
			// save
			AnnotationPlugin.saveAnnotation(annotation);

		}
		
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
					+ this.getClass().getName());
			return;
		}

		// make sure resultPanel is not null
		if (resultPane != null) {
			DataViewer dataView = resultPane.getCurrentDataViewer();
			if (dataView != null) {

				JTable table = dataView.getDataTable();
				int viewIndex = table.getSelectedColumn();
		    	int attributeIndex = table.getColumnModel().getColumn(viewIndex).getModelIndex();
				int entityIndex = dataView.getEntityIndex();
				String entityName = adp.getEntityName(entityIndex);
				String attributeName = adp.getAttributeName(entityIndex, attributeIndex);
				
				// package and entity
				String packageId = adp.getAccessionNumber();
				String dataTable = String.valueOf(entityIndex);
				
				// look up the annotation if it exists, or make new one
				List<Annotation> annotations = 
					SMS.getInstance().getAnnotationManager().getAnnotations(packageId, dataTable);

				Annotation annotation;
				if (annotations.size() > 0) {
					annotation = annotations.get(0);
				} else {
					// create a new one
					annotation = new Annotation();
					annotation.setEMLPackage(packageId);
					annotation.setDataTable(dataTable);
				}
				
				Log.debug(30, "Annotating...\n " 
						+ "Data package: " + packageId 
						+ ", entity: " + entityName 
						+ ", attribute: " + attributeName
						+ ", annotation id: " + annotation.getURI()
						);
				
				this.setAnnotation(annotation, attributeName);
								
			}

		}
	}
}
