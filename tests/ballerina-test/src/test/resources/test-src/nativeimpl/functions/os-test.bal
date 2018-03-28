import ballerina/os;

function testValidEnv () returns (string) {
    return os:getEnv("JAVA_HOME");
}

function testEmptyEnv () returns (string) {
    return os:getEnv("JAVA_XXXX");
}

function testValidMultivaluedEnv () returns (string[]) {
    return os:getMultivaluedEnv("PATH");
}

function testEmptyMultivaluedEnv () returns (string[]) {
    return os:getMultivaluedEnv("XXXX");
}

function testGetName () returns (string) {
    return os:getName();
}

function testGetVersion () returns (string) {
    return os:getVersion();
}

function testGetArchitecture () returns (string) {
    return os:getArchitecture();
}
