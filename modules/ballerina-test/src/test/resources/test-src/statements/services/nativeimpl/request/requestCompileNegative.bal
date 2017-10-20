import ballerina.net.http;

function testRequestSetStatusCode (http:Request req, string statusCode) (http:Request) {
    req.setStatusCode(statusCode);
    return req;
}

function testRequestGetContentLengthWithString (http:Request req) (http:Request) {
    req.setContentLength("hello");
    return req;
}