import ballerina/io;
import ballerina/xslt;

public function main() {
    // Gets an `XML` object, which needs to be transformed.
    xml sourceXml = getXml();
    // Gets an `XSL` style sheet represented in an XML object.
    xml xsl = getXsl();

    // Transforms the `XML` to another formats.
    xml|error target = xslt:transform(sourceXml, xsl);
    if (target is xml) {
        io:println("Transformed xml : ", target);
    } else {
        io:print("Error : ", target);
    }
}

// Returns an `XML` object, which needs to be transformed.
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

// Returns an `XSL` style sheet represented in an XML object.
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
