import ballerina.auth.utils;
import ballerina.caching;
import ballerina.net.http;
import ballerina.mime;

function testCreateDisabledBasicAuthCache () (caching:Cache) {
    return utils:createCache("basic_auth_cache");
}

function testCreateAuthzCache () (caching:Cache) {
    return utils:createCache("authz_cache");
}

function testExtractBasicAuthCredentialsFromInvalidHeader () (string, string, error) {
    return utils:extractBasicAuthCredentials("Basic FSADFfgfsagas423gfdGSdfa");
}

function testExtractBasicAuthCredentials () (string, string, error) {
    return utils:extractBasicAuthCredentials("Basic aXN1cnU6eHh4");
}

function testExtractInvalidBasicAuthHeaderValue () (string, error) {
    // create dummy request
    http:InRequest inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = [".Basic FSADFfgfsagas423gfdGSdfa"];
    mime:Entity requestEntity = {headers:{"Authorization":basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return utils:extractBasicAuthHeaderValue(inRequest);
}

function testExtractBasicAuthHeaderValue () (string, error) {
    // create dummy request
    http:InRequest inRequest = {rawPath:"/helloWorld/sayHello", method:"GET", httpVersion:"1.1",
                                   userAgent:"curl/7.35.0", extraPathInfo:"null"};
    string[] basicAutheaderValue = ["Basic aXN1cnU6eHh4"];
    mime:Entity requestEntity = {headers:{"Authorization":basicAutheaderValue}};
    inRequest.setEntity(requestEntity);
    return utils:extractBasicAuthHeaderValue(inRequest);
}
