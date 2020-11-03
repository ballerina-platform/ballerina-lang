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

import ballerina/lang.'int as 'error;
import ballerina/lang.'decimal as 'xml;
import ballerina/lang.'string as 'object;
import ballerina/lang.'string as 'future;
import ballerina/lang.'int as 'map;
import ballerina/lang.'float as 'string;
import ballerina/lang.'float as 'boolean;
import ballerina/lang.'int as 'decimal;
import ballerina/lang.'decimal as 'int;
import ballerina/lang.'string as 'float;
import ballerina/lang.'float as 'stream;
import ballerina/lang.'int as 'table;
import ballerina/lang.'decimal as 'typedesc;

function testMax(int n, int m) returns int {
    return 'error:max(n, m);
}

function testMin(int n, int m) returns int {
    return 'map:min(n, m);
}

function testMax1(int n, int m) returns int {
    return 'decimal:max(n, m);
}

function testMin1(int n, int m) returns int {
    return 'table:min(n, m);
}

function testSum1(decimal p1, decimal p2) returns decimal {
    return 'xml:sum(p1, p2);
}

function testOneArgMax(decimal arg) returns decimal {
    return 'int:max(arg);
}

function testSum2(decimal p1, decimal p2) returns decimal {
    return 'typedesc:sum(p1, p2);
}

function testStartsWith() returns boolean {
    return 'object:startsWith(" Chiran Sachintha ", "Hello");
}

function testStringConcat() returns string {
    return 'future:concat("Hello ", "from ", "Ballerina");
}

function testStringConcat1() returns string {
    return 'float:concat("Hi ", "Chiran ", "Sachintha");
}

function testSumFunctionInFloat1() returns float {
    return 'string:sum(12.34, 23.45, 34.56);
}

function testFloatConsts1() returns float {
    return 'boolean:NaN;
}

function testMaxFunctionInFloat1() returns float {
    return 'stream:max(12.34, 34.56);
}
