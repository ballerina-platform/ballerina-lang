import ballerina/http;
import ballerina/test;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("header-based-routing");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function.
    endpoint http:Client httpEndpoint { url: "http://localhost:9090" };
    // Check whether the server is started.
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    json expectedJson = {
        name: "Colombo,Sri Lanka",
        longitude: -556.49,
        latitude: 257.76,
        altitude: 230
    };

    http:Request req = new;
    req.setHeader("x-type", "location");
    // Send a GET request to the specified endpoint.
    var response = httpEndpoint->get("/hbr/route", message = req);
    match response {
        http:Response resp => {
            var realResponse = check resp.getJsonPayload();
            test:assertEquals(realResponse, expectedJson);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("header-based-routing");
}