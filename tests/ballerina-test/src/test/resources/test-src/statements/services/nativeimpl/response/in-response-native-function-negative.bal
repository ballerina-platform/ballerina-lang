import ballerina/http;
import ballerina/mime;

function testGetHeader (http:Response res, string key) returns string {
    return res.getHeader(key);
}

function testGetJsonPayload (http:Response res) returns json | http:PayloadError {
    return res.getJsonPayload();
}

function testGetStringPayload (http:Response res) returns string | http:PayloadError {
    return res.getStringPayload();
}

function testGetBinaryPayload (http:Response res) returns blob | http:PayloadError {
    return res.getBinaryPayload();
}

function testGetXmlPayload (http:Response res) returns xml | http:PayloadError {
    return res.getXmlPayload();
}

function testGetEntity (http:Response response) returns mime:Entity | http:PayloadError {
    return response.getEntity();
}


function testRemoveHeader (http:Response res, string key) returns (http:Response) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:Response ress) returns (http:Response) {
    http:Response res = new;
    res.removeAllHeaders();
    return res;
}
