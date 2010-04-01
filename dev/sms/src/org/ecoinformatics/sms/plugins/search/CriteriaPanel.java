package org.ecoinformatics.sms.plugins.search;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.ui.OntologyClassField;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

public class CriteriaPanel extends JPanel {

	private Criteria criteria;
	
	private JPanel criteriaPanel;
	private JComboBox subject;
	private JComboBox condition;
	private OntologyClassField value;
	
	private JPanel subcriteriaPanel;
	private JCheckBox anyAll;
	private CustomList subCriteria;
	
	
	public CriteriaPanel(boolean isGroup) {
		super();
				
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
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
		
		this.add(criteriaPanel);
		
		subcriteriaPanel = WidgetFactory.makePanel(5);
		anyAll = WidgetFactory.makeCheckBox("Match All", false);

		// only create the subcriteria list if this is a grouping criteria (infinite loop possible otherwise)
		if (isGroup) {
			String[] colNames = new String[] {"Subcriteria"};
			// non-group criteria are rendered by this list
			Object[] editors = new Object[] {new CriteriaRenderer(false) };
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
			// add action
			subCriteria.setCustomAddAction(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					
					// we add non-group criteria at this point
					Criteria criteria = new Criteria();
					criteria.setGroup(false);
					
					List rowList = new ArrayList();
					rowList.add(criteria);
					subCriteria.addRow(rowList);
					
				}
			});
			
			// add the subcriteria widgets to the panel
			subcriteriaPanel.setLayout(new BoxLayout(subcriteriaPanel, BoxLayout.X_AXIS));
			subcriteriaPanel.add(anyAll);
			subcriteriaPanel.add(subCriteria);
			this.add(subcriteriaPanel);
		}
		
		// set visibility for group/non-group
		criteriaPanel.setVisible(!isGroup);
		subcriteriaPanel.setVisible(isGroup);
		
	}
	
	/**
	 * gets the current state of the criteria in this panel when editing stops
	 * @return criteria as it exists in this panel
	 */
	public Criteria getCriteria() {
		// main criteria options
		criteria.setSubject((OntologyClass) subject.getSelectedItem());
		criteria.setCondition((String) condition.getSelectedItem());
		criteria.setValue(value.getOntologyClass());
		criteria.setAll(anyAll.isSelected());
		
		// if there are subcriteria, add them
		if (subCriteria != null && subCriteria.getRowCount() > 0) {
			criteria.setSubCriteria(new ArrayList<Criteria>());
			for (Object obj: subCriteria.getListOfRowLists()) {
				List rowList = (List) obj;
				criteria.getSubCriteria().add((Criteria) rowList.get(0));
			}
		}
		return criteria;
	}
	
	/**
	 * sets the panel's widgets to reflect the value of the Criteria object
	 * @param criteria
	 */
	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
		
		//set visibility for what we are showing
		criteriaPanel.setVisible(!criteria.isGroup());
		subcriteriaPanel.setVisible(criteria.isGroup());
		
		// set the values in the UI
		subject.setSelectedItem(criteria.getSubject());
		condition.setSelectedItem(criteria.getCondition());
		this.value.setOntologyClass(criteria.getValue());
		
		// are there subcriteria to show?
		if (criteria.isGroup()) {
			anyAll.setSelected(criteria.isAll());
			subCriteria.removeAllRows();
			if (criteria.getSubCriteria() != null) {
				for (Criteria c: criteria.getSubCriteria()) {
					List rowList = new ArrayList();
					rowList.add(c);
					subCriteria.addRow(rowList);
				}
			}
		}
	}
	
}
