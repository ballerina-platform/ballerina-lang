package ballerina.lang.string

native function contains(string s, string substr) (boolean);

native function equalsIgnoreCase(string s, string s1, string s2) (boolean);

native function hasPrefix(string s, string prefix) (boolean);

native function hasSuffix(string s, string sufix) (boolean);

native function indexOf(string s, string str) (int);

native function lastIndexOf(string s, string str) (int);

native function replace(string s, string old, string new) (string);

native function split(string s, string sep) (string[]);

native function toLower(string s) (string);

native function toUpper(string s) (string);

native function trim(string s) (string);

