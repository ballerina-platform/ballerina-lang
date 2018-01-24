import ballerina.net.http;
import ballerina.mime;

function testGetContentLength (http:InRequest req) (int) {
    int length = req.getContentLength();
    return length;
}

function testGetHeader (http:InRequest req, string key) (string) {
    var contentType = req.getHeader(key);
    if (contentType == null) {
        return null;
    }
    return contentType;
}

function testGetJsonPayload (http:InRequest req) (json) {
    json payload = req.getJsonPayload();
    return payload;
}

function testGetMethod (http:InRequest req) (string ) {
    string method = req.getMethod();
    return method;
}

function testGetProperty (http:InRequest req, string propertyName) (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetRequestURL (http:InRequest req) (string) {
    string url = req.getRequestURL();
    if (url == "") {
        url = "no url";
    }
    return url;
}

function testGetEntity (http:InRequest req) (mime:Entity) {
    mime:Entity entity = req.getEntity();
    return entity;
}

function testGetStringPayload (http:InRequest req) (string) {
    string payload = req.getStringPayload();
    return payload;
}

function testGetBinaryPayload (http:InRequest req) (blob) {
    blob payload = req.getBinaryPayload();
    return payload;
}

function testGetXmlPayload (http:InRequest req) (xml) {
    xml payload = req.getXmlPayload();
    return payload;
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