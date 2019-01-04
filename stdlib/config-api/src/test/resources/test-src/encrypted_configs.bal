import ballerina/config;

function getDecryptedValue(string key) returns (string|()) {
    return config:getAsString(key);
}
