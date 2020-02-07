import ballerina/http;

function testParseHeader (string value) returns [string, map<any>]|error {
    var result = http:parseHeader(value);
    if (result is http:ClientError) {
        error httpError = result;
        var cause = httpError.detail()?.cause;
        if (cause is error) {
            return cause;
        }
    }
    return result;
}
