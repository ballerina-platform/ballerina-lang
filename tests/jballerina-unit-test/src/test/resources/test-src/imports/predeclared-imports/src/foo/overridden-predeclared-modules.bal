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

import ballerina/lang.'boolean as booleans;
import ballerina/lang.'int as ints;
import ballerina/lang.'decimal as decimals;
import ballerina/lang.'float as floats;
import ballerina/lang.'string as strings;

function testSumFunctionInDecimal(decimal p1, decimal p2) returns decimal {
    return 'decimals:sum(p1, p2);
}

function testOneArgMaxFunctionInDecimal(decimal arg) returns decimal {
    return 'decimals:max(arg);
}

function testSumFunctionInFloat() returns float {
    return 'floats:sum(12.34, 23.45, 34.56);
}

function testFloatConsts() returns float {
    return 'floats:NaN;
}

function testSumFunctionInInt() returns int {
    return 'ints:sum(10, 25, 35, 40);
}

function testMaxFunctionInInt(int n, int... ns) returns int {
    return 'ints:max(n, ...ns);
}

function testFromString(string s, boolean|error expected) {
    assert(expected, 'booleans:fromString(s));
}

string str = "Hello Ballerina!";

function testSubString() returns [string,string, string] {
    return [str.substring(6, 9), str.substring(6), 'strings:substring(str,6)];
}

function testStartsWithFunctionInString() returns boolean {
    return 'strings:startsWith(str, "Hello");
}

function testOverriddenPredeclaredModules() returns [decimal, float, float, int, int, boolean]{
    return [testSumFunctionInDecimal(10.5, 11.5), testSumFunctionInFloat(), testFloatConsts(), 
    testSumFunctionInInt(), testMaxFunctionInInt(10, 15), testStartsWithFunctionInString()];
}

function assert(boolean|error expected, boolean|error actual) {
    if (expected is boolean && actual is boolean) {
        if (expected != actual) {
            string reason = "expected [" + expected.toString() + "] , but found [" + actual.toString() + "]";
            error e = error(reason);
            panic e;
        }

        return;
    }
    if (expected != actual) {
        typedesc<anydata|error> expT = typeof expected;
        typedesc<anydata|error> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}
