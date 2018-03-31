import ballerina/net.uri;

function testEncode (string url) returns (string|error) {
    return uri:encode(url, "UTF-8");
}

function testInvalidEncode (string url) returns (string|error) {
    return uri:encode(url, "abc");
}

function testDecode (string url) returns (string|error) {
    return uri:decode(url, "UTF-8");
}

function testInvalidDecode (string url) returns (string|error) {
    return uri:decode(url, "abc");
}