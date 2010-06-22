package org.ecoinformatics.oboe.util;

import java.util.List;
import java.util.Comparator;

public class ListComparator implements Comparator{

	public int compare(Object o1, Object o2) {
		List list1 = (List) o1;
		List list2 = (List) o2;
		int cmp1 = list1.size()-list2.size(); 
		if(cmp1!=0) return cmp1;
		
		int cmp2 = 0;
		for(int i=0;i<list1.size();i++){
			Object obj1 = list1.get(i);
			Object obj2 = list2.get(i);
			if(obj1 instanceof String){
				cmp2 = ((String)obj1).compareTo((String)obj2);
			}else if (obj1 instanceof Integer){
				cmp2 = ((Integer)obj1).compareTo((Integer)obj2);
			}//TODO others
			if(cmp2!=0) break;
		}
		
		return cmp2;
	}
}
