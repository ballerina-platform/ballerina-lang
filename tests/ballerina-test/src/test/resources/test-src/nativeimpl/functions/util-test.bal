import ballerina/util;

function testParseJson(string s) returns (json|error) {
    match util:parseJson(s) {
        json result => {
            return result;
        }
        error err => return err;
    }
}
