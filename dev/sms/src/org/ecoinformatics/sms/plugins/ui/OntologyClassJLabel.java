package org.ecoinformatics.sms.plugins.ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.ecoinformatics.sms.ontology.OntologyClass;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

public class OntologyClassJLabel extends JLabel {
	private OntologyClass ontologyClass;
	private OntologyClass filterClass;

	public OntologyClassJLabel(String text) {
		super(text);
		this.setIcon(new ImageIcon(this.getClass().getResource("search_icon.gif")));
		this.setHorizontalTextPosition(LEADING);
	}

	public void setOntologyClass(OntologyClass ontologyClass) {
		this.ontologyClass = ontologyClass;
		this.revalidate();
		this.repaint();
	}
	
	public OntologyClass getOntologyClass() {
		return this.ontologyClass;
	}

	public OntologyClass getFilterClass() {
		return filterClass;
	}

	public void setFilterClass(OntologyClass filterClass) {
		this.filterClass = filterClass;
		if (filterClass != null) {
			Border underline = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray);
			Border titleBorder = 
				BorderFactory.createTitledBorder(
						underline, 
						filterClass.getName(), 
						TitledBorder.CENTER, 
						TitledBorder.BELOW_BOTTOM);
			// TODO: figure out how to size it so the underline title is shown all the time
			//this.setBorder(titleBorder);
			this.setBorder(underline);
			this.setToolTipText(filterClass.getName());
		}
	}

	public String getText() {
		if (ontologyClass == null) {
			return "";
		}
		return ontologyClass.getName();
	}

	public static OntologyClassJLabel makeLabel(String text,
			boolean hiliteRequired, Dimension dims) {

		if (text == null) {
			text = "";
		}
		if (dims == null) {
			dims = WizardSettings.WIZARD_CONTENT_LABEL_DIMS;
		}
		OntologyClassJLabel label = new OntologyClassJLabel(text);

		//WidgetFactory.setPrefMaxSizes(label, dims);
		label.setPreferredSize(dims);
		label.setMinimumSize(dims);
		label.setAlignmentX(SwingConstants.LEADING);
		label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
		
		label.setBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
		if (hiliteRequired) {
			label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
		} else {
			label.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
		}

		return label;
	}

}
