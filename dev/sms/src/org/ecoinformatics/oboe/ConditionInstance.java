package org.ecoinformatics.oboe;

import java.util.List;

import org.ecoinformatics.sms.annotation.Condition;

public class ConditionInstance<T extends Comparable<T>> {
	
	/**
	 * Check whether this column satisfies all the conditions
	 * 
	 * @param attrName
	 * @param attValue
	 * @param conditionList
	 * 
	 * @return true if all the conditions are satisfies; otherwise, return false.
	 */
	public boolean ifConditionSatisfied(String attrName, T attValue, List<Condition> conditionList)
	{
		boolean isSatisfyAllCondition = true;
		for(Condition cond: conditionList){
			if(!cond.getAttribute().equals(attrName))
				continue;
			
			//this condition can apply to this column
			//FIXME: this may not be right!!!!!
			//I want something that can change condValue to the unknown T data type.
			String condValue = cond.getValue();
			
			int comp = attValue.toString().compareTo(condValue);
			
			String op = cond.getOperator();
			if(op.equals(cond.LESS_THAN)){
				if(comp>=0) isSatisfyAllCondition = false;
				break;
			}else if(op.equals(cond.GREATER_THAN)){
				if(comp<=0) isSatisfyAllCondition = false;
				break;
			}else if(op.equals(cond.EQUAL)){
				if(comp!=0) isSatisfyAllCondition = false;
				break;
			}else if(op.equals(cond.LESS_THAN_EQUAL)){
				if(comp>0) isSatisfyAllCondition = false;
				break;
			}else if(op.equals(cond.GREATER_THAN_EQUAL)){
				if(comp<0) isSatisfyAllCondition = false;
				break;
			}else if(op.equals(cond.NOT_EQUAL)){
				if(comp==0) isSatisfyAllCondition = false;
				break;
			}
		}
		
		return isSatisfyAllCondition;
	}
}
