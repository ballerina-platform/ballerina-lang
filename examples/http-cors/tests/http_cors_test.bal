import ballerina/test;
import ballerina/io;
import ballerina/http;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("http-cors");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function.
    endpoint http:Client httpEndpoint {url: "http://localhost:9092"};
    // Checking whether the server is started.
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    json response1 = {"type": "middleware"};

    http:Request req = new;
    req.setHeader("Origin", "http://www.bbc.com");
    // Sending a GET request to the specified endpoint.
    var response = httpEndpoint->get("/crossOriginService/company", request = req);
    match response {
        http:Response resp => {
            var res = check resp.getJsonPayload();
            test:assertEquals(res, response1);
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request req2 = new;
    req2.setHeader("Origin", "http://www.m3.com");
    req2.setHeader("Access-Control-Request-Method", "POST");
    // Sending a GET request to the specified endpoint.
    var response2 = httpEndpoint->options("/crossOriginService/lang", request = req2);
    match response2 {
        http:Response resp => {
            var res = check resp.getStringPayload();
            test:assertEquals(res, "");
            // Asserting the header values.
            test:assertEquals(resp.getHeader("Access-Control-Allow-Methods"), "POST");
            test:assertEquals(resp.getHeader("Access-Control-Allow-Origin"), "http://www.m3.com");
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("http-cors");
}
