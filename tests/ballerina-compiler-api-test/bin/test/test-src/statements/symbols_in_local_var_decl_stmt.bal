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

function testLocalVarDeclStmt() {
    final int integer;

    string str = "SValue";

    @annot
    final Person person = {name: "Naruto", age: 15};

    @annot
    final var foo = func1();

    [int, string, int...] [intVar, stringVar, ...otherVal] = [5, "myString", 3, 1];

    [[string, int], float] [[a1, a2], a3] = [["text", 4], 89.9];

    record {string name; int age;} {name: valName, valAge} = person;

    Dept dept = {val: 11};

    map<int> {val: jam} = dept;
}

function func1() returns int {
    return 10;
}

type Dept record {|
    int val;
|};

type Person record {|
    string name;
    int age;
|};

annotation annot;
