import ballerina/net.http;

function testParseHeader (string value) returns (string, map) | error {
    return http:parseHeader(value);
}
