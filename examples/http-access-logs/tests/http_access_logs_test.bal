import ballerina/test;
import ballerina/http;

// Invoking the main function.
http:Client httpEndpoint = new("http://localhost:9095");

@test:Config
function testFunc() {

    string response1 = "Successful";

    // Send a GET request to the specified endpoint.
    var response = httpEndpoint->get("/hello");
    if (response is http:Response) {
        var res = response.getTextPayload();
        test:assertEquals(res, response1);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}
