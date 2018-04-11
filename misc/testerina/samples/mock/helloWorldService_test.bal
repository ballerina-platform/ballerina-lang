package mock;

import ballerina/http;
import ballerina/io;
import ballerina/test;
import ballerina/config;


string url = "http://0.0.0.0:9092/hello";
boolean isHelloServiceStarted;

function startMock () {
    isHelloServiceStarted = test:startServices("mock");
}

function stopMock () {
    test:stopServices("mock");
}

@test:Config{
    before: "startMock",
    after:"stopMock"
}
function testService () {
    endpoint http:Client httpEndpoint {
        targets:[{ uri:url }]
    };

    // Check whether the service is started
    test:assertTrue(isHelloServiceStarted, msg = "Hello service failed to start");

    http:Request req = {};
    // Send a GET request to the specified endpoint
    io:println("GET request:");
    var response = httpEndpoint -> get("/hello", req);
    match response {
        http:Response resp => {
            var jsonRes = resp.getJsonPayload();
            json expected = {"Hello":"World"};
            test:assertEquals(jsonRes, expected);
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint: " +url);
    }
}
