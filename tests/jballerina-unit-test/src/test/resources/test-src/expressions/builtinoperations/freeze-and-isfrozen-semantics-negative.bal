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

function testFreezeOnValuesOfNonAnydataType() {
    PersonObj p = new;
    PersonObj q = p.cloneReadOnly();
}

function testFreezeOnMapWithoutAnydata() {
    map<PersonObj> m1 = {};
    _ = m1.cloneReadOnly();
}

function testFreezeOnArrayWithoutAnydata() {
    PersonObj[] a1 = [];
    _ = a1.cloneReadOnly();

    (PersonObjTwo|PersonObj)?[] a2 = [];
    _ =  a2.cloneReadOnly();
}

function testFreezeOnTupleWithoutAnydata() {
    PersonObj po = new;
    PersonObjTwo po2 = new;
    [PersonObj|PersonObjTwo, PersonObjTwo] t1 = [po, po2];
    _ = t1.cloneReadOnly();
}

function testFreezeOnRecordWithoutAnydata() {
    Department d1 = { head: new };
    _ = d1.cloneReadOnly();
}

function testInvalidAssignmentWithFreeze() {
    map<string|PersonObj> m = {};
    map<string|PersonObj> m1 = m.cloneReadOnly();

    map<[string|PersonObj, FreezeAllowedDepartment|float]> m2 = {};
    map<[any, any]> m3 = m2.cloneReadOnly();

    (boolean|PersonObj|float)?[] a1 = [];
    (boolean|PersonObj|float)?[] a2 = a1.cloneReadOnly();

    any[] a3 = a1.cloneReadOnly();

    [string|PersonObj, FreezeAllowedDepartment|float] t1 = ["", 0.0];
    [string|PersonObj, FreezeAllowedDepartment|float] t2 = t1.cloneReadOnly();

    FreezeAllowedDepartment fd = { head: "" };
    FreezeAllowedDepartment fd2 = fd.cloneReadOnly();

    string|PersonObj u1 = "hi";
    string|PersonObj u2 = u1.cloneReadOnly();
}

function testFreezeOnError() {
    error e = error("test error");
    _ = e.cloneReadOnly();
}

function testInvalidComplexMapFreeze() {
    map<string|PersonObj> m1 = {};
    PersonObj p = new;

    m1["one"] = "one";
    m1["two"] = p;

    map<string|PersonObj>|error res = m1.cloneReadOnly();
}

function testInvalidComplexArrayFreeze()  {
    (string|typedesc<anydata>|float)?[] a1 = [];
    typedesc<anydata> p = int;

    a1[0] = 2.0;
    a1[1] = "hello world";
    a1[2] = p;

    (string|typedesc<anydata>|float)?[]|error res = a1.cloneReadOnly();
}

function testInvalidComplexRecordFreeze() {
    PersonObj p = new;
    PersonObj p1 = new;
    PersonObj p2 = new;
    FreezeAllowedDepartment2 fd = { head: p, "e1": p1, "e2": 10 };
    FreezeAllowedDepartment2|error res = fd.cloneReadOnly();
}

function testInvalidComplexTupleFreeze() {
    PersonObj p = new;
    [int, string|PersonObj|float, boolean] t1 = [1, p, true];
    any|error res = t1.cloneReadOnly();
}

function testInvalidComplexUnionFreeze() {
    PersonObj p = new;
    int|Department|PersonObj u1 = p;

    int|Department|PersonObj|error res = u1.cloneReadOnly();
}

function testErrorValueFreeze() {
    error e = error("test error");
    anydata|error val = e;

    anydata res = val.cloneReadOnly();
}

class PersonObj {
    string name = "";

    function getName() returns string {
        return self.name;
    }
}

class PersonObjTwo {
    string id = "";

    function getId() returns string {
        return self.id;
    }
}

type Department record {|
    PersonObj head;
    PersonObjTwo...;
|};

type FreezeAllowedDepartment record {|
    PersonObj|string head;
    (PersonObjTwo|string)...;
|};

type FreezeAllowedDepartment2 record {|
    PersonObj|string head;
    (PersonObj|int)...;
|};
