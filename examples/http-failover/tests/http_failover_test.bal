import ballerina/test;
import ballerina/io;
import ballerina/http;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("http-failover");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function.
    endpoint http:Client httpEndpoint { url: "http://localhost:9090" };
    // Checking whether the server is started.
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    // Sending a GET request to the specified endpoint.
    var response = httpEndpoint->get("/fo");
    match response {
        http:Response resp => {
            var res = check resp.getTextPayload();
            test:assertEquals(res, "Mock Resource is Invoked.");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("http_failover");
}
