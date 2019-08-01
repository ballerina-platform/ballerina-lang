import ballerina/http;

function testEncode (string url) returns (string|error) {
    return http:encode(url, "UTF-8");
}

function testInvalidEncode (string url) returns (string|error) {
    return http:encode(url, "abc");
}

function testDecode (string url) returns (string|error) {
    return http:decode(url, "UTF-8");
}

function testInvalidDecode (string url) returns (string|error) {
    return http:decode(url, "abc");
}