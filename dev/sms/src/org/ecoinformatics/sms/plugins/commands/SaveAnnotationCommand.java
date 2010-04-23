/**
 *  '$RCSfile: EditColumnMetaDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
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

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.ecoinformatics.sms.plugins.AnnotationPlugin;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;

/**
 * Class to handle edit column meta data command
 */
public class SaveAnnotationCommand implements Command {
	
	/**
	 * Constructor
	 */
	public SaveAnnotationCommand() {}

	/**
	 * execute annotation wizard
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {
		
		// saving annotations for current packageId 
		AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
		String packageId = adp.getPackageId();
		
		AnnotationPlugin.serializeAnnotation(packageId);
		
		JOptionPane.showMessageDialog(UIController.getInstance().getCurrentActiveWindow(), "Annotations Saved!");
	}

}
