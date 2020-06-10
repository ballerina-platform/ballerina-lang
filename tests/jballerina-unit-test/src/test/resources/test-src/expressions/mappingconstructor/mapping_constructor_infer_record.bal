// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const ASSERTION_ERROR_REASON = "AssertionError";

type Person record {|
   string firstName;
   string lastName;
   int age;
|};

type PersonWithoutAge record {|
   string fn;
   string ln;
|};

public type IntAndStrings record {|
   int i;
   string...;
|};

Person p1 = {firstName: "Alex", lastName: "George", age: 23};
Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
Person p3 = {firstName: "John", lastName: "David", age: 33};

function testRecordInferringForMappingConstructorWithoutRestField() {
    Person[] personList = [p1, p2, p3];

    var outputList =
            from var person in personList
            select {
                   fn: person.firstName,
                   "ln": person.lastName
            };

    PersonWithoutAge[] arr = outputList;

    assertEquality(p1.firstName, arr[0].fn);
    assertEquality(p1.lastName, arr[0].ln);
    assertEquality(p2.firstName, arr[1].fn);
    assertEquality(p2.lastName, arr[1].ln);
    assertEquality(p3.firstName, arr[2].fn);
    assertEquality(p3.lastName, arr[2].ln);
}

public function testRecordInferringForMappingConstructorWithRestField1() returns IntAndStrings[] {
    Person[] personList = [p1, p2, p3];
    string key = "str";

    var outputList =
            from var person in personList
            select {
                   i: person.age,
                   [key]: person.firstName
            };

    return outputList;
}

public function testRecordInferringForMappingConstructorWithRestField2() {
    Person[] personList = [p1, p2, p3];
    string key = "str";

    var arr =
            from var person in personList
            select {
                   i: person.age,
                   [key]: person.firstName
            };

    assertEquality(p1.age, arr[0].i);
    assertEquality(p1.firstName, arr[0]["str"]);
    assertEquality(p2.age, arr[1].i);
    assertEquality(p2.firstName, arr[1]["str"]);
    assertEquality(p3.age, arr[2].i);
    assertEquality(p3.firstName, arr[2]["str"]);
}

public function testRecordInferringForMappingConstructorWithRestField3() {
    Person[] personList = [p1, p2, p3];
    string key = "str";
    string key2 = "str2";
    boolean b = false;

    var arr =
            from var person in personList
            select {
                   i: person.age,
                   "j": "hello",
                   [key]: person.firstName,
                   [key2]: b
            };

    var rec = arr[0];
    any a = rec;

    assertEquality(a is record {|int i; string j; string|boolean...;|}, true);
    assertEquality(p1.age, rec.i);
    assertEquality("hello", rec.j);
    assertEquality(p1.firstName, rec[key]);
    assertEquality(false, rec[key2]);
}

string s = "global s";
int i = 1;
Bar barVal = new ("bar");

var v1 = {
    a: 1,
    b: "hello world",
    c: barVal
};

type ExpInferredType1 record {|
    int a;
    string b;
    Bar c;
|};

function testMappingConstrExprWithNoACET() {
    ExpInferredType1 e1 = v1;

    record {|
        int a;
        string|Bar...;
    |} r1 = v1;

    assertEquality(1, e1.a);
    assertEquality("hello world", e1.b);
    assertEquality(barVal, e1.c);

    assertEquality(true, r1 is ExpInferredType1);
    assertEquality(1, r1.a);
    assertEquality("hello world", r1["b"]);
    assertEquality(barVal, r1["c"]);

    var v2 = {
        s,
        i,
        s2: s,
        t: typeof s
    };

    record {|
        string s;
        int i;
        typedesc<string> t;
        string s2;
    |} r2 = v2;

    assertEquality(s, r2.s);
    assertEquality(i, r2.i);
    assertEquality(s, r2.s2);
    assertEquality("typedesc string", r2.t.toString());
}

public type ExpInferredType2 record {|
    int a;
    json b;
    float c;
    xml d;
    string e;
    int|string...;
|};

public type ExpInferredType3 record {|
    int a;
    float c;
    xml d;
    string e;
    json...;
|};

function testMappingConstrExprWithNoACET2() {
    json j = 2;
    string f = "f2";

    var fn = function () returns string => "fn";

    var v2 = {
        a: 1,
        b: j,
        c: 23.10,
        d: xml `foo`,
        e: "str",
        [f]: 1,
        [fn()]: "other"
    };

    ExpInferredType2 e2 = v2;
    ExpInferredType3 e3 = v2;

    assertEquality(1, e2.a);
    assertEquality(2, e2.b);
    assertEquality(23.10, e2.c);
    assertEquality(xml `foo`, e2.d);
    assertEquality("str", e2.e);
    assertEquality(1, e2["f2"]);
    assertEquality("other", e2["fn"]);

    assertEquality(1, e3.a);
    assertEquality(23.10, e3.c);
    assertEquality(xml `foo`, e3.d);
    assertEquality("str", e3.e);
    assertEquality(2, e3["b"]);
    assertEquality(1, e3["f2"]);
    assertEquality("other", e3["fn"]);
}

type Bar object {
    public function init(any arg) {

    }
};

type Rec1 record {
    int i;
    boolean b;
};

type Rec2 record {
    string i?;
    float f?;
};

function testInferredRecordTypeWithOptionalTypeFieldViaSpreadOp() {
    Rec1 rec1 = {i: 1, b: true};
    Rec2 rec2 = {i: "str"};

    var r1 = {
        ...rec1,
        a: 0.1d,
        ...rec2
    };

    record {
        int|string i;
        boolean b;
        decimal a;
        float f?;
    } r2 = r1;

    assertEquality("str", r2.i);
    assertEquality(true, r2.b);
    assertEquality(0.1d, r2.a);
    assertEquality((), r2?.f);
}

type FooBar "foo"|"bar";

function testInferenceWithMappingConstrExprAsSpreadExpr() {
    FooBar fb = "foo";

    var x = {
        a: 1,
        b: 2,
        ...{e: "hello", fb},
        c: true
    };

    record {
        int a;
        int b;
        boolean c;
        string e;
        FooBar fb;
    } rec = x;

    assertEquality(1, rec.a);
    assertEquality(2, rec.b);
    assertEquality(true, rec.c);
    assertEquality("hello", rec.e);
    assertEquality("foo", rec.fb);
}

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
