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

type Employee record {
    string name;
    int id?;
    float salary?;
};

type Person record {
    string name;
    float id;
    float salary?;
};

type Foo record {
    int a;
    boolean b;
    float c?;
};

type Bar record {
    float a;
    boolean b;
    decimal c;
};

function testOptionalFieldAccessOnRequiredRecordField() returns boolean {
    string s = "Anne";
    Employee e = { name: s, id: 100 };
    string name = e?.name;
    return name == s;
}

function testOptionalFieldAccessOnRequiredRecordFieldInRecordUnion() returns boolean {
    Foo f = { a: 1, b: true };
    Foo|Bar fb = f;

    int|float a = fb?.a;
    boolean b = fb?.b;

    return a == 1 && b;
}

function testOptionalFieldAccessOnRequiredRecordFieldInNillableUnion() returns boolean {
    Foo f = { a: 1, b: true };
    Foo|Bar? fb = f;

    int|float? a = fb?.a;
    boolean? b = fb?.b;

    boolean assertion = a == 1 && b == true;

    fb = ();
    a = fb?.a;
    b = fb?.b;

    return assertion && a is () && b is ();
}

function testOptionalFieldAccessOnOptionalRecordField1() returns boolean {
    int i = 1100;
    Employee e1 = { name: "John", id: i };
    int? id1 = e1?.id;

    Employee e2 = { name: "John" };
    int? id2 = e2?.id;

    return id1 == i && id2 is ();
}

function testOptionalFieldAccessOnOptionalRecordField2() returns boolean {
    float f = 110.0;
    Employee e1 = { name: "John", id: 100, salary: f };
    float? salary1 = e1?.salary;

    Employee e2 = { name: "John" };
    float? salary2 = e2?.salary;

    return salary1 == f && salary2 is ();
}

function testOptionalFieldAccessOnOptionalRecordFieldInRecordUnion1() returns boolean {
    float f = 110.0;
    Person p = { name: "John", id: f };
    Employee|Person ep = p;

    int|float? id = ep?.id;
    float? salary = ep?.salary;

    return id == f && salary is ();
}

function testOptionalFieldAccessOnOptionalRecordFieldInRecordUnion2() returns boolean {
    Employee e = { name: "Anne" };
    Employee|Person ep = e;

    int|float? id = ep?.id;
    float? salary = ep?.salary;

    return id is () && salary is ();
}

function testOptionalFieldAccessOnOptionalRecordFieldInNillableRecordUnion1() returns boolean {
    string s = "Bob";
    float f = 110.0;

    Person p = { name: s, id: f };
    Employee|Person? ep = p;

    string? name = ep?.name;
    int|float? id = ep?.id;

    return id == f && name == s;
}

function testOptionalFieldAccessOnOptionalRecordFieldInNillableRecordUnion2() returns boolean {
    Employee? ep = ();

    string? name = ep?.name;
    int? id = ep?.id;

    return id is () && name is ();
}
