import ballerina/io;
import ballerina/xslt;

function readFromFile(string xmlFilePath, string xslFilePath) returns @tainted xml|error {
    var xmlValue = readXml(xmlFilePath);
    if (xmlValue is xml) {
        io:println(xmlValue);
        var xslValue = readXml(xslFilePath);
        if (xslValue is xml) {
            io:println(xslValue);
            io:println("perform xslt");
            var result = xslt:performXSLT(xmlValue, xslValue);
            if (result is xml) {
                io:println(result);
                return result;
            } else {
                return result;
            }
        } else {
            io:println("read xsl error");
            return xslValue;
        }
    } else {
        io:println("read xml error");
        return xmlValue;
    }
}


function readXml(string filePath) returns @tainted xml|error {
    io:println("readXml" + filePath);
    var byteChannel = io:openReadableFile(filePath);
    if (byteChannel is io:ReadableByteChannel) {
        io:ReadableCharacterChannel rch = <@untainted> new io:ReadableCharacterChannel(byteChannel, "UTF-8");
        var result = rch.readXml();
        if (result is xml) {
            io:println("return Xml");
            return result;
        } else {
            io:println("return error");
            return result;
        }
    } else {
        return byteChannel;
    }
}

function simpleTransform() {
    xml input = xml `<catalog>
	                 <cd>
	                     <title>Empire Burlesque</title>
	                     <artist>Bob Dylan</artist>
	                     <country>USA</country>
	                     <company>Columbia</company>
	                     <price>10.90</price>
	                     <year>1985</year>
	                  </cd>
                      </catalog>`;
    xml xsl = xml `<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                        <xsl:template match="/">
                            <html>
                                <body>
                                    <h2>My CD Collection</h2>
                                    <table border="1">
                                        <tr bgcolor="#9acd32">
                                            <th>Title</th>
                                            <th>Artist</th>
                                        </tr>
                                        <xsl:for-each select="catalog/cd">
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

    var result = xslt:performXSLT(input, xsl);
    if (result is xml) {
        io:println(result);
    } else {
        io:println(result);
    }
}
