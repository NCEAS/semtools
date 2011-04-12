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

    return true;
}