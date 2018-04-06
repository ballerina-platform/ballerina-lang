import ballerina/auth.utils;
import ballerina/caching;
import ballerina/http;
import ballerina/mime;

function testCreateDisabledBasicAuthCache () returns (caching:Cache|()) {
    return utils:createCache("basic_auth_cache");
}

function testCreateAuthzCache () returns (caching:Cache|()) {
    return utils:createCache("authz_cache");
}

function testExtractBasicAuthCredentialsFromInvalidHeader () returns ((string, string) | error) {
    return utils:extractBasicAuthCredentials("Basic FSADFfgfsagas423gfdGSdfa");
}

function testExtractBasicAuthCredentials () returns ((string, string) | error) {
    return utils:extractBasicAuthCredentials("Basic aXN1cnU6eHh4");
}

