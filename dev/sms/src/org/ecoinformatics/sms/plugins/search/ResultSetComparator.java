/**
 * 
 */
package org.ecoinformatics.sms.plugins.search;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * This class sorts Morpho's ResultSet based on an ordered list
 * of values. The value is looked up from the column in eac row
 * and placed according to that value's position in the given list
 * @author leinfelder
 *
 */
public class ResultSetComparator implements Comparator<Vector> {
	
	private int columnIndex;
	private List orderedValues;
	
	public ResultSetComparator(int col, List values) {
		this.columnIndex = col;
		this.orderedValues = values;
	}
	
	public int compare(Vector o1, Vector o2) {
		// look up the value from the column in the two rows
		Object value1 = o1.get(columnIndex);
		Object value2 = o2.get(columnIndex);

		// find the indexes of those two values in the ordered list
		Integer orderIndex1 = orderedValues.indexOf(value1);
		Integer orderIndex2 = orderedValues.indexOf(value2);
		
		// compare those
		if (orderIndex1 != null && orderIndex2 != null) {
			return orderIndex1.compareTo(orderIndex2);
		}
		// if null then just put them wherever
		return 0;
	}

}
