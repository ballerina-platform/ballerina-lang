import ballerina/net.http;

function testResponseSetStatusCode (http:Response res, string statusCode) (http:Response) {
    res.statusCode = statusCode;
    return res;
}

function testResponseGetMethod (http:Response res) (string) {
    string method = res.method;
    return method;
}

function testAddHeader (http:Response res, string key, string value) (http:Response) {
    res.addHeader(key, value);
    return res;
}

function testRemoveHeader (http:Response res, string key) (http:Response) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:Response res) (http:Response) {
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


