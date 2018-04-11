import ballerina/test;
import ballerina/io;

boolean serviceStarted;

function startService(){
    serviceStarted = test:startServices("http-load-balancer");
}

@test:Config {
    before:"startService",
    after:"stopService"
}
function testFunc() {
    // Invoking the main function
    endpoint http:Client httpEndpoint { targets:[{ url:"http://localhost:9090" }] };
    // Chck whether the server is started
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    http:Request req = new;
    // Send a GET request to the specified endpoint
    var response = httpEndpoint -> get("/lb", req);
    match response {
        http:Response resp => {
            var res = check resp.getStringPayload();
            test:assertEquals(res, "Mock1 Resource is invoked.");
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request req2 = new;
    // Send a GET request to the specified endpoint
    var response2 = httpEndpoint -> get("/lb", req2);
    match response2 {
        http:Response resp => {
            var res = check resp.getStringPayload();
            test:assertEquals(res, "Mock2 Resource is Invoked.");
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Invoke the service for the third time
    http:Request req3 = new;
    // Send a GET request to the specified endpoint
    var response3 = httpEndpoint -> get("/lb", req3);
    match response3 {
        http:Response resp => {
            var res = check resp.getStringPayload();
            test:assertEquals(res, "Mock3 Resource is Invoked.");
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Invoke the service for the third time
    http:Request req4 = new;
    // Send a GET request to the specified endpoint
    var response4 = httpEndpoint -> get("/lb", req4);
    match response4 {
        http:Response resp => {
            var res = check resp.getStringPayload();
            test:assertEquals(res, "Mock1 Resource is invoked.");
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService(){
    test:stopServices("http-load-balancer");
}