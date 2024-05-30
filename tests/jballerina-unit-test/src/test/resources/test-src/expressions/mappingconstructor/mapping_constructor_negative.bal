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

type Person record {|
    string name;
|};

type PersonTwo record {|
    string name;
    int id;
|};

type PersonThree record {|
    string name;
    int id;
    float...;
|};

function testUnionsWithTypesSupportingMappingConstructors() {
    string|Person x = {name: "John", age: 25};
}

function testMappingConstrWithLiteralKeysForUnionCET() {
    PersonTwo|PersonThree x = {name: "John", "id": 25};
}

function testUnionsOfTypesWithoutSupportForMappingConstructors() {
    int|float x = {name: "John", age: 25};
}

function testAmbiguousMapTarget() {
    map<int>|map<string> m1 = {};

    map<int|string>|map<string|boolean> m2 = {
        a: "hello",
        b: "bye"
    };
}

function testFieldTypeCheckingOnUnknownType() {
    PersonThree p3 = {name: "Anne", id: 123, "salary": 100.0};
    NoRecord x = {a: <string> p3.id, b: p3.salary, ...c};
}

function testMappingConstrWithIssuesInCET() {
    Foo f = {a: <boolean> "hello", b: 1}; // Unknown type.
    Foo|map<boolean> g = {a: <boolean> 1, c: true}; // Unknown type in union.
}

function testAmbiguousTypeWithAny() {
    any|map<any> _ = {a: 1};
}

function testMappingConstructorWithUndefinedVars() {
    map<string>? _ = {[NAME] : "Amy"};
    map<string>? _ = {[NAME] : NAME};
    record {|string name;|}? _ = {[NAME] : NAME};
}
