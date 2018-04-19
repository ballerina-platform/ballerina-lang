import ballerina/system;

function testValidEnv() returns (string) {
    return system:getEnv("JAVA_HOME");
}

function testEmptyEnv() returns (string) {
    return system:getEnv("JAVA_XXXX");
}

function testGetCurrentDirectory() returns (string) {
    return system:getCurrentDirectory();
}
