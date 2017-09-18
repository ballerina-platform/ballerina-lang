import ballerina.net.http;
import ballerina.net.http.request as req;

function testAddHeader(http:Request request, string key, string value) (http:Request) {
    req:addHeader(request, key, value);
    return request;
}


