import ballerina.net.http;
import ballerina.net.http.response;

function testSetStatusCode (http:Response res, string statusCode) (http:Response) {
    response:setStatusCode(res, statusCode);
    return res;
}

function testGetContentLengthWithString (http:Response res) (http:Response) {
    response:setContentLength(res, "hello");
    return res;
}