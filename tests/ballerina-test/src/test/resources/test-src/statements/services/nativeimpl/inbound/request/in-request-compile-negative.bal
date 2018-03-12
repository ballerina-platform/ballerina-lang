import ballerina/net.http;

function testRequestSetStatusCode (http:InRequest req, string statusCode) (http:InRequest) {
    req.setStatusCode(statusCode);
    req.statusCode = 204;
    return req;
}