import ballerina/net.http.authadaptor;
import ballerina/net.http;
import ballerina/mime;

function testCanHandleHttpBasicAuthWithoutHeader () returns (boolean) {
    authadaptor:HttpBasicAuthnHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "123Basic xxxxxx";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("123Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.canHandle(inRequest);
}

function testCanHandleHttpBasicAuth () returns (boolean) {
    authadaptor:HttpBasicAuthnHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic xxxxxx";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.canHandle(inRequest);
}

function testHandleHttpBasicAuthFailure () returns (boolean) {
    authadaptor:HttpBasicAuthnHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic YW1pbGE6cHFy";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest);
}

function testHandleHttpBasicAuth () returns (boolean) {
    authadaptor:HttpBasicAuthnHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest);
}

function testExtractInvalidBasicAuthHeaderValue () returns (string|error) {
    // create dummy request
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = ".Basic FSADFfgfsagas423gfdGSdfa";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authadaptor:extractBasicAuthHeaderValue(inRequest);
}

function testExtractBasicAuthHeaderValue () returns (string|error) {
    // create dummy request
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authadaptor:extractBasicAuthHeaderValue(inRequest);
}
