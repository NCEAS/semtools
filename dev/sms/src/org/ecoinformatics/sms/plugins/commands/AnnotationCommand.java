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

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.pages.AnnotationPage;

import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.UISettings;

/**
 * Class to handle edit column meta data command
 */
public class AnnotationCommand implements Command {
	
	private String attributeName;
	private AnnotationPage annotationPage = null;
	private Annotation annotation = null;

	/**
	 * Constructor
	 */
	public AnnotationCommand() {}

	/**
	 * execute annotation wizard
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {

		annotation = AnnotationPlugin.getCurrentActiveAnnotation();
		attributeName = AnnotationPlugin.getCurrentSelectedAttribute();
				
		Log.debug(30, "Annotating...\n " 
				+ "Attribute: " + attributeName
				+ ", annotation id: " + annotation.getURI()
				);
		
		
		if (showDialog()) {
			
			//the page will put things together for us
			annotation = annotationPage.getAnnotation();
			
			// save - still some TBD
			AnnotationPlugin.saveAnnotation(annotation);
			
			// fire change event
			StateChangeEvent annotationEvent = new StateChangeEvent(annotationPage, AnnotationPlugin.ANNOTATION_CHANGE_EVENT);
			StateChangeMonitor.getInstance().notifyStateChange(annotationEvent);
			
		}
	}
	
	private boolean showDialog() {
		
		// set the annotation in the page
		annotationPage = new AnnotationPage(true);
		annotationPage.setAnnotation(annotation);
		annotationPage.editAttribute(attributeName);
		
		// show the dialog
		ModalDialog dialog = 
			new ModalDialog(
					annotationPage,
					UIController.getInstance().getCurrentActiveWindow(),
					UISettings.POPUPDIALOG_WIDTH,
					UISettings.POPUPDIALOG_HEIGHT,
					false);
		dialog.setModal(false);
		dialog.setVisible(true);
		
		//get the response back
		return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
	}

}
