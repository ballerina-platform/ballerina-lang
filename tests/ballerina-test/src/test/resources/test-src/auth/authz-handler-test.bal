import ballerina.auth.authz;
import ballerina.net.http;
import ballerina.mime;

function testHandleHttpAuthzFailure () (boolean) {
    authz:HttpAuthzHandler handler = {};
    http:InRequest inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["Basic aXNoYXJhOmFiYw=="];
    mime:Entity requestEntity = {headers:{"Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest, "scope2", "/sayHello");
}

function testHandleAuthz () (boolean) {
    authz:HttpAuthzHandler handler = {};
    http:InRequest inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["Basic aXN1cnU6eHh4"];
    mime:Entity requestEntity = {headers:{"Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest, "scope2", "/sayHello");
}
