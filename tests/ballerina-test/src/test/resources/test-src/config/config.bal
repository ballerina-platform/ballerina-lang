import ballerina/config;

function testGetAsString(string key) returns (string|()) {
    return config:getAsString(key);
}

function testSetConfig(string key, string value) {
    config:setConfig(key, value);
}

function testContains(string key) returns (boolean) {
    return config:contains(key);
}

function testGetTable(string tableHeader) returns map {
    return config:getTable(tableHeader);
}