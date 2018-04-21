import ballerina/config;

function testGetAsString(string key) returns string {
    return config:getAsString(key);
}

function testSetConfig(string key, string value) {
    config:setConfig(key, value);
}

function testContains(string key) returns boolean {
    return config:contains(key);
}

function testGetAsMap(string key) returns map {
    return config:getAsMap(key);
}

function testGetAsInt(string key) returns int {
    return config:getAsInt(key);
}

function testGetAsFloat(string key) returns float {
    return config:getAsFloat(key);
}

function testGetAsBoolean(string key) returns boolean {
    return config:getAsBoolean(key);
}
