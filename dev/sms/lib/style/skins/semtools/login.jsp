<%@ page language="java"%>
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

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>Semtools Login</title>
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
<script language="Javascript">
function submitform(formObj) {

	if (trim(formObj.elements["loginAction"].value)!="Login") {
		return true;
	}
	
	//trim username & passwd:
	var username = trim(formObj.elements["uid"].value);
	var organization  = trim(formObj.elements["organization"].value);
	var password      = trim(formObj.elements["password"].value);
	
	if (username=="") {
		alert("You must type a username. \n");
		formObj.elements["uid"].focus();
		return false;
	}
	
	if (organization=="") {
		alert("You must select an organization. \n");
		formObj.elements["organization"].focus();
		return false;
	}
	
	if (password=="") {
		alert("You must type a password. \n");
		formObj.elements["password"].focus();
		return false;
	}
	
	formObj.username.value="uid="+formObj.elements["uid"].value+",o="+formObj.elements["organization"].value+",dc=ecoinformatics,dc=org";
	return true;
}

function trim(stringToTrim) {
	return stringToTrim.replace(/^\s*/, '').replace(/\s*$/,'');
}
</script>
</head>
<body>
<script language="JavaScript">
          insertTemplateOpening("<%=CONTEXT_URL%>");
          insertSearchBox("<%=CONTEXT_URL%>");
      </script>

<table class="group group_border onehundred_percent" border="0" cellpadding="0">
	<tr>
		<th colspan="1">Login</th>
	</tr>
	<tr>
		<td>
			<p>
				Please login by entering your username, affiliation and password. Only registered users will be able to access the data cart.
			</p>
		</td>
	</tr>
	<tr>
		<td>
			<form name="loginform" id="loginform" method="post"
				action="<%=SERVLET_URL%>" target="_top"
				onsubmit="return submitform(this);">
				<input type="hidden" name="action" value="login" />
				<input type="hidden" name="username" value="" /> 
				<input type="hidden" name="qformat" value="semtools" />
	
			<table>
				<tr valign="middle">
					<td align="left" valign="middle" class="text_plain">Username:</td>
	
					<td width="173" align="right" class="text_plain"
						style="padding-top: 2px; padding-bottom: 2px;">
						<input name="uid" type="text" style="width: 140px;" value=""></td>
				</tr>
	
				<tr valign="middle">
					<td height="28" align="left" valign="middle" class="text_plain">Organization:</td>
	
					<td align="right" class="text_plain"
						style="padding-top: 2px; padding-bottom: 2px;">
						<select name="organization" style="width: 140px;">
							<option value="" selected>&#8212; choose one &#8212;</option>
							<option value="NCEAS">NCEAS</option>
							<option value="MSU">MSU</option>
							<option value="LTER">LTER</option>
							<option value="UCNRS">UCNRS</option>
							<option value="PISCO">PISCO</option>
							<option value="OBFS">OBFS</option>
							<option value="OSUBS">OSUBS</option>
							<option value="SAEON">SAEON</option>
							<option value="SANParks">SANParks</option>
							<option value="SDSC">SDSC</option>
							<option value="KU">KU</option>
							<option value="unaffiliated">unaffiliated</option>
						</select>
					</td>
				</tr>
	
				<tr valign="middle">
					<td width="85" align="left" valign="middle" class="text_plain">Password:</td>
	
					<td width="150" align="right">
						<input name="password" type="password" maxlength="50" style="width: 140px;" value="">
					</td>
				</tr>
				<tr>
					<td colspan="2" align=right class="buttonBG_login">
						<input type="submit" name="loginAction" value="Login" class="button_login">
					</td>
				</tr>
			</table>
			</form>
		</td>
	</tr>
</table>

<script language="JavaScript">          
    insertTemplateClosing("<%=CONTEXT_URL%>");
</script>
</body>
</html>
