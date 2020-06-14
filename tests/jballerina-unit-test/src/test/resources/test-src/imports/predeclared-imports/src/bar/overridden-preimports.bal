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

function testMax(int n, int m) returns int {
    return 'error:max(n, m);
}

function testMin(int n, int m) returns int {
    return 'error:min(n, m);
}

function testSum(decimal p1, decimal p2) returns decimal {
    return 'xml:sum(p1, p2);
}

function testOneArgMax(decimal arg) returns decimal {
    return 'xml:max(arg);
}

function testStartsWith() returns boolean {
    return 'object:startsWith(" Chiran Sachintha ", "Hello");
}

function testStringConcat() returns string {
    return 'object:concat("Hello ", "from ", "Ballerina");
}
