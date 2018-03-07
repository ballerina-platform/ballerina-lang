import ballerina.config;

function testGetAsString(string key) (string) {
    return config:getAsString(key);
}

function testSetConfig(string key, string value) {
    config:setConfig(key, value);
}
