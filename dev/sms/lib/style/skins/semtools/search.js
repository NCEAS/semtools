function trim(stringToTrim) {
    return stringToTrim.replace(/^\s*/, '').replace(/\s*$/,'');
}

function checkSearch(submitFormObj) {

	var operator = "OR";
	if (submitFormObj.matchAll.checked) {
		operator = "AND";
	}
	var strict = "false";
	if (submitFormObj.strict.checked) {
		strict = "true";
	}
    submitFormObj.semquery.value = 
    	"<sq:query queryId='query.1' system='semtools' " +
    	"xmlns:sq='http://ecoinformatics.org/semQuery-1.0.0' " +
    	"xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
    	"xsi:schemaLocation='http://ecoinformatics.org/semQuery-1.0.0 semQuery.xsd '>" +
        	"<namespace prefix='sms'>http://ecoinformatics.org/sms/annotation.1.0beta1</namespace>" +
        	"<returnField>/@id</returnField>" +
        	"<returnField>/@dataPackage</returnField>" +
        	"<title>Semantic Search Example</title>" +
        	"<!-- Match all criteria, must be on same observation -->" +
        	"<" + operator + " strict='" + strict + "'>";
        	
        	// handle specific classes
        	// Entity
    		if (submitFormObj.activeEntitiesClass.value) {
    			var searchClass = submitFormObj.activeEntitiesClass.value;
        	    var searchValue = submitFormObj.activeEntitiesValue.value;
        	    if (searchValue!="") {
	        	    submitFormObj.semquery.value += 
	        	    	"<condition " +
		        		"type='" + searchClass + "' " +
		        		"concept='" + searchValue + "' " +
					"/>";
        	    }
    		} else {
	        	for (var i=0; i < submitFormObj.activeEntitiesValue.length; i++) {
	        		var searchClass = submitFormObj.activeEntitiesClass[i].value;
	        	    var searchValue = submitFormObj.activeEntitiesValue[i].value;
	        	    if (searchValue!="") {
	        	    	submitFormObj.semquery.value += 
		        	    	"<condition " +
			        		"type='" + searchClass + "' " +
			        		"concept='" + searchValue + "' " +
						"/>";
	        	    }
	        	}
    		}
        	// Characteristic
    		if (submitFormObj.activeCharacteristicsClass.value) {
    			var searchClass = submitFormObj.activeCharacteristicsClass.value;
        	    var searchValue = submitFormObj.activeCharacteristicsValue.value;
        	    var dataValue = submitFormObj.dataValue.value;
        	    var dataOperator = submitFormObj.dataOperator.value;
        	    if (searchValue != "") {
	        	    submitFormObj.semquery.value += 
	        	    	"<condition " +
		        		"type='" + searchClass + "' " +
		        		"concept='" + searchValue + "' ";
        	    	if (dataValue != "") {
	        	    	submitFormObj.semquery.value += 
	        	    		"operator='" + dataOperator + "'>" +
	        	    		dataValue +
	        	    		"</condition>";
	        	    } else {
		        	    submitFormObj.semquery.value += "/>";
	        	    }
        	    }
    		} else {
	        	for (var i=0; i < submitFormObj.activeCharacteristicsValue.length; i++) {
	        		var searchClass = submitFormObj.activeCharacteristicsClass[i].value;
	        	    var searchValue = submitFormObj.activeCharacteristicsValue[i].value;
	        	    var dataValue = submitFormObj.dataValue[i].value;
	        	    var dataOperator = submitFormObj.dataOperator[i].value;
	        	    if (searchValue != "") {
	        	    	submitFormObj.semquery.value += 
		        	    	"<condition " +
			        		"type='" + searchClass + "' " +
			        		"concept='" + searchValue + "' ";
	        	    	
	        	    	if (dataValue != "") {
		        	    	submitFormObj.semquery.value += 
		        	    		"operator='" + dataOperator + "'>" +
		        	    		dataValue +
		        	    		"</condition>";
		        	    } else {
			        	    submitFormObj.semquery.value += "/>";
		        	    }
	        	    }
	        	}
    		}	
        	// Protocol
    		if (submitFormObj.activeProtocolsValue.value) {
    			var searchClass = submitFormObj.activeProtocolsClass.value;
        	    var searchValue = submitFormObj.activeProtocolsValue.value;
        	    if (searchValue!="") {
        	    	submitFormObj.semquery.value += 
	        	    	"<condition " +
		        		"type='" + searchClass + "' " +
		        		"concept='" + searchValue + "' " +
					"/>";
        	    }
    		} else {
	        	for (var i=0; i < submitFormObj.activeProtocolsValue.length; i++) {
	        		var searchClass = submitFormObj.activeProtocolsClass[i].value;
	        	    var searchValue = submitFormObj.activeProtocolsValue[i].value;
	        	    if (searchValue!="") {
	        	    	submitFormObj.semquery.value += 
		        	    	"<condition " +
			        		"type='" + searchClass + "' " +
			        		"concept='" + searchValue + "' " +
						"/>";
	        	    }
	        	}
    		}	
        	// Measurement
    		if (submitFormObj.activeMeasurementsValue.value) {
    			var searchClass = submitFormObj.activeMeasurementsClass.value;
        	    var searchValue = submitFormObj.activeMeasurementsValue.value;
        	    if (searchValue!="") {
        	    	submitFormObj.semquery.value += 
	        	    	"<condition " +
		        		"type='" + searchClass + "' " +
		        		"concept='" + searchValue + "' " +
					"/>";
        	    }
    		} else {
	        	for (var i=0; i < submitFormObj.activeMeasurementsValue.length; i++) {
	        		var searchClass = submitFormObj.activeMeasurementsClass[i].value;
	        	    var searchValue = submitFormObj.activeMeasurementsValue[i].value;
	        	    if (searchValue!="") {
	        	    	submitFormObj.semquery.value += 
		        	    	"<condition " +
			        		"type='" + searchClass + "' " +
			        		"concept='" + searchValue + "' " +
						"/>";
	        	    }
	        	}
    		}
        	submitFormObj.semquery.value += "</" + operator + ">";
    	submitFormObj.semquery.value += "</sq:query>";
		
    //alert("query: " + submitFormObj.semquery.value);
    	
    constructPathQuery(submitFormObj);

    return true;
}

function generateQueryString(anyValue, searchFields) {
	var queryString = ""; 
	queryString += "<pathquery version='1.2'>";
	queryString += "<returndoctype>metadata</returndoctype>";
	queryString += "<returndoctype>eml://ecoinformatics.org/eml-2.1.1</returndoctype>";
	queryString += "<returndoctype>eml://ecoinformatics.org/eml-2.1.0</returndoctype>";
	queryString += "<returndoctype>eml://ecoinformatics.org/eml-2.0.1</returndoctype>";
	queryString += "<returndoctype>eml://ecoinformatics.org/eml-2.0.0</returndoctype>";
	queryString += "<returndoctype>-//ecoinformatics.org//eml-dataset-2.0.0beta6//EN</returndoctype>";
	queryString += "<returndoctype>-//ecoinformatics.org//eml-dataset-2.0.0beta4//EN</returndoctype>";
	queryString += "<returndoctype>-//NCEAS//eml-dataset-2.0//EN</returndoctype>";
	queryString += "<returndoctype>-//NCEAS//resource//EN</returndoctype>";
	
	queryString += "<returnfield>originator/individualName/surName</returnfield>";
	queryString += "<returnfield>originator/individualName/givenName</returnfield>";
	queryString += "<returnfield>originator/organizationName</returnfield>";
	queryString += "<returnfield>creator/individualName/surName</returnfield>";
	queryString += "<returnfield>creator/individualName/givenName</returnfield>";
	queryString += "<returnfield>creator/organizationName</returnfield>";
	queryString += "<returnfield>dataset/title</returnfield>";
	queryString += "<returnfield>dataset/title/value</returnfield>";
	queryString += "<returnfield>dataset/pubDate</returnfield>";
	queryString += "<returnfield>keyword</returnfield>";
	queryString += "<returnfield>keyword/value</returnfield>";
	queryString += "<returnfield>idinfo/citation/citeinfo/title</returnfield>";
	queryString += "<returnfield>idinfo/citation/citeinfo/origin</returnfield>";
	queryString += "<returnfield>idinfo/keywords/theme/themekey</returnfield>";
	
	queryString += "<querygroup operator='UNION'>";
	
	//search particular fields, or all?
	if (searchFields.length > 0) {
		for (var i = 0; i < searchFields.length; i++) {
			queryString += "<queryterm casesensitive='false' searchmode='contains'>";
			queryString += "<value>" + anyValue + "</value>";
			queryString += "<pathexpr>" + searchFields[i] +"</pathexpr>";
			queryString += "</queryterm>";
		}
	}
	else {
		queryString += "<queryterm casesensitive='false' searchmode='contains'>";
		queryString += "<value>" + anyValue + "</value>";
		queryString += "</queryterm>";
	}
	
	queryString += "</querygroup>";
	
	queryString += "</pathquery>";
		
	return queryString;
}

function constructPathQuery(submitFormObj) {

	var anyValue = submitFormObj.keywordValue.value;
	
	if (submitFormObj.matchAll.checked) {
		submitFormObj.queryMode.value = 'INTERSECT';
	} else {
		submitFormObj.queryMode.value = 'UNION';
	}
	// don't provide a query if there is no value
	if (anyValue.length == 0) {
    	submitFormObj.query.value = "";
    	submitFormObj.queryMode.value = "";
    	return true;
	}
	var searchAll = false;
	var searchFieldArray = new Array();
	if (!searchAll) {
		var counter = 0;
		//EML fields
		searchFieldArray[counter++] = "abstract/para";
		searchFieldArray[counter++] = "abstract/para/value";
		searchFieldArray[counter++] = "surName";
		searchFieldArray[counter++] = "givenName";
		searchFieldArray[counter++] = "organizationName";		
		searchFieldArray[counter++] = "title";
		searchFieldArray[counter++] = "title/value";
		searchFieldArray[counter++] = "keyword";
		searchFieldArray[counter++] = "keyword/value";
		searchFieldArray[counter++] = "para";
		searchFieldArray[counter++] = "geographicDescription";
		searchFieldArray[counter++] = "literalLayout";
		searchFieldArray[counter++] = "@packageId";
		
		//FGDC fields
		searchFieldArray[counter++] = "abstract";
		searchFieldArray[counter++] = "idinfo/citation/citeinfo/title";
		searchFieldArray[counter++] = "idinfo/citation/citeinfo/origin";
		searchFieldArray[counter++] = "idinfo/keywords/theme/themekey";
		searchFieldArray[counter++] = "placekey";
	}
    submitFormObj.query.value = generateQueryString(anyValue, searchFieldArray);
		
    //alert("query: " + submitFormObj.query.value);

    return true;
}