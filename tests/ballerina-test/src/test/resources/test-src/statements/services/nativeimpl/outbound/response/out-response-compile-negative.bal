import ballerina.net.http;

function testResponseSetStatusCode (http:Response res, string statusCode) (http:Response) {
    res.statusCode = statusCode;
    return res;
}

function testResponseGetMethod (http:Response res) (string) {
    string method = res.method;
    return method;
}

