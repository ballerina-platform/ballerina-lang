import ballerina/http;
import ballerina/test;

@test:Config {}
function testFunc() returns @tainted error? {
    http:Client httpEndpoint = new("http://localhost:9090");
    http:Client httpPassthroughEndpoint = new("http://localhost:9092");

    json expectedJson = {"Type":"Always but constrained by content-type"};
    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/alwaysCompress/getJson");
    // Assert the uncompressed response
    if (response is http:Response) {
        json actualPayload = check response.getJsonPayload();
        test:assertEquals(actualPayload, expectedJson);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }

    string expectedString = "Backend response was encoded : deflate, gzip";
    // Send a GET request to the passthrough endpoint
    response = httpPassthroughEndpoint->get("/passthrough/");
    // Assert the response of passthrough
    if (response is http:Response) {
        string actualPayload = check response.getTextPayload();
        test:assertEquals(actualPayload, expectedString);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
    return;
}
