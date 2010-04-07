package org.ecoinformatics.sms.plugins.search;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.ui.ContextTriplePanel;
import org.ecoinformatics.sms.plugins.ui.OntologyClassField;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

public class CriteriaPanel extends JPanel {

	public static final Dimension LIST_BUTTON_DIMS = new Dimension(55, 30);
	public static final  Dimension PICKLIST_DIMS = new Dimension(100,30);
	
	private Criteria criteria;
	
	private JPanel criteriaPanel;
	private JComboBox subject;
	private JComboBox condition;
	private OntologyClassField value;
	
	private JPanel subcriteriaPanel;
	private CriteriaPanelList subCriteria;
	
	private ContextTriplePanel contextPanel;
	
	private JPanel buttonPanel;
	private JButton removeButton;
	
	public CriteriaPanel(Criteria c) {
		super();
		
		// change the filter class based on what is selected 
		ItemListener subjectListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox source = (JComboBox) e.getSource();
				value.setFilterClass((OntologyClass) source.getSelectedItem());
			}
		};
		// use the mapped OntologyClasses for the drop down
		Object[] subjectValues = new OntologyClass[] {
				Annotation.OBOE_CLASSES.get(Entity.class), 
				Annotation.OBOE_CLASSES.get(Characteristic.class), 
				Annotation.OBOE_CLASSES.get(Standard.class), 
				Annotation.OBOE_CLASSES.get(Protocol.class)};
		subject = WidgetFactory.makePickList(subjectValues, false, 0, subjectListener);
		WidgetFactory.setPrefMaxSizes(subject, PICKLIST_DIMS);
		
		Object[] conditionValues = new String[] {"is", "is not"};
		condition = WidgetFactory.makePickList(conditionValues, false, 0, null);
		WidgetFactory.setPrefMaxSizes(condition, PICKLIST_DIMS);

		value = OntologyClassField.makeLabel("", false, null);
		value.setFilterClass((OntologyClass) subject.getSelectedItem());
		WidgetFactory.setPrefMaxSizes(value, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		
		// make the panel
		criteriaPanel = WidgetFactory.makePanel();
		//criteriaPanel.setLayout(new GridLayout(1,3));
		//criteriaPanel.setLayout(new BoxLayout(criteriaPanel, BoxLayout.X_AXIS));

		criteriaPanel.add(subject);
		criteriaPanel.add(condition);
		criteriaPanel.add(value);
		
		WidgetFactory.setPrefMaxSizes(criteriaPanel, new Dimension(350, WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.height + 10));
		
		//criteriaPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.yellow));
		
		// context triple
		contextPanel = new ContextTriplePanel();
		
		subcriteriaPanel = WidgetFactory.makePanel();
		// only create the subcriteria list if this is a grouping criteria (infinite loop possible otherwise)
		if (c.isGroup()) {
			subCriteria = new CriteriaPanelList(c);
			
			// add the subcriteria widgets to the panel
			subcriteriaPanel.setLayout(new BoxLayout(subcriteriaPanel, BoxLayout.Y_AXIS));
			subcriteriaPanel.add(subCriteria);
		}
		
		
		// remove button
		ActionListener removeListener = new PanelActionListener(PanelActionListener.REMOVE);
		removeButton = WidgetFactory.makeJButton("-", removeListener, LIST_BUTTON_DIMS);
		
		buttonPanel = WidgetFactory.makePanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(removeButton);
		
		//buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.blue));
		
		// group the content panel
		JPanel contentPanel = WidgetFactory.makePanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.add(criteriaPanel);
		contentPanel.add(contextPanel);
		contentPanel.add(subcriteriaPanel);
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		//this.setLayout(new GridLayout(1,2));
		this.add(contentPanel);
		this.add(buttonPanel);
		this.add(Box.createHorizontalGlue());
		
		//this.setMaximumSize(new Dimension(300,100));
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.gray));
		
		// set visibility for group/non-group
		setCriteria(c);
		
	}
	
	/**
	 * gets the current state of the criteria in this panel when editing stops
	 * @return criteria as it exists in this panel
	 */
	public Criteria getCriteria() {
		
		// get the criteria as it exists from the panel of lists
		if (subCriteria != null) {
			this.criteria = subCriteria.getCriteria();
		}
		
		// find the Java class we want for the selected OntologyClass - it is the key in the map
		OntologyClass selectedSubject = (OntologyClass) subject.getSelectedItem();
		Iterator<Entry<Class, OntologyClass>> subjectIter = Annotation.OBOE_CLASSES.entrySet().iterator();
		while (subjectIter.hasNext()) {
			Entry<Class, OntologyClass> entry = subjectIter.next();
			if (entry.getValue().equals(selectedSubject)) {
				criteria.setSubject(entry.getKey());
				break;
			}
		}
		
		criteria.setCondition((String) condition.getSelectedItem());
		criteria.setValue(value.getOntologyClass());
		//criteria.setAll(anyAll.isSelected());
		
		return criteria;
	}
	
	/**
	 * sets the panel's widgets to reflect the value of the Criteria object
	 * @param criteria
	 */
	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
		
		// set the values in the UI
		subject.setSelectedItem(Annotation.OBOE_CLASSES.get(criteria.getSubject()));
		condition.setSelectedItem(criteria.getCondition());
		this.value.setOntologyClass(criteria.getValue());
		
		// are there subcriteria to show?
		if (criteria.isGroup()) {
			subCriteria.setCriteria(criteria);
		}
		
		calculateVisibility();
		
	}
	
	public void calculateVisibility() {
		//set visibility for what we are showing
		criteriaPanel.setVisible(!criteria.isGroup());
		criteriaPanel.setVisible(!(criteria.isContext() || criteria.isGroup()));
		contextPanel.setVisible(criteria.isContext());
		subcriteriaPanel.setVisible(criteria.isGroup());
		
		// show buttons?
		boolean showButtons = (criteria.getSubCriteria() == null || criteria.getSubCriteria().size() == 0);
		//buttonPanel.setVisible(showButtons);
	}
}
class PanelActionListener implements ActionListener {
	
	private int mode;
	
	static final int ADD = 0;
	static final int REMOVE = 1;
	static final int ADD_GROUP = 2;
	static final int ADD_CONTEXT = 3;

	public PanelActionListener(int mode) {
		this.mode = mode;
	}
	
	public void actionPerformed(ActionEvent e) {
		switch (this.mode) {
		case ADD:
			doAdd(e);
			break;
		case ADD_GROUP:
			doAdd(e);
			break;
		case ADD_CONTEXT:
			doAdd(e);
			break;
		case REMOVE:
			doRemove(e);
			break;
		// do add handles them all
		default:
			doAdd(e);
			break;
		}
	}
	public void doAdd(ActionEvent e) {
		// get the parent list to add to
		JButton source = (JButton) e.getSource();
		Container parent = source.getParent();
		CriteriaPanelList cpl = null;
		CriteriaPanel cp = null;

		while (parent != null) {
			// get the actual panel holding this criteria
			if (parent instanceof CriteriaPanel) {
				cp = (CriteriaPanel) parent;
			}
			// get the list that is holding the criteria
			if (parent instanceof CriteriaPanelList) {
				cpl = (CriteriaPanelList) parent;
				break;
			}
			parent = parent.getParent();
		}
		// the parent criteria we will be adding to
		Criteria parentCriteria = null;				
		// the criteria for this panel that is being clicked
		Criteria criteria = cp.getCriteria();
		if (criteria.isGroup()) {
			parentCriteria = criteria;
		} else {
			// get the list's criteria, to add to the sibling subcriteria
			parentCriteria = cpl.getCriteria();		
		}
		List<Criteria> sc = parentCriteria.getSubCriteria();
		if (sc == null) {
			sc = new ArrayList<Criteria>();
		}
		Criteria c = new Criteria();
		c.setGroup(mode == ADD_GROUP);
		c.setContext(mode == ADD_CONTEXT);
		sc.add(c);
		// set the subcriteria and the list's criteria
		parentCriteria.setSubCriteria(sc);
		if (criteria.isGroup()) {
			cp.setCriteria(parentCriteria);
		} else {
			cpl.setCriteria(parentCriteria);
		}
	}
	
	
	public void doRemove(ActionEvent e) {
		JButton source = (JButton) e.getSource();
		Container parent = source.getParent();
		CriteriaPanelList cpl = null;
		CriteriaPanel cp = null;

		while (parent != null) {
			// get the actual panel we want to remove
			if (parent instanceof CriteriaPanel) {
				cp = (CriteriaPanel) parent;
			}
			// get the list to remove it from
			if (parent instanceof CriteriaPanelList) {
				cpl = (CriteriaPanelList) parent;
				break;
			}
			parent = parent.getParent();
		}
		// get the list's criteria, and remove ourselves from the subcriteria
		Criteria parentCriteria = cpl.getCriteria();
		Criteria criteria = cp.getCriteria();
		parentCriteria.getSubCriteria().remove(criteria);
		cpl.setCriteria(parentCriteria);
	}
}
