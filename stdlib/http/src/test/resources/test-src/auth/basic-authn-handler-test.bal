import ballerina/auth;
import ballerina/http;

function testCanHandleHttpBasicAuthWithoutHeader() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    http:BasicAuthnHandler handler = new(configAuthStoreProvider);
    http:Request inRequest = createRequest();
    string basicAuthHeaderValue = "123Basic xxxxxx";
    inRequest.setHeader("123Authorization", basicAuthHeaderValue);
    return handler.canHandle(inRequest);
}

function testCanHandleHttpBasicAuth() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    http:BasicAuthnHandler handler = new(configAuthStoreProvider);
    http:Request inRequest = createRequest();
    string basicAuthHeaderValue = "Basic xxxxxx";
    inRequest.setHeader("Authorization", basicAuthHeaderValue);
    return handler.canHandle(inRequest);
}

function testHandleHttpBasicAuthFailure() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    http:BasicAuthnHandler handler = new(configAuthStoreProvider);
    http:Request inRequest = createRequest();
    string basicAuthHeaderValue = "Basic YW1pbGE6cHFy";
    inRequest.setHeader("Authorization", basicAuthHeaderValue);
    return handler.handle(inRequest);
}

function testHandleHttpBasicAuth() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    http:BasicAuthnHandler handler = new(configAuthStoreProvider);
    http:Request inRequest = createRequest();
    string basicAuthHeaderValue = "Basic aXN1cnU6eHh4";
    inRequest.setHeader("Authorization", basicAuthHeaderValue);
    return handler.handle(inRequest);
}

function testNonExistingBasicAuthHeaderValue() returns string|error {
    // create dummy request
    http:Request inRequest = createRequest();
    return http:extractAuthorizationHeaderValue(inRequest);
}

function testExtractBasicAuthHeaderValue() returns string|error {
    // create dummy request
    http:Request inRequest = createRequest();
    string basicAuthHeaderValue = "Basic aXN1cnU6eHh4";
    inRequest.setHeader("Authorization", basicAuthHeaderValue);
    return http:extractAuthorizationHeaderValue(inRequest);
}

function createRequest() returns http:Request {
    http:Request inRequest = new;
    inRequest.rawPath = "/helloWorld/sayHello";
    inRequest.method = "GET";
    inRequest.httpVersion = "1.1";
    return inRequest;
}
