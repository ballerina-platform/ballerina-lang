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

import ballerina/lang.'decimal as decimals;
import ballerina/lang.test as test;

function testSum(decimal p1, decimal p2) returns decimal {
    return decimals:sum(p1, p2);
}

function testOneArgMax(decimal arg) returns decimal {
    return decimals:max(arg);
}

function testMultiArgMax(decimal arg, decimal[] otherArgs) returns decimal {
    return decimals:max(arg, ...otherArgs);
}

function testMultiArgMaxDataProvider() {
    [decimal, decimal[], decimal][] a = [[0d, [0d], 0d], [0.0d, [0d], 0d], [0d, [1d], 1d], [-1d, [0d], 0d], [-0d, [0d], 0d], [5d, [0d, 2d, -2d], 5d], [-511111111111199999999999222222222.2222222d, [0d], 0d]];
    foreach [decimal, decimal[], decimal] element in a {
        test:assertEquals(testMultiArgMax(element[0], element[1]), element[2]);
    }
}

function testOneArgMin(decimal arg) returns decimal {
    return decimals:min(arg);
}


function testMultiArgMin(decimal arg, decimal[] otherArgs) returns decimal {
    return decimals:min(arg, ...otherArgs);
}

function testMultiArgMinDataProvider() {
    [decimal, decimal[], decimal][] a = [[0d, [0d], 0d], [0.0d, [0d], 0d], [0d, [1d], 0.0d], [-1d, [0d], -1d], [-0d, [0d], 0d], [5d, [0d, 2d, -2d], -2d], [-511111111111199999999999222222222.2222222d, [0d], -511111111111199999999999222222222.2222222d]];
    foreach [decimal, decimal[], decimal] element in a {
        test:assertEquals(testMultiArgMin(element[0], element[1]), element[2]);
    }
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

function testFromStringDataProvider() {
    string[] input = ["0", "0.0", "-0", "-1", "-0.0", "-100.1", "100.1", "5", "504023030303030303030.3030303", "-504023030303030303030.3030303"];
    decimal[] expected = [0d, 0.0d, 0d, -1d, 0.0d, -100.1d, 100.1d, 5d, 504023030303030303030.3030303d, -504023030303030303030.3030303d];
    foreach var i in 0...(input.length() - 1) {
        test:assertEquals(<decimal>testFromString(input[i]), expected[i]);
    }
}

function testMaxAsMethodInvok(decimal x, decimal...xs) returns decimal {
    return x.max(...xs);
}

function testMaxAsMethodInvokDataProvider(){
    [decimal, decimal[], decimal][] a = [[0d, [0d], 0d], [0.0d, [0d], 0d], [0d, [1d], 1d], [-1d, [0d], 0d], [-0d, [0d], 0d], [5d, [0d, 2d, -2d], 5d], [-511111111111199999999999222222222.2222222d, [0d], 0d]];
    foreach [decimal, decimal[], decimal] element in a {
        test:assertEquals(testMaxAsMethodInvok(element[0], ...element[1]), element[2]);
    }
}

function testMinAsMethodInvok(decimal x, decimal...xs) returns decimal {
    return x.min(...xs);
}

function testMinAsMethodInvokDataProvider() {
    [decimal, decimal[], decimal][] a = [[0d, [0d], 0d], [0.0d, [0d], 0d], [0d, [1d], 0.0d], [-1d, [0d], -1d], [-0d, [0d], 0d], [5d, [0d, 2d, -2d], -2d], [-511111111111199999999999222222222.2222222d, [0d], -511111111111199999999999222222222.2222222d]];
    foreach [decimal, decimal[], decimal] element in a {
        test:assertEquals(testMinAsMethodInvok(element[0], ...element[1]), element[2]);
    }
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
