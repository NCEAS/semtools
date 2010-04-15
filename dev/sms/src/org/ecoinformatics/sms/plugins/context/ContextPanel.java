/**
 *  '$RCSfile: UsageRights.java,v $'
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

package org.ecoinformatics.sms.plugins.context;

import java.awt.Dimension;
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

public class ContextPanel extends JPanel {


	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *
	
	public static final Dimension LIST_BUTTON_DIMS = new Dimension(55, 30);
		
	// context options
	private JComboBox observationList;
	private JCheckBox observationIsIdentifying;
	private OntologyClassField contextRelationship;
	
	// providing observation
	private JLabel observationLabel;

	private Context currentContext;
	private Observation currentObservation;
	private Relationship relationship;
	private List<Observation> existingObservations;

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	public ContextPanel(boolean madLib) {
		if (madLib) {
			initMadlib();
		}
	}

	private void initMadlib() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				
		// Observation
		JPanel contextPanel = WidgetFactory.makePanel(2);
		contextPanel.add(WidgetFactory.makeLabel("...the ", false, null));
		
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
		
		// Relationship
		JPanel relationshipPanel = WidgetFactory.makePanel(1);
		
		// Relationship is identifying
		observationIsIdentifying = WidgetFactory.makeCheckBox("Relationship is Identifying?", false);
		relationshipPanel.add(observationIsIdentifying);
		relationshipPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		
		this.add(relationshipPanel);
		
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
