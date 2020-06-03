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

function testGetAsMap(string key) returns map<anydata> {
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

function testGetAsArray(string key) returns anydata[] {
    return config:getAsArray(key);
}

function testGetAsArray2(string key) returns int[] {
    var keyArray = config:getAsArray(key);
    int[]|error ports = keyArray.cloneWithType(int[]);
    if (ports is int[]) {
        return ports;
    } else {
        panic ports;
    }
}

function testGetAsArray3(string key) returns map<anydata>[] {
    var keyArray = config:getAsArray(key);
    map<anydata>[]|error result = keyArray.cloneWithType(map<anydata>[]);
    if (result is error) {
        panic result;
    } else {
        return result;
    }
}
