/**
 *  '$RCSfile: PersistentTableModel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
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
package org.ecoinformatics.sms.plugins.table;

import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataViewer;

/**
 * Synchronizes the annotation table (columns) with the data table columns
 * @author leinfelder
 *
 */
public class DataTableModelListener implements TableModelListener {
	
	private AbstractDataPackage adp;
	private int entityIndex;
	private AnnotationTableModel annotationTableModel;
	private DataViewer dataViewer;

	public DataTableModelListener(AbstractDataPackage adp,
			AnnotationTableModel annotationTableModel, DataViewer dataViewer, int entityIndex) {
		this.adp = adp;
		this.annotationTableModel = annotationTableModel;
		this.dataViewer = dataViewer;
		this.entityIndex = entityIndex;
	}

	public void tableChanged(TableModelEvent e) {
		List<String> columnIds = adp.getAttributeNames(entityIndex);
		annotationTableModel.setColumnIds(columnIds);
		annotationTableModel.setColumnNames(dataViewer.getColumnLabels());
	}

}
