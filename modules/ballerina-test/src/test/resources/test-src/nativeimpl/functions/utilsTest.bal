import ballerina.util;

function testEncodeDecode (string s) (string) {
    string encoded;
    encoded = util:base64encode(s);
    return util:base64decode(encoded);
}

function testRandomString () (string) {
    return util:uuid();
}

function testBase64ToBase16Encode (string base) (string) {
    string base64Str = util:base64encode(base);
    return util:base64ToBase16Encode(base64Str);
}
