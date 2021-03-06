/**
 *    '$Id$'
 *
 *     '$Author$'
 *       '$Date$'
 *   '$Revision$'
 *
 *  For Details: http://kepler-project.org
 *
 * Copyright (c) 2005 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */

package org.ecoinformatics.sms.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.ontology.OntologyProperty;
import org.ecoinformatics.sms.plugins.table.OntologyCellRenderer;
import org.ecoinformatics.sms.plugins.ui.OntologyClassField;
import org.ecoinformatics.sms.renderer.treetable.OntologyTreeCellRenderer;
import org.ecoinformatics.sms.renderer.treetable.OntologyTreeModel;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.JTreeTable;
import edu.ucsb.nceas.morpho.util.Log;


/**
 * This class implements a simple panel for selecting classes from a tree widget
 * and adding them to a target list. The panel uses drag and drop events,
 * buttons, and right-click menu items for adding and removing classes.
 * Right-click events also are used to navigate properties. This panel also
 * provides a simple term search mechanism.
 * 
 * @author Shawn Bowers
 */
public class OntologyClassSelectionPanel extends JPanel {

	private JScrollPane treeView;
	private OntologyClass filterClass;
	private boolean firstSearch = true;
	private boolean showSearch = true;


	/**
	 * Default constructor that initializes the panel, accepting all ontologies
	 * and having a default width and height.
	 */
	public OntologyClassSelectionPanel(boolean showSearch) {
		this.showSearch = showSearch;
	}

	/**
	 * Provides access to the selected ontology classes
	 * 
	 * @return the current set of selections in the panel
	 */
	public List<OntologyClass> getOntologyClasses() {
		List<OntologyClass> returnList = new ArrayList<OntologyClass>();
		int[] rows = _ontoTree.getSelectedRows();
		if (rows != null) {
			for (int i = 0; i < rows.length; i++) {
				Object obj = _ontoTree.getValueAt(rows[i], 0);
				
				if (obj instanceof OntologyClass) {
					OntologyClass cls = (OntologyClass) obj;
					returnList.add(cls);
				}
			}
		}
		return returnList;
	}
	
	/** add our own listener, disables the double click expansion **/
	public void setTreeMouseListener(MouseListener listener) {
		this.treeMouseListener = listener;
		_ontoTree.getTree().addMouseListener(treeMouseListener);
	}

	public void initialize(OntologyClass filterClass, Dimension dim) {
		this.filterClass = filterClass;
		int width = 525;
		int height = 350;
		if (dim != null) {
			width = dim.width;
			height = dim.height;
		}
		init(width, height);
	}

	// PRIVATE METHODS

	/**
	 * Private method for initializing the panel
	 */
	private void init(int width, int height) {
		

		// make the scroll pane for the tree
		treeView = 
			new JScrollPane(
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// generate the tree
		createTreeView(filterClass);

		_ontoTree.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

		// the description text area
		_commentTxt = new JTextArea();
		_commentTxt.setEditable(false);
		_commentTxt.setLineWrap(true);
		_commentTxt.setWrapStyleWord(true);
		_commentTxt.setEnabled(false);
		JScrollPane commentView = new JScrollPane(_commentTxt,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//commentView.setMaximumSize(new Dimension(width, 150));
		commentView.setPreferredSize(new Dimension(width, 150));

		// onto tree label
		JPanel panel1 = new JPanel();
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
		panel1.add(new JLabel("All Classes:", SwingConstants.LEFT));
		panel1.add(Box.createHorizontalGlue());
		
		// set up search button
		_searchBtn = WidgetFactory.makeJButton("Search", null);
		_searchTxt = new JTextField(14);
		_searchBtn.addActionListener(new ClassSearchButtonListener());
		_searchTxt.addActionListener(new ClassSearchButtonListener());
		
		// search panel
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
		searchPanel.add(_searchTxt);
		searchPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		searchPanel.add(_searchBtn);

		// onto tree
		JPanel treePanel = new JPanel();
		treePanel.setLayout(new BoxLayout(treePanel, BoxLayout.Y_AXIS));
		treePanel.add(panel1);
		treePanel.add(Box.createRigidArea(new Dimension(0, 2)));
		treePanel.add(treeView);

		// top portion
		JPanel panel7 = new JPanel();
		panel7.setLayout(new BoxLayout(panel7, BoxLayout.X_AXIS));
		panel7.add(treePanel);
		
		// comment/description label
		JPanel panel8 = new JPanel();
		panel8.setLayout(new BoxLayout(panel8, BoxLayout.X_AXIS));
		panel8.add(new JLabel("Description:", SwingConstants.LEFT));
		panel8.add(Box.createHorizontalGlue());

		// top-level panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(panel7);
		panel.add(Box.createRigidArea(new Dimension(0, 7)));
		panel.add(panel8);
		panel.add(Box.createRigidArea(new Dimension(0, 2)));
		panel.add(commentView);
		if (showSearch) {
			panel.add(Box.createRigidArea(new Dimension(0, 5)));
			panel.add(searchPanel);
		}

		//panel.setMaximumSize(new Dimension(width, height));
		//panel.setPreferredSize(new Dimension(width, height));
		
		this.setLayout(new BorderLayout());
		
		add(panel, BorderLayout.CENTER);
	}

	/**
	 * Private method that initializes and creates the tree view sub-panel
	 * 
	 * @param libraryOnly
	 *            true if only the library ontologies are selected
	 * @return a scroll pane containing the ontologies
	 */
	private void createTreeView(OntologyClass filterSuperClass) {
		// create the default root node
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("");

		if (filterSuperClass != null) {
			buildTree(filterSuperClass, rootNode);
		} else {
			// don't add ontologies to root - just the top level classes
			// get each root class of the model
			Iterator<OntologyClass> rootClasses = SMS.getInstance().getOntologyManager().getNamedClasses().iterator();
			while (rootClasses.hasNext()) {
				// build tree from the roots
				OntologyClass root = rootClasses.next();
				List<OntologyClass> superclasses = SMS.getInstance().getOntologyManager().getNamedSuperclasses(root);
				// if we don't have a filter class, then make sure we only have top level roots
				if (superclasses == null || superclasses.isEmpty()) {
				//if (true) {	
					buildTree(root, rootNode);
				}
			}
		}

		// assign root to tree
		_ontoTree = new OntoClassSelectionJTree(rootNode);
		// configure tree
		_ontoTree.getTree().setRootVisible(false);
		// ensure we continue to listen to 2x clicks
		_ontoTree.getTree().setToggleClickCount(0);
		_ontoTree.getTree().addMouseListener(treeMouseListener);
		
		//_ontoTree.setCellRenderer(new OntoClassSelectionJTreeRenderer());
		_ontoTree.setDragEnabled(false); // if true, causes problems on linux
		_ontoTree.getTree()
				.addTreeSelectionListener(new OntoClassTreeSelectionListener());
		_ontoTree.getTree().setShowsRootHandles(true);
		
		// this allows us to use "proportional" sizing"
		_ontoTree.sizeColumnsToFit( JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS );
		_ontoTree.getColumnModel().getColumn(0).setPreferredWidth(8000);
		_ontoTree.getColumnModel().getColumn(1).setPreferredWidth(2000);
		
		// wrap tree in scroll pane
		treeView.setViewportView(_ontoTree);
	}

	/**
	 * Private method that recursively initializes the tree
	 * 
	 * @param c
	 *            the named ont class parent
	 * @param parentNode
	 *            the parent tree node
	 */
	private void buildTree(OntologyClass c, DefaultMutableTreeNode parentNode) {
		// add the class to the parent
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(c);
		parentNode.add(childNode);
		// get subclasses
		Iterator<OntologyClass> subclasses = SMS.getInstance().getOntologyManager().getNamedSubclasses(c, false).iterator();
		while (subclasses.hasNext()) {
			OntologyClass subclass = subclasses.next();
			buildTree(subclass, childNode);
		}
	}


	/**
	 * Private method that returns tree nodes containing OntTreeNodes
	 * 
	 * @param str
	 *            search string
	 * @return set of paths in the ontology tree
	 */
	private Vector findMatchingClasses(String str) {
		DefaultMutableTreeNode root = 
			(DefaultMutableTreeNode) _ontoTree.getTree().getModel().getRoot();
		return findMatchingClasses(root, str);
	}

	/**
	 * Private method that returns tree nodes containing matches
	 * Searches the Class Name and also the Class Labels as defined in the Ontology
	 * @param root
	 *            The root of the tree to search
	 * @param str
	 *            The search string
	 */
	private Vector<DefaultMutableTreeNode> findMatchingClasses(DefaultMutableTreeNode root, String str) {
		Vector<DefaultMutableTreeNode> result = new Vector<DefaultMutableTreeNode>();
		Object obj = root.getUserObject();
		if (obj instanceof OntologyClass) {
			OntologyClass cls = (OntologyClass) obj;
			// match on class name
			if (approxMatch(cls.getName(), str)) {
				result.add(root);
				return result;
			} else {
				boolean searchLabels = false;
				if (searchLabels) {
					// check ontology labels for more match potential
					List<String> labels = SMS.getInstance().getOntologyManager().getNamedClassLabels(cls);
					for (String label: labels) {
						if (approxMatch(label, str)) {
							result.add(root);
							return result;
						}
					}
				}
			}
		}
		Enumeration children = root.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = 
				(DefaultMutableTreeNode) children.nextElement();
			Iterator<DefaultMutableTreeNode> ancestors = findMatchingClasses(child, str).iterator();
			while (ancestors.hasNext()) {
				result.add(ancestors.next());
			}
		}
		return result;
	}

	/**
	 * Private method for checking whether two strings match
	 * 
	 * @param val1
	 *            first string to compare
	 * @param val2
	 *            second string to compare
	 * @return true if strings approximately match
	 */
	private boolean approxMatch(String val1, String val2) {
		val1 = val1.toLowerCase();
		val2 = val2.toLowerCase();
		if (val1.indexOf(val2) != -1 || val2.indexOf(val1) != -1)
			return true;
		return false;
	}

	/**
	 * Private method for collapsing the ontology tree to just the ontology
	 * nodes
	 */
	private void collapseTree() {
		int row = _ontoTree.getRowCount() - 1;
		while (row >= 0) {
			_ontoTree.getTree().collapseRow(row);
			row--;
		}
	}

	/**
	 * Private method for executing a search
	 * @deprecated
	 * 
	 * @param searchStr
	 *            the string to search for
	 */
	public void doSearch(String searchStr) {
		// reset the selections
		_ontoTree.clearSelection();
		// collapse the tree
		collapseTree();
		// if empty return
		if (searchStr.trim().equals(""))
			return;
		// get all the matches
		Iterator results = findMatchingClasses(searchStr).iterator();
		OntologyTreeModel treeModel = (OntologyTreeModel) _ontoTree.getTree().getModel();
		while (results.hasNext()) {
			// add selection for each match
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) results
					.next();
			_ontoTree.getTree().addSelectionPath(new TreePath(node.getPath()));
		}
		_commentTxt.setText("");
	}
	
	/**
	 * Private method for executing a search
	 * 
	 * @param searchStr
	 *            the string to search for
	 */
	public void doFilterSearch(String searchStr) {
		
		if (!firstSearch) {
			// reset the selections
			createTreeView(filterClass);
		}
		
		firstSearch = false;

		// if empty reset and expand the first level
		if (searchStr.trim().equals("")) {
			_ontoTree.getTree().expandRow(0);
			return;
		}
		// get all the matches
		Vector<DefaultMutableTreeNode> matches = findMatchingClasses(searchStr);
		OntologyTreeModel treeModel = (OntologyTreeModel) _ontoTree.getTree().getModel();
		//expand to include parents
		Vector<DefaultMutableTreeNode> expandedMatches = new Vector<DefaultMutableTreeNode>();
		for (DefaultMutableTreeNode match: matches) {
			// select the "core" matches
			TreePath path = new TreePath(match.getPath());
			_ontoTree.getTree().addSelectionPath(path);
			int row = _ontoTree.getTree().getRowForPath(path);
			_ontoTree.setRowSelectionInterval(row, row);
			// add the expanded matches so the tree is not over-pruned
			expandedMatches.addAll(expandMatches(match));
		}
		// add the originals
		expandedMatches.addAll(matches);
		//remove anything that doesn't match the complete list
		for (DefaultMutableTreeNode match: expandedMatches) {
			removeNodes(match, treeModel, expandedMatches);
			_ontoTree.getTree().expandPath(new TreePath(match.getPath()));
		}
		
		//select the first "core" match
		for (DefaultMutableTreeNode match: matches) {
			TreePath path = new TreePath(match.getPath());
			_ontoTree.getTree().addSelectionPath(path);
			int row = _ontoTree.getTree().getRowForPath(path);
			_ontoTree.setRowSelectionInterval(row, row);
			break;
		}
		
		_commentTxt.setText("");
	}
	
	
	private Vector<DefaultMutableTreeNode> expandMatches(DefaultMutableTreeNode node) {
		Vector<DefaultMutableTreeNode> matches = new Vector<DefaultMutableTreeNode>();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		while (parent != null) {
			if (!matches.contains(parent)) {
				matches.add(parent);
			}
			parent = (DefaultMutableTreeNode) parent.getParent();
		}
		return matches;
	}
	
	private void removeNodes(DefaultMutableTreeNode match, OntologyTreeModel treeModel, Vector<DefaultMutableTreeNode> matches) {
		//get the parent of the match
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) match.getParent();
		if (parent == null) {
			return;
		}
		//remove the children
		Vector<DefaultMutableTreeNode> nodesToRemove = new Vector<DefaultMutableTreeNode>();
		for (int i=0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
			if (!matches.contains(child)) {
				//remove this sibling of the match
				nodesToRemove.add(child);
				// now try to remove the parent's siblings
				removeNodes(parent, treeModel, matches);
			}
		}
		// remove them now
		for (DefaultMutableTreeNode node: nodesToRemove) {
			treeModel.removeNodeFromParent(node);
		}
	}

	/**
	 * Private method to select (highlight) a OntologyClass in the onto tree
	 * 
	 * @param cls
	 *            the OntologyClass to select
	 */
	public void doSelect(OntologyClass cls) {
		if (cls == null)
			return;
		// clear current selection
		_ontoTree.clearSelection();
		// collapse the tree
		collapseTree();
		// get the matches
		DefaultMutableTreeNode root = 
			(DefaultMutableTreeNode) _ontoTree.getTree().getModel().getRoot();
		Iterator paths = findPaths(cls, root).iterator();
		while (paths.hasNext()) {
			TreePath path = (TreePath) paths.next();
			_ontoTree.getTree().addSelectionPath(path);
			int row = _ontoTree.getTree().getRowForPath(path);
			_ontoTree.setRowSelectionInterval(row, row);
		}
	}

	/**
	 * Private method for finding all paths in onto tree to given OntologyClass
	 * 
	 * @param cls
	 *            the OntologyClass to find
	 * @param root
	 *            the root of the ontology tree
	 * @return a set of paths that lead to the OntologyClass
	 */
	private Vector findPaths(OntologyClass cls, DefaultMutableTreeNode root) {
		Vector paths = new Vector();
		Object obj = root.getUserObject();
		if (obj instanceof OntologyClass) {
			if (cls.equals((OntologyClass) obj)) {
				// add path to paths
				TreeNode[] ps = root.getPath();
				paths.add(new TreePath(root.getPath()));
			}
		}
		Enumeration children = root.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children
					.nextElement();
			Iterator descendants = findPaths(cls, child).iterator();
			while (descendants.hasNext())
				paths.add(descendants.next());
		}
		return paths;

	}

	// PRIVATE CLASSES


	/**
	 * Private class implementing a listener for search button
	 */
	private class ClassSearchButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			//doSearch(_searchTxt.getText());
			doFilterSearch(_searchTxt.getText());
		}
	};


	/**
	 * Private class that extends JTree for icons and drag and drop
	 */
	private class OntoClassSelectionJTree extends JTreeTable implements
			ActionListener {
		private JPopupMenu popup;
		private JMenuItem desc;
		private JMenuItem select;

		public OntoClassSelectionJTree(DefaultMutableTreeNode node) {
			super(new OntologyTreeModel(node));
			getTree().setCellRenderer(new OntologyTreeCellRenderer());
			popup = new JPopupMenu();
			popup.setOpaque(true);
			popup.setLightWeightPopupEnabled(true);
			initPopup();
			MouseAdapter mouseAdapter = new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger())
						doPopup(e);
				}

				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger())
						doPopup(e);
				}
			};
			addMouseListener(mouseAdapter);
			DragSource ds = DragSource.getDefaultDragSource();
			ds.createDefaultDragGestureRecognizer(this,
					DnDConstants.ACTION_COPY_OR_MOVE,
					new _OntoClassSelectionTreeGestureListener());
		}
		
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 1) {
				return new OntologyCellRenderer();
			} else {
				return super.getCellRenderer(row, column);
			}
		}

		private void initPopup() {
			popup.removeAll();

			select = new JMenuItem("Add as Selection", KeyEvent.VK_A);
			select.addActionListener(this);
			select.setActionCommand("__SELECT");
			popup.add(select);
			popup.addSeparator();
		}

		private void doPopup(MouseEvent e) {

			// unhighlight nodes
			clearSelection();

			// highlight selected node
			int row = getSelectedRow();
			if (row == -1)
				return;
			Object o = getValueAt(row, 0);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
			if (!(node.getUserObject() instanceof OntologyClass))
				return;

			//setSelectionRow(row);

			// configure popup
			initPopup();
			OntologyClass cls = (OntologyClass) node.getUserObject();
			Iterator<OntologyProperty> props = SMS.getInstance().getOntologyManager().getNamedProperties().iterator();
			while (props.hasNext()) {
				OntologyProperty p = props.next();
				JMenu submenu = new JMenu(p.getName());
				popup.add(submenu);
				Iterator<OntologyClass> ranges = SMS.getInstance().getOntologyManager().getRange(p).iterator();
				while (ranges.hasNext()) {
					OntologyClass range = ranges.next();
					JMenuItem rangeitem = new JMenuItem(range.getName());
					rangeitem.addActionListener(this);
					rangeitem.setActionCommand(range.getURI());
					submenu.add(rangeitem);
				}
			}

			// show popup
			popup.show((JComponent) e.getSource(), e.getX(), e.getY());
		}

		public void actionPerformed(ActionEvent ae) {
			int row = getSelectedRow();
			Object o = getValueAt(row, 0);
			DefaultMutableTreeNode node = 
				(DefaultMutableTreeNode) o;
			OntologyClass cls = (OntologyClass) node.getUserObject();
			if (ae.getActionCommand().equals("__SELECT")) {
				//addToList(cls);
			} else {
				String searchStr = ae.getActionCommand();
				//TODO: fuzzy match for search class
				OntologyClass searchCls = null;
				try {
					searchCls = new OntologyClass(searchStr);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (searchCls != null)
					doSelect(searchCls);
			}
		}
	};

	/**
	 * Private class for handling drag and drop initiation from the JTree
	 */
	private class _OntoClassSelectionTreeGestureListener implements
			DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent e) {

			Component source = e.getComponent();
			if (source instanceof OntoClassSelectionJTree) {
				OntoClassSelectionJTree tree = (OntoClassSelectionJTree) source;
				int row = tree.getSelectedRow();
				final Object o = tree.getValueAt(row, 0);
				Log.debug(30, "dragging, object: " + o);
				// If we didn't select anything.. then don't drag.
				if (o == null) {
					return;
				}
				if (o instanceof OntologyClass) {
					
					// construct the transferable object
					Transferable transferable = new Transferable() {

						public Object getTransferData(DataFlavor flavor)
								throws UnsupportedFlavorException, IOException {
							if (flavor.equals(OntologyClassField.ontologyClassFlavor)) {
								return o;
							} else {
								throw new UnsupportedFlavorException(flavor);
								//return null;
							}
						}

						public DataFlavor[] getTransferDataFlavors() {
							return new DataFlavor[] {OntologyClassField.ontologyClassFlavor};
						}

						public boolean isDataFlavorSupported(DataFlavor flavor) {
							if (flavor.equals(OntologyClassField.ontologyClassFlavor)) {
								return true;
							}
							return false;
						}
						
					};
					
					e.startDrag(DragSource.DefaultCopyNoDrop, transferable);
				}
			}
		}
	};

	/**
	 * Private class for handling tree selections. On selection, displays the
	 * comment for the selected class (if a comment exists).
	 */
	private class OntoClassTreeSelectionListener implements
			TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent ev) {
			int[] rows = _ontoTree.getSelectedRows();

			if (rows == null || rows.length < 1) {
				_commentTxt.setText("");
				return;
			}

			if (rows.length > 1) {
				_commentTxt.setText("");
			} else {
				int row = rows[0];
				Object obj = _ontoTree.getValueAt(row, 0);

				if (!(obj instanceof OntologyClass))
					_commentTxt.setText("");
				else {
					OntologyClass cls = (OntologyClass) obj;
					String label = SMS.getInstance().getOntologyManager().getNamedClassLabel(cls);
					_commentTxt.setText(label);
					_commentTxt.setCaretPosition(0);
					//_commentTxt.setText(cls.getURI());

				}
			}
		}
	};

	private JPopupMenu popup;
	private OntoClassSelectionJTree _ontoTree;
	private MouseListener treeMouseListener = null;
	private JTextField _searchTxt;
	private JTextArea _commentTxt;
	private JButton _searchBtn;
	

	// TESTING

	/**
	 * Main method for testing
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		OntologyClassSelectionPanel ontologyPalette = new OntologyClassSelectionPanel(true);
		Dimension dim = new Dimension(250,325);
		ontologyPalette.initialize(null, dim);
		frame.getContentPane().add(ontologyPalette);
		frame.setTitle("Test Frame");
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}// testing

}