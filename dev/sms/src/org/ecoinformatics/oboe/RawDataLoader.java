package org.ecoinformatics.oboe;

import java.io.IOException;

import org.ecoinformatics.oboe.datastorage.RawDB;

public class RawDataLoader {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, Exception {
		if(args.length!=1){
			System.out.println("Usage: ./RawDataLoader <0. data file> ");
			return;
		}
		
		String rawDataFile = Constant.localUriPrefix + args[0];
		
		System.out.println(Debugger.getCallerPosition()+"rawDataFile="+rawDataFile);
		
		RawDB rawDB = new RawDB();
		
		rawDB.load(rawDataFile);
		
		System.out.println(Debugger.getCallerPosition()+"Finish loading data");
	}

}
