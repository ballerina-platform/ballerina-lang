import ballerina/http;
import ballerina/io;
import ballerina/test;

@test:Config
function testFunc() {
    endpoint http:Client httpEndpoint { url: "http://localhost:9090" };
    endpoint http:Client httpPassthroughEndpoint { url: "http://localhost:9092" };

    json expectedJson = {"Type":"Always but constrained by content-type"};
    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/alwaysCompress/getJson");
    // Assert the uncompressed response
    match response {
        http:Response resp => {
            json res = check resp.getJsonPayload();
            test:assertEquals(res, expectedJson);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    string expectedString = "Backend response was encoded : deflate, gzip";
    // Send a GET request to the passthrough endpoint
    response = httpPassthroughEndpoint->get("/passthrough/");
    // Assert the response of passthrough
    match response {
        http:Response resp => {
            string res = check resp.getTextPayload();
            test:assertEquals(res, expectedString);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}
