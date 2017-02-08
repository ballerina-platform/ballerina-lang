import ballerina.lang.string;

function contains(string source, string substring) (boolean) {
    return string:contains(source, substring);
}

function equalsIgnoreCase(string s1, string s2) (boolean) {
    return string:equalsIgnoreCase(s1, s2);
}

function hasPrefix(string s, string prefix) (boolean) {
    return string:hasPrefix(s, prefix);
}

function hasSuffix(string s, string suffix) (boolean) {
    return string:hasSuffix(s, suffix);
}

function indexOf(string s, string str) (int) {
    return string:indexOf(s, str);
}

function lastIndexOf(string s, string str) (int) {
    return string:lastIndexOf(s, str);
}

function replace(string s, string source, string target) (string) {
    return string:replace(s, source, target);
}

function replaceAll(string s, string source, string target) (string) {
    return string:replaceAll(s, source, target);
}

function replaceFirst(string s, string source, string target) (string) {
    return string:replaceFirst(s, source, target);
}

function subString(string s, int from, int to) (string) {
    return string:subString(s, from, to);
}

function toLowerCase(string s) (string) {
    return string:toLowerCase(s);
}

function toUpperCase(string s) (string) {
    return string:toUpperCase(s);
}

function trim(string s) (string) {
    return string:trim(s);
}

function intValueOf(int i) (string) {
    return string:valueOf(i);
}

function longValueOf(long l) (string) {
    return string:valueOf(l);
}

function floatValueOf(float f) (string) {
    return string:valueOf(f);
}

function doubleValueOf(double d) (string) {
    return string:valueOf(d);
}

function booleanValueOf(boolean b) (string) {
    return string:valueOf(b);
}

function stringValueOf(string s) (string) {
    return string:valueOf(s);
}

function xmlValueOf(xml x) (string) {
    return string:valueOf(x);
}

function jsonValueOf(json j) (string) {
    return string:valueOf(j);
}

function length(string j) (int) {
    return string:length(j);
}

function unescape(string j) (string) {
    return string:unescape(j);
}