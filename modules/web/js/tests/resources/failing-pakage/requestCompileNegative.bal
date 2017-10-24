import ballerina.net.http;
import ballerina.net.http.request;

function testRequestSetStatusCode (http:Request req, string statusCode) (http:Request) {
    request:setStatusCode(req, statusCode);
    return req;
}

function testRequestGetContentLengthWithString (http:Request req) (http:Request) {
    request:setContentLength(req, "hello");
    return req;
}