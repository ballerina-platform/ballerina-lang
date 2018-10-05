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

# Compares two strings, ignoring the case of the strings.
#
# + anotherString - The string to be compared
# + return - True if the strings are equal; false otherwise
public extern function string::equalsIgnoreCase(string anotherString) returns boolean;

# Returns a string with all the characters converted to uppercase.
#
# + return - The string converted to uppercase
public extern function string::toUpper() returns string;

# Returns a new string that is the substring of the specified string.
#
# + startIndex - The starting index
# + endIndex - The ending index
# + return - The derived sub string
public extern function string::substring(int startIndex, int endIndex) returns string;

# Returns the first index of the last occurence of the substring within the specified string.
#
# + substring - The substring to search for
# + return - The index of the last occurence of the substring
public extern function string::lastIndexOf(string substring) returns int;

# Replaces the first instance of the replacePattern with the replaceWith string and returns the result.
#
# + regex - The pattern to search for
# + replaceWith - The replacement string
# + return - The derived string
public extern function string::replaceFirst(string regex, string replaceWith) returns string;

# Returns the length of the specified string.
#
# + return - The length of the specified string
public extern function string::length() returns int;

# Returns a Boolean value indicating whether a string contains the specified substring.
#
# + substring - The substring to be compared
# + return - True if the string contains the substring; false otherwise
public extern function string::contains(string substring) returns boolean;

# Returns the first index of the first occurence of the substring within the specified string.
#
# + substring - The substring to search for
# + return - The index of the first occurence of the substring
public extern function string::indexOf(string substring) returns int;

# Returns a trimmed string by omitting the leading and trailing whitespaces of the original string.
#
# + return - The derived string
public extern function string::trim() returns string;

# Returns a Boolean value indicating whether the string ends with specified suffix.
#
# + suffix - The suffix to be compared
# + return - True if the string ends with the suffix; false otherwise
public extern function string::hasSuffix(string suffix) returns boolean;

# Returns an unescaped string by omitting the escape characters of the original string.
#
# + return - The derived string
public extern function string::unescape() returns string;

# Returns a string with all the characters converted to lowercase.
#
# + return - The string converted to lowercase
public extern function string::toLower() returns string;

# Returns a Boolean value indicating whether a string starts with the specified prefix.
#
# + prefix - The prefix to be compared
# + return - True if the string starts with the prefix; false otherwise
public extern function string::hasPrefix(string prefix) returns boolean;

# Replaces each substring of the string that matches the given regular expression with the given replacement.
#
# + regex - The regular expression to search for
# + replaceWith - The replacement string
# + return - The derived string
public extern function string::replaceAll(string regex, string replaceWith) returns string;

# Replaces all instances of the replacePattern string with the replaceWith string and returns the result.
#
# + regex - The pattern to search for
# + replaceWith - The replacement string
# + return - The derived string
public extern function string::replace(string regex, string replaceWith) returns string;

# Splits the string with the given regular expression to produce a string array.
#
# + regex - The regex to split the string
# + return - The split string array
public extern function string::split(string regex) returns string[];

# Finds all the strings matching the regular expression.
#
# + regex - Regular expression
# + return - The matching string array.
#            Error will be returned if there exist a syntax error in pattern
public extern function string::findAll(string regex) returns string[]|error;

# Returns a Boolean value indicating whether the string matches the regular expression.
#
# + regex - Regular expression
# + return - True if the string matches the regex; false otherwise.
#            Error will be returned if there exist a syntax error in pattern.
public extern function string::matches(string regex) returns boolean|error;

# Encode a given string with Base64 encoding scheme.
#
# + charset - Charset to be used
# + return - Return an encoded string.
#            error will be returned in case of errors.
public extern function string::base64Encode(string charset = "utf-8") returns string|error;

# Decode a given string with Base64 encoding scheme.
#
# + charset - Charset to be used
# + return - Return a decoded string.
#            error will be returned in case of errors.
public extern function string::base64Decode(string charset = "utf-8") returns string|error;

# Encodes a base16 encoded string to base64 encoding.
#
# + return - the encoded string.
public extern function string::base16ToBase64Encode() returns string;

# Encodes a base64 encoded string to base16 encoding.
#
# + return - the encoded string.
public extern function string::base64ToBase16Encode() returns string;

# Converts string to a byte array.
#
# + encoding - Encoding to be used in the conversion
# + return - The byte array representation of the given String
public extern function string::toByteArray(string encoding) returns byte[];
