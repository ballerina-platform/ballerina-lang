import ballerina.net.http;

function testParseHeader (string value) (string, map, error) {
    return http:parseHeader(value);
}
