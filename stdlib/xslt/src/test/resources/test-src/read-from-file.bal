import ballerina/io;
import ballerina/xslt;

function readFromFile(string xmlFilePath, string xslFilePath) returns  xml|error {
    var xmlValue = readXml(xmlFilePath);
    if (xmlValue is xml) {
        var xslValue = readXml(xslFilePath);
        if (xslValue is xml) {
            io:println("perform xslt");
            var result = xslt:performXSLT(xmlValue, xslValue);
            if (result is xml) {
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


function readXml(string filePath) returns xml|error {
    io:println("readXml" + filePath);
    io:ReadableByteChannel byteChannel = io:openReadableFile(filePath);
    io:ReadableCharacterChannel rch = untaint new io:ReadableCharacterChannel(byteChannel, "UTF-8");
    var result = rch.readXml();
    if (result is xml) {
        io:println("return Xml");
        return result;
    } else {
        io:println("return error");
        return result;
    }
}

function createXsltManually(string xmlFilePath, string xslFilePath) returns  xml|error {
    var xmlValue = readXml(xmlFilePath);
    if (xmlValue is xml) {
        var xslValue = readXml(xslFilePath);
        if (xslValue is xml) {
            io:println("perform xslt");
            var result = generateXSLT(xmlValue, xslValue);
            if (result is xml) {
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

function generateXSLT(xml source, xml xsl) returns xml|error {
    xml target = xml xsl.
    return source;
}