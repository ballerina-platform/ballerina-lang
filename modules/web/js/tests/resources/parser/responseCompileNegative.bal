import ballerina.net.http;
import ballerina.net.http.response;

function testResponseSetStatusCode (http:Response res, string statusCode) (http:Response) {
    response:setStatusCode(res, statusCode);
    return res;
}

function testResponseGetContentLengthWithString (http:Response res) (http:Response) {
    response:setContentLength(res, "hello");
    return res;
}

function testResponseGetMethod (http:Response res) (string) {
    string method = response:getMethod(res);
    return method;
}

