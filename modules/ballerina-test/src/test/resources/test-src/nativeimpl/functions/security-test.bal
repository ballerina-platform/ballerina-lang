import ballerina.security;

function testHmac (string base, string key, string algo) (string) {
    return security:getHmac(base, key, algo);
}

function testMessageDigest (string base, string algo) (string) {
    return security:getHash(base, algo);
}
