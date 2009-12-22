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

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Relationship;

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
public class ContextCommand implements Command {
	/* Reference to morpho frame */
	private MorphoFrame morphoFrame = null;

	private AbstractDataPackage adp = null;
	private DataViewer dataView = null;
	private JTable table = null;
	private DataViewContainerPanel resultPane = null;
	private int entityIndex = -1;
	private String entityName;
	private AnnotationPage annotationPage = new AnnotationPage();
	private Annotation annotation = null;
	private boolean add = true;

	/**
	 * Constructor
	 */
	public ContextCommand(boolean add) {
		this.add = add;
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
				
				// process the selected observation
				table = dataView.getDataTable();
				int viewIndex = table.getSelectedColumn();
				int attributeIndex =  table.getColumnModel().getColumn(viewIndex).getModelIndex();
				String attributeName = adp.getAttributeName(entityIndex, attributeIndex);

				// get the Observation that is/will be providing context
				Mapping mapping = annotation.getMapping(attributeName);
				Measurement measurement = mapping.getMeasurement();
				Observation contextObservation = annotation.getObservation(measurement);
				
				//ADD
				if (add) {
					// get the Observations it can provide context for
					List<Observation> observations = annotation.getObservations();
					
					// remove "this" one
					observations = new ArrayList<Observation>(observations);
					observations.remove(contextObservation);
					
					// ask for the observation 
					Object selectedObj = JOptionPane.showInputDialog(
							morphoFrame, 
							contextObservation + " provides context for: ", 
							"Define Context", 
							JOptionPane.INFORMATION_MESSAGE, 
							null, 
							observations.toArray(),
							null
							);
					if (selectedObj != null) {
						Observation selectedObservation = (Observation) selectedObj;
						Context context = new Context();
						context.setObservation(contextObservation);
						//TODO: relationship
						Relationship relationship = new Relationship();
						context.setRelationship(relationship);
						//TODO: identifying
						context.setIdentifying(false);
	
						selectedObservation.addContext(context);
					}
				}
				//REMOVE
				else {
					// get the existing contexts for this observation
					List<Context> existingContexts = contextObservation.getContexts();
					
					// ask for the context to remove 
					Object selectedObj = JOptionPane.showInputDialog(
							morphoFrame, 
							"Remove existing context for " + contextObservation, 
							"Remove Context", 
							JOptionPane.WARNING_MESSAGE, 
							null, 
							existingContexts.toArray(),
							null
							);
					if (selectedObj != null) {
						Context selectedContext = (Context) selectedObj;
						contextObservation.removeContext(selectedContext);
					}
				}
					
				// save - still some TBD
				AnnotationPlugin.saveAnnotation(annotation);
				
				// fire change event
				StateChangeEvent annotationEvent = new StateChangeEvent(annotationPage, AnnotationPlugin.ANNOTATION_CHANGE_EVENT);
				StateChangeMonitor.getInstance().notifyStateChange(annotationEvent);
				
			}

		}
	}
	
	

}
