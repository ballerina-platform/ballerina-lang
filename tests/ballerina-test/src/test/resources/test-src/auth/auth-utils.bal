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

