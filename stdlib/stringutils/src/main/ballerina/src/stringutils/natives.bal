// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/io;

# Checks whether the given string contains a particular substring.
# ```ballerina
# boolean contains = stringutils:contains("Ballerina", "in");
# ```
# + originalString - The string to check whether it contains the `substring`
# + substring - The substring to find within the `originalString`
# + return - `true` if the `originalString` contains the provided `substring`
#            or else `false`
public function contains(string originalString, string substring) returns boolean {
    return containsExternal(java:fromString(originalString), java:fromString(substring));
}

# Checks if two strings are equal ignoring the case of the strings.
# ```ballerina
# boolean isEqual = stringutils:equalsIgnoreCase("BaLLerinA", "ballERina");
# ```
#
# + firstString - The first string to compare
# + secondString - The second string to compare
# + return - `true` if the two strings are equal or else `false`
public function equalsIgnoreCase(string firstString, string secondString) returns boolean {
    return equalsIgnoreCaseExternal(java:fromString(firstString), java:fromString(secondString));
}

# Returns a hash code for a given string.
# ```ballerina
# int hashCode = stringutils:hashCode("Ballerina");
# ```
#
# + stringValue - The string to generate the hash code
# + return - The hash code for the given string
public function hashCode(string stringValue) returns int {
    return hashCodeExternal(java:fromString(stringValue));
}

# Returns the last index of the provided substring within a string.
# ```ballerina
# int lastIndex = stringutils:lastIndexOf("int values in Ballerina", "in");
# ```
#
# + originalString - The string to search for the index of the `substring`
# + substring - The string to search in the `originalString`
# + return - Starting index of the last appearance of the provided substring
#            if the `originalString` contains the `substring` or else `-1`
public function lastIndexOf(string originalString, string substring) returns int {
    return lastIndexOfExternal(java:fromString(originalString), java:fromString(substring));
}

# Checks whether the given string matches the provided regex.
# ```ballerina
# boolean isMatched = stringutils:matches("Ballerina is great", "Ba[a-z ]+");
# ```
#
# + stringToMatch - The string to match the regex
# + regex - The regex to match the string
# + return - `true` if the provided string matches the regex or else
#            `false`
public function matches(string stringToMatch, string regex) returns boolean {
    return matchesExternal(java:fromString(stringToMatch), java:fromString(regex));
}

# Replaces each occurrence of the provided `substring` inside the provided
# `originalString` with the specified `replacement` string.
# ```ballerina
# string result = stringutils:replace("Ballerina is great", " ", "_");
# ```
#
# + originalString - The original string to replace the substrings
# + stringToReplace - The string to replace within the `originalString`
# + replacement - The `replacement` string to replace the occurrences of
#                 the `stringToReplace`
# + return - The string with the replaced substrings
public function replace(string originalString, string stringToReplace, string replacement) returns string {
    handle value = replaceExternal(java:fromString(originalString), java:fromString(stringToReplace),
                                   java:fromString(replacement));
    string? updatedString = java:toString(value);
    if (updatedString is string) {
        return updatedString;
    } else {
        // should never reach here
        error e = error(io:sprintf("error occurred while replacing '%s' in '%s' ", stringToReplace, originalString));
        panic e;
    }
}

# Replaces each occurrence of the substrings, which matches the provided
# regular expression from the given original string value with the
# provided replacement string.
# ```ballerina
# string result = stringutils:replaceAll("Ballerina is great", "\s+", "_");
# ```
#
# + originalString - The original string to replace the occurrences of the
#                    substrings that match the provided `regex`
# + regex - The regex to match the substrings in the `originalString` to be replaced
# + replacement - The `replacement` string to replace the subsgrings, which
#                 match the `regex`
# + return - The resultant string with the replaced substrings
public function replaceAll(string originalString, string regex, string replacement) returns string {
    handle value = replaceAllExternal(java:fromString(originalString), java:fromString(regex),
                                      java:fromString(replacement));
    string? updatedString = java:toString(value);
    if (updatedString is string) {
        return updatedString;
    } else {
        // should never reach here
        error e = error(io:sprintf("error occurred while replacing '%s' in '%s' ", regex, originalString));
        panic e;
    }
}

# Replaces the first substring that matches the given regular expression with
# the provided `replacement` string.
# ```ballerina
# string result = stringutils:replaceFirst("Ballerina is great", "\s+", "_");
# ```
#
# + originalString - The original string to replace the occurrences of the
#                    substrings that match the provided `regex`
# + regex - The regex to match the first substring in the `originalString` to
#           be replaced
# + replacement - The `replacement` string to replace the first substring, which
#                 matches the `regex`
# + return - The resultant string with the replaced substring
public function replaceFirst(string originalString, string regex, string replacement) returns string {
    handle value = replaceFirstExternal(java:fromString(originalString), java:fromString(regex),
                                        java:fromString(replacement));
    string? updatedString = java:toString(value);
    if (updatedString is string) {
        return updatedString;
    } else {
        // should never reach here
        error e = error(io:sprintf("error occurred while replacing '%s' in '%s' ", regex, originalString));
        panic e;
    }
}

# Returns an array of strings by splitting a string using the provided
# delimiter.
# ```ballerina
# string[] result = stringutils:split("Ballerina is great", " ");
# ```
#
# + receiver - The string to split
# + delimiter - The delimiter to split by
# + return - An array of strings containing the individual strings that are split
public function split(string receiver, string delimiter) returns string[] {
    handle res = splitExternal(java:fromString(receiver), java:fromString(delimiter));
    return getBallerinaStringArray(res);
}

# Returns a boolean value of a given string.
# ```ballerina
# boolean result = stringutils:toBoolean("true");
# ```
#
# + stringValue - string value to convert to a boolean
# + return - `true` if the string is `"true"` (without considering the case)
#            or else `false`
public function toBoolean(string stringValue) returns boolean {
    return toBooleanExternal(java:fromString(stringValue));
}

// Interoperable external functions.
function containsExternal(handle originalString, handle substring) returns boolean = @java:Method {
    name: "contains",
    class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

function equalsIgnoreCaseExternal(handle firstString, handle secondString) returns boolean = @java:Method {
    name: "equalsIgnoreCase",
    class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

function lastIndexOfExternal(handle originalString, handle substring) returns int = @java:Method {
    name: "lastIndexOf",
    class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

function matchesExternal(handle stringToMatch, handle regex) returns boolean = @java:Method {
    name: "matches",
    class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

function replaceExternal(handle originalString, handle stringToReplace, handle replacement)
                         returns handle = @java:Method {
    name: "replace",
    class: "java.lang.String",
    paramTypes: ["java.lang.String", "java.lang.String"]
} external;

function replaceAllExternal(handle originalString, handle regex, handle replacement) returns handle = @java:Method {
    name: "replaceAll",
    class: "java.lang.String",
    paramTypes: ["java.lang.String", "java.lang.String"]
} external;

function replaceFirstExternal(handle originalString, handle regex, handle replacement) returns handle = @java:Method {
    name: "replaceFirst",
    class: "java.lang.String",
    paramTypes: ["java.lang.String", "java.lang.String"]
} external;

function splitExternal(handle receiver, handle delimiter) returns handle = @java:Method {
    name: "split",
    class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

function hashCodeExternal(handle stringValue) returns int = @java:Method {
    name: "hashCode",
    class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

function toBooleanExternal(handle stringValue) returns boolean = @java:Method {
    name: "valueOf",
    class: "java.lang.Boolean",
    paramTypes: ["java.lang.String"]
} external;

function getBallerinaStringArray(handle h) returns string[] = @java:Constructor {
    class:"org/ballerinalang/jvm/values/ArrayValueImpl",
    paramTypes:["[Ljava.lang.String;"]
} external;
