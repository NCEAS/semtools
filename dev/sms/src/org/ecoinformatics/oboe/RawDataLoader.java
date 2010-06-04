package org.ecoinformatics.oboe;

import java.io.IOException;

import org.ecoinformatics.oboe.datastorage.RawDB;
import org.ecoinformatics.oboe.util.Debugger;

public class RawDataLoader {

	private static void usage()
	{
		System.out.println("Usage: ./RawDataLoader <0. data file>");
		//System.out.println("Usage: ./RawDataLoader <0. data file> [1.clean(true/false)]");
	}
	/**
	 * @param args
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, Exception {
		//if(args.length<1||args.length>2)
		if(args.length!=1)
		{
			usage();
			return;
		}
		
		String rawDataFile = Constant.localUriPrefix + args[0];
		//boolean removeTb = false;
		//if(args.length==2){
		//	removeTb = Boolean.parseBoolean(args[1]);
		//}
		
		System.out.println(Debugger.getCallerPosition()+"rawDataFile="+rawDataFile);
		
		RawDB rawDB = new RawDB();
		
		//if(!removeTb)
			rawDB.load(rawDataFile);
		//else
		//	rawDB.delete(rawDataFile);
		
		System.out.println(Debugger.getCallerPosition()+"Finish loading data");
	}

}
