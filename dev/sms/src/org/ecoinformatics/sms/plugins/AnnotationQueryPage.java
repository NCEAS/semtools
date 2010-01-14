/**
 *  '$RCSfile: UsageRights.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Ben Leinfelder
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.18 $'
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

package org.ecoinformatics.sms.plugins;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.plugins.ui.SimpleAnnotationPanel;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.query.Query;
import edu.ucsb.nceas.utilities.OrderedMap;

public class AnnotationQueryPage extends AbstractUIPage {

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	private final String pageID = "";
	private final String pageNumber = "0";
	private final String title = "Annotation Query";
	private final String subtitle = "";

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *
	
	private SimpleAnnotationPanel simpleAnnotationPanel = null;

	private Query query = null;

	private static Entity currentEntity;
	private static Characteristic currentCharacteristic;
	private static Standard currentStandard;
	private static Protocol currentProtocol;
	
	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	public AnnotationQueryPage() {
		nextPageID = null;
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
						"<b>Select Observation Entity, Characteristic and Standard to search by.</b> "
								+ "The Ontology Browser can be used to navigate specific ontologies.",
						2);
		this.add(desc);

		this.add(WidgetFactory.makeDefaultSpacer());
		
		//add the main panel here
		simpleAnnotationPanel = new SimpleAnnotationPanel();
		this.add(simpleAnnotationPanel);

		this.add(WidgetFactory.makeHalfSpacer());
		this.add(WidgetFactory.makeDefaultSpacer());

	}
	
	public Query getQuery() {
		
		// generate query
		generateQuery();
		
		return query;
	}

	private void generateQuery() {

		// get the values
		try {
			currentCharacteristic = new Characteristic(
					simpleAnnotationPanel.getObservationCharacteristic().getURI());
		} catch (Exception e) {
			currentCharacteristic = null;
		}

		try {
			currentStandard = new Standard(simpleAnnotationPanel.getObservationStandard().getURI());
		} catch (Exception e) {
			currentStandard = null;
		}
		
		try {
			currentProtocol = new Protocol(simpleAnnotationPanel.getObservationProtocol().getURI());
		} catch (Exception e) {
			currentProtocol = null;
		}
		
		try {
			currentEntity = new Entity(simpleAnnotationPanel.getObservationEntity().getURI());
		} catch (Exception e) {
			currentEntity = null;
		}
		
		// generate the query
		List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getMatchingAnnotations(
				currentEntity, 
				currentCharacteristic, 
				currentStandard,
				currentProtocol,
				true);
		
		// get the query text
		String querySpec = AnnotationPlugin.getDocQuery(annotations);
		
		// make it
		query = new Query(querySpec, Morpho.thisStaticInstance);
		query.setSearchLocal(true);
		query.setSearchMetacat(false);
	

	}

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	/**
	 * The action to be executed when the page is displayed. May be empty
	 */
	public void onLoadAction() {
		// load the last values if they exist
		try {
			simpleAnnotationPanel.setObservationEntity(currentEntity);
		} catch (Exception e) {}
		try {
			simpleAnnotationPanel.setObservationStandard(currentStandard);
		} catch (Exception e) {}
		try {
			simpleAnnotationPanel.setObservationCharacteristic(currentCharacteristic);
		} catch (Exception e) {}
		try {
			simpleAnnotationPanel.setObservationProtocol(currentProtocol);
		} catch (Exception e) {}

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
