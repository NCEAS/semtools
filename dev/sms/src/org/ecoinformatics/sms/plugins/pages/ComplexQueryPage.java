/**
 *  '$RCSfile: UsageRights.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Ben Leinfelder
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

package org.ecoinformatics.sms.plugins.pages;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.annotation.Triple;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.search.Criteria;
import org.ecoinformatics.sms.plugins.search.CriteriaRenderer;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.query.Query;
import edu.ucsb.nceas.utilities.OrderedMap;

public class ComplexQueryPage extends AbstractUIPage {

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	private final String pageID = null;
	private final String pageNumber = "0";
	private final String title = "Complex Query";
	private final String subtitle = "";

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *
		
	// query list
	private CustomList queryList;
	private JLabel queryListLabel;
	
	// the final query
	private Query query;
	
	// docids from the annotation query
	private List<String> docids = new ArrayList<String>();
	

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	public ComplexQueryPage() {
		init();
	}

	/**
	 * initialize method does frame-specific design - i.e. adding the widgets
	 * that are displayed only in this frame (doesn't include prev/next buttons
	 * etc)
	 */
	private void init() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel desc = WidgetFactory
				.makeHTMLLabel(
						"<b>Annotation Search</b> "
								+ "Specify one or more search criteria",
						2);
		this.add(desc);
		this.add(WidgetFactory.makeDefaultSpacer());
		
		this.add(WidgetFactory.makeDefaultSpacer());
		
		// Query list
		JPanel queryListPanel = WidgetFactory.makePanel(10);
		queryListLabel = WidgetFactory.makeLabel("Conditions:", false);
		queryListPanel.add(queryListLabel);
		String[] colNames = new String[] {"Conditions"};
		Object[] editors = new Object[] {new CriteriaRenderer(false) };
		queryList = WidgetFactory.makeList(
				colNames, 
				editors, 
				5, //displayRows, 
				true, //showAddButton, 
				true, //showEditButton, 
				false, //showDuplicateButton, 
				true, //showDeleteButton, 
				false, //showMoveUpButton, 
				false //showMoveDownButton
				);		
		// add
		queryList.setCustomAddAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
	
				Criteria criteria = new Criteria();
				
				List rowList = new ArrayList();
				rowList.add(criteria);
				queryList.addRow(rowList);
				
				
			}
		});
		/**
		// edit
		queryList.setCustomEditAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				List rowList = queryList.getSelectedRowList();
				AnnotationQueryPage aqp = (AnnotationQueryPage) rowList.get(0);
				
				// show the dialog
				ModalDialog dialog = 
					new ModalDialog(
							aqp, 
							UIController.getInstance().getCurrentActiveWindow(), 
							UISettings.POPUPDIALOG_WIDTH,
							UISettings.POPUPDIALOG_HEIGHT);

				// get the response back
				if (dialog.USER_RESPONSE == ModalDialog.OK_OPTION) {
					// make sure the UI reflects the changes
					queryList.revalidate();
					queryList.repaint();

				}
				
			}
		});
		**/
		
		queryListPanel.add(queryList);
		queryListPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(queryListPanel);

		this.add(WidgetFactory.makeDefaultSpacer());

	}
	
	public List<String> getDocids() {
		return docids;
	}
	
	public Query getQuery() {
		
		// generate query
		generateQuery();
		
		return query;
	}

	private void generateQuery() {

		List<OntologyClass> characteristics = new ArrayList<OntologyClass>();
		List<OntologyClass> standards = new ArrayList<OntologyClass>();
		List<OntologyClass> protocols = new ArrayList<OntologyClass>();
		List<OntologyClass> entities = new ArrayList<OntologyClass>();
		List<Triple> contexts = new ArrayList<Triple>();

		
		for (int i = 0; i < queryList.getListOfRowLists().size(); i++) {
			
			// get the query page
			List rowList = (List) queryList.getListOfRowLists().get(i);
			AnnotationQueryPage aqp = (AnnotationQueryPage) rowList.get(0);
			
			Characteristic currentCharacteristic;
			Standard currentStandard;
			Protocol currentProtocol;
			Entity currentEntity;
			Triple context;
			
			// get the values form the page
			try {
				currentCharacteristic = new Characteristic(
						aqp.getSimpleAnnotationPanel().getObservationCharacteristic().getURI());
				characteristics.add(currentCharacteristic);
			} catch (Exception e) {
				currentCharacteristic = null;
			}
	
			try {
				currentStandard = new Standard(aqp.getSimpleAnnotationPanel().getObservationStandard().getURI());
				standards.add(currentStandard);
			} catch (Exception e) {
				currentStandard = null;
			}
			
			try {
				currentProtocol = new Protocol(aqp.getSimpleAnnotationPanel().getObservationProtocol().getURI());
				protocols.add(currentProtocol);
			} catch (Exception e) {
				currentProtocol = null;
			}
			
			try {
				currentEntity = new Entity(aqp.getSimpleAnnotationPanel().getObservationEntity().getURI());
				entities.add(currentEntity);
			} catch (Exception e) {
				currentEntity = null;
			}
			
			try {
				context = aqp.getContextTriplePanel().getContextTriple();
			} catch (Exception e) {
				context = null;
			}
			if (context != null) {
				contexts.add(context);
			}
			
		}
		
		// TODO: handle AND/OR
		
		// generate the query
		List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getMatchingAnnotations(
				entities, 
				characteristics, 
				standards,
				protocols,
				contexts,
				true);
		
		
		// get the query text
		String querySpec = AnnotationPlugin.getDocQuery(annotations);
		
		// make it
		query = new Query(querySpec, Morpho.thisStaticInstance);
		query.setSearchLocal(true);
		query.setSearchMetacat(false);
		
		// set the docids
		docids.clear();
		for (Annotation a: annotations) {
			docids.add(a.getEMLPackage());
		}
		
	}

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	/**
	 * The action to be executed when the page is displayed. May be empty
	 */
	public void onLoadAction() {
	}

	/**
	 * The action to be executed when the "Prev" button is pressed. May be empty
	 * 
	 */
	public void onRewindAction() {

	}

	/**
	 * The action to be executed when the "Next" button (pages 1 to
	 * last-but-one) or "Finish" button(last page) is pressed. May be empty, but
	 * if so, must return true
	 * 
	 * @return boolean true if wizard should advance, false if not (e.g. if a
	 *         required field hasn't been filled in)
	 */
	public boolean onAdvanceAction() {
		return true;
	}

	/**
	 * gets the OrderedMap object that contains all the key/value paired
	 * settings for this particular wizard page
	 * 
	 * @return data the OrderedMap object that contains all the key/value paired
	 *         settings for this particular wizard page
	 */
	private OrderedMap returnMap = new OrderedMap();

	public OrderedMap getPageData() {
		return getPageData(null);
	}

	/**
	 * gets the Map object that contains all the key/value paired settings for
	 * this particular wizard page
	 * 
	 * @param rootXPath
	 *            the root xpath to prepend to all the xpaths returned by this
	 *            method
	 * @return data the Map object that contains all the key/value paired
	 *         settings for this particular wizard page
	 */
	public OrderedMap getPageData(String rootXPath) {

		return returnMap;
	}

	/**
	 * gets the unique ID for this wizard page
	 * 
	 * @return the unique ID String for this wizard page
	 */
	public String getPageID() {
		return pageID;
	}

	/**
	 * gets the title for this wizard page
	 * 
	 * @return the String title for this wizard page
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * gets the subtitle for this wizard page
	 * 
	 * @return the String subtitle for this wizard page
	 */
	public String getSubtitle() {
		return subtitle;
	}

	/**
	 * Returns the ID of the page that the user will see next, after the "Next"
	 * button is pressed. If this is the last page, return value must be null
	 * 
	 * @return the String ID of the page that the user will see next, or null if
	 *         this is te last page
	 */
	public String getNextPageID() {
		return nextPageID;
	}

	/**
	 * Returns the serial number of the page
	 * 
	 * @return the serial number of the page
	 */
	public String getPageNumber() {
		return pageNumber;
	}

	public boolean setPageData(OrderedMap data, String _xPathRoot) {
		return true;
	}

}
