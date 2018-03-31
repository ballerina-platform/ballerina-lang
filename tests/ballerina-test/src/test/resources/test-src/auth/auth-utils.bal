import ballerina/auth.utils;
import ballerina/caching;
import ballerina/net.http;
import ballerina/mime;

function testCreateDisabledBasicAuthCache () returns (caching:Cache|null) {
    return utils:createCache("basic_auth_cache");
}

function testCreateAuthzCache () returns (caching:Cache|null) {
    return utils:createCache("authz_cache");
}

function testExtractBasicAuthCredentialsFromInvalidHeader () returns ((string, string) | error) {
    return utils:extractBasicAuthCredentials("Basic FSADFfgfsagas423gfdGSdfa");
}

function testExtractBasicAuthCredentials () returns ((string, string) | error) {
    return utils:extractBasicAuthCredentials("Basic aXN1cnU6eHh4");
}

