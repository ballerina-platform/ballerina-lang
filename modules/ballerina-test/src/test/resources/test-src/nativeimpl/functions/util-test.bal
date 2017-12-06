import ballerina.util;
import ballerina.security.crypto;

function testEncodeDecode (string s) (string) {
    return util:base64Decode(util:base64Encode(s));
}

function testRandomString () (string) {
    return util:uuid();
}

function testBase64Encoding (string s) (string) {
    return util:base64Encode(s);
}

function testBase64Decoding (string s) (string) {
    return util:base64Decode(s);
}

function testBase16ToBase64Encoding (string s) (string) {
    return util:base16ToBase64Encode(s);
}

function testBase64ToBase16Encoding (string s) (string) {
    return util:base64ToBase16Encode(s);
}

function testHMACValueFromBase16ToBase64Encoding (string base, string key) (string) {
    return util:base16ToBase64Encode(crypto:getHmac(base, key, crypto:Algorithm.MD5));
}

function testHMACValueFromBase64ToBase16Encoding (string base, string key) (string) {
    return util:base64ToBase16Encode(util:base16ToBase64Encode(crypto:getHmac(base, key, crypto:Algorithm.MD5)));
}
