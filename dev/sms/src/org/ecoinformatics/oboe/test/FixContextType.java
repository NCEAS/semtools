package org.ecoinformatics.oboe.test;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;
import org.ecoinformatics.oboe.datastorage.RawDB;

public class FixContextType {

	public static void main(String[] args) throws Exception 
	{
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
				
				while(directCotypelabellist!=null&&directCotypelabellist.size()>0){
					allCoTypeLabelSet.addAll(directCotypelabellist);
				
					List<String> nextCotypelabellist = new ArrayList<String>();
					for(int j=0;j<directCotypelabellist.size();j++){
						String directCotypeLabel = directCotypelabellist.get(j);
						List<String> OneNextCotypelabellist = directOneTbOtypelabel2cotypelabel.get(directCotypeLabel);
						if(OneNextCotypelabellist!=null){
							nextCotypelabellist.addAll(OneNextCotypelabellist);
						}
					}
					
					directCotypelabellist.clear();
					directCotypelabellist.addAll(nextCotypelabellist);
				}
				List<String> allCoTypeLabelList = new ArrayList<String>();
				allCoTypeLabelList.addAll(allCoTypeLabelSet);
				newOneTbOtypelabel2cotypelabel.put(otypeLabel, allCoTypeLabelList);
			}
			
			rawdb.insertAllCT(tbId,newOneTbOtypelabel2cotypelabel,newtb,true);
			
			Map<String,List<String>> directOneTbOtypelabel2cotypelabelFalse = rawdb.retrieveDirectContextTypeLabels(tbId,oldtb,"'f'");
			rawdb.insertAllCT(tbId,directOneTbOtypelabel2cotypelabelFalse,newtb,false);
		}
	}
}
