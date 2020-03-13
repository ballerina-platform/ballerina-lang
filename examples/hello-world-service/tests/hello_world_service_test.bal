import ballerina/test;
import ballerina/http;

boolean serviceStarted = false;

function startService() {
    //serviceStarted = test:startServices("hello-world-service");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function
    http:Client httpEndpoint = new("http://localhost:9090");
    // Check whether the server is started
    //test:assertTrue(serviceStarted, msg = "Unable to start the service");

    string response1 = "Hello, World!";

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        test:assertEquals(response.getTextPayload(), response1);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    //test:stopServices("hello-world-service");
}