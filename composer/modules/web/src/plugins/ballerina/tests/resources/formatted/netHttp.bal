import ballerina/http;

function testGetMethod (message msg) (string) {
    return http:getMethod(msg);
}

function testConvertToResponse (message m) {
    http:convertToResponse(m);
}

function testSetStatusCode (message m, int statusCode) {
    http:setStatusCode(m, statusCode);
}

function testSetContentLength (message m, int contentLength) {
    http:setContentLength(m, contentLength);
}

function testGetStatusCode (message m) (int) {
    return http:getStatusCode(m);
}

function testGetContentLength (message m) (int) {
    return http:getContentLength(m);
}

function testSetReasonPhrase (message m, string reasonPhrase) {
    http:setReasonPhrase(m, reasonPhrase);
}

