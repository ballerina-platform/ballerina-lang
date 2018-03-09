import ballerina.net.http;
import ballerina.mime;

function testAddHeader (http:Response res, string key, string value) (http:Response) {
    res.addHeader(key, value);
    return res;
}

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

function testGetEntity (http:Response response) (mime:Entity, mime:EntityError) {
    return response.getEntity();
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

function testRemoveHeader (http:Response res, string key) (http:Response) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:Response ress) (http:Response) {
    http:Response res = {};
    res.removeAllHeaders();
    return res;
}

function testSetHeader (http:Response res, string key, string value) (http:Response) {
    res.setHeader(key, value);
    return res;
}

function testSetJsonPayload (http:Response res, json value) (http:Response) {
    res.setJsonPayload(value);
    return res;
}

function testSetProperty (http:Response res, string name, string value) (http:Response) {
    res.setProperty(name, value);
    return res;
}

function testSetStringPayload (http:Response res, string value) (http:Response) {
    res.setStringPayload(value);
    return res;
}

function testSetXmlPayload (http:Response res, xml value) (http:Response) {
    res.setXmlPayload(value);
    return res;
}
