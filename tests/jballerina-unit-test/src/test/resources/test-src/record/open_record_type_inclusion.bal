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

function testTypeReferencingInBALAs() returns records:BManager {
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

function testDefaultValueInitInBALAs() returns records:BManager {
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

public type Foo record {
    anydata body;
};

public type AnotherFoo record {
    anydata body;
};

// Out of order inclusion test : added to test a NPE
type Bar record {
    *Foo;
    *AnotherFoo;
    Baz body;   // defined after the type definition
};

type Baz record {
    int id;
};

type Student record {
    *records:BClosedStudent;
};

type Student1 record {
    string name = "chiranS";
    *records:BClosedStudent;
};

type Foo5 record {
    *records:BClosedStudent;
    *records:BClosedPerson;
    string name = "chiranS";
    int age = 25;
};

type Foo6 record {
    *records:BClosedStudent;
    *records:BClosedPerson;
    string name;
    int age;
};

type Info record {
    *records:Info;
};

type Info1 record {
    *records:Info1;
};

type Location record {|
    *records:Location;
|};

function testOutOfOrderFieldOverridingFieldFromTypeInclusion() {
    Baz bazRecord = {id: 4};
    Bar barRecord = {body: bazRecord};
    assertEquality(4, barRecord.body.id);
}

function testCyclicRecord() {
    records:C1 cc = {auth: {d1: {x: 34}}};
    assertEquality(34, cc?.auth?.d1?.x);
}

function testDefaultValuesOfRecordFieldsWithTypeInclusion() {
    Student student = {};
    assertEquality("anonymous", student.name);
    assertEquality(20, student.age);
    Student1 student1 = {};
    assertEquality("chiranS", student1.name);
    assertEquality(20, student1.age);
    Foo5 foo5 = {};
    assertEquality("chiranS", foo5.name);
    assertEquality(25, foo5.age);
    Foo6 foo6 = {name: "sachintha", age: 28};
    assertEquality("sachintha", foo6.name);
    assertEquality(28, foo6.age);
    Info info = {};
    assertEquality("John", info.name);
    assertEquality(30, info.age);
    Location location = {city: "Colombo", country: "Sri Lanka"};
    assertEquality("Colombo", location.city);
    assertEquality("Sri Lanka", location.country);
    assertEquality("abc", location.street);
    assertEquality(123, location.zipCode);
    Info1 info1 = {};
    assertEquality("James", info1.name);
    assertEquality(30, info1.age);
}

type Inner record {|
    int foo;
|};

type Outer record {|
    Inner inner?;
|};

isolated int count = 0;

isolated function getDefaultInner() returns Inner {
    lock {
        count += 1;
    }
    return {foo: 10};
}

type OuterXBase record {
    Inner inner = getDefaultInner();
};

type OuterX record {|
    *OuterXBase;
|};

type OuterXOpenRecord record {|
    *OuterXBase;
    Inner...;
|};

type InnerOpenRec record {|
    Inner...;
|};

type OuterXAlsoClosed record {|
    *OuterXBase;
    never...;
|};

type EffectivelyCloseRecord record {|
    *OuterXBase;
    record {|
        never bar;
    |}...;
|};

isolated function testDefaultValueFromInclusion() {
    Outer o = {};
    OuterX ox = {...o};
    assertEquality(ox.inner.foo, 10);
    lock {
        assertEquality(1, count);
    }
    Outer o1 = {inner: {foo: 20}};
    OuterX ox1 = {...o1};
    assertEquality(20, ox1.inner.foo);
    lock {
        assertEquality(1, count);
    }
    map<Inner> innerMap = {};
    OuterX ox2 = {...innerMap};
    assertEquality(10, ox2.inner.foo);
    lock {
        assertEquality(2, count);
    }

    map<Inner> innerMap1 = {inner: {foo: 20}};
    OuterX ox3 = {...innerMap1};
    assertEquality(20, ox3.inner.foo);
    lock {
        assertEquality(2, count);
    }

    InnerOpenRec innerMap2 = {"inner": {foo: 20}};

    OuterX ox4 = {...innerMap2};
    assertEquality(20, ox4.inner.foo);
    lock {
        assertEquality(2, count);
    }

    OuterXOpenRecord ox5 = {...innerMap2};
    assertEquality(20, ox5.inner.foo);
    lock {
        assertEquality(2, count);
    }

    OuterXOpenRecord ox6 = {...o};
    assertEquality(10, ox6.inner.foo);
    lock {
        assertEquality(3, count);
    }

    OuterXAlsoClosed oxx = {...o};
    assertEquality(oxx.inner.foo, 10);
    lock {
        assertEquality(4, count);
    }
    OuterXAlsoClosed oxx1 = {...o1};
    assertEquality(20, oxx1.inner.foo);
    lock {
        assertEquality(4, count);
    }

    OuterX ox7 = {};
    assertEquality(ox7.inner.foo, 10);
    lock {
        assertEquality(5, count);
    }

    OuterX ox8 = {inner: {foo: 20}};
    assertEquality(ox8.inner.foo, 20);
    lock {
        assertEquality(5, count);
    }

    EffectivelyCloseRecord ecr = {...o};
    assertEquality(ecr.inner.foo, 10);
    lock {
        assertEquality(6, count);
    }

    EffectivelyCloseRecord ecr1 = {...o1};
    assertEquality(ecr1.inner.foo, 20);
    lock {
        assertEquality(6, count);
    }
}

type Data record {
    string id = fn();
    string name;
};

type OpenData record {|
    string name;
    string...;
|};

isolated function fn() returns string {
    panic error("shouldn't be called");
}

public function testSpreadOverrideDefault() {
    OpenData or = {name: "May", "id": "A1234"};
    Data emp = {...or};
    assertEquality("A1234", emp.id);
}

const ASSERTION_ERROR_REASON = "AssertionError";

isolated function assertEquality(any|error expected, any|error actual) {
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
