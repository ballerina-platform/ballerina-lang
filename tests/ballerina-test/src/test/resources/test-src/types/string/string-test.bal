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