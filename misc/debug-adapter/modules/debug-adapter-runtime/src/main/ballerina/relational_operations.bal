// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
//

function lessThan(any lhs, any rhs) returns boolean|error {
    boolean|error result;
    if (lhs is () && rhs is ()) {
        result = trap (lhs < rhs); // () < ()
    } else if (lhs is boolean && rhs is boolean) {
        result = trap (lhs < rhs); // boolean < boolean
    } else if (lhs is int && rhs is int) {
        result = trap (lhs < rhs); // int < int
    } else if (lhs is float && rhs is float) {
        result = trap (lhs < rhs); // float < float
    } else if (lhs is decimal && rhs is decimal) {
        result = trap (lhs < rhs); // decimal < decimal
    } else if (lhs is string && rhs is string) {
        result = trap (lhs < rhs); // string < string
    } else {
        result = error("operator '<' not defined for '" + check getType(lhs) + "' and '" + check getType(rhs) + "'.");
    }
    return result;
}

function lessThanOrEquals(any lhs, any rhs) returns boolean|error {
    boolean|error result;
    if (lhs is () && rhs is ()) {
        result = trap (lhs <= rhs); // () <= ()
    } else if (lhs is boolean && rhs is boolean) {
        result = trap (lhs <= rhs); // boolean <= boolean
    } else if (lhs is int && rhs is int) {
        result = trap (lhs <= rhs); // int <= int
    } else if (lhs is float && rhs is float) {
        result = trap (lhs <= rhs); // float <= float
    } else if (lhs is decimal && rhs is decimal) {
        result = trap (lhs <= rhs); // decimal <= decimal
    } else if (lhs is string && rhs is string) {
        result = trap (lhs <= rhs); // string <= string
    } else {
        result = error("operator '<=' not defined for '" + check getType(lhs) + "' and '" + check getType(rhs) + "'.");
    }
    return result;
}

function greaterThan(any lhs, any rhs) returns boolean|error {
    boolean|error result = lessThanOrEquals(lhs, rhs);
    return result is boolean ? (!result) : result;
}

function greaterThanOrEquals(any lhs, any rhs) returns boolean|error {
    boolean|error result = lessThan(lhs, rhs);
    return result is boolean ? (!result) : result;
}
