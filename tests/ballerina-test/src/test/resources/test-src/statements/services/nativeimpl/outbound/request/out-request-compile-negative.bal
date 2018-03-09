import ballerina.net.http;

function testRequestSetStatusCode (http:Request req, string statusCode) (http:Request) {
    req.setStatusCode(statusCode);
    req.statusCode = 204;
    return req;
}

function testGetMethod (http:Request req) (string ) {
    string method = req.method;
    return method;
}

function testGetRequestURL (http:Request req) (string) {
    string url = req.rawPath;
    if (url == "") {
        url = "no url";
    }
    return url;
}