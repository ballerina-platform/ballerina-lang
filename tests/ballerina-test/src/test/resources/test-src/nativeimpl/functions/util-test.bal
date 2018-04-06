import ballerina/util;
import ballerina/security.crypto;
import ballerina/io;

function testEncodeDecode (string content) returns (string  | blob  | io:ByteChannel  | util:Base64EncodeError) {
    util:Base64EncodeError errorStruct = {};
    errorStruct.message = "Error";
    match util:base64Encode(content) {
        string returnString => return util:base64Decode(returnString);
        blob returnBlob => return errorStruct;
        io:ByteChannel returnChannel => return errorStruct;
        util:Base64EncodeError returnError => return returnError;
    }
}

function testRandomString () returns (string) {
    return util:uuid();
}

function testBase64EncodeString (string contentToBeEncoded) returns (string  | blob  | io:ByteChannel  | util:Base64EncodeError) {
    return util:base64Encode(contentToBeEncoded);
}

function testBase64DecodeString (string contentToBeDecoded) returns (string  | blob  | io:ByteChannel  | util:Base64DecodeError) {
    return util:base64Decode(contentToBeDecoded);
}

function testBase64EncodeBlob (blob contentToBeEncoded) returns (string  | blob  | io:ByteChannel  | util:Base64EncodeError) {
    return util:base64Encode(contentToBeEncoded);
}

function testBase64DecodeBlob (blob contentToBeDecoded) returns (string  | blob  | io:ByteChannel  | util:Base64DecodeError) {
    return util:base64Decode(contentToBeDecoded);
}

function testBase64EncodeByteChannel (io:ByteChannel contentToBeEncoded) returns (string  | blob  | io:ByteChannel  | util:Base64EncodeError) {
    return util:base64Encode(contentToBeEncoded);
}

function testBase64DecodeByteChannel (io:ByteChannel contentToBeDecoded) returns (string  | blob  | io:ByteChannel  | util:Base64DecodeError) {
    return util:base64Decode(contentToBeDecoded);
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
