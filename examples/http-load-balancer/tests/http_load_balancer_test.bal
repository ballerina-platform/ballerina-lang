import ballerina/test;
import ballerina/io;
import ballerina/http;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("http-load-balancer");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function
    endpoint http:Client httpEndpoint { url: "http://localhost:9090" };
    // Chck whether the server is started
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/lb");
    match response {
        http:Response resp => {
            var res = check resp.getTextPayload();
            test:assertEquals(res, "Mock1 Resource is invoked.");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Send a GET request to the specified endpoint
    var response2 = httpEndpoint->get("/lb");
    match response2 {
        http:Response resp => {
            var res = check resp.getTextPayload();
            test:assertEquals(res, "Mock2 Resource is Invoked.");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Invoke the service for the third time
    // Send a GET request to the specified endpoint
    var response3 = httpEndpoint->get("/lb");
    match response3 {
        http:Response resp => {
            var res = check resp.getTextPayload();
            test:assertEquals(res, "Mock3 Resource is Invoked.");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Invoke the service for the third time
    // Send a GET request to the specified endpoint
    var response4 = httpEndpoint->get("/lb");
    match response4 {
        http:Response resp => {
            var res = check resp.getTextPayload();
            test:assertEquals(res, "Mock1 Resource is invoked.");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("http_load_balancer");
}