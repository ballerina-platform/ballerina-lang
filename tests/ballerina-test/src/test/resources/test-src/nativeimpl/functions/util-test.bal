import ballerina/util;
import ballerina/security.crypto;

function testEncodeDecode (string s) returns (string) {
    return util:base64Decode(util:base64Encode(s));
}

function testRandomString () returns (string) {
    return util:uuid();
}

function testBase64Encoding (string s) returns (string) {
    return util:base64Encode(s);
}

function testBase64Decoding (string s) returns (string) {
    return util:base64Decode(s);
}

function testBase16ToBase64Encoding (string s) returns (string) {
    return util:base16ToBase64Encode(s);
}

function testBase64ToBase16Encoding (string s) returns (string) {
    return util:base64ToBase16Encode(s);
}

function testHMACValueFromBase16ToBase64Encoding (string base, string key) returns (string) {
    return util:base16ToBase64Encode(crypto:getHmac(base, key, crypto:Algorithm.MD5));
}

function testHMACValueFromBase64ToBase16Encoding (string base, string key) returns (string) {
    return util:base64ToBase16Encode(util:base16ToBase64Encode(crypto:getHmac(base, key, crypto:Algorithm.MD5)));
}

function testParseJson (string s) returns (json|error) {
    match util:parseJson(s) {
        json result => {
            return result;
        }
        error err => return err;
    }
}
