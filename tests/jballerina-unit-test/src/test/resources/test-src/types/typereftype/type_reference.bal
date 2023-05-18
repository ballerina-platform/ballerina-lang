// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;

type Nil ();

type Integer int;

type Decimal decimal;

type Strings string;

type IntArray int[2];

type FloatBooleanTuple [float, boolean];

type Record record {|
    FloatBooleanTuple a;
    Nil b;
|};

type FunctionType1 function (Integer i) returns Decimal;

type FunctionType2 function (FloatBooleanTuple i) returns Record;

type Object1 object {
    public int a;
    *Object2;
};

type Object2 object {
    public int b;
    *Object3;
};

type Object3 object {
    public int c;
};

type One 1;

type ImmutableIntArray int[] & readonly;

type ImmutableIntArrayOrStringArray string[]|ImmutableIntArray;

type IntOrBoolean int|boolean;

type FooBar "foo"|One;

type OptionalInt int?;

record {|
    ImmutableIntArrayOrStringArray a;
    FooBar b;
|} X = {a: ["a"], b: 1};

map<FooBar> fooBar = {"1": 1, "foo": "foo"};

type MapFooBar map<FooBar>;

const int ConstInt = 5;

type TypeConstInt ConstInt;

function testTypeRef() {
    Nil nil = ();
    assertEqual(nil, ());

    Integer c1 = int:MAX_VALUE;
    assertEqual(c1, int:MAX_VALUE);
    anydata a1 = int:MAX_VALUE;
    assertEqual(c1 is Integer, true);
    Integer c2 = int:MIN_VALUE;
    assertEqual(c2, int:MIN_VALUE);
    a1 = int:MIN_VALUE;
    assertEqual(a1 is Integer, true);
    a1 = 1.0;
    assertEqual(a1 is Integer, false);
    a1 = "a";
    assertEqual(a1 is Integer, false);

    Strings c3 = "abc_/<>";
    assertEqual(c3, "abc_/<>");
    a1 = "abc";
    assertEqual(a1 is Strings, true);
    a1 = 1;
    assertEqual(a1 is Strings, false);

    IntArray c4 = [1, 2];
    assertEqual(c4, [1, 2]);
    a1 = [1, 2];
    assertEqual(a1.cloneReadOnly() is IntArray, true);
    a1 = [1, 2, 3];
    assertEqual(a1.cloneReadOnly() is IntArray, false);
    a1 = [1];
    assertEqual(a1.cloneReadOnly() is int[2], false);

    FloatBooleanTuple c5 = [1.2, true];
    assertEqual(c5, [1.2, true]);
    a1 = [1.2, true];
    assertEqual(a1.cloneReadOnly() is FloatBooleanTuple, true);
    a1 = [1.2d, true];
    assertEqual(a1.cloneReadOnly() is FloatBooleanTuple, false);

    FunctionType1 func1 = function(int x) returns decimal {
        return <Decimal>x;
    };
    Decimal a2 = func1(3);
    assertEqual(a2, 3d);

    var func2 = function([float, boolean] x) returns Record {
        return {a: x, b: ()};
    };
    FloatBooleanTuple a3 = [1.2, true];
    Record a4 = func2(a3);
    assertEqual(a4, {a: [1.2, true], b: ()});
    assertEqual(func2 is function (FloatBooleanTuple i) returns Record, true);

    One c6 = 1;
    assertEqual(c6, 1);
    a1 = 1;
    assertEqual(a1 is One, true);
    assertEqual(a1 is Decimal, false);
    Decimal c7 = 1d;
    assertEqual(c7, 1d);
    a1 = 1.0;
    assertEqual(a1 is One, false);
    assertEqual(a1 is Decimal, false);
    a1 = 1.0d;
    assertEqual(a1 is One, false);
    assertEqual(a1 is Decimal, true);

    a1 = [1, 2];
    ImmutableIntArray a5 = [1, 2];
    assertEqual(a5 is int[], true);
    assertEqual(a5 is int[] & readonly, true);
    assertEqual(a1 is ImmutableIntArray, false);
    assertEqual(a5 is ImmutableIntArrayOrStringArray, true);
    assertEqual(a1 is ImmutableIntArrayOrStringArray, false);

    IntOrBoolean c8 = 1;
    assertEqual(c8, 1);
    IntOrBoolean c9 = true;
    assertEqual(c9, true);
    IntOrBoolean c10 = !true;
    assertEqual(c10, false);
    a1 = 1;
    assertEqual(a1 is IntOrBoolean, true);
    a1 = true;
    assertEqual(a1 is IntOrBoolean, true);
    a1 = "1";
    assertEqual(a1 is IntOrBoolean, false);
    a1 = 1.0;
    assertEqual(a1 is IntOrBoolean, false);
    a1 = ();
    assertEqual(a1 is IntOrBoolean, false);

    FooBar a6 = "foo";
    assertEqual("foo" is FooBar, true);
    assertEqual(1 is FooBar, true);
    assertEqual("1" is FooBar, false);
    assertEqual(a6 is 1|"foo", true);

    OptionalInt a7 = ();
    assertEqual(a7 is int|(), true);

    assertEqual(X is record {|ImmutableIntArrayOrStringArray a; FooBar b;|}, true);

    MapFooBar a8 = {a: 1};
    assertEqual(a8 is map<FooBar>, true);

    boolean b1 = true;
    "foo"|1 b2 = 1;
    if b1 {
        FooBar b = "foo";
        b2 = b;
    } else {
        FooBar b = 1;
        b2 = b;
    }
    assertEqual(b2, "foo");

    IntArray array = [1, 2];
    int b3 = 0;
    foreach Integer i in array {
        Integer b = i;
        b3 = b;
    }
    assertEqual(b3, 2);

    assertEqual(getImmutable([1, 2]), [1, 2]);
    assertEqual(getImmutable(["1", "2"]), []);

    TypeConstInt b4 = 5;
    assertEqual(b4 is ConstInt, true);
    assertEqual(ConstInt is TypeConstInt, true);

    Object1 obj = object {
        public int a = 1;
        public int b = 2;
        public int c = 3;
    };

    assertEqual(obj.a, 1);
    assertEqual(obj.b, 2);
    assertEqual(obj.c, 3);

    testFieldAccessExp();
}

type IntStringFloat [int, string, float];

function testTypeRef2() {
    anydata a1 = [1.2, true];
    boolean x1 = a1.cloneReadOnly() is FloatBooleanTuple;
    assertTrue(x1);

    anydata a2 = [1.2, true];
    boolean x2 = a2.cloneReadOnly() is [float, boolean];
    assertTrue(x2);

    var a3 = [1.2, true];
    boolean x3 = a3.cloneReadOnly() is FloatBooleanTuple;
    assertTrue(x3);

    var a4 = [1.2, true];
    boolean x4 = a4.cloneReadOnly() is [float, boolean];
    assertTrue(x4);

    anydata a5 = [1, "string", 1.1];
    boolean x5 = a5.cloneReadOnly() is IntStringFloat;
    assertTrue(x5);

    anydata a6 = [1, "string", 1.1];
    boolean x6 = a6.cloneReadOnly() is [int, string, float];
    assertTrue(x6);

    var a7 = [1, "string", 1.1];
    boolean x7 = a7.cloneReadOnly() is IntStringFloat;
    assertTrue(x7);

    var a8 = [1, "string", 1.1];
    boolean x8 = a8.cloneReadOnly() is [int, string, float];
    assertTrue(x8);
}

int i = 12;

type Json json;

type JsonMap map<json>;

function testFieldAccessExp() {
    JsonMap jsonMap = {"a": 149, "b": {"a": 1}, "c": null};
    Json|error res1 = jsonMap?.b?.a;
    assertTrue(res1 is Json && res1 == 1);

    Json j1 = {a: {b: i}};
    JsonMap j2 = {a: {b: i}};
    assertTrue(testJsonFieldAccessPositive1(j1));
    assertTrue(testJsonFieldAccessPositive2(j2));

    assertTrue(testNonMappingJsonFieldAccessNegative1(j1));
    assertTrue(testNonMappingJsonFieldAccessNegative2(j1));
    assertTrue(testJsonFieldAccessNegativeMissingKey2(j1));
    assertTrue(testJsonFieldAccessNegativeMissingKey3(j1));
}

function testJsonFieldAccessPositive1(Json j) returns boolean {
    Json be = {b: i};
    Json|error a = j.a;
    return a is Json && a == be;
}

function testJsonFieldAccessPositive2(Json j) returns boolean {
    Json|error b = j.a.b;
    return b is Json && b == i;
}

function testNonMappingJsonFieldAccessNegative1(Json j) returns boolean {
    Json|error a = j.a.b.c;
    return assertNonMappingJsonError(a);
}

function testNonMappingJsonFieldAccessNegative2(Json j) returns boolean {
    Json|error a = j.a.b.c.d;
    return assertNonMappingJsonError(a);
}

function testJsonFieldAccessNegativeMissingKey2(Json j) returns boolean {
    Json|error a = j.e;
    return assertKeyNotFoundError(a, "e");
}

function testJsonFieldAccessNegativeMissingKey3(Json j) returns boolean {
    Json|error a = j.e.f;
    return assertKeyNotFoundError(a, "e");
}

function assertNonMappingJsonError(json|error je) returns boolean {
    if (je is error) {
        var detailMessage = je.detail()["message"];
        string detailMessageString = detailMessage is error ? detailMessage.toString() : detailMessage.toString();
        return je.message() == "{ballerina}JSONOperationError" && detailMessageString == "JSON value is not a mapping";
    }
    return false;
}

function assertKeyNotFoundError(json|error je, string key) returns boolean {
    if (je is error) {
        var detailMessage = je.detail()["message"];
        string detailMessageString = detailMessage is error ? detailMessage.toString() : detailMessage.toString();
        return je.message() == "{ballerina/lang.map}KeyNotFound" &&
                                detailMessageString == "key '" + key + "' not found in JSON mapping";
    }
    return false;
}

function getImmutable(ImmutableIntArrayOrStringArray x) returns ImmutableIntArray {
    if x is ImmutableIntArray {
        return x;
    }
    return [];
}

type AnyDataType anydata;

type JsonType json;

type AnyType any;

function testUnionTypeRefWithMap() {
    AnyDataType map1 = {"a": 1};
    JsonType map2 = {"b": 2};
    AnyType map3 = {"c": 3};

    assertTrue(map1 is anydata);
    assertEqual(map1.toString(), "{\"a\":1}");

    assertTrue(map2 is json);
    assertEqual(map2.toString(), "{\"b\":2}");

    assertTrue(map3 is any);
    assertEqual(map3.toString(), "{\"c\":3}");
}

public class Student {
    string name;
    int id;
    float avg = 80.0;
    public function init(string n, int i) {
        self.name = n;
        self.id = i;
    }
}

public class NonReadOnlyStudent {
    final string name;
    final int id;
    int yob;

    public function init(string n, int i, int y) {
        self.name = n;
        self.id = i;
        self.yob = y;
    }
}

type StudentRef Student;

type NonReadOnlyStudentRef NonReadOnlyStudent;

type UnionRef StudentRef|NonReadOnlyStudentRef;

function testObjectTypeReferenceType() {
    StudentRef|NonReadOnlyStudentRef st1 = new ("John", 1234);
    assertTrue(st1 is Student);
    string name = st1.name;
    assertEqual("John", name);

    UnionRef st2 = new ("John", 1234);
    assertTrue(st2 is Student);
    name = st2.name;
    assertEqual("John", name);

    StudentRef st3 = new ("John", 1234);
    assertTrue(st3 is Student);
    name = st3.name;
    assertEqual("John", name);
}

type BTable table<map<int>>|BarTable;

type BarTable table<Bar> key(a);

type Bar record {
    readonly string a;
};

function testTableTypeReferenceType() {
    BTable tab1 = table key(a) [{a: "a"}];
    assertTrue(tab1 is BarTable);
    assertTrue(tab1 is table<Bar> key(a));

    BarTable tab2 = table key(a) [{a: "a"}];
    assertTrue(tab2 is table<Bar> key(a));
}

type Details record {|
    string reason;
    boolean fatal?;
|};

type E1 error<Details>;

type E2 error<record {|string reason;|}>;

type E1Ref E1;

type E2Ref E2;

function errorTypeReferenceTypeTest() {
    E1 err1 = error("invalid username", reason = "contains spaces");
    test:assertEquals(err1 is E2, true);

    E1Ref err2 = error("invalid username", reason = "contains spaces");
    test:assertEquals(err2 is E2, true);

    E2 err3 = error("invalid username", reason = "contains spaces");
    test:assertEquals(err3 is E1, true);

    E2Ref err4 = error("invalid username", reason = "contains spaces");
    test:assertEquals(err4 is E1Ref, true);

    error err5 = <error<Details>>error("invalid username", reason = "contains spaces");
    test:assertEquals(err5 is E1, true);
    test:assertEquals(err5 is E1Ref, true);
    test:assertEquals(err5 is E2, true);
    test:assertEquals(err5 is E2Ref, true);

    error err6 = <error<Details>>error("invalid username", reason = "contains spaces", fatal = true);
    test:assertEquals(err6 is E1, true);
    test:assertEquals(err6 is E1Ref, true);
    test:assertEquals(err6 is E2, false);
    test:assertEquals(err6 is E2Ref, false);
}

function assertTrue(anydata actual) {
    return assertEqual(actual, true);
}

function assertEqual(anydata actual, anydata expected) {
    if expected == actual {
        return;
    }
    panic error(string `expected '${expected.toString()}', found '${actual.toString()}'`);
}
