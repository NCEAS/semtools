<?xml version="1.0"?>
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
	* This is an XSLT (http://www.w3.org/TR/xslt) stylesheet designed to
	* convert an XML file showing the resultset of a query
	* into an HTML format suitable for rendering with modern web browsers.
-->
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sms="http://ecoinformatics.org/sms/annotation.1.0beta1"
	version="1.0">
	<xsl:import href="annotation-root.xsl"/>

	<xsl:output method="html" />
	<xsl:param name="sessid" />
	<xsl:param name="qformat">semtools</xsl:param>
	<xsl:param name="enableediting">false</xsl:param>
	<xsl:param name="contextURL"/>
	<xsl:param name="attributeLabel"/>
	
	<xsl:template match="/">
	
		<html>
			<head>
				<title>Annotation Details</title>
				<script language="javascript" type="text/javascript" 
					src="{$contextURL}/style/common/jquery/jquery.js"></script>	
				<script language="Javascript" type="text/JavaScript"
					src="{$contextURL}/style/skins/{$qformat}/{$qformat}.js" />
				<script language="Javascript" type="text/JavaScript"
					src="{$contextURL}/style/common/branding.js" />
				<script language="Javascript" type="text/JavaScript"
					src="{$contextURL}/style/skins/semtools/search.js" />
				<link rel="stylesheet" type="text/css"
					src="{$contextURL}/style/skins/{$qformat}/{$qformat}.css" />		
			</head>

			<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
				<div id="content_wrapper">
												
					<!-- single attribute detail -->
					<xsl:choose>
						<xsl:when test="$attributeLabel != ''">
							
							<xsl:call-template name="attributeDetail">
								<xsl:with-param name="attributeLabel" select="$attributeLabel" />
							</xsl:call-template>
															
						</xsl:when>
						<xsl:otherwise>
						
							<script language="JavaScript">
								insertTemplateOpening('<xsl:value-of select="$contextURL" />');
							</script>
							
													
							<!-- annotation details -->
							<div class="group group_border">
								<h3>Semantic Annotation</h3>
							</div>
		
							<xsl:for-each select="/*[local-name()='annotation']">
								<xsl:call-template name="annotation">
									<xsl:with-param name="showAll" select="'true'"/>
								</xsl:call-template>
							</xsl:for-each>
							
							
							<script language="JavaScript">
								insertTemplateClosing('<xsl:value-of select="$contextURL" />');
							</script>
							
						</xsl:otherwise>
					</xsl:choose>
				</div>
			</body>
			
		</html>
		
	</xsl:template>
	
</xsl:stylesheet>