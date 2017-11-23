import ballerina.util;

function testEncodeDecode (string s) (string) {
    string encoded;
    encoded = util:base64Encode(s);
    return util:base64Decode(encoded);
}

function testRandomString () (string) {
    return util:uuid();
}

function testHmac (string base, string key, string algo) (string) {
    return util:getHmac(base, key, algo);
}

function testHmacFromBase64 (string base, string key, string algo) (string) {
    return util:getHmacFromBase64(base, util:base64Encode(key), algo);
}

function testMessageDigest (string base, string algo) (string) {
    return util:getHash(base, algo);
}

function testBase64ToBase16Encode (string base) (string) {
    string base64Str = util:base64Encode(base);
    return util:base64ToBase16Encode(base64Str);
}

function testBase16Encoding (string s) (string) {
    return util:base16Encode(s);
}

function testBase16Decoding (string s) (string) {
    return util:base16Decode(s);
}

function testBase64Encoding (string s) (string) {
    return util:base64Encode(s);
}

function testBase64Decoding (string s) (string) {
    return util:base64Decode(s);
}

function testBase16ToBase64Encoding (string s) (string) {
    return util:base64Encode(util:base16Decode(s));
}

function testBase64ToBase16Encoding (string s) (string) {
    return util:base16Encode(util:base64Decode(s));
}
