import ballerina/test;
import ballerina/http;

boolean serviceStarted = false;

function startService() {
    //serviceStarted = test:startServices("http-disable-chunking");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function.
    http:Client httpEndpoint = new("http://localhost:9092");
    // Checking whether the server is started.
    //test:assertTrue(serviceStarted, msg = "Unable to start the service");

    json response1 = { "Outbound request content": "Length-20" };

    // Sending a GET request to the specified endpoint.
    var response = httpEndpoint->get("/chunkingSample");
    if (response is http:Response) {
        test:assertEquals(response.getJsonPayload(), response1);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    //test:stopServices("http_disable_chunking");
}
