import ballerina/config;
import ballerina/http;
import ballerina/test;

function startService() {
    config:setConfig("b7a.users.tom.password", "password1");
    config:setConfig("b7a.users.tom.scopes", "scope2,scope3");
    config:setConfig("b7a.users.dick.password", "password2");
    config:setConfig("b7a.users.dick.scopes", "scope1");
}

@test:Config {
    before: "startService"
}
function testFunc() {
    testAuthSuccess();
    testAuthnFailure();
    testAuthzFailure();
}

function testAuthSuccess() {
    // Create client. 
    http:Client httpEndpoint = new("https://localhost:9090", config = {
        auth: {
            scheme: http:BASIC_AUTH,
            username: "tom",
            password: "password1"
        }
    });
    // Send a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        test:assertEquals(response.statusCode, 200,
            msg = "Expected status code 200 not received");
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function testAuthnFailure() {
    // Create client.
    http:Client httpEndpoint = new("https://localhost:9090", config = {
        auth: {
            scheme: http:BASIC_AUTH,
            username: "tom",
            password: "password"
        }
    });
    // Send a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        test:assertEquals(response.statusCode, 401,
            msg = "Expected status code 401 not received");
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function testAuthzFailure() {
    // Create client.
    http:Client httpEndpoint = new("https://localhost:9090", config = {
        auth: {
            scheme: http:BASIC_AUTH,
            username: "dick",
            password: "password2"
        }
    });
    // Send a `GET` request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        test:assertEquals(response.statusCode, 403,
            msg = "Expected status code 403 not received");
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}
