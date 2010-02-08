/**
 *  '$RCSfile: EditColumnMetaDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
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

package org.ecoinformatics.sms.plugins.commands;

import java.awt.event.ActionEvent;
import java.util.Map.Entry;

import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.pages.OntologyManagerPage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * Class to handle managing the ontologies
 */
public class OntologyManagementCommand implements Command {

	private OntologyManagerPage managerPage;
	
	/**
	 * Constructor
	 */
	public OntologyManagementCommand() {}

	/**
	 * execute annotation wizard
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {
		
		managerPage = new OntologyManagerPage();
		if (showDialog()) {
			OrderedMap map = managerPage.getPageData();
			
			int i = 0;
			Morpho.getConfiguration().removeChildren(AnnotationPlugin.ONTOLOGY_TAG_NAME, i);
			for (Object entryObj: map.entrySet()) {
				Entry<String, String> entry = (Entry<String, String>) entryObj;
				String uri = entry.getKey();
				String url = entry.getValue();
				
				// TODO: verify values?
				
				// uri
				Morpho.getConfiguration().addChild(
						AnnotationPlugin.ONTOLOGY_TAG_NAME, 
						i, 
						AnnotationPlugin.LOGICAL_URI_TAG_NAME, 
						uri);
				// url
				Morpho.getConfiguration().addChild(
						AnnotationPlugin.ONTOLOGY_TAG_NAME, 
						i, 
						AnnotationPlugin.PHYSICAL_URI_TAG_NAME, 
						url);
			}
			Morpho.getConfiguration().save();
			// TODO: reload ontologies
		}
	}
	
	private boolean showDialog() {
		// show the dialog
		ModalDialog dialog = 
			new ModalDialog(
					managerPage,
					UIController.getInstance().getCurrentActiveWindow(),
					UISettings.POPUPDIALOG_WIDTH,
					UISettings.POPUPDIALOG_HEIGHT);
		
		//get the response back
		return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
	}

}
