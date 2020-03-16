import ballerina/test;
import ballerina/http;

boolean serviceStarted = false;

function startService() {
    //serviceStarted = test:startServices("http-100-continue");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function
    http:Client httpEndpoint = new("http://localhost:9090");
    // Chck whether the server is started
    //test:assertTrue(serviceStarted, msg = "Unable to start the service");

    string response1 = "Hello World!\n";

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->post("/hello", "Hello from client");
    if (response is http:Response) {
        test:assertEquals(response.getTextPayload(), response1);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    //test:stopServices("http-100-continue");
}