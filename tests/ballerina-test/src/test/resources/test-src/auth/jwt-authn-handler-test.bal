import ballerina/http;
import ballerina/mime;

function testCanHandleHttpJwtAuthWithoutHeader () returns (boolean) {
    http:HttpJwtAuthnHandler handler = new;
    http:Request request = createRequest ();
    string authHeaderValue = "Basic xxxxxx";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.canHandle(request);
}

function testCanHandleHttpJwtAuth () returns (boolean) {
    http:HttpJwtAuthnHandler handler = new;
    http:Request request = createRequest ();
    string authHeaderValue = "Bearer xxx.yyy.zzz";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.canHandle(request);
}

function testHandleHttpJwtAuthFailure () returns (boolean) {
    http:HttpJwtAuthnHandler handler = new;
    http:Request request = createRequest ();
    string authHeaderValue = "Bearer xxx.yyy.zzz";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.handle(request);
}

function testHandleHttpJwtAuth (string token) returns (boolean) {
    http:HttpJwtAuthnHandler handler = new;
    http:Request request = createRequest ();
    string authHeaderValue = "Bearer " + token;
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.handle(request);
}

function createRequest () returns (http:Request) {
    http:Request inRequest = new;
    inRequest.rawPath = "/helloWorld/sayHello";
    inRequest.method = "GET";
    inRequest.httpVersion = "1.1";
    return inRequest;
}
