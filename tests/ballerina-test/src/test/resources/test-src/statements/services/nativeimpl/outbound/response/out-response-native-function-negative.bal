import ballerina.net.http;
import ballerina.mime;

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

function testGetEntity (http:OutResponse response) (mime:Entity) {
    mime:Entity entity = response.getEntity();
    return entity;
}

function testGetJsonPayload (http:OutResponse res) (json) {
    json payload = res.getJsonPayload();
    return payload;
}

function testGetProperty (http:OutResponse res, string propertyName) (string) {
    string payload = res.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:OutResponse res) (string) {
    string payload = res.getStringPayload();
    return payload;
}

function testGetBinaryPayload (http:OutResponse res) (blob) {
    blob payload = res.getBinaryPayload();
    return payload;
}

function testGetXmlPayload (http:OutResponse res) (xml) {
    xml payload = res.getXmlPayload();
    return payload;
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
