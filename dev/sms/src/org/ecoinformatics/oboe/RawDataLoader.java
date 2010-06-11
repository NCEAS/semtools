package org.ecoinformatics.oboe;

import java.io.IOException;

import org.ecoinformatics.oboe.datastorage.RawDB;
import org.ecoinformatics.oboe.util.Debugger;

public class RawDataLoader {

	private static void usage()
	{
		System.out.println("Usage: ./RawDataLoader <0. data file> <1. dbname>");
		//System.out.println("Usage: ./RawDataLoader <0. data file> [1.clean(true/false)]");
	}
	/**
	 * @param args
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, Exception {
		
		if(args.length!=2){
			usage();
			return;
		}
		
		String rawDataFile = Constant.localUriPrefix + args[0];
		String dbname = args[1];
		
		
		System.out.println(Debugger.getCallerPosition()+"rawDataFile="+rawDataFile);
		
		//load data to the raw database
		RawDB rawDB = new RawDB(dbname);
		rawDB.load(rawDataFile);
		
		System.out.println(Debugger.getCallerPosition()+"Finish loading data");
	}

}
