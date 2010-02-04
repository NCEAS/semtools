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
import org.ecoinformatics.sms.plugins.OntologyClassSelectionPage;

import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;

public class SimpleAnnotationPanel extends JPanel {
	
	private OntologyClassJLabel observationEntity;
	private OntologyClassJLabel observationCharacteristic;
	private OntologyClassJLabel observationStandard;
	private OntologyClassJLabel observationProtocol;
	
	public SimpleAnnotationPanel() {
		super();
		init();
	}
	
	private void init() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		MouseListener mListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				OntologyClassJLabel source = (OntologyClassJLabel) e.getSource();
				showDialog(source);
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
		entityPanel.add(WidgetFactory.makeHTMLLabel("The Entity is the 'thing' being observed. If the diameter of a tree is measured, the Entity will be the tree.", 2));
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
		characteristicPanel.add(WidgetFactory.makeHTMLLabel("The Characteristic is the property being measured. If the diameter of a tree is measured, the Characteristic will be the diameter (length).", 2));
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
		standardPanel.add(WidgetFactory.makeHTMLLabel("The Standard is the unit used for the measurement. If the diameter of a tree is measured, the Standard will be a length unit (meters).", 2));

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
		protocolPanel.add(WidgetFactory.makeHTMLLabel("The Protocol is the method used for taking the measurement. If the diameter of a tree is measured at breast height, this will be the Protocol.", 2));

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
	
	public static void showDialog(OntologyClassJLabel source) {
		OntologyClassSelectionPage page = new OntologyClassSelectionPage();
		
		try {
			OntologyClass currentClass = source.getOntologyClass();
			OntologyClass filterClass = source.getFilterClass();
			if (currentClass != null) {
				page.setCurrentClass(currentClass);
			}
			page.setFilterClass(filterClass);
		} catch (Exception e) {
			//ignore
		}
		
		// show the dialog
		ModalDialog dialog = 
			new ModalDialog(
					page, 
					UIController.getInstance().getCurrentActiveWindow(), 
					UISettings.POPUPDIALOG_WIDTH,
					UISettings.POPUPDIALOG_HEIGHT);

		// get the response back
		if (dialog.USER_RESPONSE == ModalDialog.OK_OPTION) {
			String selectedClassString = null;
			if (page.getSelectedTerms() !=null && page.getSelectedTerms().size() > 0) {
				selectedClassString = page.getSelectedTerms().get(0);
			}
			OntologyClass selectedClass = null;
			try {
				selectedClass = new OntologyClass(selectedClassString);
			} catch (Exception e) {
				selectedClass = null;
				Log.debug(20, "error constructing selectedClass from string: " + selectedClassString);
			}
			source.setOntologyClass(selectedClass);
		}
		page = null;
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
