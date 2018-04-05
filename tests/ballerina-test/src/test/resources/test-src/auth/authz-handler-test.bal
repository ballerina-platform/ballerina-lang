import ballerina/http;
import ballerina/mime;
import ballerina/runtime;

function testHandleHttpAuthzFailure () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    runtime:getInvocationContext().authenticationContext.username = "ishara";
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXNoYXJhOmFiYw==";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2"];
    return handler.handle(inRequest, scopes, "/sayHello");
}

function testHandleAuthzFailureWithNoUsernameInAuthContext () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] scopes = ["scope2"];
    return handler.handle(inRequest, scopes, "/sayHello");
}

function testCanHandleAuthzFailureWithNoUsernameInAuthContext () returns (boolean) {
    http:HttpAuthzHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    return handler.canHandle(inRequest);
}

function testHandleAuthz () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    runtime:getInvocationContext().authenticationContext.username = "isuru";
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2"];
    return handler.handle(inRequest, scopes, "/sayHello");
}

function testCanHandleAuthz () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    runtime:getInvocationContext().authenticationContext.username = "isuru";
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    return handler.canHandle(inRequest);
}

function testHandleAuthzWithMultipleScopes () returns (boolean) {
    http:HttpAuthzHandler handler = new;
    runtime:getInvocationContext().authenticationContext.username = "isuru";
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2", "scope4"];
    return handler.handle(inRequest, scopes, "/sayHello");
}

function testNonExistingBasicAuthHeaderValue () returns (string|()) {
    // create dummy request
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    mime:Entity requestEntity = {};
    inRequest.setEntity(requestEntity);
    return http:extractBasicAuthHeaderValue(inRequest);
}

function testExtractBasicAuthHeaderValue () returns (string|()) {
    // create dummy request
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return http:extractBasicAuthHeaderValue(inRequest);
}
