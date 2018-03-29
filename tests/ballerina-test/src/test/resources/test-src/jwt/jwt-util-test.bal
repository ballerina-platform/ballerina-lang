import ballerina/jwt.util;

function testParseJson (string s) returns (json|error) {
    match util:parseJwtComponent(s) {
        json result => {
            return result;
        }
        error err => return err;
    }
}
