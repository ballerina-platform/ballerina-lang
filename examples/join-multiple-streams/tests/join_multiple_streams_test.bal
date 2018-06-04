import ballerina/test;
import ballerina/io;
import ballerina/http;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("join-multiple-streams");
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoke the main function. 
    endpoint http:Client httpEndpoint { url: "http://localhost:9090" };
    // Chck whether the server is started
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    json clientResp1 = { "message": "Raw material request successfully received" };
    json clientResp2 = { "message": "Production input request successfully received" };

    http:Request req = new;
    req.setJsonPayload({ "name": "Teak", "amount": 1000.0 });
    // Send a `GET` request to the specified endpoint.
    var response = httpEndpoint->post("/rawmaterial", req);
    match response {
        http:Response resp => {
            var res = check resp.getJsonPayload();
            test:assertEquals(res, clientResp1);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    http:Request req2 = new;
    req2.setJsonPayload({ "name": "Teak", "amount": 500.0 });
    // Send a `GET` request to the specified endpoint.
    var response2 = httpEndpoint->post("/productionmaterial", req2);
    match response2 {
        http:Response resp => {
            var res = check resp.getJsonPayload();
            test:assertEquals(res, clientResp2);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("join_multiple_streams");
}
