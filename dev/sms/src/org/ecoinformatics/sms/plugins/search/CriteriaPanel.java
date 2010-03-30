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
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.ui.OntologyClassField;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

public class CriteriaPanel extends JPanel {

	private Criteria criteria;
	
	private JPanel criteriaPanel;
	private JPanel subcriteriaPanel;
	private JComboBox subject;
	private JComboBox condition;
	private OntologyClassField value;
	
	private JCheckBox anyAll;
	private CustomList subCriteria;
	
	public CriteriaPanel(Criteria criteria) {
		super();
		
		this.criteria = criteria;
		
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

		if (criteria.isGroup()) {
			
			anyAll = WidgetFactory.makeCheckBox("Match All", false);
			String[] colNames = new String[] {"Subcriteria"};
			Object[] editors = new Object[] {new CriteriaRenderer(0) };
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
			this.add(subcriteriaPanel);
			
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
		
		//set visibility
		criteriaPanel.setVisible(!criteria.isGroup());
		subcriteriaPanel.setVisible(criteria.isGroup());
		
		subject.setSelectedItem(criteria.getSubject());
		condition.setSelectedItem(criteria.getCondition());
		this.value.setOntologyClass(criteria.getValue());

	}
	
}
