import ballerina/http;
import ballerina/io;
import ballerina/test;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("content-based-routing");
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
    json payload = { name: "sanFrancisco" };
    json payload2 = { name: "london" };

    json response1 = {
        name: "San Francisco Test Station,USA",
        longitude: -122.43,
        latitude: 37.76,
        altitude: 150,
        rank: 1
    };

    json response2 = {
        name: "London Test Station,England",
        longitude: -156.49,
        latitude: 57.76,
        altitude: 430,
        rank: 5
    };

    http:Request req = new;
    req.setJsonPayload(payload);
    // Send a GET request to the specified endpoint
    var response = httpEndpoint->post("/cbr/route", req);
    match response {
        http:Response resp => {
            var jsonRes = check resp.getJsonPayload();
            test:assertEquals(jsonRes, response1);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request req2 = new;
    req2.setJsonPayload(payload2);
    // Send a GET request to the specified endpoint
    var respnc = httpEndpoint->post("/cbr/route", req2);
    match respnc {
        http:Response resp => {
            var jsonRes = check resp.getJsonPayload();
            test:assertEquals(jsonRes, response2);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("content-based-routing");
}