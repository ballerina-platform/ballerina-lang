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

type ALL_WITHIN_RANGE 0|1;
type HALF_WITHIN_RANGE 1|2|9;

function testOpenArrayMemberAccessByLiteralPositive() returns boolean {
    int[] ia = [1, 2, 3];
    return ia[0] == 1;
}

const I2 = 2;

function testOpenArrayMemberAccessByConstPositive() returns boolean {
    int[] ia = [1, 2, 3];
    return ia[I2] == 3;
}

function testOpenArrayMemberAccessByVariablePositive() returns boolean {
    int[] ia = [1, 2, 3];
    int index = 1;
    return ia[index] == 2;
}

function testOpenArrayMemberAccessByFiniteTypeVariablePositive() returns boolean {
    int[] ia = [1, 2, 3];
    HALF_WITHIN_RANGE index = 2;
    return ia[index] == 3;
}

function testOpenArrayMemberAccessByLiteralIndexOutOfRange() returns boolean {
    int[] ia = [1, 2, 3];
    return ia[4] == 1;
}

const I6 = 6;

function testOpenArrayMemberAccessByConstIndexOutOfRange() returns boolean {
    int[] ia = [1, 2];
    return ia[I6] == 1;
}

function testOpenArrayMemberAccessByVariableIndexOutOfRange() returns boolean {
    int[] ia = [1, 2];
    int index = 5;
    return ia[index] == 1;
}

function testOpenArrayMemberAccessByFiniteTypeVariableIndexOutOfRange() returns boolean {
    int[] ia = [1, 2];
    HALF_WITHIN_RANGE index = 9;
    return ia[index] == 1;
}

function testClosedArrayMemberAccessByLiteralPositive() returns boolean {
    string[2] ia = ["one", "two"];
    return ia[0] == "one";
}

const I1 = 1;

function testClosedArrayMemberAccessByConstPositive() returns boolean {
    boolean[3] ia = [true, true, false];
    return ia[I1];
}

function testClosedArrayMemberAccessByVariablePositive() returns boolean {
    boolean[3] ia = [true, false, false];
    int index = 1;
    return ia[index] == false;
}

function testClosedArrayMemberAccessByFiniteTypeVariablePositive() returns boolean {
    int[3] ia = [1, 2, 3];
    HALF_WITHIN_RANGE index = 2;
    return ia[index] == 3;
}

function testClosedArrayMemberAccessByVariableIndexOutOfRange() returns boolean {
    int[2] ia = [1, 2];
    int index = 5;
    return ia[index] == 1;
}

function testClosedArrayMemberAccessByFiniteTypeVariableIndexOutOfRange() returns boolean {
    int[4] ia = [1, 2, 3, 4];
    HALF_WITHIN_RANGE index = 9;
    return ia[index] == 1;
}

function testTupleMemberAccessByLiteralPositive() returns boolean {
    [int, int, int] ia = [1, 2, 3];
    return ia[0] == 1;
}

function testTupleMemberAccessByConstPositive() returns boolean {
    [float, float] ia = [1.2, 32.2];
    return ia[I1] == 32.2;
}

function testTupleMemberAccessByVariablePositive() returns boolean {
    [int, int, int] ia = [1, 2, 3];
    int index = 1;
    return ia[index] == 2;
}

function testTupleMemberAccessByFiniteTypeVariablePositive() returns boolean {
    [int, int, int] ia = [1, 2, 3];
    HALF_WITHIN_RANGE index = 2;
    return ia[index] == 3;
}

function testTupleMemberAccessByVariableIndexOutOfRange() returns boolean {
    [string, float] ia = ["hello", 2.0];
    int index = 3;
    return ia[index] == 1.0;
}

function testTupleMemberAccessByFiniteTypeVariableIndexOutOfRange() returns boolean {
    [float, boolean, string] ia = [1.0, true, "hello"];
    HALF_WITHIN_RANGE index = 9;
    return ia[index] == false;
}

type Employee record {
    string name;
    boolean? registered = false;
    int id?;
};

function testRecordMemberAccessByLiteral() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    Employee e = { name: s, id: i, registered: b, "other": 1.0 };

    string s2 = e["name"];
    boolean? b2 = e["registered"];
    int? i2 = e["id"];
    anydata v1 = e["other"];

    return s == s2 && b == b2 && i == i2 && v1 == 1.0;
}

const NAMEC = "name";
const REGISTEREDC = "registered";
const IDC = "id";
const OTHERC = "other";

function testRecordMemberAccessByConstant() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    Employee e = { name: s, id: i, registered: b, "other": 1.0 };

    string s2 = e[NAMEC];
    boolean? b2 = e[REGISTEREDC];
    int? i2 = e[IDC];
    anydata v1 = e[OTHERC];

    return s == s2 && b == b2 && i == i2 && v1 == 1.0;
}


function testRecordMemberAccessByVariable() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    Employee e = { name: s, id: i, registered: b, "other": 1.0 };

    string index = "name";
    anydata s2 = e[index];
    index = "registered";
    anydata b2 = e[index];
    index = "id";
    anydata i2 = e[index];
    index = "other";
    anydata v1 = e[index];

    return s == s2 && b == b2 && i == i2 && v1 == 1.0;
}

// TODO Maryam - add by finite

function testRecordMemberAccessForNonExistingKey() returns boolean {
    Employee e = { name: "Anne", id: 111, registered: true };

    anydata s2 = e["non_existing_key"];
    string index = "non_existing_key";
    anydata s4 = e[index];

    return s2 is () && s4 is ();
}

function testMapMemberAccessByLiteral() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    map<string|int|boolean> e = { name: s, id: i, registered: b };

    string|int|boolean? s2 = e["name"];
    string|int|boolean? b2 = e["registered"];
    string|int|boolean? i2 = e["id"];

    return s == s2 && b == b2 && i == i2;
}

function testMapMemberAccessByConstant() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    map<string|int|boolean> e = { name: s, id: i, registered: b };

    string|int|boolean? s2 = e[NAMEC];
    string|int|boolean? b2 = e[REGISTEREDC];
    string|int|boolean? i2 = e[IDC];

    return s == s2 && b == b2 && i == i2;
}

function testMapMemberAccessByVariable() returns boolean {
    string s = "Anne";
    boolean b = true;
    int i = 100;

    map<anydata> e = { name: s, id: i, registered: b };

    string index = "name";
    anydata s2 = e[index];
    index = "registered";
    anydata b2 = e[index];
    index = "id";
    anydata i2 = e[index];

    return s == s2 && b == b2 && i == i2;
}

function testMapAccessForNonExistingKey() returns boolean {
    map<boolean> e = { one: true, two: false };
    anydata s2 = e["non_existing_key"];
    string index = "non_existing_key";
    anydata s4 = e[index];

    return s2 is () && s4 is ();
}

function testMemberAccessOnNillableMap1() returns boolean {
    map<string>? m = { "one": "1", "two": "2" };

    string index = "two";
    string? s1 = m["one"];
    string? s2 = m[index];
    return s1 == "1" && s2 == "2";
}

function testMemberAccessOnNillableMap2() returns boolean {
    map<map<int>>? m = { "one": { first: 1, second: 2 }, "two": { third: 3 } };

    string index = "two";
    int? s1 = m["one"]["first"];
    int? s2 = m[index]["third"];
    return s1 == 1 && s2 == 3;
}

function testMemberAccessNilLiftingOnNillableMap1() returns boolean {
    map<string>? m = ();

    string index = "two";
    string? s1 = m["one"];
    string? s2 = m[index];
    return s1 == () && s2 == ();
}

function testMemberAccessNilLiftingOnNillableMap2() returns boolean {
    map<map<string>>? m = ();

    string index = "two";
    string? s1 = m["one"]["two"];
    string? s2 = m[index]["three"];
    return s1 == () && s2 == ();
}

type Foo record {
    Bar b = { id: 1 };
};

type Bar record {
    int id;
};

function testMemberAccessOnNillableRecord1() returns boolean {
    Employee? m = { name: "Anne", registered: true };
    string index = "registered";
    string? s1 = m["name"];
    string|boolean|int|anydata s2 = m[index];
    return s1 == "Anne" && s2 == true;
}

function testMemberAccessOnNillableRecord2() returns boolean {
    Foo? m = { b: { id: 100 } };
    string index = "registered";
    int? s1 = m["b"]["id"];
    int|anydata s2 = m["b"][index];
    return s1 == 100 && s2 == ();
}

function testMemberAccessNilLiftingOnNillableRecord1() returns boolean {
    Employee? m = ();
    string index = "registered";
    string? s1 = m["name"];
    string|boolean|int|anydata s2 = m[index];
    return s1 == () && s2 == ();
}

function testMemberAccessNilLiftingOnNillableRecord2() returns boolean {
    Foo? m = ();
    string index = "registered";
    int? s1 = m["b"]["id"];
    int|anydata s2 = m["b"][index];
    return s1 == () && s2 == ();
}

type Baz record {
    string x;
    int y?;
};

type Qux record {
    string x;
    boolean y;
    float z = 1.0;
};

function testMemberAccessOnRecordUnion() returns boolean {
    Baz b = { x: "hello", y: 11 };
    Baz|Qux bq = b;
    string index = "x";

    string x = bq["x"];
    anydata x2 = bq[index];
    int|boolean? y = bq["y"];
    anydata z = bq["z"];

    return x == "hello" && x2 == "hello" && y == 11 && z is ();
}

function testMemberAccessOnMapUnion() returns boolean {
    map<string> b = { x: "hello", y: "world" };
    map<string>|map<map<boolean>> bq = b;
    string index = "x";

    string|map<boolean>? x = bq["x"];
    anydata x2 = bq[index];
    string|map<boolean>? y = bq["z"];

    return x == "hello" && x2 == "hello" && y is ();
}

function testMemberAccessOnMappingUnion() returns boolean {
    Baz b = { x: "hello", y: 11 };
    Baz|map<int>|map<map<float>> bq = b;
    string index = "x";

    string|int|map<float>? x = bq["x"];
    anydata x2 = bq[index];
    int|map<float>? y = bq["y"];
    anydata z = bq["z"];

    return x == "hello" && x2 == "hello" && y == 11 && z is ();
}

function testVariableStringMemberAccess() returns boolean {
    string s = "hello world";
    string[] sa = [];

    foreach int i in 0 ..< s.length() {
        sa[i] = s[i];
    }

    int index = 0;
    string newSt = "";
    foreach string st in sa {
        newSt += st;
    }
    return newSt == s;
}

const CONST_STRING_1 = "string value";

function testConstStringMemberAccess1() returns boolean {
    string[] sa = [];

    foreach int i in 0 ..< CONST_STRING_1.length() {
        sa[i] = CONST_STRING_1[i];
    }

    int index = 0;
    string newSt = "";
    foreach string st in sa {
        newSt += st;
    }
    return newSt == CONST_STRING_1;
}

const CONST_STRING_2 = "abcdef value";

const IIC1 = 1;
const IIC3 = 3;

function testConstStringMemberAccess2() returns boolean {
    return CONST_STRING_2[0] == "a" && CONST_STRING_2[IIC1] == "b" && CONST_STRING_2[2] == "c" &&
            CONST_STRING_2[IIC3] == "d";
}

function testOutOfRangeStringMemberAccess1() {
    string s = "hello";
    string s2 = s[-1];
}

function testOutOfRangeStringMemberAccess2() {
    string s = "hello world";
    string s2 = s[s.length()];
}

function testOutOfRangeStringMemberAccess3() {
    int i = 25;
    string s2 = CONST_STRING_2[i];
}

type StFooBar "foo"|"bar";
type IntValues -1|0|4;

function testFiniteTypeStringMemberAccess() returns boolean {
    StFooBar s1 = "bar";
    string s2 = "bar";
    IntValues i1 = 0;
    int i2 = 1;
    return s1[i1] == "b" && s1[i2] == "a" && s2[i1] == "b" && s2[i2] == "a";
}

function testOutOfRangeFiniteTypeStringMemberAccess() {
    StFooBar s = "foo";
    IntValues i = 4;
    _ = s[i];
}


type Foo1 record {|
    string s1;
    int i1;
|};

type Bar1 record {|
    string s2;
    int i2;
|};

function testMemberAccessInUnionType() {
    testMemberAccessInUnionType1();
    testMemberAccessInUnionType2();
}

function testMemberAccessInUnionType1() {
    Foo1 f = {s1: "s", i1: 1};
    Foo1|Bar1? fb = f;

    string|int? x1 = fb["s1"];
    string|int? x2 = fb["i1"];
    string|int? x3 = fb["s2"];
    string|int? x4 = fb["i2"];

    if !(x1 == "s" && x2 == 1 && x3 == () && x4 == ()) {
        panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found 'false'");
    }
}

function testMemberAccessInUnionType2() {
    Bar1 b = {s2: "s", i2: 1};
    Foo1|Bar1 fb = b;

    string|int? x1 = fb["s1"];
    string|int? x2 = fb["i1"];
    string|int? x3 = fb["s2"];
    string|int? x4 = fb["i2"];

    if !(x1 == ()) && x2 == () && x3 == "s" && x4 == 1 {
        panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found 'false'");
    }
}

type EmployeeEntity record {
    int id;
    string fname;
    string lname;
    int age;
    AddressEntity addr;
};

type AddressEntity record {
    string line1;
    string line2;
    string city;
    string state;
    string postalCode;
};

type EmployeeView record {|
    string fname;
    string lname;
    int age;
    AddressView addr;
|};

type AddressView record {|
    string city;
    string state;
    string postalCode;
|};

function testMemberAccessOnStructuralConstructs() {
    testMemberAccessOnMapConstruct();
    testMemberAccessOnListConstruct();
    testMemberAccessOnTableConstruct();
    testMemberAccessOnQueryExperssion();
}

function testMemberAccessOnMapConstruct() {
    string name = {name: "Sanjiva", registered: true, id: 1}["name"];
    assertEquality("Sanjiva", name);
}

function testMemberAccessOnListConstruct() {
    EmployeeEntity entity = [{id: 1232, fname: "S", lname: "J", age: 30,
        addr: {line1: "N", line2: "561", city: "S", state: "C", postalCode: "95"}}][0];
    assertEquality(1232, entity["id"]);
}

function testMemberAccessOnTableConstruct() {
    Employee? emp  = table key(name) [{name: "Sanjiva", registered: true, id: 1},
        {name: "James", registered: true, id: 2}]["James"];
    assertEquality(2, emp["id"]);
}

function testMemberAccessOnQueryExperssion() {
    EmployeeEntity entity = {id: 1232, fname: "S", lname: "J", age: 30,
        addr: {line1: "N", line2: "561", city: "S", state: "C", postalCode: "95"}};
    EmployeeView emp = (from var {fname, lname, age, addr:{city, state, postalCode}} in [entity]
                        select {fname, lname, age, addr:{city, state, postalCode}})[0];
    assertEquality(30, emp["age"]);
}

type StringRecord record {|
    string...;
|};

type IntRecord record {|
    int...;
|};

function testRestFieldAccessOnNilableRecordUnion() returns boolean {
    StringRecord? f = {"x": "abc", "y": "def"};
    var v1 = f["x"];
    assertEquality("abc", v1);
    var v2 = f["z"];
    assertEquality((), v2);

    StringRecord|IntRecord? fb1 = {"x": 100, "y": 200};
    int|string? v3 = fb1["z"];
    assertEquality((), v3);
    int|string? v4 = fb1["y"];
    assertEquality(200, v4);

    StringRecord|IntRecord? fb2 = ();
    var v5 = fb2["x"];
    assertEquality((), v5);
    return true;
}

function testAccessOnNilableMapUnion() returns boolean {
    map<string>? f = {"x": "abc", "y": "def"};
    var v1 = f["x"];
    assertEquality("abc", v1);
    var v2 = f["z"];
    assertEquality((), v2);

    map<string>|map<int>? fb1 = {"x": 100, "y": 200};
    int|string? v3 = fb1["z"];
    assertEquality((), v3);
    int|string? v4 = fb1["y"];
    assertEquality(200, v4);

    map<int>|map<string>? fb2 = ();
    var v5 = fb2["x"];
    assertEquality((), v5);
    return true;
}

type IntAndStringRecord record {|
    int x;
    string...;
|};

function testAccessOnNilableRecordMapUnion() returns boolean {
    map<string>|IntRecord? r1 = {"x": "abc", "y": "def"};
    var v1 = r1["x"];
    assertEquality("abc", v1);
    var v2 = r1["a"];
    assertEquality((), v2);

    map<string>|IntRecord? r2 = {"x": 111, "y": 222};
    var v3 = r2["a"];
    assertEquality((), v3);
    var v4 = r2["y"];
    assertEquality(222, v4);

    IntAndStringRecord|map<string> r3 = {"a": "foo", "b": "bar"};
    var v5 = r3["x"];
    assertEquality((), v5);
    var v6 = r3["b"];
    assertEquality("bar", v6);

    IntAndStringRecord|map<string> r4 = {"x": 1234, "y": "bar"};
    var v7 = r4["x"];
    assertEquality(1234, v7);
    var v8 = r4["y"];
    assertEquality("bar", v8);
    var v9 = r4["a"];
    assertEquality((), v9);
    return true;
}

type RecordWithRecordTypedRestDesc record {|
    boolean b;
    IntAndStringRecord...;
|};

function testNestedAccessOnNilableUnion() returns boolean {
    RecordWithRecordTypedRestDesc rec = {
        b: true,
        "rec1": {x: 2, "s1": "str1", "s2": "str2"},
        "rec2": {x: 3, "s3": "str3", "s4": "str4"}
    };

    string? v1 = rec["rec2"]["s4"];
    assertEquality("str4", v1);

    string? v2 = rec["not_present"]["s4"];
    assertEquality((), v2);

    string? v3 = rec["rec2"]["not_present"];
    assertEquality((), v3);
    return true;
}

function testUnavailableFinalAccessInNestedAccess() {
    map<map<int>> f = {b: {}}; // `b` is present, but `b` doesn't have `i`.
    int? i = f["b"]["i"];
    int? j = (f["b"])["i"];
    int? k = ((f["b"]["i"]));

    assertTrue(i is ());
    assertTrue(j is ());
    assertTrue(k is ());
}

function testAvailableFinalAccessInNestedAccess() {
    map<map<int>> f = {b: {i: 1234}}; // `b` is present, and has `i`.
    int? i = f["b"]["i"];
    int? j = (f["b"])["i"];
    int? k = ((f["b"]["i"]));

    assertEquality(1234, i);
    assertEquality(1234, j);
    assertEquality(1234, k);
}

function testUnavailableIntermediateAccessInNestedAccess() {
    map<map<int>> f = {c: {}};
    int? i = f["b"]["i"];
    int? j = (f["b"])["i"];
    int? k = ((f["b"]["i"]));

    assertTrue(i is ());
    assertTrue(j is ());
    assertTrue(k is ());
}

function testNilValuedFinalAccessInNestedAccess() {
    map<map<int?>> f = {x: {i: (), j: 2}};
    int? i = f["x"]["i"];
    int? j = (f["x"])["i"];
    int? k = ((f["x"]["i"]));

    assertTrue(i is ());
    assertTrue(j is ());
    assertTrue(k is ());

    int? q = ((f["x"]["j"]));
    assertEquality(2, q);
}

function testMemberAccessOnStrings() {
    string a = "ABC"[0];
    assertEquality("A", a);

    string b = "EFG";
    string c = b[1];
    assertEquality("F", c);

    string e = string `${"HIJ"}`;
    string f = e[2];
    assertEquality("J", f);

    string:Char g = "K";
    string h = g[0];
    assertEquality("K", h);
}

function testInvalidMemberAccessOnStrings1() {
    string a = "ABC";
    string b = a[5];
}

function testInvalidMemberAccessOnStrings2() {
    string:Char a = "K";
    string b = a[3];
}

public type Quux record {|
    int i;
    Corge baz?;
|};

public type Corge record {|
    string a;
    int i?;
|};

public function testNestedMemberAccessOnIntersectionTypes() {
    Quux & readonly q1 = {i: 1, baz: {a: "hello", i: 2}};
    var v1 = q1["baz"]["i"];
    assertTrue(v1 is int);
    assertEquality(2, v1);

    Quux & readonly q2 = {i: 1, baz: {a: "hello"}};
    int? v2 = q2["baz"]["i"];
    assertTrue(v2 is ());
    assertEquality((), v2);

    Quux & readonly q3 = {i: 1};
    var v3 = q3["baz"]["i"];
    assertTrue(v3 is ());
    assertEquality((), v3);
}

type MyRec record {|
    string name;
|};

const LENGTH = 10;

function testMemberAccessWithBinaryExprAsIndex() {
    int x = 8;
    int[3] intArr = [0, 1, 2];
    int member1 = intArr[10 - 9];
    assertEquality(1,member1);
    member1 = intArr[x - 6];
    assertEquality(2, member1);
    
    [int, string] tupleVar = [101, "Jhone"];
    int|string member2 = tupleVar[2 - 1];
    assertEquality("Jhone", member2);
    member2 = tupleVar[x - 8];
    assertEquality(101, member2);
    
    int member3 = intArr[10 - x];
    assertEquality(2, member3);
    
    int member4 = intArr[10 - LENGTH];
    assertEquality(0, member4);
    
    string y = "na";
    MyRec myRecVar = {name: "Ballerina"};
    string? member5 = myRecVar["na" + "me"];
    assertEquality("Ballerina", member5);
    member5 = myRecVar[y + "me"];
    assertEquality("Ballerina", member5);
    
    (string|error)? member6 = trap myRecVar["Ballerina" + "1"];
    assertEquality(null, member6);
    member6 = trap myRecVar["Ballerina" + y];
    assertEquality(null, member6);
    
    map<int> mapVar = {field1: 202, field2: 303};
    int? member7 = mapVar["field" + "1"];
    assertEquality(202, member7);
    string z = "2";
    member7 = mapVar["field" + z];
    assertEquality(303, member7);
    
    (int|error)? member8 = trap mapVar["field1" + "1"];
    assertEquality(null, member8);
    member8 = trap mapVar["field1" + z];
    assertEquality(null, member8);
}

function testMemberAccessWithGroupExprAsIndex() {
    int[3] intArr = [0, 1, 2];
    int member1 = intArr[((1))];
    assertEquality(1, member1);
    
    [int, int] intTuple = [3, 4];
    int member2 = intTuple[(10 - 9)];
    assertEquality(4, member2);
    member2 = intTuple[(LENGTH - 10)];
    assertEquality(3, member2);
    
    MyRec myRecVar = {name: "Ballerina"};
    string member3 = myRecVar[("name")];
    assertEquality("Ballerina", member3);
    
    map<int> mapVar = {field1: 202};
    int? member4 = mapVar[("field1")];
    assertEquality(202, member4);
}

function testMemberAccessOutOfRangeWithBinaryExpr1() {
    int[3] intArr = [0, 1, 2];
    int|error a = trap intArr[2 + 1];
    
    assertTrue(a is error);
    error e = <error> a;
    assertEquality("{ballerina/lang.array}IndexOutOfRange", e.message());
    assertEquality("array index out of range: index: 3, size: 3", e.detail()["message"]);
}

function testMemberAccessOutOfRangeWithBinaryExpr2() {
    [int, string] tupleVar = [0, "Car"];
    int|string|error a = trap tupleVar[1 + 1];
    
    assertTrue(a is error);
    error e = <error> a;
    assertEquality("{ballerina/lang.array}IndexOutOfRange", e.message());
    assertEquality("tuple index out of range: index: 2, size: 2", e.detail()["message"]);
}

function testMemberAccessOutOfRangeWithBinaryExpr3() {
    int[3] intArr = [0, 1, 2];
    int int2 = 2;
    int|error a = trap intArr[int2 + 1];
    
    assertTrue(a is error);
    error e = <error> a;
    assertEquality("{ballerina/lang.array}IndexOutOfRange", e.message());
    assertEquality("array index out of range: index: 3, size: 3", e.detail()["message"]);
}

function testMemberAccessOutOfRangeWithBinaryExpr4() {
    [int, string] tupleVar = [0, "Car"];
    int int1 = 1;
    int|string|error a = trap tupleVar[int1 + 1];
    
    assertTrue(a is error);
    error e = <error> a;
    assertEquality("{ballerina/lang.array}IndexOutOfRange", e.message());
    assertEquality("tuple index out of range: index: 2, size: 2", e.detail()["message"]);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

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
