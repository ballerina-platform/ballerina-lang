import ballerina/http;
import ballerina/mime;
import ballerina/auth;

function testCanHandleHttpBasicAuthWithoutHeader () returns (boolean) {
    auth:ConfigAuthProvider configAuthProvider = new;
    auth:AuthProvider authProvider = check <auth:AuthProvider> configAuthProvider;
    http:HttpBasicAuthnHandler handler = new(authProvider);
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "123Basic xxxxxx";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("123Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.canHandle(inRequest);
}

function testCanHandleHttpBasicAuth () returns (boolean) {
    auth:ConfigAuthProvider configAuthProvider = new;
    auth:AuthProvider authProvider = check <auth:AuthProvider> configAuthProvider;
    http:HttpBasicAuthnHandler handler = new(authProvider);
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic xxxxxx";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.canHandle(inRequest);
}

function testHandleHttpBasicAuthFailure () returns (boolean) {
    auth:ConfigAuthProvider configAuthProvider = new;
    auth:AuthProvider authProvider = check <auth:AuthProvider> configAuthProvider;
    http:HttpBasicAuthnHandler handler = new(authProvider);
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic YW1pbGE6cHFy";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest);
}

function testHandleHttpBasicAuth () returns (boolean) {
    auth:ConfigAuthProvider configAuthProvider = new;
    auth:AuthProvider authProvider = check <auth:AuthProvider> configAuthProvider;
    http:HttpBasicAuthnHandler handler = new(authProvider);
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest);
}

function testNonExistingBasicAuthHeaderValue () returns (string|()) {
    // create dummy request
    http:Request inRequest = createRequest();
    mime:Entity requestEntity = new;
    inRequest.setEntity(requestEntity);
    return http:extractBasicAuthHeaderValue(inRequest);
}

function testExtractBasicAuthHeaderValue () returns (string|()) {
    // create dummy request
    http:Request inRequest = createRequest();
    mime:Entity requestEntity = new;
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return http:extractBasicAuthHeaderValue(inRequest);
}

function createRequest () returns (http:Request) {
    http:Request inRequest = new;
    inRequest.rawPath = "/helloWorld/sayHello";
    inRequest.method = "GET";
    inRequest.httpVersion = "1.1";
    return inRequest;
}
