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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.TableCellRenderer;

import jp.gr.java_conf.tame.swing.table.CellSpan;
import jp.gr.java_conf.tame.swing.table.MultiSpanCellTable;

import org.ecoinformatics.sms.annotation.Observation;

/**
 * Panel for viewing and editing data-centric Annotations
 * @author leinfelder
 *
 */
public class AnnotationTable extends MultiSpanCellTable {

	public AnnotationTable(AnnotationTableModel annotationTableModel) {
		super(annotationTableModel);
//		this.setIntercellSpacing(new Dimension(0,0));
//		this.setShowVerticalLines(false);
//		this.setShowHorizontalLines(false);
//		this.setShowGrid(false);
	}

	public TableCellRenderer getCellRenderer(int row, int column) {
		return new ObservationCellRenderer();
	}
	
	public void reorder(boolean includeContext) {
		List<Observation> sortedObservations = new ArrayList<Observation>();
		
		for (int i = 0; i < this.getColumnCount(); i++) {
			Observation cellObs = (Observation) this.getValueAt(AnnotationTableModel.OBSERVATION_ROW, i);
			if (cellObs != null) {
				sortedObservations.add(cellObs);
			}
		}
		if (includeContext) {
			Collections.sort(sortedObservations, new ObservationComparator());
		} else {
			Collections.sort(sortedObservations);
		}
		
		for (int i = 0; i < this.getColumnCount(); i++) {
			Observation cellObs = (Observation) this.getValueAt(AnnotationTableModel.OBSERVATION_ROW, i);
			if (cellObs != null) {
				int sortedIndex = sortedObservations.indexOf(cellObs);
				this.moveColumn(i, sortedIndex);
			}
		}
		
		// merge the "duplicate" cells
		this.group();

	}
	
	private void group() {
		
		//group the same cells in the table
		CellSpan cellAtt = (CellSpan) ((AnnotationTableModel)this.getModel()).getCellAttribute();
				
		int[] contextRows = new int[]{AnnotationTableModel.CONTEXT_ROW};
		int[] obsRows = new int[]{AnnotationTableModel.OBSERVATION_ROW};
		int[] spacerRows = new int[]{AnnotationTableModel.SPACER_ROW};
		int[] entityRows = new int[]{AnnotationTableModel.ENTITY_ROW};
		
		// group the same observations now that they are in order
		List<Integer> sameColumns = new ArrayList<Integer>();
		
		Observation cellObs = (Observation) this.getValueAt(AnnotationTableModel.OBSERVATION_ROW, 0);
		for (int i = 0; i < this.getColumnCount(); i++) {
			Observation nextCellObs = (Observation) this.getValueAt(AnnotationTableModel.OBSERVATION_ROW, i);
			if (cellObs == null) {
				continue;
			}
			// still the same cell
			if (nextCellObs != null
				&& cellObs.equals(nextCellObs)) {
					sameColumns.add(i);
			} else {
				// different cell
				int[] columns = new int[sameColumns.size()];
				for (int j = 0; j < sameColumns.size(); j++) {
					columns[j] = sameColumns.get(j);
				}
				cellAtt.combine(contextRows, columns);
				cellAtt.combine(obsRows, columns);
				cellAtt.combine(entityRows, columns);
				sameColumns.clear(); //start again
				sameColumns.add(i);
				cellObs = (Observation) this.getValueAt(AnnotationTableModel.OBSERVATION_ROW, i);
			}
		}
		
		//combine all spacer cells into one
		int[] columns = new int[this.getColumnCount()];
		for (int j = 0; j < this.getColumnCount(); j++) {
			columns[j] = j;
		}
		cellAtt.combine(spacerRows, columns);

		// show it
		this.clearSelection();
		this.validate();
		this.repaint();
	}
	
	public void reset() {
		
		CellSpan cellAtt = (CellSpan) ((AnnotationTableModel)this.getModel()).getCellAttribute();

		for (int column = 0; column < this.getColumnCount(); column++) {
		
			cellAtt.split(AnnotationTableModel.CONTEXT_ROW, column);
			cellAtt.split(AnnotationTableModel.OBSERVATION_ROW, column);
			cellAtt.split(AnnotationTableModel.ENTITY_ROW, column);
		}
		
		// show it
		this.clearSelection();
		this.revalidate();
		this.repaint();
	}
}
class ObservationComparator implements Comparator<Observation> {

	public int compare(Observation o1, Observation o2) {
		if (o1 == null && o2 == null) {
			return 0;
		}
		if (o1.containsObservation(o2)) {
			return 1;
		}
		else if (o2.containsObservation(o1)) {
			return -1;
		}
		// default w/o context consideration
		return o1.compareTo(o2);
	}
	
}