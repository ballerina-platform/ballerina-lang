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

package ballerina.builtin;

@Description {value:"Compares two strings, ignoring the case of the strings"}
@Param {value:"s: The original string argument"}
@Param {value:"anotherString: The string to be compared"}
@Return {value:"True if the strings are equal; false otherwise"}
public native function<string s> equalsIgnoreCase(string anotherString) returns (boolean);

@Description {value:"Returns a string with all the characters converted to uppercase"}
@Param {value:"s: The original string argument"}
@Return {value:"The string converted to uppercase"}
public native function<string s> toUpperCase() returns (string);

@Description {value:"Returns a new string that is the substring of the specified string"}
@Param {value:"s: The original string argument"}
@Param {value:"beginIndex: The starting index"}
@Param {value:"endIndex: The ending index"}
@Return {value:"The derived sub string"}
public native function<string s> subString(int beginIndex, int endIndex) returns (string);

@Description {value:"Returns the first index of the last occurence of the substring within the specified string"}
@Param {value:"s: The original string argument"}
@Param {value:"subString: The substring to search for"}
@Return {value:"The index of the last occurence of the substring"}
public native function<string s> lastIndexOf(string subString) returns (int);

@Description {value:"Replaces the first instance of the replacePattern with the replaceWith string and returns the
result"}
@Param {value:"s: The original string argument"}
@Param {value:"replacePattern: The pattern to search for "}
@Param {value:"replaceWith: The replacement string"}
@Return {value:"The derived string"}
public native function<string s> replaceFirst(string replacePattern, string replaceWith) returns (string);

@Description {value:"Returns the length of the specified string "}
@Param {value:"s: The original string argument"}
@Return {value:"The length of the specified string"}
public native function<string s> length() returns (int);

@Description {value:"Returns a Boolean value indicating whether a string contains the specified substring"}
@Param {value:"s: The original string argument"}
@Param {value:"subString: The substring to be compared"}
@Return {value:"True if the string contains the substring; false otherwise"}
public native function<string s> contains(string subString) returns (boolean);

@Description {value:"Returns the first index of the first occurence of the substring within the specified string"}
@Param {value:"s: The original string argument"}
@Param {value:"subString: The substring to search for"}
@Return {value:"The index of the first occurence of the substring"}
public native function<string s> indexOf(string subString) returns (int);

@Description {value:"Returns a trimmed string by omitting the leading and trailing whitespaces of the original string"}
@Param {value:"s: The original string argument"}
@Return {value:"The derived string"}
public native function<string s> trim() returns (string);

@Description {value:"Returns a Boolean value indicating whether the string ends with specified suffix"}
@Param {value:"s: The original string argument"}
@Param {value:"suffix: The suffix to be compared"}
@Return {value:"True if the string ends with the suffix; false otherwise"}
public native function<string s> hasSuffix(string suffix) returns (boolean);

@Description {value:"Returns an unescaped string by omitting the escape characters of the original string"}
@Param {value:"s: The original string argument"}
@Return {value:"The derived string"}
public native function<string s> unescape() returns (string);

@Description {value:"Returns a string with all the characters converted to lowercase"}
@Param {value:"s: The original string argument"}
@Return {value:"The string converted to lowercase"}
public native function<string s> toLowerCase() returns (string);

@Description {value:"Returns a Boolean value indicating whether a string starts with the specified prefix"}
@Param {value:"s: The original string argument"}
@Param {value:"prefix: The prefix to be compared"}
@Return {value:"True if the string starts with the prefix; false otherwise"}
public native function<string s> hasPrefix(string prefix) returns (boolean);

@Description {value:"Replaces each substring of the string that matches the given regular expression with the
given replacement"}
@Param {value:"s: The original string argument"}
@Param {value:"replacePattern: The regular expression to search for"}
@Param {value:"replaceWith: The replacement string"}
@Return {value:"The derived string"}
public native function<string s> replaceAll(string replacePattern, string replaceWith) returns (string);

@Description {value:"Replaces all instances of the replacePattern string with the replaceWith string and returns the
result"}
@Param {value:"s: The original string argument"}
@Param {value:"replacePattern: The pattern to search for"}
@Param {value:"replaceWith: The replacement string"}
@Return {value:"The derived string"}
public native function<string s> replace(string replacePattern, string replaceWith) returns (string);

@Description {value:"Splits the string with the given regular expression to produce a string array."}
@Param {value:"s: The original string argument"}
@Param {value:"regex: The regex to split the string"}
@Return {value:"The split string array"}
public native function<string s> split(string regex) returns (string[]);

@Description {value:"Converts string to a blob"}
@Param {value:"s: string value to be converted"}
@Param {value:"encoding: Encoding to used in the conversion"}
@Return {value:"The blob representation of the given String"}
public native function<string s> toBlob(string encoding) returns (blob);

@Description {value:"Represents a Regular expression in ballerina and can perform various Regular expression
methods."}
@Field {value:"regex:Pattern as a String"}
public type Regex {
    string pattern;
};

@Description {value:"Finds all the strings matching the regular expression"}
@Param {value:"s: The original string argument"}
@Param {value:"reg: Regular expression"}
@Return {value:"The matching string array"}
@Return {value:"error: Error will be returned if there exist a syntax error in pattern"}
public native function<string s> findAllWithRegex(Regex reg) returns (string[]|error);

@Description {value:"Returns a Boolean value indicating whether the string matches the regular expression"}
@Param {value:"s: The original string argument"}
@Param {value:"reg: Regular expression"}
@Return {value:"True if the string matches the regex; false otherwise"}
@Return {value:"error: Error will be returned if there exist a syntax error in pattern"}
public native function<string s> matchesWithRegex(Regex reg) returns (boolean|error);

@Description {value:"Replaces the string with the replacement of occurrences that matches the given regular
expression"}
@Param {value:"s: The original string argument"}
@Param {value:"reg: Regular expression"}
@Param {value:"replaceWith: The replacement string"}
@Return {value:"The derived string"}
@Return {value:"error: Error will be returned if there exist a syntax error in pattern"}
public native function<string s> replaceAllWithRegex(Regex reg, string replaceWith) returns (string|error);

@Description {value:"Replaces the first instance of the regular expression matching area with the replaceWith string
and returns the result"}
@Param {value:"s: The original string argument"}
@Param {value:"reg: Regular expression"}
@Param {value:"replaceWith: The replacement string"}
@Return {value:"The derived string"}
@Return {value:"error: Error will be returned if there exist a syntax error in pattern"}
public native function<string s> replaceFirstWithRegex(Regex reg, string replaceWith)
    returns (string|error);

@Description {value:"Encode a given string with Base64 encoding scheme."}
@Param {value:"s: Content that needs to be encoded"}
@Param {value:"charset: Charset to be used"}
@Return {value:"Return an encoded string"}
@Return {value:"error will get return, in case of errors"}
public native function<string s> base64Encode(string charset = "utf-8") returns string|error;

@Description {value:"Decode a given string with Base64 encoding scheme."}
@Param {value:"s: Content that needs to be decoded"}
@Param {value:"charset: Charset to be used"}
@Return {value:"Return a decoded string"}
@Return {value:"error will get return, in case of errors"}
public native function<string s> base64Decode(string charset = "utf-8") returns string|error;

@Description {value:"Encodes a base16 encoded string to base64 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public native function<string s> base16ToBase64Encode() returns string;

@Description {value:"Encodes a base64 encoded string to base16 encoding."}
@Param {value:"s: string to be encoded"}
@Return {value:"the encoded string."}
public native function<string s> base64ToBase16Encode() returns string;
