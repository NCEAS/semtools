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
	
	<xsl:param name="labelWidth">120</xsl:param>
	
	<xsl:param name="showAdd">true</xsl:param>
	<xsl:param name="showRemove">true</xsl:param>	
	
	<xsl:template match="/">
		<html>
			<head>
				<title>Search Results</title>
				<link rel="stylesheet" type="text/css"
					href="/knb/style/skins/{$qformat}/{$qformat}.css" />
				<script language="Javascript" type="text/JavaScript"
					src="/knb/style/skins/{$qformat}/{$qformat}.js" />
				<script language="Javascript" type="text/JavaScript"
					src="/knb/style/common/branding.js" />
			</head>

			<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
				<script language="JavaScript">
					insertTemplateOpening('<xsl:value-of select="$contextURL" />'); insertSearchBox('<xsl:value-of select="$contextURL" />');
				</script>
				
				<div id="content_wrapper">
				
				<table style="width:100%;" align="center" border="0"
					cellpadding="5" cellspacing="0">
					<tr>
						<td align="left">
							<p class="emphasis">
								<span class="resultCount">
									<xsl:number
										value="count(resultset/document)" />
								</span>		
								total records found
							</p>
						</td>
					</tr>
				</table>

				<table align="left" border="0" cellpadding="0" cellspacing="5" width="100%">
					<tr valign="top">
							
						<!-- EML HERE  -->
						<xsl:if test="count(resultset/document[docname='eml']) &gt; 0">
							<td>
								<xsl:call-template name="emlResults"/>
							</td>	
						</xsl:if>
						
					</tr>	
				</table>
				
				<!-- render the unioned data -->				
				<xsl:if test="count(resultset/data) &gt; 0">
					<table class="subGroup subGroup_border onehundred_percent" align="center" border="0"
						cellpadding="5" cellspacing="0">
						<tr>
							<th align="left">
								<p class="emphasis">	
									Combined Data
								</p>
							</th>
						</tr>
						<tr>
							<td align="left">
								<pre>	
									<xsl:value-of select="resultset/data" />
								</pre>
							</td>
						</tr>
					</table>
				</xsl:if>	
				
				</div>
				
				<script language="JavaScript">
					insertTemplateClosing('<xsl:value-of select="$contextURL" />');
				</script>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template name="emlResults">
	
		<div id="results">
	
			<xsl:for-each
				select="resultset/document[docname='eml']">
				<xsl:sort
					select="./param[@name='dataset/title']" />
					
				<!-- the header portion of the result -->
				<div>
					<xsl:attribute name="class">
	              			<xsl:choose>
						    <xsl:when test="position() mod 2 = 1">rowodd</xsl:when>
						    <xsl:when test="position() mod 2 = 0">roweven</xsl:when>
						</xsl:choose>
					</xsl:attribute>
					
					<a href="#" class="accordian">+</a><xsl:text> </xsl:text>
					
					<!-- citation -->
					<xsl:call-template name="citation"/>
					
					<!-- edit cart -->
					<xsl:if test="$showAdd = 'true'">
						<input type="button" class="addCartButton">
							<xsl:attribute name="id">
								<xsl:text>add_</xsl:text><xsl:value-of select="replace(./docid, '\.', '_')"/>
							</xsl:attribute>
							<xsl:attribute name="onclick">
								var params = 
									{
										'action': 'editcart',
										'docid': '<xsl:value-of select="./docid"/>',
										'operation': 'add',
										'qformat': 'semtools'
									};
								load(
									'<xsl:value-of select="$contextURL" />/metacat', //url
									params 
									);
								loadCart();	
							</xsl:attribute>
							<xsl:attribute name="value">
								Add to cart
							</xsl:attribute>	
						</input>
					</xsl:if>
					<xsl:if test="$showRemove = 'true'">
						<input type="button" class="removeCartButton">
							<xsl:attribute name="id">
								<xsl:text>remove_</xsl:text><xsl:value-of select="replace(./docid, '\.', '_')"/>
							</xsl:attribute>
							<xsl:attribute name="onclick">
								var params = 
									{
										'action': 'editcart',
										'docid': '<xsl:value-of select="./docid"/>',
										'operation': 'remove',
										'qformat': 'semtools'
									};
								load(
									'<xsl:value-of select="$contextURL" />/metacat', //url
									params 
									);
								loadCart();	
							</xsl:attribute>
							<xsl:attribute name="value">
								Remove from cart
							</xsl:attribute>	
						</input>
					</xsl:if>	
				</div>
				
				<!-- the content part -->	
				<div>
					<xsl:attribute name="class">
	              		<xsl:choose>
						    <xsl:when test="position() mod 2 = 1">rowodd</xsl:when>
						    <xsl:when test="position() mod 2 = 0">roweven</xsl:when>
						</xsl:choose>
					</xsl:attribute>
					
					<table class="onehundred_percent">
						<tr>
							<td>
								<!-- keywords -->
								<xsl:call-template name="keywords"/>								
							</td>
						</tr>
						<tr>
						
							<!-- annotation section -->
							<td>
								<!-- render the annotation -->
								<xsl:for-each select="./*[local-name()='annotation']">
									<xsl:call-template name="modelSummary">
										 <xsl:with-param name="showAll" select="'false'"/>
									</xsl:call-template>	
								</xsl:for-each>
							</td>
							
						</tr>
						
						<!-- data -->
						<xsl:if test="count(./data) != 0">
							<tr>
								<td>
									<xsl:call-template name="data"/>	
								</td>
							</tr>
						</xsl:if>
						
					</table>
											
				</div>
		
			</xsl:for-each>
			
		</div>	
			
	</xsl:template>
	
	<xsl:template name="citationContainer">
		<table class="subGroup subGroup_border onehundred_percent">
			<tr>
				<td class="rowodd" width="{$labelWidth}">
					Citation:
				</td>
				<td class="roweven">
					<xsl:call-template name="citation"/>
				</td>
			</tr>		
		</table>
	</xsl:template>
	
	<xsl:template name="citation">
		<!-- the author -->
		<xsl:choose>
			<xsl:when test="count(./param[@name='creator/individualName/surName']) > 0">
				<xsl:for-each select="./param[@name='creator/individualName/surName']">
					<xsl:value-of select="." />
					<xsl:if test="position() != last()">, </xsl:if>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="./param[@name='creator/organizationName']">
					<xsl:value-of select="." />
					<xsl:if test="position() != last()">, </xsl:if>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>. </xsl:text>
		<!-- the pubdate -->
		<xsl:value-of select="substring(string(./param[@name='dataset/pubDate']),1,4)"/>
		<xsl:if test="substring(string(./param[@name='dataset/pubDate']),1,4) != ''">. </xsl:if>
		<!-- the title -->
		<b>
		<xsl:value-of select="./param[@name='dataset/title']"/>
		</b>
		<xsl:text>. </xsl:text>
		<!-- the id -->
		<xsl:value-of select="./docid"/>
		<xsl:text>. </xsl:text>
		<br/>
		<!-- the link -->
		(<a>
			<xsl:attribute name="href">
				<xsl:value-of select="$tripleURI"/><xsl:value-of select="./docid"/>
			</xsl:attribute>
			<xsl:value-of select="$contextURL" /><![CDATA[/metacat/]]><xsl:value-of select="./docid"/><![CDATA[/]]><xsl:value-of select="$qformat" />										
		</a>)
	</xsl:template>
	
	<xsl:template name="keywords">
		<!-- render the keywords -->
		<table class="subGroup subGroup_border onehundred_percent">	
			<tr>
				<td class="rowodd" width="{$labelWidth}">
					Keywords:
				</td>
				<td>
					<p>
					<xsl:for-each select="./param[@name='keyword']">
						<xsl:value-of select="." />
						<xsl:if test="position() != last()">, </xsl:if>
					</xsl:for-each>
					</p>
				</td>
			</tr>			
		</table>
	</xsl:template>
	
	<xsl:template name="data">
		<!-- render the data -->
		<table class="subGroup subGroup_border onehundred_percent">	
			<tr>
				<td class="rowodd" width="{$labelWidth}">
					Data:
				</td>
				<td>
					<pre>
						<xsl:value-of select="./data" />
					</pre>
				</td>
			</tr>			
		</table>
	</xsl:template>
	

</xsl:stylesheet>