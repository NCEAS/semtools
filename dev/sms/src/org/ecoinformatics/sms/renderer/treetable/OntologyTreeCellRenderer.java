/**
 *       Name: AccessTreeCellRenderer.java
 *    Purpose: Uses the ImageIcon stored in a UserObject
 *             as the icon of TreeCellNode
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author$'
 *     '$Date$'
 * '$Revision$'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ecoinformatics.sms.renderer.treetable;

import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.ecoinformatics.sms.ontology.Ontology;
import org.ecoinformatics.sms.ontology.OntologyClass;

/**
 * This class is a simple extension of the DefaultTreeCell Renderer that uses an
 * ImageIcon stored in the NodeInfo UserObject of a tree node as the icon of a
 * node when it is displayed in a tree. Using an icon from the UserObject allows
 * each node to have its own icon which can be dynamically changed.
 * 
 * @author leinfelder
 */
public class OntologyTreeCellRenderer extends
		javax.swing.tree.DefaultTreeCellRenderer {
	
	private final String CLASS_ICON = "";
	private final String ONTO_ICON = "";
	
	private ImageIcon _classIcon;
	private ImageIcon _ontoIcon;

	/** initializes the renderer */
	public OntologyTreeCellRenderer() {
		URL ontoImgURL = 
			OntologyTreeCellRenderer.class.getResource(ONTO_ICON);
		if (ontoImgURL != null) {
			_ontoIcon = new ImageIcon(ontoImgURL);
		}
		URL classImgURL = OntologyTreeCellRenderer.class.getResource(CLASS_ICON);
		if (classImgURL != null) {
			_classIcon = new ImageIcon(classImgURL);
		}

		// _classIcon = new ImageIcon(CLASS_ICON);
		// _ontoIcon = new ImageIcon(ONTO_ICON);
	}

	/** returns tree cell renderer */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded,
				leaf, row, hasFocus);
		
		if (isClassNode(value)) {
			setIcon(_classIcon);
			//setToolTipText("This is a class ... ");
		} else if (isOntoNode(value)) {
			setIcon(_ontoIcon);
			//setToolTipText("This is an ontology ... ");
		}
		return this;
	}

	/**
	 * Determines if a given value is a class node
	 * 
	 * @return true if the given node object is a OntologyClass
	 */
	protected boolean isClassNode(Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object obj = node.getUserObject();
		if (obj instanceof OntologyClass)
			return true;
		return false;
	}

	/**
	 * Determines if a given value is an ontology node
	 * 
	 * @return true if the given node object is a Ontology
	 */
	protected boolean isOntoNode(Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object obj = node.getUserObject();
		if (obj instanceof Ontology)
			return true;
		return false;
	}	

}
