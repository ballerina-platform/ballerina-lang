import ballerina.lang.strings;

function contains(string source, string substring) (boolean) {
    return strings:contains(source, substring);
}

function equalsIgnoreCase(string s1, string s2) (boolean) {
    return strings:equalsIgnoreCase(s1, s2);
}

function hasPrefix(string s, string prefix) (boolean) {
    return strings:hasPrefix(s, prefix);
}

function hasSuffix(string s, string suffix) (boolean) {
    return strings:hasSuffix(s, suffix);
}

function indexOf(string s, string str) (int) {
    return strings:indexOf(s, str);
}

function lastIndexOf(string s, string str) (int) {
    return strings:lastIndexOf(s, str);
}

function replace(string s, string source, string target) (string) {
    return strings:replace(s, source, target);
}

function replaceAll(string s, string source, string target) (string) {
    return strings:replaceAll(s, source, target);
}

function replaceFirst(string s, string source, string target) (string) {
    return strings:replaceFirst(s, source, target);
}

function subString(string s, int from, int to) (string) {
    return strings:subString(s, from, to);
}

function toLowerCase(string s) (string) {
    return strings:toLowerCase(s);
}

function toUpperCase(string s) (string) {
    return strings:toUpperCase(s);
}

function trim(string s) (string) {
    return strings:trim(s);
}

function intValueOf(int i) (string) {
    return strings:valueOf(i);
}

function longValueOf(long l) (string) {
    return strings:valueOf(l);
}

function floatValueOf(float f) (string) {
    return strings:valueOf(f);
}

function doubleValueOf(double d) (string) {
    return strings:valueOf(d);
}

function booleanValueOf(boolean b) (string) {
    return strings:valueOf(b);
}

function stringValueOf(string s) (string) {
    return strings:valueOf(s);
}

function xmlValueOf(xml x) (string) {
    return strings:valueOf(x);
}

function jsonValueOf(json j) (string) {
    return strings:valueOf(j);
}

function length(string j) (int) {
    return strings:length(j);
}

function unescape(string j) (string) {
    return strings:unescape(j);
}