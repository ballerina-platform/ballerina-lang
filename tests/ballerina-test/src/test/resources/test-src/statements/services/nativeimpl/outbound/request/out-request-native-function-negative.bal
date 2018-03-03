import ballerina.net.http;
import ballerina.mime;

function testGetContentLength (http:OutRequest req) (int) {
    int length = req.getContentLength();
    return length;
}

function testGetHeader (http:OutRequest req, string key) (string) {
    var contentType = req.getHeader(key);
    if (contentType == null) {
        return null;
    }
    return contentType;
}

function testGetJsonPayload (http:OutRequest req) (json, mime:EntityError) {
    return req.getJsonPayload();
}

function testGetProperty (http:OutRequest req, string propertyName) (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetEntity (http:OutRequest req) (mime:Entity) {
    mime:Entity entity = req.getEntity();
    return entity;
}

function testGetStringPayload (http:OutRequest req) (string, mime:EntityError) {
    return req.getStringPayload();
}

function testGetBinaryPayload (http:OutRequest req) (blob, mime:EntityError) {
    return req.getBinaryPayload();
}

function testGetXmlPayload (http:OutRequest req) (xml, mime:EntityError) {
    return req.getXmlPayload();
}

function testRemoveHeader (http:OutRequest req, string key) (http:OutRequest) {
    req.removeHeader(key);
    return req;
}

function testRemoveAllHeaders (http:OutRequest req) (http:OutRequest) {
    req.removeAllHeaders();
    return req;
}

function testSetHeader (http:OutRequest req, string key, string value) (http:OutRequest) {
    req.setHeader(key, value);
    return req;
}

function testSetJsonPayload (http:OutRequest req, json value) (http:OutRequest) {
    req.setJsonPayload(value);
    return req;
}

function testSetProperty (http:OutRequest req, string name, string value) (http:OutRequest) {
    req.setProperty(name, value);
    return req;
}

function testSetStringPayload (http:OutRequest req, string value) (http:OutRequest) {
    req.setStringPayload(value);
    return req;
}

function testSetXmlPayload (http:OutRequest req, xml value) (http:OutRequest) {
    req.setXmlPayload(value);
    return req;
}