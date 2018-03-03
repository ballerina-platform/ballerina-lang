import ballerina.net.http;
import ballerina.mime;

function testGetContentLength (http:InResponse res) (int) {
    int length = res.getContentLength();
    return length;
}

function testGetHeader (http:InResponse res, string key) (string) {
    var contentType = res.getHeader(key);
    if (contentType == null) {
        return null;
    }
    return contentType;
}

function testGetJsonPayload (http:InResponse res) (json) {
    json payload = res.getJsonPayload();
    return payload;
}

function testGetProperty (http:InResponse res, string propertyName) (string) {
    string payload = res.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:InResponse res) (string) {
    string payload = res.getStringPayload();
    return payload;
}

function testGetBinaryPayload (http:InResponse res) (blob) {
    blob payload = res.getBinaryPayload();
    return payload;
}

function testGetXmlPayload (http:InResponse res) (xml) {
    xml payload = res.getXmlPayload();
    return payload;
}
