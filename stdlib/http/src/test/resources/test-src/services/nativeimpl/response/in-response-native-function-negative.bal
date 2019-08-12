import ballerina/http;
import ballerina/mime;

function testGetHeader(http:Response res, string key) returns @tainted string {
    return res.getHeader(key);
}

function testGetJsonPayload(http:Response res) returns @tainted json|error {
    return res.getJsonPayload();
}

function testGetTextPayload(http:Response res) returns @tainted string|error {
    return res.getTextPayload();
}

function testGetBinaryPayload(http:Response res) returns @tainted byte[]|error {
    return res.getBinaryPayload();
}

function testGetXmlPayload(http:Response res) returns @tainted xml|error {
    return res.getXmlPayload();
}

function testGetEntity(http:Response response) returns mime:Entity|error {
    return response.getEntity();
}


function testRemoveHeader(http:Response res, string key) returns (http:Response) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders(http:Response ress) returns (http:Response) {
    http:Response res = new;
    res.removeAllHeaders();
    return res;
}
