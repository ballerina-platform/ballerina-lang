// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Age record {
    int age;
    string format;
};

type Person record {
    string name;
    boolean married;
    Age age;
    (string, int) extra;
    !...
};

function testVariableAssignment() returns (string, boolean, int, string) {
    string fName;
    boolean married;
    int theAge;
    string format;
    map theMap;

    {name: fName, married, age: {age: theAge, format}, ...theMap} = getPerson();
    return (fName, married, theAge, format);
}

function getPerson() returns Person {
    return {name: "Peter", married: true, age: {age:12, format: "Y"}};
}

type Foo record {
    map<int> var1;
    Bar var2;
};

type Bar record {
    int|float var1;
    Some var2;
};

type Some record {
    string var1;
    string? var2;
};

function testRecVarRefInsideRecVarRefInsideRecVarRef() returns (map<int>, int|float, string, string?) {
    map<int> fooVar1;
    int|float barVar1;
    string someVar1;
    string? someVar2;

    {var1: fooVar1, var2: {var1: barVar1, var2: {var1: someVar1, var2: someVar2}}} = getFoo();
    return (fooVar1, barVar1, someVar1, someVar2);
}

function getFoo() returns Foo {
    return {var1: {"mKey1": 1, "mKey2": 2}, var2: {var1: 12, var2: {var1: "SomeVar1", var2: ()}}};
}

type OpenRecord record {
    string var1;
    boolean var2;
};

function testRestParam() returns map {
    OpenRecord openRecord = {var1: "var1", var2: false, var3: 12, var4: "text"};
    string var1;
    boolean var2;
    map rest;
    {var1, var2, ...rest} = openRecord;
    return rest;
}

function testRecordTypeInRecordVarRef() returns (map, int|float, Some) {
    map<int> fooVar1;
    int|float barVar1;
    Some var2;

    {var1: fooVar1, var2: {var1: barVar1, var2}} = getFoo();
    return (fooVar1, barVar1, var2);
}

// TODO: Uncomment below tests once record literal is supported with var ref

//function testVarAssignmentOfRecordLiteral() returns (string, boolean, int, string) {
//    string fName;
//    boolean married;
//    int theAge;
//    string format;
//    map theMap;
//
//    {name: fName, age: {age: theAge, format}, married, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y"}};
//    return (fName, married, theAge, format);
//}
//
//function testVarAssignmentOfRecordLiteral2() returns (string, boolean, Age) {
//    string fName;
//    boolean married;
//    Age age;
//    map theMap;
//
//    {name: fName, age, married, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y"}};
//    return (fName, married, age);
//}
//
//function testVarAssignmentOfRecordLiteral3() returns (string, boolean, map) {
//    string fName;
//    boolean married;
//    map age;
//    map theMap;
//
//    {name: fName, age, married, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y"}};
//    return (fName, married, age);
//}
//
//function testVarAssignmentOfRecordLiteral4() returns (string, boolean, json) {
//    string fName;
//    boolean married;
//    json age;
//    map theMap;
//
//    {name: fName, age, married, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y"}};
//    return (fName, married, age);
//}
