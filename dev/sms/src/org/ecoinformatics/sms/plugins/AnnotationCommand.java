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
import java.util.List;

import javax.swing.JTable;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AccessionNumber;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.DataViewer;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;

/**
 * Class to handle edit column meta data command
 */
public class AnnotationCommand implements Command {
	/* Reference to morpho frame */
	private MorphoFrame morphoFrame = null;

	private AbstractDataPackage adp = null;
	private DataViewer dataView = null;
	private JTable table = null;
	private DataViewContainerPanel resultPane = null;
	private int entityIndex = -1;
	private int attributeIndex = -1;
	private String entityName;
	private String attributeName;
	private AnnotationPage annotationPage = new AnnotationPage();
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

				String entityId = dataView.getEntityFileId();
				table = dataView.getDataTable();
				attributeIndex = table.getSelectedColumn();
				entityIndex = dataView.getEntityIndex();
				entityName = adp.getEntityName(entityIndex);
				attributeName = adp.getAttributeName(entityIndex, attributeIndex);
				
				// package and entity
				String packageId = adp.getAccessionNumber();
				String dataTable = String.valueOf(entityIndex);
				
				// look up the annotation if it exists, or make new one
				List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getAnnotations(packageId, dataTable);

				if (annotations.size() > 0) {
					annotation = annotations.get(0);
				} else {
					// create a new one
					annotation = new Annotation();
					annotation.setEMLPackage(packageId);
					annotation.setDataTable(dataTable);
				}
				
				Log.debug(5, "Annotating...\n " 
						+ "Data package: " + packageId 
						+ ", entity: " + entityName 
						+ ", attribute: " + attributeName
						+ ", annotation id: " + annotation.getURI()
						);
				
				
				if (showDialog()) {
					
					// save - still some TBD
					saveAnnotation();
					
				}
			}

		}
	}
	
	private boolean showDialog() {
		
		// set the annotation in the page
		annotationPage = new AnnotationPage();
		annotationPage.setAnnotation(annotation, attributeName);
		
		// show the dialog
		ModalDialog dialog = 
			new ModalDialog(
					annotationPage,
					UIController.getInstance().getCurrentActiveWindow(),
					UISettings.POPUPDIALOG_WIDTH,
					UISettings.POPUPDIALOG_HEIGHT);
		
		//get the response back
		return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
	}
	
	private void saveAnnotation() {
		
		try {
			
			//the page will put things together for us
			annotation = annotationPage.getAnnotation(attributeName);
			
			// about to save
			AccessionNumber accNum = new AccessionNumber(Morpho.thisStaticInstance);
			String id = annotation.getURI();
			if (id == null) {
				id = accNum.getNextId();
			} else {
				// remove the old one if present
				if (SMS.getInstance().getAnnotationManager().isAnnotation(id)) {
					SMS.getInstance().getAnnotationManager().removeAnnotation(id);
				}
				id = accNum.incRev(id);
			}
			annotation.setURI(id);
			
			//save in the manager
			SMS.getInstance().getAnnotationManager().importAnnotation(annotation, annotation.getURI());
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
