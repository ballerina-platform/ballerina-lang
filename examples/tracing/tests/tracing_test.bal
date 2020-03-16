import ballerina/test;
import ballerina/http;

@test:Config { }
function testFunc() {
    // Invoking the service
    http:Client httpEndpoint = new ("http://localhost:9234");

    string response1 = "Hello, World!";

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    if response is http:Response {
        var res = response.getTextPayload();
        if res is error {
            test:assertFail(msg = "Failed to call the endpoint:");
        } else {
            test:assertEquals(res, response1);
        }
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}
