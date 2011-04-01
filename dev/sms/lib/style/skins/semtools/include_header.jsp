<%@ page    language="java" %>
<%@page import="edu.ucsb.nceas.metacat.util.AuthUtil"%>
<!--
  *  '$RCSfile$'
  *      Authors: Matt Jones, CHad Berkley
  *    Copyright: 2000 Regents of the University of California and the
  *               National Center for Ecological Analysis and Synthesis
  *  For Details: http://www.nceas.ucsb.edu/
  *
  *   '$Author$'
  *     '$Date$'
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
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
-->
<%@ include file="../../common/common-settings.jsp"%>
<%@ include file="../../common/configure-check.jsp"%>
<head>
  <title>Metacat Data Catalog - Semtools</title>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
  <link href="<%=STYLE_SKINS_URL%>/semtools/semtools.css" 
    rel="stylesheet" type="text/css">
  <script language="javascript" type="text/javascript" 
    src="<%=STYLE_SKINS_URL%>/semtools/semtools.js"></script>
  </head>

<body>
<table width="100%" height="100" class="banner" cellpadding="0" cellspacing="0" border="0">
	<tr>
		<td valign="bottom">
			<table width="1022" height="100" background="<%=STYLE_SKINS_URL%>/semtools/images/semtools_web_banner.jpg">
				<tr height="80%">
					<td>&nbsp;</td>
				</tr>
				<tr>
				    <td align="center" valign="middle" class="sectionheader">
				    	<a target="_top" href="<%=STYLE_SKINS_URL%>/semtools/index.jsp">Home</a></td>
					<td align="center" valign="middle" class="sectionheader">
						<%
							boolean loggedIn = AuthUtil.isUserLoggedIn(request);
							if (loggedIn) {
						%>
							<a href="<%=SERVLET_URL%>?action=logout&qformat=semtools" target="_top">Logout</a>
						<%
							} else {
						%>
							<a target="_top" href="<%=STYLE_SKINS_URL%>/semtools/login.jsp">Login</a>
						<%
							}
						%>
				    </td>
					<td align="center" valign="middle" class="sectionheader">
						<a target="_top" href="<%=STYLE_SKINS_URL%>/semtools/ontologies.jsp">Ontologies</a></td>
					<td align="center" valign="middle" class="sectionheader">
						<a target="_new" href="http://bioportal.bioontology.org/">BioPortal</a></td>	
				</tr>
			</table>
		</td>
	</tr>
</table>

</body>
</html>
