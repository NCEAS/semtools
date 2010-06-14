package org.ecoinformatics.oboe.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ecoinformatics.oboe.datastorage.MDB;
import org.ecoinformatics.oboe.datastorage.RawDB;
import org.ecoinformatics.oboe.datastorage.PostgresDB;

import org.ecoinformatics.oboe.util.Debugger;
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
	 * (did, record_id, eid, oid)
	 * 
	 * @param mdb
	 * @return
	 * @throws Exception 
	 * @throws SQLException 
	 */
	public String formSQL(MDB mdb, String nonAggMeasSql) throws SQLException, Exception
	{
		String sql = "SELECT DISTINCT did, record_id, eid, oid FROM " + mdb.getObsInstanceTable() +" AS oi ";
		sql +=" WHERE (did, eid) IN (\n";
		String subquerySQL = formGroupByQuerySQL(mdb,nonAggMeasSql);
		sql += subquerySQL+")";
		
		
		System.out.println(Debugger.getCallerPosition()+" SQL: \n"+sql+"\n");
		return sql;
	}
	
	/**
	 * Form the sql for this basic query
	 * 
	 * @param mdb
	 * @return
	 */
	private String formSQL(MDB mdb, String entityNameCond,Set<OboeQueryResult> nonAggQueryResult)
	{
		String sql = "SELECT DISTINCT did, record_id FROM " + mdb.getObsInstanceTable() +" AS oi ";
		sql +=" WHERE (did, eid) IN (\n";
		String subquerySQL = formSubQuerySQL(mdb,entityNameCond,nonAggQueryResult);
		sql += subquerySQL+");";
		
		
		System.out.println(Debugger.getCallerPosition()+" SQL: \n"+sql);
		return sql;
	}
	
	/**
	 * For the SQL for ONE non aggregate conditions over Materialized database
	 * (did,record_id,oid,eid,mvalue,characteristic)
	 * 
	 * @param mdb
	 * @param entityNameCond
	 * @param qm
	 * @return
	 */
	public String formSQLNonAggCondOverMDB(MDB mdb, String entityNameCond)
	{
		//String sql= "SELECT DISTINCT oi.did,eic.compressed_record_id as record_id, oi.eid, oi.oid, mi.mvalue, mt.characteristic ";
		String sql= "SELECT DISTINCT oi.did,eic.compressed_record_id as record_id, oi.oid, mi.mvalue, mt.characteristic ";
		sql +=" FROM "+mdb.getMeasInstanceTable()+" AS mi,"
							+ mdb.getObsInstanceTable() +" AS oi,"
							//+ mdb.getEntityInstanceTable() +" AS ei,"
							+ mdb.getMmeasTypeTable() + " AS mt," 
							+ mdb.m_entityInstanceCompressTable +" AS eic ";
		if(valueCond!=null&&valueCond.trim().length()>0){
			sql += "\nWHERE ";
			
			//value condition process
			if(valueCond.contains("'")){ //string conditions has ', e.g., like 'California', = 'California'
				sql +=" mi.mvalue "+valueCond+" AND ";
			}else{//numeric conditions, e.g., >15.0
				sql += "mi.mvalue ~ " + mdb.m_DIGIT_RE +" AND mi.mvalue !~ "+mdb.m_STRING_RE+" AND ";
				sql +=" (CAST(mi.mvalue AS numeric)"+valueCond+") AND ";
			}
		}
		
		//entity name
		if(entityNameCond.contains("%")){
			sql += " oi.etype ILIKE "+ entityNameCond +" AND ";
		}else{
			sql += " oi.etype = "+ entityNameCond +" AND ";
		}
		
		sql +=" oi.oid=mi.oid AND ";
			
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
			if(standardCond.contains("%")){
				sql +=" AND mt.standard ILIKE" + standardCond;
			}else{
				sql +=" AND mt.standard=" + standardCond;
			}
		}
		//TOTO: HP check
		//sql +=" AND ei.eid=eic.eid AND ei.did=eic.did";
		sql +=")";
		
		return sql;
	}
	
	
	private String formSubQuerySQL(MDB mdb, String entityNameCond,Set<OboeQueryResult> nonAggQueryResult)
	{	
		
		//FIXME: HP: this second function is not finished yet. TO finish this as the second method.
		//For the SQL for all the non aggregate conditions
		//String sqlReturn = "SELECT did, eid FROM ("+sqlNonAggCond+") AS tmp";
		String sqlReturn="";
		//measurement type: characteristic
		if(characteristicCond!=null&&characteristicCond.trim().length()>0){
			if(characteristicCond.contains("%")){
				sqlReturn += " WHERE characteristic ILIKE " + characteristicCond+" ";
			}else{
				sqlReturn += " WHERE characteristic=" + characteristicCond+" ";
			}
		}
		
		//aggregation
		if(aggregationFunc!=null&&aggregationFunc.length()>0){
			sqlReturn+="\nGROUP BY did, eid ";
			//sql+="\nHAVING "+aggregationFunc+"(CAST (mvalue AS numeric))"+valueCond;
			sqlReturn+="\nHAVING "+aggregationFunc+"(mvalue)"+valueCond;
		}
		return sqlReturn;
	}
		
	/**
	 * Form a sub query to get the (did, eid) set that satisfies the condition
	 * 
	 * @param mdb
	 * @param entityNameCond
	 * @return
	 * @throws Exception 
	 * @throws SQLException 
	 */
	private String formGroupByQuerySQL(MDB mdb, String nonAggMeasSql) 
		throws SQLException, Exception
	{	
		
		//For the SQL for all the non aggregate conditions
		//String sqlNonAggCond = OMQueryBasic.formSqlOneCNFNonAggregateMDB(mdb,entityNameCond,nonAggMeas);
//		for(int i=0;i<nonAggMeas.size();i++){
//			QueryMeasurement qm = nonAggMeas.get(i); //get 
//			//(did,eid,mvalue,characteristic
//			String oneNonAggCondSql = qm.formSQLNonAggCondOverMDB(mdb,entityNameCond);
//			if(i==0){
//				sqlNonAggCond = "("+oneNonAggCondSql+")";
//			}else{
//				sqlNonAggCond = "INTERSECT \n("+oneNonAggCondSql+")";
//			}
//		}
//		
//		System.out.println(Debugger.getCallerPosition()+"sqlNonAggCond:\n"+sqlNonAggCond);
		String sqlReturn = "SELECT did, eid FROM ("+nonAggMeasSql+") AS tmp";
		
		//measurement type: characteristic
		if(characteristicCond!=null&&characteristicCond.trim().length()>0){
			if(characteristicCond.contains("%")){
				sqlReturn += " WHERE characteristic ILIKE " + characteristicCond+" ";
			}else{
				sqlReturn += " WHERE characteristic=" + characteristicCond+" ";
			}
		}
		
		//aggregation
		if(aggregationFunc!=null&&aggregationFunc.length()>0){
			sqlReturn+="\nGROUP BY did, eid ";
			sqlReturn+="\nHAVING "+aggregationFunc+"(mvalue)"+valueCond;
		}
		
		System.out.println(Debugger.getCallerPosition()+"Group by SQL:\n"+sqlReturn+"\n");

		return sqlReturn;
	}
	
	
	
	/**
	 * Execute one query aggregate query condition 
	 * 
	 * @param mdb
	 * @return
	 * @throws Exception 
	 */
	public Set<OboeQueryResult> executeAggQueryMDB(MDB mdb, String nonAggMeasSql) 
		throws Exception
	{
		
		//form sql query for this condition
		String sql = formSQL(mdb, nonAggMeasSql);
		
		Set<OboeQueryResult> resultSet = mdb.executeSQL(sql);
		
		System.out.println(Debugger.getCallerPosition()+"One QM result="+resultSet);
		return resultSet;
	}
	
	public Set<OboeQueryResult> executeAggQueryMDB(MDB mdb, String entityNameCond,
			Set<OboeQueryResult> nonAggQueryResult) 
		throws Exception
	{
		
		//form sql query for this condition
		//String sql = formSQL(mdb, entityNameCond, nonAggMeas);
		String sql = formSQL(mdb, entityNameCond, nonAggQueryResult);
		
		Set<OboeQueryResult> resultSet = mdb.executeSQL(sql);
		
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


	/**
	 * Form a query string (to put to query file) from this QueryMeasurement
	 * 
	 * @return
	 */
	public String formQueryString(int qmno, int dnfno)
	{
		String queryString = "";
		queryString +=Constant.MEASUREMENT_START+qmno+"\n";
		
		if(this.characteristicCond!=null) queryString +=Constant.CHARACTERISTIC+this.characteristicCond+"\n";
		else queryString +=Constant.CHARACTERISTIC+"\n";
		
		if(this.standardCond!=null) queryString +=Constant.STANDARD+this.standardCond+"\n";
		else queryString +=Constant.STANDARD+"\n";
		
		if(this.valueCond!=null) queryString +=Constant.COND+this.valueCond+"\n";
		else queryString +=Constant.COND+"\n";
		
		if(this.aggregationFunc!=null)queryString +=Constant.AGGREGATION+this.aggregationFunc+"\n";
		else queryString +=Constant.AGGREGATION+"\n";
		
		queryString+=Constant.DNFNO+dnfno+"\n";
		queryString +=Constant.MEASUREMENT_END+"\n";
		return queryString; 
	}
	
}
