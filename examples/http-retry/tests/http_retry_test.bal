import ballerina/test;
import ballerina/io;
import ballerina/http;

@test:Config
function testFunc() {
    http:Client httpEndpoint = new("http://localhost:9090");

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/retry");
    if (response is http:Response) {
        var result = response.getTextPayload();
        if (result is string) {
            test:assertEquals(result, "Hello World!!!");
        } else {
            test:assertFail(msg = "Invalid response message");
        }
    } else {
        test:assertFail(msg = "Failed to call the endpoint");
    }
}
