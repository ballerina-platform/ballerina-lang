import ballerina/test;
import ballerina/http;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("http-filters");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function
    endpoint http:Client httpEndpoint {url: "http://localhost:9090"};
    // Chck whether the server is started
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    match response {
        http:Response resp => {
            test:assertEquals(resp.getHeader("X-filterName"), "RequestFilter-1");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("http-filters");
}
