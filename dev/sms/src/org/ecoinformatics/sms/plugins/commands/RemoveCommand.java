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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.DataViewer;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

/**
 * Class to handle edit column meta data command
 */
public class RemoveCommand implements Command {
	/* Reference to morpho frame */
	private MorphoFrame morphoFrame = null;

	private AbstractDataPackage adp = null;
	private DataViewer dataView = null;
	private JTable table = null;
	private DataViewContainerPanel resultPane = null;
	private int entityIndex = -1;
	private Annotation annotation = null;
	private Class toRemove = Observation.class;

	/**
	 * Constructor
	 */
	public RemoveCommand(Class toRemove) {
		this.toRemove = toRemove;
	}

	/**
	 * execute annotation wizard
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {

		morphoFrame = UIController.getInstance().getCurrentActiveWindow();

		if (morphoFrame != null) {
			resultPane = morphoFrame.getDataViewContainerPanel();
		}

		if (resultPane != null) {
			adp = resultPane.getAbstractDataPackage();
		}

		if (adp == null) {
			Log.debug(16, " Abstract Data Package is null in "
					+ this.getClass().getName());
			return;
		}

		// make sure resultPanel is not null
		if (resultPane != null) {
			dataView = resultPane.getCurrentDataViewer();
			if (dataView != null) {
				
				//entity
				entityIndex = dataView.getEntityIndex();
				
				// package and entity
				String packageId = adp.getAccessionNumber();
				String dataTable = String.valueOf(entityIndex);
				
				// look up the annotation if it exists
				List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getAnnotations(packageId, dataTable);

				if (annotations.size() > 0) {
					annotation = annotations.get(0);
				} else {
					Log.debug(5, "No existing annotation found!");
					return;
				}
				
				// process the attribute
				table = dataView.getDataTable();
				int viewIndex = table.getSelectedColumn();		
				int attributeIndex =  table.getColumnModel().getColumn(viewIndex).getModelIndex();
				String attributeName = adp.getAttributeName(entityIndex, attributeIndex);

				// get the annotation elements
				Mapping mapping = annotation.getMapping(attributeName);
				Measurement measurement = mapping.getMeasurement();
				Observation observation = annotation.getObservation(measurement);

				if (toRemove.equals(Observation.class)) {
					int remove = JOptionPane.showConfirmDialog(
							morphoFrame, 
							"Are you sure you want to remove Observation, " + observation, 
							"Removing " + observation, 
							JOptionPane.YES_NO_OPTION);
					
					if (remove == JOptionPane.NO_OPTION) {
						return;
					}
					
					// check for contexts
					for (Observation obs: annotation.getObservations()) {
						for (Context c: obs.getContexts()) {
							if (c.getObservation() != null && c.getObservation().equals(observation)) {
								//TODO concurrent modification?
								//obs.removeContext(c);
								Log.debug(5, 
										observation + " provides context for " + obs 
										+ "\nRemove this Context relationship before removing " + observation);
								return;
							}
						}
					}
					// remove the mapping
					annotation.removeMapping(mapping);
					// remove the observation
					annotation.removeObservation(observation);
				}
				
				else if (toRemove.equals(Measurement.class)) {
					int remove = JOptionPane.showConfirmDialog(
							morphoFrame, 
							"Are you sure you want to remove Measurement, " + measurement, 
							"Removing " + measurement, 
							JOptionPane.YES_NO_OPTION);
					
					if (remove == JOptionPane.NO_OPTION) {
						return;
					}
					
					// remove it from the observation
					observation.removeMeasurement(measurement);
					// just remove the mapping
					annotation.removeMapping(mapping);
				}
					
				// save the change
				AnnotationPlugin.saveAnnotation(annotation);
				
				// fire change event
				StateChangeEvent annotationEvent = new StateChangeEvent(morphoFrame, AnnotationPlugin.ANNOTATION_CHANGE_EVENT);
				StateChangeMonitor.getInstance().notifyStateChange(annotationEvent);
				
			}

		}
	}

}
