import ballerina/lang.'boolean as bl;
import ballerina/lang.'string as newString;

function testFunction() returns boolean|error {
    string lowerAscii = newString:toLowerAscii("FALSE");
    return bl:fromString(lowerAscii);
}
