package org.ecoinformatics.sms.plugins.search;

import java.util.List;

import org.ecoinformatics.sms.ontology.OntologyClass;

public class Criteria  {

	private OntologyClass subject;
	private String condition;
	private OntologyClass value;
	
	private boolean group = false;
	private boolean any = true;
	private List<Criteria> subCriteria;
	
	public OntologyClass getSubject() {
		return subject;
	}
	public void setSubject(OntologyClass subject) {
		this.subject = subject;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public OntologyClass getValue() {
		return value;
	}
	public void setValue(OntologyClass value) {
		this.value = value;
	}
	
	public boolean isGroup() {
		return group;
	}
	public void setGroup(boolean group) {
		this.group = group;
	}
	public List<Criteria> getSubCriteria() {
		return subCriteria;
	}
	public void setSubCriteria(List<Criteria> subCriteria) {
		this.subCriteria = subCriteria;
	}
	public boolean isAny() {
		return any;
	}
	public void setAny(boolean any) {
		this.any = any;
	}
	public String toString() {
		return subject + " " + condition + " " + value; 
	}
	
}
