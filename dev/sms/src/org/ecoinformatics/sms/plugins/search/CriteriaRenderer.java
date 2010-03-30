package org.ecoinformatics.sms.plugins.search;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.ui.OntologyClassField;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

public class CriteriaRenderer extends AbstractCellEditor implements 
	TableCellRenderer,
	TableCellEditor {

	private Criteria criteria;
	
	private JPanel criteriaPanel;
	private JPanel subcriteriaPanel;
	private JPanel instance;
	private JComboBox subject;
	private JComboBox condition;
	private OntologyClassField value;
	
	private JCheckBox anyAll;
	private CustomList subCriteria;
	
	public CriteriaRenderer(int level) {
		
		instance = WidgetFactory.makePanel();
		instance.setLayout(new BoxLayout(instance, BoxLayout.Y_AXIS));
		
		// change the filter class based on what is selected 
		ItemListener subjectListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox source = (JComboBox) e.getSource();
				value.setFilterClass((OntologyClass) source.getSelectedItem());
			}
		};
		Object[] subjectValues = new OntologyClass[] {
				AnnotationPlugin.OBOE_CLASSES.get(Entity.class), 
				AnnotationPlugin.OBOE_CLASSES.get(Characteristic.class), 
				AnnotationPlugin.OBOE_CLASSES.get(Standard.class), 
				AnnotationPlugin.OBOE_CLASSES.get(Protocol.class)};
		subject = WidgetFactory.makePickList(subjectValues, false, 0, subjectListener);
		
		Object[] conditionValues = new String[] {"is", "is not"};
		condition = WidgetFactory.makePickList(conditionValues, false, 0, null);
		
		value = OntologyClassField.makeLabel("", false, null);
		value.setFilterClass((OntologyClass) subject.getSelectedItem());
		
		// make the panel
		criteriaPanel = WidgetFactory.makePanel();
		criteriaPanel.setLayout(new GridLayout(1,3));

		criteriaPanel.add(subject);
		criteriaPanel.add(condition);
		criteriaPanel.add(value);
		
		instance.add(criteriaPanel);
		
		subcriteriaPanel = WidgetFactory.makePanel(5);

		if (level > 0) {
			
			anyAll = WidgetFactory.makeCheckBox("Match Any", false);
			String[] colNames = new String[] {"Subcriteria"};
			Object[] editors = new Object[] {new CriteriaRenderer(--level) };
			subCriteria = WidgetFactory.makeList(
					colNames, 
					editors, 
					4, //displayRows, 
					true, //showAddButton, 
					false, //showEditButton, 
					false, //showDuplicateButton, 
					true, //showDeleteButton, 
					false, //showMoveUpButton, 
					false //showMoveDownButton
					);		
			// add
			subCriteria.setCustomAddAction(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
		
					Criteria criteria = new Criteria();
					criteria.setGroup(false);
					
					List rowList = new ArrayList();
					rowList.add(criteria);
					subCriteria.addRow(rowList);
					
				}
			});
			subcriteriaPanel.setLayout(new BoxLayout(subcriteriaPanel, BoxLayout.X_AXIS));

			subcriteriaPanel.add(anyAll);
			subcriteriaPanel.add(subCriteria);
			instance.add(subcriteriaPanel);
		}
		
	}
	
	public void setEnabled(boolean enabled) {
		subject.setEnabled(enabled);
		condition.setEnabled(enabled);
		value.setEnabled(enabled);
	}
	
	private Component getComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		if (value instanceof Criteria) {
			criteria = (Criteria) value;
			
			//set visibility
			criteriaPanel.setVisible(!criteria.isGroup());
			subcriteriaPanel.setVisible(criteria.isGroup());
			
			subject.setSelectedItem(criteria.getSubject());
			condition.setSelectedItem(criteria.getCondition());
			this.value.setOntologyClass(criteria.getValue());
			
			if (criteria.isGroup()) {
				anyAll.setSelected(criteria.isAny());
				subCriteria.removeAllRows();
				if (criteria.getSubCriteria() != null) {
					for (Criteria c: criteria.getSubCriteria()) {
						List rowList = new ArrayList();
						rowList.add(c);
						subCriteria.addRow(rowList);
					}
				}
			}
			
			table.setRowHeight(instance.getPreferredSize().height + 5);
		}
		return instance;

	}
	
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		return getComponent(table, value, isSelected, row, column);
	}

	public Object getCellEditorValue() {
		criteria.setSubject((OntologyClass) subject.getSelectedItem());
		criteria.setCondition((String) condition.getSelectedItem());
		criteria.setValue(value.getOntologyClass());
		if (subCriteria != null && subCriteria.getRowCount() > 0) {
			criteria.setAny(anyAll.isSelected());
			criteria.setSubCriteria(new ArrayList<Criteria>());
			for (Object obj: subCriteria.getListOfRowLists()) {
				List rowList = (List) obj;
				criteria.getSubCriteria().add((Criteria) rowList.get(0));
			}
		}
		return criteria;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return getComponent(table, value, isSelected, row, column);
	}
	
}
