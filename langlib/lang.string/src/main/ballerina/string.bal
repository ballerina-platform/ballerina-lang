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

import ballerina/jballerina.java;
import ballerina/lang.regexp;

# Built-in subtype of string containing strings of length 1.
@builtinSubtype
type Char string;

# Returns the length of the string.
#
# ```ballerina
# "Hello, World!".length() ⇒ 13;
# ```
#
# + str - the string
# + return - the number of characters (code points) in parameter `str`
public isolated function length(string str) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.string.Length",
    name: "length"
} external;

# Returns an iterator over the string.
#
# The iterator will yield the substrings of length 1 in order.
#
# ```ballerina
# object {
#   public isolated function next() returns record {|string:Char value;|}?;
# } iterator = "Hello, World!".iterator();
# iterator.next() ⇒ {"value":"H"}
# ```
#
# + str - the string to be iterated over
# + return - a new iterator object
public isolated function iterator(string str) returns object {
    public isolated function next() returns record {| Char value; |}?;
}{
    StringIterator stringIterator = new(str);
    return stringIterator;
}

# Concatenates zero or more strings.
#
# ```ballerina
# "http://worldtimeapi.org".concat("/api/timezone/", "Asia", "/", "Colombo") ⇒ http://worldtimeapi.org/api/timezone/Asia/Colombo
#
# // Alternative approach to achieve the same.
# string:concat("http://worldtimeapi.org", "/api/timezone/", "Asia", "/", "Colombo") ⇒ http://worldtimeapi.org/api/timezone/Asia/Colombo
# ```
#
# + strs - strings to be concatenated
# + return - concatenation of all of the parameter `strs`; empty string if parameter `strs` is empty
public isolated function concat(string... strs) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.Concat",
    name: "concat"
} external;

# Returns the code point of a character in a string.
#
# ```ballerina
# "Hello, World!".getCodePoint(3) ⇒ 108
# ```
#
# + str - the string
# + index - an index in parameter `str`
# + return - the Unicode code point of the character at parameter `index` in parameter `str`
public isolated function getCodePoint(string str, int index) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.string.GetCodePoint",
    name: "getCodePoint"
} external;

# Returns a substring of a string.
#
# ```ballerina
# "Hello, my name is John".substring(7) ⇒ my name is John
#
# "Hello, my name is John Anderson".substring(18, 22) ⇒ John
# ```
#
# + str - source string.
# + startIndex - the starting index, inclusive
# + endIndex - the ending index, exclusive
# + return - substring consisting of characters with index >= `startIndex` and < `endIndex`
public isolated function substring(string str, int startIndex, int endIndex = str.length()) returns string =
@java:Method {
    'class: "org.ballerinalang.langlib.string.Substring",
    name: "substring"
} external;

# Lexicographically compares strings using their Unicode code points.
#
# This orders strings in a consistent and well-defined way,
# but the ordering will often not be consistent with cultural expectations
# for sorted order.
#
# ```ballerina
# "Austria".codePointCompare("Australia") ⇒ 1
# ```
#
# + str1 - the first string to be compared
# + str2 - the second string to be compared
# + return - an int that is less than, equal to or greater than zero,
#    according as parameter `str1` is less than, equal to or greater than parameter `str2`
public isolated function codePointCompare(string str1, string str2) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.string.CodePointCompare",
    name: "codePointCompare"
} external;

# Joins zero or more strings together with a separator.
#
# ```ballerina
# string:'join(" ", "Ballerina", "is", "a", "programming", "language") ⇒ Ballerina is a programming language
#
# string[] array = ["John", "23", "USA", "Computer Science", "Swimmer"];
# string:'join(",", ...array) ⇒ John,23,USA,Computer Science,Swimmer
# ```
#
# + separator - separator string
# + strs - strings to be joined
# + return - a string consisting of all of parameter `strs` concatenated in order
#     with parameter `separator` in between them
public isolated function 'join(string separator, string... strs) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.Join",
    name: "join"
} external;

# Finds the first occurrence of one string in another string.
#
# ```ballerina
# "New Zealand".indexOf("land") ⇒ 7
#
# "Ballerinalang is a unique programming language".indexOf("lang", 15) ⇒ 38
# ```
#
# + str - the string in which to search
# + substr - the string to search for
# + startIndex - index to start searching from
# + return - index of the first occurrence of parameter `substr` in parameter `str` that is >= parameter `startIndex`,
#    or `()` if there is no such occurrence
public isolated function indexOf(string str, string substr, int startIndex = 0) returns int? = @java:Method {
    'class: "org.ballerinalang.langlib.string.IndexOf",
    name: "indexOf"
} external;

# Finds the last occurrence of one string in another string.
#
# ```ballerina
# "Ballerinalang is a unique programming language".lastIndexOf("lang") ⇒ 38
#
# // Search backwards for the last occurrence of a string from a specific index.
# "Ballerinalang is a unique programming language".lastIndexOf("lang", 15) ⇒ 9
# ```
#
# + str - the string in which to search
# + substr - the string to search for
# + startIndex - index to start searching backwards from
# + return - index of the last occurrence of parameter `substr` in parameter `str` that is <= parameter `startIndex`,
#    or `()` if there is no such occurrence
public isolated function lastIndexOf(string str, string substr, int startIndex = str.length() - substr.length())
returns int? = @java:Method {
    'class: "org.ballerinalang.langlib.string.LastIndexOf",
    name: "lastIndexOf"
} external;

# Tests whether a string includes another string.
#
# ```ballerina
# "Hello World, from Ballerina".includes("Bal") ⇒ true
#
# "Hello World! from Ballerina".includes("Hello", 10) ⇒ false
# ```
#
# + str - the string in which to search
# + substr - the string to search for
# + startIndex - index to start searching from
# + return - `true` if there is an occurrence of parameter `substr` in parameter `str` at an index >= parameter `startIndex`,
#    or `false` otherwise
public isolated function includes(string str, string substr, int startIndex = 0) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.string.Includes",
    name: "includes"
} external;

# Tests whether a string starts with another string.
#
# ```ballerina
# "Welcome to the Ballerina programming language".startsWith("Welcome") ⇒ true
# ```
#
# + str - the string to be tested
# + substr - the starting string
# + return - true if parameter `str` starts with parameter `substr`; false otherwise
public isolated function startsWith(string str, string substr) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.string.StartsWith",
    name: "startsWith"
} external;

# Tests whether a string ends with another string.
#
# ```ballerina
# "Welcome to the Ballerina programming language".endsWith("language") ⇒ true
# ```
#
# + str - the string to be tested
# + substr - the ending string
# + return - true if parameter `str` ends with parameter `substr`; false otherwise
public isolated function endsWith(string str, string substr) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.string.EndsWith",
    name: "endsWith"
} external;

// Standard lib (not lang lib) should have a Unicode module (or set of modules)
// to deal with Unicode properly. These will need to be updated as each
// new Unicode version is released.

# Converts occurrences of A-Z to a-z.
#
# Other characters are left unchanged.
#
# ```ballerina
# "HELLO, World!".toLowerAscii() ⇒ hello, world!
# ```
#
# + str - the string to be converted
# + return - parameter `str` with any occurrences of A-Z converted to a-z
public isolated function toLowerAscii(string str) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.ToLowerAscii",
    name: "toLowerAscii"
} external;

# Converts occurrences of a-z to A-Z.
#
# Other characters are left unchanged.
#
# ```ballerina
# "hello, World!".toUpperAscii() ⇒ HELLO, WORLD!
# ```
#
# + str - the string to be converted
# + return - parameter `str` with any occurrences of a-z converted to A-Z
public isolated function toUpperAscii(string str) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.ToUpperAscii",
    name: "toUpperAscii"
} external;

# Tests whether two strings are the same, ignoring the case of ASCII characters.
#
# A character in the range a-z is treated the same as the corresponding character in the range A-Z.
#
# ```ballerina
# "BALLERINA".equalsIgnoreCaseAscii("ballerina") ⇒ true
# ```
#
# + str1 - the first string to be compared
# + str2 - the second string to be compared
# + return - true if parameter `str1` is the same as parameter `str2`, treating upper-case and lower-case
#           ASCII letters as the same; false, otherwise
public isolated function equalsIgnoreCaseAscii(string str1, string str2) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.string.EqualsIgnoreCaseAscii",
    name: "equalsIgnoreCaseAscii"
} external;

# Removes ASCII white space characters from the start and end of a string.
#
# The ASCII white space characters are 0x9...0xD, 0x20.
#
# ```ballerina
# " Hello World   ".trim() + "!" ⇒ Hello World!
# ```
#
# + str - the string
# + return - parameter `str` with leading or trailing ASCII white space characters removed
public isolated function trim(string str) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.Trim",
    name: "trim"
} external;

# Represents a string as an array of bytes using UTF-8.
#
# ```ballerina
# "Hello, World!".toBytes() ⇒ [72,101,108,108,111,44,32,87,111,114,108,100,33]
# ```
#
# + str - the string
# + return - UTF-8 byte array
public isolated function toBytes(string str) returns byte[] = @java:Method {
    'class: "org.ballerinalang.langlib.string.ToBytes",
    name: "toBytes"
} external;

# Constructs a string from its UTF-8 representation in bytes.
#
# ```ballerina
# string:fromBytes([72, 101, 108, 108, 111, 32, 66, 97, 108, 108, 101, 114, 105, 110, 97, 33]) ⇒ Hello, World!
#
# string:fromBytes([149, 169, 224]) ⇒ error
# ```
#
# + bytes - UTF-8 byte array
# + return - parameter `bytes` converted to string or error
public isolated function fromBytes(byte[] bytes) returns string|error = @java:Method {
    'class: "org.ballerinalang.langlib.string.FromBytes",
    name: "fromBytes"
} external;

# Converts a string to an array of code points.
#
# ```ballerina
# "Hello, world 🌎".toCodePointInts() ⇒ [72,101,108,108,111,44,32,119,111,114,108,100,32,127758]
# ```
#
# + str - the string
# + return - an array with a code point for each character of parameter `str`
public isolated function toCodePointInts(string str) returns int[] = @java:Method {
    'class: "org.ballerinalang.langlib.string.ToCodePointInts",
    name: "toCodePointInts"
} external;

# Converts a single character string to a code point.
#
# ```ballerina
# string:toCodePointInt("a") ⇒ 97
# ```
#
# + ch - a single character string
# + return - the code point of parameter `ch`
public isolated function toCodePointInt(Char ch) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.string.ToCodePointInt",
    name: "toCodePointInt"
} external;

# Constructs a string from an array of code points.
#
# An int is a valid code point if it is in the range 0 to 0x10FFFF inclusive,
# but not in the range 0xD800 or 0xDFFF inclusive.
#
# ```ballerina
# string:fromCodePointInts([66, 97, 108, 108, 101, 114, 105, 110, 97]) ⇒ Ballerina
#
# string:fromCodePointInts([1114113, 1114114, 1114115]) ⇒ error
# ```
#
# + codePoints - an array of ints, each specifying a code point
# + return - a string with a character for each code point in parameter `codePoints`; or an error
# if any member of parameter `codePoints` is not a valid code point
public isolated function fromCodePointInts(int[] codePoints) returns string|error = @java:Method {
    'class: "org.ballerinalang.langlib.string.FromCodePointInts",
    name: "fromCodePointInts"
} external;

# Constructs a single character string from a code point.
#
# An int is a valid code point if it is in the range 0 to 0x10FFFF inclusive,
# but not in the range 0xD800 or 0xDFFF inclusive.
#
# ```ballerina
# string:fromCodePointInt(97) ⇒ a
#
# string:fromCodePointInt(1114113) ⇒ error
# ```
#
# + codePoint - an int specifying a code point
# + return - a single character string whose code point is parameter `codePoint`; or an error
# if parameter `codePoint` is not a valid code point
public isolated function fromCodePointInt(int codePoint) returns Char|error = @java:Method {
    'class: "org.ballerinalang.langlib.string.FromCodePointInt",
    name: "fromCodePointInt"
} external;

# Adds padding to the start of a string.
# Adds sufficient `padChar` characters at the start of `str` to make its length be `len`.
# If the length of `str` is >= `len`, returns `str`.
#
# ```ballerina
# "100Km".padStart(10) ⇒      100Km
#
# "100Km".padStart(10, "0") ⇒ 00000100Km
# ```
#
# + str - the string to pad
# + len - the length of the string to be returned
# + padChar - the character to use for padding `str`; defaults to a space character
# + return - `str` padded with `padChar`
public isolated function padStart(string str, int len, Char padChar = " ") returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.PadStart",
    name: "padStart"
} external;

# Adds padding to the end of a string.
# Adds sufficient `padChar` characters to the end of `str` to make its length be `len`.
# If the length of `str` is >= `len`, returns `str`.
#
# ```ballerina
# "Ballerina for developers".padEnd(30) ⇒ Ballerina for developers
#
# "Ballerina for developers".padEnd(30, "!") ⇒ Ballerina for developers!!!!!!
# ```
#
# + str - the string to pad
# + len - the length of the string to be returned
# + padChar - the character to use for padding `str`; defaults to a space character
# + return - `str` padded with `padChar`
public isolated function padEnd(string str, int len, Char padChar = " ") returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.PadEnd",
    name: "padEnd"
} external;

# Pads a string with zeros.
# The zeros are added at the start of the string, after a `+` or `-` sign if there is one.
# Sufficient zero characters are added to `str` to make its length be `len`.
# If the length of `str` is >= `len`, returns `str`.
#
# ```ballerina
# "-256".padZero(9) ⇒ -00000256
#
# "-880".padZero(8, "#") ⇒ -####880
# ```
#
# + str - the string to pad
# + len - the length of the string to be returned
# + zeroChar - the character to use for the zero; defaults to ASCII zero `0`
# + return - `str` padded with zeros
public isolated function padZero(string str, int len, Char zeroChar = "0") returns string = @java:Method {
    'class: "org.ballerinalang.langlib.string.PadZero",
    name: "padZero"
} external;

# Refers to the `RegExp` type defined by lang.regexp module.
public type RegExp regexp:RegExp;

# Tests whether there is a full match of a regular expression with a string.
# A match of a regular expression in a string is a full match if it
# starts at index 0 and ends at index `n`, where `n` is the length of the string.
# This is equivalent to `regex:isFullMatch(re, str)`.
#
# ```ballerina
# "This is a Match".matches(re `A|Th.*ch|^`) ⇒ true
#
# "Not a Match".matches(re `A|Th.*ch|^`) ⇒ false
# ```
#
# + str - the string
# + re - the regular expression
# + return - true if there is full match of `re` with `str`, and false otherwise
public function matches(string str, RegExp re) returns boolean {
   return re.isFullMatch(str);
}

# Tests whether there is a match of a regular expression somewhere within a string.
# This is equivalent to `regexp:find(re, str, startIndex) != ()`.
#
# ```ballerina
# "This will match".includesMatch(re `Th.*ch`) ⇒ true
#
# "Will this match".includesMatch(re `th.*ch`, 5) ⇒ true
#
# "Not a match".includesMatch(re `Th.*ch`) ⇒ false
#
# "Will this match".includesMatch(re `Th.*ch`, 5) ⇒ false
# ```
#
# + str - the string to be matched
# + re - the regular expression
# + return - true if the is a match of `re` somewhere within `str`, otherwise false
public function includesMatch(string str, RegExp re, int startIndex = 0) returns boolean {
   return re.find(str, startIndex) != ();
}
