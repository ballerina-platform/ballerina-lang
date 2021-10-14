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

function testMappingBindingPatternNegative1() returns string {
    boolean v1 = true;
    match v1 {
        var {a: b} => { // pattern will not be matched
            return "Match";
        }
    }

    record{| int a; |} v2 = { a : 2 };
    match v2 {
        var {b: a}  => { // pattern will not be matched
            return "Match";
        }
    }

    return "No match";
}

function testSameMatchPatternsNegative1() {
    map<int|string> v = { l : "1", m : "2", x : 1, y : 2 };
    match v {
        var {x: a, y: b} | var {x: a, y: b} => { // unreachable pattern
        }
        var {l: a, m: b} => {
        }
        var {l: a, m: b} => { // unreachable pattern
        }
        var {x: a, y: b} if a is int => {
        }
        var {x: a, y: b} if a is int => { // unreachable pattern
        }
    }
}

function testSameMatchPatternsNegative2(any v) {
    match v {
        var {x: a, ...b} => {}
        var {x: c, ...d} => {} // unreachable pattern
    }
}

function testSameMatchPatternsNegative3(any v) {
    match v {
        var {x: a, ...b} => {}
        var {x: a, y: b, ...c} => {} // unreachable pattern
    }
}

type Person record {|
    int id;
    string name;
    boolean employed;
|};

function testUnmatchedPatternAgainstClosedRecord(Person person) {
    match person {
        var {x, ...rest} => {
        }
        var {id, name, ...rest} => {
        }
        var {id, name} => {
        }
    }
}

type Record record {
    int|float a;
};

function func() {
    Record r1 = {a: 200};
    match r1 {
        {a: 150, b: "A"} => {}
        {a: 150, b: "A", c: false} => {} // unreachable pattern
    }
}

enum Department {
    FINANCE,
    LEGAL,
    ENGINEERING
}

type Employee record {|
    string name;
    Department department;
|};

public function testWarningForNonMatchingPattern() {
    Employee & readonly employee = {name: "Jo", department: LEGAL};

    match employee {
        {department: FINANCE} => {

        }
        {deparment: LEGAL} if !update(employee) => {

        }
    }
}

isolated function update(Employee & readonly employee) returns boolean => true;
