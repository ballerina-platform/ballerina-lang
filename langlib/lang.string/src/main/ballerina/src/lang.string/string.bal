// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type StringIterator object {

    private string m;

    public function __init(string m) {
        self.m = m;
    }

    public function next() returns record {|
        string value;
    |}? = external;
};

# Returns the length of the string.
#
# + str - the string
# + return - length of the `str`
public function length(string str) returns int = external;

# Returns an iterator over the string
# The iterator will return the substrings of length 1 in order.
#
# + str - the string
# + return - iterator object
public function iterator(string str) returns abstract object {
    public function next() returns record {|
        string value;
    |}?;
    } {
    StringIterator stringIterator = new(str);
    return stringIterator;
}

# Concatenate all the `strs`. Empty string if empty.
#
# + strs - strings to concat
# + return - concatanated string
public function concat(string... strs) returns string = external;

# Returns the unicode codepoint at index `i`.
#
# + str - the string
# + i - code point index
# + return - code point
public function getCodePoint(string str, int i) returns int = external;

// todo: endIndx should be a defaultable param: int endIndx = str.length()
# Returns a string that is a substring of this string.
#
# + str - source string.
# + startIndex - the beginning index, inclusive.
# + endIndex - the ending index, exclusive.
# + return - specified substring.
public function substring(string str, int startIndex, int endIndex) returns string = external;

# Lexicographically compare strings using their Unicode code points.
# This will allow strings to be ordered in a consistent and well-defined way,
# but the ordering will not typically be consistent with cultural expectations
# for sorted order.
#
# + str1 - string to compare
# + str2 - string to compare
# + return - whether `str1` is greater than `str2`
public function codePointCompare(string str1, string str2) returns int = external;

# Returns a new string composed of `strs` elements joined together with `separator`.
#
# + seperator - delimiter string
# + strs - strings to join
# + return - joined string
public function 'join(string separator, string... strs) returns string = external;

# Returns the index of the first occurrence of `substr` in the part of the `str` starting at `startIndex`
# or nil if it does not occur.
#
# + str - the string
# + substr - sub string to search for
# + start - index to start search from
# + return - index of first `substr` occurrence or nil
public function indexOf(string str, string substr, int startIndx = 0) returns int? = external;

# Returns true if `str` starts with `substr`.
#
# + str - the string
# + substr - sub string
# + return - whether `str` starts with `substr`
public function startsWith(string str, string substr) returns boolean = external;

# Returns true if `str` end with `substr`.
#
# + str - the string
# + substr - sub string
# + return - whether `str` ends with `substr`
public function endsWith(string str, string substr) returns boolean = external;

// Standard lib (not lang lib) should have a Unicode module (or set of modules)
// to deal with Unicode properly. These will need to be updated as each
// new Unicode version is released.

# Return A-Z into a-z and leave other characters unchanged.
#
# + str - the string
# + return - lower Ascii cased string
public function toLowerAscii(string str) returns string = external;

# Return a-z into A-Z and leave other characters unchanged.
#
# + str - the string
# + return - Ascii upper cased string
public function toUpperAscii(string str) returns string = external;

# Remove ASCII white space characters (0x9...0xD, 0x20) from start and end of `str`.
#
# + str - the string
# + return - trimmed string
public function trim(string str) returns string = external;

# Represents `str` as an array of bytes using UTF-8.
#
# + str - the string
# + return - UTF-8 byte array
public function toBytes(string str) returns byte[] = external;

# Convert back to a string from its UTF-8 representation in `bytes`.
#
# + bytes - UTF-8 byte array
# + return - `bytes` converted to string or error
public function fromBytes(byte[] bytes) returns string|error = external;

# Returns an array with an int for each code point in `str`.
#
# + str - the string
# + return - CodePoint array
public function toCodePointInts(string str) returns int[] = external;

# Creates a string from an array of ints representing its code points.
# Returns an error if any member of `codePoints` is negative or greater than 0x10FFFF
# or is a surrogate (i.e. in the range 0xD800 or 0xDFFF inclusive).
public function fromCodePointInts(int[] codePoints) returns string|error = external;
