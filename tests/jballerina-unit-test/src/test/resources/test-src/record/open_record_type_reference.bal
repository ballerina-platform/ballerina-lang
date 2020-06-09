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

import testorg/records;

// TESTS FOR RECORDS WHERE THE REFERENCED TYPE ONLY HAS VALUE TYPE FIELDS

type ValType record {
    int ri;
    float rf;
    string rs;
    boolean rb?;
    byte ry?;
};

type ClosedValType record {|
    int cri;
    float crf;
    string crs;
    boolean crb?;
    byte cry?;
|};

type Foo1 record {
    int a;
    float b;
    *ValType;
    string s;
    *ClosedValType;
};

function testValRefType() returns Foo1 {
    Foo1 f = {a:10, b:23.45, s:"hello foo", ri:20, crs:"qwerty", rf:45.6, rs:"asdf", cri:20, crf:12.34};
    f.rb = true;
    f.ry = 255;
    f.crb = true;
    f.cry = 254;
    return f;
}

// TESTS FOR RECORDS WHERE THE REFERENCED TYPE HAS COMPLEX REF TYPE FIELDS

type Person object {
    string name;

    function __init(string name) {
        self.name = name;
    }
};

type Employee record {|
    int id;
    string name;
    float salary;
|};

type Address record {
    string city;
    string country;
};

type RefType record {
    json rj?;
    xml rx;
    Person rp;
    Address ra?;
};

type ClosedRefType record {|
    json crj?;
    xml crx;
    Person crp;
    Address cra?;
|};

type Foo2 record {
    string s;
    *RefType;
    int i;
    *ClosedRefType;
};

function testRefTypes() returns Foo2 {
    Foo2 f = {s:"qwerty", i:10, rx:xml `<book>Count of Monte Cristo</book>`, rp:new("John Doe"), crp:new("Jane Doe"), crx:xml `<book>Count of Monte Cristo</book>`};
    json j = {name: "apple", color: "red", price: 40};
    Address adr = {city:"Colombo", country:"Sri Lanka"};

    f.rj = j;
    f.ra = adr;
    f.crj = j;
    f.cra = adr;

    return f;
}

// Testing the order of resolving

type Foo3 record {
    *OrderTest;
    string s;
};

type OrderTest record {
    int ri;
    string rs;
};

function testOrdering() returns Foo3 {
    Foo3 f = {s:"qwerty", ri:10, rs:"asdf"};
    return f;
}

type AB record {
    int abi;
};

type CD record {
    *EF;
    AB cdr;
};

type EF record {
    *AB;
    string efs;
};

type Foo4 record {
    string s;
    *CD;
};

function testReferenceChains() returns Foo4 {
    AB ab = {abi:123};
    Foo4 f = {s:"qwerty", abi:10, efs:"asdf", cdr:ab};
    return f;
}

function testTypeReferencingInBALOs() returns records:BManager {
    records:BManager m = {name:"John Doe", age:25, adr:{city:"Colombo", country:"Sri Lanka"},
                          company:"WSO2", dept:"Engineering"};
    return m;
}

// TEST DEFAULT VALUE INIT IN TYPE REFERENCED FIELDS

type PersonRec record {
    string name = "John Doe";
    int age = 25;
    Address adr = {city: "Colombo", country: "Sri Lanka"};
};

type EmployeeRec record {
    *PersonRec;
    string company = "WSO2";
};

type ManagerRec record {
    string dept = "";
    *EmployeeRec;
};

function testDefaultValueInit() returns ManagerRec {
    ManagerRec mgr = {};
    return mgr;
}

function testDefaultValueInitInBALOs() returns records:BManager {
    records:BManager mgr = {};
    return mgr;
}

// Test overriding of referenced fields

type DefaultPerson record {
    *records:AgedPerson;
    int|string age = 18;
    string name = "UNKNOWN";
};

function testCreatingRecordWithOverriddenFields() {
    DefaultPerson dummyPerson = {};
    assertEquality(18, dummyPerson.age);
    dummyPerson.age = 400;
    assertEquality(400, dummyPerson.age);
    assertEquality("UNKNOWN", dummyPerson.name);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
