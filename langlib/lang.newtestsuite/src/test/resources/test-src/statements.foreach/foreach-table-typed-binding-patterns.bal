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

string output = "";

type Employee record {
    readonly int id;
    string name;
    float salary;
};

table<Employee> data = table key(id)[
        { id: 1, name: "Mary", salary: 300.5 },
        { id: 2, name: "John", salary: 200.5 },
        { id: 3, name: "Jim", salary: 330.5 }
    ];

function concatIntAny(int i, any a) {
    output = output + i.toString() + ":" + a.toString() + " ";
}

function concatIntIntStringFloat(int i1, int i2, string s, float f) {
    output = output + i1.toString() + ":" + i2.toString() + ":" + s + ":" + f.toString() + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testTableWithoutType() returns string {
    output = "";

    int i = 0;
    foreach var v in data {
        concatIntAny(i, v);
        i += 1;
    }
    return output;
}

function testTableWithType() returns string {
    output = "";

    int i = 0;
    foreach Employee v in data {
        concatIntAny(i, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testRecordInTableWithoutType() returns string {
    output = "";

    int i = 0;
    foreach var {id, name, salary} in data {
    concatIntIntStringFloat(i, id, name, salary);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testEmptyTableIteration() returns string {
    output = "";

    table<Employee> d = table key(id) [];

    int i = 0;
    foreach var {id, name, salary} in d {
    concatIntIntStringFloat(i, id, name, salary);
        i += 1;
    }
    return output;
}

function testIterationOverKeylessTable() returns boolean {
    table<Employee> data = table [
            { id: 1, name: "Mary", salary: 300.5 },
            { id: 2, name: "John", salary: 200.5 },
            { id: 3, name: "Jim", salary: 330.5 }
        ];
    Employee[] ar = [];
    foreach var v in data {
        ar.push(v);
    }
   return ar.length() == 3 && ar[0].name == "Mary" && ar[1].name == "John" && ar[2].name == "Jim";
}
