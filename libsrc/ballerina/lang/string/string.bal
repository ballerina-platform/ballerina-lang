package ballerina.lang.string

native function contains(string s, string substr) (boolean);

native function equalsIgnoreCase(string s1, string s2) (boolean);

native function hasPrefix(string s, string prefix) (boolean);

native function hasSuffix(string s, string sufix) (boolean);

native function indexOf(string s, string str) (int);

native function lastIndexOf(string s, string str) (int);

native function replace(string s, string old, string new) (string);

native function replaceAll(string s, string old, string new) (string);

native function replaceFirst(string s, string old, string new) (string);

native function split(string s, string sep) (string[]);

native function toLowerCase(string s) (string);

native function toUpperCase(string s) (string);

native function trim(string s) (string);

native function valueOf(int i) (string);

native function valueOf(long l) (string);

native function valueOf(float f) (string);

native function valueOf(double d) (string);

native function valueOf(boolean b) (string);

native function valueOf(string s) (string);

native function valueOf(xml e) (string);

native function valueOf(json j) (string);

native function length(string s) (int);

native function unescape(string s) (string);