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

type Employee record {
    readonly int id;
    string name;
    float salary;
};

function testTableGeneration() returns int {
   table<Employee> tbEmployee = table key(id) [
               {id: 1, name: "Mary🤒", salary: 300.5},
               {id: 2, name: "John💉", salary: 200.5},
               {id: 3, name: "Jim", salary: 330.5}
    ];

    return tbEmployee.toString().length();
}

type Names record {|
    readonly string country;
    string[] names;
|};

function testTableWithArrayGeneration() returns int {
    string[] names = ["Sam🚜", "John🕔", "Ann"];

   table<Names> tbNames = table key(country)[
                {country:"Ireland🔀", names: names}
    ];

    return tbNames.toString().length();
}



type AssertionError error<ASSERTION_ERROR_REASON>;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
