import ballerina/http;

function testRequestSetStatusCode (http:Request req, string statusCode) returns (http:Request) {
    req.setStatusCode(statusCode);
    req.statusCode = 204;
    return req;
}
