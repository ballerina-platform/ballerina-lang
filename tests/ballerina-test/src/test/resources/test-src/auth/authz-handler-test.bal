import ballerina/http;
import ballerina/http;
import ballerina/mime;

function testHandleHttpAuthzFailure () returns (boolean) {
    http:HttpAuthzHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXNoYXJhOmFiYw==";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2"];
    return handler.handle(inRequest, scopes, "/sayHello");
}

function testHandleAuthz () returns (boolean) {
    http:HttpAuthzHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2"];
    return handler.handle(inRequest, scopes, "/sayHello");
}

function testHandleAuthzWithMultipleScopes () returns (boolean) {
    http:HttpAuthzHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2", "scope4"];
    return handler.handle(inRequest, scopes, "/sayHello");
}

function testNonExistingBasicAuthHeaderValue () returns (string|null) {
    // create dummy request
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    mime:Entity requestEntity = {};
    inRequest.setEntity(requestEntity);
    return http:extractBasicAuthHeaderValue(inRequest);
}

function testExtractBasicAuthHeaderValue () returns (string|null) {
    // create dummy request
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return http:extractBasicAuthHeaderValue(inRequest);
}
