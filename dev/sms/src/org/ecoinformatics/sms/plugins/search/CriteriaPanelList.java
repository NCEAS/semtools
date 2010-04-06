package org.ecoinformatics.sms.plugins.search;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.ecoinformatics.sms.annotation.search.Criteria;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

public class CriteriaPanelList extends JPanel {
	
	private Criteria criteria;
	
	private JPanel subcriteriaPanel;
	private JCheckBox anyAll;
	
	
	public CriteriaPanelList(Criteria c) {
		super();
						
		this.criteria = c;
		
		// any all checkbox
		anyAll = WidgetFactory.makeCheckBox("Match All", false);

		// add the subcriteria widgets to the panel
		subcriteriaPanel = WidgetFactory.makePanel();
		subcriteriaPanel.setLayout(new BoxLayout(subcriteriaPanel, BoxLayout.Y_AXIS));
		subcriteriaPanel.removeAll();
		if (criteria.getSubCriteria() != null) {
			for (Criteria sc: criteria.getSubCriteria()) {
				CriteriaPanel cp = new CriteriaPanel(sc);
				subcriteriaPanel.add(cp);	
			}
		}
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(anyAll);
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
