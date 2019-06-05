import ballerina/auth;
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
    auth:OutboundBasicAuthProvider outboundBasicAuthProvider1 = new({ username: "tom", password: "password1" });
    http:BasicAuthHeaderAuthnHandler outboundBasicAuthnHandler1 = new(outboundBasicAuthProvider1);
    http:Client httpEndpoint = new("https://localhost:9090", config = {
        auth: {
            authnHandler: outboundBasicAuthnHandler1
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
    auth:OutboundBasicAuthProvider outboundBasicAuthProvider2 = new({ username: "tom", password: "password" });
    http:BasicAuthHeaderAuthnHandler outboundBasicAuthnHandler2 = new(outboundBasicAuthProvider2);
    http:Client httpEndpoint = new("https://localhost:9090", config = {
        auth: {
            authnHandler: outboundBasicAuthnHandler2
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
    auth:OutboundBasicAuthProvider outboundBasicAuthProvider3 = new({ username: "dick", password: "password2" });
    http:BasicAuthHeaderAuthnHandler outboundBasicAuthnHandler3 = new(outboundBasicAuthProvider3);
    http:Client httpEndpoint = new("https://localhost:9090", config = {
        auth: {
            authnHandler: outboundBasicAuthnHandler3
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
