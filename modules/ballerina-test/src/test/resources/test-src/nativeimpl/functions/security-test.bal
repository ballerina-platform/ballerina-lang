import ballerina.security;
import ballerina.util;

function testHmac (string base, string key, string algo) (string) {
    return security:getHmac(base, key, algo);
}

function testHmacFromBase64 (string base, string key, string algo) (string) {
    return security:getHmacFromBase64(base, util:base64encode(key), algo);
}

function testMessageDigest (string base, string algo) (string) {
    return security:getHash(base, algo);
}
