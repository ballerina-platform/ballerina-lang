import ballerina/test;
import ballerina/http;

@test:Config {}
function testFunc() returns @tainted error? {
    // Invoking the main function.
    http:Client httpEndpoint = new("http://localhost:9090");

    // Send a `POST` request to the specified endpoint.
    json payload = { "hello": "world" };
    var response = httpEndpoint->post("/foo/bar", payload);
    if (response is http:Response) {
        var jsonRes = check response.getJsonPayload();
        test:assertEquals(jsonRes, payload);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
    return;
}
