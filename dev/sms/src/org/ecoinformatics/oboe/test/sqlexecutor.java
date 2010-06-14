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
		String sql32="SELECT distinct did FROM ((SELECT DISTINCT oi.did FROM mi_index AS mi,observation_instance AS oi,measurement_type AS mt WHERE mi.mvalue ~ '^[-]?[0-9]+' AND mi.mvalue !~ '[a-zA-Z]+' AND  (CAST(mi.mvalue AS numeric)>=78) AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic ILIKE 'm5%'))) AS tmp;";
		System.out.println("sql32:\n"+sql32);
		execute(mdb,sql32); //3374 ms = 3s
		
		//NOTE: IMPROVES with Value type change on mvalue (compare with sql3)
		String sql4 = "SELECT distinct did,record_id FROM ((SELECT DISTINCT oi.did,oi.record_id, oi.oid, mi.mvalue, mt.characteristic  FROM mi_num AS mi,observation_instance AS oi,measurement_type AS mt"+ 
		" WHERE mi.mvalue>=78 AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic = 'm5cha'))) AS tmp";
		System.out.println("sql4:\n"+sql4);
		execute(mdb,sql4); //3433 ms = 3s
		
		//NOTE: IMPROVES with when removing the projection on record_id (compare with sql4)
		String sql5 = "SELECT distinct did FROM ((SELECT DISTINCT oi.did FROM mi_num AS mi,observation_instance AS oi,measurement_type AS mt WHERE mi.mvalue>=78 AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic = 'm5cha'))) AS tmp";
		System.out.println("sql5:\n"+sql5);
		execute(mdb,sql5);
		//1.167s
		
		//NOTE: IMPROVES with when removing the projection on record_id (compare with sql4)
		String sql52 = "SELECT distinct did FROM ((SELECT DISTINCT oi.did FROM mi_num AS mi,observation_instance AS oi,measurement_type AS mt WHERE mi.mvalue=78 AND  oi.etype = 'e5' AND  oi.oid=mi.oid AND  (mt.mtypelabel = mi.mtypelabel AND mt.characteristic = 'm5cha'))) AS tmp";
		System.out.println("sql5:\n"+sql52);//5ms
		execute(mdb,sql52); 
		
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
