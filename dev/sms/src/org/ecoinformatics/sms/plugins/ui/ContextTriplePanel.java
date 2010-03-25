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

package org.ecoinformatics.sms.plugins.ui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Relationship;
import org.ecoinformatics.sms.annotation.Triple;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

public class ContextTriplePanel extends JPanel {

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *
		
	// context options
	private OntologyClassField entityA;
	private OntologyClassField contextRelationship;
	private OntologyClassField entityB;
	
	// the triple
	private Triple contextTriple;


	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	public ContextTriplePanel() {
		initMadlib();
	}

	private void initMadlib() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Context
		JPanel contextPanel = WidgetFactory.makePanel(2);
		contextPanel.add(WidgetFactory.makeLabel("The Observation was made where the ", false, null));
		
		// entity A
		entityA = OntologyClassField.makeLabel("", false, null);
		entityA.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Entity.class));
		contextPanel.add(entityA);
		
		contextPanel.add(WidgetFactory.makeLabel(" was ", false, null));

		// context
		contextRelationship = OntologyClassField.makeLabel("", false, null);
		contextRelationship.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Relationship.class));
		contextPanel.add(contextRelationship);
		
		contextPanel.add(WidgetFactory.makeLabel(" the ", false, null));
		
		// entity B
		entityB = OntologyClassField.makeLabel("", false, null);
		entityB.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Entity.class));
		contextPanel.add(entityB);
		
		contextPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		
		this.add(contextPanel);

		this.add(WidgetFactory.makeDefaultSpacer());

	}
	
	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	private void harvestValues() {
		if (contextTriple == null) {
			contextTriple = new Triple();
		}
		contextTriple.a = entityA.getOntologyClass();
		contextTriple.b = contextRelationship.getOntologyClass();
		contextTriple.c = entityB.getOntologyClass();
	}
	
	public Triple getContextTriple() {
		harvestValues();
		return contextTriple;
	}

	public void setContextTriple(Triple contextTriple) {
		this.contextTriple = contextTriple;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("The Observation was made where the ");
		sb.append(entityA.getText());
		sb.append(" was ");
		sb.append(contextRelationship.getText());
		sb.append(" the ");
		sb.append(entityB.getText());
		sb.append(".");
		return sb.toString();
		
	}

}
