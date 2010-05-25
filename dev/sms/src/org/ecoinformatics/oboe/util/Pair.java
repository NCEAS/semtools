package org.ecoinformatics.oboe.util;

public class Pair {
	String first;
	String second;
	
	public Pair(String _first, String _second){
		first = _first;
		second = _second;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}
}
