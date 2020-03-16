import ballerina/http;
import ballerina/test;

@test:Config {}
function testFunc() returns @tainted error? {
    // Invoking the main function.
    http:Client httpEndpoint = new("http://localhost:9090");
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
