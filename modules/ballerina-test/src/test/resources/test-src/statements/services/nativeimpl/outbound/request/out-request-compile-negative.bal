import ballerina.net.http;

function testRequestSetStatusCode (http:OutRequest req, string statusCode) (http:OutRequest) {
    req.setStatusCode(statusCode);
    req.statusCode = 204;
    return req;
}

function testGetMethod (http:OutRequest req) (string ) {
    string method = req.getMethod();
    return method;
}

function testGetRequestURL (http:OutRequest req) (string) {
    string url = req.getRequestURL();
    if (url == "") {
        url = "no url";
    }
    return url;
}