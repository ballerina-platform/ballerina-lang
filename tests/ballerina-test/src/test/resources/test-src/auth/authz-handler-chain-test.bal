import ballerina/http;
import ballerina/mime;
import ballerina/runtime;

function testCreateAuthzHandlerChain () returns (http:AuthzHandlerChain) {
    http:AuthzHandlerChain authzHandlerChain = http:createAuthzHandlerChain();
    return authzHandlerChain;
}

function testAuthzFailure () returns (boolean) {
    http:AuthzHandlerChain authzHandlerChain = http:createAuthzHandlerChain();
    runtime:getInvocationContext().authenticationContext.username = "testuser";
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "123Basic xxxxx";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("123Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2"];
    return authzHandlerChain.handle(inRequest, scopes, "sayHello");
}

function testAuthzFailureNonMatchingScope () returns (boolean) {
    http:AuthzHandlerChain authzHandlerChain = http:createAuthzHandlerChain();
    runtime:getInvocationContext().authenticationContext.username = "ishara";
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXNoYXJhOmFiYw==";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2"];
    return authzHandlerChain.handle(inRequest, scopes, "sayHello");
}

function testAuthzSucess () returns (boolean) {
    http:AuthzHandlerChain authzHandlerChain = http:createAuthzHandlerChain();
    runtime:getInvocationContext().authenticationContext.username = "isuru";
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2"];
    return authzHandlerChain.handle(inRequest, scopes, "sayHello");
}

function testAuthzSucessWithMultipleScopes () returns (boolean) {
    http:AuthzHandlerChain authzHandlerChain = http:createAuthzHandlerChain();
    runtime:getInvocationContext().authenticationContext.username = "isuru";
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    string[] scopes = ["scope2", "scope1"];
    return authzHandlerChain.handle(inRequest, scopes, "sayHello");
}