import ballerina.os;

function testValidEnv () (string) {
    return os:getEnv("JAVA_HOME");
}

function testEmptyEnv () (string) {
    return os:getEnv("JAVA_XXXX");
}

function testValidMultivaluedEnv () (string[]) {
    return os:getMultivaluedEnv("PATH");
}

function testEmptyMultivaluedEnv () (string[]) {
    return os:getMultivaluedEnv("XXXX");
}

function testGetName () (string) {
    return os:getName();
}

function testGetVersion () (string) {
    return os:getVersion();
}

function testGetArchitecture () (string) {
    return os:getArchitecture();
}
