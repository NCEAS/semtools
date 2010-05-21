package org.ecoinformatics.oboe.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ecoinformatics.oboe.Debugger;
import org.ecoinformatics.oboe.datastorage.MDB;

public class QueryMeasurement {

	private String characteristicCond ="";
	private String standardCond ="";
	private String valueCond ="";
	private String aggregationFunc = "";
	
	public String getCharacteristicCond() {
		return characteristicCond;
	}
	public void setCharacteristicCond(String characteristicCond) {
		this.characteristicCond = characteristicCond;
	}
	public String getStandardCond() {
		return standardCond;
	}
	public void setStandardCond(String _standardCond) {
		this.standardCond = _standardCond;
	}
	public String getValueCond() {
		return valueCond;
	}
	public void setValueCond(String _valueCond) {
		this.valueCond = _valueCond;
	}
	public String getAggregationFunc() {
		return aggregationFunc;
	}
	public void setAggregationFunc(String aggregationFunc) {
		this.aggregationFunc = aggregationFunc;
	}
	
//	public String toString(){
//		String str="";
//		if(characteristicCond!=null&&characteristicCond.trim().length()>0){
//			str+="characteristicCond= ["+characteristicCond+"];";			
//		}
//		
//		if(standardCond!=null&&standardCond.trim().length()>0){
//			str+="standardCond= ["+standardCond+"];";			
//		}
//		
//		if(valueCond!=null&&valueCond.trim().length()>0){
//			str+="valueCond= ["+valueCond+"];";			
//		}
//		
//		if(aggregationFunc!=null&&aggregationFunc.trim().length()>0){
//			str+="aggregationFunc= ["+aggregationFunc+"];";			
//		}
//		
//		return str;
//	}
	
	//tested, ok
	public String toString(){
		String str="";
		
		if(aggregationFunc!=null&&aggregationFunc.trim().length()>0){
			str += aggregationFunc+"(";			
		}
		if(characteristicCond!=null&&characteristicCond.trim().length()>0){
			str += characteristicCond;			
		}
		if(aggregationFunc!=null&&aggregationFunc.trim().length()>0){
			str += ")";			
		}
		if(valueCond!=null&&valueCond.trim().length()>0){
			str += " "+valueCond;			
		}
		if(standardCond!=null&&standardCond.trim().length()>0){
			str += " "+standardCond;			
		}
		
		return str;
	}
	
	/**
	 * Form the sql for this basic query
	 * 
	 * @param mdb
	 * @return
	 */
	private String formSQL(MDB mdb, String entityNameCond)
	{
		String sql = "SELECT DISTINCT did, record_id, mvalue";
		
		sql +=" FROM "+mdb.getMeasInstanceTable()+" AS mi,"+ mdb.getMmeasTypeTable() + " AS mt, ";
		sql += mdb.getObsTypeTable() +" AS ot ";
		
		if(valueCond!=null&&valueCond.trim().length()>0){
			sql += " WHERE ";
			
			//value condition process
			if(valueCond.contains("'")){ //string conditions has ', e.g., like 'California', = 'California'
				sql +=" mi.mvalue "+valueCond+") AND ";
			}else{//numeric conditions, e.g., >15.0
				sql += "mi.mvalue ~ " + mdb.m_DIGIT_RE +" AND mi.mvalue !~ "+mdb.m_STRING_RE+" AND ";
				sql +=" (CAST(mi.mvalue AS numeric)"+valueCond+") AND ";
			}
			
			//entity name
			if(entityNameCond.contains("%")){
				sql +=" (mt.otypelabel = ot.otypelabel AND ot.ename ILIKE "+entityNameCond+") AND ";
			}else{
				sql +=" (mt.otypelabel = ot.otypelabel AND ot.ename="+entityNameCond+") AND ";
			}
			
			//measurement type: characteristic, standard, etc.
			sql +=" (mt.mtypelabel = mi.mtypelabel";
			if(characteristicCond!=null&&characteristicCond.trim().length()>0){
				if(characteristicCond.contains("%")){
					sql += " AND mt.characteristic ILIKE " + characteristicCond+"";
				}else{
					sql += " AND mt.characteristic=" + characteristicCond+"";
				}
			}
			if(standardCond!=null&&standardCond.trim().length()>0){
				sql +=" AND mt.standard " + standardCond;
			}
			sql +=")";
			
			//aggregation
			if(aggregationFunc!=null&&aggregationFunc.length()>0){
				sql+="GROUP BY did ";
				sql+="HAVING "+aggregationFunc+"(CAST (mvalue AS numeric))"+valueCond;
			}
		}
		sql +=";";
		System.out.println(Debugger.getCallerPosition()+" SQL: \n"+sql);
		
		return sql;
	}
	
	/**
	 * Execute one query measurement
	 * 
	 * @param mdb
	 * @return
	 * @throws Exception 
	 */
	public Set<OboeQueryResult> execute(MDB mdb, String entityNameCond) 
		throws Exception
	{
		Set<OboeQueryResult> resultSet = new TreeSet<OboeQueryResult>();
		
		Connection conn = mdb.getConnection();
		if(conn==null){
			mdb.open();
			conn = mdb.getConnection();
		}
		
		//form sql query for this condition
		String sql = formSQL(mdb, entityNameCond);
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		//ResultSetMetaData rsmd = rs.getMetaData();
		//int numOfCols = rsmd.getColumnCount();
		while(rs.next()){
			OboeQueryResult queryResult = new OboeQueryResult();
			
			Long datasetId = rs.getLong(1);
			queryResult.setDatasetId(datasetId);
			
			String recordId = rs.getString(2);
			queryResult.setRecordId(recordId);
			
			resultSet.add(queryResult);
		}
		rs.close();
		stmt.close();
		
		System.out.println(Debugger.getCallerPosition()+"One QM result="+resultSet);
		return resultSet;
	}

}
