/**
 *    '$Id$'
 *
 *     '$Author$'
 *       '$Date$'
 *   '$Revision$'
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
package org.ecoinformatics.sms.annotation;

/**
 * Objects of this class represent observation contexts
 */
public class Context implements Comparable<Context>{

    /**
     * Default constructor
     */
    public Context() {
    }

    /**
     * @param obs the observation
     * @param rel the relationship
     * @param isIdentifying the identifying status
     */
    public Context(Observation obs, Relationship rel, boolean isIdentifying) {
        _observation = obs;
        _relationship = rel;
        _isIdentifying = isIdentifying;
    }

    /** 
     * Set the contextual observation
     * @param observation the observation
     */
    public void setObservation(Observation observation) {
        _observation = observation;
    }

    /** 
     * Get the contextual observation
     * @return the observation
     */
    public Observation getObservation() {
        return _observation;
    }

    /**
     * Set the context relationship
     * @param relationship the relationship
     */
    public void setRelationship(Relationship relationship) {
        _relationship = relationship;
    }

    /**
     * Get the context relationship
     * @return the relationship
     */
    public Relationship getRelationship() {
        return _relationship;
    }

    /**
     * Set whether this is an identifying context relationship
     * @param isIdentifying if true, this is an identifying
     * relationship
     */
    public void setIdentifying(boolean isIdentifying) {
        _isIdentifying = isIdentifying;
    }

    /**
     * Get the identifying status of this context relationship
     * @return true if this is an identifying relationship
     */
    public boolean isIdentifying() {
        return _isIdentifying;
    }
    
    public String toString() {
    	return getMadlib();
//    	if (_observation != null) {
//    		return _observation.toString();
//    	}
//    	return super.toString();
    }
    
    public String getMadlib() {
    	StringBuffer sb = new StringBuffer();
    	//sb.append("The Observation was ");
    	if (_relationship != null) {
    		sb.append(_relationship.toString());
    	}
    	else {
    		sb.append("<relationship>");
    	}
    	sb.append(" the ");
    	if (_observation != null) {
    		sb.append(_observation.toString());
    	}
    	else {
    		sb.append("<observation>");
    	}
    	return sb.toString();
    }
    
    private Observation _observation;
    private Relationship _relationship;
    private boolean _isIdentifying;
    
    /**
     * check whether two Context types are the same
     * @author cao
     */
	public boolean isSame(Context o)
		throws Exception
	{
		boolean cmp1 = _observation.getLabel().equals(o.getObservation().getLabel());
		
		if(!cmp1) return cmp1;
			
		if(_relationship==null||o.getRelationship()==null){
			throw new Exception("_relationship="+_relationship+", o.getRelationship()="+o.getRelationship());
		}
		boolean cmp2 = _relationship.getURI().equals(o.getRelationship().getURI());
		if(!cmp2) return cmp2;
		
		boolean cmp3 = (_isIdentifying==o.isIdentifying());
		return cmp3;
	}

	/**
	 * For comparing two contexts
	 * @author cao
	 */
	public int compareTo(Context c)	
	{
		
		int cmp1 = _observation.getLabel().compareTo(c.getObservation().getLabel());
		
		if(cmp1!=0) return cmp1;
		
		int cmp2=0;
		if(_relationship!=null||c.getRelationship()!=null){
			cmp2 = _relationship.getURI().compareTo(c.getRelationship().getURI());
		}
		if(cmp2!=0) return cmp2;
		
		return 0;
	}
} 