package org.ecoinformatics.sms.plugins.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.renderer.OntologyClassSelectionPanel;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;

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
	
	public static final Dimension DEFAULT_DIMS = new Dimension(150,20);
	
	public static final DataFlavor ontologyClassFlavor = new DataFlavor(OntologyClass.class, null);
	
	private static Point initialPosition = null;
	
	private static Point lastPosition = null;
	
	private static JDialog popup = null;
	
	private static OntologyClassField currentField;
	
	private OntologyClass ontologyClass;
	private OntologyClass filterClass;
	private OntologyClassSelectionPanel selectionPanel;
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
			// set the label border if we have it wrapped
			Container parent = this.getParent();
			if (parent instanceof OntologyClassFieldPanel) {
				setPanelBorder((OntologyClassFieldPanel) parent, filterClass.getName(), TitledBorder.ABOVE_BOTTOM);
			}
		}
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		// make sure we are not highlighted
		if (currentField != null) {
			if (enabled) {
				currentField.highlight();
			}
			else {
				currentField.unhighlight();
			}
		}
	}
	
	public String getText() {
		if (editing || ontologyClass == null) {
			return super.getText();
		}
		return ontologyClass.getName();
	}
	
	public void unhighlight() {
			this.setBorder(new JTextField().getBorder());
	}
	
	public void highlight() {
		setBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(Color.BLUE, 2), 
					new JTextField().getBorder()));
	}

	public static JPanel wrapField(OntologyClassField field) {
		OntologyClassFieldPanel panel = new OntologyClassFieldPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(field);
		if (field.getFilterClass() != null) {
			setPanelBorder(panel, field.getFilterClass().getName(), TitledBorder.ABOVE_BOTTOM);
		}
		return panel;
	}
	
	public static JPanel wrapField(OntologyClassField field, String label) {
		OntologyClassFieldPanel panel = new OntologyClassFieldPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(field);
		if (field.getFilterClass() != null) {
			setPanelBorder(panel, label, TitledBorder.ABOVE_TOP);
		}
		return panel;	
	}
	
	public static void setPanelBorder(OntologyClassFieldPanel panel, String title, int location) {
		Font titleFont = new Font("Sans-Serif", Font.PLAIN, 8);
		Border existingBorder = BorderFactory.createEmptyBorder();
		panel.setBorder(
				BorderFactory.createTitledBorder(
						existingBorder, 
						title, 
						TitledBorder.CENTER, 
						location, 
						titleFont, 
						WizardSettings.WIZARD_CONTENT_TEXT_COLOR));
	}
	
	public static OntologyClassField makeLabel(String text,
			boolean hiliteRequired, Dimension dims) {

		if (text == null) {
			text = "";
		}
		if (dims == null) {
			dims = DEFAULT_DIMS;
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
				Log.debug(40, "mouseClicked, editing: "  + source.editing);
					// show dialog
					OntologyClassField.showPopupDialog(source);
			}
		};
		label.addMouseListener(mListener);
		
		// listen to the key events
		KeyListener kListener = new KeyAdapter() {
			private void doKey(KeyEvent e) {
				OntologyClassField source = (OntologyClassField) e.getSource();
				
				// start off with the popup if not already showing
				if (!source.editing) {
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
					if (source.editing) {
						source.stopEditing(false, false);
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
		
		// focus listener - probably not needed now
		FocusListener fListener = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
				OntologyClassField source = (OntologyClassField) e.getSource();
				Log.debug(40, "focus lost, editing=" + source.editing);

				// check what gained the focus
				Component gained = e.getOppositeComponent();
				if (!(gained instanceof OntologyClassField)) {
					// ignore unless switching to another field
					return;
				}
				
				// save and leave 
				if (source.editing) {
					source.stopEditing(true, false);
					return;
				}
            }
            
		};
		//label.addFocusListener(fListener);
		
		// handle drop events for OntologyClass objects that are dragged into the field
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
	 * closes the popup, optionally saving the value that was selected
	 * @param save
	 */
	private void stopEditing(boolean save, boolean close) {
		if (save) {
			syncSource();
		}
		//editing = false;
		// did someone move the popup?
		Point currentPosition = popup.getLocation();
		if (currentPosition.equals(initialPosition)) {
			lastPosition = null;
		} else {
			lastPosition = currentPosition;
		}
		if (close) {
			popup.setVisible(false);
			selectionPanel = null;
			// make sure we are not highlighted
			unhighlight();
		}
	}
	
	/**
	 * Gets the selected term from the popup and sets the ontology class accordingly
	 * Also notifies the field listeners that the value has been set
	 */
	private void syncSource() {
		if (selectionPanel == null) {
			return;
		}
		if (!isEnabled()) {
			return;
		}
		// get the response back
		OntologyClass selectedClass = null;
		if (selectionPanel.getOntologyClasses() !=null && selectionPanel.getOntologyClasses().size() > 0) {
			selectedClass = selectionPanel.getOntologyClasses().get(0);
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
	public static void showPopupDialog(final OntologyClassField source) {
		
		if (!source.isEnabled()) {
			return;
		}

		OntologyClassSelectionPanel selectionPanel = null;
		selectionPanel = new OntologyClassSelectionPanel(true);
		 // set the size of the contents so it's not too large
		selectionPanel.setPreferredSize(new Dimension(400, 300));
       
		if (popup == null) {
	        
	        // get the owning frame for this field, or null if it is in a non-modal dialog
	        Frame owner = null; //UIController.getInstance().getCurrentActiveWindow();
	        Component parent = source;
	        while (parent != null) {
	        	parent = parent.getParent();
	        	if (parent instanceof Frame) {
	        		owner = (Frame) parent;
	        		break;
	        	}
	        	if (parent instanceof JDialog) {
	        		owner = null;
	        		break;
	        	}
	        }
	        
	        // make the popup
	        popup = new JDialog(owner, "Ontology Browser", false);
			
			int x = source.getLocationOnScreen().x;
	        int y = source.getLocationOnScreen().y + source.getSize().height;
	        
			initialPosition = new Point(x,y);
			if (lastPosition != null) {
				popup.setLocation(lastPosition);
			} else {
				popup.setLocation(initialPosition);
			}
			// add window close listener
			popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			final OntologyClassField sourceRef = source;
			popup.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					sourceRef.stopEditing(false, true);
				}
			});
			
		}
		
		// set the contents
		try {
			OntologyClass currentClass = source.getOntologyClass();
			OntologyClass filterClass = source.getFilterClass();
			
			selectionPanel.initialize(filterClass, null);
			if (currentClass != null) {
				selectionPanel.doSelect(currentClass);
			}
			selectionPanel.validate();
			
			// listen for double click on the tree
			MouseListener doubleClickTreeListener = source.new TreeMouseListener(source);
			selectionPanel.setTreeMouseListener(doubleClickTreeListener);
		} catch (Exception e) {
			//ignore
		}
		
		// begin editing on this field
		source.editing = true;
		source.selectionPanel = selectionPanel;
		
		// highlight the field
		if (currentField != null) {
			currentField.unhighlight();
		}
		source.highlight();
		currentField = source;
		
		// the select button
		boolean showSelect = true;
		if (showSelect) {
			AbstractAction selectAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					source.stopEditing(true, false);
				}
			};
			JButton selectButton = WidgetFactory.makeJButton("Select", selectAction);
			JPanel containerPanel = WidgetFactory.makePanel();
			containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
			containerPanel.add(selectionPanel);
			JPanel buttonPanel = WidgetFactory.makePanel();
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.add(selectButton);
			containerPanel.add(buttonPanel);
			popup.setContentPane(containerPanel);
		} else {
			popup.setContentPane(selectionPanel);
		}
		
		popup.pack();
		popup.setVisible(true);
		popup.toFront();
		
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
				if (label.editing) {
					label.stopEditing(true, false);
				}
			}
		}
	}
}
// for ID purposes
class OntologyClassFieldPanel extends JPanel {
	
}
