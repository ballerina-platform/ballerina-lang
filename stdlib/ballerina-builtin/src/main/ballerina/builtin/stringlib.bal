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

@Description { value:"Compares two strings, ignoring the case of the strings"}
@Param { value:"mainString: The original string argument" }
@Param { value:"anotherString: The string to be compared" }
@Return { value:"True if the strings are equal; false otherwise" }
public native function <string mainString> equalsIgnoreCase (string anotherString) returns (boolean);

@Description { value:"Returns a string with all the characters converted to uppercase"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The string converted to uppercase" }
public native function <string mainString> toUpperCase () returns (string);

@Description { value:"Returns a new string that is the substring of the specified string"}
@Param { value:"mainString: The original string argument" }
@Param { value:"beginIndex: The starting index" }
@Param { value:"endIndex: The ending index" }
@Return { value:"The derived sub string" }
public native function <string mainString> subString (int beginIndex, int endIndex) returns (string);

@Description { value:"Returns the first index of the last occurence of the substring within the specified string"}
@Param { value:"mainString: The original string argument" }
@Param { value:"subString: The substring to search for" }
@Return { value:"The index of the last occurence of the substring" }
public native function <string mainString> lastIndexOf (string subString) returns (int);

@Description { value:"Replaces the first instance of the replacePattern with the replaceWith string and returns the result"}
@Param { value:"mainString: The original string argument" }
@Param { value:"replacePattern: The pattern to search for " }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
public native function <string mainString> replaceFirst (string replacePattern, string replaceWith) returns (string);

@Description { value:"Returns the length of the specified string "}
@Param { value:"mainString: The original string argument" }
@Return { value:"The length of the specified string" }
public native function <string mainString> length () returns (int);

@Description { value:"Returns a Boolean value indicating whether a string contains the specified substring"}
@Param { value:"mainString: The original string argument" }
@Param { value:"subString: The substring to be compared" }
@Return { value:"True if the string contains the substring; false otherwise" }
public native function <string mainString> contains (string subString) returns (boolean);

@Description { value:"Returns the first index of the first occurence of the substring within the specified string"}
@Param { value:"mainString: The original string argument" }
@Param { value:"subString: The substring to search for" }
@Return { value:"The index of the first occurence of the substring" }
public native function <string mainString> indexOf (string subString) returns (int);

@Description { value:"Returns a trimmed string by omitting the leading and trailing whitespaces of the original string"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The derived string" }
public native function <string mainString> trim () returns (string);

@Description { value:"Returns a Boolean value indicating whether the string ends with specified suffix"}
@Param { value:"mainString: The original string argument" }
@Param { value:"suffix: The suffix to be compared" }
@Return { value:"True if the string ends with the suffix; false otherwise" }
public native function <string mainString> hasSuffix (string suffix) returns (boolean);

@Description { value:"Returns an unescaped string by omitting the escape characters of the original string"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The derived string" }
public native function <string mainString> unescape () returns (string);

@Description { value:"Returns a string with all the characters converted to lowercase"}
@Param { value:"mainString: The original string argument" }
@Return { value:"The string converted to lowercase" }
public native function <string mainString> toLowerCase () returns (string);

@Description { value:"Returns a Boolean value indicating whether a string starts with the specified prefix"}
@Param { value:"mainString: The original string argument" }
@Param { value:"prefix: The prefix to be compared" }
@Return { value:"True if the string starts with the prefix; false otherwise" }
public native function <string mainString> hasPrefix (string prefix) returns (boolean);

@Description { value:"Replaces each substring of the mainString that matches the given regular expression with the given replacement"}
@Param { value:"mainString: The original string argument" }
@Param { value:"replacePattern: The regular expression to search for" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
public native function <string mainString> replaceAll (string replacePattern, string replaceWith) returns (string);

@Description { value:"Replaces all instances of the replacePattern string with the replaceWith string and returns the result"}
@Param { value:"mainString: The original string argument" }
@Param { value:"replacePattern: The pattern to search for" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
public native function <string mainString> replace (string replacePattern, string replaceWith) returns (string);

@Description { value:"Splits the string with the given regular expression to produce a string array."}
@Param { value:"mainString: The original string argument" }
@Param { value:"regex: The regex to split the string" }
@Return { value:"The split string array" }
public native function <string mainString> split (string regex) returns (string[]);

@Description { value:"Converts string to a blob"}
@Param { value:"mainString: string value to be converted" }
@Param { value:"encoding: Encoding to used in the conversion" }
@Return { value:"The blob representation of the given String" }
public native function <string mainString> toBlob (string encoding) returns (blob);

@Description { value: "Represents a Regular expression in ballerina and can perform various Regular expression methods."}
@Field { value : "regex:Pattern as a String"}
public type Regex {
   string pattern;
};

@Description { value:"Finds all the strings matching the regular expression"}
@Param { value:"mainString: The original string argument" }
@Param { value:"reg: Regular expression" }
@Return { value: "The matching string array"}
@Return { value:"error: Error will be returned if there exist a syntax error in pattern" }
public native function <string mainString> findAllWithRegex (Regex reg) returns (string[] | error);

@Description { value:"Returns a Boolean value indicating whether the string matches the regular expression"}
@Param { value:"mainString: The original string argument" }
@Param { value:"reg: Regular expression" }
@Return { value: "True if the string matches the regex; false otherwise"}
@Return { value:"error: Error will be returned if there exist a syntax error in pattern" }
public native function <string mainString>  matchesWithRegex (Regex reg) returns (boolean | error);

@Description { value:"Replaces the mainString with the replacement of occurrences that matches the given regular expression" }
@Param { value:"mainString: The original string argument" }
@Param { value:"reg: Regular expression" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
@Return { value:"error: Error will be returned if there exist a syntax error in pattern" }
public native function <string mainString> replaceAllWithRegex (Regex reg, string replaceWith) returns (string | error);

@Description { value:"Replaces the first instance of the regular expression matching area with the replaceWith string and returns the result"}
@Param { value:"mainString: The original string argument" }
@Param { value:"reg: Regular expression" }
@Param { value:"replaceWith: The replacement string" }
@Return { value:"The derived string" }
@Return { value:"error: Error will be returned if there exist a syntax error in pattern" }
public native function <string mainString> replaceFirstWithRegex (Regex reg, string replaceWith) returns (string | error);
