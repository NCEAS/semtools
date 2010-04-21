package org.ecoinformatics.sms.plugins.context;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Observation;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

public class ContextPanelList extends JPanel {
	
	private Annotation annotation;
	
	private Observation observation;
	
	private JPanel contextsPanel;
	
	private JPanel buttonPanel;
	private JButton addContextButton;
	
	
	public ContextPanelList(Annotation a) {
		super();
		
		this.annotation = a;
		
		// add the context widgets to the panel
		contextsPanel = WidgetFactory.makePanel();
		contextsPanel.setLayout(new BoxLayout(contextsPanel, BoxLayout.Y_AXIS));
		contextsPanel.setAlignmentY(TOP_ALIGNMENT);
		contextsPanel.removeAll();

		// context
		ActionListener addContextListener = new ListActionListener(ListActionListener.ADD_CONTEXT);
		addContextButton = WidgetFactory.makeJButton("+", addContextListener, ContextPanel.LIST_BUTTON_DIMS);
		addContextButton.setToolTipText("Add Context");

		buttonPanel = WidgetFactory.makePanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(addContextButton);
		
		JPanel optionPanel = WidgetFactory.makePanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		optionPanel.setAlignmentY(TOP_ALIGNMENT);
		optionPanel.add(buttonPanel);
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(optionPanel);
		
		JScrollPane contextScrollPane = new JScrollPane(contextsPanel);
		this.add(contextScrollPane);
		
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		addContextButton.setEnabled(enabled);
		// set each context panel
		if (contextsPanel != null && contextsPanel.getComponentCount() > 0) {
			for (Object obj: contextsPanel.getComponents()) {
				ContextPanel cp = (ContextPanel) obj;
				cp.setEnabled(enabled);
			}
		}
	}
	
	/**
	 * gets the current state of the observation in this panel when editing stops
	 * @return observation as it exists in this panel
	 */
	public Observation getObservation() {
				
		// if there are contexts, add them to the Observation
		if (contextsPanel != null && contextsPanel.getComponentCount() > 0) {
			observation.getContexts().clear();
			for (Object obj: contextsPanel.getComponents()) {
				ContextPanel cp = (ContextPanel) obj;
				Context c = cp.getContext();
				observation.addContext(c);
			}
		}
		return observation;
	}
	
	/**
	 * sets the panel's widgets to reflect the value of the Observation object
	 * @param observation
	 */
	public void setObservation(Observation o) {
		this.observation = o;
		
		// get the Observations it can provide context for
		List<Observation> observations = annotation.getObservations();
		// remove "this" one
		observations = new ArrayList<Observation>(observations);
		observations.remove(observation);
				
		contextsPanel.removeAll();
		if (observation != null && observation.getContexts() != null) {
			for (Context c: observation.getContexts()) {
				ContextPanel cp = new ContextPanel(true);
				cp.setObservation(observation);
				cp.setObservations(observations);
				cp.setContext(c);
				contextsPanel.add(cp);	
			}
		}
		// since it is in a scrollpane, we need to be more forceful with the refresh
		contextsPanel.repaint();
		contextsPanel.revalidate();
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
		ContextPanelList cpl = null;
		while (parent != null) {
			// get the list that is holding the criteria
			if (parent instanceof ContextPanelList) {
				cpl = (ContextPanelList) parent;
				break;
			}
			parent = parent.getParent();
		}
		// the parent observation we will be adding to
		Observation parentObservation = cpl.getObservation();		
		Context c = new Context();
		// add the context to the observation
		parentObservation.addContext(c);
		cpl.setObservation(parentObservation);
		
	}
	
}
