import ballerina/test;
import ballerina/io;
import ballerina/http;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("http-retry");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    endpoint http:Client httpEndpoint { url: "http://localhost:9090" };
    // Chck whether the server is started
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/retry");
    match response {
        http:Response resp => {
            var res = check resp.getTextPayload();
            test:assertEquals(res, "Hello World!!!");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("http_retry");
}
