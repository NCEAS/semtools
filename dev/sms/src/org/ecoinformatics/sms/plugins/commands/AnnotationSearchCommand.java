/**
 *  '$RCSfile: SearchCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: leinfelder
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

package org.ecoinformatics.sms.plugins.commands;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.query.HeadResultSet;
import edu.ucsb.nceas.morpho.query.Query;
import edu.ucsb.nceas.morpho.query.ResultPanel;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.SortableJTable;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.UISettings;

import java.util.Vector;
import java.awt.event.ActionEvent;

import org.ecoinformatics.sms.plugins.pages.AnnotationQueryPage;

/**
 * Class to handle annotation search command
 */
public class AnnotationSearchCommand implements Command {


	/**
	 * Constructor of AnnotationSearchCommand
	 * 

	 */
	public AnnotationSearchCommand() {

	}// SearchCommand

	/**
	 * execute cancel command
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {
		MorphoFrame morphoFrame = null;
		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		// QueryDialog Create and show as modal
		if (morphoFrame != null) {
			AnnotationQueryPage aqp = new AnnotationQueryPage();
			// show the dialog
			ModalDialog dialog = 
				new ModalDialog(
						aqp, 
						UIController.getInstance().getCurrentActiveWindow(), 
						UISettings.POPUPDIALOG_WIDTH,
						UISettings.POPUPDIALOG_HEIGHT);

			// get the response back
			if (dialog.USER_RESPONSE == ModalDialog.OK_OPTION) {
				
				Query query = aqp.getQuery();
				if (query != null) {
					MorphoFrame box = 
						UIController.getInstance().addWindow(query.getQueryTitle());
					// first true is sorted or not, 5 is sorted column index,
					// second true
					// is send event of not
					doQuery(box, query, true, 5, SortableJTable.DECENDING, true);
				}// if
			}
		}// if
	}// execute

	/**
	 * Run the search query
	 * 
	 * @param resultWindow
	 *            MorphoFrame
	 * @param query
	 *            Query
	 */
	public static void doQuery(MorphoFrame resultWindow, Query query,
			boolean sorted, int sortedIndex, String sortedOder,
			boolean sendEvent) {
		
		resultWindow.setVisible(true);
		Vector vector = new Vector();
		HeadResultSet results = new HeadResultSet(query, vector, Morpho.thisStaticInstance);
		ResultPanel resultDisplayPanel = 
			new ResultPanel(null, results, 12,
				null, resultWindow.getDefaultContentAreaSize());
		resultDisplayPanel.setVisible(true);
		resultWindow.setMainContentPane(resultDisplayPanel);
		boolean showSearchNumber = true;
		StateChangeEvent event = null;
		if (sendEvent) {
			event = new StateChangeEvent(resultDisplayPanel,
					StateChangeEvent.CREATE_SEARCH_RESULT_FRAME);

		}
		query.displaySearchResult(resultWindow, resultDisplayPanel, sorted,
				sortedIndex, sortedOder, showSearchNumber, event);

	}// doQuery

	
}

