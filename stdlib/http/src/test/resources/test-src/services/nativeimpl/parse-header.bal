import ballerina/http;

function testParseHeader (string value) returns [string, map<any>]|error {
    return  http:parseHeader(value);
}
