/**
 *  '$RCSfile: PersistentTableModel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2004-04-06 01:27:05 $'
 * '$Revision: 1.15 $'
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

import javax.swing.table.AbstractTableModel;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;

/**
 * TableModel for viewing and editing data-centric Annotations
 * @author leinfelder
 *
 */
public class AnnotationTableModel extends AbstractTableModel {

	private Annotation annotation;
	
	private List<String> columnNames;
	
	private static final int ROW_COUNT = 3;
	private static final int ENTITY_ROW = 0;
	private static final int CHARACTERISTIC_ROW = 1;
	private static final int STANDARD_ROW = 2;
	
	public AnnotationTableModel(Annotation annotation, List<String> columns) {
		this.annotation = annotation;
		this.columnNames = columns;
	}
	
	public String[] getRowHeaders() {
		return (new String[] {"Entity", "Characteristic", "Standard"});
	}
	
	public int getColumnCount() {
		return columnNames.size();
	}

	public int getRowCount() {
		return ROW_COUNT;
	}
	
	public String getColumnName(int column) {
		return columnNames.get(column);
	}
	

	public Object getValueAt(int rowIndex, int columnIndex) {
		// look up the column attribute
		String column = columnNames.get(columnIndex);
		
		// look up in the annotation
		Mapping mapping = annotation.getMapping(column);
		if (mapping != null) {
			Measurement measurement = mapping.getMeasurement();
			if (measurement != null) {
				switch (rowIndex) {
				case ENTITY_ROW:
					Observation observation = annotation.getObservation(measurement);
					return observation.getEntity();
				case CHARACTERISTIC_ROW:
					// TODO multiple characteristics
					return measurement.getCharacteristics().get(0);
				case STANDARD_ROW:
					return measurement.getStandard();
				default:
					break;
				}
			}
		}
		
		return (rowIndex + ", " + columnIndex);
		//return null;
	}
 
}
