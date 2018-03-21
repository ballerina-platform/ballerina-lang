import ballerina/net.http;
import ballerina/mime;

function testGetContentLength (http:Response res) (int) {
    int length = res.getContentLength();
    return length;
}

function testGetHeader (http:Response res, string key) (string) {
    var contentType = res.getHeader(key);
    if (contentType == null) {
        return null;
    }
    return contentType;
}

function testGetJsonPayload (http:Response res) (json, mime:EntityError) {
    return res.getJsonPayload();
}

function testGetProperty (http:Response res, string propertyName) (string) {
    string payload = res.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:Response res) (string, mime:EntityError) {
    return res.getStringPayload();
}

function testGetBinaryPayload (http:Response res) (blob, mime:EntityError) {
    return res.getBinaryPayload();
}

function testGetXmlPayload (http:Response res) (xml, mime:EntityError) {
    return res.getXmlPayload();
}

function testGetEntity (http:Response response) (mime:Entity, mime:EntityError) {
    return response.getEntity();
}


function testRemoveHeader (http:Response res, string key) (http:Response) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:Response ress) (http:Response) {
    http:Response res = {};
    res.removeAllHeaders();
    return res;
}
