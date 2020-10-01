// Copyright (c) 2019, 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;

# Built-in subtype of string containing strings of length 1.
@builtinSubtype
type Char string;

# Returns the length of the string.
#
# + str - the string
# + return - the number of characters (code points) in `str`
public isolated function length(string str) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.string.Length",
    name: "length"
} external;

# Returns an iterator over the string.
# The iterator will yield the substrings of length 1 in order.
#
# + str - the string to be iterated over
# + return - a new iterator object
public isolated function iterator(string str) returns object {
    public isolated function next() returns record {| string value; |}?;
}{
    StringIterator stringIterator = new(str);
    return stringIterator;
}

# Concatenates zero or more strings.
#
# + strs - strings to be concatenated
# + return - concatenation of all of the `strs`; empty string if `strs` is empty
public isolated function concat(string... strs) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.Concat",
    name: "concat"
} external;

# Returns the code point of a character in a string.
#
# + str - the string
# + index - an index in `str`
# + return - the Unicode code point of the character at `index` in `str`
public isolated function getCodePoint(string str, int index) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.string.GetCodePoint",
    name: "getCodePoint"
} external;

# Returns a substring of a string.
#
# + str - source string.
# + startIndex - the starting index, inclusive
# + endIndex - the ending index, exclusive
# + return - substring consisting of characters with index >= startIndex and < endIndex
public isolated function substring(string str, int startIndex, int endIndex = str.length()) returns string =
@java:Method {
    'class: "org.ballerinalang.langlib.string.Substring",
    name: "substring"
} external;

# Lexicographically compares strings using their Unicode code points.
# This orders strings in a consistent and well-defined way,
# but the ordering will often not be consistent with cultural expectations
# for sorted order.
#
# + str1 - the first string to be compared
# + str2 - the second string to be compared
# + return - an int that is less than, equal to or greater than zero,
#    according as `str1` is less than, equal to or greater than `str2`
public isolated function codePointCompare(string str1, string str2) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.string.CodePointCompare",
    name: "codePointCompare"
} external;

# Joins zero or more strings together with a separator.
#
# + separator - separator string
# + strs - strings to be joined
# + return - a string consisting of all of `strs` concatenated in order
#     with `separator` in between them
public isolated function 'join(string separator, string... strs) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.Join",
    name: "join"
} external;

# Finds the first occurrence of one string in another string.
#
# + str - the string in which to search
# + substr - the string to search for
# + startIndex - index to start searching from
# + return - index of the first occurrence of `substr` in `str` that is >= `startIndex`,
#    or `()` if there is no such occurrence
public isolated function indexOf(string str, string substr, int startIndex = 0) returns int? = @java:Method {
    'class: "org.ballerinalang.langlib.string.IndexOf",
    name: "indexOf"
} external;

# Finds the last occurrence of one string in another string.
#
# + str - the string in which to search
# + substr - the string to search for
# + startIndex - index to start searching backwards from
# + return - index of the last occurrence of `substr` in `str` that is <= `startIndex`,
#    or `()` if there is no such occurrence
public isolated function lastIndexOf(string str, string substr, int startIndex = str.length() - substr.length())
returns int? = @java:Method {
    'class: "org.ballerinalang.langlib.string.LastIndexOf",
    name: "lastIndexOf"
} external;

# Tests whether a string starts with another string.
#
# + str - the string to be tested
# + substr - the starting string
# + return - true if `str` starts with `substr`; false otherwise
public isolated function startsWith(string str, string substr) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.string.StartsWith",
    name: "startsWith"
} external;

# Tests whether a string ends with another string.
#
# + str - the string to be tested
# + substr - the ending string
# + return - true if `str` ends with `substr`; false otherwise
public isolated function endsWith(string str, string substr) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.string.EndsWith",
    name: "endsWith"
} external;

// Standard lib (not lang lib) should have a Unicode module (or set of modules)
// to deal with Unicode properly. These will need to be updated as each
// new Unicode version is released.

# Converts occurrences of A-Z to a-z.
# Other characters are left unchanged.
#
# + str - the string to be converted
# + return - `str` with any occurrences of A-Z converted to a-z
public isolated function toLowerAscii(string str) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.ToLowerAscii",
    name: "toLowerAscii"
} external;

# Converts occurrences of a-z to A-Z.
# Other characters are left unchanged.
#
# + str - the string to be converted
# + return - `str` with any occurrences of a-z converted to A-Z
public isolated function toUpperAscii(string str) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.ToUpperAscii",
    name: "toUpperAscii"
} external;

# Tests whether two strings are the same, ignoring the case of ASCII characters.
# A character in the range a-z is treated the same as the corresponding character in the range A-Z.
#
# + str1 - the first string to be compared
# + str2 - the second string to be compared
# + return - true if `str1` is the same as `str2`, treating upper-case and lower-case
# ASCII letters as the same; false, otherwise
public isolated function equalsIgnoreCaseAscii(string str1, string str2) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.string.EqualsIgnoreCaseAscii",
    name: "equalsIgnoreCaseAscii"
} external;

# Removes ASCII white space characters from the start and end of a string.
# The ASCII white space characters are 0x9...0xD, 0x20.
#
# + str - the string
# + return - `str` with leading or trailing ASCII white space characters removed
public isolated function trim(string str) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.Trim",
    name: "trim"
} external;

# Represents `str` as an array of bytes using UTF-8.
#
# + str - the string
# + return - UTF-8 byte array
public isolated function toBytes(string str) returns byte[] = @java:Method {
    'class: "org.ballerinalang.langlib.string.ToBytes",
    name: "toBytes"
} external;

# Constructs a string from its UTF-8 representation in `bytes`.
#
# + bytes - UTF-8 byte array
# + return - `bytes` converted to string or error
public isolated function fromBytes(byte[] bytes) returns string|error = @java:Method {
    'class: "org.ballerinalang.langlib.string.FromBytes",
    name: "fromBytes"
} external;

# Converts a string to an array of code points.
#
# + str - the string
# + return - an array with a code point for each character of `str`
public isolated function toCodePointInts(string str) returns int[] = @java:Method {
    'class: "org.ballerinalang.langlib.string.ToCodePointInts",
    name: "toCodePointInts"
} external;

# Converts a single character string to a code point.
#
# + ch - a single character string
# + return - the code point of `ch`
public isolated function toCodePointInt(Char ch) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.string.ToCodePointInt",
    name: "toCodePointInt"
} external;

# Constructs a string from an array of code points.
# An int is a valid code point if it is in the range 0 to 0x10FFFF inclusive,
# but not in the range 0xD800 or 0xDFFF inclusive.
#
# + codePoints - an array of ints, each specifying a code point
# + return - a string with a character for each code point in `codePoints`; or an error
# if any member of `codePoints` is not a valid code point
public isolated function fromCodePointInts(int[] codePoints) returns string|error = @java:Method {
    'class: "org.ballerinalang.langlib.string.FromCodePointInts",
    name: "fromCodePointInts"
} external;

# Constructs a single character string from a code point.
# An int is a valid code point if it is in the range 0 to 0x10FFFF inclusive,
# but not in the range 0xD800 or 0xDFFF inclusive.
#
# + codePoint - an int specifying a code point
# + return - a single character string whose code point is `codePoint`; or an error
# if `codePoint` is not a valid code point
public isolated function fromCodePointInt(int codePoint) returns Char|error = @java:Method {
    'class: "org.ballerinalang.langlib.string.FromCodePointInt",
    name: "fromCodePointInt"
} external;
