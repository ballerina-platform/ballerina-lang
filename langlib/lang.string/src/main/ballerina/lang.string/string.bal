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
# + str - The source string
# + anotherString - The string to be compared
# + return - True if the strings are equal; false otherwise
public function equalsIgnoreCase(string str, string anotherString) returns boolean = external;

# Returns a string with all the characters converted to uppercase.
#
# + str - The source string
# + return - The string converted to uppercase
public function toUpper(string str) returns string = external;

# Returns a new string that is the substring of the specified string.
#
# + str - The source string
# + startIndex - The starting index
# + endIndex - The ending index
# + return - The derived sub string
public function substring(string str, int startIndex, int endIndex) returns string = external;

# Returns the first index of the last occurence of the substring within the specified string.
#
# + str - The source string
# + substr - The substring to search for
# + return - The index of the last occurence of the substring
public function lastIndexOf(string str, string substr) returns int = external;

# Replaces the first instance of the replacePattern with the replaceWith string and returns the result.
#
# + str - The source string
# + regex - The pattern to search for
# + replaceWith - The replacement string
# + return - The derived string
public function replaceFirst(string str, string regex, string replaceWith) returns string = external;

# Returns the length of the specified string.
#
# + str - The source string
# + return - The length of the specified string
public function length(string str) returns int = external;

# Returns a Boolean value indicating whether a string contains the specified substring.
#
# + str - The source string
# + substr - The substring to be compared
# + return - True if the string contains the substring; false otherwise
public function contains(string str, string substr) returns boolean = external;

# Returns the first index of the first occurence of the substring within the specified string.
#
# + str - The source string
# + substr - The substring to search for
# + return - The index of the first occurence of the substring
public function indexOf(string str, string substr) returns int = external;

# Returns a trimmed string by omitting the leading and trailing whitespaces of the original string.
#
# + str - The source string
# + return - The derived string
public function trim(string str) returns string = external;

# Returns a Boolean value indicating whether the string ends with specified suffix.
#
# + str - The source string
# + suffix - The suffix to be compared
# + return - True if the string ends with the suffix; false otherwise
public function hasSuffix(string str, string suffix) returns boolean = external;

# Returns an unescaped string by omitting the escape characters of the original string.
#
# + str - The source string
# + return - The derived string
public function unescape(string str) returns string = external;

# Returns a string with all the characters converted to lowercase.
#
# + str - The source string
# + return - The string converted to lowercase
public function toLower(string str) returns string = external;

# Returns a Boolean value indicating whether a string starts with the specified prefix.
#
# + str - The source string
# + prefix - The prefix to be compared
# + return - True if the string starts with the prefix; false otherwise
public function hasPrefix(string str, string prefix) returns boolean = external;

# Replaces each substring of the string that matches the given regular expression with the given replacement.
#
# + str - The source string
# + regex - The regular expression to search for
# + replaceWith - The replacement string
# + return - The derived string
public function replaceAll(string str, string regex, string replaceWith) returns string = external;

# Replaces all instances of the replacePattern string with the replaceWith string and returns the result.
#
# + str - The source string
# + regex - The pattern to search for
# + replaceWith - The replacement string
# + return - The derived string
public function replace(string str, string regex, string replaceWith) returns string = external;

# Splits the string with the given regular expression to produce a string array.
#
# + str - The source string
# + regex - The regex to split the string
# + return - The split string array
public function split(string str, string regex) returns string[] = external;

# Finds all the strings matching the regular expression.
#
# + str - The source string
# + regex - Regular expression
# + return - The matching string array.
#            Error will be returned if there exist a syntax error in pattern
public function findAll(string str, string regex) returns string[]|error = external;

# Returns a Boolean value indicating whether the string matches the regular expression.
#
# + str - The source string
# + regex - Regular expression
# + return - True if the string matches the regex; false otherwise.
#            Error will be returned if there exist a syntax error in pattern.
public function matches(string str, string regex) returns boolean|error = external;

# Converts string to a byte array.
#
# + str - The source string
# + encoding - Encoding to be used in the conversion
# + return - The byte array representation of the given String
public function toByteArray(string str, string encoding) returns byte[] = external;

# Returns a hash code for this string.
#
# + str - The source string
# + return - a hash code value for this string.
public function hashCode(string str) returns int = external;
