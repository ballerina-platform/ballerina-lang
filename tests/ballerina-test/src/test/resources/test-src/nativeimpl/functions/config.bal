import ballerina/config;

function testGetGlobalValues(string key) (string) {
    return config:getGlobalValue(key);
}

function testGetInstanceValues(string id, string key) (string) {
    return config:getInstanceValue(id, key);
}

function testConfigsWithWhitespace(string id, string key) (string) {
    return config:getInstanceValue(id, key);
}