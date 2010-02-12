package org.ecoinformatics.sms.plugins.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.pages.OntologyClassSelectionPage;

import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;

public class OntologyClassField extends JTextField {
	private OntologyClass ontologyClass;
	private OntologyClass filterClass;
	private OntologyClassSelectionPage page;
	//private JPopupMenu popup = null;
	private Popup popup = null;
	private boolean isPopupShowing = false;
	private boolean editing = false;

	public OntologyClassField(String text) {
		super(text);
		//this.setIcon(new ImageIcon(this.getClass().getResource("search_icon.gif")));
		//this.setHorizontalTextPosition(LEADING);
	}

	public void setOntologyClass(OntologyClass ontologyClass) {
		this.ontologyClass = ontologyClass;
		this.setText(ontologyClass.getName());
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
			//this.setBorder(underline);
			this.setToolTipText(filterClass.getName());
		}
	}

	public String getText() {
		if (editing || ontologyClass == null) {
			return super.getText();
		}
		return ontologyClass.getName();
	}

	/**
	 * @deprecated in favor of using the showPopupDialog() method
	 * @param source
	 */
	public static void showDialog(OntologyClassField source) {
		OntologyClassSelectionPage page = new OntologyClassSelectionPage();
		
		try {
			OntologyClass currentClass = source.getOntologyClass();
			OntologyClass filterClass = source.getFilterClass();
			if (currentClass != null) {
				page.setCurrentClass(currentClass);
			}
			page.setFilterClass(filterClass);
		} catch (Exception e) {
			//ignore
		}
		
		// show the dialog
		ModalDialog dialog = 
			new ModalDialog(
					page, 
					UIController.getInstance().getCurrentActiveWindow(), 
					UISettings.POPUPDIALOG_WIDTH,
					UISettings.POPUPDIALOG_HEIGHT);

		// get the response back
		if (dialog.USER_RESPONSE == ModalDialog.OK_OPTION) {
			String selectedClassString = null;
			if (page.getSelectedTerms() !=null && page.getSelectedTerms().size() > 0) {
				selectedClassString = page.getSelectedTerms().get(0);
			}
			OntologyClass selectedClass = null;
			try {
				selectedClass = new OntologyClass(selectedClassString);
			} catch (Exception e) {
				selectedClass = null;
				Log.debug(20, "error constructing selectedClass from string: " + selectedClassString);
			}
			source.setOntologyClass(selectedClass);
		}
		page = null;
	}

	public static OntologyClassField makeLabel(String text,
			boolean hiliteRequired, Dimension dims) {

		if (text == null) {
			text = "";
		}
		if (dims == null) {
			dims = WizardSettings.WIZARD_CONTENT_LABEL_DIMS;
		}
		OntologyClassField label = new OntologyClassField(text);

		//WidgetFactory.setPrefMaxSizes(label, dims);
		label.setPreferredSize(dims);
		label.setMinimumSize(dims);
		label.setMaximumSize(dims);
		label.setAlignmentX(SwingConstants.LEADING);
		label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
		
//		label.setBorder(
//				BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
		if (hiliteRequired) {
			label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
		} else {
			label.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
		}

		// listen to the click
		MouseListener mListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				OntologyClassField source = (OntologyClassField) e.getSource();
				if (source.isPopupShowing) {
					// the popup on the subsequent click
					source.syncSource();
					source.isPopupShowing = false;
					source.editing = false;
					source.popup.hide();
				} else {
					OntologyClassField.showPopupDialog(source);
				}
			}

		};
		label.addMouseListener(mListener);
		
		// listen to the keys
		KeyListener kListener = new KeyListener() {
			private void doKey(KeyEvent e) {
				OntologyClassField source = (OntologyClassField) e.getSource();
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					source.syncSource();
					source.isPopupShowing = false;
					source.editing = false;
					source.popup.hide();
				}

				String searchTerm = source.getText();
				source.page.getSelectionPanel().doSearch(searchTerm);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				//doKey(e);
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				doKey(e);
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyTyped(KeyEvent e) {
				//doKey(e);
				
			}

		};
		label.addKeyListener(kListener);
		return label;
	}
	
	private void syncSource() {
		// get the response back
		String selectedClassString = null;
		if (this.page.getSelectedTerms() !=null && this.page.getSelectedTerms().size() > 0) {
			selectedClassString = this.page.getSelectedTerms().get(0);
		}
		OntologyClass selectedClass = null;
		try {
			selectedClass = new OntologyClass(selectedClassString);
		} catch (Exception ex) {
			selectedClass = null;
			Log.debug(20, "error constructing selectedClass from string: " + selectedClassString);
		}
		this.setOntologyClass(selectedClass);
	}
	
	public static void showPopupDialog(OntologyClassField source) {
		OntologyClassSelectionPage page = new OntologyClassSelectionPage();
		
		try {
			OntologyClass currentClass = source.getOntologyClass();
			OntologyClass filterClass = source.getFilterClass();
			if (currentClass != null) {
				page.setCurrentClass(currentClass);
			}
			page.setFilterClass(filterClass);
			page.onLoadAction();
		} catch (Exception e) {
			//ignore
		}
		
		//clear out the text area
		source.editing = true;
		
		int x = source.getLocationOnScreen().x;
        int y = source.getLocationOnScreen().y + source.getSize().height;
       
		Popup popup = PopupFactory.getSharedInstance().getPopup(source, page.getSelectionPanel(), x, y);
		popup.show();
		
//		int x = 0;
//        int y = source.getSize().height;
//        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
//        JPopupMenu popup = new JPopupMenu();
//        popup.add(page.getSelectionPanel());
//        popup.show(source, x, y);
		
		// set the popup
		source.popup = popup;
		source.page = page;
		source.isPopupShowing = true;

		
	}


}
