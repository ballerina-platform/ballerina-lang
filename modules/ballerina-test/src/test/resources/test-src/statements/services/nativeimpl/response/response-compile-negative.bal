import ballerina.net.http;

function testResponseSetStatusCode (http:Response res, string statusCode) (http:Response) {
    res.setStatusCode(statusCode);
    return res;
}

function testResponseGetMethod (http:Response res) (string) {
    string method = res.getMethod();
    return method;
}

