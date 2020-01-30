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

function testGetAsMap(string key) returns map<any> {
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

function testGetAsArray(string key) returns any[] {
    return config:getAsArray(key);
}

function testGetAsArray2(string key) returns int[] {
    int[]|error ports = int[].constructFrom(config:getAsArray(key));
    if (ports is int[]) {
        return ports;
    } else {
        panic ports;
    }
}
