function hasPrefix(string s, string prefix) returns (boolean) {
    return s.startsWith(prefix);
}

function hasSuffix(string s, string suffix) returns (boolean) {
    return s.endsWith(suffix);
}

function indexOf(string s, string str) returns (int?) {
    return s.indexOf(str);
}

function substring(string s, int beginIndex, int endIndex) returns (string) {
    return s.substring(beginIndex, endIndex);
}

function toLower(string s) returns (string) {
    return s.toLowerAscii();
}

function toUpper(string s) returns (string) {
    return s.toUpperAscii();
}

function trim(string s) returns (string) {
    return s.trim();
}

function intValueOf(int i) returns (string) {
    return i.toString();
}

function floatValueOf(float f) returns (string) {
    return f.toString();
}

function booleanValueOf(boolean b) returns (string) {
    return b.toString();
}

function stringValueOf(string s) returns (string) {
    return s.toString();
}

function xmlValueOf(xml x) returns (string) {
    return x.toString();
}

function jsonValueOf(json j) returns (string?) {
    return j.toJsonString();
}

function lengthOfStr(string j) returns (int) {
    return j.length();
}

function toByteArray(string l) returns (byte[]) {
    return l.toBytes();
}
