import ballerina/test;
import ballerina/io;
import ballerina/http;

@test:Config
function testFunc() {
    // Invoking the service on localhost:9090.
    http:Client httpEndpoint = new("http://localhost:9090");

    string expectedResponse = "Hello World!";

    // Send a GET request to the specified endpoint
    var actualResponse = httpEndpoint->get("/hello/hi");
    if (actualResponse is http:Response) {
        test:assertEquals(actualResponse.getTextPayload(), expectedResponse);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}
