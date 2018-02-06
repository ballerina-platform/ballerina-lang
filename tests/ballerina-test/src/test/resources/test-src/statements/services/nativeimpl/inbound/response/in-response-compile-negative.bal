import ballerina.net.http;

function testResponseSetStatusCode (http:InResponse res, string statusCode) (http:InResponse) {
    res.statusCode = statusCode;
    return res;
}

function testResponseGetMethod (http:InResponse res) (string) {
    string method = res.method;
    return method;
}

function testAddHeader (http:InResponse res, string key, string value) (http:InResponse) {
    res.addHeader(key, value);
    return res;
}

function testRemoveHeader (http:InResponse res, string key) (http:InResponse) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:InResponse res) (http:InResponse) {
    res.removeAllHeaders();
    return res;
}

function testSetHeader (http:InResponse res, string key, string value) (http:InResponse) {
    res.setHeader(key, value);
    return res;
}

function testSetJsonPayload (http:InResponse res, json value) (http:InResponse) {
    res.setJsonPayload(value);
    return res;
}

function testSetProperty (http:InResponse res, string name, string value) (http:InResponse) {
    res.setProperty(name, value);
    return res;
}

function testSetStringPayload (http:InResponse res, string value) (http:InResponse) {
    res.setStringPayload(value);
    return res;
}

function testSetXmlPayload (http:InResponse res, xml value) (http:InResponse) {
    res.setXmlPayload(value);
    return res;
}


