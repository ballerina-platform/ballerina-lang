import ballerina/http;

function testParseHeader (string value) returns (string, map) | error {
    return http:parseHeader(value);
}
