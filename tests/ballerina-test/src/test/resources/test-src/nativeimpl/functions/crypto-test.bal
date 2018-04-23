import ballerina/crypto;

function testHmacWithMD5 (string base, string key) returns (string) {
    return crypto:hmac(base, key, crypto:MD5);
}

function testHmacWithSHA1 (string base, string key) returns (string) {
    return crypto:hmac(base, key, crypto:SHA1);
}

function testHmacWithSHA256 (string base, string key) returns (string) {
    return crypto:hmac(base, key, crypto:SHA256);
}

function testHashWithMD5 (string base) returns (string) {
    return crypto:hash(base, crypto:MD5);
}

function testHashWithSHA1 (string base) returns (string) {
    return crypto:hash(base, crypto:SHA1);
}

function testHashWithSHA256 (string base) returns (string) {
    return crypto:hash(base, crypto:SHA256);
}

function testHashWithCRC32ForText (string text) returns (string) {
    return crypto:crc32(text);
}

function testHashWithCRC32ForBinary (blob payload) returns (string) {
    return crypto:crc32(payload);
}

function testHashWithCRC32ForJSON (json payload) returns (string) {
    return crypto:crc32(payload);
}

function testHashWithCRC32ForXML (xml payload) returns (string) {
    return crypto:crc32(payload);
}
