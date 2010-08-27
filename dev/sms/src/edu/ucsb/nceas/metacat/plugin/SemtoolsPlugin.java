package edu.ucsb.nceas.metacat.plugin;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.annotation.search.CriteriaReader;

import edu.ucsb.nceas.metacat.DBQuery;
import edu.ucsb.nceas.metacat.DocumentIdQuery;
import edu.ucsb.nceas.metacat.shared.HandlerException;

public class SemtoolsPlugin implements MetacatHandlerPlugin {

	private static List<String> supportedActions = new ArrayList<String>();
	
	static {
		supportedActions.add("semquery");
		
		//TODO: load annotations
	}
	
	public boolean handleAction(String action,
			Hashtable<String, String[]> params, HttpServletRequest request,
			HttpServletResponse response, String username, String[] groups,
			String sessionId) throws HandlerException {

		try {
			// get the semantic query xml
			String semquery = params.get("query")[0];
			
			//parse it 
			Criteria criteria = CriteriaReader.read(semquery);
			
			List<Annotation> matches = SMS.getInstance().getAnnotationManager().getMatchingAnnotations(criteria);
			
			// look up annotation matches - for the data package ids
			Vector<String> docids = new Vector<String>();
			for (Annotation annotation: matches) {
				String docid = annotation.getEMLPackage();
				docid = docid.substring(0, docid.lastIndexOf("."));
				docids.add(docid);
			}
			docids.add("knb-lter-sbc.6");
			
			//just need a valid pathquery - this is not actually used
			String squery = DocumentIdQuery.createDocidQuery(docids.toArray(new String[0]));
			String[] queryArray = new String[1];
	        queryArray[0] = squery;
	        params.put("query", queryArray);
	        
	        // now perform an squery with the given docids
	        String[] actionArray = new String[1];
	        actionArray[0] = "squery";
	        params.put("action", actionArray);
			
	        response.setContentType("text/xml");
	        PrintWriter out = response.getWriter();
	        
	        DBQuery dbQuery = new DBQuery(docids);
			dbQuery.findDocuments(response, out, params, username, groups, sessionId);
			
	        out.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException(e.getMessage());
		}
		return true;
	}

	public boolean handlesAction(String action) {
		return supportedActions.contains(action.toLowerCase());
	}

}
