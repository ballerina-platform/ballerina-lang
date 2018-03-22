import ballerina/net.http;
import ballerina/mime;

function testGetHeader (http:Response res, string key) returns string | null {
    return res.getHeader(key);
}

function testGetJsonPayload (http:Response res) returns json | null | mime:EntityError {
    return res.getJsonPayload();
}

function testGetStringPayload (http:Response res) returns string | null | mime:EntityError {
    return res.getStringPayload();
}

function testGetBinaryPayload (http:Response res) returns blob | mime:EntityError {
    return res.getBinaryPayload();
}

function testGetXmlPayload (http:Response res) returns xml | null| mime:EntityError {
    return res.getXmlPayload();
}

function testGetEntity (http:Response response) returns mime:Entity | mime:EntityError {
    return response.getEntity();
}


function testRemoveHeader (http:Response res, string key) returns (http:Response) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:Response ress) returns (http:Response) {
    http:Response res = {};
    res.removeAllHeaders();
    return res;
}
