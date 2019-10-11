import ballerina/auth;
import ballerina/config;
import ballerina/http;
import ballerina/test;

@test:Config {}
function testAuthSuccess() {
    // Creates a client.
    auth:OutboundBasicAuthProvider outboundBasicAuthProvider1 = new({ username: "generalUser2", password: "password" });
    http:BasicAuthHandler outboundBasicAuthHandler1 = new(outboundBasicAuthProvider1);
    http:Client httpEndpoint = new("https://localhost:9090", {
        auth: {
            authHandler: outboundBasicAuthHandler1
        }
    });
    // Sends a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        test:assertEquals(response.statusCode, 200,
            msg = "Expected status code 200 not received");
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}

@test:Config {}
function testAuthnFailure() {
    // Creates a client.
    auth:OutboundBasicAuthProvider outboundBasicAuthProvider2 = new({ username: "invalidUser", password: "password" });
    http:BasicAuthHandler outboundBasicAuthHandler2 = new(outboundBasicAuthProvider2);
    http:Client httpEndpoint = new("https://localhost:9090", {
        auth: {
            authHandler: outboundBasicAuthHandler2
        }
    });
    // Sends a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        test:assertEquals(response.statusCode, 401,
            msg = "Expected status code 401 not received");
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}

@test:Config {}
function testAuthzFailure() {
    // Creates a client.
    auth:OutboundBasicAuthProvider outboundBasicAuthProvider3 = new({ username: "generalUser1", password: "password" });
    http:BasicAuthHandler outboundBasicAuthHandler3 = new(outboundBasicAuthProvider3);
    http:Client httpEndpoint = new("https://localhost:9090", {
        auth: {
            authHandler: outboundBasicAuthHandler3
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
