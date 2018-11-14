import ballerina/internal;

function testParseJson (string s) returns (json|error) {
    return internal:parseJson(s);
}
