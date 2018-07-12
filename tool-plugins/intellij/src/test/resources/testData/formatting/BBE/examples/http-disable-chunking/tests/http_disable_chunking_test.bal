import ballerina/test;
import ballerina/io;
import ballerina/http;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("http-disable-chunking");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function.
    endpoint http:Client httpEndpoint { url: "http://localhost:9092" };
    // Checking whether the server is started.
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    json response1 = { "Outbound request content": "Length-20" };

    // Sending a GET request to the specified endpoint.
    var response = httpEndpoint->get("/chunkingSample");
    match response {
        http:Response resp => {
            var res = check resp.getJsonPayload();
            test:assertEquals(res, response1);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("http_disable_chunking");
}
