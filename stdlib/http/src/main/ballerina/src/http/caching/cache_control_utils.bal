// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'int;
import ballerina/stringutils;

function appendFields (string[] fields) returns string {
    if (fields.length() > 0) {
        return "=\"" + buildCommaSeparatedString(fields) + "\"";
    }
    return "";
}

function buildCommaSeparatedString (string[] values) returns string {
    string delimitedValues = values[0];
    int arrLength = values.length();

    int i = 1;
    while (i < arrLength) {
        delimitedValues = delimitedValues + ", " + values[i];
        i = i + 1;
    }

    return delimitedValues;
}

function getDirectiveValue (string directive) returns int {
    string[] directiveParts = stringutils:split(directive, "=");

    // Disregarding the directive if a value isn't provided
    if (directiveParts.length() != 2) {
        return -1;
    }

    var age = 'int:fromString(directiveParts[1]);
    if (age is int) {
        return age;
    }
    return -1; // Disregarding the directive if the value cannot be parsed
}
