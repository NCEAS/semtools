package org.ecoinformatics.sms.annotation.search;

import java.util.List;

import org.ecoinformatics.sms.annotation.Triple;
import org.ecoinformatics.sms.ontology.OntologyClass;

public class Criteria  {
	
	public static final String IS = "is";

	public static final String ISNOT = "is not";

	private Class subject;
	private String condition;
	private OntologyClass value;
	
	private boolean context = false;
	private Triple contextTriple;

	private boolean group = false;
	private boolean all = true;
	private boolean same = true;

	private List<Criteria> subCriteria;
	
	public Class getSubject() {
		return subject;
	}
	public void setSubject(Class subject) {
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
	public boolean isAll() {
		return all;
	}
	public boolean isSame() {
		return same;
	}
	public void setSame(boolean same) {
		this.same = same;
	}
	public void setAll(boolean all) {
		this.all = all;
	}
	public boolean isContext() {
		return context;
	}
	public void setContext(boolean context) {
		this.context = context;
	}
	public Triple getContextTriple() {
		return contextTriple;
	}
	public void setContextTriple(Triple contextTriple) {
		this.contextTriple = contextTriple;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (isGroup()) {
			if (isAll()) {
				sb.append("Match All ");
			} else {
				sb.append("Match Any ");
			}
			sb.append(" [");
			if (subCriteria != null) {
				for (Criteria c: subCriteria) {
					sb.append(c.toString());
					sb.append(", ");
				}
			}
			sb.append("]");
		} else if (isContext()) {
			sb.append("(");
			sb.append(getContextTriple());
			sb.append(")");
		} else {
			sb.append("(");
			sb.append(subject + " " + condition + " " + value);
			sb.append(")");
		}
		return sb.toString(); 
	}
	
}
