/**
 *    '$RCSfile: Owlifier.java,v $'
 *
 *     '$Author: bowers $'
 *       '$Date: 2007/12/09 17:20:31 $'
 *   '$Revision: 1.16 $'
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


//import org.apache.log4j.Logger;
//import org.apache.log4j.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import org.mindswap.pellet.jena.PelletInfGraph;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Shawn Bowers
 */
public class Owlifier {

    /**
     * Default constructor
     */
    public Owlifier() {
    }


    /**
     * Converts a sequence tab-delimitted lines into an OWL document.
     */
    public void owlify(String uri, Reader reader, Writer writer) 
	throws Exception 
    {
	_debug("Reading table ... ");
	// create the basic table
	ArrayList table = _createTable(reader);
	// check that table is well formed
	_checkTable(table);
	// fill-in the missing leading columns of rows
	_fillTable(table);
	// _debug("OK");
	// create object model
	_createObjectModel(table);
	// compute closures over the object model
	_closeObjectModel();
	// check for concept cycles
	_checkForCycles();
	// create owl model
	_createOwlModel(uri, writer);
    }

    

    ////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    
    /**
     * Creates an initial table from a sequence of tab-delimitted
     * lines.  The table (list of list of string) denotes rows in
     * which each row has the same number of columns.
     * @param reader a reader for the sequence of lines
     * @return the table 
     */
    private ArrayList _createTable(Reader reader) throws Exception {
	BufferedReader in = new BufferedReader(reader); 
	ArrayList<ArrayList<String>> rows = new ArrayList();
	String line = ""; 
	int lineno = 1;
	while((line = in.readLine()) != null) {
	    String rowstr[] = line.split("\t");
	    if(rowstr.length > 0 && !_emptyRow(rowstr)) {
		ArrayList row = new ArrayList();
		row.add("" + lineno);
		for(String s : rowstr) 
		    row.add(s);
		rows.add(row);
	    }
	    lineno++;
	}
	// create padded table
	ArrayList table = new ArrayList(); 
	for(ArrayList<String> r : rows) {
	    ArrayList paddedRow = new ArrayList();
	    table.add(paddedRow);
	    for(int i = 0; i < r.size(); i++) {
		if(i < r.size() && r.get(i).trim().length() > 0)
		    paddedRow.add(r.get(i));
		else 
		    paddedRow.add(NIL);
	    }
	}
	return table;
    }

    /**
     * @return true if the given row is empty
     */
    private boolean _emptyRow(String[] row) {
	for(String s : row) {
	    if(s.trim().length() > 0)
		return false;
	}
	return true;
    }

    /**
     * Check to make sure there are no gaps 
     */
    private void _checkTable(ArrayList table) throws Exception {
	for(int i = 0; i < table.size(); i++) {
	    ArrayList<String> r = (ArrayList)table.get(i);
	    boolean empty = true;
	    for(int j = 1; j < r.size(); j++) {
		String s = (String)r.get(j);
		if(!empty && s.equals(NIL))
		    _error(r.get(0), "empty cell between non-empty cells");
		if(!s.equals(NIL))
		    empty = false;
	    }
	}
    }

    /**
     * Fills the leading cells in of the table, based on values in
     * previous rows. Ensures all cells can be filled in.
     */
    private void _fillTable(ArrayList table) throws Exception {
	if(table.size() <= 1) 
	    return;
	ArrayList<String> r = (ArrayList)table.get(0);
	for(int i = 1; i < table.size(); i++) {
	    ArrayList<String> r_i = (ArrayList)table.get(i);
	    for(int j = 0; j < r_i.size(); j++) {
		if(r_i.get(j).equals(NIL)) {
		    if(r.size() <= j)
			_error(r_i.get(0), "undefined cell");
		    r_i.set(j, r.get(j));
		}
	    }
	    r = r_i;
	}
    }

    /**
     */
    private void _createObjectModel(ArrayList table) throws Exception {
	for(ArrayList<String> row : (ArrayList<ArrayList<String>>)table) {
	    // get the first row to see what type of entry this is
	    String lineno = row.get(0);
	    String type = row.get(1);
	    switch (_getRowType(lineno, type)) {
	    case CONCEPT: _createConcepts(row); break;
	    case IMPORT: _createImport(row); break;
	    case SYNONYM: _createSynonyms(row); break;
	    case HAS_PART: _createHasParts(row); break;
	    case PART_OF: _createPartOfs(row); break;
	    case OVERLAP: _createOverlaps(row); break;
	    case COMMENT: _createComment(row); break;
	    }
	}
    }

    private int _getRowType(String lineno, String str) throws Exception {
	str = str.trim().toLowerCase();
	if(str.equals("concept"))
	    return CONCEPT;
	if(str.equals("import"))
	    return IMPORT;
	if(str.equals("synonym"))
	    return SYNONYM;
	if(str.equals("has part"))
	    return HAS_PART; 
	if(str.equals("part of"))
	    return PART_OF;
	if(str.equals("overlap"))
	    return OVERLAP;
	if(str.equals("comment"))
	    return COMMENT;
	_error(lineno, "invalid keyword '" + str + "'");
	return -1;
    }

    /**
     * Create concept from row
     */
    private void _createConcepts(ArrayList<String> row) throws Exception {
	// warn if row redefines prefixed concepts
	for(int i = 3; i < row.size(); i++) {
	    String s1 = row.get(i-1);
	    String s2 = row.get(i);
	    if((s1.split(":").length >= 1) && (s2.split(":").length > 1)) 
		_warning(s2 + " now defined as subconcept of " + s1);
	}
	    
	// default case
	if(row.size() > 2)
	    _getConcept(new _ConceptName(row.get(2).trim()));
	// create remaining concepts and is-a relations
	for(int i = 2; i < row.size() - 1; i++) {
	    _Concept c1 = _getConcept(new _ConceptName(row.get(i).trim()));
	    _Concept c2 = _getConcept(new _ConceptName(row.get(i+1).trim()));
	    c1.addSubconcept(c2);
	    c2.addSuperconcept(c1);
	}
    }

    /**
     * Create import from row
     */
    private void _createImport(ArrayList<String> row) throws Exception {
	if(row.size() < 4) 
	    _error(row.get(0), "import statement missing value");
	String prefix = row.get(2).trim();
	if(_namespaces.containsKey(prefix))
	    _error(row.get(0), "duplicate prefix");
	String uri = row.get(3).trim();
	_namespaces.put(prefix, uri);
    }

    /**
     * Create synonyms from row
     */
    private void _createSynonyms(ArrayList<String> row) throws Exception {
	// ensure row does not redefine prefix
	for(int i = 2; i < row.size(); i++) {
	    String s = row.get(i);
	    if(s.split(":").length > 1) 
		_warning(s + " is now defined as a synonym");
	}
	for(int i = 2; i < row.size(); i++) {
	    _Concept c1 = _getConcept(new _ConceptName(row.get(i)));
	    for(int j = 2; j < row.size(); j++) {
		if(i != j) {
		    _Concept c2 = _getConcept(new _ConceptName(row.get(j)));
		    c1.addSynonym(c2);
		    c2.addSynonym(c1);
		}
	    }
	}
    }

    /**
     * Create parts from row
     */
    private void _createHasParts(ArrayList<String> row) throws Exception {
	// ensure row does not redefine prefix
	for(int i = 2; i < row.size(); i++) {
	    String s = row.get(i);
	    if(s.split(":").length > 1) 
		_warning(s + " is now defined with (possibly) new parts");
	}

	if(row.size() < 4)
	    _error(row.get(0), "missing part");
	for(int i = 2; i < row.size() - 1; i++) {
	    _Concept c1 = _getConcept(new _ConceptName(row.get(i)));
	    _Concept c2 = _getConcept(new _ConceptName(row.get(i+1)));
	    c1.addHasPart(c2);
	}
    }

    /**
     * Create wholes from row
     */
    private void _createPartOfs(ArrayList<String> row) throws Exception {
	// ensure row does not redefine prefix
	for(int i = 2; i < row.size(); i++) {
	    String s = row.get(i);
	    if(s.split(":").length > 1) 
		_warning(s + " is now defined to be a (possibly) new part");
	}

	if(row.size() < 4)
	    _error(row.get(0), "missing whole");
	for(int i = 2; i < row.size() - 1; i++) {
	    _Concept c1 = _getConcept(new _ConceptName(row.get(i)));
	    _Concept c2 = _getConcept(new _ConceptName(row.get(i+1)));
	    c1.addPartOf(c2);
	}
    }

    /**
     * Create overlaps from row
     */
    private void _createOverlaps(ArrayList<String> row) throws Exception {
	// ensure row does not redefine prefix
	for(int i = 2; i < row.size(); i++) {
	    String s = row.get(i);
	    if(s.split(":").length > 1) 
		_warning(s + " is now defined to overlap");
	}

	for(int i = 2; i < row.size(); i++) {
	    _Concept c1 = _getConcept(new _ConceptName(row.get(i)));
	    for(int j = 2; j < row.size(); j++) {
		if(i != j) {
		    _Concept c2 = _getConcept(new _ConceptName(row.get(j)));
		    c1.addOverlap(c2);
		    c2.addOverlap(c1);
		}
	    }
	}
    }

    /**
     * Create comment from row
     */
    private void _createComment(ArrayList<String> row) throws Exception {
	// ensure row does not redefine prefix
	if(row.size() > 3) {
	    if(row.get(2).split(":").length > 1) 
		_warning(row.get(2) + " is defined with a comment");
	}

	if(row.size() < 4) 
	    _error(row.get(0), "comment statement missing value");
	_Concept c = _getConcept(new _ConceptName(row.get(2)));
	String comment = row.get(3).trim();
	c.setComment(comment);
    }


    /**
     * Build concept from concept name
     * @return concept
     */
    private _Concept _getConcept(_ConceptName name) {
	_Concept c = _concepts.get(name);
	if(c == null) {
	    c = new _Concept();
	    c.setName(name);
	    _concepts.put(name, c);
	}
	return c;
    }


    /**
     * Propagate definitions to synonyms
     */
    private void _closeObjectModel() {
	_closeSynonyms();
	_closeComments();
	_closeSuperconcepts();
	_closeSubconcepts();
	_closeOverlaps();
	_closeDescendents();
    }


    /**
     * Compute the closure over the synonym relation
     */ 
    private void _closeSynonyms() {
	for(_Concept x : _concepts.values()) {
	    for(_Concept y : _getAllSynonyms(x))
		if(!x.equals(y))
		    x.addSynonym(y);
	}
    }

    /**
     * Gets all synonyms
     */
    private ArrayList<_Concept> _getAllSynonyms(_Concept c) {
	ArrayList synonyms = new ArrayList();
	_getAllSynonyms(c, synonyms);
	synonyms.remove(c);
	return synonyms;
    }

    /**
     * Recursively gets all synonyms
     */
    private void _getAllSynonyms(_Concept c, ArrayList<_Concept> synonyms) {
	for(_Concept x : c.getSynonyms()) {
	    if(!synonyms.contains(x)) {
		synonyms.add(x);
		_getAllSynonyms(x, synonyms);
	    }
	}
    }

    /**
     * Compute the closure of comments across synonyms
     */ 
    private void _closeComments() {
	for(_Concept x : _concepts.values()) {
	    for(_Concept y : x.getSynonyms()) {
		if(x.hasComment() && !y.hasComment())
		    y.setComment(x.getComment());
	    }
	}
    }

    /**
     * Compute the closure of the superconcept relation
     */
    private void _closeSuperconcepts() {
	for(_Concept x : _concepts.values()) 
	    for(_Concept y : _getAllParents(x))
		if(!x.equals(y))
		    x.addSuperconcept(y);
    }

    /**
     * Get all parents 
     */
    private ArrayList<_Concept> _getAllParents(_Concept child) {
	ArrayList<_Concept> parents = new ArrayList();
	_getParents(child, parents);
	for(_Concept synonym : child.getSynonyms()) 
	    _getParents(synonym, parents);
	parents.remove(child);
	return parents;
    }

    /**
     * Get defined parents of child, including parent synonyms
     */
    private void _getParents(_Concept child, ArrayList<_Concept> parents) {
	for(_Concept parent : child.getSuperconcepts()) {
	    if(!parents.contains(parent)) {
		parents.add(parent);
		for(_Concept synonym : parent.getSynonyms())
		    if(!parents.contains(synonym)) 
			parents.add(synonym);
	    }
	}
    }
    
    /**
     * Compute the closure of the subconcept relation
     */
    private void _closeSubconcepts() {
	for(_Concept x : _concepts.values()) 
	    for(_Concept y : _getAllChildren(x))
		if(!x.equals(y))
		    x.addSubconcept(y);
    }

    /**
     * Get all children 
     */
    private ArrayList<_Concept> _getAllChildren(_Concept parent) {
	ArrayList<_Concept> children = new ArrayList();
	_getChildren(parent, children);
	for(_Concept synonym : parent.getSynonyms()) 
	    _getChildren(synonym, children);
	return children;
    }

    /**
     * Get defined children of parent, including children synonyms
     */
    private void _getChildren(_Concept parent, ArrayList<_Concept> children) {
	for(_Concept child : parent.getSubconcepts()) {
	    if(!children.contains(child)) {
		children.add(child);
		for(_Concept synonym : child.getSynonyms())
		    if(!children.contains(synonym)) 
			children.add(synonym);
	    }
	}
    }

    /**
     * Compute the closure of the overlaps relation
     */
    private void _closeOverlaps() {
	for(_Concept x : _concepts.values()) 
	    for(_Concept y : _getAllOverlaps(x))
		if(!x.equals(y))
		    x.addOverlap(y);
    }

    /**
     * Get all overlaps 
     */
    private ArrayList<_Concept> _getAllOverlaps(_Concept c) {
	ArrayList<_Concept> overlaps = new ArrayList();
	_getOverlaps(c, overlaps);
	for(_Concept synonym : c.getSynonyms()) 
	    _getOverlaps(synonym, overlaps);
	return overlaps;
    }

    /**
     * Get defined overlap including overlap synonyms
     */
    private void _getOverlaps(_Concept c, ArrayList<_Concept> overlaps) {
	for(_Concept overlap : c.getOverlaps()) {
	    if(!overlaps.contains(overlap)) {
		overlaps.add(overlap);
		_getOverlaps(overlap, overlaps);
		for(_Concept synonym : overlap.getSynonyms()) {
		    if(!overlaps.contains(synonym)) {
			overlaps.add(overlap);
			_getOverlaps(synonym, overlaps);
		    }
		}
	    }
	}
    }

    /**
     * Compute the closure of the overlaps relation
     */
    private void _closeDescendents() {
	for(_Concept x : _concepts.values()) 
	    for(_Concept y : _getDescendents(x))
		if(!x.equals(y))
		    x.addDescendent(y);
    }

    /**
     * Get all descendents
     */
    private ArrayList<_Concept> _getDescendents(_Concept parent) {
	ArrayList<_Concept> descendents = new ArrayList();
	descendents.add(parent);
	_getDescendents(parent, descendents);
	descendents.remove(parent);
	return descendents;
    }

    /**
     * Recursively obtain descendents
     */
    private void _getDescendents(_Concept parent, 
				 ArrayList<_Concept> descendents)
    {
 	for(_Concept child : parent.getSubconcepts()) {
	    if(!descendents.contains(child)) {
		descendents.add(child); 
		_getDescendents(child, descendents);
	    }
	}
    }


    /**
     * Check for cycles that are not synonyms
     */
    private void _checkForCycles() {
	// find concepts descended from each other, and not synonyms
	ArrayList<_Concept> concepts = new ArrayList(_concepts.values());
	for(int i = 0; i < concepts.size(); i++) {
	    for(int j = i+1; j < concepts.size(); j++) {
		_Concept x = concepts.get(i);
		_Concept y = concepts.get(j);
		if(x.getDescendents().contains(y) &&
		   y.getDescendents().contains(x) &&
		   !x.getSynonyms().contains(y)) 
		    _warning(x.getName() + " and " + y.getName() + 
			     " are not synonyms and contain each other");
	    }
	}
    }

    /**
     * Create the owl model 
     */
    private void _createOwlModel(String uri, Writer writer) throws Exception {
        // create empty ontology model using Pellet spec
	// OntModel m = 
	// ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
	OntModel m = 
	    ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	// the 'oboe' prefix should always be defined as a namespace

	_debug("Creating OWL model ... ");

	if(!_namespaces.containsKey("oboe"))
	    _error("Missing required 'oboe' import");
	// create the ontology
	Ontology ontology = m.createOntology(uri);
	// add a comment
	ontology.addComment("Automatically generated using " + VERSION, "EN");
	// add the namespaces and imports 
	for(String prefix : _namespaces.keySet()) {
	    m.setNsPrefix(prefix, _namespaces.get(prefix) + "#");
	    ontology.addImport(m.createResource(_namespaces.get(prefix)));
	}
	// create initial classes
	for(_ConceptName n : _concepts.keySet()) {
	    if(!n.hasPrefix()) {
		String id = _toClassId(n.getName());
		OntClass c = m.createClass(uri + "#" + id); 
		c.addLabel(n.getName(), "EN");
		_classes.put(n, c);
	    } else {
		String ns = _namespaces.get(n.getPrefix());
		OntClass c = m.createClass(ns + "#" + n.getName());
		_classes.put(n, c);
	    }
	}
	// get the oboe part-of and has-part props
	String oboe_ns = _namespaces.get("oboe");
	Property hasPart = ResourceFactory.createProperty(oboe_ns + "#hasPart");
	Property partOf = ResourceFactory.createProperty(oboe_ns + "#partOf");

	// add subclasses, synonyms, comments, has-part, and part-of
	for(_ConceptName n : _concepts.keySet()) {
	    //	    if(!n.hasPrefix()) {
	    _Concept concept1 = _concepts.get(n);
	    OntClass c1 = _classes.get(n);
	    for(_Concept concept2 : concept1.getSubconcepts()) {
		OntClass c2 = _classes.get(concept2.getName());
		c1.addSubClass(c2);
	    }
	    for(_Concept concept2 : concept1.getSynonyms()) {
		OntClass c2 = _classes.get(concept2.getName());
		c1.addEquivalentClass(c2);
	    }
	    for(_Concept concept2 : concept1.getHasParts()) {
		OntClass c2 = _classes.get(concept2.getName());
		OntClass r = 
		    m.createSomeValuesFromRestriction(null, hasPart, c2);
		c1.addSuperClass(r);
	    }
	    for(_Concept concept2 : concept1.getPartOfs()) {
		OntClass c2 = _classes.get(concept2.getName());
		OntClass r = 
		    m.createSomeValuesFromRestriction(null, partOf, c2);
		c1.addSuperClass(r);
	    }
	    if(concept1.hasComment()) {
		c1.addComment(concept1.getComment(), "EN");
	    }
	}

	// add the disjoint classes
	for(ArrayList<_Concept> pair : _computeDisjointPairs()) {
	    // get each and assign disjoint
	    _ConceptName n1 = pair.get(0).getName();
	    _ConceptName n2 = pair.get(1).getName();
	    OntClass c1 = _classes.get(n1);
	    OntClass c2 = _classes.get(n2);
	    if(!n1.hasPrefix() && !n2.hasPrefix())
		c1.addDisjointWith(c2);
	}

	//_debug("OK");
	// _debug("Validating ontology ... ");
        // Get validation report
  	// ValidityReport report = m.validate();
   	// if(report != null && !report.isClean()) {
//    	    Iterator reports = report.getReports();
//    	    while(reports.hasNext()) {
//    		ValidityReport.Report r = (ValidityReport.Report)reports.next();
//    		if(r.isError())
//    		    _error(r.toString());
//    		else
//    		    System.out.println(r.toString()); 
//    	    }
//    	}

	_debug("Saving ontology (may take a few seconds) ...");

	// write out the final model
	// m.write(writer, "RDF/XML-ABBREV", uri);
	m.write(writer, "RDF/XML", uri);
    }

    /** 
     * Create a pairs of disjoint concepts
     */
    private ArrayList<ArrayList<_Concept>> _computeDisjointPairs() {
	// candidate and temporary set of pairs
	ArrayList<ArrayList<_Concept>> pairs = null, temp = null;
	// get all pairs of concepts
	pairs = new ArrayList();
	ArrayList<_Concept> concepts = new ArrayList(_concepts.values());
	for(int i = 0; i < concepts.size(); i++) {
	    for(int j = i+1; j < _concepts.keySet().size(); j++) {
		ArrayList<_Concept> pair = new ArrayList();
		pair.add(concepts.get(i));
		pair.add(concepts.get(j));
		pairs.add(pair);
	    }
	}
	// take pairs that are siblings or roots
	temp = new ArrayList();
	for(ArrayList<_Concept> pair : pairs) {
	    _Concept c1 = pair.get(0);
	    _Concept c2 = pair.get(1);
	    if(_root(c1) && _root(c2))
		temp.add(pair);
	    if(_siblings(c1, c2))
		temp.add(pair);
	}
	// take pairs that are not descendents or share a descendent
	pairs = temp;
	temp = new ArrayList();
	for(ArrayList<_Concept> pair : pairs) {
	    _Concept c1 = pair.get(0);
	    _Concept c2 = pair.get(1);
	    if(!_descendent(c1, c2) && !_descendent(c2, c1) && 
	       !_shareDescendent(c1, c2)) {
		temp.add(pair);
	    }
	}
	// take pairs that are not overlapping or synonyms
	pairs = temp;
	temp = new ArrayList();
	for(ArrayList<_Concept> pair : pairs) {
	    _Concept c1 = pair.get(0);
	    _Concept c2 = pair.get(1);
	    if(!_overlapping(c1, c2) && !_synonyms(c1, c2))
		temp.add(pair);
	}
	return temp;
    }

    /**
     * Check if two concepts share a parent
     */
    private boolean _siblings(_Concept c1, _Concept c2) {
	ArrayList<_Concept> parents = c1.getSuperconcepts();
	for(_Concept c : c2.getSuperconcepts()) 
	    if(parents.contains(c))
		return true;
	return false;
    }

    /**
     * Check if concept is a root
     */
    private boolean _root(_Concept c) {
	return c.getSuperconcepts().size() == 0;
    }

    /**
     * Check if child is a descendent of parent
     */
    private boolean _descendent(_Concept child, _Concept parent) {
	return parent.getDescendents().contains(child);
    }
    
    /**
     * Check if two concepts share a descendent
     */
    private boolean _shareDescendent(_Concept c1, _Concept c2) {
	ArrayList<_Concept> descendents = c1.getDescendents();
	for(_Concept c : c2.getDescendents())
	    if(descendents.contains(c))
		return true;
	return false;
    }

    /**
     * Check if the two concepts overlap or have descendents that overlap
     */
    private boolean _overlapping(_Concept c1, _Concept c2) {
	// directly overlap
	if(c1.getOverlaps().contains(c2))
	    return true;
	// descendents overlap
	for(_Concept x : c1.getDescendents()) 
	    for(_Concept y : c2.getDescendents()) 
		if(x.getOverlaps().contains(y))
		    return true;
	return false;
    }

    /**
     * Check if the two concepts are synonyms
     */
    private boolean _synonyms(_Concept c1, _Concept c2) {
	return c1.getSynonyms().contains(c2);
    }

    /**
     * Prints a table to standard out.
     * @param table the table
     */
    private void _printTable(ArrayList<ArrayList<String>> table) {
	System.out.println();
	for(ArrayList<String> r : table) {
	    for(String s : r) {
		if(s.equals(NIL))
		    s = "na";
		System.out.print(s + "\t");
	    }
	    System.out.println();
	}
    }
    
    /**
     * Convert a string to a class id
     * @param str the string to be converted
     * @return the id string
     */
    private String _toClassId(String str) {
	return str.replace(' ', '_');
    }

    /**
     * Throws an exception.
     * @param lineno the line number of the error 
     * @param msg the message string
     */
    private void _error(String lineno, String msg) throws Exception {
	throw new Exception("ERROR on line '" + lineno + "': " + msg);
    }

    /**
     * Throws an exception.
     * @param msg the message string
     */
    private void _error(String msg) throws Exception {
	throw new Exception("ERROR: " + msg);
    }

    /**
     * Declare a warning
     */
    private void _warning(String msg) {
	System.out.println("WARNING: " + msg);
    }


    /**
     * Start a debug string output
     */
    private void _debug(String msg) throws Exception {
	if(_debug) 
	    System.out.println(msg);
    }


    ////////////////////////////////////////////////////////////////////////
    // PRIVATE DATA

    private static String NIL = "";
    private final int CONCEPT = 1;
    private final int IMPORT = 2;
    private final int SYNONYM = 3;
    private final int HAS_PART = 4;
    private final int PART_OF = 5;
    private final int OVERLAP = 6;
    private final int COMMENT = 7;

    /* Class for representing concepts */
    private class _Concept { 
	public _ConceptName getName() {return _name;}
	public String getComment() {return _comment;}
	public boolean hasComment() {return getComment() != null;}
	public ArrayList<_Concept> getSubconcepts() {return _subconcepts;}
	public ArrayList<_Concept> getSuperconcepts() {return _superconcepts;}
	public ArrayList<_Concept> getSynonyms() {return _synonyms;}
	public ArrayList<_Concept> getHasParts() {return _hasParts;}
	public ArrayList<_Concept> getPartOfs() {return _partOfs;}
	public ArrayList<_Concept> getOverlaps() {return _overlaps;}
	public ArrayList<_Concept> getDescendents() {return _descendents;}
	public void setName(_ConceptName name) {_name = name;}
	public void setComment(String comment) {_comment = comment;}
	public void addSubconcept(_Concept c) {_add(_subconcepts, c);}
	public void addSuperconcept(_Concept c) {_add(_superconcepts, c);}
	public void addHasPart(_Concept c) {_add(_hasParts, c);}
	public void addPartOf(_Concept c) {_add(_partOfs, c);}
	public void addSynonym(_Concept c) {_add(_synonyms, c);}
	public void addOverlap(_Concept c) {_add(_overlaps, c);}
	public void addDescendent(_Concept c) {_add(_descendents, c);}
	public boolean equals(Object obj) {
	    if(obj instanceof _Concept)
		return getName().equals(((_Concept)obj).getName());
	    return false;
	}
	private void _add(ArrayList list, _Concept concept) {
	    if(!list.contains(concept))
		list.add(concept);
	}
	private _ConceptName _name;
	private String _comment;
	private ArrayList _subconcepts = new ArrayList();
	private ArrayList _superconcepts = new ArrayList();
	private ArrayList _hasParts = new ArrayList();
	private ArrayList _partOfs = new ArrayList();
	private ArrayList _synonyms = new ArrayList();
	private ArrayList _overlaps = new ArrayList();
	private ArrayList _descendents = new ArrayList();
    };


    private class _ConceptName {
	public _ConceptName(String name) {
	    String [] splitname = name.trim().split(":");
	    if(splitname.length < 2) {
		_name = name;
	    } else {
		_prefix = splitname[0].trim();
		_name = splitname[1].trim();
	    }
	}
	public boolean hasPrefix() {return getPrefix() != null;}
	public String getPrefix() {return _prefix;}
	public String getName() {return _name;}
	public boolean equals(Object obj) {
	    if(!(obj instanceof _ConceptName))
		return false;
	    _ConceptName c = (_ConceptName)obj;
	    if(!c.getName().equals(this.getName()))
		return false;
	    if(!c.hasPrefix() && this.hasPrefix())
		return false;
	    if(c.hasPrefix() && !this.hasPrefix())
		return false;
	    if(c.hasPrefix() && this.hasPrefix() && 
	       !(c.getPrefix().equals(this.getPrefix())))
		return false;
	    return true;
	}
 	public int hashCode() {return (_prefix + ":" + _name).hashCode();}
	public String toString() {
	    return hasPrefix() ? (getPrefix()+":"+getName()) : getName();
	}
	private String _prefix;
	private String _name;
    };

    private HashMap<_ConceptName,_Concept> _concepts = new HashMap();
    private HashMap<String,String> _namespaces = new HashMap();
    private HashMap<_ConceptName, OntClass> _classes = new HashMap();
    private HashMap<_ConceptName, Resource> _resources = new HashMap();

    private boolean _debug = true;

    ////////////////////////////////////////////////////////////////////////
    // MAIN

    public static String VERSION = "OWLIFIER Version 0.0a1";

    public static void main(String [] args) {
	if(args.length < 2) {
	    System.err.println("owlifier: infile and/or outfile missing");
	    System.err.println("usage: java Owlifier <infile> <outfile>");
	    System.exit(-1);
	}
	if(args[0].trim().equals("")) {
	    System.err.println("owlifier: input file not given");
	    System.err.println("usage: java owlifier <infile> <outfile>");
	    System.exit(-1);
	}
	if(args[1].trim().equals("")) {
	    System.err.println("owlifier: output file not given");
	    System.err.println("usage: java Owlifier <infile> <outfile>");
	    System.exit(-1);
	}
	String arg1 = args[0];
	String arg2 = args[1];
	// System.out.println("Processing file: " + arg1);

	Owlifier o = new Owlifier();
	try {
	    Reader reader = new FileReader(arg1);
	    File f = new File(arg2);
	    FileWriter writer = new FileWriter(f);
	    String uri = "file:" + f.getAbsolutePath();
	    o.owlify(uri, reader, writer);
	    reader.close();
	    writer.close();
	} catch(Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	if(args.length >= 2) {
	    // write result to file
	} else {
	    // print result to std out
	}
    }

}


