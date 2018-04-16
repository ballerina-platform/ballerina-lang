import ballerina/http;
import ballerina/mime;
import ballerina/auth;

function testCreateAuthnHandlerChain () returns (http:AuthnHandlerChain) {
    http:AuthHandlerRegistry registry = new;
    http:AuthnHandlerChain authnHandlerChain = new(registry);
    return authnHandlerChain;
}

function testAuthFailure () returns (boolean) {
    http:AuthHandlerRegistry registry = new;
    registry.add("basicProvider1", createBasicAuthnHandler());
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "123Basic xxxxx";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("123Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    http:AuthnHandlerChain authnHandlerChain = new(registry);
    return authnHandlerChain.handle(inRequest);
}

function testAuthFailureWithSpecificHandlers () returns (boolean) {
    http:AuthHandlerRegistry registry = new;
    registry.add("basicProvider1", createBasicAuthnHandler());
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "123Basic xxxxx";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("123Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    http:AuthnHandlerChain authnHandlerChain = new(registry);
    string[] authProviders = [];
    authProviders[0] = "basicProvider1";
    return authnHandlerChain.handleWithSpecificAuthnHandlers(authProviders, inRequest);
}

function testAuthSuccess () returns (boolean) {
    http:AuthHandlerRegistry registry = new;
    registry.add("basicProvider1", createBasicAuthnHandler());
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    http:AuthnHandlerChain authnHandlerChain = new(registry);
    return authnHandlerChain.handle(inRequest);
}

function testAuthSuccessWithSpecificHandlers () returns (boolean) {
    http:AuthHandlerRegistry registry = new;
    registry.add("basicProvider1", createBasicAuthnHandler());
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    http:AuthnHandlerChain authnHandlerChain = new(registry);
    string[] authProviders = [];
    authProviders[0] = "basicProvider1";
    return authnHandlerChain.handleWithSpecificAuthnHandlers(authProviders, inRequest);
}

function createRequest () returns (http:Request) {
    http:Request inRequest = new;
    inRequest.rawPath = "/helloWorld/sayHello";
    inRequest.method = "GET";
    inRequest.httpVersion = "1.1";
    return inRequest;
}

function createBasicAuthnHandler () returns (http:HttpAuthnHandler) {
    auth:ConfigAuthProvider configAuthProvider = new;
    auth:AuthProvider authProvider = <auth:AuthProvider> configAuthProvider;
    http:HttpBasicAuthnHandler basicAuthnHandler = new(authProvider);
    return check <http:HttpAuthnHandler> basicAuthnHandler;
}
