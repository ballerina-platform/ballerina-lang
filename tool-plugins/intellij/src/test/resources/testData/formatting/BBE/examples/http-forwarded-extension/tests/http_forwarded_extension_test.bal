import ballerina/test;
import ballerina/io;
import ballerina/http;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("http-forwarded-extension");
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

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/proxy");
    match response {
        http:Response resp => {
            var res = check resp.getTextPayload();
            string matchWith = "forwarded header value : for=127.0.0.1; by=127.0.0.1; proto=http";
            test:assertEquals(res, matchWith);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("http-forwarded-extension");
}