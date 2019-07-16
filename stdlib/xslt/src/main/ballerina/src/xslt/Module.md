## Module Overview
This module provides a function to transform the XML content to another XML/HTML/plain text using XSL transformations.

## Samples
###Transform XML using a given XSL
The following program depicts how the XSL transformation can be done for a given single-rooted XML, using a XSL.
``` ballerina
import ballerina/io;
import ballerina/xslt;

public function main(string... args) {
    xml sourceXml = getXml();
    xml xsl = getXsl();
    
    xml|error target = xslt:performXSLT(sourceXml, xsl);
    if (target is xml) {
        io:println("Transformed xml : ");
        io:println(target);
    } else {
        io:print("Error : ");
        io:println(target);
    }    
}

function getXml() returns xml {
    return xml `<samples>
                    <song>
                        <title>Summer of 69</title>
                        <artist>Bryan Adams</artist>
                        <country>Canada</country>
                        <year>1984</year>
                    </song>
                    <song>
                        <title>Zombie</title>
                        <artist>Bad Wolves</artist>
                        <country>USA</country>
                        <year>2018</year>
                    </song>
                </samples>`;
}

function getXsl() returns xml {
    return xml `<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                    <xsl:template match="/">
                        <html>
                            <body>
                                <h2>All time favourites</h2>
                                <table border="1">
                                    <tr bgcolor="#9acd33">
                                        <th>Title</th>
                                        <th>Artist</th>
                                    </tr>
                                <xsl:for-each select="samples/song">
                                    <tr>
                                        <td>
                                            <xsl:value-of select="title"/>
                                        </td>
                                        <td>
                                            <xsl:value-of select="artist"/>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                                </table>
                            </body>
                        </html>
                    </xsl:template>
                </xsl:stylesheet>`;
}
```
