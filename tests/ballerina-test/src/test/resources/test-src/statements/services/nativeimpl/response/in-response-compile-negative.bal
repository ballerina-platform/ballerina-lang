import ballerina/http;

function testResponseSetStatusCode (http:Response res, string statusCode) returns (http:Response) {
    res.statusCode = statusCode;
    return res;
}

function testResponseGetMethod (http:Response res) returns (string) {
    string method = res.method;
    return method;
}

function testAddHeader (http:Response res, string key, string value) returns (http:Response) {
    res.addHeader(key, value);
    return res;
}

function testRemoveHeader (http:Response res, string key) returns (http:Response) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:Response res) returns (http:Response) {
    res.removeAllHeaders();
    return res;
}

function testSetHeader (http:Response res, string key, string value) returns (http:Response) {
    res.setHeader(key, value);
    return res;
}

function testSetJsonPayload (http:Response res, json value) returns (http:Response) {
    res.setJsonPayload(value);
    return res;
}

function testSetStringPayload (http:Response res, string value) returns (http:Response) {
    res.setStringPayload(value);
    return res;
}

function testSetXmlPayload (http:Response res, xml value) returns (http:Response) {
    res.setXmlPayload(value);
    return res;
}


