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

type ClosedAge record {
    int age;
    string format;
    !...;
};

type Person record {
    string name;
    boolean married;
    Age age;
    (string, int) extra?;
    !...;
};

type Person2 record {
    string name;
    boolean married;
    ClosedAge age;
    (string, int) extra?;
    !...;
};

function testUndefinedSymbol() {
    // undefined symbols. age is not a closed record
    {name: fName, married, age: { age: theAge, format, !...}, ...theMap} = getPerson1();
}

function getPerson1() returns Person {
    Age a = {age:12, format: "Y", three: "three"};
    return {name: "Peter", married: true, age: a, extra: ("extra", 12)};
}

function testClosedRecordVarRef() {
    string fName;
    boolean married;
    int theAge;
    string format;
    string extraLetter;
    int extraInt;
    map<any> theMap;

    Age age1 = {age:12, format: "Y", three: "three"};
    Person p1 = {name: "Peter", married: true, age: age1, extra: ("extra", 12)};
    {name: fName, married, age: { age: theAge, format, !...}, ...theMap} = p1;  // Age is not a closed record

    ClosedAge age2 = {age:12, format: "Y"};
    Person2 p2 = {name: "Peter", married: true, age: age2, extra: ("extra", 12)};
    {name: fName, married, age: { age: theAge, format, !...}, ...theMap} = p2;  // valid

    Person p5 = {name: "Peter", married: true, age: {age:12, format: "Y"}, extra: ("extra", 12)};
    {name: fName, married, age: { age: theAge, format}, !...} = p5; // not enough fields to match to closed record type 'Person'

    Person2 p6 = {name: "Peter", married: true, age: {age:12, format: "Y"}, extra: ("extra", 12)};
    {name: fName, married, age: { age: theAge, format, !...}} = p6; // valid

    Person p7 = {name: "Peter", married: true, age: {age:12, format: "Y", three: "three"}, extra: ("extra", 12)};
    {name: fName, married, age: { age: theAge, format}} = p7; // valid
}

type Foo record {
    string var1;
    Bar var2;
};

type Bar record {
    int var1;
    (string, int, boolean) var2;
};

function testInvalidTypes() {

    Bar fooVar1 = {var1: 0, var2: ("", 0, false)};
    string fooVar2 = "";

    Foo f = {var1: "var1String", var2: {var1: 12, var2: ("barString", 14, true)}};
    {var1: fooVar1, var2: fooVar2};
    {var1: fooVar1, var2: fooVar2} = f;
    {var1: fooVar1, var2: fooVar2} = 12;
    {var1: fooVar1, var2: fooVar2} = {var1: "var1String", var2: {var1: 12, var2: ("barString", 14, true)}};

    string fName;
    string lName;
    boolean married;
    Person age;
    map<any> theMap;

    Person p = {name: "Peter", married: true, age: {age: 12, format: "Y"}};
    {name: fName, age, married, ...theMap} = p; // incompatible types of age field

    {name: fName, name: lName} = p; // multiple matching patterns
}

function testUnknownFields() {
    Person p = {name: "Peter", married: true, age: {age: 12, format: "Y"}};
    any name;
    any married;
    any age;
    any format;
    any unknown1;
    any unknown2;
    {name, married, age: {age, format, unknown1}, unknown2} = p; // unknown fields
}

type UserData1 record {
    *Data;
};

type UserData2 record {
    int index;
    *Data;
};

type Object object {
    private int field;

    public function __init() {
        self.field = 12;
    }

    public function getField() returns int {
        return self.field;
    }
};

type IntRestRecord record {
    string name;
    boolean married;
    int...;
};

type ObjectRestRecord record {
    string name;
    boolean married;
    Object...;
};

function testRestParameterType() {
    string name;
    map<int> other1 = {};
    map<anydata> other2 = {};

    IntRestRecord rec1 = { name: "A", married: true, age: 19, token: 200 };
    { name, ...other1 } = rec1; // incompatible types: expected 'map<int>', found 'map<anydata>'

    ObjectRestRecord rec2 = { name: "A", married: true, extra: new };
    { name, ...other2 } = rec2; // incompatible types: expected 'map<anydata>', found 'map'
}
