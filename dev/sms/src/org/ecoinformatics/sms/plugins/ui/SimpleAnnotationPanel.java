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
package org.ecoinformatics.sms.plugins.ui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.ontology.OntologyObjectProperty;
import org.ecoinformatics.sms.ontology.OntologyProperty;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.utilities.Log;

public class SimpleAnnotationPanel extends JPanel {
	
	private OntologyClassField observationEntity;
	private OntologyClassField observationCharacteristic;
	private OntologyClassField observationStandard;
	private OntologyClassField observationProtocol;
	
	// for Measurement "template"
	private OntologyClassField observationMeasurement;

	public static String MEASUREMENT_HELP = "The <b>Measurement</b> defines rules for what Characteristics of which Entities can be taken using certain Standards and Protocols.";

	public static String ENTITY_HELP = "The <b>Entity</b> is the 'thing' being observed. If the diameter of a tree is measured, the Entity will be the tree.";
	public static String CHARACTERISTIC_HELP = "The <b>Characteristic</b> is the property being measured. If the diameter of a tree is measured, the Characteristic will be the diameter (length).";
	public static String STANDARD_HELP = "The <b>Standard</b> is the unit used for the measurement. If the diameter of a tree is measured, the Standard will be a length unit (meters).";
	public static String PROTOCOL_HELP = "The <b>Protocol</b> is the method used for taking the measurement. If the diameter of a tree is measured at breast height, this will be the Protocol.";

	public SimpleAnnotationPanel(boolean madLib, boolean showHelp) {
		super();
		if (madLib) {
			initMadLib(showHelp);
		} else {
			init();
		}

	}
	
	private void init() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Entity
		JPanel entityPanel = WidgetFactory.makePanel(2);
		entityPanel.setLayout(new GridLayout(1,2));

		JPanel entityLabelPanel = WidgetFactory.makePanel(2);
		entityLabelPanel.add(WidgetFactory.makeLabel("Entity:", false));
		observationEntity = OntologyClassField.makeLabel("", false, null);
		observationEntity.setFilterClass(Annotation.OBOE_CLASSES.get(Entity.class));
		entityLabelPanel.add(observationEntity);
		
		entityPanel.add(entityLabelPanel);
		entityPanel.add(WidgetFactory.makeHTMLLabel(ENTITY_HELP, 2));
		entityPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));

		// Characteristic
		JPanel characteristicPanel = WidgetFactory.makePanel(2);
		characteristicPanel.setLayout(new GridLayout(1,2));

		JPanel characteristicLabelPanel = WidgetFactory.makePanel(2);
		characteristicLabelPanel.add(WidgetFactory.makeLabel("Characteristic:",
				false));
		observationCharacteristic = OntologyClassField.makeLabel("", false, null);
		observationCharacteristic.setFilterClass(Annotation.OBOE_CLASSES.get(Characteristic.class));
		characteristicLabelPanel.add(observationCharacteristic);
		
		characteristicPanel.add(characteristicLabelPanel);
		characteristicPanel.add(WidgetFactory.makeHTMLLabel(CHARACTERISTIC_HELP, 2));
		characteristicPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
				0, 8 * WizardSettings.PADDING));

		// Standard
		JPanel standardPanel = WidgetFactory.makePanel(2);
		standardPanel.setLayout(new GridLayout(1,2));
		
		JPanel standardLabelPanel = WidgetFactory.makePanel(2);
		standardLabelPanel.add(WidgetFactory.makeLabel("Standard:", false));
		observationStandard = OntologyClassField.makeLabel("", false, null);
		observationStandard.setFilterClass(Annotation.OBOE_CLASSES.get(Standard.class));

		standardLabelPanel.add(observationStandard);
		standardPanel.add(standardLabelPanel);
		standardPanel.add(WidgetFactory.makeHTMLLabel(STANDARD_HELP, 2));

		standardPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		
		// Protocol
		JPanel protocolPanel = WidgetFactory.makePanel(2);
		protocolPanel.setLayout(new GridLayout(1,2));
		
		JPanel protocolLabelPanel = WidgetFactory.makePanel(2);
		protocolLabelPanel.add(WidgetFactory.makeLabel("Protocol:", false));
		observationProtocol = OntologyClassField.makeLabel("", false, null);
		observationProtocol.setFilterClass(Annotation.OBOE_CLASSES.get(Protocol.class));

		protocolLabelPanel.add(observationProtocol);
		protocolPanel.add(protocolLabelPanel);
		protocolPanel.add(WidgetFactory.makeHTMLLabel(PROTOCOL_HELP, 2));

		protocolPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));

		
		// Measurement template
		JPanel measurementPanel = WidgetFactory.makePanel(3);
		measurementPanel.setLayout(new GridLayout(1,2));
		
		JPanel measurementLabelPanel = WidgetFactory.makePanel(3);
		measurementLabelPanel.add(WidgetFactory.makeLabel("Measurement:", false));
		observationMeasurement = OntologyClassField.makeLabel("", true, null);
		observationMeasurement.setFilterClass(Annotation.OBOE_CLASSES.get(Measurement.class));
		measurementLabelPanel.add(observationMeasurement);
		
		measurementPanel.add(measurementLabelPanel);
		measurementPanel.add(WidgetFactory.makeHTMLLabel(MEASUREMENT_HELP, 2));
		measurementPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
				0, 8 * WizardSettings.PADDING));
		WidgetFactory.addTitledBorder(measurementPanel, "Measurement Type");
		
		// listen for the measurement to be set
		observationMeasurement.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						handleMeasurementSelected();
					}
				});
		
		// the annotation classes
		JPanel classesPanel = WidgetFactory.makePanel();
		classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
		
		classesPanel.add(entityPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		classesPanel.add(characteristicPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		classesPanel.add(standardPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		classesPanel.add(protocolPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		WidgetFactory.addTitledBorder(classesPanel, "Annotations");
		
		// put them all together
		JPanel annotationPanel = WidgetFactory.makePanel();
		annotationPanel.setLayout(new BoxLayout(annotationPanel, BoxLayout.Y_AXIS));
		annotationPanel.add(measurementPanel);
		annotationPanel.add(WidgetFactory.makeDefaultSpacer());
		annotationPanel.add(classesPanel);
		
		this.add(annotationPanel);

	}
	
	private void initMadLib(boolean showHelp) {
				
		// Measurement template
		JPanel measurementPanel = WidgetFactory.makePanel(2);
		measurementPanel.add(WidgetFactory.makeLabel("The ", false, null));
		observationMeasurement = OntologyClassField.makeLabel("", true, null);
		observationMeasurement.setFilterClass(Annotation.OBOE_CLASSES.get(Measurement.class));
		measurementPanel.add(observationMeasurement);
		measurementPanel.add(WidgetFactory.makeLabel(" was recorded.", false, null));
		measurementPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
		
		// listen for the measurement to be set
		observationMeasurement.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						handleMeasurementSelected();
					}
				});
		
		// Characteristic and Entity
		JPanel characteristicPanel = WidgetFactory.makePanel(1);
		
		characteristicPanel.add(WidgetFactory.makeLabel("The ", false, null));
		
		observationCharacteristic = OntologyClassField.makeLabel("", true, null);
		observationCharacteristic.setFilterClass(Annotation.OBOE_CLASSES.get(Characteristic.class));
		characteristicPanel.add(observationCharacteristic);
		
		characteristicPanel.add(WidgetFactory.makeLabel(" of the ", false, null));
		
		observationEntity = OntologyClassField.makeLabel("", true, null);
		observationEntity.setFilterClass(Annotation.OBOE_CLASSES.get(Entity.class));
		characteristicPanel.add(observationEntity);
		characteristicPanel.add(WidgetFactory.makeLabel(" was recorded ", false, null));
		
		characteristicPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
				0, 8 * WizardSettings.PADDING));
		
	
		// Standard and Protocol
		JPanel standardPanel = WidgetFactory.makePanel(1);
				
		standardPanel.add(WidgetFactory.makeLabel(" using the ", false, null));

		observationStandard = OntologyClassField.makeLabel("", true, null);
		observationStandard.setFilterClass(Annotation.OBOE_CLASSES.get(Standard.class));

		standardPanel.add(observationStandard);
		standardPanel.add(WidgetFactory.makeLabel(" standard, ", false, null));

		standardPanel.add(WidgetFactory.makeLabel("and the ", false, null));

		observationProtocol = OntologyClassField.makeLabel("", true, null);
		observationProtocol.setFilterClass(Annotation.OBOE_CLASSES.get(Protocol.class));
		standardPanel.add(observationProtocol);

		standardPanel.add(WidgetFactory.makeLabel(" protocol.", false, null));

		standardPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		

		// the help panel
		JPanel helpPanel = WidgetFactory.makePanel();
		helpPanel.setLayout(new GridLayout(5,1));
		helpPanel.add(WidgetFactory.makeHTMLLabel(MEASUREMENT_HELP, 1));
		helpPanel.add(WidgetFactory.makeHTMLLabel(ENTITY_HELP, 1));
		helpPanel.add(WidgetFactory.makeHTMLLabel(CHARACTERISTIC_HELP, 1));
		helpPanel.add(WidgetFactory.makeHTMLLabel(STANDARD_HELP, 1));
		helpPanel.add(WidgetFactory.makeHTMLLabel(PROTOCOL_HELP, 1));
		helpPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		

		// put them together
		JPanel classesPanel = WidgetFactory.makePanel();
		classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		classesPanel.add(characteristicPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		classesPanel.add(standardPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		if (showHelp) {
			classesPanel.add(helpPanel);
			classesPanel.add(WidgetFactory.makeDefaultSpacer());
		}
		//classesPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(measurementPanel);
		this.add(classesPanel);
	}
	
	private void handleMeasurementSelected() {
		Log.debug(30, "observationMeasurement set");
		OntologyClass measurement = getObservationMeasurement();
		
		// reset the fields
//		setObservationCharacteristic(null);
//		setObservationEntity(null);
//		setObservationProtocol(null);
//		setObservationStandard(null);
		
		// assume the filter class has the OBOE ontology
		Ontology oboeOntology = observationMeasurement.getFilterClass().getOntology();
		OntologyProperty property = null;
		List<OntologyClass> classes = null;
		
		// get the entity
		try {
			property = new OntologyObjectProperty(oboeOntology, "measurementFor");
			classes = SMS.getInstance().getOntologyManager().getNamedClassesForPropertyRestriction(property, measurement);
			observationEntity.setOntologyClass(classes.get(0));
		} catch (Exception ex) {
			Log.debug(30, "ignoring measurement entity template exception");
			//ex.printStackTrace();
		}
		// get the characteristic
		try {
			property = new OntologyObjectProperty(oboeOntology, "ofCharacteristic");
			classes = SMS.getInstance().getOntologyManager().getNamedClassesForPropertyRestriction(property, measurement);
			observationCharacteristic.setOntologyClass(classes.get(0));
		} catch (Exception ex) {
			Log.debug(30, "ignoring measurement characteristic template exception");
			//ex.printStackTrace();
		}
		// get the standard
		try {
			property = new OntologyObjectProperty(oboeOntology, "usesStandard");
			classes = SMS.getInstance().getOntologyManager().getNamedClassesForPropertyRestriction(property, measurement);
			observationStandard.setOntologyClass(classes.get(0));
		} catch (Exception ex) {
			Log.debug(30, "ignoring measurement standard template exception");
			//ex.printStackTrace();
		}
		// get the protocol
		try {
			property = new OntologyObjectProperty(oboeOntology, "usesProtocol");
			classes = SMS.getInstance().getOntologyManager().getNamedClassesForPropertyRestriction(property, measurement);
			observationProtocol.setOntologyClass(classes.get(0));
		} catch (Exception ex) {
			Log.debug(30, "ignoring measurement protocol template exception");
			//ex.printStackTrace();
		}
		
		// disable other fields if measurement is selected
//		if (measurement != null) {
//			setEnabled(false);
//			observationMeasurement.setEnabled(true);
//		} else {
//			setEnabled(true);
//		}
		
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		observationMeasurement.setEnabled(enabled);
		observationEntity.setEnabled(enabled);
		observationCharacteristic.setEnabled(enabled);
		observationStandard.setEnabled(enabled);
		observationProtocol.setEnabled(enabled);
	}
	
	public void reset() {
		setObservationMeasurement(null);
		setObservationCharacteristic(null);
		setObservationEntity(null);
		setObservationProtocol(null);
		setObservationStandard(null);
	}
	
	public OntologyClass getObservationEntity() {
		return observationEntity.getOntologyClass();
	}

	public void setObservationEntity(OntologyClass observationEntity) {
		this.observationEntity.setOntologyClass(observationEntity);
	}

	public OntologyClass getObservationCharacteristic() {
		return observationCharacteristic.getOntologyClass();
	}

	public void setObservationCharacteristic(OntologyClass observationCharacteristic) {
		this.observationCharacteristic.setOntologyClass(observationCharacteristic);
	}

	public OntologyClass getObservationStandard() {
		return observationStandard.getOntologyClass();
	}

	public void setObservationStandard(OntologyClass observationStandard) {
		this.observationStandard.setOntologyClass(observationStandard);
	}

	public OntologyClass getObservationProtocol() {
		return observationProtocol.getOntologyClass();
	}

	public void setObservationProtocol(OntologyClass observationProtocol) {
		this.observationProtocol.setOntologyClass(observationProtocol);
	}
	
	public OntologyClass getObservationMeasurement() {
		return observationMeasurement.getOntologyClass();
	}

	public void setObservationMeasurement(OntologyClass observationMeasurement) {
		this.observationMeasurement.setOntologyClass(observationMeasurement);
	}

}
