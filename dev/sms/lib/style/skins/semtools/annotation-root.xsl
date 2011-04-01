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
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	version="2.0">

	<xsl:output method="html" />
	<xsl:param name="sessionid" />
	<xsl:param name="qformat">semtools</xsl:param>
	<xsl:param name="enableediting">false</xsl:param>
	<xsl:param name="contextURL"/>
	<xsl:param name="tripleURI"><xsl:value-of select="$contextURL" /><![CDATA[/metacat?action=read&qformat=]]><xsl:value-of select="$qformat" /><![CDATA[&sessionid=]]><xsl:value-of select="$sessionid" /><![CDATA[&docid=]]></xsl:param>
	<xsl:param name="labelWidth">120</xsl:param>
	<xsl:param name="showEntity">false</xsl:param>
	
	<xsl:key name="mapping" match="//*[local-name()='map']" use="concat(../@id, @measurement)" />
	<xsl:key name="attributes" match="//*[local-name()='map']" use="concat(../@id, @attribute)" />
	<xsl:key name="measurements" match="//*[local-name()='measurement']" use="concat(../../@id, @label)" />
	
	<xsl:template name="annotation">
		<xsl:param name="showAll">true</xsl:param>
		<table align="center" width="100%" class="subGroup">
			<tr>
				<td>
					<!-- annotation summary -->
					<xsl:call-template name="annotationSummary" />
				</td>
			</tr>
			
			<xsl:choose>
			
				<xsl:when test="$showAll='true'">
				
					<!-- render the full observations and measurements -->
					<xsl:for-each select="./*[local-name()='observation']">
						<tr>
							<td>
								<table class="subGroup subGroup_border onehundred_percent">
									<tr valign="top">
										<td>
											<!-- observation entity -->
											<xsl:call-template name="observation" />
										</td>
									</tr>
									<tr>
										<td>
											<table class="onehundred_percent">
												<tr>
													<!-- measurement -->
													<xsl:for-each select="./*[local-name()='measurement']">
														<td>
															<xsl:call-template name="measurement" />
														</td>	
													</xsl:for-each> 
												</tr>
											</table>			
										</td>	
									</tr>
								</table>
							</td>		
						</tr>
					</xsl:for-each><!-- end observation -->
						
				</xsl:when><!--end show all-->	
				<xsl:otherwise>
					<tr>
						<td>
							<xsl:call-template name="modelSummary" />
						</td>
					</tr>		
				</xsl:otherwise>
			</xsl:choose>
			
			<tr class="searchresultsdivider">
				<td></td>
			</tr>
		</table>		
		
	</xsl:template>
	
	<xsl:template name="annotationSummary">
		<table class="subGroup subGroup_border onehundred_percent">
			<tr>
				<th colspan="2">
     				Annotation
     				<a>
						<xsl:attribute name="href">
							<xsl:value-of select="$tripleURI"/><xsl:value-of select="./@id"/>
						</xsl:attribute>
						(<xsl:value-of select="./@id"/>)
					</a>
										
					<!-- stats -->
					<span id="annotationStats"></span>
					<script language="JavaScript">
						loadStats(
							'annotationStats', 
							'<xsl:value-of select="./@id" />', 
							'<xsl:value-of select="$contextURL" />/metacat',
							'<xsl:value-of select="$qformat" />');
					</script>
				</th>	
			</tr>
			<tr>
				<xsl:variable name="mapCount">
					<xsl:value-of select="count(./*[local-name()='map'])"/>
				</xsl:variable>
				<!-- reference to the annotation parent -->
				<xsl:variable name="thisAnnotationId">
					<xsl:value-of select="./@id"/>
				</xsl:variable>
				<!-- reference to the dataPackage parent -->
				<xsl:variable name="thisDataPackage">
					<xsl:value-of select="./@dataPackage"/>
				</xsl:variable>
				<td class="rowodd" width="{$labelWidth}">
					Data Object  
					(<xsl:value-of select="$mapCount"/>
		     		<xsl:text> attributes</xsl:text>):
     			</td>
     			<td class="roweven">
     				<xsl:for-each select="./*[local-name()='map']">
     					<a>
							<xsl:attribute name="href">
								<xsl:value-of select="$tripleURI"/><xsl:value-of select="$thisDataPackage"/>&amp;displaymodule=entity&amp;entitytype=dataTable&amp;entityindex=<xsl:value-of select="number(./@dataObject)+1"/>&amp;annotationId=<xsl:value-of select="$thisAnnotationId"/>
							</xsl:attribute>
     						<xsl:value-of select="./@attribute"/>
						</a>
     					<xsl:if test="position() != last()">, </xsl:if>
     				</xsl:for-each>
     				
     			</td>
     		</tr>
			<tr>
				<td class="rowodd" width="{$labelWidth}">
     				Data Package:
     			</td>
     			<td class="roweven">
     				<xsl:value-of select="./@dataPackage"/>
     				(<a>
						<xsl:attribute name="href">
							<xsl:value-of select="$tripleURI"/><xsl:value-of select="./@dataPackage"/>
						</xsl:attribute>
						<xsl:text>View Metadata</xsl:text>
					</a>)
											
					<!-- stats -->
					<span id="emlStats"></span>
					<script language="JavaScript">
						loadStats(
							'emlStats', 
							'<xsl:value-of select="./@dataPackage" />', 
							'<xsl:value-of select="$contextURL" />/metacat',
							'<xsl:value-of select="$qformat" />');
					</script>
						
				</td>
     		</tr>
     	</table>
	</xsl:template>
	
	<xsl:template name="observation">
		<table class="onehundred_percent">
			<tr>
				<th colspan="2">
					Observation (<xsl:value-of select="./@label"/>)
				</th>
			</tr>
			<tr>
				<td class="rowodd" width="{$labelWidth}">
					Entity:
				</td>
				<td class="roweven">
					<p>
						<xsl:attribute name="title">
							<xsl:value-of select="fn:namespace-uri-for-prefix(substring-before(./*[local-name()='entity']/@id, ':'), .)"/>
							<xsl:text>#</xsl:text>
							<xsl:value-of select="substring-after(./*[local-name()='entity']/@id, ':')"/>
						</xsl:attribute> 
						<xsl:value-of select="substring-after(./*[local-name()='entity']/@id, ':')"/>
					</p>	
				</td>
			</tr>
			
			<tr>		
				<td class="rowodd" width="{$labelWidth}">
					Ontology:
				</td>
				<td class="roweven">	 
					<xsl:value-of select="fn:namespace-uri-for-prefix(substring-before(./*[local-name()='entity']/@id, ':'), .)"/>
				</td>
			</tr>
			
		</table>
	
	</xsl:template>
	
	<xsl:template name="measurement">
		<!-- measurement -->
		<table class="subGroup subGroup_border onehundred_percent">
			<tr>
				<th colspan="2"> 	
					Measurement (<xsl:value-of select="./@label" />)
				</th>
			</tr>
			<xsl:choose>
				<xsl:when test="$showEntity = 'true'">
					<tr>
						<td class="rowodd" width="{$labelWidth}">					
							Entity:
						</td>
						<td class="roweven">
							<xsl:for-each select="../*[local-name()='entity']">
								<span>
									<xsl:attribute name="title">
										<xsl:value-of select="fn:namespace-uri-for-prefix(substring-before(./@id, ':'), .)"/>
										<xsl:text>#</xsl:text>
										<xsl:value-of select="substring-after(./@id, ':')"/>
									</xsl:attribute>
									<xsl:value-of select="substring-after(./@id, ':')"/>
								</span>	
							</xsl:for-each>
						</td>
					</tr>
				</xsl:when>
				<xsl:otherwise>
					<tr>
						<td class="rowodd" width="{$labelWidth}">					
							Column:
						</td>
						<td class="roweven">
							<xsl:variable name="columnkey">
								<xsl:value-of select="concat(../../@id, @label)" />
							</xsl:variable>
							<xsl:for-each select="key('mapping', $columnkey )">
								<xsl:value-of select="./@attribute" />
							</xsl:for-each>
						</td>
					</tr>
				</xsl:otherwise>
			</xsl:choose>
			
			
			<tr>
				<td class="rowodd" width="{$labelWidth}">					
					Characteristic[s]:
				</td>
				<td class="roweven">
					<xsl:for-each select="./*[local-name()='characteristic']">
						<span>
							<xsl:attribute name="title">
								<xsl:value-of select="fn:namespace-uri-for-prefix(substring-before(./@id, ':'), .)"/>
								<xsl:text>#</xsl:text>
								<xsl:value-of select="substring-after(./@id, ':')"/>
							</xsl:attribute>
							<xsl:value-of select="substring-after(./@id, ':')"/>
						</span>	
					</xsl:for-each>
				</td>
			</tr>
			<tr>
				<td class="rowodd" width="{$labelWidth}">					
					Standard:
				</td>
				<td class="roweven">
					<span>
						<xsl:attribute name="title">
							<xsl:value-of select="fn:namespace-uri-for-prefix(substring-before(./*[local-name()='standard']/@id, ':'), .)"/>
							<xsl:text>#</xsl:text>
							<xsl:value-of select="substring-after(./*[local-name()='standard']/@id, ':')"/>	
						</xsl:attribute>					
						<xsl:value-of select="substring-after(./*[local-name()='standard']/@id, ':')"/>
					</span>							
				</td>
			</tr>
			<tr>
				<td class="rowodd" width="{$labelWidth}">					
					Protocol:
				</td>
				<td class="roweven">
					<span>
						<xsl:attribute name="title">
							<xsl:value-of select="fn:namespace-uri-for-prefix(substring-before(./*[local-name()='protocol']/@id, ':'), .)"/>
							<xsl:text>#</xsl:text>
							<xsl:value-of select="substring-after(./*[local-name()='protocol']/@id, ':')"/>
						</xsl:attribute>					
						<xsl:value-of select="substring-after(./*[local-name()='protocol']/@id, ':')"/>
					</span>
				</td>
			</tr>
		</table>
		
	</xsl:template>
	
	<xsl:template name="measurementSummary">
		<!-- entity -->
		<td>
			<xsl:for-each select="../*[local-name()='entity']">
				<span>
					<xsl:attribute name="title">
						<xsl:value-of select="fn:namespace-uri-for-prefix(substring-before(./@id, ':'), .)"/>
						<xsl:text>#</xsl:text>
						<xsl:value-of select="substring-after(./@id, ':')"/>
					</xsl:attribute>
					<xsl:value-of select="substring-after(./@id, ':')"/>
				</span>
				(<xsl:value-of select="../@label"/>)
				<xsl:if test="position() != last()">, </xsl:if>
			</xsl:for-each>
		</td>
		<td>
			<!-- measurement -->
			<!-- 
			<xsl:value-of select="./@label" />
			-->
			<!-- characteristic -->
			<xsl:for-each select="./*[local-name()='characteristic']">
				<span>
					<xsl:attribute name="title">
						<xsl:value-of select="fn:namespace-uri-for-prefix(substring-before(./@id, ':'), .)"/>
						<xsl:text>#</xsl:text>
						<xsl:value-of select="substring-after(./@id, ':')"/>
					</xsl:attribute>
					<xsl:value-of select="substring-after(./@id, ':')"/>
				</span>
				<xsl:if test="position() != last()">, </xsl:if>
			</xsl:for-each>
		</td>
		<td>
			<!-- standard -->
			<span>
				<xsl:attribute name="title">
					<xsl:value-of select="fn:namespace-uri-for-prefix(substring-before(./*[local-name()='standard']/@id, ':'), .)"/>
					<xsl:text>#</xsl:text>
					<xsl:value-of select="substring-after(./*[local-name()='standard']/@id, ':')"/>	
				</xsl:attribute>					
				<xsl:value-of select="substring-after(./*[local-name()='standard']/@id, ':')"/>
			</span>
		</td>
		<td>							
			<!-- protocol -->
			<span>
				<xsl:attribute name="title">
					<xsl:value-of select="fn:namespace-uri-for-prefix(substring-before(./*[local-name()='protocol']/@id, ':'), .)"/>
					<xsl:text>#</xsl:text>
					<xsl:value-of select="substring-after(./*[local-name()='protocol']/@id, ':')"/>
				</xsl:attribute>					
				<xsl:value-of select="substring-after(./*[local-name()='protocol']/@id, ':')"/>
			</span>
		</td>	
	</xsl:template>
	
	<xsl:template name="modelSummary">
		<table class="subGroup subGroup_border onehundred_percent">
			<tr>
				<th colspan="5">
					Attribute Summary
				</th>
			</tr>
			
			<tr>
				<td class="roweven" width="{$labelWidth}"></td>
     			<td class="rowodd">
     				Entity
     			</td>
     			<td class="rowodd">
     				Characteristic
     			</td>
     			<td class="rowodd">
     				Standard
     			</td>
     			<td class="rowodd">
     				Protocol
     			</td>
     		</tr>
			
			<!-- reference to the annotation parent -->
			<xsl:variable name="thisAnnotationId">
				<xsl:value-of select="./@id"/>
			</xsl:variable>
			<!-- reference to the dataPackage parent -->
			<xsl:variable name="thisDataPackage">
				<xsl:value-of select="./@dataPackage"/>
			</xsl:variable>
			
			<xsl:for-each select="./*[local-name()='map']">
				<tr>
					<!-- look up the attribute -->
					<xsl:variable name="attributeLabel">
						<xsl:value-of select="./@attribute"/>
					</xsl:variable>
					<td class="rowodd">
						<!-- the dataObject that contains the attribute -->
						<a>
							<xsl:attribute name="href">
								<xsl:value-of select="$tripleURI"/><xsl:value-of select="$thisDataPackage"/>&amp;displaymodule=entity&amp;entitytype=dataTable&amp;entityindex=<xsl:value-of select="number(./@dataObject)+1"/>&amp;annotationId=<xsl:value-of select="$thisAnnotationId"/>
							</xsl:attribute>
							<xsl:value-of select="$attributeLabel"/>
							<xsl:text> (</xsl:text>
							<xsl:value-of select="./@dataObject"/>
							<xsl:text>)</xsl:text>
						</a>
					</td>
					
					<!-- look up the attribute mapping for the given attribute -->
					<xsl:for-each select="key('attributes', concat($thisAnnotationId, $attributeLabel))">
						<!-- get the <measurement> node using this label -->
						<xsl:for-each select="key('measurements', concat($thisAnnotationId, ./@measurement))">
							<xsl:call-template name="measurementSummary"/>
						</xsl:for-each>		
					</xsl:for-each>
					
				</tr>
					
			</xsl:for-each>
			
		</table>	
		
	</xsl:template>
	
	<xsl:template name="attributeDetail">
		<xsl:param name="attributeLabel"/>
		<!-- look up the attribute mapping for the given label -->
		<xsl:for-each select="key('attributes', concat(//*[local-name()='annotation']/@id, $attributeLabel))">
			<!-- get the <measurement> node using this label -->
			<table>
				<tr>
					<xsl:for-each select="key('measurements', concat(//*[local-name()='annotation']/@id, ./@measurement))">
						<td>
							<xsl:call-template name="measurement"/>
						</td>	
					</xsl:for-each>
				</tr>
			</table>		
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>