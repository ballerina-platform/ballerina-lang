import ballerina/test;
import ballerina/http;

@test:Config {}
function testFunc() returns @tainted  error? {
    http:Client httpEndpoint = new("http://localhost:9092");
    json expectedJson = { "type": "middleware" };

    http:Request companyReq = new;
    companyReq.setHeader("Origin", "http://www.bbc.com");
    // Send a `GET` request to the specified endpoint.
    var companyResponse = httpEndpoint->get("/crossOriginService/company", message = companyReq);
    if (companyResponse is http:Response) {
        var res = check companyResponse.getJsonPayload();
        test:assertEquals(res, expectedJson);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request langReq = new;
    langReq.setHeader("Origin", "http://www.m3.com");
    langReq.setHeader("Access-Control-Request-Method", "POST");
    // Send a `GET` request to the specified endpoint.
    var langResponse = httpEndpoint->options("/crossOriginService/lang", message = langReq);
    if (langResponse is http:Response) {
        // Asserting the header values.
        test:assertEquals(langResponse.getHeader("Access-Control-Allow-Methods"), "POST");
        test:assertEquals(langResponse.getHeader("Access-Control-Allow-Origin"), "http://www.m3.com");
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
    return;
}
