package org.ecoinformatics.sms.plugins.search;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class CriteriaRenderer 
	extends AbstractCellEditor 
		implements 
			TableCellRenderer,
			TableCellEditor {

	private Criteria criteria;
	
	private CriteriaPanel criteriaPanelEditor;
	
	public CriteriaRenderer(boolean isGroup) {
		criteriaPanelEditor = new CriteriaPanel(isGroup);
	}
	
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		// get the existing Criteria value
		if (value instanceof Criteria) {
			criteria = (Criteria) value;
			// make an editor panel if needed
			if (criteriaPanelEditor == null) {
				criteriaPanelEditor = new CriteriaPanel(criteria.isGroup());
			}
			// set the existing value
			criteriaPanelEditor.setCriteria(criteria);
			table.setRowHeight(criteriaPanelEditor.getPreferredSize().height + 5);
		}
		return criteriaPanelEditor;

	}
	
	public Object getCellEditorValue() {
		criteria = criteriaPanelEditor.getCriteria();
		return criteria;
		
	}

	// different panel instance for the renderer - very important!
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		CriteriaPanel criteriaPanelRenderer = null;
		// get the value to render
		// get the existing Criteria value
		if (value instanceof Criteria) {
			Criteria c = (Criteria) value;
			// make a renderer panel
			criteriaPanelRenderer = new CriteriaPanel(c.isGroup());
			// set the existing value
			criteriaPanelRenderer.setCriteria(c);
			table.setRowHeight(criteriaPanelRenderer.getPreferredSize().height + 5);
		}
		return criteriaPanelRenderer;
	}
	
	public static void main(String[] args) {
		JTable table = new JTable(new Object[][] {new Object[] {new Criteria()}}, new Object[][] {new Object[] {"Criteria"}} );
		table.getColumnModel().getColumn(0).setCellRenderer(new CriteriaRenderer(true));
		JFrame frame = new JFrame();
		frame.add(table);
		frame.setVisible(true);
	}
	
}
