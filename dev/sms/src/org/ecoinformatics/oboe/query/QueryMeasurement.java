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
	private String aggregationCond = "";
	
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
	public String getAggregationCond() {
		return aggregationCond;
	}
	public void setAggregationCond(String aggregationCond) {
		this.aggregationCond = aggregationCond;
	}

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
			str += " agg "+aggregationCond;
		}
		if(standardCond!=null&&standardCond.trim().length()>0){
			str += " "+standardCond;			
		}
		
		return str;
	}
	
	/**
	 * Form the sql for this basic query
	 * (did, record_id, oid)
	 * 
	 * @param mdb
	 * @return
	 * @throws Exception 
	 * @throws SQLException 
	 */
	public String formSQL(MDB mdb, String nonAggMeasSql,
			NonAggSubQueryReturn requiredReturnStru) 
	throws SQLException, Exception
	{
		String sql = "SELECT DISTINCT did";
		if(requiredReturnStru.m_include_record_id)	sql+=", record_id";
		if(requiredReturnStru.m_include_oid) sql+=", oid ";
		sql +=" FROM " + mdb.getObsInstanceTable() +" AS oi ";
		sql +=" WHERE (did, oid) IN (\n";
		String subquerySQL = formGroupByQuerySQL(mdb,nonAggMeasSql);//this need did, oid, characteristic
		sql += subquerySQL+")";
		
		
		System.out.println(Debugger.getCallerPosition()+" SQL: \n"+sql+"\n");
		return sql;
	}
	
	/**
	 * Form the sql for this basic query (with aggregate condition)
	 * 
	 * @param mdb
	 * @return
	 * @throws Exception 
	 */
	private String formSQL(MDB mdb, String nonAggQuerySql,Set<OboeQueryResult> nonAggQueryResult,boolean resultWithRid) throws Exception
	{
		String sql = "SELECT DISTINCT did";
		if(resultWithRid){
			sql +=", record_id ";
		}
		sql +=" FROM " + mdb.getObsInstanceTable() +" AS oi ";		
		sql +=" WHERE (did, oid) IN (\n";
		String subquerySQL = formAggSubQuerySQL(mdb,nonAggQuerySql,nonAggQueryResult);
		sql += subquerySQL+");";
		
		
		System.out.println(Debugger.getCallerPosition()+" SQL: \n"+sql);
		return sql;
	}
	

	
	/**
	 * For the SQL for ONE non aggregate conditions over Materialized database
	 * (did,record_id,oid,mvalue,characteristic)
	 * 
	 * @param mdb
	 * @param entityNameCond
	 * @return
	 * @throws Exception 
	 */
	public String formSQLNonAggCondOverMDBView(MDB mdb, String entityNameCond, NonAggSubQueryReturn returnStru) throws Exception
	{
		//Here, we must have DISTINCT, otherwith mdb context search cannot be finished!!!
		String sql= "SELECT DISTINCT did";
		//String sql= "SELECT did";
		if(returnStru.m_include_record_id) sql +=",record_id";
		if(returnStru.m_include_oid) sql +=	",oid";
		if(returnStru.m_include_mvalue&&(valueCond!=null&&valueCond.trim().length()>0)) 
			sql +=",mvalue";
		if(returnStru.m_include_characteristic &&(characteristicCond!=null&&characteristicCond.trim().length()>0)) 
			sql+=",characteristic";
		if(returnStru.m_include_standard&&(standardCond!=null&&standardCond.trim().length()>0)) 
			sql+=",standard";
		
		if(returnStru.m_include_record_id){ 
			sql +=" FROM " + mdb.m_nonAggMeasView+" ";
		}else{
			if(mdb.getQueryStrategy()==2||mdb.getQueryStrategy()==12){
				sql +=" FROM " + mdb.m_nonAggMeasViewBasicInitData+" ";
			}else if(mdb.getQueryStrategy()==3||mdb.getQueryStrategy()==13){
				sql +=" FROM " + mdb.m_nonAggMeasViewBasic+" ";
			}else if(mdb.getQueryStrategy()==4||mdb.getQueryStrategy()==14){
				sql +=" FROM "+mdb.m_mergedTableView +" ";
			}else if(mdb.getQueryStrategy()==5||mdb.getQueryStrategy()==15){
				sql +=" FROM "+mdb.m_mergedFullTable +" ";
			}else{
				throw new Exception ("Wrong materialization strategy...");
			}
		}
		
		//1. value condition
		if(valueCond!=null&&valueCond.trim().length()>0){
			if(mdb.getQueryStrategy()==2||mdb.getQueryStrategy()==12){
				sql += "\nWHERE CAST (mvalue AS numeric) "+valueCond; 
			}else{
				sql += "\nWHERE mvalue "+valueCond;
			}
		}
				
		//2.entity name condition
		if(entityNameCond.contains("%")) sql += " AND etype ILIKE "+ entityNameCond ;
		else sql += " AND etype = "+ entityNameCond;
		
		//3. measurement type condition: characteristic, standard, etc.
		if(characteristicCond!=null&&characteristicCond.trim().length()>0){
			if(characteristicCond.contains("%")){
				sql += " AND characteristic ILIKE " + characteristicCond+"";
			}else{
				sql += " AND characteristic=" + characteristicCond+"";
			}
		}
		if(standardCond!=null&&standardCond.trim().length()>0){
			if(standardCond.contains("%")){
				sql +=" AND standard ILIKE" + standardCond;
			}else{
				sql +=" AND standard=" + standardCond;
			}
		}
		
		return sql;
		
	}
	
	private String formAggSubQuerySQL(MDB mdb, String nonAggQuerySql,Set<OboeQueryResult> nonAggQueryResult) throws Exception
	{	
		//For the SQL for all the non aggregate conditions
		String fromTb = "";
		String sqlReturn = "";
		if(nonAggQuerySql!=null&&nonAggQuerySql.trim().length()>0)
			sqlReturn +="SELECT did, oid FROM ("+nonAggQuerySql+") AS tmp WHERE ";
		else{
			if(mdb.getQueryStrategy()==2||mdb.getQueryStrategy()==12){
				fromTb = mdb.m_nonAggMeasViewBasicInitData;
			}else if(mdb.getQueryStrategy()==3||mdb.getQueryStrategy()==13){
				fromTb = mdb.m_nonAggMeasViewBasic;
			}else if(mdb.getQueryStrategy()==4||mdb.getQueryStrategy()==14){
				fromTb = mdb.m_mergedTableView;
			}else if(mdb.getQueryStrategy()==5||mdb.getQueryStrategy()==15){
				fromTb = mdb.m_mergedFullTable;
			}else{
				throw new Exception ("Wrong materialization strategy...");
			}
			sqlReturn +="SELECT "+fromTb+".did, "+fromTb+".oid " + " FROM " + fromTb+", oi_compress";
			sqlReturn +=" WHERE "+fromTb+".oid=oi_compress.oid AND ";
		}
		
		//measurement type: characteristic
		if(characteristicCond!=null&&characteristicCond.trim().length()>0){
			if(characteristicCond.contains("%")){
				sqlReturn += " characteristic ILIKE " + characteristicCond+" ";
			}else{
				sqlReturn += " characteristic=" + characteristicCond+" ";
			}
		}
		
		//aggregation
		if(aggregationFunc!=null&&aggregationFunc.length()>0){
			sqlReturn+="\nGROUP BY "+fromTb+".did, "+fromTb+".oid ";
			sqlReturn+="\nHAVING "+aggregationFunc+"(mvalue)"+aggregationCond;
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
		String sqlReturn = "SELECT did, oid FROM ("+nonAggMeasSql+") AS tmp";
		
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
			sqlReturn+="\nGROUP BY did, oid ";
			//sqlReturn+="\nHAVING "+aggregationFunc+"(mvalue)"+valueCond;
			sqlReturn+="\nHAVING "+aggregationFunc+"(mvalue)"+aggregationCond;
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
	public Set<OboeQueryResult> executeAggQueryMDB(MDB mdb, String nonAggMeasSql, //boolean withRecordId,
			NonAggSubQueryReturn requiredReturnStru) 
		throws Exception
	{
		
		//form sql query for this condition
		String sql = formSQL(mdb, nonAggMeasSql,requiredReturnStru);
		
		Set<OboeQueryResult> resultSet = mdb.executeSQL(sql,requiredReturnStru.m_include_record_id);
		
		System.out.println(Debugger.getCallerPosition()+"One QM result="+resultSet);
		return resultSet;
	}
	
	/**
	 * Perform a non aggregate query over MDB
	 * 
	 * @param mdb
	 * @param entityNameCond
	 * @param nonAggQueryResult
	 * @param resultWithRid
	 * @return
	 * @throws Exception
	 */
	public Set<OboeQueryResult> executeAggQueryMDB(MDB mdb, String nonAggQuerySql,
			Set<OboeQueryResult> nonAggQueryResult, boolean resultWithRid) 
		throws Exception
	{
		
		//form sql query for this condition
		String sql = formSQL(mdb, nonAggQuerySql, nonAggQueryResult,resultWithRid);
		
		//execute the SQL Query
		Set<OboeQueryResult> resultSet = mdb.executeSQL(sql,resultWithRid);
		
		System.out.println(Debugger.getCallerPosition()+"One QM result="+resultSet);
		return resultSet;
	}
	
	private String getGroupByAttList(List<String> groupByAttName)
	{
		String str="";
		for(int i=0;i<groupByAttName.size();i++){
			String att = groupByAttName.get(i);
			if(i<groupByAttName.size()-1){
				str += att+",";
			}else{
				str += att;
			}
		}
		
		return str;
	}
	/**
	 * Execute aggregation query in the raw db
	 * TODO: check the consistency with the description in the paper
	 * @param rawdb
	 * @param resultWithRecord
	 * @param annotId2KeyAttrList
	 * @return
	 * @throws Exception
	 */
	public Set<OboeQueryResult> executeAggQueryRawDB(RawDB rawdb, boolean resultWithRecord, 
			Map<Long, List<String>>  annotId2KeyAttrList,
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
			List<Pair<QueryMeasurement,String> >qm2att = entry.getValue();
			
			
			//pair is <QueryMeasurement,attribute name>
			//where clause
			String whereSql = tbid2nonAggWhereclasue.get(tbId);
			
			String aggAtt = "";
			for(int i=0;i<qm2att.size();i++){
				Pair<QueryMeasurement,String> pair = qm2att.get(i);
				if(pair.getFirst().equals(this)){
					aggAtt= "'"+pair.getSecond()+"'";
					break;
				}
			}
			
			//group by clause
			List<String> groupByAttName = annotId2KeyAttrList.get(tbId);
			String groupByAttList = getGroupByAttList(groupByAttName);
			
			String sql = "";
			if(groupByAttName!=null&&groupByAttName.size()>0){
				sql = "SELECT DISTINCT "+tbId;
				if(resultWithRecord){
					sql+= ",record_id ";
				}
				sql+= " FROM " + RawDB.TB_PREFIX+tbId;
				
				//WHERE clause
				if((whereSql!=null)&&(whereSql.trim().length()>0))
					sql+= whereSql;
				
				//GROUP BY clause
				if(groupByAttName!=null&&groupByAttName.size()>0){
					sql +=" GROUP BY " +groupByAttList;
				}
			
				//HAVING clause
				sql += " HAVING " + aggregationFunc+"("+aggAtt+")" + this.aggregationCond;
				sql += ";";
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
		
		if(this.aggregationFunc!=null){
			queryString +=Constant.AGGREGATION+this.aggregationFunc+"\n";
			queryString +=Constant.AGGREGATION_COND+this.aggregationCond+"\n";
		}else{
			queryString +=Constant.AGGREGATION+"\n";
			queryString +=Constant.AGGREGATION_COND+"\n";
		}
		
		queryString+=Constant.DNFNO+dnfno+"\n";
		queryString +=Constant.MEASUREMENT_END+"\n";
		return queryString; 
	}
	
}
