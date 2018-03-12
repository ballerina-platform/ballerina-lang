import ballerina.net.http;

function testResponseSetStatusCode (http:OutResponse res, string statusCode) (http:OutResponse) {
    res.statusCode = statusCode;
    return res;
}

function testResponseGetMethod (http:InResponse res) (string) {
    string method = res.method;
    return method;
}

