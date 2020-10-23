// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    if (x is int) {
        return "int";
    } else {
        return "string";
    }
}

function testUnionTypeInUnion() returns string {
    int|string|float x = 5;
    if (x is int|float) {
        return "numeric";
    } else {
        return "string";
    }
}

function testNestedTypeCheck() returns [any, any, any] {
    return [bar(true), bar(1234), bar("hello")];
}

function bar (string | int | boolean i)  returns string {
    if (i is int){
        return "int";
    } else {
        if (i is string) {
            return "string";
        } else {
            return "boolean";
        }
    }
}

function testTypeInAny() returns (string) {
    any a = "This is working";
    if (a is string) {
        return "string value: " + <string> a;
    } else if(a is int) {
        return "int";
    } else {
        return "any";
    }
}

function testNilType() returns (string) {
    any a = ();
    if (a is string) {
        return "string";
    } else if(a is int) {
        return "int";
    } else if(a is ()) {
        return "nil";
    }else {
        return "any";
    }
}

function testTypeChecksWithLogicalAnd() returns string {
    int|string x = "hello";
    float|boolean y = true;
    if (x is int && y is float) {
        return "int and float";
    } else if (x is int && y is boolean) {
        return "int and boolean";
    } else if (x is string && y is float) {
        return "string and float";
    } else if (x is string && y is boolean) {
        return "string and boolean";
    } else {
        return "I don't know";
    }
}

function testTypeCheckInTernary() returns string {
    any a = 6;
    return a is string ? "A string" : a is int ? "An integer" : "Not an integer nor string";
}

// ========================== Records ==========================

type A1 record {
    int x = 0;
};

type B1 record {
    int x = 0;
    string y = "";
};

function testSimpleRecordTypes_1() returns string {
    A1 a1 = {};
    any a = a1;
     if (a is A1) {
        return "a is A1";
    } else if (a is B1) {
        return "a is B1";
    }

    return "n/a";
}

function testSimpleRecordTypes_2() returns [boolean, boolean] {
    B1 b = {};
    any a = b;
    return [a is A1, a is B1];
}

type A2 record {
    int x = 0;
};

type B2 record {
    int x = 0;
};

function testSimpleRecordTypes_3() returns [boolean, boolean] {
    B2 b = {};
    any a = b;
    return [a is A2, a is B2];
}

type Human record {
    string name;
    (function (int, string) returns string) | () foo = ();
};

type Man record {
    string name;
    (function (int, string) returns string) | () foo = ();
    int age = 0;
};

function testRecordsWithFunctionType_1() returns [string, string] {
    Human m = {name:"Piyal"};
    any a = m;
    string s1;
    string s2;
    
    if (a is Man) {
        s1 = "Man: " + m.name;
    } else {
        s1 = "a is not a man";
    }

    if (a is Human) {
        s2 = "Human: " + m.name;
    } else {
        s2 = "a is not a human";
    }

    return [s1, s2];
}

function testRecordsWithFunctionType_2() returns [string, string] {
    Man m = {name:"Piyal"};
    any a = m;
    string s1;
    string s2;
    
    if (a is Man) {
        s1 = "Man: " + m.name;
    } else {
        s1 = "a is not a man";
    }

    if (a is Human) {
        s2 = "Human: " + m.name;
    } else {
        s2 = "a is not a human";
    }

    return [s1, s2];
}

type X record {
    int p = 0;
    string q = "";
    A1 r = {};
};

type Y record {
    int p = 0;
    string q = "";
    B1 r = {};   // Assignable to A1. Hence Y is assignable to X.
};

function testNestedRecordTypes() returns [boolean, boolean] {
    Y y = {};
    any x = y;
    return [x is X, x is Y];
}

type A3 record {
    int x = 0;
};

type B3 record {|
    int x = 0;
|};

function testSealedRecordTypes() returns [boolean, boolean] {
    A3 a3 = {};
    any a = a3;
    return [a is A3, a is B3];
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

    if(a is SameAsPerson) {
        s1 = "I am same as person: " + a.getName();
    }

    if (a is Person) {
        s2 = "I am a person: " + a.getName();
    }

    if (b is SameAsPerson) {
        s3 = "I am same as person: " + b.getName();
    }

    if (b is Person) {
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

    if (a is PersonInOrder) {
        s1 = "I am a person in order: " + a.getName();
    }

    if (a is PersonNotInOrder) {
        s2 = "I am a person not in order: " + a.getName();
    }

    if (b is PersonInOrder) {
        s3 = "I am a person in order: " + b.getName();
    }

    if (b is PersonNotInOrder) {
        s4 = "I am a person not in order: " + b.getName();
    }

    return [s1, s2, s3, s4];
}

public type A4 object {
    public int p;
    public string q;
};

public type B4 object {
    public float r;
    *A4;
};

public class C4 {
    *B4;
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

    if(x is A4) {
        s1 = "values: " + x.p.toString() + ", " + x.q;
    }

    if (x is B4) {
        s2 = "values: " + x.p.toString() + ", " + x.q + ", " + x.r.toString();
    }

    if (x is Person) {   // shouldn't match
        s3 = "values: " + x.name + ", " + x.age.toString();
    }

    return [s1, s2, s3];
}

type A5 object {
    int p;
    string q;
};

type B5 object {
    float r;
    *A5;
};

class C5 {
    *B5;
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

    if(x is A5) {
        s1 = "values: " + x.p.toString() + ", " + x.q;
    }

    if (x is B5) {
        s2 = "values: " + x.p.toString() + ", " + x.q + ", " + x.r.toString();
    }

    if (x is Person) {   // shouldn't match
        s3 = "values: " + x.name + ", " + x.age.toString();
    }

    return [s1, s2, s3];
}

function testAnonymousObjectEquivalency() returns [string, string, string] {
    any x = new C4(5, "foo", 6.7, true);
    string s1 = "n/a";
    string s2 = "n/a";
    string s3 = "n/a";

    if(x is object { public float r; *A4; }) {
        s1 = "values: " + x.p.toString() + ", " + x.q + ", " + x.r.toString();
    }

    if(x is object {  public int p;  public string q;  public float r;  public boolean s;}) {
        s2 = "values: " + x.p.toString() + ", " + x.q + ", " + x.r.toString() + ", " + x.s.toString();
    }

    if(x is object { public int p;  public boolean q;  public float r;}) {  // shouldn't match
        s3 = "values: " + x.p.toString() + ", " + x.q.toString() + ", " + x.r.toString();
    }

    return [s1, s2, s3];
}

class Qux {
    Qux? fn;

    public function init(Qux? fn = ()) {
        self.fn = fn;
    }
}

class Quux {
    Quux? fn = ();
}

class Quuz {
    Quuz? fn = ();
    int i = 1;
}

class ABC {
    Qux f;
    string s;

    function init(Qux f, string s) {
        self.f = f;
        self.s = s;
    }
}

function testObjectIsCheckWithCycles() {
    Qux f1 = new;
    Qux f2 = new (f1);

    any a1 = <any> f1;
    assertTrue(a1 is Quux);
    assertFalse(a1 is Quuz);

    any a2 = <any> f2;
    assertTrue(a2 is Quux);
    assertFalse(a2 is Quuz);

    ABC ob = new (f2, "ballerina");

    any a3 = ob;
    assertTrue(a3 is object { Qux f; });
    assertFalse(a3 is object { Quuz f; });
}

// ========================== Arrays ==========================

function testSimpleArrays() returns [boolean, boolean, boolean, boolean, boolean] {
    int[] a = [1, 2, 3];
    int[][] b = [[1, 2, 3], [4, 5, 6]];
    any c = a;
    any d = b;
    return [(c is int[] && d is int[][]), c is float[], d is json, d is json[], d is json[][]];
}

function testRecordArrays() returns [boolean, boolean, boolean, boolean] {
    X[] a = [{}, {}];
    X[][] b = [[{}, {}], [{}, {}]];
    any c = a;
    any d = b;
    return [c is X[], d is X[][], c is Y[], d is Y[][]];
}

public function testUnionType() {
    [int, ()] x = [1, ()];
    any y = x;
    assertEquality(y is (string|int?)[2], true);
    assertEquality(y is (string|int?)[], true);
    assertEquality(y is (int|string?)[], true);
    assertEquality(y is (int|string?)[3], false);
    assertEquality(y is (int|string)[], false);
    assertEquality(y is int?[], true);
    assertEquality(y is int?[2], true);

    [int, string?] a = [1, "union"];
    any b = a;
    assertEquality(b is (int|string?)[], true);
    assertEquality(b is (int|string?)[2], true);
    assertEquality(b is (int?|string)[2], true);
    assertEquality(b is (int|string?)[1], false);
    assertEquality(b is int?[2], false);

    (int|string)[] c = [1, 2];
    any d = c;
    assertEquality(d is (int|string?)[], true);
    assertEquality(d is (int|string)[2], false);
    assertEquality(d is [int, string], false);
    assertEquality(d is [int, int], false);
    assertEquality(d is [string, string], false);
    assertEquality(d is [int, ()], false);
    assertEquality(d is [int...], false);
    assertEquality(d is [string...], false);
    assertEquality(d is [string, int...], false);
}

public function testClosedArrayType(){
      int[2] b = [1, 2];
      any y = b;
      assertEquality(y is [int, int], true);
      assertEquality(y is [int...], true);
      assertEquality(y is [int, int, int...], true);
      assertEquality(y is [string, string, int...], false);
      assertEquality(y is [int, int, int], false);
      assertEquality(y is [int, int, int, int...], false);
      assertEquality(y is [int, int, string...], true);
}

public function testInferredArrayType() {
    int[*] b = [1, 2];
    any y = b;
    assertEquality(y is [int, int], true);
    assertEquality(y is [int...], true);
    assertEquality(y is [int, int, int...], true);
    assertEquality(y is [string, string, int...], false);
    assertEquality(y is [int, int, int], false);
    assertEquality(y is [int, int, int, int...], false);
    assertEquality(y is [int, int, string...], true);
}

public function testEmptyArrayType() {
    var x = [];
    any a = x;
    assertEquality(a is int[2], false);
    assertEquality(a is int[], true);
    assertEquality(a is [int...], true);

    string[] sa = [];
    any arr = sa;
    assertEquality(arr is string[], true);
    assertEquality(arr is int[], false);

    int[0] ia = [];
    any iarr = ia;
    assertEquality(iarr is int[0], true);
    assertEquality(iarr is int[], true);

    int[] b = [1, 2];
    any c = b;
    assertEquality(c is [int...], true);
    assertEquality(c is int[], true);
}

// ========================== Tuples ==========================

function testSimpleTuples() returns [boolean, boolean, boolean, boolean, boolean] {
    [int, string] x = [4, "hello"];
    any y = x;

    boolean b0 = y is [int, string];
    boolean b1 = y is [int, boolean];
    boolean b2 = y is [float, boolean];
    boolean b3 = y is [any, any];
    boolean b4 = y is [json, json];

    return [b0, b1, b2, b3, b4];
}

function testTupleWithAssignableTypes_1() returns [boolean, boolean, boolean, boolean] {
    [X, Y] p = [{}, {}];
    any q = p;
    boolean b0 = q is [X, X];
    boolean b1 = q is [X, Y];
    boolean b2 = q is [Y, X];
    boolean b3 = q is [Y, Y];
    return [b0, b1, b2, b3];
}

function testTupleWithAssignableTypes_2() returns boolean {
    [Y, Y] p = [{}, {}];
    [X, Y] q = p;
    boolean b1 = q is [Y, Y];
    return q is [Y, Y];
}

public function testRestType() {
    [int...] x = [1, 2];
    any y = x;
    assertEquality(y is string[], false);
    assertEquality(y is [int...], true);
    assertEquality(y is int[0], false);
    assertEquality(y is int[], true);
    assertEquality(y is [int], false);
    assertEquality(y is int[2], false);

    [int, int, int...] a = [1, 2];
    any b = a;
    assertEquality(b is int[2], false);
    assertEquality(b is [int, int...], true);
    assertEquality(b is [int...], true);
}

// ========================== Map ==========================

function testSimpleUnconstrainedMap_1() returns [boolean, boolean] {
    map<any> m = {"key1": "value1"};
    boolean b0 = m is map<string>;
    boolean b1 = m is map<json>;
    return [b0, b1];
}

function testSimpleUnconstrainedMap_2() returns [boolean, boolean, boolean, boolean, boolean] {
    map<any> m = {"key1": "value1"};
    any a = m;
    boolean b0 = a is map<any>;
    boolean b1 = a is map<any>;
    boolean b2 = a is map<string>;
    boolean b3 = a is json;
    boolean b4 = a is map<json>;
    return [b0, b1, b2, b3, b4];
}

function testSimpleConstrainedMap() returns [boolean, boolean, boolean, boolean] {
    map<string> m1 = {"key1": "value1"};
    any m2 = m1;
    boolean b0 = m2 is map<any>;
    boolean b1 = m2 is map<any>;
    boolean b2 = m2 is map<string>;
    boolean b3 = m2 is map<json>;
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
    if (j is string) {
        return "json string: " + j;
    } else if(j is int) {
        return "json int";
    } else if(j is float) {
        return "json float";
    } else if(j is boolean) {
        return "json boolean";
    } else if(j is ()) {
        return "json null";
    } else if(j is json[]) {
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
    boolean b0 = x is int[];
    boolean b1 = y is int[][];
    boolean b3 = z is int[][];

    return [b0, b1, b3];
}

// ========================== Finite type ==========================

type State "on"|"off";

function testFiniteType() returns [boolean, boolean, boolean] {
    State a = "on";
    any b = a;
    any c = "off";
    any d = "hello";

    return [b is State, c is State, d is State];
}

function testFiniteTypeInTuple() returns [boolean, boolean, boolean, boolean] {
    [State, string] x = ["on", "off"];
    any y = x;
    
    boolean b0 = y is [State, State];
    boolean b1 = y is [State, string];
    boolean b2 = y is [string, State];
    boolean b3 = y is [string, string];

    return [b0, b1, b2, b3];
}

function testFiniteTypeInTuplePoisoning() returns [State, State] {
    [State, string] x = ["on", "off"];
    any y = x;
    [State, State] z = ["on", "on"];
    
    if (y is [State, State]) {
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
    if (a is Fruit) {
        return "a is a fruit";
    }

    return "a is not a fruit";
}

function testFiniteType_2() returns string {
    any a = APPLE;
    if (a is APPLE) {
        return "a is an Apple";
    }

    return "a is not an Apple";
}

function testFiniteTypeAsBroaderType_1() returns boolean {
    any a = GRAPE;
    return a is string;
}

type FRUIT_OR_COUNT "apple"|2|"grape"|10;

function testFiniteTypeAsBroaderType_2() returns [boolean, boolean] {
    FRUIT_OR_COUNT fc1 = GRAPE;
    FRUIT_OR_COUNT fc2 = 10;

    return [fc1 is string, fc2 is int];
}

type FooBarOneBoolean "foo"|"bar"|1|boolean;
type FooBarBaz "foo"|"bar"|"baz";
type FalseFooThree false|"foo"|3;
type IntTwo int|2.0;

function testUnionWithFiniteTypeAsFiniteTypeTrue() returns [boolean, boolean] {
    FooBarOneBoolean f1 = "foo";
    FooBarOneBoolean f2 = 1;

    return [f1 is FooBarBaz, f2 is IntTwo];
}

function testUnionWithFiniteTypeAsFiniteTypeFalse() returns [boolean, boolean] {
    FooBarOneBoolean f1 = "foo";
    FooBarOneBoolean f2 = 1;

    return [f1 is IntTwo, f2 is FooBarBaz];
}

function testFiniteTypeAsFiniteTypeTrue() returns boolean {
    FooBarBaz f1 = "foo";
    return f1 is FalseFooThree;
}

function testFiniteTypeAsFiniteTypeFalse() returns boolean {
    FooBarBaz f1 = "bar";
    return f1 is FalseFooThree;
}

function testIntersectingUnionTrue() returns [boolean, boolean] {
    string|int|typedesc<any> x = 1;
    return [x is int|boolean, x is json];
}

function testIntersectingUnionFalse() returns [boolean, boolean] {
    string|int|typedesc<any> x = int;
    return [x is int|boolean, x is anydata];
}

function testValueTypeAsFiniteTypeTrue() returns [boolean, boolean] {
    string s = "orange";
    float f = 2.0;
    return [s is Fruit, f is IntTwo];
}

function testValueTypeAsFiniteTypeFalse() returns [boolean, boolean] {
    string s = "mango";
    float f = 12.0;
    return [s is Fruit, f is IntTwo];
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
    boolean b1 = f is error;
    boolean b2 = f is MyError;

    e = error(ERR_REASON, errDetail = "error detail");
    f = e;
    boolean b3 = f is error;
    boolean b4 = f is MyError;
    return [b1, b2, b3, b4];
}

function testError_2() returns [boolean, boolean, boolean] {
    MyError e = MyError(ERR_REASON, message = "detail message");
    any|error f = e;
    return [f is MyError, f is error, f is MyErrorTwo];
}

function testClosedArrayAsOpenArray() returns boolean {
    (int|string)[3] a = [1, "hello world", 2];
    any b = a;
    return b is anydata[] && b is (int|string)[];
}

function testClosedArrayAsInvalidClosedArray() returns boolean {
    (int|string)[3] a = [1, "hello world", 2];
    any b = a;
    return b is (int|string)[4] || b is (int|string)[2];
}

function funcWithUnionParam(string|int s) returns string  {
    return "string";
}

function funcReturningString() returns string  {
    return "string";
}

function testFunctions1() returns [boolean, boolean, boolean, boolean] {
    any a = funcWithUnionParam;
    boolean b1 = a is function(string s) returns string;
    boolean b2 = a is function(int i) returns string;
    boolean b3 = a is function(string|int) returns string;
    boolean b4 = a is function(float f) returns string;
    return [b1, b2, b3, b4];
}

function testFunctions2() returns [boolean, boolean, boolean, boolean] {
    any a = funcReturningString;
    boolean b1 = a is function() returns string|int;
    boolean b2 = a is function() returns int;
    boolean b3 = a is function() returns string;
    boolean b4 = a is function() returns float;
    return [b1, b2, b3, b4];
}

function testFutureTrue() returns boolean {
    any a = start name();
    if (a is future<string>) {
        return true;
    } else {
        return false;
    }
}

function testFutureFalse() returns boolean {
    any a = start name();
    if (a is future<int>) {
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

    assertTrue(<any> mp1 is MarksWithOptionalPhysics);
    assertTrue(<any> mp1 is OpenMarks);
    assertFalse(<any> mp1 is MarksWithRequiredPhysics);
    assertFalse(<any> mp1 is NoRestFieldMarks);
    assertFalse(<any> mp1 is MarksWithOptionalPhysicsAndFloatRestField);

    assertTrue(<any> mp2 is MarksWithOptionalPhysics);
    assertTrue(<any> mp2 is OpenMarks);
    assertFalse(<any> mp2 is MarksWithRequiredPhysics);
    assertFalse(<any> mp2 is NoRestFieldMarks);
    assertFalse(<any> mp2 is MarksWithOptionalPhysicsAndFloatRestField);

    assertFalse(<any> mp3 is MarksWithOptionalPhysics);
    assertFalse(<any> mp3 is OpenMarks);
    assertFalse(<any> mp3 is MarksWithRequiredPhysics);
    assertFalse(<any> mp3 is NoRestFieldMarks);
    assertFalse(<any> mp3 is MarksWithOptionalPhysicsAndFloatRestField);

    assertFalse(<any> mp4 is MarksWithOptionalPhysics);
    assertTrue(<any> mp4 is OpenMarks);
    assertFalse(<any> mp4 is MarksWithRequiredPhysics);
    assertFalse(<any> mp4 is NoRestFieldMarks);
    assertFalse(<any> mp4 is MarksWithOptionalPhysicsAndFloatRestField);

    int|error x = 'int:fromString("foo");
    any det = ();
    if x is error {
        det = x.detail();
    }
    assertTrue(det is map<anydata|readonly>);
    assertTrue(det is 'error:Detail);
    assertFalse(det is record {| string message; |});
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
