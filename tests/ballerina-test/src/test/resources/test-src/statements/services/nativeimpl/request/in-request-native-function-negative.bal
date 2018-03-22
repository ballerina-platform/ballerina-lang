import ballerina/net.http;
import ballerina/mime;

function testGetContentLength (http:Request req) returns (int) {
    int length = req.getContentLength();
    return length;
}

function testGetHeader (http:Request req, string key) returns (string) {
    var contentType = req.getHeader(key);
    if (contentType == null) {
        return null;
    }
    return contentType;
}

function testGetJsonPayload (http:Request req) returns (json, mime:EntityError) {
    return req.getJsonPayload();
}

function testGetMethod (http:Request req) returns (string) {
    string method = req.method;
    return method;
}

function testGetProperty (http:Request req, string propertyName) returns (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetRequestURL (http:Request req) returns (string) {
    string url = req.rawPath;
    if (url == null || url == "") {
        url = "no url";
    }
    return url;
}

function testGetStringPayload (http:Request req) returns (string, mime:EntityError) {
    return req.getStringPayload();
}

function testGetBinaryPayload (http:Request req) returns (blob, mime:EntityError) {
    return req.getBinaryPayload();
}

function testGetXmlPayload (http:Request req) returns (xml, mime:EntityError) {
    return req.getXmlPayload();
}

function testSetHeader (http:Request req, string key, string value) returns (http:Request) {
    req.setHeader(key, value);
    return req;
}

function testGetEntity (http:Request req) returns (mime:Entity, mime:EntityError) {
    return req.getEntity();
}

function testRemoveHeader (http:Request req, string key) returns (http:Request) {
    req.removeHeader(key);
    return req;
}

function testRemoveAllHeaders (http:Request req) returns (http:Request) {
    req.removeAllHeaders();
    return req;
}
