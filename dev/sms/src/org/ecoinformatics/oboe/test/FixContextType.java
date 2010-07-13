package org.ecoinformatics.oboe.test;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;
import org.ecoinformatics.oboe.datastorage.RawDB;
import org.ecoinformatics.oboe.util.Debugger;

public class FixContextType {

	public static void main(String[] args) throws Exception 
	{
		if(args.length!=1){
			System.out.println(Debugger.getCallerPosition()+"Usage:./FixContextType <dbname>");
			return;
		}
		
		String dbName = args[0];
		
		RawDB rawdb = new RawDB(dbName);
		String oldtb = "context_type_raw";
		String newtb = "context_type";
		
		List<Long> allTbIdList =rawdb.getAllTbIds();
		
		for(int i=0;i<allTbIdList.size();i++){
			long tbId = allTbIdList.get(i);
			Map<String,List<String>> newOneTbOtypelabel2cotypelabel = new TreeMap<String, List<String> >();
			Map<String,List<String>> directOneTbOtypelabel2cotypelabel = rawdb.retrieveDirectContextTypeLabels(tbId,oldtb,"'t'");
			
			for(String otypeLabel: directOneTbOtypelabel2cotypelabel.keySet()){
				List<String> directCotypelabellist = directOneTbOtypelabel2cotypelabel.get(otypeLabel);
				Set<String> allCoTypeLabelSet = new TreeSet<String>();
				
				List<String> nextCotypelabellist = new ArrayList<String>();
				nextCotypelabellist.addAll(directCotypelabellist);
				
				while(nextCotypelabellist!=null&&nextCotypelabellist.size()>0){
					allCoTypeLabelSet.addAll(nextCotypelabellist);
					
					List<String> newNextCotypelabellist = new ArrayList<String>();
					
					for(int j=0;j<nextCotypelabellist.size();j++){
						String directCotypeLabel = nextCotypelabellist.get(j);
						List<String> OneNextCotypelabellist = directOneTbOtypelabel2cotypelabel.get(directCotypeLabel);
						if(OneNextCotypelabellist!=null){
							newNextCotypelabellist.addAll(OneNextCotypelabellist);
						}
					}
					
					nextCotypelabellist.clear();
					nextCotypelabellist.addAll(newNextCotypelabellist);
				}
				List<String> allCoTypeLabelList = new ArrayList<String>();
				allCoTypeLabelList.addAll(allCoTypeLabelSet);
				newOneTbOtypelabel2cotypelabel.put(otypeLabel, allCoTypeLabelList);
			}
			
			rawdb.insertAllCT(tbId,newOneTbOtypelabel2cotypelabel,newtb,true);
			
			Map<String,List<String>> directOneTbOtypelabel2cotypelabelFalse = rawdb.retrieveDirectContextTypeLabels(tbId,oldtb,"'f'");
			rawdb.insertAllCT(tbId,directOneTbOtypelabel2cotypelabelFalse,newtb,false);
		}
		
		System.out.println(Debugger.getCallerPosition()+"Finish....");
	}
}
