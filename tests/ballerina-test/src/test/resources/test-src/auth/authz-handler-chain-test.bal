import ballerina.auth.authz;
import ballerina.mime;
import ballerina.net.http;

function testCreateAuthzHandlerChain () (authz:AuthzHandlerChain) {
    authz:AuthzHandlerChain authzHandlerChain = authz:createAuthzHandlerChain();
    return authzHandlerChain;
}

function testAuthzFailure () (boolean) {
    authz:AuthzHandlerChain authzHandlerChain = authz:createAuthzHandlerChain();
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["123Basic xxxxx"];
    mime:Entity requestEntity = {headers:{"123Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return authzHandlerChain.handle(inRequest, "scope2", "sayHello");
}

function testAuthzFailureNonMatchingScope () (boolean) {
    authz:AuthzHandlerChain authzHandlerChain = authz:createAuthzHandlerChain();
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["Basic aXNoYXJhOmFiYw=="];
    mime:Entity requestEntity = {headers:{"Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return authzHandlerChain.handle(inRequest, "scope2", "sayHello");
}

function testAuthzSucess () (boolean) {
    authz:AuthzHandlerChain authzHandlerChain = authz:createAuthzHandlerChain();
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["Basic aXN1cnU6eHh4"];
    mime:Entity requestEntity = {headers:{"Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return authzHandlerChain.handle(inRequest, "scope2", "sayHello");
}
