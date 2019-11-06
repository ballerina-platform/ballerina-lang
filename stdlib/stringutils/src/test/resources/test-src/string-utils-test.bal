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

import ballerina/stringutils;

function testContains(string originalString, string substring) returns boolean {
    return stringutils:contains(originalString, substring);
}

function testEqualsIgnoreCase(string firstString, string secondString) returns boolean {
    return stringutils:equalsIgnoreCase(firstString, secondString);
}

function testHashCode(string stringValue) returns int {
    return stringutils:hashCode(stringValue);
}

function testLastIndexOf(string originalString, string substring) returns int {
    return stringutils:lastIndexOf(originalString, substring);
}

function testMatches(string stringToMatch, string regex) returns boolean {
    return stringutils:matches(stringToMatch, regex);
}

function testReplace() returns string {
    string testStr = "Hello Amal!!! Nimal!!!";
    string newStr = stringutils:replace(testStr, "!!!", "!");
    return newStr;
}

function testReplaceAll(string original, string regex, string replacement) returns string {
    return stringutils:replaceAll(original, regex, replacement);
}

function testReplaceFirst(string original, string regex, string replacement) returns string {
    return stringutils:replaceFirst(original, regex, replacement);
}

function testSplit() returns string[] {
    string testStr = "amal,,kamal,,nimal,,sunimal,";
    string[] arr = stringutils:split(testStr, ",,");
    return arr;
}

function testToBoolean(string stringValue) returns boolean {
    return stringutils:toBoolean(stringValue);
}
