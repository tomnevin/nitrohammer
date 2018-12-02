<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
	<xsl:output method="xml" omit-xml-declaration="yes" doctype-system="about:legacy-compat" indent="yes" />
	<xsl:key name="type"
		match="/xsd:schema/xsd:complexType | /xsd:schema/xsd:simpleType | /xsd:schema/xsd:redefine/xsd:complexType | /xsd:schema/xsd:redefine/xsd:simpleType"
		use="@name" />
	<xsl:key name="complexType" match="/xsd:schema/xsd:complexType | /xsd:schema/xsd:redefine/xsd:complexType" use="@name" />
	<xsl:key name="simpleType" match="/xsd:schema/xsd:simpleType | /xsd:schema/xsd:redefine/xsd:simpleType" use="@name" />
	<xsl:key name="attributeGroup" match="/xsd:schema/xsd:attributeGroup | /xsd:schema/xsd:redefine/xsd:attributeGroup"
		use="@name" />
	<xsl:key name="group" match="/xsd:schema/xsd:group | /xsd:schema/xsd:redefine/xsd:group" use="@name" />
	<xsl:key name="attribute" match="/xsd:schema/xsd:attribute" use="@name" />
	<xsl:key name="element" match="/xsd:schema/xsd:element" use="@name" />
	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>
	<xsl:template match="xsd:schema">
		<html>
			<head>
				<title> Database Schema Definitions
				</title>
				<style>
					table {
					font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
					border-collapse: collapse;
					width: 100%;
					}

					table td, table
					th {
					border: 1px solid #ddd;
					padding: 8px;
					}

					table tr:nth-child(even){background-color: #f2f2f2;}

					table th {
					padding-top: 12px;
					padding-bottom: 12px;
					text-align: left;
					color: black;
					}
				</style>
			</head>
			<body style="width: 800px">
				<xsl:apply-templates select="xsd:complexType[not (xsd:simpleContent)]">
					<xsl:sort select="@name" />
				</xsl:apply-templates>
				<xsl:apply-templates select="xsd:complexType[xsd:simpleContent]">
					<xsl:sort select="@name" />
				</xsl:apply-templates>
				<xsl:apply-templates select="xsd:simpleType">
					<xsl:sort select="@name" />
				</xsl:apply-templates>

				<h7>Examples</h7>
				<!-- <xsl:variable name="documents" select="fn:document('../model/examples/')"/> -->
				<!-- <xsl:for-each select="$documents"> -->
				<!-- <h2> -->
				<!-- Example <xsl:value-of select="@title" /> -->
				<!-- </h2> -->
				<!-- <xsl:copy-of select="/*"/> -->
				<!-- </xsl:for-each> -->
			</body>
		</html>
	</xsl:template>

	<xsl:template match="xsd:complexType[not (xsd:simpleContent)]">
		<xsl:variable name="name" select="@name" />
		<xsl:variable name="filename" select="string(concat('../model/examples/', @name, '.xml'))" />
		<h1>
			<xsl:value-of select="@name" />
			Definition
		</h1>
		<xsl:copy-of select="xsd:annotation/xsd:documentation" />
		<table border="1">
			<tr>
				<th>Name</th>
				<th>Type</th>
				<th>Default</th>
			</tr>
			<tr>
				<th colspan="3">Comments</th>
			</tr>
			<xsl:apply-templates select="xsd:sequence/xsd:element | xsd:attribute">
				<xsl:sort select="@name" />
			</xsl:apply-templates>
		</table>
		<xsl:choose>
		    <xsl:when test="fs:exists(fs:new($filename))" xmlns:fs="java.io.File">
                <xsl:copy-of select="document($filename)" />
            </xsl:when>
            <xsl:otherwise> 
            </xsl:otherwise>
        </xsl:choose>
	</xsl:template>

	<xsl:template match="xsd:complexType[xsd:simpleContent]">
		<xsl:variable name="name" select="@name" />
		<h1>
			<xsl:value-of select="@name" />
			Definition
			<xsl:value-of select="xsd:extension/@base" />
		</h1>
		<xsl:copy-of select="xsd:annotation/xsd:documentation" />
		<table border="1">
			<tr>
				<th>Name</th>
				<th>Type</th>
				<th>Default</th>
			</tr>
			<tr>
				<th colspan="3">Comments</th>
			</tr>
			<xsl:apply-templates select="xsd:simpleContent/xsd:extension/xsd:attribute">
				<xsl:sort select="@name" />
			</xsl:apply-templates>
		</table>
	</xsl:template>
	<xsl:template match="xsd:sequence/xsd:element">
		<tr>
			<td>
				<xsl:value-of select="@name" />
			</td>
			<td>
				<xsl:value-of select="@type" />
			</td>
			<td>
				<xsl:value-of select="@default" />
			</td>
		</tr>
		<tr>
			<td colspan="3">
				<xsl:value-of select="xsd:annotation/xsd:documentation" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="xsd:extension/xsd:attribute">
		<tr>
			<td>
				<xsl:value-of select="@name" />
			</td>
			<td>
				<xsl:value-of select="@type" />
			</td>
			<td>
				<xsl:value-of select="@default" />
			</td>
		</tr>
		<tr>
			<td colspan="3">
				<xsl:value-of select="xsd:annotation/xsd:documentation" />
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="xsd:attribute">
		<tr>
			<td>
				<xsl:value-of select="@name" />
			</td>
			<td>
				<xsl:value-of select="@type" />
			</td>
			<td>
				<xsl:value-of select="@default" />
			</td>
		</tr>
		<tr>
			<td colspan="3">
				<xsl:value-of select="xsd:annotation/xsd:documentation" />
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="xsd:simpleType">
		<h2>
			<xsl:value-of select="@name" />
			Definition
		</h2>

		<xsl:value-of select="xsd:restriction/@name" />
		<xsl:copy-of select="xsd:annotation/xsd:documentation" />
		<table border="1">
			<tr>
				<th>Value</th>
				<th>Description</th>
			</tr>
			<xsl:apply-templates select="xsd:restriction/xsd:enumeration">
				<xsl:sort select="@value" />
			</xsl:apply-templates>
		</table>
	</xsl:template>
	<xsl:template match="xsd:enumeration">
		<tr>
			<td>
				<xsl:value-of select="@value" />
			</td>

			<td>
				<xsl:copy-of select="xsd:annotation/xsd:documentation" />
			</td>
		</tr>
	</xsl:template>
	
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>