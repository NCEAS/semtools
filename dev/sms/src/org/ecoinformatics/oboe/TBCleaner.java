package org.ecoinformatics.oboe;

import java.io.IOException;

import org.ecoinformatics.oboe.datastorage.RawDB;
import org.ecoinformatics.oboe.datastorage.MDB;
import org.ecoinformatics.oboe.util.Debugger;

/**
 * To clean the content in the database of the given files's materialized data or raw data.
 * 
 * @author cao
 *
 */
public class TBCleaner {

	private static void usage(){
		System.out.println("Usage: ./TbCleaner <0. data file> <1. dbname> <2. which content to clean (0. both, 1: rawdb, 2: mdb)>");
	}
	
	public static void main(String[] args) throws IOException, Exception {
		if(args.length!=3){
			usage();
			return;
		}
		
		String dataFile = Constant.localUriPrefix + args[0];
		String dbname = args[1];
		int cleanContent = Integer.parseInt(args[2]);
		
		if(cleanContent==0){
			MDB mdb = new MDB(dbname);
			mdb.delete(dataFile);
			
			RawDB rawDB = new RawDB(dbname);
			rawDB.delete(dataFile);
		}else if(cleanContent==1){
			RawDB rawDB = new RawDB(dbname);
			rawDB.delete(dataFile);
		}else if(cleanContent==2){
			MDB mdb = new MDB(dbname);
			mdb.delete(dataFile);
		}else{
			System.out.println(Debugger.getCallerPosition()+"Wrong 2nd parameter.");
			usage();
		}
		
		
		System.out.println("\n********************\n"+Debugger.getCallerPosition()+"Finish cleaning database for file="+dataFile);
		System.out.println("********************\n");
		
	}
}
