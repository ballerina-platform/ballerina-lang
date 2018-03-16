import ballerina.io;
import ballerina.net.http;

endpoint<http:Client> clientEndpoint {
    serviceUri: "https://postman-echo.com"
}

function main (string[] args) {
    http:Request req = {};
    // Send a GET request to the specified endpoint
    http:Response resp = {};

    resp, _ = clientEndpoint-> get("/get?test=123", req);
    testFunction(resp, resp);

    var jsonPayload1, payloadError1 = resp.getJsonPayload();
    testFunction(jsonPayload1, jsonPayload1);

    string contentType = resp.getHeader("Content-Type");
    testFunction(contentType, contentType);

    resp, _ = clientEndpoint-> get("/get?test=123" +args[0], req);
}


public function testFunction (@sensitive any sensitiveValue, any anyValue) {

}