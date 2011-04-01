<%@ page language="java"%>
<%@page import="org.ecoinformatics.sms.ontology.bioportal.OntologyService,org.ecoinformatics.sms.ontology.bioportal.OntologyBean,java.util.List"%>
<%@page import="org.ecoinformatics.sms.SMS,org.ecoinformatics.sms.ontology.Ontology"%>
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
	// construct the drop down and the lookup key for the available ontologies
	StringBuffer dropDown = new StringBuffer();
	List<OntologyBean> beans = OntologyService.getInstance().getOntologyBeans();
	for (OntologyBean bean: beans) {

		dropDown.append("<option value='");
		dropDown.append(bean.getId());
		dropDown.append("'>");
		dropDown.append(bean.getDisplayLabel());
		dropDown.append("</option>");
		
	}
%>
<%
	//show the currently registered ontologies in metacat's plugin
	StringBuffer registeredDropDown = new StringBuffer();
	List<String> ontologies = SMS.getInstance().getOntologyManager().getOntologyIds();
	for (String uri: ontologies) {
		Ontology ontology = SMS.getInstance().getOntologyManager().getOntology(uri);
		String label = SMS.getInstance().getOntologyManager().getOntologyLabel(ontology);
		registeredDropDown.append("<option value='");
		registeredDropDown.append(uri);
		registeredDropDown.append("'>");
		registeredDropDown.append(label);
		registeredDropDown.append("</option>");
	}
%>

<html>
<head>
<title>Semantic search</title>
<link rel="stylesheet" type="text/css" href="<%=STYLE_SKINS_URL%>/semtools/semtools.css">

<script type="text/javascript">
                    	    // Set defaults for this installation
                    	    //var BP_SEARCH_SERVER = "http://oor-01.cim3.net";
                    	    //var BP_SITE = "Sandbox";
                    	    //var BP_ORG = "OOR";
                    	  </script>
<script language="Javascript" type="text/JavaScript"
	src="<%=STYLE_SKINS_URL%>/semtools/bioportal/form_complete.js">
</script>

<script language="javascript" type="text/javascript" src="<%=STYLE_SKINS_URL%>/semtools/semtools.js"></script>
<script language="javascript" type="text/javascript" src="<%=STYLE_COMMON_URL%>/branding.js"></script>

</head>
<body>
<script language="javascript">
	insertTemplateOpening("<%=CONTEXT_URL%>");
</script>

<div id="content_wrapper">

<h2>Ontology Management (Metacat)</h2>
<p>Ontologies should be registered in Metacat before they can be used in Annotation-based searches.</p>

<h3>Currently Registered Ontologies</h3>
<form method="POST" action="<%=SERVLET_URL%>" target="_top" id="existingRegistrationForm" name="existingRegistrationForm" ">
	<input name="qformat" value="semtools" type="hidden" />
	<table>
		<tr>
			<td>Action:</td>
			<td>
				<select name="action" id="action" disabled="disabled">
					<option value="registerOntology">Register Ontology</option>
					<option value="unregisterOntology" selected="selected">Unregister Ontology</option>
				</select>
			</td>
		</tr>
		<tr>
			<td>Ontology:</td>
			<td>
				<select name='id' id='id'>
					<%=registeredDropDown.toString()%>
				</select>
			</td>
		</tr>
		<tr>
			<td colspan="2"><input type="submit" value="Submit"/></td>
		</tr>
	</table>
</form>

<h3>Available BioPortal Ontologies</h3>
<form method="POST" action="<%=SERVLET_URL%>" target="_top" id="bioportalRegistrationForm" name="bioportalRegistrationForm" ">
	<input name="qformat" value="semtools" type="hidden" />
	<table>
		<tr>
			<td>Action:</td>
			<td>
				<select name="action" id="action">
					<option value="registerOntology">Register Ontology</option>
					<option value="unregisterOntology">Unregister Ontology</option>
				</select>
			</td>
		</tr>
		<tr>
			<td>Ontology:</td>
			<td>
				<select name='id' id='id'>
					<%=dropDown.toString()%>
				</select>
			</td>
		</tr>
		<tr>
			<td colspan="2"><input type="submit" value="Submit"/></td>
		</tr>
	</table>
</form>

<h3>Other Ontologies</h3>
<form method="POST" action="<%=SERVLET_URL%>" target="_top" id="registrationForm" name="registrationForm" ">
	<input name="qformat" value="semtools" type="hidden" />
	<table>
		<tr>
			<td>Action:</td>
			<td>
				<select name="action" id="action">
					<option value="registerOntology">Register Ontology</option>
					<option value="unregisterOntology">Unregister Ontology</option>
				</select>
			</td>
		</tr>
		<tr>
			<td>URI:</td>
			<td><input type="text" name="uri" id="uri" size="100" /></td>
		</tr>
		<tr>
			<td>URL:</td>
			<td><input type="text" name="url" id="url" size="100" /></td>
		</tr>
		<tr>
			<td colspan="2"><input type="submit" value="Submit"/></td>
		</tr>
	</table>
</form>

<h2>Ontology search (BioPortal)</h2>
<p>Not sure which BioPortal ontology to use?</p>
<p>Explore ontologies in BioPortal before registering them with Metacat. Use this search interface to find appropriate ontologies.</p>
<div id="bp_quick_jump"></div>
<script type="text/javascript">
    var BP_ontology_id = "all";
</script>
<script src="<%=STYLE_SKINS_URL%>/semtools/bioportal/quick_jump.js" type="text/javascript" charset="utf-8">
</script>

<h2>Tree browser (BioPortal)</h2>
<p>Explore existing ontologies in BioPortal</p>
<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
	id="OntologyTree" width="300" height="100%"
	codebase="http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab">
	<param name="movie" value="http://keg.cs.uvic.ca/ncbo/ontologytree/OntologyTree.swf" />
	<param name="quality" value="high" />
	<param name="bgcolor" value="#ffffff" />
	<param name="allowScriptAccess" value="always" />
	<param name="flashVars" value="ontology=1523&alerterrors=false&canchangeontology=true&virtual=true" />
	<embed src="http://keg.cs.uvic.ca/ncbo/ontologytree/OntologyTree.swf" quality="high" bgcolor="#ffffff"
		width="300" height="100%" name="OntologyTree" align="middle"
		play="true"
		loop="false"
		allowScriptAccess="always"
		type="application/x-shockwave-flash"
		flashVars="ontology=1523&alerterrors=false&canchangeontology=true&virtual=true"
		pluginspage="http://www.adobe.com/go/getflashplayer">
	</embed>
</object>

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
