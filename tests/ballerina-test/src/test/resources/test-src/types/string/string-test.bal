import ballerina/crypto;
import ballerina/io;

function contains(string source, string substring) returns (boolean) {
    return source.contains(substring);
}

function equalsIgnoreCase(string s1, string s2) returns (boolean) {
    return s1.equalsIgnoreCase(s2);
}

function hasPrefix(string s, string prefix) returns (boolean) {
    return s.hasPrefix(prefix);
}

function hasSuffix(string s, string suffix) returns (boolean) {
    return s.hasSuffix(suffix);
}

function indexOf(string s, string str) returns (int) {
    return s.indexOf(str);
}

function lastIndexOf(string s, string str) returns (int) {
    return s.lastIndexOf(str);
}

function replace(string s, string source, string target) returns (string) {
    return s.replace(source, target);
}

function replaceAll(string s, string source, string target) returns (string) {
    return s.replaceAll(source, target);
}

function replaceFirst(string s, string source, string target) returns (string) {
    return s.replaceFirst(source, target);
}

function subString(string s, int beginIndex, int endIndex) returns (string) {
    return s.subString(beginIndex, endIndex);
}

function toLowerCase(string s) returns (string) {
    return s.toLowerCase();
}

function toUpperCase(string s) returns (string) {
    return s.toUpperCase();
}

function trim(string s) returns (string) {
    return s.trim();
}

function intValueOf(int i) returns (string) {
    return <string>(i);
}

function floatValueOf(float f) returns (string) {
    return <string>(f);
}

function booleanValueOf(boolean b) returns (string) {
    return <string>(b);
}

function stringValueOf(string s) returns (string) {
    return <string>(s);
}

function xmlValueOf(xml x) returns (string) {
    return io:sprintf("%s", x);
}

function jsonValueOf(json j) returns (string?) {
    return j.toString();
}

function length(string j) returns (int) {
    return j.length();
}

function unescape(string j) returns (string) {
    return j.unescape();
}

function split(string j, string k) returns (string[]) {
    return j.split(k);
}

function toBlob(string l, string m) returns (blob) {
    return l.toBlob(m);
}

function testEncodeDecode(string content) returns (string|error) {
    match content.base64Encode() {
        string returnString => return returnString.base64Decode();
        error e => return e;
    }
}

function testBase64EncodeString(string contentToBeEncoded) returns (string|error) {
    return contentToBeEncoded.base64Encode();
}

function testBase64DecodeString(string contentToBeDecoded) returns (string|error) {
    return contentToBeDecoded.base64Decode();
}

function testBase64EncodeBlob(blob contentToBeEncoded) returns blob {
    return contentToBeEncoded.base64Encode();
}

function testBase64DecodeBlob(blob contentToBeDecoded) returns blob {
    return contentToBeDecoded.base64Decode();
}


function testBase16ToBase64Encoding(string s) returns string {
    return s.base16ToBase64Encode();
}

function testBase64ToBase16Encoding(string s) returns string {
    return s.base64ToBase16Encode();
}

function testHMACValueFromBase16ToBase64Encoding(string base, string key) returns (string) {
    return crypto:getHmac(base, key, crypto:MD5).base16ToBase64Encode();
}

function testHMACValueFromBase64ToBase16Encoding(string base, string key) returns (string) {
    return crypto:getHmac(base, key, crypto:MD5).base16ToBase64Encode().base64ToBase16Encode();
}
