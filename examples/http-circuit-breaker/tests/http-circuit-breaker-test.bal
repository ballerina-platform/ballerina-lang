import ballerina/test;
import ballerina/io;

boolean serviceStarted;

function startService(){
    serviceStarted = test:startServices("http-circuit-breaker");
}

@test:Config {
    before:"startService",
    after:"stopService"
}
function testFunc() {
    // Invoking the main function.
    endpoint http:Client httpEndpoint { targets:[{ url:"http://localhost:9090" }] };
    // Check whether the server has started.
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    string response1 = "Hello World!!!";
    http:Request req = new;
    // Send a GET request to the specified endpoint
    var response = httpEndpoint -> get("/cb", req);
    match response {
        http:Response resp => {
            var res = check resp.getStringPayload();
            test:assertEquals(res, response1);
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request req2 = new;
    // Send a GET request to the specified endpoint.
    var response2 = httpEndpoint -> get("/cb", req2);
    match response2 {
        http:Response resp => {
            var res = check resp.getStringPayload();
            test:assertEquals(res, response1);
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request req3 = new;
    // Send a GET request to the specified endpoint.
    var response3 = httpEndpoint -> get("/cb", req3);
    match response3 {
        http:Response resp => {
            var res = check resp.getStringPayload();
            test:assertEquals(res, "Internal error occurred while processing the request.");
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request req4 = new;
    // Send a GET request to the specified endpoint.
    var response4 = httpEndpoint -> get("/cb", req4);
    match response4 {
        http:Response resp => {
            var res = check resp.getStringPayload();
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request req5 = new;
    // Send a GET request to the specified endpoint.
    var response5 = httpEndpoint -> get("/cb", req5);
    match response5 {
        http:Response resp => {
            var res = check resp.getStringPayload();
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    io:println("Reached");

    http:Request req6 = new;
    // Send a GET request to the specified endpoint.
    var response6 = httpEndpoint -> get("/cb", req6);
    match response6 {
        http:Response resp => {
            var res = check resp.getStringPayload();
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService(){
    test:stopServices("http-circuit-breaker");
}
