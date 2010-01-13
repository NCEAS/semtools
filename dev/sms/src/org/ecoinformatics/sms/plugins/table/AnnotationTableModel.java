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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import jp.gr.java_conf.tame.swing.table.AttributiveCellTableModel;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;

/**
 * TableModel for viewing and editing data-centric Annotations
 * @author leinfelder
 *
 */
public class AnnotationTableModel extends AttributiveCellTableModel {

	private Annotation annotation;
	
	private List<String> columnNames;
	
	private List<String> columnIds;
	
	private static final int ROW_COUNT = 6;
	public static final int CONTEXT_ROW = 0;
	public static final int OBSERVATION_ROW = 1;
	public static final int ENTITY_ROW = 2;
	public static final int CHARACTERISTIC_ROW = 3;
	public static final int STANDARD_ROW = 4;
	public static final int PROTOCOL_ROW = 5;

	
	public AnnotationTableModel(Annotation annotation, List<String> columns) {
		super(columns.toArray(), ROW_COUNT);
		this.annotation = annotation;
		this.columnIds = columns;
	}
	
	public Object[] getRowHeaders() {
		List<Object> rows = new ArrayList<Object>();
		//add the measurements
		rows.add("Context");
		rows.add("Observation");
		rows.add("Entity");
		rows.add("Characteristic");
		rows.add("Standard");
		rows.add("Protocol");
		return rows.toArray();
	}
	
	public int getColumnCount() {
		if (columnIds == null) {
			return 0;
		}
		return columnIds.size();
	}

	public int getRowCount() {
		return ROW_COUNT;
	}
	
	public String getColumnName(int column) {
		if (columnNames != null && !columnNames.isEmpty()) {
			return columnNames.get(column);
		}
		return columnIds.get(column);
	}	

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public List<String> getColumnIds() {
		return columnIds;
	}

	public void setColumnIds(List<String> columnIds) {
		this.columnIds = columnIds;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		
		
		// look up the column attribute
		String column = columnIds.get(columnIndex);
		
		// look up in the annotation
		Mapping mapping = annotation.getMapping(column);
		if (mapping != null) {
			Measurement measurement = mapping.getMeasurement();
			if (measurement != null) {
				Observation observation = annotation.getObservation(measurement);
				
				if (rowIndex == (ENTITY_ROW)) {
					if (observation != null) {
						return observation.getEntity();
					}
				}
				else if (rowIndex == (CHARACTERISTIC_ROW)) {
					// TODO multiple characteristics
					return measurement.getCharacteristics().get(0);
				}
				else if (rowIndex == (STANDARD_ROW)) {
					return measurement.getStandard();
				}
				else if (rowIndex == (PROTOCOL_ROW)) {
					return measurement.getProtocol();
				}
				else if (rowIndex == (OBSERVATION_ROW)) {
					return observation;
				}
				else if (rowIndex == (CONTEXT_ROW)) {
					//TODO handle multiple contexts?
					if (observation != null) {
						return observation.getContexts();
					}
				}
			}
		}
		return null;
	}
 
}
