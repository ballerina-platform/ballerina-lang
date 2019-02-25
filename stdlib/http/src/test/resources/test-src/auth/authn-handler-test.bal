import ballerina/auth;
import ballerina/http;

function testCanHandleHttpBasicAuthWithoutHeader() returns (boolean) {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    auth:AuthStoreProvider authStoreProvider = configAuthStoreProvider;
    http:HttpBasicAuthnHandler handler = new(authStoreProvider);
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "123Basic xxxxxx";
    inRequest.setHeader("123Authorization", basicAutheaderValue);
    return handler.canHandle(inRequest);
}

function testCanHandleHttpBasicAuth() returns (boolean) {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    auth:AuthStoreProvider authStoreProvider = configAuthStoreProvider;
    http:HttpBasicAuthnHandler handler = new(authStoreProvider);
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic xxxxxx";
    inRequest.setHeader("Authorization", basicAutheaderValue);
    return handler.canHandle(inRequest);
}

function testHandleHttpBasicAuthFailure() returns (boolean) {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    auth:AuthStoreProvider authStoreProvider = configAuthStoreProvider;
    http:HttpBasicAuthnHandler handler = new(authStoreProvider);
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic YW1pbGE6cHFy";
    inRequest.setHeader("Authorization", basicAutheaderValue);
    return handler.handle(inRequest);
}

function testHandleHttpBasicAuth() returns (boolean) {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    auth:AuthStoreProvider authStoreProvider = configAuthStoreProvider;
    http:HttpBasicAuthnHandler handler = new(authStoreProvider);
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    inRequest.setHeader("Authorization", basicAutheaderValue);
    return handler.handle(inRequest);
}

function testNonExistingBasicAuthHeaderValue() returns (string|()) {
    // create dummy request
    http:Request inRequest = createRequest();
    return http:extractBasicAuthHeaderValue(inRequest);
}

function testExtractBasicAuthHeaderValue() returns (string|()) {
    // create dummy request
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    inRequest.setHeader("Authorization", basicAutheaderValue);
    return http:extractBasicAuthHeaderValue(inRequest);
}

function createRequest() returns (http:Request) {
    http:Request inRequest = new;
    inRequest.rawPath = "/helloWorld/sayHello";
    inRequest.method = "GET";
    inRequest.httpVersion = "1.1";
    return inRequest;
}
