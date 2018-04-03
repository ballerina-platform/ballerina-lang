import ballerina/http;
import ballerina/mime;

function testCreateAuthnHandlerChain () returns (http:AuthnHandlerChain) {
    http:AuthnHandlerChain authnHandlerChain = http:createAuthnHandlerChain();
    return authnHandlerChain;
}

function testAuthFailure () returns (boolean) {
    http:AuthnHandlerChain authnHandlerChain = http:createAuthnHandlerChain();
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "123Basic xxxxx";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("123Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authnHandlerChain.handle(inRequest);
}

function testAuthSuccess () returns (boolean) {
    http:AuthnHandlerChain authnHandlerChain = http:createAuthnHandlerChain();
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authnHandlerChain.handle(inRequest);
}
