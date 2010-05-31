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
import org.ecoinformatics.oboe.datastorage.RawDB;
import org.ecoinformatics.oboe.util.Pair;

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
		String sql = "SELECT DISTINCT did, record_id FROM " + mdb.getObsInstanceTable() +" AS oi ";
		sql +=" WHERE (did, eid) IN (\n";
		String subquerySQL = formSubQuerySQL(mdb,entityNameCond);
		sql += subquerySQL+");";
		
		
		System.out.println(Debugger.getCallerPosition()+" SQL: \n"+sql);
		return sql;
	}
	
	/**
	 * Form a sub query to get the (did, eid) set that satisfies the condition
	 * 
	 * @param mdb
	 * @param entityNameCond
	 * @return
	 */
	private String formSubQuerySQL(MDB mdb, String entityNameCond)
	{	
		
		String sqlReturn = "";
		String sql = "SELECT DISTINCT oi.did, oi.eid, mi.mvalue ";
		sql +=" FROM "+mdb.getMeasInstanceTable()+" AS mi,"
			+ mdb.getObsInstanceTable() +" AS oi,"
			+ mdb.getEntityInstanceTable() +" AS ei,"
			+ mdb.getMmeasTypeTable() + " AS mt ";
			//+ mdb.getObsTypeTable() +" AS ot ";
		
		if(valueCond!=null&&valueCond.trim().length()>0){
			sql += "\nWHERE ";
			
			//value condition process
			if(valueCond.contains("'")){ //string conditions has ', e.g., like 'California', = 'California'
				sql +=" mi.mvalue "+valueCond+") AND ";
			}else{//numeric conditions, e.g., >15.0
				sql += "mi.mvalue ~ " + mdb.m_DIGIT_RE +" AND mi.mvalue !~ "+mdb.m_STRING_RE+" AND ";
				sql +=" (CAST(mi.mvalue AS numeric)"+valueCond+") AND ";
			}
			
			//entity name
			if(entityNameCond.contains("%")){
				//sql +=" (mt.otypelabel = ot.otypelabel AND ot.ename ILIKE "+entityNameCond+") AND ";
				sql += " ei.etype ILIKE "+ entityNameCond +" AND ";
			}else{
				//sql +=" (mt.otypelabel = ot.otypelabel AND ot.ename="+entityNameCond+") AND ";
				sql += " ei.etype = "+ entityNameCond +" AND ";
			}
			
			sql +="ei.eid=oi.eid AND oi.oid=mi.oid AND ";
			
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
			
			
			sqlReturn = "SELECT did, eid FROM ("+sql+") AS tmp";
			
					
			//aggregation
			if(aggregationFunc!=null&&aggregationFunc.length()>0){
				sqlReturn+="\nGROUP BY did, eid ";
				//sql+="\nHAVING "+aggregationFunc+"(CAST (mvalue AS numeric))"+valueCond;
				sqlReturn+="\nHAVING "+aggregationFunc+"(mvalue)"+valueCond;
			}
		}
		//sql +=";";
		return sqlReturn;
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
	
	/**
	 * Execute aggregation query in the raw db
	 * 
	 * @param rawdb
	 * @param resultWithRecord
	 * @param annotId2KeyAttrList
	 * @return
	 * @throws Exception
	 */
	public Set<OboeQueryResult> executeAggQueryRawDB(RawDB rawdb, boolean resultWithRecord, Map<Long, 
			List<String>>  annotId2KeyAttrList,
			Map<Long, String> tbid2nonAggWhereclasue) throws Exception
	{
		Set<OboeQueryResult> result = new TreeSet<OboeQueryResult>();
		
		if((this.aggregationFunc==null)||(aggregationFunc.trim().length()==0)){
			throw new Exception ("executeAggQueryRawDB aggregationFunc="+aggregationFunc);
		}
		if((this.valueCond==null)||(valueCond.trim().length()==0)){
			throw new Exception ("executeAggQueryRawDB valueCond="+valueCond);
		}
		
		String cha = this.characteristicCond; 
		
		Map<Long, List<Pair<QueryMeasurement,String>>> tb2Attribute = rawdb.retrieveOneTbAttribute(cha,this);
		
		for(Map.Entry<Long, List<Pair<QueryMeasurement,String> >> entry: tb2Attribute.entrySet()){
			Long tbId = entry.getKey();
			
			//pair is <QueryMeasurement,attribute name>
			//List<Pair<QueryMeasurement,String>> chaAttributeNamePairList= entry.getValue();
			
			//where clause
			String whereSql = tbid2nonAggWhereclasue.get(tbId);
			
			//group by clause
			List<String> groupByAttName = annotId2KeyAttrList.get(tbId);
			String sql = "";
			
			//TODO: HP simplify this part of codes
			if(groupByAttName!=null&&groupByAttName.size()>0){
				sql = "SELECT DISTINCT "+tbId + ",";
				if(resultWithRecord){
					sql+= "record_id ";
				}
				sql+= " FROM " + rawdb.TB_PREFIX+tbId;
				
				if(whereSql.trim().length()>0)
					sql+=whereSql +" AND (";
				else
					sql+= " WHERE (";
				for(int i=0;i<groupByAttName.size();i++){
					String att = groupByAttName.get(i);
					if(i<groupByAttName.size()-1){
						sql += att+",";
					}else{
						sql += att;
					}
				}
				sql+= ") IN (\n";
				sql+= " SELECT DISTINCT ";
			
				for(int i=0;i<groupByAttName.size();i++){
					String att = groupByAttName.get(i);
					if(i<groupByAttName.size()-1){
						sql += att+","; 
					}else{
						sql += att;
					}
				}
			
			
				//sql += aggregationFunc+"("+this.characteristicCond+") ";
				sql += " FROM " + rawdb.TB_PREFIX+tbId;
				if(whereSql.trim().length()>0)
					sql+=whereSql+" ";
				if(groupByAttName!=null&&groupByAttName.size()>0){
					sql +=" GROUP BY " + tbId ;
					for(int i=0;i<groupByAttName.size();i++){
						String att = groupByAttName.get(i);
						sql += "," + att;
					}
				}
			
				//HAVING clause
				sql += " HAVING " + aggregationFunc+"("+this.characteristicCond+")" + this.valueCond;
				sql += ");";
			}else{
				//TODO:HP need to be tested
				sql = "SELECT DISTINCT "+tbId + ",";
				if(resultWithRecord){
					sql+= "record_id ";
				}
				sql+= " FROM " + rawdb.TB_PREFIX+tbId;
				if(whereSql.trim().length()>0)
					sql+=whereSql;
				//Aggregation function should not be in where clause
				//sql += " WHERE " + aggregationFunc+"("+this.characteristicCond+")" + this.valueCond+";";
			}
			
			//3. execute sql
			System.out.println(Debugger.getCallerPosition()+"sql= "+sql);
			Set<OboeQueryResult> oneTbResult = rawdb.dataQuery(sql);
			if(oneTbResult!=null&&oneTbResult.size()>0){
				result.addAll(oneTbResult);
			}
		}
		
		return result;
	}

}
