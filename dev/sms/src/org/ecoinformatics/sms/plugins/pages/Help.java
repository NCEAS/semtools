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

package org.ecoinformatics.sms.plugins.pages;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.HelpDialog;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;


public class Help  {
	
	
	public static String MEASUREMENT_ISKEY_HELP = 
		"<p><b>Measurement (Is Key)</b></p>" +
		"<p>The measurement 'iskey' attribute in the annotation syntax is a constraint, much like (primary) key constraints in relational databases." +
		"<br>A measurement 'isKey = yes' constraint states that a particular measurement (characteristic-value pair) determines the identity of the corresponding entity instance." +
		"<br>For example, if we have a plot entity with a label characteristic measurement, and state that this measurement is a key, then each occurrence of the label value in a row of the dataset relates to the same entity instance (see example below)." +
		"<br>For instance, each site label '1' would denote a unique site instance, each site label '2' would denote a different site instance, and so on." +
		"<br>If multiple measurements in an observation have an 'isKey = yes' constraint, then each measurement serves as a partial key, i.e., the identity of the entity instance is determined by a combination of the corresponding characteristic-value pairs (and not by just one of the pairs).</p>";	
	
	public static String OBSERVATION_ISKEY_HELP = 
		"<p><b>Observation (Is Distinct)</b></p>" +
		"<p>The observation 'isDistinct=yes' constraint is somewhat similar, but promoted to the level of the observation." +
		"<br>It states that if two of the observation instances are of the same entity instance, then the these observation instances are the same instance (i.e., there is only one 'distinct' or 'unique' observation).</p>";
	
	public static String MEASUREMENT_HELP = "<p>The <b>Measurement</b> defines rules for what Characteristics of which Entities can be taken using certain Standards and Protocols.</p>";
	
	public static String ENTITY_HELP = "<p>The <b>Entity</b> is the 'thing' being observed. If the diameter of a tree is measured, the Entity will be the tree.</p>";
	
	public static String CHARACTERISTIC_HELP = "<p>The <b>Characteristic</b> is the property being measured. If the diameter of a tree is measured, the Characteristic will be the diameter (length).</p>";
	
	public static String STANDARD_HELP = "<p>The <b>Standard</b> is the unit used for the measurement. If the diameter of a tree is measured, the Standard will be a length unit (meters).</p>";
	
	public static String PROTOCOL_HELP = "<p>The <b>Protocol</b> is the method used for taking the measurement. If the diameter of a tree is measured at breast height, this will be the Protocol.</p>";

	public static String CONTEXT_HELP = "<p><b>Context</b> defines the relationship between two Observation Entities. If a Tree (observation 1) is located in a Plot (observation 2) the Context might be 'Tree isWithin Plot'.</p>";

	
	public static String COLUMN_ANNOTATION_HELP_TEXT = 
		"<html> <body>" 
		+ MEASUREMENT_HELP
		+ ENTITY_HELP
		+ CHARACTERISTIC_HELP
		+ STANDARD_HELP
		+ PROTOCOL_HELP
		+ CONTEXT_HELP
		+ MEASUREMENT_ISKEY_HELP
		+ OBSERVATION_ISKEY_HELP
		+ "</body> </html>";
	
	public static JButton createHelpButton(final Component pageRef, String label) {
		// help button
		Command command = new Command() {
			private JDialog helpDialog = null;

			public void execute(ActionEvent ae) {

				if (helpDialog == null) {
					String title = "Help with Semantic Annotations";

					Window owner = SwingUtilities.getWindowAncestor(pageRef);
					if (owner instanceof Frame) {
						helpDialog = new HelpDialog((Frame) owner, title, Help.COLUMN_ANNOTATION_HELP_TEXT);
					}
					if (owner instanceof Dialog) {
						helpDialog = new HelpDialog((Dialog) owner, title, Help.COLUMN_ANNOTATION_HELP_TEXT);
					}
				}
				Point loc = pageRef.getLocationOnScreen();
				int wd = pageRef.getWidth();
				int ht = pageRef.getHeight();
				int dwd = HelpDialog.HELP_DIALOG_SIZE.width;
				int dht = HelpDialog.HELP_DIALOG_SIZE.height;
				helpDialog.setLocation((int) loc.getX() + wd / 2 - dwd / 2, (int) loc.getY() + ht / 2 - dht / 2);
				helpDialog.setSize(HelpDialog.HELP_DIALOG_SIZE);
				helpDialog.setVisible(true);
			}
		};

		GUIAction helpAction = 
			new GUIAction(label, null, command);
		JButton helpButton = new HyperlinkButton(helpAction);
		WidgetFactory.setPrefMaxSizes(helpButton, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		return helpButton;
	}
}
