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

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.ecoinformatics.sms.annotation.Observation;

/**
 * Panel for viewing and editing data-centric Annotations
 * @author leinfelder
 *
 */
public class AnnotationTable extends JTable {

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