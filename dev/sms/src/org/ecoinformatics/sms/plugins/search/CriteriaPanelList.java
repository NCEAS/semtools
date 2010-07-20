package org.ecoinformatics.sms.plugins.search;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.ecoinformatics.sms.annotation.search.Criteria;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

public class CriteriaPanelList extends JPanel {
	
	private Criteria criteria;
	
	private JPanel subcriteriaPanel;
	private JCheckBox anyAll;
	private JCheckBox same;
	
	private JPanel buttonPanel;
	private JButton addButton;
	private JButton addGroupButton;
	private JButton addContextButton;
	
	
	public CriteriaPanelList(Criteria c) {
		super();
						
		this.criteria = c;
		
		// any all checkbox
		anyAll = WidgetFactory.makeCheckBox("Match All", false);
		// same observation
		same = WidgetFactory.makeCheckBox("Same Obsevation", false);

		// add the subcriteria widgets to the panel
		subcriteriaPanel = WidgetFactory.makePanel();
		subcriteriaPanel.setLayout(new BoxLayout(subcriteriaPanel, BoxLayout.Y_AXIS));
		subcriteriaPanel.setAlignmentY(TOP_ALIGNMENT);
		subcriteriaPanel.removeAll();
		if (criteria.getSubCriteria() != null) {
			for (Criteria sc: criteria.getSubCriteria()) {
				CriteriaPanel cp = new CriteriaPanel(sc);
				subcriteriaPanel.add(cp);	
			}
		}
		
		// add
		ActionListener addListener = new ListActionListener(ListActionListener.ADD);
		addButton = WidgetFactory.makeJButton("+", addListener, CriteriaPanel.LIST_BUTTON_DIMS);
		addButton.setToolTipText("Add Criteria");
		// group
		ActionListener addGroupListener = new ListActionListener(ListActionListener.ADD_GROUP);
		addGroupButton = WidgetFactory.makeJButton("[+]", addGroupListener, CriteriaPanel.LIST_BUTTON_DIMS);
		addGroupButton.setToolTipText("Add Group");

		// context
		ActionListener addContextListener = new ListActionListener(ListActionListener.ADD_CONTEXT);
		addContextButton = WidgetFactory.makeJButton("<+>", addContextListener, CriteriaPanel.LIST_BUTTON_DIMS);
		addContextButton.setToolTipText("Add Context");

		buttonPanel = WidgetFactory.makePanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(addButton);
		buttonPanel.add(addContextButton);
		buttonPanel.add(addGroupButton);
		
		JPanel optionPanel = WidgetFactory.makePanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		optionPanel.setAlignmentY(TOP_ALIGNMENT);
		optionPanel.add(anyAll);
		optionPanel.add(same);
		optionPanel.add(buttonPanel);
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(optionPanel);
		this.add(subcriteriaPanel);
		//this.add(Box.createHorizontalGlue());
		
		this.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.gray));
		
	}
	
	/**
	 * gets the current state of the criteria in this panel when editing stops
	 * @return criteria as it exists in this panel
	 */
	public Criteria getCriteria() {
		
		criteria.setAll(anyAll.isSelected());
		criteria.setSame(same.isSelected());
		
		// if there are subcriteria, add them to the list
		if (subcriteriaPanel != null && subcriteriaPanel.getComponentCount() > 0) {
			criteria.setSubCriteria(new ArrayList<Criteria>());
			for (Object obj: subcriteriaPanel.getComponents()) {
				CriteriaPanel cp = (CriteriaPanel) obj;
				Criteria c = cp.getCriteria();
				criteria.getSubCriteria().add(c);
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
		
		anyAll.setSelected(criteria.isAll());
		same.setSelected(criteria.isSame());
		
		subcriteriaPanel.removeAll();
		if (criteria.getSubCriteria() != null) {
			for (Criteria sc: criteria.getSubCriteria()) {
				CriteriaPanel cp = new CriteriaPanel(sc);
				subcriteriaPanel.add(cp);	
			}
		}
		
		subcriteriaPanel.revalidate();
	}
	
}
class ListActionListener implements ActionListener {
	
	private int mode;
	
	static final int ADD = 0;
	static final int ADD_GROUP = 2;
	static final int ADD_CONTEXT = 3;

	public ListActionListener(int mode) {
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
		while (parent != null) {
			// get the list that is holding the criteria
			if (parent instanceof CriteriaPanelList) {
				cpl = (CriteriaPanelList) parent;
				break;
			}
			parent = parent.getParent();
		}
		// the parent criteria we will be adding to
		Criteria parentCriteria = cpl.getCriteria();		
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
		cpl.setCriteria(parentCriteria);
		
	}
	
}
