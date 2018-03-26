import ballerina/net.http.authadaptor;
import ballerina/net.http;
import ballerina/mime;

function testHandleHttpAuthzFailure () returns (boolean) {
    authadaptor:HttpAuthzHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXNoYXJhOmFiYw==";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest, "scope2", "/sayHello");
}

function testHandleAuthz () returns (boolean) {
    authadaptor:HttpAuthzHandler handler = {};
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return handler.handle(inRequest, "scope2", "/sayHello");
}

function testExtractInvalidBasicAuthHeaderValue () returns (string|error) {
    // create dummy request
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = ".Basic FSADFfgfsagas423gfdGSdfa";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authadaptor:extractBasicAuthHeaderValue(inRequest);
}

function testExtractBasicAuthHeaderValue () returns (string|error) {
    // create dummy request
    http:Request inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                 userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string basicAutheaderValue = "Basic aXN1cnU6eHh4";
    mime:Entity requestEntity = {};
    requestEntity.setHeader("Authorization", basicAutheaderValue);
    inRequest.setEntity(requestEntity);
    return authadaptor:extractBasicAuthHeaderValue(inRequest);
}
