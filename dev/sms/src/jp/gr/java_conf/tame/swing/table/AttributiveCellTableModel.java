/*
 * (swing1.1beta3)
 * 
 */

package jp.gr.java_conf.tame.swing.table;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;


/**
 * @version 1.0 11/22/98
 */

public class AttributiveCellTableModel extends DefaultTableModel {

  protected CellAttribute cellAtt;
    
  public AttributiveCellTableModel() {
    this((Vector)null, 0);
  }
  public AttributiveCellTableModel(int numRows, int numColumns) {
    Vector names = new Vector(numColumns);
    names.setSize(numColumns);
    setColumnIdentifiers(names);
    dataVector = new Vector();
    setNumRows(numRows);
    cellAtt = new DefaultCellAttribute(numRows,numColumns);
  }
  public AttributiveCellTableModel(Vector columnNames, int numRows) {
    setColumnIdentifiers(columnNames);
    dataVector = new Vector();
    setNumRows(numRows);
    cellAtt = new DefaultCellAttribute(numRows,columnNames.size());
  }
  public AttributiveCellTableModel(Object[] columnNames, int numRows) {
    this(convertToVector(columnNames), numRows);
  }  
  public AttributiveCellTableModel(Vector data, Vector columnNames) {
    setDataVector(data, columnNames);
  }
  public AttributiveCellTableModel(Object[][] data, Object[] columnNames) {
    setDataVector(data, columnNames);
  }

    
  public void setDataVector(Vector newData, Vector columnNames) {
    if (newData == null)
      throw new IllegalArgumentException("setDataVector() - Null parameter");
    dataVector = new Vector(0);
    columnIdentifiers = columnNames;
    dataVector = newData;
    
    //
    cellAtt = new DefaultCellAttribute(dataVector.size(),
                                       columnIdentifiers.size());
    
    newRowsAdded(new TableModelEvent(this, 0, getRowCount()-1,
		 TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
  }

  public void addColumn(Object columnName, Vector columnData) {
    if (columnName == null)
      throw new IllegalArgumentException("addColumn() - null parameter");
    columnIdentifiers.addElement(columnName);
    int index = 0;
    Enumeration enumeration = dataVector.elements();
    while (enumeration.hasMoreElements()) {
      Object value;
      if ((columnData != null) && (index < columnData.size()))
	  value = columnData.elementAt(index);
      else
	value = null;
      ((Vector)enumeration.nextElement()).addElement(value);
      index++;
    }

    //
    cellAtt.addColumn();

    fireTableStructureChanged();
  }

  public void addRow(Vector rowData) {
    Vector newData = null;
    if (rowData == null) {
      newData = new Vector(getColumnCount());
    }
    else {
      rowData.setSize(getColumnCount());
    }
    dataVector.addElement(newData);

    //
    cellAtt.addRow();

    newRowsAdded(new TableModelEvent(this, getRowCount()-1, getRowCount()-1,
       TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
  }

  public void insertRow(int row, Vector rowData) {
    if (rowData == null) {
      rowData = new Vector(getColumnCount());
    }
    else {
      rowData.setSize(getColumnCount());
    }

    dataVector.insertElementAt(rowData, row);

    //
    cellAtt.insertRow(row);

    newRowsAdded(new TableModelEvent(this, row, row,
       TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
  }

  public CellAttribute getCellAttribute() {
    return cellAtt;
  }

  public void setCellAttribute(CellAttribute newCellAtt) {
    int numColumns = getColumnCount();
    int numRows    = getRowCount();
    if ((newCellAtt.getSize().width  != numColumns) ||
        (newCellAtt.getSize().height != numRows)) {
      newCellAtt.setSize(new Dimension(numRows, numColumns));
    }
    cellAtt = newCellAtt;
    fireTableDataChanged();
  }

  /*
  public void changeCellAttribute(int row, int column, Object command) {
    cellAtt.changeAttribute(row, column, command);
  }

  public void changeCellAttribute(int[] rows, int[] columns, Object command) {
    cellAtt.changeAttribute(rows, columns, command);
  }
  */
    
}

