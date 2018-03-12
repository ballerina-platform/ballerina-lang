import ballerina/net.http;
import ballerina/mime;

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

function testGetJsonPayload (http:InRequest req) (json, mime:EntityError) {
    return req.getJsonPayload();
}

function testGetMethod (http:InRequest req) (string ) {
    string method = req.method;
    return method;
}

function testGetProperty (http:InRequest req, string propertyName) (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetRequestURL (http:InRequest req) (string) {
    string url = req.rawPath;
    if (url == "") {
        url = "no url";
    }
    return url;
}

function testGetStringPayload (http:InRequest req) (string, mime:EntityError) {
    return req.getStringPayload();
}

function testGetBinaryPayload (http:InRequest req) (blob, mime:EntityError) {
    return req.getBinaryPayload();
}

function testGetXmlPayload (http:InRequest req) (xml, mime:EntityError) {
    return req.getXmlPayload();
}

function testSetHeader (http:OutRequest req, string key, string value) (http:OutRequest) {
    req.setHeader(key, value);
    return req;
}
