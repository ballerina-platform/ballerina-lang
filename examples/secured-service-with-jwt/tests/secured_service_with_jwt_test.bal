import ballerina/config;
import ballerina/http;
import ballerina/test;
import ballerina/runtime;

boolean serviceStarted;

function startService() {
    config:setConfig("b7a.users.ballerina.scopes", "hello");
    serviceStarted = test:startServices("secured-service-with-jwt");
}

@test:Config {
    enable: true,
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Chck whether the server is started
    test:assertTrue(serviceStarted, msg = "Unable to start the service");
    setJwtTokenToAuthContext();
    testAuthSuccess();
    clearTokenFromAuthContext();
    testAuthnFailure();
    setJwtTokenWithNoScopesToAuthContext();
    //testAuthzFailure();
    //clearTokenFromAuthContext();
}

function testAuthSuccess() {
    // create client
    endpoint http:Client httpEndpoint {
        url: "https://localhost:9090",
        auth: { scheme: "jwt" }
    };
    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    match response {
        http:Response resp => {
            test:assertEquals(resp.statusCode, 200,
                msg = "Expected status code 200 not received");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function testAuthnFailure() {
    // create client
    endpoint http:Client httpEndpoint {
        url: "https://localhost:9090",
        auth: { scheme: "jwt" }
    };
    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    match response {
        http:Response resp => {
            test:assertEquals(resp.statusCode, 401,
                msg = "Expected status code 401 not received");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function testAuthzFailure() {
    // create client
    endpoint http:Client httpEndpoint { url: "https://localhost:9090",
        auth: { scheme: "jwt" } };
    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    match response {
        http:Response resp => {
            test:assertEquals(resp.statusCode, 403, msg =
                "Expected status code 403 not received");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function setJwtTokenToAuthContext () {
    string token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJ" +
        "pbmEiLCJpc3MiOiJiYWxsZXJpbmEiLCJleHAiOjI4MTg0MTUwMTksImlhdCI6MTUyND" +
        "U3NTAxOSwianRpIjoiZjVhZGVkNTA1ODVjNDZmMmI4Y2EyMzNkMGMyYTNjOWQiLCJhdW" +
        "QiOlsiYmFsbGVyaW5hIiwiYmFsbGVyaW5hLm9yZyIsImJhbGxlcmluYS5pbyJdLCJzY" +
        "29wZSI6ImhlbGxvIn0.bNoqz9_DzgeKSK6ru3DnKL7NiNbY32ksXPYrh6Jp0_O3ST7W" +
        "fXMs9WVkx6Q2TiYukMAGrnMUFrJnrJvZwC3glAmRBrl4BYCbQ0c5mCbgM9qhhCjC1tB" +
        "A50rjtLAtRW-JTRpCKS0B9_EmlVKfvXPKDLIpM5hnfhOin1R3lJCPspJ2ey_Ho6fDhs" +
        "KE3DZgssvgPgI9PBItnkipQ3CqqXWhV-RFBkVBEGPDYXTUVGbXhdNOBSwKw5ZoVJrCU" +
        "iNG5XD0K4sgN9udVTi3EMKNMnVQaq399k6RYPAy3vIhByS6QZtRjOG8X93WJw-9GLiH" +
        "vcabuid80lnrs2-mAEcstgiHVw";
    runtime:getInvocationContext().authContext.scheme = "jwt";
    runtime:getInvocationContext().authContext.authToken = token;
}

function clearTokenFromAuthContext () {
    runtime:getInvocationContext().authContext.scheme = "jwt";
    runtime:getInvocationContext().authContext.authToken = "";
}

function setJwtTokenWithNoScopesToAuthContext () {
    string token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZX" +
        "JpbmEiLCJpc3MiOiJiYWxsZXJpbmEiLCJleHAiOjI4MTg0MTUwMTksImlhdCI6MTUy" +
        "NDU3NTAxOSwianRpIjoiZjVhZGVkNTA1ODVjNDZmMmI4Y2EyMzNkMGMyYTNjOWQiLCJ" +
        "hdWQiOlsiYmFsbGVyaW5hIiwiYmFsbGVyaW5hLm9yZyIsImJhbGxlcmluYS5pbyJdf" +
        "Q.X2mHWCr8A5UaJFvjSPUammACnTzFsTdre-P5yWQgrwLBmfcpr9JaUuq4sEwp6to3" +
        "xSKN7u9QKqRLuWH1SlcphDQn6kdF1ZrCgXRQ0HQTilZQU1hllZ4c7yMNtMgMIaPgEB" +
        "rStLX1Ufr6LpDkTA4VeaPCSqstHt9WbRzIoPQ1fCxjvHBP17ShiGPRza9p_Z4t897s" +
        "40aQMKbKLqLQ8rEaYAcsoRBXYyUhb_PRS-YZtIdo7iVmkMVFjYjHvmYbpYhNo57Z1Y" +
        "5dNa8h8-4ON4CXzcJ1RzuyuFVz1a3YL3gWTsiliVmno7vKyRo8utirDRIPi0dPJPuW" +
        "i2uMtJkqdkpzJQ";
    runtime:getInvocationContext().authContext.scheme = "jwt";
    runtime:getInvocationContext().authContext.authToken = token;
}

function stopService() {
    test:stopServices("secured-service-with-jwt");
}
