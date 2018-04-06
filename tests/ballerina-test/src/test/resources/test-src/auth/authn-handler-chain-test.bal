import ballerina/http;
import ballerina/mime;

function testCreateAuthnHandlerChain () returns (http:AuthnHandlerChain) {
    http:AuthnHandlerChain authnHandlerChain = http:createAuthnHandlerChain();
    return authnHandlerChain;
}

function testAuthFailure () returns (boolean) {
    http:AuthnHandlerChain authnHandlerChain = http:createAuthnHandlerChain();
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "123Basic xxxxx";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("123Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authnHandlerChain.handle(inRequest);
}

function testAuthSuccess () returns (boolean) {
    http:AuthnHandlerChain authnHandlerChain = http:createAuthnHandlerChain();
    http:Request inRequest = createRequest();
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = new;
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authnHandlerChain.handle(inRequest);
}

function createRequest () returns (http:Request) {
    http:Request inRequest = new;
    inRequest.rawPath = "/helloWorld/sayHello";
    inRequest.method = "GET";
    inRequest.httpVersion = "1.1";
    return inRequest;
}
