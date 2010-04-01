package org.ecoinformatics.sms.plugins.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.pages.OntologyClassSelectionPage;
import org.ecoinformatics.sms.renderer.OntologyClassSelectionPanel;

import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;

/**
 * An extension of a textfield, this widget pops up an ontology browser that is used to select a class
 * Typing in the text field starts a search based on the text currently entered.
 * Clicking in the text field opens the popup
 * Clicking again in the text field closes the popup and uses the selected (if any) class as the value
 * Pressing ESC when the popup is showing closes the popup and does not set a value
 * Tabbing or changing focus to another field uses the currently selected class from the popup and closes the popup
 * @author leinfelder
 *
 */
public class OntologyClassField extends JTextField {
	
	public static final DataFlavor ontologyClassFlavor = new DataFlavor(OntologyClass.class, null);
	
	private OntologyClass ontologyClass;
	private OntologyClass filterClass;
	private OntologyClassSelectionPanel selectionPanel;
	private Popup popup = null;
	private boolean isPopupShowing = false;
	private boolean editing = false;

	public OntologyClassField(String text) {
		super(text);
	}

	/**
	 * sets the ontology class selected for this widget
	 * @param ontologyClass the class that has been selected (from the popup or drag event)
	 */
	public void setOntologyClass(OntologyClass ontologyClass) {
		this.ontologyClass = ontologyClass;
		if (ontologyClass != null) {
			this.setText(ontologyClass.getName());
			this.setToolTipText(ontologyClass.getName());
		}
		else {
			this.setText("");
			if (filterClass != null) {
				this.setToolTipText(filterClass.getName());
			}
		}
		this.setCaretPosition(0);
		this.revalidate();
		this.repaint();
	}
	
	public OntologyClass getOntologyClass() {
		return this.ontologyClass;
	}

	public OntologyClass getFilterClass() {
		return filterClass;
	}

	/**
	 * sets the filter class for the popup - only subclasses of the filter class are shown
	 * @param filterClass the superclass of the subclasses that should be shown in the popup
	 */
	public void setFilterClass(OntologyClass filterClass) {
		this.filterClass = filterClass;
		if (filterClass != null) {
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
		
		if (hiliteRequired) {
			label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
		} else {
			label.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
		}

		// listen to the click
		MouseListener mListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				OntologyClassField source = (OntologyClassField) e.getSource();
				Log.debug(40, "mouseClicked, dialogIsShowing: "  + source.isPopupShowing);
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
		KeyListener kListener = new KeyAdapter() {
			private void doKey(KeyEvent e) {
				OntologyClassField source = (OntologyClassField) e.getSource();
				
				// start off with the popup if not already showing
				if (!source.isPopupShowing) {
					showPopupDialog(source);
				}

				// search, or continue to search with the text provided
				String searchTerm = source.getText();
				source.selectionPanel.doFilterSearch(searchTerm);
			}

			public void keyReleased(KeyEvent e) {
				OntologyClassField source = (OntologyClassField) e.getSource();
				// get out of here!
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					if (source.isPopupShowing) {
						source.isPopupShowing = false;
						source.editing = false;
						source.popup.hide();
						return;
					}
				}			
			}
			public void keyTyped(KeyEvent e) {
				// only do this for keys that have characters
				doKey(e);				
			}

		};
		label.addKeyListener(kListener);
		
		FocusListener fListener = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
				OntologyClassField source = (OntologyClassField) e.getSource();
				Log.debug(40, "focusLost, source.isPopupShowing=" + source.isPopupShowing);

				// check what gained the focus
				Component gained = e.getOppositeComponent();
				if (!(gained instanceof OntologyClassField)) {
					// ignore unless switching to another field
					return;
				}
				
				// save and leave 
				if (source.isPopupShowing) {
					source.syncSource();
					source.isPopupShowing = false;
					source.editing = false;
					source.popup.hide();
					return;
				}
            }
            
		};
		label.addFocusListener(fListener);
		
		// handle drop events for OntologyClass objects that are dagged into the field
		TransferHandler dropHandler = new TransferHandler() {
			public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
				//TODO: check flavors
				if (comp instanceof OntologyClassField) {
					return true;
				}
				return false;
			}
			public boolean importData(JComponent comp, Transferable t) {
                Log.debug(30, "importing: " + t);

                // get the drop target
				OntologyClassField source = (OntologyClassField) comp;

				// is it enabled
				if (!source.isEnabled()) {
					return false;
				}
				
                // Get the OntologyClass that is being dropped.
                OntologyClass data;
                try {
                    data = (OntologyClass) t.getTransferData(ontologyClassFlavor);
                    // check that the dropped OntologyClass is a subclass of the filter class, if not the actual class even
                    if (!data.equals(source.getFilterClass())) {
	                    boolean isSubclass = SMS.getInstance().getOntologyManager().isSubClass(data, source.getFilterClass(), true);
	                    if (!isSubclass) {
	                    	return false;
	                    }
                    }
                } 
                catch (Exception e) {
                	return false;
                }
                
                // set the class that was dropped
                source.setOntologyClass(data);
			
                return true;
            }
            
		};
		label.setTransferHandler(dropHandler);
		
		return label;
	}
	
	/**
	 * Gets the selected term from the popup and sets the ontology class accordingly
	 * Also notifies the field listeners that the value has been set
	 */
	private void syncSource() {
		if (this.selectionPanel == null) {
			return;
		}
		// get the response back
		OntologyClass selectedClass = null;
		if (this.selectionPanel.getOntologyClasses() !=null && this.selectionPanel.getOntologyClasses().size() > 0) {
			selectedClass = this.selectionPanel.getOntologyClasses().get(0);
		}
		this.setOntologyClass(selectedClass);
		this.notifyListeners();
	}
	
	/**
	 * Notifies any listeners that the value has been set so that they can react to this event
	 * Typically used when the value of one field affects possible values of another
	 */
	private void notifyListeners() {
		for (ActionListener al: getActionListeners()) {
			ActionEvent e = new ActionEvent(this, 1, "value set");
			al.actionPerformed(e);
		}
	}
	
	/**
	 * Constructs and shows the popup ontology browser tree
	 * @param source the textfield that the popup is "editing"
	 */
	public static void showPopupDialog(OntologyClassField source) {
		OntologyClassSelectionPanel selectionPanel = new OntologyClassSelectionPanel(false);
		
		if (!source.isEnabled()) {
			return;
		}
		
		try {
			OntologyClass currentClass = source.getOntologyClass();
			OntologyClass filterClass = source.getFilterClass();
			
			selectionPanel.initialize(filterClass, null);
			if (currentClass != null) {
				selectionPanel.doSelect(currentClass);
			}
		} catch (Exception e) {
			//ignore
		}
		
		//clear out the text area
		source.editing = true;
		
		int x = source.getLocationOnScreen().x;
        int y = source.getLocationOnScreen().y + source.getSize().height;

        // set the size of the contents so it's not too large
        selectionPanel.setPreferredSize(new Dimension(400, 300));

		Popup popup = PopupFactory.getSharedInstance().getPopup(source, selectionPanel, x, y);
		popup.show();
		
		// set the popup
		source.popup = popup;
		source.selectionPanel = selectionPanel;
		source.isPopupShowing = true;

		// listen for double click on the tree
		MouseListener doubleClickTreeListener = source.new TreeMouseListener(source);
		source.selectionPanel.setTreeMouseListener(doubleClickTreeListener);
		
	}
	
	/**
	 * Simple mouse listener for catching double clicks on the pop up tree
	 * A double click will select the class, set the class in the field and close the popup
	 * @author leinfelder
	 *
	 */
	class TreeMouseListener extends MouseAdapter {
		private OntologyClassField label;
		public TreeMouseListener(OntologyClassField label) {
			this.label = label;
		}
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() > 1) {
				if (label.isPopupShowing) {
					label.syncSource();
					label.isPopupShowing = false;
					label.editing = false;
					label.popup.hide();
				}
			}
		}
	}


}
