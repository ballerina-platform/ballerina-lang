import ballerina/lang.'boolean as bl;

function testFunction() returns boolean|error {
    return bl:fromString("false");
}
