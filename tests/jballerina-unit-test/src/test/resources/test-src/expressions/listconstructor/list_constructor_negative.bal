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

function testIncompatibleListConstructorExprs1() {
    var [v1, v2, v3] = ["hello", 123, 34.56];
}

function testIncompatibleListConstructorExprs2() {
    [NoFillerObject, int] a = [];
    [[int, int], [NoFillerObject, NoFillerObject]] b = [];
}

type NoFillerObject object {
};

function testInvalidAssignmentForInferredTuple() {
    var tup = [
        {id: 123, name: "Anne", city: "Colombo"},
        {id: 456, name: "Jo", age: 40},
        1,
        "abc"
    ];

    [record {|
         int id;
         string name;
         int city;
    |},
    record {}, boolean, string] tup2 = tup;

    record {|
         int id;
         string name;
         string age;
    |} v1 = tup[1];
    float v2 = tup[2];
}

function testInferringForReadOnlyNonReadOnlyMemberNegative() {
    int[] arr = [1, 2];

    future<()> ft = start testInferringForReadOnlyNonReadOnlyMemberNegative();

    readonly rd = [1, arr, ft];
}

function testInferringForReadOnlyNegativeInUnion() {
    map<boolean|int> mp = {
        i: 1,
        b: true
    };

    readonly|int[] rd = [1, mp];

    boolean[] & readonly arr = [];

    boolean[][]|readonly br = [arr];
}

function testListConstrWithIssuesInCET() {
    Foo f = ["hello", <string> 1]; // Unknown type.
    string[]|Foo g = [<string> 1, "hello"]; // Unknown type in union.
}
