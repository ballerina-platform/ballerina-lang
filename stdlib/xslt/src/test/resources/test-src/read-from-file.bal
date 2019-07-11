import ballerina/io;
import ballerina/xslt;

function readXml(string filePath) returns @tainted xml|error {
    var byteChannel = io:openReadableFile(filePath);
    if (byteChannel is io:ReadableByteChannel) {
        io:ReadableCharacterChannel rch = <@untainted> new io:ReadableCharacterChannel(byteChannel, "UTF-8");
        var result = rch.readXml();
        if (result is xml) {
            return result;
        } else {
            return result;
        }
    } else {
        return byteChannel;
    }
}

function readFromFile(string xmlFilePath, string xslFilePath) returns @tainted xml|error {
    var xmlValue = readXml(xmlFilePath);
    if (xmlValue is xml) {
        var xslValue = readXml(xslFilePath);
        if (xslValue is xml) {
            var result = xslt:transform(xmlValue, xslValue);
            if (result is xml) {
                return result;
            } else {
                return result;
            }
        } else {
            return xslValue;
        }
    } else {
        return xmlValue;
    }
}

function readMultiRootedXml(string xmlFilePath, string xslFilePath) returns @tainted xml|error {
    var xmlValue = readXml(xmlFilePath);
    if (xmlValue is xml) {
        var xslValue = readXml(xslFilePath);
        if (xslValue is xml) {
            var result = xslt:transform(xmlValue.*, xslValue);
            if (result is xml) {
                return result;
            } else {
                return result;
            }
        } else {
            return xslValue;
        }
    } else {
        return xmlValue;
    }
}

function transform() returns @tainted xml|error {
    xml xmlValue = xml `Hello, World!`;
    xml xslValue = xml `<name>Book1</name>`;
    var result = xslt:transform(xmlValue, xslValue);
    if (result is xml) {
        return result;
    } else {
        return result;
    }
}
