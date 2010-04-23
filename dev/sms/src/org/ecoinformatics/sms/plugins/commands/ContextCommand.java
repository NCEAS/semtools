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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Relationship;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.pages.AddContextPage;

import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.UISettings;

/**
 * Class to handle edit column meta data command
 */
public class ContextCommand implements Command {

	private AddContextPage contextPage = null;
	private Annotation annotation = null;
	private Context currentContext;

	private int mode = 0;
	
	public static final int ADD = 0;
	public static final int REMOVE = 1;
	public static final int EDIT = 2;

	/**
	 * Constructor
	 */
	public ContextCommand(int mode) {
		this.mode = mode;
	}

	/**
	 * execute annotation wizard
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {

		annotation = AnnotationPlugin.getCurrentActiveAnnotation();
		String attributeName = AnnotationPlugin.getCurrentSelectedAttribute();

		// get the Observation that is/will be providing context
		Mapping mapping = annotation.getMapping(attributeName);
		Measurement measurement = mapping.getMeasurement();
		Observation currentObservation = annotation.getObservation(measurement);
		
		Component source = null;
		//ADD
		if (mode == ADD || mode == EDIT) {
			// get the Observations it can provide context for
			List<Observation> observations = annotation.getObservations();
			
			// remove "this" one
			observations = new ArrayList<Observation>(observations);
			observations.remove(currentObservation);
			
			// page for editing the new context relationship
			contextPage = new AddContextPage(true);
			
			// for the state change event
			source = contextPage;
			
			// set the context if we have it
			if (mode == ADD) {
				currentContext = null;
			}
			contextPage.setContext(currentContext);
			
			// set "this" observation
			contextPage.setObservation(currentObservation);
			
			// set the observations it might have a relationship with
			contextPage.setObservations(observations);
			
			if (showContextDialog()) {
				// check that the context exists, add it otherwise
				currentContext = contextPage.getContext();
				if (currentContext == null) {
					currentContext = new Context();
					currentObservation.addContext(currentContext);
				}
				Observation selectedObservation = (Observation) contextPage.getSelectedObservation();
				currentContext.setObservation(selectedObservation);
				
				// relationship
				Relationship relationship = contextPage.getRelationship();
				currentContext.setRelationship(relationship);
				if (relationship != null) {
					annotation.addOntology(relationship.getOntology());
				}
				
				// identifying
				boolean isIdentifying = contextPage.getIsIdentifying();
				currentContext.setIdentifying(isIdentifying);

			} else {
				// no need to continue - canceled
				return;
			}
		}
		//REMOVE
		else if (mode == REMOVE){
			// get the existing contexts for this observation
			List<Context> existingContexts = currentObservation.getContexts();
			
			// ask for the context to remove 
			Object selectedObj = JOptionPane.showInputDialog(
					UIController.getInstance().getCurrentActiveWindow(), 
					"Remove existing context from: " + currentObservation, 
					"Remove Context", 
					JOptionPane.WARNING_MESSAGE, 
					null, 
					existingContexts.toArray(),
					null
					);
			
			if (selectedObj != null) {
				Context selectedContext = (Context) selectedObj;
				currentObservation.removeContext(selectedContext);
			} else {
				// cancelled
				return;
			}
			
			// for the state change event
			source = UIController.getInstance().getCurrentActiveWindow();
		}
			
		// made it here
		
		// save to the annotation
		AnnotationPlugin.saveAnnotation(annotation);
		
		// fire change event
		StateChangeEvent annotationEvent = new StateChangeEvent(source, AnnotationPlugin.ANNOTATION_CHANGE_EVENT);
		StateChangeMonitor.getInstance().notifyStateChange(annotationEvent);
				

	}
	
	private boolean showContextDialog() {
		// show the dialog
		ModalDialog dialog = 
			new ModalDialog(
					contextPage,
					UIController.getInstance().getCurrentActiveWindow(),
					UISettings.POPUPDIALOG_WIDTH,
					UISettings.POPUPDIALOG_HEIGHT);
		
		//get the response back
		return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
	}

	public Context getCurrentContext() {
		return currentContext;
	}

	public void setCurrentContext(Context currentContext) {
		this.currentContext = currentContext;
	}
	
	

}
