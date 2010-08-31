/**
 *  '$Id$'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
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

package org.ecoinformatics.sms.plugins.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.commands.DirectAnnotationCommand;
import org.ecoinformatics.sms.plugins.pages.Help;

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

/**
 * Panel for viewing and editing data-centric Annotations
 * @author leinfelder
 *
 */
public class AnnotationTablePanel extends JPanel implements StateChangeListener  {

	public static final Dimension rowHeaderDim = new Dimension(100, 17);
	private AnnotationTable annotationTable;
	private JScrollPane annotationScrollPane;

	public AnnotationTablePanel(AnnotationTableModel annotationTableModel) {
		//super(new BorderLayout(0,0));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// add the main table
		annotationTable = new AnnotationTable(annotationTableModel);
		annotationTable.setColumnSelectionAllowed(true);
		annotationTable.setRowSelectionAllowed(true);
		annotationTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    annotationTable.getTableHeader().setReorderingAllowed(false);
	    annotationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    
	    
	    // when we click on the table, we invoke the command
	    MouseListener mouseListener = new MouseAdapter() {
	    	
	    	DirectAnnotationCommand directAnnotationCommand = new DirectAnnotationCommand(annotationTable);
			
	    	@Override
			public void mouseClicked(MouseEvent e) {
	    		if (!e.isPopupTrigger()) {
		    		// this counts as selection of the column
		    		StateChangeEvent event = new StateChangeEvent(annotationTable, StateChangeEvent.SELECT_DATATABLE_COLUMN);
		    		StateChangeMonitor.getInstance().notifyStateChange(event);
	    		}
	    		
	    		// open the direct editor if they double click
	    		if (e.getClickCount() > 1) {
	    			directAnnotationCommand.execute(null);
	    		}
				
			}
	    	
	    };
	    annotationTable.addMouseListener(mouseListener);
	    
	    
	    Dimension dim = new Dimension(rowHeaderDim.width, AnnotationTableModel.ROW_COUNT * rowHeaderDim.height);
		annotationTable.setPreferredScrollableViewportSize(dim);
		
		// put it in a scrollpane
		annotationScrollPane = new JScrollPane(annotationTable);
		
		// add the row headers
	    JList rowheaders = new JList(annotationTableModel.getRowHeaders());
	    rowheaders.setPreferredSize(rowHeaderDim);
		rowheaders.setCellRenderer(new RowHeaderRenderer());
		annotationScrollPane.setRowHeaderView(rowheaders);
		annotationScrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));

		// the reorder command
		Command reorderCommand = new Command() {
			public void execute(ActionEvent event) {
				((AnnotationTable)annotationTable).reorder(true);					
			}
		};
		GUIAction reorderAction = new GUIAction("Reorder the columns", null, reorderCommand);
		JButton reorder = new HyperlinkButton(reorderAction);
		
		// the help button
		final AnnotationTablePanel pageRef = this;
		JButton helpButton = Help.createHelpButton(pageRef, "Information about Semantic Annotation");
		
		JPanel descPanel = WidgetFactory.makePanel(4);
		descPanel.setLayout(new GridLayout(3, 1));
		descPanel.add(WidgetFactory.makeHTMLLabel("<p>Double click cells to directly edit the Annotation</p>", 1));
		descPanel.add(helpButton);
		descPanel.add(reorder);
		
		this.add(descPanel);
		
		this.add(annotationScrollPane);
		
	}

	public void handleStateChange(StateChangeEvent event) {
		//check that this is an even or our frame
		MorphoFrame thisAncestor = GUIAction.getMorphoFrameAncestor(this);
		if (!GUIAction.isLocalEvent(event, thisAncestor)) {
			return;
		}
		
		if (event.getChangedState().equals(AnnotationPlugin.ANNOTATION_CHANGE_EVENT)) {
			AnnotationTableModel model = (AnnotationTableModel) annotationTable.getModel();
			
			//get the latest annotation
			Annotation annotation = AnnotationPlugin.getCurrentActiveAnnotation();
			model.setAnnotation(annotation);
			
			// remember the current selection state
			int viewIndex = annotationTable.getSelectedColumn();
			int modelIndex = -1;
			if (viewIndex >= 0) {
				modelIndex = annotationTable.getColumnModel().getColumn(viewIndex).getModelIndex();
			}
			
			model.fireTableRowsUpdated(0, model.getRowCount());
			model.fireTableStructureChanged();
			
			JList rowheaders = new JList(model.getRowHeaders());
			rowheaders.setCellRenderer(new RowHeaderRenderer());
			annotationScrollPane.setRowHeaderView(rowheaders);
			
			annotationTable.reset();
			
			if (annotationTable.isReordered()) {
				annotationTable.reorder(true);
			}
			
			// set the original selection index
			annotationTable.setColumnSelectionInterval(viewIndex, viewIndex);
		}
	
	}

	public JTable getAnnotationTable() {
		return annotationTable;
	}

	public void setAnnotationTable(AnnotationTable annotationTable) {
		this.annotationTable = annotationTable;
	}

	public JScrollPane getAnnotationScrollPane() {
		return annotationScrollPane;
	}

	public void setAnnotationScrollPane(JScrollPane annotationScrollPane) {
		this.annotationScrollPane = annotationScrollPane;
	}
	
	
}

/**
 * Renderer for the Row Headers - supposed to mimic the Column header LaF
 * @author leinfelder
 *
 */
class RowHeaderRenderer extends JLabel implements ListCellRenderer, TableCellRenderer {  
    RowHeaderRenderer() {
      setOpaque(true);
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      //setBorder(header.getBorder());
      setHorizontalAlignment(CENTER);
      setForeground(UIManager.getColor("TableHeader.foreground"));
      //setBackground(UIManager.getColor("TableHeader.background"));
      setFont(UIManager.getFont("TableHeader.font"));
      setPreferredSize(AnnotationTablePanel.rowHeaderDim);
    }
   
    public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {
      setText((value == null) ? "" : value.toString());
      switch (row) {
		case AnnotationTableModel.CONTEXT_ROW:
			setToolTipText("The context[s] in which the observation was made");
			break;
		case AnnotationTableModel.OBSERVATION_ROW:
			setToolTipText("The 'thing' being observed");
			break;
		case AnnotationTableModel.CHARACTERISTIC_ROW:
			setToolTipText("The characteristic of the 'thing' being measured");
			break;
		case AnnotationTableModel.STANDARD_ROW:
			setToolTipText("The measurement unit");
			break;
		case AnnotationTableModel.PROTOCOL_ROW:
			setToolTipText("The protocol used for collecting the measurement");
			break;	
		default:
			break;
      }
      return this;
    }

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		return getTableCellRendererComponent(null, value, isSelected, cellHasFocus, index, 0);
	}
}