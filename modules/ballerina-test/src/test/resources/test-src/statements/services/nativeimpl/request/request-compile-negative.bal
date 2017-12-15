import ballerina.net.http;

function testRequestSetStatusCode (http:Request req, string statusCode) (http:Request) {
    req.setStatusCode(statusCode);
    return req;
}