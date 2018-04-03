import ballerina/http;
import ballerina/http;
import ballerina/mime;

function testCanHandleHttpJwtAuthWithoutHeader () returns (boolean) {
    http:HttpJwtAuthnHandler handler = {};
    http:Request request = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string authHeaderValue = "Basic xxxxxx";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.canHandle(request);
}

function testCanHandleHttpJwtAuth () returns (boolean) {
    http:HttpJwtAuthnHandler handler = {};
    http:Request request = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string authHeaderValue = "Bearer xxx.yyy.zzz";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.canHandle(request);
}

function testHandleHttpJwtAuthFailure () returns (boolean) {
    http:HttpJwtAuthnHandler handler = {};
    http:Request request = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string authHeaderValue = "Bearer xxx.yyy.zzz";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.handle(request);
}

function testHandleHttpJwtAuth (string token) returns (boolean) {
    http:HttpJwtAuthnHandler handler = {};
    http:Request request = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string authHeaderValue = "Bearer " + token;
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.handle(request);
}