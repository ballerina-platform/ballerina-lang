import ballerina/http;
import ballerina/mime;
import ballerina/auth.jwtAuth;

function testCanHandleHttpJwtAuthWithoutHeader () returns (boolean) {
    jwtAuth:JWTAuthenticator jwtAuthenticator = jwtAuth:createAuthenticator();
    http:HttpJwtAuthnHandler handler = new(jwtAuthenticator);
    http:Request request = createRequest ();
    string authHeaderValue = "Basic xxxxxx";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.canHandle(request);
}

function testCanHandleHttpJwtAuth () returns (boolean) {
    jwtAuth:JWTAuthenticator jwtAuthenticator = jwtAuth:createAuthenticator();
    http:HttpJwtAuthnHandler handler = new(jwtAuthenticator);
    http:Request request = createRequest ();
    string authHeaderValue = "Bearer xxx.yyy.zzz";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.canHandle(request);
}

function testHandleHttpJwtAuthFailure () returns (boolean) {
    jwtAuth:JWTAuthenticator jwtAuthenticator = jwtAuth:createAuthenticator();
    http:HttpJwtAuthnHandler handler = new(jwtAuthenticator);
    http:Request request = createRequest ();
    string authHeaderValue = "Bearer xxx.yyy.zzz";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", authHeaderValue);
    request.setEntity(requestEntity);
    return handler.handle(request);
}

function testHandleHttpJwtAuth (string token) returns (boolean) {
    jwtAuth:JWTAuthenticator jwtAuthenticator = jwtAuth:createAuthenticator();
    http:HttpJwtAuthnHandler handler = new(jwtAuthenticator);
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
