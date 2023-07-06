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

const string CONST1 = "str";

function testMappingMatchPatternNegative() returns string {

    boolean v1 = true;
    match v1 {
        {a: true} => { // pattern will not be matched
            return "Match";
        }
    }

    map<int> v2 = {a: 2};
    match v2 {
        {a: "2"} => { // pattern will not be matched
            return "Match";
        }
        {x: 2, y: "2"} | {x: 3, y: "3"} => { // pattern will not be matched
            return "Match";
        }
        {c1: CONST1, c2: 2} => { // pattern will not be matched
            return "Match";
        }
    }

    map<int|string> v3 = {a: 2, b: "2"};
    match v3 {
        {a: 2 , b: true} => { // pattern will not be matched
            return "Match";
        }
        {a: CONST1 , b: true} => { // pattern will not be matched
            return "Match";
        }
    }

    return "No match";

}

const X = 2;
const Y = 4;
function testSameMatchPatternsNegative1() {
    map<int> v = {x: 1, y: 2};
    match v {
        {x: var a, y: 2} | {x: var a, y: 2} => { // unreachable pattern
        }
        {x: var a, y: 3} => {
        }
        {x: var a, y: 3} => { // unreachable pattern
        }
        {x: 31, y: 21} | {x: 31, y: 21} => { // unreachable pattern
        }
        {x: 1, y: X} | {x: 1, y: X} => { // unreachable pattern
        }
        {x : 11, y: Y} => {
        }
        {x: 11, y: Y} => { // unreachable pattern
        }
        {x: var a, y: 21} if a is int => {
        }
        {x: var a, y: 21} if a is int => { // unreachable pattern
        }
    }
}

function testSameMatchPatternsNegative2(any v) {
    match v {
        {x: var a, y: var b} => {}
        {x: var c, y: var d} => {} // unreachable pattern
    }
}

function testSameMatchPatternsNegative3(any v) {
    match v {
        {x: var a, ...var b} => {}
        {x: var c, ...var d} => {} // unreachable pattern
    }
}

function testSameMatchPatternsNegative4(any v) {
    match v {
        {x: var a, ...var b} => {}
        {x: var a, y: var b, ...var c} => {} // unreachable pattern
    }
}

type Person record {|
    int id;
    string name;
    boolean employed;
|};

function testUnmatchedPatternAgainstClosedRecord(Person person) {
    match person {
        {x: var a, ...var rest} => {
        }
    }
}

function testReachabilityAfterUnmatchedPattern(Person person) {
    match person {
        var {x, ...rest} => { // pattern will not be matched
        }
        var {id, name, ...rest} => {
        }
    }
}
