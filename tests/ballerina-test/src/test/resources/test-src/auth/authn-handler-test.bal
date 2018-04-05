import ballerina/http;
import ballerina/mime;

function testCanHandleHttpBasicAuthWithoutHeader () returns (boolean) {
    http:HttpBasicAuthnHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "123Basic xxxxxx";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("123Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.canHandle(inRequest);
}

function testCanHandleHttpBasicAuth () returns (boolean) {
    http:HttpBasicAuthnHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic xxxxxx";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.canHandle(inRequest);
}

function testHandleHttpBasicAuthFailure () returns (boolean) {
    http:HttpBasicAuthnHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic YW1pbGE6cHFy";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest);
}

function testHandleHttpBasicAuth () returns (boolean) {
    http:HttpBasicAuthnHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest);
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
