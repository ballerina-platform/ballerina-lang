import ballerina/config;

function getDecryptedValue(string key) returns (string|null) {
    return config:getAsString(key);
}
