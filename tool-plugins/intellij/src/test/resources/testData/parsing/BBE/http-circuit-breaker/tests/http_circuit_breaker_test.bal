import ballerina/test;
import ballerina/io;
import ballerina/http;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("http-circuit-breaker");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function.
    endpoint http:Client httpEndpoint { url: "http://localhost:9090" };
    // Check whether the server has started.
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    string response1 = "Hello World!!!";
    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/cb");
    match response {
        http:Response resp => {
            var res = check resp.getTextPayload();
            test:assertEquals(res, response1);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Send a GET request to the specified endpoint.
    var response2 = httpEndpoint->get("/cb");
    match response2 {
        http:Response resp => {
            var res = check resp.getTextPayload();
            test:assertEquals(res, response1);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Send a GET request to the specified endpoint.
    var response3 = httpEndpoint->get("/cb");
    match response3 {
        http:Response resp => {
            var res = check resp.getTextPayload();
            test:assertEquals(res, "Internal error occurred while processing the request.");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Send a GET request to the specified endpoint.
    var response4 = httpEndpoint->get("/cb");
    match response4 {
        http:Response resp => {
            var res = check resp.getTextPayload();
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Send a GET request to the specified endpoint.
    var response5 = httpEndpoint->get("/cb");
    match response5 {
        http:Response resp => {
            var res = check resp.getTextPayload();
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    io:println("Reached");

    // Send a GET request to the specified endpoint.
    var response6 = httpEndpoint->get("/cb");
    match response6 {
        http:Response resp => {
            var res = check resp.getTextPayload();
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("http_circuit_breaker");
}
