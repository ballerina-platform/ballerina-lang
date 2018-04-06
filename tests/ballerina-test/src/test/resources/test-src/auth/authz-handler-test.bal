import ballerina/http;
import ballerina/mime;
import ballerina/runtime;

function testHandleHttpAuthzFailure () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    runtime:getInvocationContext().authenticationContext.username = "ishara";
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic aXNoYXJhOmFiYw==";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2"];
    return handler.handle(inRequest, scopes, "/sayHello");
}

function testHandleAuthzFailureWithNoUsernameInAuthContext () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    http:Request inRequest = createRequest();
    string[] scopes = ["scope2"];
    return handler.handle(inRequest, scopes, "/sayHello");
}

function testCanHandleAuthzFailureWithNoUsernameInAuthContext () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    http:Request inRequest = createRequest();
    return handler.canHandle(inRequest);
}

function testHandleAuthz () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    runtime:getInvocationContext().authenticationContext.username = "isuru";
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2"];
    return handler.handle(inRequest, scopes, "/sayHello");
}

function testCanHandleAuthz () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    runtime:getInvocationContext().authenticationContext.username = "isuru";
    http:Request inRequest = createRequest();
    return handler.canHandle(inRequest);
}

function testHandleAuthzWithMultipleScopes () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    runtime:getInvocationContext().authenticationContext.username = "isuru";
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2", "scope4"];
    return handler.handle(inRequest, scopes, "/sayHello");
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
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = new;
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
