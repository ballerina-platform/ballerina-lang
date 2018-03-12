import ballerina.auth.basic;
import ballerina.net.http;
import ballerina.mime;

function testCanHandleHttpBasicAuthWithoutHeader () (boolean) {
    basic:HttpBasicAuthnHandler handler = {};
    http:InRequest inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["123Basic xxxxxx"];
    mime:Entity requestEntity = {headers:{"123Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return handler.canHandle(inRequest);
}

function testCanHandleHttpBasicAuth () (boolean) {
    basic:HttpBasicAuthnHandler handler = {};
    http:InRequest inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["Basic xxxxxx"];
    mime:Entity requestEntity = {headers:{"Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return handler.canHandle(inRequest);
}

function testHandleHttpBasicAuthFailure () (boolean) {
    basic:HttpBasicAuthnHandler handler = {};
    http:InRequest inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["Basic YW1pbGE6cHFy"];
    mime:Entity requestEntity = {headers:{"Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest);
}

function testHandleHttpBasicAuth () (boolean) {
    basic:HttpBasicAuthnHandler handler = {};
    http:InRequest inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["Basic aXN1cnU6eHh4"];
    mime:Entity requestEntity = {headers:{"Authorization": basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest);
}
