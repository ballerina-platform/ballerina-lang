import ballerina/test;
import ballerina/io;
import ballerina/http;

boolean serviceStarted;

function startService() {
    //serviceStarted = test:startServices("http-retry");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    http:Client httpEndpoint = new("http://localhost:9090");
    // Check whether the server is started
    //test:assertTrue(serviceStarted, msg = "Unable to start the service");

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

function stopService() {
    //test:stopServices("http_retry");
}
