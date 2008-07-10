/**
 *    '$RCSfile: ResourceManager.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2007/05/21 20:25:10 $'
 *   '$Revision: 1.1 $'
 *
 *  For Details: http://daks.ucdavis.edu
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

package org.ecoinformatics.sms;

import org.ecoinformatics.sms.resource.Resource;
import java.util.Vector;

/**
 */
public interface ResourceManager {

    /**
     * Register a resource with the manager
     * @param uri the identifier (e.g., location) of the resource
     * @param type the type of the resource
     */
    public void registerResource(String uri, Class type) throws Exception;

    /**
     * Unregister the resource from the manager
     * @param uri the identifier of the resource
     */
    public void unregisterResource(String uri) throws Exception;

    /**
     * Checks if the resource is registered with this manager
     * @param uri the identifier of the resource
     */
    public boolean isResource(String uri);

    /**
     * Gets the resource with the given identifier
     * @param uri the identifier of the resource
     * @return the resource
     */
    public Resource getResource(String uri) throws Exception;

    /**
     * Gets the type of the resource with the given identifier
     * @param uri the identifier of the resource
     * @return the type class
     */
    public Class getResourceType(String uri) throws Exception;

    /**
     * Gets the resource identifiers registered with this manager
     * @return the resource identifiers
     */
    public Vector<String> getResourceIDs();

}