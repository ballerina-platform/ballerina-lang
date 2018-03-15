import ballerina.config;

function getDecryptedValue(string key) (string) {
    return config:getAsString(key);
}
