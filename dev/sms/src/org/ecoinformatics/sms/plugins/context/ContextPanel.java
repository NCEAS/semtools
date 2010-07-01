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

package org.ecoinformatics.sms.plugins.context;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.ui.OntologyClassField;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

public class ContextPanel extends JPanel {


	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *
	
	public static final Dimension LIST_BUTTON_DIMS = new Dimension(55, 30);
	public static final  Dimension PICKLIST_DIMS = new Dimension(250,30);
		
	// context options
	private JComboBox observationList;
	private JCheckBox observationIsIdentifying;
	private OntologyClassField contextRelationship;
	
	// providing observation
	//private JLabel observationLabel;

	private Context currentContext;
	private Observation currentObservation;
	private OntologyClass relationship;
	private List<Observation> existingObservations;
	
	private JButton removeButton;

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
		JPanel contextPanel = WidgetFactory.makePanel();
		//contextPanel.add(WidgetFactory.makeLabel("...the ", false, null));
		
		// entity
		//observationLabel = WidgetFactory.makeLabel("", true, null);
		//observationLabel.setEnabled(false);
		//contextPanel.add(observationLabel);
		
		//contextPanel.add(WidgetFactory.makeLabel(" was ", false, null));

		// relationship
		contextRelationship = OntologyClassField.makeLabel("<relationship>", false, null);
		contextRelationship.setFilterClass(Annotation.OBOE_CLASSES.get(Context.class)); // odd because this is not actually an OntologyClass subclass, but the mapping holds
		WidgetFactory.setPrefMaxSizes(contextRelationship, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		contextPanel.add(OntologyClassField.wrapField(contextRelationship, "Context Characteristic"));
		
		contextPanel.add(WidgetFactory.makeLabel(" the ", false, null));
		
		// other entity
		observationList = WidgetFactory.makePickList(null, false, 0, null);
		WidgetFactory.setPrefMaxSizes(observationList, PICKLIST_DIMS);
		contextPanel.add(observationList);
		
		
		contextPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		
		// Relationship is identifying
		observationIsIdentifying = WidgetFactory.makeCheckBox("Identifying?", false);
		
		ActionListener removeListener = new PanelActionListener(PanelActionListener.REMOVE);
		removeButton = WidgetFactory.makeJButton("-", removeListener, ContextPanel.LIST_BUTTON_DIMS);
		removeButton.setToolTipText("Remove Context");
		
		// Relationship panel
		JPanel relationshipPanel = WidgetFactory.makePanel();
		relationshipPanel.add(observationIsIdentifying);
		relationshipPanel.add(removeButton);
		//relationshipPanel.add(Box.createHorizontalGlue());
		
		contextPanel.add(relationshipPanel);
		contextPanel.add(Box.createHorizontalGlue());
		this.add(contextPanel);
		//this.add(relationshipPanel);
		
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
		
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		contextRelationship.setEnabled(enabled);
		observationIsIdentifying.setEnabled(enabled);
		observationList.setEnabled(enabled);
		removeButton.setEnabled(enabled);
	}

	public Observation getSelectedObservation() {
		return (Observation) observationList.getSelectedItem();
	}

	public OntologyClass getRelationship() {
		try {
			String uri = contextRelationship.getOntologyClass().getURI();
			relationship = new OntologyClass(uri);
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
		//harvest the current values
		currentContext.setObservation(getSelectedObservation());
		currentContext.setIdentifying(getIsIdentifying());
		currentContext.setRelationship(getRelationship());
		return this.currentContext;
	}
	
	public void setObservation(Observation contextObservation) {
		this.currentObservation = contextObservation;
		//this.observationLabel.setText(currentObservation.toString());
		//this.observationLabel.setToolTipText(currentObservation.toString());

	}
}
class PanelActionListener implements ActionListener {
	
	private int mode;
	
	static final int REMOVE = 1;

	public PanelActionListener(int mode) {
		this.mode = mode;
	}
	
	public void actionPerformed(ActionEvent e) {
		switch (this.mode) {
		case REMOVE:
			doRemove(e);
			break;
		// do add handles them all
		default:
			doRemove(e);
			break;
		}
	}
	
	
	public void doRemove(ActionEvent e) {
		JButton source = (JButton) e.getSource();
		Container parent = source.getParent();
		ContextPanelList cpl = null;
		ContextPanel cp = null;

		while (parent != null) {
			// get the actual panel we want to remove
			if (parent instanceof ContextPanel) {
				cp = (ContextPanel) parent;
			}
			// get the list to remove it from
			if (parent instanceof ContextPanelList) {
				cpl = (ContextPanelList) parent;
				break;
			}
			parent = parent.getParent();
		}
		// get the list's observation, and remove ourselves from the contexts
		Observation parentObservation = cpl.getObservation();
		Context context = cp.getContext();
		parentObservation.getContexts().remove(context);
		cpl.setObservation(parentObservation);
	}
}

