import ballerina/http;
import ballerina/test;

boolean serviceStarted;

function startService() {
    //serviceStarted = test:startServices("header-based-routing");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() returns error? {
    // Invoking the main function.
    http:Client httpEndpoint = new("http://localhost:9090");
    // Check whether the server is started.
    //test:assertTrue(serviceStarted, msg = "Unable to start the service");

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
    if (response is http:Response) {
        var realResponse = check response.getJsonPayload();
        test:assertEquals(realResponse, expectedJson);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
    return;
}

function stopService() {
    //test:stopServices("header-based-routing");
}