import ballerina/net.http;
import ballerina/mime;

function testAddHeader (http:OutResponse res, string key, string value) (http:OutResponse) {
    res.addHeader(key, value);
    return res;
}

function testGetContentLength (http:OutResponse res) (int) {
    int length = res.getContentLength();
    return length;
}

function testGetHeader (http:OutResponse res, string key) (string) {
    var contentType = res.getHeader(key);
    if (contentType == null) {
        return null;
    }
    return contentType;
}

function testGetEntity (http:OutResponse response) (mime:Entity, mime:EntityError) {
    return response.getEntity();
}

function testGetJsonPayload (http:OutResponse res) (json, mime:EntityError) {
    return res.getJsonPayload();
}

function testGetProperty (http:OutResponse res, string propertyName) (string) {
    string payload = res.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:OutResponse res) (string, mime:EntityError) {
    return res.getStringPayload();
}

function testGetBinaryPayload (http:OutResponse res) (blob, mime:EntityError) {
    return res.getBinaryPayload();
}

function testGetXmlPayload (http:OutResponse res) (xml, mime:EntityError) {
    return res.getXmlPayload();
}

function testRemoveHeader (http:OutResponse res, string key) (http:OutResponse) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:OutResponse ress) (http:OutResponse) {
    http:OutResponse res = {};
    res.removeAllHeaders();
    return res;
}

function testSetHeader (http:OutResponse res, string key, string value) (http:OutResponse) {
    res.setHeader(key, value);
    return res;
}

function testSetJsonPayload (http:OutResponse res, json value) (http:OutResponse) {
    res.setJsonPayload(value);
    return res;
}

function testSetProperty (http:OutResponse res, string name, string value) (http:OutResponse) {
    res.setProperty(name, value);
    return res;
}

function testSetStringPayload (http:OutResponse res, string value) (http:OutResponse) {
    res.setStringPayload(value);
    return res;
}

function testSetXmlPayload (http:OutResponse res, xml value) (http:OutResponse) {
    res.setXmlPayload(value);
    return res;
}
