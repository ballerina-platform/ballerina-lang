import ballerina/internal;

function testParseJson (string s) returns (json|error) {
    match internal:parseJson(s) {
        json result => {
            return result;
        }
        error err => return err;
    }
}
