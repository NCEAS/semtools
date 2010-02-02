/**
 *  '$RCSfile: EditColumnMetaDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 22:03:01 $'
 * '$Revision: 1.24 $'
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

package org.ecoinformatics.sms.plugins;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;

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
public class ObservationCommand implements Command {
	/* Reference to morpho frame */
	private MorphoFrame morphoFrame = null;

	private AbstractDataPackage adp = null;
	private DataViewer dataView = null;
	private JTable table = null;
	private DataViewContainerPanel resultPane = null;
	private int entityIndex = -1;
	private String entityName;
	private Annotation annotation = null;
	private boolean merge = true;

	/**
	 * Constructor
	 */
	public ObservationCommand(boolean merge) {
		this.merge = merge;
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
				entityName = adp.getEntityName(entityIndex);
				String entityId = dataView.getEntityFileId();
				
				// package and entity
				String packageId = adp.getAccessionNumber();
				String dataTable = String.valueOf(entityIndex);
				
				// look up the annotation if it exists, or make new one
				List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getAnnotations(packageId, dataTable);

				if (annotations.size() > 0) {
					annotation = annotations.get(0);
				} else {
					Log.debug(5, "No existing annotation found!");
					return;
				}
				
				// process the attributes
				table = dataView.getDataTable();
				int[] selectedColumns = table.getSelectedColumns();
				
				Observation targetObservation = null;
				List<Observation> observations = new ArrayList<Observation>();
				
				// iterate through the selected columns
				for (int viewIndex: selectedColumns) {

					int attributeIndex =  table.getColumnModel().getColumn(viewIndex).getModelIndex();
					String attributeName = adp.getAttributeName(entityIndex, attributeIndex);

					// get the annotation elements
					Mapping mapping = annotation.getMapping(attributeName);
					Measurement measurement = mapping.getMeasurement();
					Observation observation = annotation.getObservation(measurement);
					if (targetObservation == null) {
						targetObservation = observation;
					}
					
					// merge or split them?
					if (merge) {
						//must be multiples
						if (selectedColumns.length == 1) {
							Log.debug(5, "Multiple columns must be selected!");
							return;
						}
						// must be of the same entity
						if (!observation.getEntity().equals(targetObservation.getEntity())) {
							Log.debug(5, 
									"Observations must share the same Entity Type! \n" +
									observation.getEntity() + " != " + targetObservation.getEntity()
									);
							return;
						}
						//check that they are actually different observation instances currently
						if (!observation.equals(targetObservation)) {
							targetObservation.addMeasurement(measurement);
							observation.removeMeasurement(measurement);
							// remember to remove this observation form the annotation later
							observations.add(observation);
						}
					} else {
						if (selectedColumns.length != 1) {
							Log.debug(5, "Only a single column can be selected!");
							return;
						}
						if (observation.getMeasurements().size() == 1) {
							Log.debug(5, "Selected observation contains only one measurement - will not split!");
							return;
						}
						// create a new observation from the old one
						Observation splitObservation = new Observation();
						splitObservation.setEntity(observation.getEntity());
						splitObservation.setLabel(Annotation.getNextObservationLabel(annotation, "o"));
						splitObservation.addMeasurement(measurement);				
						annotation.addObservation(splitObservation);
						// remove the measurement from the old observation
						observation.removeMeasurement(measurement);
					}
				}
				
				// get rid of the old observations
				for (Observation o: observations) {
					annotation.removeObservation(o);
				}
				
					
				// save - still some TBD
				AnnotationPlugin.saveAnnotation(annotation);
				
				// fire change event
				StateChangeEvent annotationEvent = new StateChangeEvent(morphoFrame, AnnotationPlugin.ANNOTATION_CHANGE_EVENT);
				StateChangeMonitor.getInstance().notifyStateChange(annotationEvent);
				
			}

		}
	}

}
