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

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

public class SimpleAnnotationPanel extends JPanel {
	
	private OntologyClassJLabel observationEntity;
	private OntologyClassJLabel observationCharacteristic;
	private OntologyClassJLabel observationStandard;
	private OntologyClassJLabel observationProtocol;
	
	public static String ENTITY_HELP = "The <b>Entity</b> is the 'thing' being observed. If the diameter of a tree is measured, the Entity will be the tree.";
	public static String CHARACTERISTIC_HELP = "The <b>Characteristic</b> is the property being measured. If the diameter of a tree is measured, the Characteristic will be the diameter (length).";
	public static String STANDARD_HELP = "The <b>Standard</b> is the unit used for the measurement. If the diameter of a tree is measured, the Standard will be a length unit (meters).";
	public static String PROTOCOL_HELP = "The <b>Protocol</b> is the method used for taking the measurement. If the diameter of a tree is measured at breast height, this will be the Protocol.";
	
	public SimpleAnnotationPanel(boolean madLib) {
		super();
		if (madLib) {
			initMadLib();
		} else {
			init();
		}

	}
	
	private void init() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		MouseListener mListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				OntologyClassJLabel source = (OntologyClassJLabel) e.getSource();
				OntologyClassJLabel.showDialog(source);
			}

		};
		
		JPanel classesPanel = WidgetFactory.makePanel();
		classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));

		// Entity
		JPanel entityPanel = WidgetFactory.makePanel(2);
		entityPanel.setLayout(new GridLayout(1,2));

		JPanel entityLabelPanel = WidgetFactory.makePanel(2);
		entityLabelPanel.add(WidgetFactory.makeLabel("Entity:", false));
		observationEntity = OntologyClassJLabel.makeLabel("<entity>", false, null);
		observationEntity.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Entity.class));
		observationEntity.addMouseListener(mListener);
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
		observationCharacteristic = OntologyClassJLabel.makeLabel("<characteristic>", false, null);
		observationCharacteristic.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Characteristic.class));
		observationCharacteristic.addMouseListener(mListener);
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
		observationStandard = OntologyClassJLabel.makeLabel("<standard>", false, null);
		observationStandard.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Standard.class));
		observationStandard.addMouseListener(mListener);

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
		observationProtocol = OntologyClassJLabel.makeLabel("<protocol>", false, null);
		observationProtocol.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Protocol.class));
		observationProtocol.addMouseListener(mListener);

		protocolLabelPanel.add(observationProtocol);
		protocolPanel.add(protocolLabelPanel);
		protocolPanel.add(WidgetFactory.makeHTMLLabel(PROTOCOL_HELP, 2));

		protocolPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));

		// put them together
		classesPanel.add(entityPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		classesPanel.add(characteristicPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		classesPanel.add(standardPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		classesPanel.add(protocolPanel);

		this.add(classesPanel);
	}
	
	private void initMadLib() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		MouseListener mListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				OntologyClassJLabel source = (OntologyClassJLabel) e.getSource();
				OntologyClassJLabel.showDialog(source);
			}

		};
		
		JPanel classesPanel = WidgetFactory.makePanel();
		classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
		
		// Characteristic and Entity
		JPanel characteristicPanel = WidgetFactory.makePanel(1);
		
		characteristicPanel.add(WidgetFactory.makeLabel("The ", false, null));
		
		observationCharacteristic = OntologyClassJLabel.makeLabel("<characteristic>", true, null);
		observationCharacteristic.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Characteristic.class));
		observationCharacteristic.addMouseListener(mListener);
		characteristicPanel.add(observationCharacteristic);
		
		characteristicPanel.add(WidgetFactory.makeLabel(" of the ", false, null));
		
		observationEntity = OntologyClassJLabel.makeLabel("<entity>", true, null);
		observationEntity.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Entity.class));
		observationEntity.addMouseListener(mListener);
		characteristicPanel.add(observationEntity);
		characteristicPanel.add(WidgetFactory.makeLabel(" was recorded ", false, null));
		
		characteristicPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
				0, 8 * WizardSettings.PADDING));
		
	
		// Standard and Protocol
		JPanel standardPanel = WidgetFactory.makePanel(1);
				
		standardPanel.add(WidgetFactory.makeLabel(" using the ", false, null));

		observationStandard = OntologyClassJLabel.makeLabel("<standard>", true, null);
		observationStandard.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Standard.class));
		observationStandard.addMouseListener(mListener);

		standardPanel.add(observationStandard);
		standardPanel.add(WidgetFactory.makeLabel(" standard, ", false, null));

		standardPanel.add(WidgetFactory.makeLabel("and the ", false, null));

		observationProtocol = OntologyClassJLabel.makeLabel("<protocol>", true, null);
		observationProtocol.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Protocol.class));
		observationProtocol.addMouseListener(mListener);
		standardPanel.add(observationProtocol);

		standardPanel.add(WidgetFactory.makeLabel(" protocol.", false, null));

		standardPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		

		// the help panel
		JPanel helpPanel = WidgetFactory.makePanel();
		helpPanel.setLayout(new GridLayout(4,1));
		helpPanel.add(WidgetFactory.makeHTMLLabel(ENTITY_HELP, 1));
		helpPanel.add(WidgetFactory.makeHTMLLabel(CHARACTERISTIC_HELP, 1));
		helpPanel.add(WidgetFactory.makeHTMLLabel(STANDARD_HELP, 1));
		helpPanel.add(WidgetFactory.makeHTMLLabel(PROTOCOL_HELP, 1));
		helpPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		

		// put them together
		classesPanel.add(characteristicPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		classesPanel.add(standardPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());
		classesPanel.add(helpPanel);
		classesPanel.add(WidgetFactory.makeDefaultSpacer());

		this.add(classesPanel);
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

}
