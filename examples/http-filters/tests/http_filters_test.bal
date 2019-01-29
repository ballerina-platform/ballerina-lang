import ballerina/test;
import ballerina/http;

@test:Config
function testFunc() {
    http:Client httpEndpoint = new("http://localhost:9090");

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        test:assertEquals(response.getHeader("X-filterName"), "RequestFilter-1");
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}
