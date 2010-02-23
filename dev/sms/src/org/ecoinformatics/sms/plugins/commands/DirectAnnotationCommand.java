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
import java.util.List;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.pages.OntologyClassSelectionPage;
import org.ecoinformatics.sms.plugins.table.AnnotationTable;
import org.ecoinformatics.sms.plugins.table.AnnotationTableModel;

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
public class DirectAnnotationCommand implements Command {
	
	private String attributeName;
	private OntologyClassSelectionPage ontologyPage = null;
	private Annotation annotation = null;

	private AnnotationTable annotationTable = null;

	private Observation currentObservation;
	private Mapping currentMapping;
	private Measurement currentMeasurement;
	private Characteristic currentCharacteristic;
	private Standard currentStandard;
	private Protocol currentProtocol;
	
	/**
	 * Constructor
	 */
	public DirectAnnotationCommand(AnnotationTable annotationTable) {
		this.annotationTable = annotationTable;
		
	}
	
	private void reset() {
		annotation = null;
		currentObservation = null;
		currentMapping = null;
		currentMeasurement = null;
		currentCharacteristic = null;
		currentStandard = null;
		currentProtocol = null;
	}

	/**
	 * execute annotation wizard
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {
		
		// start from scratch each time
		reset();
		
		// check if we have a selection
		int selectedColumn = annotationTable.getSelectedColumn();
    	int selectedRow = annotationTable.getSelectedRow();
    	
    	if (selectedColumn == -1 || selectedRow == -1) {
    		return;
    	}
    	
    	annotation = AnnotationPlugin.getCurrentActiveAnnotation();
		String attributeName = AnnotationPlugin.getCurrentSelectedAttribute();
				
		Log.debug(30, "Directly Annotating...\n " 
				+ "Attribute: " + attributeName
				+ ", annotation id: " + annotation.getURI()
				);
		
		
		if (showDialog()) {
			
			try {
				setClass();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// save - still some TBD
			AnnotationPlugin.saveAnnotation(annotation);
			
			// fire change event
			StateChangeEvent annotationEvent = new StateChangeEvent(ontologyPage, AnnotationPlugin.ANNOTATION_CHANGE_EVENT);
			StateChangeMonitor.getInstance().notifyStateChange(annotationEvent);
			
		}
	}
	
	private boolean setClass() throws Exception {
		
		// what's the class?
		String selectedClassString = null;
		List<String> terms = ontologyPage.getSelectedTerms();
		if (!terms.isEmpty()) {
			selectedClassString = terms.get(0);
		}
		
		if (currentMapping == null) {
			currentMapping = new Mapping();
			currentMapping.setAttribute(attributeName);
			annotation.addMapping(currentMapping);
		}
		// most things rely on a measurement
		if (currentMeasurement == null) {
			currentMeasurement = new Measurement();
			currentMeasurement.setLabel(Annotation.getNextMeasurementLabel(annotation, "m"));
		}
		currentMapping.setMeasurement(currentMeasurement);
		
		// if we don't have an observation, we don't have anything!
		if (currentObservation == null) {
			currentObservation = new Observation();
			currentObservation.setLabel(Annotation.getNextObservationLabel(annotation, "o"));
			currentObservation.addMeasurement(currentMeasurement);
			annotation.addObservation(currentObservation);
		}
		
		int selectedRow = annotationTable.getSelectedRow();
		switch (selectedRow) {
			case AnnotationTableModel.OBSERVATION_ROW:
				Entity entity = null;
				if (selectedClassString != null) {
					entity = new Entity(selectedClassString);
					annotation.addOntology(entity.getOntology());
				}
				currentObservation.setEntity(entity);
				break;
			case AnnotationTableModel.CHARACTERISTIC_ROW:
				// handle null selection
				if (selectedClassString == null) {
					currentMeasurement.getCharacteristics().clear();
					break;
				}
				if (currentCharacteristic == null) {
					currentCharacteristic = new Characteristic();
					currentMeasurement.getCharacteristics().clear();
					currentMeasurement.addCharacteristic(currentCharacteristic);
				}
				currentCharacteristic.setURI(selectedClassString);
				annotation.addOntology(currentCharacteristic.getOntology());
				break;
			case AnnotationTableModel.STANDARD_ROW:
				// handle null selection
				if (selectedClassString == null) {
					currentMeasurement.setStandard(null);
					break;
				}
				if (currentStandard == null) {
					currentStandard = new Standard();
				}
				currentMeasurement.setStandard(currentStandard);
				currentStandard.setURI(selectedClassString);
				annotation.addOntology(currentStandard.getOntology());
				break;
			case AnnotationTableModel.PROTOCOL_ROW:
				// handle null selection
				if (selectedClassString == null) {
					currentMeasurement.setProtocol(null);
					break;
				}
				if (currentProtocol == null) {
					currentProtocol = new Protocol();
				}
				currentMeasurement.setProtocol(currentProtocol);
				currentProtocol.setURI(selectedClassString);
				annotation.addOntology(currentProtocol.getOntology());
				break;	
	
			default:
				break;
			}
		
		return true;
	}
	private boolean showDialog() {
		
		// what do we have set already?
		currentMapping = annotation.getMapping(attributeName);

		if (currentMapping != null) {
			currentMeasurement = currentMapping.getMeasurement();
		}
		if (currentMeasurement != null) {
			currentObservation = annotation.getObservation(currentMeasurement);
		}
		
		// set the class in the page in the page
		ontologyPage = new OntologyClassSelectionPage();
		OntologyClass currentClass = null;
		
		// what are we editing?
		int selectedRow = annotationTable.getSelectedRow();
		switch (selectedRow) {
			case AnnotationTableModel.CONTEXT_ROW:
				// we don't edit this directly
				return false;
			case AnnotationTableModel.OBSERVATION_ROW:
				ontologyPage.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Entity.class));
				if (currentObservation != null) {
					currentClass = currentObservation.getEntity();
				}
				break;
			case AnnotationTableModel.CHARACTERISTIC_ROW:
				ontologyPage.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Characteristic.class));
				if (currentMeasurement != null) {
					if (!currentMeasurement.getCharacteristics().isEmpty()) {
						currentCharacteristic = currentMeasurement.getCharacteristics().get(0);
						currentClass = currentCharacteristic;
					}
				}
				break;
			case AnnotationTableModel.STANDARD_ROW:
				ontologyPage.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Standard.class));
				if (currentMeasurement != null) {
					currentStandard = currentMeasurement.getStandard();
					currentClass = currentStandard;
				}
				break;
			case AnnotationTableModel.PROTOCOL_ROW:
				ontologyPage.setFilterClass(AnnotationPlugin.OBOE_CLASSES.get(Protocol.class));
				if (currentMeasurement != null) {
					currentProtocol = currentMeasurement.getProtocol();
					currentClass = currentProtocol;
				}
				break;	
	
			default:
				break;
			}
		
		// set the current class
		ontologyPage.setCurrentClass(currentClass);
		
		// show the dialog
		ModalDialog dialog = 
			new ModalDialog(
					ontologyPage,
					UIController.getInstance().getCurrentActiveWindow(),
					UISettings.POPUPDIALOG_WIDTH,
					UISettings.POPUPDIALOG_HEIGHT);
		
		//get the response back
		return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
	}

}
