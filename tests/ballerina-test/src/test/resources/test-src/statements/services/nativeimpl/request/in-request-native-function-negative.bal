import ballerina.net.http;
import ballerina.mime;

function testGetContentLength (http:Request req) (int) {
    int length = req.getContentLength();
    return length;
}

function testGetHeader (http:Request req, string key) (string) {
    var contentType = req.getHeader(key);
    if (contentType == null) {
        return null;
    }
    return contentType;
}

function testGetJsonPayload (http:Request req) (json, mime:EntityError) {
    return req.getJsonPayload();
}

function testGetMethod (http:Request req) (string) {
    string method = req.method;
    return method;
}

function testGetProperty (http:Request req, string propertyName) (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetRequestURL (http:Request req) (string) {
    string url = req.rawPath;
    if (url == null || url == "") {
        url = "no url";
    }
    return url;
}

function testGetStringPayload (http:Request req) (string, mime:EntityError) {
    return req.getStringPayload();
}

function testGetBinaryPayload (http:Request req) (blob, mime:EntityError) {
    return req.getBinaryPayload();
}

function testGetXmlPayload (http:Request req) (xml, mime:EntityError) {
    return req.getXmlPayload();
}

function testSetHeader (http:Request req, string key, string value) (http:Request) {
    req.setHeader(key, value);
    return req;
}

function testGetEntity (http:Request req) (mime:Entity, mime:EntityError) {
    return req.getEntity();
}

function testRemoveHeader (http:Request req, string key) (http:Request) {
    req.removeHeader(key);
    return req;
}

function testRemoveAllHeaders (http:Request req) (http:Request) {
    req.removeAllHeaders();
    return req;
}
