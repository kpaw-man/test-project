<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:plmet="https://fbc.pionier.net.pl/schemas/plmet/1.0/"
                xmlns:dace="https://bs.katowice.pl/bsa/"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:terms="http://purl.org/dc/terms/"
                exclude-result-prefixes="plmet"
                version="2.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <dace:pbi>
            <dace:dc>
                <xsl:for-each select="//*[starts-with(name(), 'dc:')]">
                    <xsl:if test="local-name() != 'dc'">
                        <xsl:element name="{concat('dace:', local-name())}">
                            <xsl:if test="@xml:lang">
                                <xsl:attribute name="xml:lang">
                                    <xsl:value-of select="@xml:lang"/>
                                </xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="dace:original">
                                <xsl:value-of select="name()"/>
                            </xsl:attribute>
                            <xsl:value-of select="node()"/>
                        </xsl:element>
                    </xsl:if>
                </xsl:for-each>
            </dace:dc>
            <dace:terms>
                <xsl:for-each select="//*[starts-with(name(), 'terms:')]">
                    <xsl:element name="{concat('dace:', local-name())}">
                        <xsl:if test="@xml:lang">
                            <xsl:attribute name="xml:lang">
                                <xsl:value-of select="@xml:lang"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:attribute name="dace:original">
                            <xsl:value-of select="name()"/>
                        </xsl:attribute>
                        <xsl:value-of select="node()"/>
                    </xsl:element>
                </xsl:for-each>
            </dace:terms>
        </dace:pbi>
    </xsl:template>

</xsl:stylesheet>