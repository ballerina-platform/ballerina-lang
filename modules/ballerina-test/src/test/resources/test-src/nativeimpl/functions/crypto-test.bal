import ballerina.crypto;

function testHmac (string base, string key, string algo) (string) {
    return crypto:getHmac(base, key, algo);
}

function testMessageDigest (string base, string algo) (string) {
    return crypto:getHash(base, algo);
}
