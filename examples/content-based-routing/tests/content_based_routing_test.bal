import ballerina/http;
import ballerina/io;
import ballerina/test;

@test:Config
function testFunc() returns error? {
    // Invoking the main function
    http:Client httpEndpoint = new("http://localhost:9090");
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
    if (response is http:Response) {
        var jsonRes = check response.getJsonPayload();
        test:assertEquals(jsonRes, response1);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request req2 = new;
    req2.setJsonPayload(payload2);
    // Send a GET request to the specified endpoint
    var respnc = httpEndpoint->post("/cbr/route", req2);
    if (respnc is http:Response) {
        var jsonRes = check respnc.getJsonPayload();
        test:assertEquals(jsonRes, response2);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
    return;
}
