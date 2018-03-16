import ballerina.auth;
import ballerina.net.http;
import ballerina.mime;

function testCreateAuthnHandlerChain () (auth:AuthnHandlerChain) {
    auth:AuthnHandlerChain authnHandlerChain = auth:createAuthnHandlerChain();
    return authnHandlerChain;
}

function testAuthFailure () (boolean) {
    auth:AuthnHandlerChain authnHandlerChain = auth:createAuthnHandlerChain();
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["123Basic xxxxx"];
    mime:Entity requestEntity = {headers:{"123Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return authnHandlerChain.handle(inRequest);
}

function testAuthSuccess () (boolean) {
    auth:AuthnHandlerChain authnHandlerChain = auth:createAuthnHandlerChain();
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["Basic aXN1cnU6eHh4"];
    mime:Entity requestEntity = {headers:{"Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return authnHandlerChain.handle(inRequest);
}
