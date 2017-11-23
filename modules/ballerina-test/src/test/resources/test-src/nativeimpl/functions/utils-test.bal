import ballerina.util;

function testEncodeDecode (string s) (string) {
    return util:base64Decode(util:base64Encode(s));
}

function testRandomString () (string) {
    return util:uuid();
}

function testHmac (string base, string key, string algo) (string) {
    return util:getHmac(base, key, algo);
}

function testMessageDigest (string base, string algo) (string) {
    return util:getHash(base, algo);
}

function testBase64Encoding (string s) (string) {
    return util:base64Encode(s);
}

function testBase64Decoding (string s) (string) {
    return util:base64Decode(s);
}

function testConvertingBase16ToBase64 (string s) (string) {
    return util:convertBase16ToBase64(s);
}

function testConvertingBase64ToBase16 (string s) (string) {
    return util:convertBase64ToBase16(s);
}

function testConvertingHMACValueFromBase16ToBase64 (string base, string key, string algo) (string) {
    return util:convertBase16ToBase64(util:getHmac(base, key, algo));
}

function testConvertingHMACValueFromBase64ToBase16 (string base, string key, string algo) (string) {
    return util:convertBase64ToBase16(util:convertBase16ToBase64(util:getHmac(base, key, algo)));
}
