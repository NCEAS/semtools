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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.OntologyClassSelectionPage;

import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.UISettings;

public class SimpleAnnotationPanel extends JPanel {
	
	private JTextField observationEntity;
	private JTextField observationCharacteristic;
	private JTextField observationStandard;
	private JTextField observationProtocol;
	
	public SimpleAnnotationPanel() {
		super();
		init();
	}
	
	private void init() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		MouseListener mListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				JTextField source = (JTextField) e.getSource();
				showDialog(source);
			}

		};
		
		JPanel classesPanel = WidgetFactory.makePanel();
		classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));

		// Entity
		JPanel entityPanel = WidgetFactory.makePanel(1);
		entityPanel.add(WidgetFactory.makeLabel("Entity:", false));
		observationEntity = WidgetFactory.makeOneLineTextField("<entity>");
		observationEntity.addMouseListener(mListener);
		entityPanel.add(observationEntity);
		entityPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));

		// Characteristic
		JPanel characteristicPanel = WidgetFactory.makePanel(1);
		characteristicPanel.add(WidgetFactory.makeLabel("Characteristic:",
				false));
		observationCharacteristic = WidgetFactory
				.makeOneLineTextField("<characteristic>");
		observationCharacteristic.addMouseListener(mListener);

		characteristicPanel.add(observationCharacteristic);
		characteristicPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
				0, 8 * WizardSettings.PADDING));

		// Standard
		JPanel standardPanel = WidgetFactory.makePanel(1);
		standardPanel.add(WidgetFactory.makeLabel("Standard:", false));
		observationStandard = WidgetFactory.makeOneLineTextField("<standard>");
		observationStandard.addMouseListener(mListener);

		standardPanel.add(observationStandard);
		standardPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		
		// Protocol
		JPanel protocolPanel = WidgetFactory.makePanel(1);
		protocolPanel.add(WidgetFactory.makeLabel("Protocol:", false));
		observationProtocol = WidgetFactory.makeOneLineTextField("<protocol>");
		observationProtocol.addMouseListener(mListener);

		protocolPanel.add(observationProtocol);
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
	
	private void showDialog(JTextField source) {
		OntologyClassSelectionPage page = new OntologyClassSelectionPage();
		
		try {
			OntologyClass currentClass = new OntologyClass(source.getText());
			if (currentClass != null) {
				page.setCurrentClass(currentClass);
			}
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
			String selectedClass = null;
			if (page.getSelectedTerms() !=null && page.getSelectedTerms().size() > 0) {
				selectedClass = page.getSelectedTerms().get(0);
			}
			source.setText(selectedClass);
		}
		page = null;
	}

	public String getObservationEntity() {
		return observationEntity.getText();
	}

	public void setObservationEntity(String observationEntity) {
		this.observationEntity.setText(observationEntity);
	}

	public String getObservationCharacteristic() {
		return observationCharacteristic.getText();
	}

	public void setObservationCharacteristic(String observationCharacteristic) {
		this.observationCharacteristic.setText(observationCharacteristic);
	}

	public String getObservationStandard() {
		return observationStandard.getText();
	}

	public void setObservationStandard(String observationStandard) {
		this.observationStandard.setText(observationStandard);
	}

	public String getObservationProtocol() {
		return observationProtocol.getText();
	}

	public void setObservationProtocol(String observationProtocol) {
		this.observationProtocol.setText(observationProtocol);
	}

}
