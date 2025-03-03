// Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

// ========================== Basics ==========================

function testValueTypeInUnion() returns string {
    int|string x = "hello";
    int y = 10;
    if (x !is int) {
        return "string";
    } else {
        return "int";
    }
}

function testUnionTypeInUnion() returns string {
    int|string|float x = 5;
    if (x !is int|float) {
        return "string";
    } else {
        return "numeric";
    }
}

function testNestedTypeCheck() returns [any, any, any] {
    return [bar(true), bar(1234), bar("hello")];
}

function bar (string | int | boolean i)  returns string {
    if (i !is string|boolean){
        return "int";
    } else {
        if (i !is string) {
            return "boolean";
        } else {
            return "string";
        }
    }
}

function testTypeInAny() returns (string) {
    any a = "This is working";
    if (a !is string) {
        return "any";
    } else {
        return "string value: " + <string> a;
    }
}

function testNilType() returns (string) {
    any a = ();
    if(a !is ()) {
        return "any";
    }else {
        return "nil";
    }
}

function testTypeChecksWithLogicalAnd() returns string {
    int|string x = "hello";
    float|boolean y = true;
    if (x !is int && y !is float) {
        return "string and boolean";
    } else if (x !is int && y !is boolean) {
        return "string and float";
    } else if (x !is string && y !is float) {
        return "int and boolean";
    } else if (x !is string && y !is boolean) {
        return "int and float";
    } else {
        return "I don't know";
    }
}

function testTypeCheckInTernary() returns string {
    any a = "hello";
    return a !is string|float ? "An integer" : a !is string ? "A float" : "A string";
}

// ========================== Records ==========================

type A1NTT record {
    int x = 0;
};

type B1NTT record {
    int x = 0;
    string y = "";
};

function testSimpleRecordTypes_1() returns string {
    A1NTT a1 = {};
    any a = a1;
     if (a !is A1NTT) {
        return "n/a";
    } else {
        return "a is A1";
    }
}

function testSimpleRecordTypes_2() returns [boolean, boolean] {
    B1NTT b = {};
    any a = b;
    return [a !is A1NTT, a !is B1NTT];
}

type A2NTT record {
    int x = 0;
};

type B2NTT record {
    int x = 0;
};

function testSimpleRecordTypes_3() returns [boolean, boolean] {
    B2NTT b = {};
    any a = b;
    return [a !is A2NTT, a !is B2NTT];
}

type HumanNTT record {
    string name;
    (function (int, string) returns string) | () foo = ();
};

type ManNTT record {
    string name;
    (function (int, string) returns string) | () foo = ();
    int age = 0;
};

function testRecordsWithFunctionType_1() returns [string, string] {
    HumanNTT m = {name:"Piyal"};
    any a = m;
    string s1;
    string s2;

    if (a !is ManNTT) {
        s1 = "a is not a man";
    } else {
        s1 = "Man: " + m.name;
    }

    if (a !is HumanNTT) {
        s2 = "a is not a human";
    } else {
        s2 = "Human: " + m.name;
    }

    return [s1, s2];
}

function testRecordsWithFunctionType_2() returns [string, string] {
    ManNTT m = {name:"Piyal"};
    any a = m;
    string s1;
    string s2;

    if (a !is ManNTT) {
        s1 = "a is not a man";
    } else {
        s1 = "Man: " + m.name;
    }

    if (a !is HumanNTT) {
        s2 = "a is not a human";
    } else {
        s2 = "Human: " + m.name;
    }

    return [s1, s2];
}

type XTN record {
    int p = 0;
    string q = "";
    A1NTT r = {};
};

type YTN record {
    int p = 0;
    string q = "";
    B1NTT r = {};   // Assignable to A1. Hence Y is assignable to X.
};

function testNestedRecordTypes() returns [boolean, boolean] {
    YTN y = {};
    any x = y;
    return [x is XTN, x is YTN];
}

type A3NTT record {
    int x = 0;
};

type B3NTT record {|
    int x = 0;
|};

function testSealedRecordTypes() returns [boolean, boolean] {
    A3NTT a3 = {};
    any a = a3;
    return [a !is A3NTT, a !is B3NTT];
}

// ========================== Objects ==========================

public class Person {
    public int age;
    public string name;
    public string address = "";

    public function init(string name, int age) {
        self.age = age;
        self.name = name;
    }

    public function getName() returns (string) {
        return self.name;
    }

    public function getAge() returns (int) {
        return self.age;
    }

    public function getAddress() returns (string) {
        return self.address;
    }
}

public class SameAsPerson {
    public int age;
    public string name;
    public string address = "";

    public function init(string name, int age) {
        self.age = age;
        self.name = name;
    }

    public function getName() returns (string) {
        return self.name;
    }

    public function getAge() returns (int) {
        return self.age;
    }

    public function getAddress() returns (string) {
        return self.address;
    }
}

function testObjectWithSameMembersButDifferentAlias() returns [string, string, string, string] {
    Person p1 = new("John", 35);
    any a = p1;

    SameAsPerson p2 = new ("Doe", 45);
    any b = p2;

    string s1 = "I am no one";
    string s2 = "I am no one";
    string s3 = "I am no one";
    string s4 = "I am no one";

    if !(a !is SameAsPerson) {
        s1 = "I am same as person: " + a.getName();
    }

    if !(a !is Person) {
        s2 = "I am a person: " + a.getName();
    }

    if !(b !is SameAsPerson) {
        s3 = "I am same as person: " + b.getName();
    }

    if !(b !is Person) {
        s4 = "I am a person: " + b.getName();
    }

    return [s1, s2, s3, s4];
}

public class PersonInOrder {
    public int age;
    public string name;
    public string address = "";

    public function init(string name, int age) {
        self.age = age;
        self.name = name;
    }

    public function getName() returns (string) {
        return self.name;
    }

    public function getAge() returns (int) {
        return self.age;
    }

    public function getAddress() returns (string) {
        return self.address;
    }
}

public class PersonNotInOrder {

    public function getName() returns (string) {
        return self.name;
    }

    public int age;

    public function getAge() returns (int) {
        return self.age;
    }

    public function init(string name, int age) {
        self.age = age;
        self.name = name;
    }

    public string name;

    public function getAddress() returns (string) {
        return self.address;
    }

    public string address = "";
}

function testObjectWithUnorderedFields() returns [string, string, string, string] {
    PersonInOrder p1 = new("John", 35);
    any a = p1;

    PersonNotInOrder p3 = new ("Doe", 45);
    any b = p3;

    string s1 = "I am no one";
    string s2 = "I am no one";
    string s3 = "I am no one";
    string s4 = "I am no one";

    if !(a !is PersonInOrder) {
        s1 = "I am a person in order: " + a.getName();
    }

    if !(a !is PersonNotInOrder) {
        s2 = "I am a person not in order: " + a.getName();
    }

    if !(b !is PersonInOrder) {
        s3 = "I am a person in order: " + b.getName();
    }

    if !(b !is PersonNotInOrder) {
        s4 = "I am a person not in order: " + b.getName();
    }

    return [s1, s2, s3, s4];
}

public type A4NTT object {
    public int p;
    public string q;
};

public type B4NTT object {
    public float r;
    *A4NTT;
};

public class C4 {
    *B4NTT;
    public boolean s;

    public function init(int p, string q, float r, boolean s) {
        self.p = p;
        self.q = q;
        self.r = r;
        self.s = s;
    }
}

function testPublicObjectEquivalency() returns [string, string, string] {
    any x = new C4(5, "foo", 6.7, true);
    string s1 = "n/a";
    string s2 = "n/a";
    string s3 = "n/a";

    if !(x !is A4NTT) {
        s1 = "values: " + x.p.toString() + ", " + x.q;
    }

    if !(x !is B4NTT) {
        s2 = "values: " + x.p.toString() + ", " + x.q + ", " + x.r.toString();
    }

    if !(x !is Person) {   // shouldn't match
        s3 = "values: " + x.name + ", " + x.age.toString();
    }

    return [s1, s2, s3];
}

type A5NTT object {
    int p;
    string q;
};

type B5NTT object {
    float r;
    *A5NTT;
};

class C5 {
    *B5NTT;
    boolean s;

    public function init(int p, string q, float r, boolean s) {
        self.p = p;
        self.q = q;
        self.r = r;
        self.s = s;
    }
}

function testPrivateObjectEquivalency() returns [string, string, string] {
    any x = new C5(5, "foo", 6.7, true);
    string s1 = "n/a";
    string s2 = "n/a";
    string s3 = "n/a";

    if !(x !is A5NTT) {
        s1 = "values: " + x.p.toString() + ", " + x.q;
    }

    if !(x !is B5NTT) {
        s2 = "values: " + x.p.toString() + ", " + x.q + ", " + x.r.toString();
    }

    if !(x !is Person) {   // shouldn't match
        s3 = "values: " + x.name + ", " + x.age.toString();
    }

    return [s1, s2, s3];
}

function testAnonymousObjectEquivalency() returns [string, string, string] {
    any x = new C4(5, "foo", 6.7, true);
    string s1 = "n/a";
    string s2 = "n/a";
    string s3 = "n/a";

    if !(x !is object { public float r; *A4NTT; }) {
        s1 = "values: " + x.p.toString() + ", " + x.q + ", " + x.r.toString();
    }

    if !(x !is object {  public int p;  public string q;  public float r;  public boolean s;}) {
        s2 = "values: " + x.p.toString() + ", " + x.q + ", " + x.r.toString() + ", " + x.s.toString();
    }

    if !(x !is object { public int p;  public boolean q;  public float r;}) {  // shouldn't match
        s3 = "values: " + x.p.toString() + ", " + x.q.toString() + ", " + x.r.toString();
    }

    return [s1, s2, s3];
}

class QuxNeg {
    QuxNeg? fn;

    public function init(QuxNeg? fn = ()) {
        self.fn = fn;
    }
}

class QuuxNeg {
    QuuxNeg? fn = ();
}

class QuuzNeg {
    QuuzNeg? fn = ();
    int i = 1;
}

class ABCNeg {
    QuxNeg f;
    string s;

    function init(QuxNeg f, string s) {
        self.f = f;
        self.s = s;
    }
}

function testObjectIsCheckWithCycles() {
    QuxNeg f1 = new;
    QuxNeg f2 = new (f1);

    any a1 = <any>f1;
    assertFalse(a1 !is QuuxNeg);
    assertTrue(a1 !is QuuzNeg);

    any a2 = <any>f2;
    assertFalse(a2 !is QuuxNeg);
    assertTrue(a2 !is QuuzNeg);

    ABCNeg ob = new (f2, "ballerina");

    any a3 = ob;
    assertFalse(a3 !is object {QuxNeg f;});
    assertTrue(a3 !is object {QuuzNeg f;});
}

// ========================== Arrays ==========================

function testSimpleArrays() returns [boolean, boolean, boolean, boolean, boolean] {
    int[] a = [1, 2, 3];
    int[][] b = [[1, 2, 3], [4, 5, 6]];
    any c = a;
    any d = b;
    return [(c !is int[] && d !is int[][]), c !is float[], d !is json, d !is json[], d !is json[][]];
}

function testRecordArrays() returns [boolean, boolean, boolean, boolean] {
    XTN[] a = [{}, {}];
    XTN[][] b = [[{}, {}], [{}, {}]];
    any c = a;
    any d = b;
    return [c !is XTN[], d !is XTN[][], c !is YTN[], d !is YTN[][]];
}

public function testUnionType() {
    [int, ()] x = [1, ()];
    any y = x;
    assertEquality(y !is (string|int?)[2], false);
    assertEquality(y !is (string|int?)[], false);
    assertEquality(y !is (int|string?)[], false);
    assertEquality(y !is (int|string?)[3], true);
    assertEquality(y !is (int|string)[], true);
    assertEquality(y !is int?[], false);
    assertEquality(y !is int?[2], false);

    [int, string?] a = [1, "union"];
    any b = a;
    assertEquality(b !is (int|string?)[], false);
    assertEquality(b !is (int|string?)[2], false);
    assertEquality(b !is (int?|string)[2], false);
    assertEquality(b !is (int|string?)[1], true);
    assertEquality(b !is int?[2], true);

    (int|string)[] c = [1, 2];
    any d = c;
    assertEquality(d !is (int|string?)[], false);
    assertEquality(d !is (int|string)[2], true);
    assertEquality(d !is [int, string], true);
    assertEquality(d !is [int, int], true);
    assertEquality(d !is [string, string], true);
    assertEquality(d !is [int, ()], true);
    assertEquality(d !is [int...], true);
    assertEquality(d !is [string...], true);
    assertEquality(d !is [string, int...], true);
}

public function testClosedArrayType(){
      int[2] b = [1, 2];
      any y = b;
      assertEquality(y !is [int, int], false);
      assertEquality(y !is [int...], false);
      assertEquality(y !is [int, int, int...], false);
      assertEquality(y !is [string, string, int...], true);
      assertEquality(y !is [int, int, int], true);
      assertEquality(y !is [int, int, int, int...], true);
      assertEquality(y !is [int, int, string...], false);
}

public function testInferredArrayType() {
    int[*] b = [1, 2];
    any y = b;
    assertEquality(y !is [int, int], false);
    assertEquality(y !is [int...], false);
    assertEquality(y !is [int, int, int...], false);
    assertEquality(y !is [string, string, int...], true);
    assertEquality(y !is [int, int, int], true);
    assertEquality(y !is [int, int, int, int...], true);
    assertEquality(y !is [int, int, string...], false);
}

public function testEmptyArrayType() {
    var x = [];
    any a = x;
    assertEquality(a !is int[2], true);
    assertEquality(a !is int[], false);
    assertEquality(a !is [int...], false);

    string[] sa = [];
    any arr = sa;
    assertEquality(arr !is string[], false);
    assertEquality(arr !is int[], true);

    int[0] ia = [];
    any iarr = ia;
    assertEquality(iarr !is int[0], false);
    assertEquality(iarr !is int[], false);

    int[] b = [1, 2];
    any c = b;
    assertEquality(c !is [int...], false);
    assertEquality(c !is int[], false);
}

// ========================== Tuples ==========================

function testSimpleTuples() returns [boolean, boolean, boolean, boolean, boolean] {
    [int, string] x = [4, "hello"];
    any y = x;

    boolean b0 = y !is [int, string];
    boolean b1 = y !is [int, boolean];
    boolean b2 = y !is [float, boolean];
    boolean b3 = y !is [any, any];
    boolean b4 = y !is [json, json];

    return [b0, b1, b2, b3, b4];
}

function testTupleWithAssignableTypes_1() returns [boolean, boolean, boolean, boolean] {
    [XTN, YTN] p = [{}, {}];
    any q = p;
    boolean b0 = q !is [XTN, XTN];
    boolean b1 = q !is [XTN, YTN];
    boolean b2 = q !is [YTN, XTN];
    boolean b3 = q !is [YTN, YTN];
    return [b0, b1, b2, b3];
}

function testTupleWithAssignableTypes_2() returns boolean {
    [YTN, YTN] p = [{}, {}];
    [XTN, YTN] q = p;
    boolean b1 = q !is [YTN, YTN];
    return q !is [YTN, YTN];
}

public function testRestType() {
    [int...] x = [1, 2];
    any y = x;
    assertEquality(y !is string[], true);
    assertEquality(y !is [int...], false);
    assertEquality(y !is int[0], true);
    assertEquality(y !is int[], false);
    assertEquality(y !is [int], true);
    assertEquality(y !is int[2], true);

    [int, int, int...] a = [1, 2];
    any b = a;
    assertEquality(b !is int[2], true);
    assertEquality(b !is [int, int...], false);
    assertEquality(b !is [int...], false);
}

// ========================== Map ==========================

function testSimpleUnconstrainedMap_1() returns [boolean, boolean] {
    map<any> m = {"key1": "value1"};
    boolean b0 = m !is map<string>;
    boolean b1 = m !is map<json>;
    return [b0, b1];
}

function testSimpleUnconstrainedMap_2() returns [boolean, boolean, boolean, boolean, boolean] {
    map<any> m = {"key1": "value1"};
    any a = m;
    boolean b0 = a !is map<any>;
    boolean b1 = a !is map<any>;
    boolean b2 = a !is map<string>;
    boolean b3 = a !is json;
    boolean b4 = a !is map<json>;
    return [b0, b1, b2, b3, b4];
}

function testSimpleConstrainedMap() returns [boolean, boolean, boolean, boolean] {
    map<string> m1 = {"key1": "value1"};
    any m2 = m1;
    boolean b0 = m2 !is map<any>;
    boolean b1 = m2 !is map<any>;
    boolean b2 = m2 !is map<string>;
    boolean b3 = m2 !is map<json>;
    return [b0, b1, b2, b3];
}

// ========================== JSON ==========================

function testJSONTypeCheck() returns [string, string, string, string, string, string, string] {
    json j1 = 3;
    json j2 = 4.5;
    json j3 = "hello";
    json j4 = true;
    json j5 = [78, true, {"name": "john"}];
    json j6 = {"name": "john"};
    json j7 = null;
    return [checkJSON(j1), checkJSON(j2), checkJSON(j3), checkJSON(j4), checkJSON(j5), checkJSON(j6), checkJSON(j7)];
}

function checkJSON(json j) returns string {
    if !(j !is string) {
        return "json string: " + j;
    } else if !(j !is int) {
        return "json int";
    } else if !(j !is float) {
        return "json float";
    } else if !(j !is boolean) {
        return "json boolean";
    } else if !(j !is ()) {
        return "json null";
    } else if !(j !is json[]) {
        return "json array";
    } else {
        return "json object";
    }
}

function testJsonArrays() returns [boolean, boolean, boolean] {
    json[] p = [1, 2, 3];
    json[][] q = [[1, 2, 3], [4, 5, 6]];
    int[][] r = [[1, 2, 3], [4, 5, 6]];
    any x = p;
    any y = q;
    json z = r;
    boolean b0 = x !is int[];
    boolean b1 = y !is int[][];
    boolean b3 = z !is int[][];

    return [b0, b1, b3];
}

// ========================== Finite type ==========================

type State "on"|"off";

function testFiniteType() returns [boolean, boolean, boolean] {
    State a = "on";
    any b = a;
    any c = "off";
    any d = "hello";

    return [b !is State, c !is State, d !is State];
}

function testFiniteTypeInTuple() returns [boolean, boolean, boolean, boolean] {
    [State, string] x = ["on", "off"];
    any y = x;

    boolean b0 = y !is [State, State];
    boolean b1 = y !is [State, string];
    boolean b2 = y !is [string, State];
    boolean b3 = y !is [string, string];

    return [b0, b1, b2, b3];
}

function testFiniteTypeInTuplePoisoning() returns [State, State] {
    [State, string] x = ["on", "off"];
    any y = x;
    [State, State] z = ["on", "on"];

    if !(y !is [State, State]) {
        z = y;
    }

    x[1] = "surprise!";
    return [z[0], z[1]];
}

public const APPLE = "apple";
public const ORANGE = "orange";
public const GRAPE = "grape";

type Fruit APPLE | ORANGE | GRAPE;

function testFiniteType_1() returns string {
    any a = APPLE;
    if (a !is Fruit) {
        return "a is not a fruit";
    }

    return "a is a fruit";
}

function testFiniteType_2() returns string {
    any a = APPLE;
    if (a !is APPLE) {
        return "a is not an Apple";
    }

    return "a is an Apple";
}

function testFiniteTypeAsBroaderType_1() returns boolean {
    any a = GRAPE;
    return a !is string;
}

type FRUIT_OR_COUNT "apple"|2|"grape"|10;

function testFiniteTypeAsBroaderType_2() returns [boolean, boolean] {
    FRUIT_OR_COUNT fc1 = GRAPE;
    FRUIT_OR_COUNT fc2 = 10;

    return [fc1 !is string, fc2 !is int];
}

type FooBarOneBoolean "foo"|"bar"|1|boolean;
type FooBarBaz "foo"|"bar"|"baz";
type FalseFooThree false|"foo"|3;
type IntTwo int|2.0;

function testUnionWithFiniteTypeAsFiniteTypeTrue() returns [boolean, boolean] {
    FooBarOneBoolean f1 = "foo";
    FooBarOneBoolean f2 = 1;

    return [f1 !is FooBarBaz, f2 !is IntTwo];
}

function testUnionWithFiniteTypeAsFiniteTypeFalse() returns [boolean, boolean] {
    FooBarOneBoolean f1 = "foo";
    FooBarOneBoolean f2 = 1;

    return [f1 !is IntTwo, f2 !is FooBarBaz];
}

function testFiniteTypeAsFiniteTypeTrue() returns boolean {
    FooBarBaz f1 = "foo";
    return f1 !is FalseFooThree;
}

function testFiniteTypeAsFiniteTypeFalse() returns boolean {
    FooBarBaz f1 = "bar";
    return f1 is FalseFooThree;
}

function testIntersectingUnionTrue() returns [boolean, boolean] {
    string|int|typedesc<any> x = 1;
    return [!(x !is int|boolean), !(x !is json)];
}

function testIntersectingUnionFalse() returns [boolean, boolean] {
    string|int|typedesc<any> x = int;
    return [!(x !is int|boolean), !(x !is anydata)];
}

function testValueTypeAsFiniteTypeTrue() returns [boolean, boolean] {
    string s = "orange";
    float f = 2.0;
    return [!(s !is Fruit), !(f !is IntTwo)];
}

function testValueTypeAsFiniteTypeFalse() returns [boolean, boolean] {
    string s = "mango";
    float f = 12.0;
    return [!(s !is Fruit), !(f !is IntTwo)];
}

const ERR_REASON = "error reason";
const ERR_REASON_TWO = "error reason two";

type Details record {
    string message;
    error cause?;
};

type MyError distinct error<Details>;
type MyErrorTwo distinct error<Details>;

function testError_1() returns [boolean, boolean, boolean, boolean] {
    error e = error("error reason one");
    any|error f = e;
    boolean b1 = f !is error;
    boolean b2 = f !is MyError;

    e = error(ERR_REASON, errDetail = "error detail");
    f = e;
    boolean b3 = f !is error;
    boolean b4 = f !is MyError;
    return [b1, b2, b3, b4];
}

function testError_2() returns [boolean, boolean, boolean] {
    MyError e = error MyError(ERR_REASON, message = "detail message");
    any|error f = e;
    return [f !is MyError, f !is error, f !is MyErrorTwo];
}

function testClosedArrayAsOpenArray() returns boolean {
    (int|string)[3] a = [1, "hello world", 2];
    any b = a;
    return b !is anydata[] && b !is (int|string)[];
}

function testClosedArrayAsInvalidClosedArray() returns boolean {
    (int|string)[3] a = [1, "hello world", 2];
    any b = a;
    return !(b !is (int|string)[4]) || !(b !is (int|string)[2]);
}

function funcWithUnionParam(string|int s) returns string  {
    return "string";
}

function funcReturningString() returns string  {
    return "string";
}

function testFunctions1() returns [boolean, boolean, boolean, boolean] {
    any a = funcWithUnionParam;
    boolean b1 = a !is function(string s) returns string;
    boolean b2 = a !is function(int i) returns string;
    boolean b3 = a !is function(string|int) returns string;
    boolean b4 = a !is function(float f) returns string;
    return [b1, b2, b3, b4];
}

function testFunctions2() returns [boolean, boolean, boolean, boolean] {
    any a = funcReturningString;
    boolean b1 = a !is function() returns string|int;
    boolean b2 = a !is function() returns int;
    boolean b3 = a !is function() returns string;
    boolean b4 = a !is function() returns float;
    return [b1, b2, b3, b4];
}

function testFutureTrue() returns boolean {
    any a = start name();
    if !(a !is future<string>) {
        return true;
    } else {
        return false;
    }
}

function testFutureFalse() returns boolean {
    any a = start name();
    if !(a !is future<int>) {
        return true;
    } else {
        return false;
    }
}

function name() returns string {
    return "em";
}

type MarksWithOptionalPhysics record {|
    int physics?;
    int...;
|};

type MarksWithOptionalPhysicsAndFloatRestField record {|
    int physics?;
    float...;
|};

type MarksWithRequiredPhysics record {|
    int physics;
    int|float...;
|};

type OpenMarks record {|
    int|float...;
|};

type NoRestFieldMarks record {|
    int physics?;
|};

public function testMapAsRecord() {
    map<int> mp1 = {
        physics: 65,
        chemistry: 75
    };

    map<int> mp2 = {
        chemistry: 75,
        biology: 80
    };

    map<anydata> mp3 = {
        chemistry: 75,
        biology: 80
    };

    map<int|float> mp4 = {
        physics: 75,
        biology: 80.5
    };

    assertFalse(<any> mp1 !is MarksWithOptionalPhysics);
    assertFalse(<any> mp1 !is OpenMarks);
    assertTrue(<any> mp1 !is MarksWithRequiredPhysics);
    assertTrue(<any> mp1 !is NoRestFieldMarks);
    assertTrue(<any> mp1 !is MarksWithOptionalPhysicsAndFloatRestField);

    assertFalse(<any> mp2 !is MarksWithOptionalPhysics);
    assertFalse(<any> mp2 !is OpenMarks);
    assertTrue(<any> mp2 !is MarksWithRequiredPhysics);
    assertTrue(<any> mp2 !is NoRestFieldMarks);
    assertTrue(<any> mp2 !is MarksWithOptionalPhysicsAndFloatRestField);

    assertTrue(<any> mp3 !is MarksWithOptionalPhysics);
    assertTrue(<any> mp3 !is OpenMarks);
    assertTrue(<any> mp3 !is MarksWithRequiredPhysics);
    assertTrue(<any> mp3 !is NoRestFieldMarks);
    assertTrue(<any> mp3 !is MarksWithOptionalPhysicsAndFloatRestField);

    assertTrue(<any> mp4 !is MarksWithOptionalPhysics);
    assertFalse(<any> mp4 !is OpenMarks);
    assertTrue(<any> mp4 !is MarksWithRequiredPhysics);
    assertTrue(<any> mp4 !is NoRestFieldMarks);
    assertTrue(<any> mp4 !is MarksWithOptionalPhysicsAndFloatRestField);

    int|error x = 'int:fromString("foo");
    any det = ();
    if !(x !is error) {
        det = x.detail();
    }
    assertFalse(det !is map<anydata|readonly>);
    assertFalse(det !is 'error:Detail);
    assertFalse(det !is record {| string message; |});
}

// ========================== XML ==========================

public function testXMLNeverType() {
    string empty = "";
    'xml:Text a = xml `${empty}`;
    any y = a;
    assertEquality(y !is xml<never>, false);
    assertEquality(y !is xml, false);
    assertEquality(y !is 'xml:Text, false);
    assertEquality(y !is 'xml:Element, true);

    xml b = 'xml:createText("");
    any x = b;
    assertEquality(x !is xml<never>, false);
    assertEquality(x !is xml, false);
    assertEquality(x !is 'xml:Text, false);
    assertEquality(x !is 'xml:ProcessingInstruction, true);

    'xml:Text c = xml ``;
    any z = c;
    assertEquality(z !is xml<never>, false);
    assertEquality(z !is xml, false);
    assertEquality(z !is 'xml:Text, false);
    assertEquality(z !is 'xml:Comment, true);

    xml<never> d = xml ``;
    any w = d;
    assertEquality(w !is xml<never>, false);
    assertEquality(w !is xml, false);
    assertEquality(w !is 'xml:Text, false);
    assertEquality(w !is 'xml:Element, true);
    assertEquality(w !is xml<'xml:Text|'xml:Comment>, false);

    xml e = xml ``;
    assertEquality(<any> e !is byte, true);
    assertEquality(<any> e is xml<'xml:Element>, true);
    assertEquality(<any> e !is xml<'xml:Text>, false);
    assertEquality(<any> e !is xml, false);
    assertEquality(<any> e !is 'xml:Text, false);
    assertEquality(<any> e !is 'xml:Element, true);
    assertEquality(<any> e is xml<'xml:Element|'xml:Comment>, true);
}

function testXMLTextType(){
    'xml:Text t = xml `foo`;
    assertEquality(<any> t !is string, true);
}

function testRecordIntersections() {
    BazNTTE|int val = 11;
    assertTrue(val !is BarNTTE);

    BazNTTE|int val2 = {};
    assertTrue(val2 !is BarNTTE);

    BazNTTE|int val3 = <BarNTTE> {code: new};
    assertFalse(val3 !is BarNTTE);

    BarNTTE val4 = {code: new};
    assertTrue(val4 !is FooNTTE);

    BarNTTE val5 = <FooNTTE> {code: new, index: 0};
    assertFalse(val5 !is FooNTTE);

    OpenRecordWithIntFieldNTTE val6 = {i: 1, "s": "hello"};
    assertTrue(val6 !is record {| int i; string s; |});

    record {| int i; string s; |} v = {i: 2, s: "world"};
    OpenRecordWithIntFieldNTTE val7 = v;
    assertFalse(val7 !is record {| int i; string s; |});

    ClosedRecordWithIntFieldNTTE val8 = {i: 10};
    assertFalse(val8 is record {| byte i; |});
    assertFalse(<any> val8 !is record {});
    assertFalse(<any> val8 !is record {| int...; |});

    int|ClosedRecordWithIntFieldNTTE val9 = <record {| byte i; |}> {i: 10};
    assertFalse(val9 !is record {| byte i; |});
    assertFalse(val9 !is record {});
    assertFalse(val9 !is record {| int...; |});
}

type BazNTTE record {|
    anydata|object {}...;
|};

type BarNTTE record {
    readonly Class code = new;
};

readonly class Class {

}

type FooNTTE record {|
    readonly Class code;
    int index;
|};

type OpenRecordWithIntFieldNTTE record {
    int i;
};

type ClosedRecordWithIntFieldNTTE record {|
    int i;
|};

type RecordWithIntFieldAndNeverField record {|
    int i;
    never j?;
|};

type RecordWithIntFieldAndEffectivelyNeverField record {|
    int i;
    [never, int] j?;
|};

type OpenRecordWithIntFieldAndEffectivelyNeverRestField record {|
    int i;
    [never]...;
|};

function testRecordIntersectionWithEffectivelyNeverFields() {
    RecordWithIntFieldAndNeverField rec = {i: 1};
    assertFalse(rec !is ClosedRecordWithIntFieldNTTE);
    assertFalse(rec !is OpenRecordWithIntFieldAndEffectivelyNeverRestField);

    RecordWithIntFieldAndEffectivelyNeverField rec2 = {i: 1};
    assertFalse(rec2 !is ClosedRecordWithIntFieldNTTE);
    assertFalse(rec2 !is OpenRecordWithIntFieldAndEffectivelyNeverRestField);

    OpenRecordWithIntFieldAndEffectivelyNeverRestField rec3 = {i: 1};
    assertFalse(rec3 !is record {| int...; |});

    record {| int...; |} rec4 = {"i": 1};
    assertTrue(rec4 !is OpenRecordWithIntFieldAndEffectivelyNeverRestField);
    assertTrue(rec4 !is ClosedRecordWithIntFieldNTTE);
}

type Foo2 record {|
    function (int, int) returns int x?;
    boolean y?;
|};

function sum(int a, int b) returns int {
    return a + b;
}

function recordIntersectionWithFunctionFields() returns boolean {
    record {| function (int, int) returns int x; boolean y; int i?; |} rec = {x: sum, y: true};
    if (rec !is Foo2) {
        return false;
    }
    return true;
}

function testRecordIntersectionWithFunctionFields() {
    assertFalse(recordIntersectionWithFunctionFields());
}

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertFalse(anydata actual) {
    assertEquality(false, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
