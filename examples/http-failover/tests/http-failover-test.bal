import ballerina/test;
import ballerina/io;

boolean serviceStarted;

function startService(){
    serviceStarted = test:startServices("http-failover");
}

@test:Config {
    before:"startService",
    after:"stopService"
}
function testFunc() {
    // Invoking the main function.
    endpoint http:Client httpEndpoint { targets:[{ url:"http://localhost:9090" }] };
    // Checking whether the server is started.
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    http:Request req = new;
    // Sending a GET request to the specified endpoint.
    var response = httpEndpoint -> get("/fo", req);
    match response {
        http:Response resp => {
            var res = check resp.getStringPayload();
            test:assertEquals(res, "Mock Resource is Invoked.");
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService(){
    test:stopServices("http-failover");
}
