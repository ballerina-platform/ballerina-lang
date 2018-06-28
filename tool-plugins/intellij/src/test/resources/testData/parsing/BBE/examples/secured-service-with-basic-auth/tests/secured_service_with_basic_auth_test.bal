import ballerina/config;
import ballerina/http;
import ballerina/test;

boolean serviceStarted;

function startService() {
    config:setConfig("b7a.users.tom.password", "password1");
    config:setConfig("b7a.users.tom.scopes", "scope2,scope3");
    config:setConfig("b7a.users.dick.password", "password2");
    config:setConfig("b7a.users.dick.scopes", "scope1");
    serviceStarted = test:startServices("secured-service-with-basic-auth");
}

@test:Config {
    enable: true,
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Check whether the server has started.
    test:assertTrue(serviceStarted, msg = "Unable to start the service");
    testAuthSuccess();
    testAuthnFailure();
    testAuthzFailure();
}

function testAuthSuccess() {
    // Create client. 
    endpoint http:Client httpEndpoint {
        url: "https://localhost:9090",
        auth: { scheme: http:BASIC_AUTH, username: "tom", password: "password1" }
    };
    // Send a `GET` request to the specified endpoint.
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
    // Create client.
    endpoint http:Client httpEndpoint {
        url: "https://localhost:9090",
        auth: { scheme: http:BASIC_AUTH, username: "tom", password: "password" }
    };
    // Send a `GET` request to the specified endpoint.
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
    // Create client.
    endpoint http:Client httpEndpoint { url: "https://localhost:9090",
        auth: { scheme: http:BASIC_AUTH, username: "dick", password: "password2" } };
    // Send a `GET` request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    match response {
        http:Response resp => {
            test:assertEquals(resp.statusCode, 403, msg =
                "Expected status code 403 not received");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("secured-service-with-basic-auth");
}
