import ballerina/net.http.authadaptor;
import ballerina/mime;
import ballerina/net.http;

function testCreateAuthzHandlerChain () returns (authadaptor:AuthzHandlerChain) {
    authadaptor:AuthzHandlerChain authzHandlerChain = authadaptor:createAuthzHandlerChain();
    return authzHandlerChain;
}

function testAuthzFailure () returns (boolean) {
    authadaptor:AuthzHandlerChain authzHandlerChain = authadaptor:createAuthzHandlerChain();
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "123Basic xxxxx";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("123Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authzHandlerChain.handle(inRequest, "scope2", "sayHello");
}

function testAuthzFailureNonMatchingScope () returns (boolean) {
    authadaptor:AuthzHandlerChain authzHandlerChain = authadaptor:createAuthzHandlerChain();
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXNoYXJhOmFiYw==";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authzHandlerChain.handle(inRequest, "scope2", "sayHello");
}

function testAuthzSucess () returns (boolean) {
    authadaptor:AuthzHandlerChain authzHandlerChain = authadaptor:createAuthzHandlerChain();
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authzHandlerChain.handle(inRequest, "scope2", "sayHello");
}