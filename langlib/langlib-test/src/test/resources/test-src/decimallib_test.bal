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

import ballerina/lang.'decimal as decimals;

function testSum(decimal p1, decimal p2) returns decimal {
    return decimals:sum(p1, p2);
}

function testOneArgMax(decimal arg) returns decimal {
    return decimals:max(arg);
}

function testMultiArgMax(decimal arg, decimal[] otherArgs) returns decimal {
    return decimals:max(arg, ...otherArgs);
}

function testOneArgMin(decimal arg) returns decimal {
    return decimals:min(arg);
}

function testMultiArgMin(decimal arg, decimal[] otherArgs) returns decimal {
    return decimals:min(arg, ...otherArgs);
}

function testAbs(decimal arg) returns decimal {
    return decimals:abs(arg);
}

function testRound(decimal arg) returns decimal {
    return decimals:round(arg);
}

function testFloor(decimal arg) returns decimal {
    return decimals:floor(arg);
}

function testCeiling(decimal arg) returns decimal {
    return decimals:ceiling(arg);
}

function testFromString(string arg) returns decimal|error {
    return decimals:fromString(arg);
}

function testMaxAsMethodInvok(decimal x, decimal...xs) returns decimal {
    return x.max(...xs);
}

function testMinAsMethodInvok(decimal x, decimal...xs) returns decimal {
    return x.min(...xs);
}

function testAbsAsMethodInvok(decimal x) returns decimal {
    return x.abs();
}

function testRoundAsMethodInvok(decimal x) returns decimal {
    return x.round();
}

function testFloorAsMethodInvok(decimal x) returns decimal {
    return x.floor();
}

function testCeilingAsMethodInvok(decimal x) returns decimal {
    return x.ceiling();
}

function value() returns decimal|error {
    return 'decimal:fromString("x");
}

function testFromStringWithStringArg() {
    decimal|error res = value();
    assertEquality(true, res is error);

    error resError = <error> res;
    assertEquality("'string' value 'x' cannot be converted to 'decimal'", resError.detail().get("message"));
}

type Decimals 12d|21d;

function testLangLibCallOnFiniteType() {
    Decimals x = 12;
    decimal y = x.sum(1, 2, 3);
    assertEquality(18d, y);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
