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

import testorg/foo.records;

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

type Foo1 record {|
    int a;
    float b;
    *ValType;
    string s;
    *ClosedValType;
|};

function testValRefType() returns Foo1 {
    Foo1 f = {a:10, b:23.45, s:"hello foo", ri:20, crs:"qwerty", rf:45.6, rs:"asdf", cri:20, crf:12.34};
    f.rb = true;
    f.ry = 255;
    f.crb = true;
    f.cry = 254;
    return f;
}

// TESTS FOR RECORDS WHERE THE REFERENCED TYPE HAS COMPLEX REF TYPE FIELDS

class Person {
    string name;

    function init(string name) {
        self.name = name;
    }
}

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

type Foo2 record {|
    string s;
    *RefType;
    int i;
    *ClosedRefType;
|};

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

type Foo3 record {|
    *OrderTest;
    string s;
|};

type OrderTest record {|
    int ri;
    string rs;
|};

function testOrdering() returns Foo3 {
    Foo3 f = {s:"qwerty", ri:10, rs:"asdf"};
    return f;
}

type AB record {
    int abi;
};

type CD record {|
    *EF;
    AB cdr;
|};

type EF record {|
    *AB;
    string efs;
|};

type Foo4 record {|
    string s;
    *CD;
|};

function testReferenceChains() returns Foo4 {
    AB ab = {abi:123};
    Foo4 f = {s:"qwerty", abi:10, efs:"asdf", cdr:ab};
    return f;
}

function testTypeReferencingInBALAs() returns records:BClosedManager {
    records:BClosedManager m = {name:"John Doe", age:25, adr:{city:"Colombo", country:"Sri Lanka"}, company:"WSO2", dept:"Engineering"};
    return m;
}

// TEST DEFAULT VALUE INIT IN TYPE REFERENCED FIELDS

type PersonRec record {|
    string name = "John Doe";
    int age = 25;
    Address adr = {city: "Colombo", country: "Sri Lanka"};
|};

type EmployeeRec record {|
    *PersonRec;
    string company = "WSO2";
|};

type ManagerRec record {|
    string dept = "";
    *EmployeeRec;
|};

function testDefaultValueInit() returns ManagerRec {
    ManagerRec mgr = {};
    return mgr;
}

function testDefaultValueInitInBALAs() returns records:BClosedManager {
    records:BClosedManager mgr = {};
    return mgr;
}

// Test overriding rest descriptor.

type Rec1 record {|
    int i;
    string...;
|};

type Rec2 record {|
    string s;
    any|error...;
|};

type IncludingRec1 record {|
    boolean b;
    *Rec1;
|};

type IncludingRec2 record {|
    *Rec1;
    boolean...;
|};

type IncludingRec3 record {|
    int i;
    *Rec2;
|};

type Rec3 record {
    int i;
};

type Rec4 record {
    int j;
};

type IncludingRec4 record {|
    int k;
    *Rec3;
    *Rec4;
|};

type Rec5 record {|
    int i;
    string...;
|};

type Rec6 record {|
    int j;
    string...;
|};

type IncludingRec5 record {|
    int k;
    *Rec5;
    *Rec6;
|};

function testRestTypeOverriding() {
    IncludingRec1 r1 = {b: false, i: 1, "s": "str"};
    assertEquality("str", r1["s"]);
    IncludingRec2 r2 = {i: 1, "b": false};
    assertEquality(false, r2["b"]);
    IncludingRec3 r3 = {i: 1, s: "str", "e": error("Message")};
    assertEquality(true, r3["e"] is error);
    error e = <error> r3["e"];
    assertEquality("Message", e.message());
    IncludingRec4 r4 = {i: 1, j: 2, k: 3, "s": "str"};
    assertEquality("str", r4["s"]);
    IncludingRec4 r5 = {i: 1, j: 2, k: 3, "s": "str", "b": false};
    assertEquality(false, r5["b"]);
    IncludingRec5 r6 = {i: 1, j: 2, k: 3, "s1": "str1", "s2": "str2"};
    assertEquality("str2", r6["s2"]);
}

 public type Foo record {|
     anydata body;
 |};

 // Out of order inclusion test : added to test a NPE
 type Bar record {|
     *Foo;
     Baz2 body;   // defined after the type definition
 |};

 type Baz2 record {|
     int id;
 |};

 function testOutOfOrderFieldOverridingFieldFromTypeInclusion() {
     Baz2 bazRecord = {id: 4};
     Bar barRecord = {body: bazRecord};
     assertEquality(4, barRecord.body.id);
 }

type UnaryExprOp "-"|"~"|"!";

type UnaryExpr record {|
    UnaryExprOp op;
|};

type SimpleConstNegateExpr record {|
    *UnaryExpr;
    "-" op = "-";
|};

function testTypeInclusionWithFiniteField() {
    SimpleConstNegateExpr expr = {};
    assertEquality(true, expr is UnaryExpr);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
