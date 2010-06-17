package org.ecoinformatics.oboe.test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.ecoinformatics.oboe.query.OboeQueryResult;
import org.ecoinformatics.oboe.util.Debugger;
import org.ecoinformatics.oboe.datastorage.MDB;

public class sqlexecutor {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) 
		throws Exception 
	{
		
		String dbName ="oboe_syn";
		
		MDB mdb = new MDB(dbName);
		mdb.open();
		
//		String sql1 = "SELECT distinct did,record_id FROM ((SELECT DISTINCT oi.did,oi.record_id, oi.oid, mi.mvalue, mt.characteristic  " +
//				" FROM measurement_instance AS mi,observation_instance AS oi,measurement_type AS mt"+ 
//			" WHERE mi.mvalue ~ '^[-]?[0-9]+' AND mi.mvalue !~ '[a-zA-Z]+' AND  (CAST(mi.mvalue AS numeric)>=78) AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic ILIKE 'm5%'))) AS tmp";
//		System.out.println("sql1:\n"+sql1);
//		
//		Statement stmt = mdb.getConnection().createStatement();
//		long t1 = System.currentTimeMillis();
//		stmt.executeQuery(sql1);
//		long t2 = System.currentTimeMillis();
//		System.out.println(Debugger.getCallerPosition()+"Time used (Query): "+ (t2-t1) +" ms" +" = "+ ((t2-t1)/1000) +"s\n-----------\n");
//		//Time used (Query): 5397 ms = 5s
//		stmt.close();
//		
//		String sql2 = "SELECT distinct did FROM ((SELECT DISTINCT oi.did,oi.record_id, oi.oid, mi.mvalue, mt.characteristic " +
//				" FROM measurement_instance AS mi,observation_instance AS oi,measurement_type AS mt"+ 
//		" WHERE mi.mvalue ~ '^[-]?[0-9]+' AND mi.mvalue !~ '[a-zA-Z]+' AND  (CAST(mi.mvalue AS numeric)>=78) AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic ILIKE 'm5%'))) AS tmp";
//		
//		System.out.println("sql2:\n"+sql2);
//		Statement stmt2 = mdb.getConnection().createStatement();
//		long t3 = System.currentTimeMillis();
//		stmt2.executeQuery(sql2);
//		long t4 = System.currentTimeMillis();
//		System.out.println(Debugger.getCallerPosition()+"Time used (Query): "+ (t4-t3) +" ms" +" = "+ ((t4-t3)/1000) +"s\n-----------\n");
//		//Time used (Query): 5180 ms = 5s
//		stmt2.close();
		
		//create table mi_index as (select * from measurement_instance);
		//create index mi_index_mvalue_index on mi_index(mvalue);
		//NOTE: With index on mvalue does not help a lot (compare with sql1)
		//Reason:
		//There is always a sequential scan on mi_index because of the type conversion condition
		//Seq Scan on mi_index mi  (cost=0.00..24392.00 rows=233288 width=41)
        //Filter: (((mvalue)::text ~ '^[-]?[0-9]+'::text) AND ((mvalue)::text !~ '[a-zA-Z]+'::text) AND ((mvalue)::numeric >= 78::numeric))

//		String sql3 = "SELECT distinct did,record_id FROM ((SELECT DISTINCT oi.did,oi.record_id, oi.oid, mi.mvalue, mt.characteristic " +
//				" FROM mi_index AS mi,observation_instance AS oi,measurement_type AS mt"+ 
//		" WHERE mi.mvalue ~ '^[-]?[0-9]+' AND mi.mvalue !~ '[a-zA-Z]+' AND  (CAST(mi.mvalue AS numeric)>=78) AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic ILIKE 'm5%'))) AS tmp";
//		System.out.println("sql3:\n"+sql3);
//		execute(mdb,sql3);//5321 ms = 5s
		
		//NOTE: Remove the projection on record_id helps a lot (compare with sql3) 
//		String sql32="SELECT distinct did FROM ((SELECT DISTINCT oi.did FROM mi_index AS mi,observation_instance AS oi,measurement_type AS mt WHERE mi.mvalue ~ '^[-]?[0-9]+' AND mi.mvalue !~ '[a-zA-Z]+' AND  (CAST(mi.mvalue AS numeric)>=78) AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic ILIKE 'm5%'))) AS tmp;";
//		System.out.println("sql32:\n"+sql32);
//		execute(mdb,sql32); //3374 ms = 3s
//		
//		//NOTE: IMPROVES with Value type change on mvalue (compare with sql3)
//		String sql4 = "SELECT distinct did,record_id FROM ((SELECT DISTINCT oi.did,oi.record_id, oi.oid, mi.mvalue, mt.characteristic  FROM mi_num AS mi,observation_instance AS oi,measurement_type AS mt"+ 
//		" WHERE mi.mvalue>=78 AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic = 'm5cha'))) AS tmp";
//		System.out.println("sql4:\n"+sql4);
//		execute(mdb,sql4); //3433 ms = 3s
//		
//		//NOTE: IMPROVES with when removing the projection on record_id (compare with sql4)
//		String sql5 = "SELECT distinct did FROM ((SELECT DISTINCT oi.did FROM mi_num AS mi,observation_instance AS oi,measurement_type AS mt WHERE mi.mvalue>=78 AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic = 'm5cha'))) AS tmp";
//		System.out.println("sql5:\n"+sql5);
//		execute(mdb,sql5);
//		//1.167s
//		
//		//NOTE: IMPROVES with when removing the projection on record_id (compare with sql4)
//		String sql52 = "SELECT distinct did FROM ((SELECT DISTINCT oi.did FROM mi_num AS mi,observation_instance AS oi,measurement_type AS mt WHERE mi.mvalue=78 AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic = 'm5cha'))) AS tmp";
//		System.out.println("sql5:\n"+sql52);//5ms
//		execute(mdb,sql52); 
		
		//////////////////////////////
		//////////////////////////////
		String sql61 = "SELECT DISTINCT did FROM non_agg_meas_view_basic WHERE mvalue <=5094 AND etype = 'ent-key-m5' AND characteristic='m5cha');";
		System.out.println("sql61:\n"+sql61);
		execute(mdb,sql61); 
		//Has distinct, 1369 ms
	    //Remove distinct in the definition, //939 ms
		//add "AND mt.annot_id = mi.did" in view definition //757 ms
		
		String sql62 = "SELECT DISTINCT did FROM non_agg_meas_view_onlydid WHERE mvalue <=5094 AND etype = 'ent-key-m5' AND characteristic='m5cha');";
		System.out.println("sql62:\n"+sql62);//1314 ms = 1s
		execute(mdb,sql62); 
		//Has distinct, 1314 ms = 1s
		//Remove distinct in the select clause //904 ms
		//add "AND mt.annot_id = mi.did" in view definition //658 ms
		
		//String sql63 = " SELECT DISTINCT oi.did FROM mi_numeric mi, observation_instance oi, measurement_type mt" +
		//		" WHERE oi.oid = mi.oid AND mt.mtypelabel = mi.mtypelabel AND mt.annot_id = mi.did " +
		//		" AND mvalue <=5094 AND etype = 'ent-key-m5' AND characteristic='m5cha';";
		//System.out.println("sql63:\n"+sql63);//889 ms = 0s
		//execute(mdb,sql63);
		//////////////////////////////
		//Comparison of sql61, sql62 and sql63 are the same see sqlQueryPlanView.txt
		//The result of sql62 and sql63 is similar, shows that view definition does not matter
		//////////////////////////////
		
		//This one test the table merging oi and mi
		String sql71 = " SELECT DISTINCT omi.did FROM omi_numeric as omi, measurement_type mt" +
		" WHERE mt.mtypelabel = omi.mtypelabel AND mt.annot_id = omi.did " +
		" AND mvalue <=5094 AND etype = 'ent-key-m5' AND characteristic='m5cha';";
		System.out.println("sql71:\n"+sql71); 
		execute(mdb,sql71); 
		//607ms without "mt.annot_id = omi.did", This is wrong
		//528 ms with index on mvalue
		//106 ms with index on etype

		//This one test the table merging oi, mi, and mt
		String sql72 = "SELECT DISTINCT omi.did FROM omi_numeric_full as omi WHERE mvalue <=5094 AND etype = 'ent-key-m5' AND characteristic='m5cha';";
		System.out.println("sql72:\n"+sql72); 
		execute(mdb,sql72); 
		//559 ms without index
		//105 ms with index on etype and mvalue
		
		mdb.close();
	}

	private static void execute(MDB mdb, String sql) throws SQLException
	{
		Statement stmt4 = mdb.getConnection().createStatement();
	
		long t1 = System.currentTimeMillis();
		stmt4.executeQuery(sql);
		long t2 = System.currentTimeMillis();
		System.out.println(Debugger.getCallerPosition()+"Time used (Query): "+ (t2-t1) +" ms" +" = "+ ((t2-t1)/1000) +"s\n-----------\n");
		
		stmt4.close();
	}
}
