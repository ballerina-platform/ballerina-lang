import ballerina/auth;
import ballerina/http;

function testCanHandleHttpJwtAuthWithoutHeader() returns (boolean) {
    http:HttpJwtAuthnHandler handler = new(createJwtAuthProvider("ballerina/security/ballerinaTruststore.p12"));
    http:Request request = createRequest();
    string authHeaderValue = "Basic xxxxxx";
    request.setHeader("Authorization", authHeaderValue);
    return handler.canHandle(request);
}

function testCanHandleHttpJwtAuth() returns (boolean) {
    http:HttpJwtAuthnHandler handler = new(createJwtAuthProvider("ballerina/security/ballerinaTruststore.p12"));
    http:Request request = createRequest();
    string authHeaderValue = "Bearer xxx.yyy.zzz";
    request.setHeader("Authorization", authHeaderValue);
    return handler.canHandle(request);
}

function testHandleHttpJwtAuthFailure() returns (boolean) {
    http:HttpJwtAuthnHandler handler = new(createJwtAuthProvider("ballerina/security/ballerinaTruststore.p12"));
    http:Request request = createRequest();
    string authHeaderValue = "Bearer xxx.yyy.zzz";
    request.setHeader("Authorization", authHeaderValue);
    return handler.handle(request);
}

function testHandleHttpJwtAuth(string token, string trustStorePath) returns (boolean) {
    http:HttpJwtAuthnHandler handler = new(createJwtAuthProvider(trustStorePath));
    http:Request request = createRequest();
    string authHeaderValue = "Bearer " + token;
    request.setHeader("Authorization", authHeaderValue);
    return handler.handle(request);
}

function createRequest() returns (http:Request) {
    http:Request inRequest = new;
    inRequest.rawPath = "/helloWorld/sayHello";
    inRequest.method = "GET";
    inRequest.httpVersion = "1.1";
    return inRequest;
}

function createJwtAuthProvider(string trustStorePath) returns auth:JWTAuthProvider {
    auth:JWTAuthProviderConfig jwtConfig = {};
    jwtConfig.issuer = "wso2";
    jwtConfig.audience = "ballerina";
    jwtConfig.certificateAlias = "ballerina";
    jwtConfig.trustStoreFilePath = trustStorePath;
    jwtConfig.trustStorePassword = "ballerina";
    auth:JWTAuthProvider jwtAuthProvider = new(jwtConfig);
    return jwtAuthProvider;
}
