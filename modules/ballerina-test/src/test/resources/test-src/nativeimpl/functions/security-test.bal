import ballerina.security;

function testEncodeDecode (string s) (string) {
    string encoded;
    encoded = security:base64encode(s);
    return security:base64decode(encoded);
}

function testRandomString () (string) {
    return security:uuid();
}

function testHmac (string base, string key, string algo) (string) {
    return security:getHmac(base, key, algo);
}

function testHmacFromBase64 (string base, string key, string algo) (string) {
    return security:getHmacFromBase64(base, security:base64encode(key), algo);
}

function testMessageDigest (string base, string algo) (string) {
    return security:getHash(base, algo);
}

function testBase64ToBase16Encode (string base) (string) {
    string base64Str = security:base64encode(base);
    return security:base64ToBase16Encode(base64Str);
}
