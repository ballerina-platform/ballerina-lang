import ballerina/os;

function testValidEnv() returns (string) {
    return os:getEnv("JAVA_HOME");
}

function testEmptyEnv() returns (string) {
    return os:getEnv("JAVA_XXXX");
}

function testGetCurrentDirectory() returns (string) {
    return os:getCurrentDirectory();
}
