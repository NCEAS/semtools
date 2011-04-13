<%@ page language="java"%>
<%@page import="edu.ucsb.nceas.metacat.util.AuthUtil"%>
<%@page import="edu.ucsb.nceas.metacat.util.GeoserverUtil"%>
<%
/**
 * 
 * '$RCSfile$'
 * Copyright: 2008 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    '$Author$'
 *      '$Date$'
 * '$Revision$'
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
     
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */  
%>

<%@ include file="../../common/common-settings.jsp"%>
<%@ include file="../../common/configure-check.jsp"%>
<%
	String GEOSERVER_URL = GeoserverUtil.getGeoserverContextURL();
%>
<html>
<head>
<title>Semantic search</title>
<link rel="stylesheet" type="text/css" href="<%=STYLE_SKINS_URL%>/semtools/semtools.css">
<link rel="stylesheet" type="text/css" href="<%=STYLE_COMMON_URL%>/jquery/jqueryui/css/smoothness/jquery-ui-1.8.6.custom.css">

<script language="javascript" type="text/javascript" src="<%=STYLE_COMMON_URL%>/jquery/jquery.js"></script>
<script language="javascript" type="text/javascript" src="<%=STYLE_COMMON_URL%>/jquery/jsTree/_lib/jquery.cookie.js"></script>
<script language="javascript" type="text/javascript" src="<%=STYLE_COMMON_URL%>/jquery/jsTree/jquery.jstree.js"></script>
<script language="javascript" type="text/javascript" src="<%=STYLE_COMMON_URL%>/jquery/jqueryui/js/jquery-ui-1.8.6.custom.min.js"></script>
<script language="javascript" type="text/javascript" src="<%=STYLE_COMMON_URL%>/jquery/busy/jquery.busy.js"></script>

<script language="javascript" type="text/javascript" src="<%=STYLE_SKINS_URL%>/semtools/search.js"></script>
<script language="javascript" type="text/javascript" src="<%=STYLE_SKINS_URL%>/semtools/semtools.js"></script>
<script language="javascript" type="text/javascript" src="<%=STYLE_COMMON_URL%>/branding.js"></script>

<script language="Javascript" type="text/JavaScript"><!--
function populateActiveDomain(divId, class) {
	// collect the filtering values we have so far
	// these are hidden input fields in the form for holding the selected values
	var entity = $("#activeEntitiesValue").val() ? $("#activeEntitiesValue").val() : "";
	var characteristic = $("#activeCharacteristicsValue").val() ? $("#activeCharacteristicsValue").val() : "";
	var protocol = $("#activeProtocolsValue").val() ? $("#activeProtocolsValue").val() : "";
	var measurement = $("#activeMeasurementsValue").val() ? $("#activeMeasurementsValue").val() : "";
	
	// TODO: remember the selected value for this when filtering by active domain
	var selectedNode = $("#" + divId).jstree("get_selected", $("#" + divId));
	var selectedNodeId = $(selectedNode).attr("id");
	//alert(divId + " selected node: " + selectedNodeId);
	
	// load the tree for the given div, passing in the other filtered values
	$("#" + divId).load(
		"<%=SERVLET_URL%>", 
		{
			'action': "getactivedomain",
			'class': class,
			'entity': entity,
			'characteristic': characteristic,
			'protocol': protocol,
			'measurement': measurement
		},
		// call back function when loading finishes
		function(response, status, xhr) {
			//alert("callback for: " + divId + " selected node: " + selectedNodeId);
			// error
			if (status == "error") {
				var msg = "Sorry but there was an error: ";
				$("#error").html(msg + xhr.status + " " + xhr.statusText);
			}
 
			// make it a js tree
			$(function () {
				$("#" + divId)
					//bind calls here
					.jstree({
						"ui" : {
							"select_limit" : 1,
							"select_multiple_modifier" : "alt",
							"selected_parent_close" : "select_parent"//,
							//"initially_select" : [ selectedNodeId ]
						},
						"themes" : {
							"theme" : "default",
							"dots" : true,
							"icons" : false
						},
						//"core" : { "initially_open" : [ selectedNodeId ] },
						"search" : { "case_insensitive" : true },
						//"cookies" : { 
						//		"save_opened" : "jstree_open_" + divId,
						//		"save_selected" : "jstree_select_" + divId,
						//},
						"plugins" : [ 
							"themes", 
							"html_data", 
							"ui", 
							//"cookies", 
							"search" ]
					});
			});

			// enable searching on it
			$("#" + divId + "Search").keyup(
				function () {
					var searchTerm = $("#" + divId + "Search").val();
					if (searchTerm.length >= 3) {
						// search
						$("#" + divId).jstree("search", searchTerm);
						// now prune
						prune(divId, "jstree-search");
						// is it an exact match?
						checkExactMatch(divId, searchTerm, "jstree-search");
					}
				});
			
			// toggle the active domain prune
			$("#" + divId + "Only").click(function() {
				doActiveDomain(divId);
			});	

			// actually prune if we should
			doActiveDomain(divId);

			// open first node always
			$("#" + divId).jstree("open_node", $("#" + divId).children("ul").first().children("li").first());
			
			// open to the node to last selected
			var nodePath = $("#" + divId).jstree("get_path", $(selectedNode));
			if (nodePath) {
				for (var index = 0; index < nodePath.length; index++) {
					$("#" + divId).jstree("open_node", $("#" + nodePath[index]));
				}
				// select the original node
				$("#" + divId).jstree("select_node", $(selectedNode), false);
				$("#" + divId).jstree("refresh", $(selectedNode));
				// TODO: scroll to selected node. 
				// This is supposed to be part of jsTree 1.0-rc2 but appears to be broken
			}
			
		});
}
/**
* Prunes the given tree to inlcude the given matched class 
**/
function prune(divId, matchClass) {

	// show all nodes (reset)
	$("#" + divId).find("li").show();
	
	// done if we don't have any thing to prune
	if (!matchClass) {
		return;
	}
	
	// get all non-matched anchor tags
	var nonmatches = $("#" + divId).find("a").not("." + matchClass);
	
	// get their parent nodes
	nonmatches = $(nonmatches).parents("li");

	// are there any matches under each parent node?
	$(nonmatches).each(
		function(index) {
			// do any chidren match?
			var childMatches = $(this).find("a." + matchClass);
			if (childMatches && childMatches.length > 0) {
				return true;
			}
			// parent matches?
			var parentMatches = $(this).parents("li").children("a." + matchClass);
			if (parentMatches && parentMatches.length > 0) {
				return true;
			}
			// hide this node if no matches under it
			$(this).hide();
		});
}
function checkExactMatch(divId, searchTerm, matchClass) {
	// get the current search matches
	var matches = $("#" + divId).find("a." + matchClass);
	// get their parent nodes
	matches = $(matches).parents("li");
	// check for exact matches
	var exactMatch = $(matches).filter("#" + searchTerm).first();
	if (exactMatch && exactMatch.length == 1) {
		//alert("exactMatch: " + exactMatch);
		// select in the tree, honoring the configured selection limit
		$("#" + divId).jstree("select_node", $(exactMatch), true);
		// act as those you clicked it
		// TODO: convert to event listeners on the tree
		select($(exactMatch).children("a"));
	}
}
function doActiveDomain(divId) {
	if ($("#" + divId + "Only").is(":checked")) {
		// prune to active
		prune(divId, "bold");
	} else {
		// reset
		prune(divId, null);
	}
}
function initialize(source) {
	// we don't want to reload the source of the filtering request
	// but we do want to reload the other trees for active domains
	// we reload all of them if no source is given - first time the page loads
	if (source) {
		source = $(source).attr("id");
	}
	if (!source) {
		source = "";
	}
	if (source != 'activeEntities') {
		populateActiveDomain('activeEntities', 'org.ecoinformatics.sms.annotation.Entity');
	}
	if (source != 'activeCharacteristics') {
		populateActiveDomain('activeCharacteristics', 'org.ecoinformatics.sms.annotation.Characteristic');
	}
	if (source != 'activeProtocols') {
		populateActiveDomain('activeProtocols', 'org.ecoinformatics.sms.annotation.Protocol');
	}
	if (source != 'activeMeasurements') {
		populateActiveDomain('activeMeasurements', 'org.ecoinformatics.sms.annotation.Measurement');
	}
}
function select(item) {

	// get the selected value, stored in the title attribute of the item <a> tag
	var value = $(item).attr("title");
	//alert("value: " + value);
	
	// get the parent div so we know what kind of class it is meant to filter
	// this is "the first parent of the class 'select'"
	var parent = $(item).parents("div.select:first");
	//alert("parent: " + parent);
	
	// set the value for the hidden input value
	// the input field of class "conceptValue" will hold it, this way we don't need to know the id
	var input = $(parent).children("input.conceptValue");
	$(input).val(value);
	//alert("input: " + input);
	
	// set it in the search field
	var treeInstance = $(item).parents("div.jstree:first");
	var shortName = $(item).parent().attr("id");
	$("#" + $(treeInstance).attr("id") + "Search").val(shortName)

	// refresh the search results
	doSearch();
	
	// refresh the other trees for active domain after this filtering action
	initialize($(parent).children("div"));
}
function doSearch() {

	// get a reference
	var formObj = $("#searchForm").get(0);
	
	// set the hidden parameters based on the current state of the form
	checkSearch(formObj);
	
	// start the busy indicator
	$("#searchResults").busy(
			{
				position	: 'left', 
				offset		: -30, 
				hide		: true, 
				img			: "<%=STYLE_COMMON_URL%>/jquery/busy/busy.gif" 
			});
	
	//load the results
	$("#searchResults").load(
		"<%=SERVLET_URL%>" + " #content_wrapper",
		$(formObj).serialize(),
		// call back function when loading finishes
		function(response, status, xhr) {
			if (status == "error") {
				var msg = "Sorry but there was an error performing the search: ";
				$("#error").html(msg + xhr.status + " " + xhr.statusText);
			}
			// collapsible search results - show and hide the next div
			$(function() {
				$('#searchResults').find('.accordian').click(function() {
					var ref = $(this);
					$(this).parent().next().slideToggle(
						"slow",
						function() {
							if ($(ref).parent().next().is(":visible")) {
								$(ref).html("-");
							} else {
								$(ref).html("+");
							}
						});
					return false;
				}).parent().next().hide();
			});
			
			// stop the busy indicator
			$("#searchResults").busy("hide");
		});
		
	return false;
	
}
function loadCart() {

	// start the busy indicator
	$("#cartResults").busy(
		{
			position	: 'left', 
			offset		: -30, 
			hide		: true, 
			img			: "<%=STYLE_COMMON_URL%>/jquery/busy/busy.gif" 
		});
	
	// for looking up the cart
	var params = 
	{
		'action': 'getcart',
		'showAdd': 'false',
		'showRemove': 'true',
		'qformat': 'semtools'
	};
	//load the cart results
	$("#cartResults").load(
		"<%=SERVLET_URL%>" + " #content_wrapper",
		params,
		// call back function when loading finishes
		function(response, status, xhr) {
			if (status == "error") {
				var msg = "Sorry but there was an error performing the search: ";
				$("#error").html(msg + xhr.status + " " + xhr.statusText);
			}
			// collapsible search results - show and hide the next div
			$(function() {
				$('#cartResults').find('.accordian').click(function() {
					var ref = $(this);
					$(this).parent().next().slideToggle(
						"slow",
						function() {
							if ($(ref).parent().next().is(":visible")) {
								$(ref).html("-");
							} else {
								$(ref).html("+");
							}
						});
					return false;
				}).parent().next().hide();
			});
			
			// stop the busy indicator
			$("#cartResults").busy("hide");
			
			//set the count for the tab label
			var title = "Cart (" + $("#cartResults").find(".resultCount:first").html() + ")";
			$("#searchTabs > ul > li").last().children("a").html("<span>" + title + "</span>"); 
			
		});
	return true;
}
function clearCart() {

	// for looking up the cart
	var params = 
	{
		'action': 'editcart',
		'operation': 'clear',
		'qformat': 'semtools'
	};
	// post the cart clear
	$("#cartResults").load(
		"<%=SERVLET_URL%>",
		params,
		// call back function when loading finishes
		function(response, status, xhr) {
			if (status == "error") {
				var msg = "Sorry but there was an error clearing the cart: ";
				$("#error").html(msg + xhr.status + " " + xhr.statusText);
			}
		});
	return true;
}
function addAllToCart() {
	// press all the add cart buttons?
	// TODO: add them in a single request (the service handles multiple docids)
	$(".addCartButton").click();
}
function clearForm() {
	// remember the check boxes
	var matchAll = $('#matchAll').attr("checked");
	var strict = $('#strict').attr("checked");
	
	// clear the form values
	$('#searchForm').get(0).reset();
	// clear each of the tree selections
	$(".jstree").each(function(index) {
		$(this).jstree("deselect_all");
	});
	$("input.conceptValue").each(function(index) {
		$(this).val("");
	});
	
	// reload the trees
	initialize();
	
	// set the saved checkbox values
	$('#matchAll').attr("checked", matchAll);
	$('#strict').attr("checked", strict);
	
	// reload the search results
	//alert($('#searchForm').get(0));
	doSearch();
}
function addCurrent() {

	// make a container for this item
	var count = $("#searchCriteria").children(".searchItem").length;
	count++;
	var containerId = "searchItem_" + count;
	// ensure the containerId is unique
	while ($("#" + containerId).length > 0) {
		count++;
		containerId = "searchItem_" + count;
	}
	var container = "<div class='searchItem' id='" + containerId + "'/>";
	$("#searchCriteria").append(container);
	
	// get the current values
	$("input.conceptValue").each(function(index) {
		var title = $(this).attr("title");
		var value = $(this).val();
		var shortName = value.substr(value.lastIndexOf("#") + 1);
		var clone = $(this).clone();
		$(clone).removeClass("conceptValue");
		// put the value in the container
		$("#" + containerId).append(clone);
		$("#" + containerId).append("[" + title + " = " + shortName + "] ");
	});
	// get the current classes (for search to work correctly we need class+value for each entry)
	$("input.conceptClass").each(function(index) {
		var clone = $(this).clone();
		$(clone).removeClass("conceptClass");
		// put the class in the container
		$("#" + containerId).append(clone);
	});
	
	// get the current data values (and operator)
	$("#" + containerId).append("(");
	$(".dataClass").each(function(index) {
		var clone = $(this).clone();
		var value = $(this).val();
		var name = $(this).attr("name");
		var id = $(this).attr("id");
		
		// put the data conditions in as hidden params
		$("#" + containerId).append("<input type='hidden' name='" + name + "' id='" + id + "' value='" + value + "'");
		// with a text representation
		if (index > 0) {
			$("#" + containerId).append(" ");
		}
		$("#" + containerId).append(value);
		
	});
	$("#" + containerId).append(")");
	
	// add the remove button
	var removeButtonId = containerId + "_remove";
	$("#" + containerId).append("<input type='button' value='Remove' id='" + removeButtonId + "'/>");
	$("#" + removeButtonId).click(function() {
		// remove the container (includes the form objects we added)
		$("#" + containerId).remove();
		// refresh the search results now that they are less restrictive
		doSearch();
	});

	// clear the form of what we just saved to the criteria list
	clearForm();
}
function clearCriteria() {

	// remove all children of the criteria
	$("#searchCriteria").children().remove();

	// clear the form of any selections
	clearForm();

	// refresh the search results now that they are less restrictive
	doSearch();
}
/**
 * Perform this when the page first loads
 */
function pageLoad() {
	initialize();
	doSearch();
	loadCart();
}
function donothing() {}
--></script>

</head>
<body onload="pageLoad()">
<script language="javascript">
	insertTemplateOpening("<%=CONTEXT_URL%>");
</script>

<div id="content_wrapper">
 
<h2>Semantic search</h2>

<div id="error">
	<!-- error messages here -->
</div>

<!-- set up the tabs -->
<script>
	$(function() {
		$("#searchTabs").tabs();
		$("#searchTabs").tabs("add", "#ecpTab", "Entity, Characteristic, Protocol");
		$("#searchTabs").tabs("add", "#measurementTab", "Measurement");
		$("#searchTabs").tabs("add", "#optionsTab", "Options");
		$("#searchTabs").tabs("add", "#keywordsTab", "Keywords");
		$("#searchTabs").tabs("add", "#spatialTab", "Spatial");
		$("#searchTabs").tabs("add", "#cartTab", "Cart");
	});
</script>

<form method="POST" 
		action="<%=SERVLET_URL%>" 
		target="_top" 
		id="searchForm" 
		name="searchForm" 
		onSubmit="return doSearch()">
	<input name="semquery" type="hidden" />
	<input name="query" type="hidden" />
	<input name="qformat" value="semtools" type="hidden" />
	<input name="includeHeader" value="false" type="hidden" />
	<input name="useUnion" value="true" type="hidden" />
	<input name="showAdd" value="<%=AuthUtil.isUserLoggedIn(request)%>" type="hidden" />
	<input name="showRemove" value="false" type="hidden" />
	<input name="action" value="semquery" type="hidden" />

	<!-- tabs for the search interface -->		
	<div id="searchTabs">
		<!-- place holder for ui tabs -->
		<ul></ul>
	
		<!-- other criteria tabs -->
		<div id="ecpTab">
			<table>
				<tr>
					<td>
						<table class="subGroup subGroup_border">
							
							<tr>
								<th><p>Find observations of</p></th>
							</tr>
							<tr>
								<td>
									<input type="text" id="activeEntitiesSearch" />
									<input type="checkbox" id="activeEntitiesOnly" title="Show only active concepts" />
									<div class="select">
										<div id="activeEntities" class="activeTree">
											<p>loading...</p>
										</div>
										<input type="hidden" class="conceptValue" name="activeEntitiesValue" id="activeEntitiesValue" title="Entity"/>
										<input type="hidden" class="conceptClass" name="activeEntitiesClass" id="activeEntitiesClass" value="http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#Entity"/>
									</div>
								</td>
							</tr>
						</table>
					</td>
					<td>
						<table class="subGroup subGroup_border">
							<tr>
								<th><p>with measurements of</p></th>
							</tr>
							<tr>
								<td>
									<input type="text" id="activeCharacteristicsSearch" />
									<input type="checkbox" id="activeCharacteristicsOnly" title="Show only active concepts" />
									<div class="select">
										<div id="activeCharacteristics" class="activeTree">
											<p>loading...</p>
										</div>
										<input type="hidden" class="conceptValue" name="activeCharacteristicsValue" id="activeCharacteristicsValue" title="Characteristic"/>
										<input type="hidden" class="conceptClass" name="activeCharacteristicsClass" id="activeCharacteristicsClass" value="http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#Characteristic"/>
									</div>
									
								</td>
							</tr>
							<tr>
								<th>
									<p>and data values</p>
								</th>
							</tr>
							<tr>
								<td>
									<div id="characteristicData">
										<select id="dataOperator" name="dataOperator" class="dataClass" onchange="doSearch()">
											<option value="EQUALS">=</option>
											<option value="NOT EQUALS">!=</option>

											<option value="LESS THAN OR EQUALS">&lt;=</option>
											<option value="LESS THAN">&lt;</option>
											<option value="GREATER THAN OR EQUALS">&gt;=</option>
											<option value="GREATER THAN">&gt;</option>

											<option value="LIKE">like</option>
											<option value="NOT LIKE">not like</option>
										</select>
										<input type="text" id="dataValue" name="dataValue" class="dataClass"/>
									</div>
								</td>
							</tr>
						</table>
					</td>
					<td>
						<table class="subGroup subGroup_border">
							<tr>
								<th><p>using procedures outlined by</p></th>
							</tr>
							<tr>
								<td>
									<input type="text" id="activeProtocolsSearch" />
									<input type="checkbox" id="activeProtocolsOnly" title="Show only active concepts" />
									<div class="select">
										<div id="activeProtocols" class="activeTree">
											<p>loading...</p>
										</div>
										<input type="hidden" class="conceptValue" name="activeProtocolsValue" id="activeProtocolsValue" title="Protocol"/>
										<input type="hidden" class="conceptClass" name="activeProtocolsClass" id="activeProtocolsClass" value="http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#Protocol"/>
									</div>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>

			<!-- collected search criteria here -->
			<table class="subGroup subGroup_border onehundred_percent">
				<tr>
					<th>
						<p>
						Search criteria
						<input type="button" value="Add selected criteria" onclick="addCurrent()"/>
						<input type="button" value="Remove all" onclick="clearCriteria()"/>
						</p>
					</th>
				</tr>
				<tr>
					<td>
						<div id="searchCriteria">
						</div>
					</td>
				</tr>
			</table>

		</div>
		
		<!-- measurement -->
		<div id="measurementTab">
			<table class="subGroup subGroup_border onehundred_percent">
				
				<tr>
					<th><p>a template that defines Entity, Characteristic, Standard, and/or Protocol</p></th>
				</tr>
				
				<tr>
					<td>
						<input type="text" id="activeMeasurementsSearch" />
						Only active? <input type="checkbox" id="activeMeasurementsOnly" title="Show only active concepts"/>
						<div class="select">
							<div id="activeMeasurements" class="activeTree" style="width: 100%">
								<p>loading...</p>
							</div>
							<input type="hidden" class="conceptValue" name="activeMeasurementsValue" id="activeMeasurementsValue" title="Measurement"/>
							<input type="hidden" class="conceptClass" name="activeMeasurementsClass" id="activeMeasurementsClass" value="http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#Measurement"/>
						</div>
					</td>
				</tr>
			</table>
		</div>
		
		<!-- query options -->
		<div id="optionsTab">
			<table class="group group_border">
				<tr>
					<th colspan="2">
						<p>
							Locate <b>data packages</b> that have been semantically annotated within the observation model by
							selecting concepts from OBOE extension ontologies
						</p>
					</th>
				</tr>
				
				<tr>
					
					<td colspan="1">
						Match All? 
						<input type="checkbox" name="matchAll" id="matchAll" checked="checked" onchange="doSearch()"/>
					</td>
				
					<td colspan="1">
						From same Observation? 
						<input type="checkbox" name="strict" id="strict" onchange="doSearch()"/>
					</td>
				</tr>
				
			</table>
		</div>

		<!-- keywords -->
		<div id="keywordsTab">
			<table class="subGroup subGroup_border onehundred_percent">
				
				<tr>
					<th><p>Standard keyword-based searching</p></th>
				</tr>
				
				<tr>
					<td>
						Search term: <input type="text" id="keywordValue" name="keywordValue" />
					</td>
				</tr>
			</table>
		</div>

		<!-- spatial -->
		<div id="spatialTab">
			<table class="subGroup subGroup_border onehundred_percent">
				
				<tr>
					<th><p>Spatial query</p></th>
				</tr>
				
				<tr>
					<td>
						<table width="100%" cellspacing="0" cellpadding="0" border="0" class="subpanel">
							<tr><td> 
								<iframe id="mapFrame" name="mapFrame" scrolling="no" frameborder="0" width="780" height="420" src="<%=STYLE_SKINS_URL%>/semtools/spatial/map.jsp"> You need iframe support </iframe>
							</td></tr>
						</table>
						xmax: <input type="text" id="xmax" name="xmax" /><br/>
						ymax: <input type="text" id="ymax" name="ymax" /><br/>
						xmin: <input type="text" id="xmin" name="xmin" /><br/>
						ymin: <input type="text" id="ymin" name="ymin" /><br/>
					</td>
				</tr>
			</table>
		</div>

		<!-- cart -->
		<div id="cartTab">
			<!--cart here -->	
			<table class="subGroup subGroup_border onehundred_percent">
				<tr>
					<th>
						<p>
						Cart 
						<%
							if (AuthUtil.isUserLoggedIn(request)) {
						%>
							<!-- <input type="button" value="Refresh" onclick="loadCart()"/> -->
							<input type="button" value="Remove all" onclick="clearCart(); loadCart()"/>
						<%
							} else {
						%>
							(<a target="_top" href="<%=STYLE_SKINS_URL%>/semtools/login.jsp">Login</a> to edit cart)
						<%
							}
						%>
						</p>
					</th>
				</tr>
				<tr>
					<td>
						<div id="cartResults">
						No items in cart
						</div>
					</td>
				</tr>
			</table>
		</div>
					
	</div>
	
	<br/>

	<!-- search results here -->	
	<table class="subGroup subGroup_border onehundred_percent">
		<tr>
			<th>
				Search Results
				<%
					if (AuthUtil.isUserLoggedIn(request)) {
				%>
					<input type="button" value="Add all to cart" onclick="addAllToCart()"/>
				<%
					}
				%>
			</th>
		</tr>
		<tr>
			<td>
				<div id="searchResults">
				No query has been specified	
				</div>
			</td>
		</tr>
	</table>	

</form>



<!-- Included default search/login -->
<% if ( PropertyService.getProperty("spatial.runSpatialOption").equals("true") ) { %>
<script language="javascript">
	insertMap("<%=CONTEXT_URL%>");
</script>
<br/>
<% } %>
  
<script language="javascript">
	insertSearchBox("<%=CONTEXT_URL%>");
	insertLoginBox("<%=CONTEXT_URL%>");	
</script>

</div>

<script language="javascript">
	insertTemplateClosing("<%=CONTEXT_URL%>");
</script>

</body>
</html>
