import ballerina/security.crypto;

function testHmacWithMD5 (string base, string key) returns (string) {
    return crypto:getHmac(base, key, crypto:Algorithm.MD5);
}

function testHmacWithSHA1 (string base, string key) returns (string) {
    return crypto:getHmac(base, key, crypto:Algorithm.SHA1);
}

function testHmacWithSHA256 (string base, string key) returns (string) {
    return crypto:getHmac(base, key, crypto:Algorithm.SHA256);
}

function testHashWithMD5 (string base) returns (string) {
    return crypto:getHash(base, crypto:Algorithm.MD5);
}

function testHashWithSHA1 (string base) returns (string) {
    return crypto:getHash(base, crypto:Algorithm.SHA1);
}

function testHashWithSHA256 (string base) returns (string) {
    return crypto:getHash(base, crypto:Algorithm.SHA256);
}
