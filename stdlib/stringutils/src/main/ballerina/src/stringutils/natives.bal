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

import ballerinax/java;
import ballerina/io;

# Checks whether the given string contains a particular substring.
#
# + originalString - the original string
# + substring - string to match
# + return - `true` if the original string contains the substring or `false` otherwise
public function contains(string originalString, string substring) returns boolean {
    return containsExternal(java:fromString(originalString), java:fromString(substring));
}

# Checks if two strings are equal, ignoring the case of the strings.
#
# + firstString - first string to compare
# + secondString - second string to compare
# + return - `true` if the two strings are the same or `false` if the strings do not match
public function equalsIgnoreCase(string firstString, string secondString) returns boolean {
    return equalsIgnoreCaseExternal(java:fromString(firstString), java:fromString(secondString));
}

# Returns a hash code for a given string.
#
# + stringValue - string to generate the hash code
# + return - hash code for the given string
public function hashCode(string stringValue) returns int {
    return hashCodeExternal(java:fromString(stringValue));
}

# Returns the last index of the provided substring within a string.
#
# + originalString - the original string to search in
# + substring - string to look for
# + return - starting point of the last appearance of the provided substring
public function lastIndexOf(string originalString, string substring) returns int {
    return lastIndexOfExternal(java:fromString(originalString), java:fromString(substring));
}

# Checks whether the given string matches with the provided regex.
#
# + stringToMatch - string to match with the regex
# + regex - regex to match with the string
# + return - `true` if the provided string is matched with the regex, `false` otherwise
public function matches(string stringToMatch, string regex) returns boolean {
    return matchesExternal(java:fromString(stringToMatch), java:fromString(regex));
}

# Replaces each substring of the provided string, that matches the provided substring, with the specified replacement
# string.
#
# + originalString - original string
# + stringToReplace - string to replace
# + replacement - replacement string
# + return - the resultant string
public function replace(string originalString, string stringToReplace, string replacement) returns string {
    handle value = replaceExternal(java:fromString(originalString), java:fromString(stringToReplace), java:fromString(replacement));
    string? updatedString = java:toString(value);
    if (updatedString is string) {
        return updatedString;
    } else {
        // should never reach here
        error e = error(io:sprintf("error occurred while replacing '%s' in '%s' ", stringToReplace, originalString));
        panic e;
    }
}

# Replaces each substring which matches the given regular expression, from the given original string value, with the
# specified replacement string.
#
# + originalString - original string
# + regex - Regex to find substrings to replace
# + replacement - the replacement string
# + return - the resultant string
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

# Replaces the first substring that matches the given regular expression, with the specified replacement string.
#
# + originalString - the original string
# + regex - Regex to find substring to replace
# + replacement - the replacement string
# + return - the resultant string
public function replaceFirst(string originalString, string regex, string replacement) returns string {
    handle value = replaceFirstExternal(java:fromString(originalString),
                                java:fromString(regex),
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

# Splits a string using the given delimiter.
#
# + receiver - the original string
# + delimiter - delimiter to split by
# + return - array of strings containing the split individual strings
public function split(string receiver, string delimiter) returns string[] {
    handle res = splitExternal(java:fromString(receiver), java:fromString(delimiter));
    return getBallerinaStringArray(res);
}

# Returns a boolean value of a given string.
#
# + stringValue - string value to convert to a boolean
# + return - `true` is the string is `"true"` (without considering the case), returns `false` otherwise
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

function replaceExternal(handle originalString, handle stringToReplace, handle replacement) returns handle = @java:Method {
    name: "replace",
    class: "java.lang.String",
    paramTypes: ["java.lang.String", "java.lang.String"]
} external;

function replaceAllExternal(handle originalString, handle regex, handle replacement) returns handle = @java:Method {
    name: "replaceAll",
    class: "java.lang.String",
    paramTypes: ["java.lang.String", "java.lang.String"]
} external;

function replaceFirstExternal(handle originalString, handle regex, handle replacement) returns
                            handle = @java:Method {
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
