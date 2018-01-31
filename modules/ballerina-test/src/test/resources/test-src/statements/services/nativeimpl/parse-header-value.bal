import ballerina.net.http;

function testParseHeaderValue (string value) (string, map) {
    return http:parseHeaderValue(value);
}
