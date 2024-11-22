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

import ballerina/lang.'value;
import ballerina/test;
// ========================== Basics ==========================


function testValueTypeInUnion() returns string {
    int|string x = 5;
    if (x is int) {
        int y = x;
        return "int: " + y.toString();
    } else {
        return "string";
    }
}

type A record {
    string a;
};

type B record {
    string b;
    string c;
};

function testSimpleRecordTypes_1() returns string {
    A x = {a:"foo"};
    any y = x;
     if (y is A) {
        return y.a;
    } else if (y is B) {
        return y.b + "-" + y.c;
    }

    return "n/a";
}

function testSimpleRecordTypes_2() returns string {
    B x = {b:"foo", c:"bar"};
    any y = x;
     if (y is A) {
        return y.a;
    } else if (y is B) {
        return y.b + "-" + y.c;
    }

    return "n/a";
}

function testSimpleTernary() returns string {
    any a = "hello";
    return a is string ? a : "not a string";
}

function testMultipleTypeGuardsWithAndOperator() returns int {
    int|string x = 5;
    any y = 7;
    if (x is int && y is int) {
        return x + y;
    } else {
        return -1;
    }
}

function testMultipleTypeGuardsWithAndOperatorInTernary() returns int {
    int|string x = 5;
    any y = 7;
    return (x is int && y is int) ? x + y  : -1;
}

function testTypeGuardInElse_1() returns string {
    int|string x = 5;
    if (x is int) {
        int y = x;
        return "int: " + y.toString();
    } else {
        return "string";
    }
}

function testTypeGuardInElse_2() returns string {
    int|string|float|boolean x = true;
    if (x is int) {
        int y = x;
        return "int: " + y.toString();
    } else if (x is string) {
        return "string: " + x;
    } else if (x is float) {
        float y = x;
        return "float: " + y.toString();
    } else {
        boolean b = x;
        return "boolean: " + b.toString();
    }
}

function testTypeGuardInElse_3() returns string {
    int|string|float|boolean x = true;
    int|string|float|boolean y = false;
    if (x is int|string) {
        if (y is string) {
            return "y is string: " + y;
        } else if (y is int) {
            int i = y;
            return "y is float: " + i.toString();
        } else {
            return "x is int|string";
        }
    } else if (x is float) {
        float f = x;
        return "float: " + f.toString();
    } else {
        if (y is int) {
            int i = y;
            return "x is boolean and y is int: " + i.toString();
        } else if (y is string) {
            return "x is boolean and y is string: " + y;
        } else if (y is float) {
            float f = y;
            return "x is boolean and y is float: " + f.toString();
        } else {
            boolean b = y;
            return "x is boolean and y is boolean: " + b.toString();
        }
    }
}

function testTypeGuardInElse_4() returns string {
    int|string|float|boolean x = true;
    int|string|float|boolean y = false;
    string val = "1st round: ";
    if (x is int|string) {
        if (y is string) {
            val += "y is string: " + y;
        } else if (y is int) {
            int i = y;
            val += "y is float: " + i.toString();
        } else {
            val += "x is int|string";
        }
    } else if (x is float) {
        float f = x;
        val += "float: " + f.toString();
    } else {
        if (y is int) {
            int i = y;
            val += "x is boolean and y is int: " + i.toString();
        } else if (y is string) {
            val += "x is boolean and y is string: " + y;
        } else if (y is float) {
            float f = y;
            val += "x is boolean and y is float: " + f.toString();
        } else {
            boolean b = y;
            val += "x is boolean and y is boolean: " + b.toString();
        }
    }

    val += " | 2nd round: ";
    if (x is int|string) {
        if (y is string) {
            val += "y is string: " + y;
        } else if (y is int) {
            int i = y;
            val += "y is float: " + i.toString();
        } else {
            val += "x is int|string";
        }
    } else if (x is float) {
        float f = x;
        val += "float: " + f.toString();
    } else {
        if (y is int) {
            int i = y;
            val += "x is boolean and y is int: " + i.toString();
        } else if (y is string) {
            val += "x is boolean and y is string: " + y;
        } else if (y is float) {
            float f = y;
            val += "x is boolean and y is float: " + f.toString();
        } else {
            boolean b = y;
            val += "x is boolean and y is boolean: " + b.toString();
        }
    }

    return val;
}

function testTypeGuardInElse_5() returns string {
    int|string|float|boolean x = 5;
    if (x is int|string) {
        if (x is string) {
            return "x is string: " + x;
        } else {
            int i = x;
            return "x is int: " + i.toString();
        }
    } else if (x is float) {
        float f = x;
        return "float: " + f.toString();
    } else {
        return "x is boolean: " + x.toString();
    }
}

function testTypeGuardInElse_6() returns string {
    int|string|table<record {}> x = 5;
    if (x is table<record {}>) {
        table<record {}> t = x;
        return "table";
    } else {
        int|string y = x;
        if (y is string) {
            string s = y;
            return "string: " + y;
        } else {
            int i = y;
            return "int: " + i.toString();
        }
    }
}

function testTypeGuardInElse_7() returns string {
    int|string|table<A> x = 5;
    if (x is table<A>) {
        table<A> t = x;
        return "table";
    } else {
        int|string y = x;
        if (y is string) {
            string s = y;
            return "string: " + y;
        } else {
            int i = y;
            return "int: " + i.toString();
        }
    }
}

function testComplexTernary_1() returns string {
    int|string|float|boolean|int[] x = "string";
    return x is int ? "int" : (x is float ? "float" : (x is boolean ? "boolean" : (x is int[] ? "int[]" : x)));
}

function testComplexTernary_2() returns string {
    int|string|float|boolean|xml x = "string";
    if(x is boolean) {return "boolean";}
    if (x is int|string|float|boolean) {
        return x is int ? "int" : (x is float ? "float" : x);
    } else {
        xml y = x;
        return "xml";
    }
}

function testArray() returns int {
    int [] intArr = [10, 20];
    any[] arr = intArr;
    if (arr is int[]) {
        return arr[1];
    } else {
        return -1;
    }
}

function testUpdatingGuardedVar_1() returns string {
    any value = "BALLERINA";
    if (value is int|string|float) {
        if (value is string) {
         value = value + " - updated";
        } else {
            return "an int or float";
        }
    } else {
        return "some other type";
    }

    return value.toString();
}

function testUpdatingGuardedVar_2() returns string {
    any value = "BALLERINA";
    if (!(value is int|string|float)) {
        return "some other type";
    } else {
        if (value is string) {
            value = value + " - updated once";
            value = getUpdatedString(<string> value);
        } else {
            return "an int or float";
        }
    }

    return value.toString();
}

function getUpdatedString(string s) returns string {
    return s + " - updated via function";
}

type func function() returns boolean;
int fPtrFlag = 0;

function testFuncPtrTypeInferenceInElseGuard() returns [boolean, int] {
    func? f = function () returns boolean {
        fPtrFlag = 100;
        return true;
    };

    if (f is ()) {
        return [false, fPtrFlag];
    } else {
        return [f(), fPtrFlag];
    }
}

function testTypeGuardNegation(int|string|boolean x) returns string {
    if!(x is int) {
        if !(x is string) {
            boolean y = x;
            return "boolean: " + y.toString();
        } else {
            string y = x;
            return "string: " + y;
        }
    } else {
        int y = x;
        return "int: " + x.toString();
    }
}

function testTypeGuardsWithBinaryOps(int|string|boolean|float x) returns string {
    if ((x is int|string && x is int) || (x is boolean)) {
        if (x is boolean) {
            boolean y = x;
            return "boolean: " + y.toString();
        } else {
            int y = x;
            return "int: " + y.toString();
        }
    } else {
        if (x is float) {
            float y = x;
            return "float: " + y.toString();
        } else {
            string y = x;
            return "string: " + y;
        }
    }
}

type Person record {
    string name;
    int age;
};

type Student record {
    string name;
    int age;
    float gpa;
};

function testTypeGuardsWithRecords_1() returns string {
    Student s = {name:"John", age:20, gpa:3.5};
    Person|Student|string x = s;

    if (x is Person) {
        Person y = x;
        return y.name;
    } else {
        string y = x;
        return y;
    }
}

function testTypeGuardsWithRecords_2() returns string {
    Student s = {name:"John", age:20, gpa:3.5};
    Person|Student|string x = s;
    if (x is Student) {
        Student y = x;
        return "student: " + y.name;
    } else if (x is Person) {
        Person y = x;
        return "person: " + y.name;
    } else {
        string y = x;
        return y;
    }
}

public type CustomError error<record { int status = 500; string message?; error cause?; }>;

function testTypeGuardsWithError() returns string {
    CustomError err = error CustomError("some error");
    any|error e = err;
    if (e is error) {
        if (e is CustomError) {
            CustomError ce = e;
            return "status: " + ce.detail().status.toString();
        } else {
            return "not a custom error";
        }
    } else {
        return "not an error";
    }
}

function testTypeGuardsWithErrorInmatch() returns string {
    error e = error("some error");
    any|error x = e;
    match x {
        var p if p is error => {return string `${p.message()}`;}
        var p => {return "Internal server error";}
    }
}

function testTypeNarrowingWithClosures() returns string {
    int|string x = 8;
    if (x is string) {
        return "string: "+ x;
    } else {
        var y = function() returns int {
                    if (x is int) {
                        return <int>x;
                    } else {
                        return -1;
                    }
                };
        return "int: "+ y().toString();
    }
}

function testTypeGuardsWithBinaryAnd(string|int x) returns string {
    if (x is int && x < 5) {
        return "int: " + x.toString() + " is < 5";
    } else if (x is int) {
        return "int: " + x.toString() + " is >= 5";
    } else {
        return "string: " + x;
    }
}

function testTypeGuardsWithBinaryOpsInTernary(int|string|boolean|float x) returns string {
    return ((x is int|string && x is int) || (x is boolean)) ?
            (x is boolean ? booleanToString(x) : intToString(x)) :
            (x is float ? floatToString(x) : "string: " + x);
}

function booleanToString(boolean a) returns string {
    return "boolean: " + a.toString();
}

function intToString(int a) returns string {
    return "int: " + a.toString();
}

function floatToString(float a) returns string {
    return "float: " + a.toString();
}

public function testUpdatingTypeNarrowedVar_1() returns string {
    int|string|boolean x = 5;
    if (x is int|boolean) {
        x = "hello";   // update the var with a type outside of narrowed types
        if (x is int) {
            int z = x;
            return "int: " + z.toString();
        } else if (x is string) {
            string z = x;
            return "string: " + z;
        } else {
            boolean z = x;
            return "boolean: " + z.toString();
        }
    } else {
        string z = x;
        return "outer string: " + z;
    }
}

public function testUpdatingTypeNarrowedVar_3() returns string {
    int|string|boolean x = 5;
    if (x is int|boolean) {
        if (x is int) {
            x = "hello";   // update the var with a type outside of narrowed types
        }

        if (x is int) {
            int z = x;
            return "int: " + z.toString();
        } else if (x is string) {
            string z = x;
            return "string: " + z;
        } else {
            boolean z = x;
            return "boolean: " + z.toString();
        }
    } else {
        string z = x;
        return "outer string: " + z.toString();
    }
}

error e1 = error("e1");
error e2 = error("e2");
error? errorW1 = e1;
error? errorW2 = e2;

function testTypeGuardForGlobalVarsUsingLocalAssignment() returns [string, string?] {
    string w1ErrMsg = "";
    string? w2ErrMsg = "";
    if (errorW1 is error) {
        error? e3 = errorW1;
        if (e3 is error) {
            w1ErrMsg = e3.message();
        }
    }
    if (errorW2 is error) {
        error? e4 = errorW2;
        if (e4 is error) {
            w2ErrMsg = e4.message();
        }
    }
    return [w1ErrMsg, w2ErrMsg];
}

type FooBarOneTwoTrue "foo"|"bar"|1|2.0|true;
type FooBar "foo"|"bar";
type OneTwo 1|2.0;

function testFiniteTypeAsBroaderTypes_1() returns boolean {
    FooBarOneTwoTrue f = "foo";
    boolean 'equals = finiteTypeAsBroaderTypesHelper(f) == "string: foo";

    f = "bar";
    return 'equals && finiteTypeAsBroaderTypesHelper(f) == "string: bar";
}

function testFiniteTypeAsBroaderTypes_2() returns boolean {
    FooBarOneTwoTrue f = 1;
    return finiteTypeAsBroaderTypesHelper(f) == "int: 1";
}

function testFiniteTypeAsBroaderTypes_3() returns boolean {
    FooBarOneTwoTrue f = 2.0;
    return finiteTypeAsBroaderTypesHelper(f) == "float: 2.0";
}

function testFiniteTypeAsBroaderTypes_4() returns boolean {
    FooBarOneTwoTrue f = true;
    return finiteTypeAsBroaderTypesHelper(f) == "boolean: true";
}

function finiteTypeAsBroaderTypesHelper(FooBarOneTwoTrue f) returns string {
    if (f is string) {
        match f {
            "foo" => {return "string: foo";}
            "bar" => {return "string: bar";}
        }
        return "expected foo or bar!";
    } else {
        if (f is int|float) {
            int|float ot = f;
            if (ot is int) {
                int i = ot;
                return string `int: ${i}`;
            } else {
                float fl = ot;
                return string `float: ${fl}`;
            }
        } else {
            boolean b = f;
            return string `boolean: ${b}`;
        }
    }
}

function testFiniteTypeAsBroaderTypesAndFiniteType_1() returns boolean {
    FooBarOneTwoTrue f = "foo";
    boolean 'equals = finiteTypeAsBroaderTypesAndFiniteTypeHelper(f) == "string: foo";

    f = "bar";
    return 'equals && finiteTypeAsBroaderTypesAndFiniteTypeHelper(f) == "string: bar";
}

function testFiniteTypeAsBroaderTypesAndFiniteType_2() returns boolean {
    FooBarOneTwoTrue f = 1;
    return finiteTypeAsBroaderTypesAndFiniteTypeHelper(f) == "int: 1";
}

function testFiniteTypeAsBroaderTypesAndFiniteType_3() returns boolean {
    FooBarOneTwoTrue f = 2.0;
    return finiteTypeAsBroaderTypesAndFiniteTypeHelper(f) == "float: 2.0";
}

function testFiniteTypeAsBroaderTypesAndFiniteType_4() returns boolean {
    FooBarOneTwoTrue f = true;
    return finiteTypeAsBroaderTypesAndFiniteTypeHelper(f) == "boolean: true";
}

function finiteTypeAsBroaderTypesAndFiniteTypeHelper(FooBarOneTwoTrue f) returns string {
    if (f is string) {
        FooBar fb = f;
        match fb {
            "foo" => {return "string: foo";}
            "bar" => {return "string: bar";}
        }
        return "expected foo or bar!";
    } else {
        if (f is OneTwo) {
            OneTwo ot = f;
            if (ot is int) {
                int i = ot;
                return string `int: ${i}`;
            } else {
                float fl = ot;
                return string `float: ${fl}`;
            }
        } else {
            boolean b = f;
            return string `boolean: ${b}`;
        }
    }
}

type FooBarOneTwoBoolean "foo"|"bar"|1|2.0|boolean;
type FooBarBaz "foo"|"bar"|"baz";
type FooBarInt "foo"|"bar"|int;

function testFiniteTypeInUnionAsComplexFiniteTypes_1() returns boolean {
    FooBarOneTwoBoolean f = "foo";
    [string, FooBarBaz|OneTwo|boolean] [s, v] = finiteTypeAsComplexFiniteTypesHelperOne(f);
    return s == "FooBarBaz" && f == v;
}

function testFiniteTypeInUnionAsComplexFiniteTypes_2() returns boolean {
    FooBarOneTwoBoolean f = 2.0;
    [string, FooBarBaz|OneTwo|boolean] [s, v] = finiteTypeAsComplexFiniteTypesHelperOne(f);
    return s == "OneTwo" && f == v;
}

function testFiniteTypeInUnionAsComplexFiniteTypes_3() returns boolean {
    FooBarOneTwoBoolean f = true;
    [string, FooBarBaz|OneTwo|boolean] [s, v] = finiteTypeAsComplexFiniteTypesHelperOne(f);
    return s == "boolean" && f == v;
}

function testFiniteTypeInUnionAsComplexFiniteTypes_4() returns boolean {
    FooBarOneTwoBoolean f = "bar";
    [string, FooBarInt|OneTwo|boolean] [s, v] = finiteTypeAsComplexFiniteTypesHelperTwo(f);
    return s == "FooBarInt" && f == v;
}

function testFiniteTypeInUnionAsComplexFiniteTypes_5() returns boolean {
    FooBarOneTwoBoolean f = 1;
    [string, FooBarInt|OneTwo|boolean] [s, v] = finiteTypeAsComplexFiniteTypesHelperTwo(f);
    return s == "FooBarInt" && f == v;
}

function testFiniteTypeInUnionAsComplexFiniteTypes_6() returns boolean {
    FooBarOneTwoBoolean f = 2.0;
    [string, FooBarInt|OneTwo|boolean] [s, v] = finiteTypeAsComplexFiniteTypesHelperTwo(f);
    return s == "OneTwo" && f == v;
}

function testFiniteTypeInUnionAsComplexFiniteTypes_7() returns boolean {
    FooBarOneTwoBoolean f = false;
    [string, FooBarInt|OneTwo|boolean] [s, v] = finiteTypeAsComplexFiniteTypesHelperTwo(f);
    return s == "boolean" && f == v;
}

function finiteTypeAsComplexFiniteTypesHelperOne(FooBarOneTwoBoolean f) returns [string, FooBarBaz|OneTwo|boolean] {
    if (f is FooBarBaz) {
        FooBarBaz x = f;
        return ["FooBarBaz", x];
    } else {
        if (f is OneTwo) {
            OneTwo x = f;
            return ["OneTwo", x];
        } else {
            boolean x = f;
            return ["boolean", x];
        }
    }
}

function finiteTypeAsComplexFiniteTypesHelperTwo(FooBarOneTwoBoolean f) returns [string, FooBarInt|OneTwo|boolean] {
    if (f is FooBarInt) {
        FooBarInt x = f;
        return ["FooBarInt", x];
    } else {
        if (f is OneTwo) {
            OneTwo x = f;
            return ["OneTwo", x];
        } else {
            boolean x = f;
            return ["boolean", x];
        }
    }
}

type TrueBazOne true|"baz"|1;
type TrueOneBarFoo true|1|"bar"|"foo";
type OneTrue 1|true;
type Baz "baz";

function testFiniteTypeAsFiniteTypeWithIntersectionPositive() returns boolean {
    TrueBazOne f = 1;
    if (f is TrueOneBarFoo) {
        OneTrue g = f;
        return f == g;
    }
    return false;
}

function testFiniteTypeAsFiniteTypeWithIntersectionNegative() returns boolean {
    TrueBazOne f = "baz";
    if (f is TrueOneBarFoo) {
        return false;
    } else {
        Baz b = f; // Would have matched to `TrueOneBarFoo` if `true` or `1`
        return b == f;
    }
}

function testTypeNarrowingForIntersectingDirectUnion_1() returns boolean {
    string s = "hello world";
    string|typedesc<any> st = s;
    if (st is string|boolean) {
        string s2 = st;
        return s2 == s;
    }
    return false;
}

type CyclicComplexUnion int|CyclicComplexUnion[]|object {};
type CyclicFloatUnion float|CyclicComplexUnion[]|object {};

function testTypeNarrowingForIntersectingCyclicUnion() returns boolean {
    CyclicComplexUnion s = 1;
    anydata ma = <anydata> s;
    float|CyclicComplexUnion m = <CyclicComplexUnion>ma;
    if (m is CyclicComplexUnion|string) {
        CyclicComplexUnion f2 = m;
        return f2 === s;
    }
    return false;
}

function testTypeNarrowingForIntersectingCyclicUnionNegative() returns boolean {
    CyclicComplexUnion s = 1;
    anydata ma = <anydata> s;
    float|CyclicComplexUnion m = <CyclicComplexUnion>ma;
    if (m is CyclicFloatUnion|string) {
        return false;
    }
    return true;
}

function testTypeNarrowingForIntersectingDirectUnion_2() returns boolean {
    xml x = xml `Hello World`;
    string|xml st = x;
    if (st is string|boolean) {
        return true;
    } else {
        xml t2 = st;
        return t2 == x;
    }
}

function testTypeNarrowingForIntersectingAssignableUnion_1() returns boolean {
    string s = "hello world";
    string|typedesc<any> st = s;
    if (st is json|xml) {
        string s2 = st;
        return s2 == s;
    }
    return false;
}

function testTypeNarrowingForIntersectingAssignableUnion_2() returns boolean {
    record{} t = { "name": "Maryam" };
    string|record{} st = t;
    if (st is json|xml) {
        return false;
    } else {
        record{} t2 = st;
        return t2 == t;
    }
}

function testTypeNarrowingForValueTypeAsFiniteType_1() returns boolean {
    string s = "bar";
    if (s is FooBar) {
        FooBar f = s;
        return f == s;
    } else {
        string s2 = s;
        return false;
    }
}

function testTypeNarrowingForValueTypeAsFiniteType_2() returns boolean {
    float f = 11.0;
    if (f is FooBarOneTwoBoolean) {
        FooBarOneTwoBoolean f2 = f;
        return true;
    } else {
        float f3 = f;
        return f == f3;
    }
}

const FIVE = 5.0;
type FooBarTen "foo"|"bar"|10;
type FloatFive FIVE;
type IntTen 10;

function testFiniteTypeAsBroaderTypeInStructurePositive() returns boolean {
    FooBarTen f = "bar";
    [FooBarTen, FloatFive, boolean] g = [f, FIVE, true];
    any a = g;
    if (a is [string|int|xml, float, boolean]) {
        return a === g;
    }
    return false;
}

function testFiniteTypeAsBroaderTypeInStructureNegative() returns boolean {
    FooBarTen f = "bar";
    [string|float|int, FloatFive, boolean] g = [f, FIVE, true];
    any a = g;
    if (a is [string|int|xml, float, boolean]) {
        return true;
    }
    return false;
}

function testFiniteTypeReassignmentToBroaderType() returns boolean {
    int i = 10;
    if (i is FooBarTen) {
        IntTen j = i; // assignment should not fail
        int k = i;
        return i == k;
    }
    return false;
}

type Foo "foo";
type Bar "bar";

type X "x";
type Y "y";

function testFiniteTypeUnionAsFiniteTypeUnionPositive() returns boolean {
    Foo|Bar|X|int q = 1;
    if (q is Foo|Bar|Y|int) {
        Foo|Bar|int w = q;
        return q == w;
    }
    return false;
}

function testFiniteTypeUnionAsFiniteTypeUnionNegative() returns boolean {
    Foo|Bar|X|int q = "x";
    if (q is Foo|Bar|int|Y) {
        return true;
    }
    return q != "x";
}

string reason = "error reason";
map<value:Cloneable> detail = { code: 11, detail: "detail message" };

function testTypeGuardForErrorPositive() returns boolean {
    any|error a1 = <error> error(reason);
    any|error a2 = <error> error(reason, code = 11, detail = "detail message");
    return errorGuardHelper(a1, a2);
}

function testTypeGuardForErrorNegative() returns boolean {
    return errorGuardHelper("hello world", 1);
}

function errorGuardHelper(any|error a1, any|error a2) returns boolean {
    if (a1 is error && a2 is error) {
        error e3 = a1;
        error e4 = a2;

        map<value:Cloneable> m = <map<value:Cloneable>> e4.detail();
        return e3.message() == reason && e4.message() === reason
            && m["code"] === detail["code"] && m["detail"] === detail["detail"];
    }
    return false;
}

const ERR_REASON = "error reason";
const ERR_REASON_TWO = "error reason two";

type Details record {
    string message;
    error cause?;
};

type MyError distinct error<Details>;
type MyErrorTwo distinct error<Details>;

function testTypeGuardForCustomErrorPositive() returns [boolean, boolean] {
    Details d = { message: "detail message" };
    MyError e3 = error MyError(ERR_REASON, message = d.message);
    MyErrorTwo e4 = error MyErrorTwo(ERR_REASON_TWO, message = "detail message");

    any|error a1 = e3;
    any|error a2 = e4;

    boolean isSpecificError = false;

    if (a1 is MyError && a2 is MyErrorTwo) {
        MyError e5 = a1;
        MyErrorTwo e6 = a2;

        Details m1 = e5.detail();
        Details m2 = e6.detail();
        isSpecificError = e5.message() == ERR_REASON && e6.message() == ERR_REASON_TWO && m1.message == m2.message;
    }

    boolean isGenericError = a1 is error && a2 is error;
    return [isSpecificError, isGenericError];
}

function testCustomErrorType() {
    Details d = {message: "detail message"};
    MyError|MyErrorTwo e = error MyError(ERR_REASON, message = d.message);
    test:assertFalse(e is MyErrorTwo);
    test:assertTrue(e is MyError);

    MyErrorTwo|MyError e1 = error MyError(ERR_REASON, message = d.message);
    test:assertFalse(e1 is MyErrorTwo);
    test:assertTrue(e1 is MyError);
}

function testTypeGuardForCustomErrorNegative() returns boolean {
    error e3 = error(ERR_REASON);
    error e4 = error("error reason x", message = "detail message");

    any|error a1 = e3;
    any|error a2 = e4;

    if (a1 is MyError || a2 is MyErrorTwo) {
        return true;
    } else {
        any|error a3 = a1;
        any|error a4 = a2;
        return false;
    }
}

function testTypeGuardForTupleDestructuringAssignmentPositive() returns boolean {
    [string?, int] [s, i] = tupleReturningFunc("str");
    if (s is string) {
        string strVal = s;
        [s, i] = tupleReturningFunc(());
        return s is ();
    }

    return false;
}

function testTypeGuardForTupleDestructuringAssignmentNegative() returns boolean {
    var [s, i] = tupleReturningFunc("str");
    if (s is string) {
        string strVal = s;
        [s, i] = tupleReturningFunc(strVal);
        return s is ();
    }

    return true;
}

function tupleReturningFunc(string? s) returns [string?, int] {
    return [s, 1];
}

function testTypeGuardForRecordDestructuringAssignmentPositive() returns boolean {
    var {s: s, i: i, ...rest} = recordReturningFunc(1);
    if (i is int) {
        int intVal = i;
        {s: s, i: i, ...rest} = recordReturningFunc(());
        return i is ();
    }

    return false;
}

function testTypeGuardForRecordDestructuringAssignmentNegative() returns boolean {
    record {
        string s;
        int? i;
    } {s, i, ...rest} = recordReturningFunc(1);
    if (i is int) {
        int intVal = i;
        {s: s, i: i, ...rest} = recordReturningFunc(2);
        return i is ();
    }

    return true;
}

function recordReturningFunc(int? i) returns record {string s; int? i;} {
    return {s: "hello", i: i, "f": 1.0};
}

function testTypeGuardForErrorDestructuringAssignmentPositive() returns boolean {
    var error(s, message = message, code = code) = errorReturningFunc(1);
    if (code is int) {
        int intVal = code;
        error(s, message = message, code = code) = errorReturningFunc(());
        return code is ();
    }

    return false;
}

function testTypeGuardForErrorDestructuringAssignmentNegative() returns boolean {
    error<Detail> error(s, message = message, code = code) = errorReturningFunc(1);
    if (code is int) {
        int intVal = code;
        error(s, message = message, code = code) = errorReturningFunc(3);
        return code is ();
    }

    return true;
}

type Detail record {
    string message;
    error cause?;
    int? code;
    float f?;
};

type ErrorD error<Detail>;

function errorReturningFunc(int? i) returns error<Detail> {
    return error ErrorD("hello", message = "hello", code = i, f = 1.0);
}

const ASSERTION_ERROR_REASON = "AssertionError";
const EXP_STR = "hello world";

function testSameVarNameInDifferentScopes() {
    string|int val = "hello ";

    string str = "";
    if (val is string) {
        str = val;
        boolean bool = false;

        if bool {
            string s = "you";
            str += s;
            val = 1;
        } else {
            string s = "world";
            str += s;
        }

        if str == EXP_STR {
            return;
        }
    }
    panic error(ASSERTION_ERROR_REASON, message = "expected '" + EXP_STR + "', found '" + str + "'");
}

public type XYZ record {
    string x;
    string y;
    int z;
};

function testNarrowedTypeResetWithMultipleBranches() {

    XYZ|string sampleValue = {x: "X", y :"Y", z: -1};

    if sampleValue is XYZ {
        if isZpositive(sampleValue) {
            sampleValue = "one";
        } else if isXNotEmpty(sampleValue) {
            sampleValue = "two";
        } else if isYEmpty(sampleValue) {
            sampleValue = "three";
        }
    }

    if sampleValue is string && sampleValue == "two" {
        return;
    }

    panic error(ASSERTION_ERROR_REASON, message = "expected 'two', found '" + sampleValue.toString() + "'");
}

function isXNotEmpty(XYZ xyz) returns boolean {
    return xyz.x.length() > 0;
}

function isYEmpty(XYZ xyz) returns boolean {
    return xyz.y.length() == 0;
}

function isZpositive(XYZ xyz) returns boolean {
    return xyz.z > 0;
}

function testNarrowedTypeResetWithNestedTypeGuards() {
    int|string? i = 1;
    int jo = 0;
    int|string? qo = 0;
    int|string? ro = 0;

    if i is int|string {
        if true {
            if i is int {
                if true {
                    int j = i;
                    jo = j;
                    i = "hello";
                }
            } else {
                string s = i;
            }
            int|string? q = i;
            qo = q;
        }
        int|string? r = i;
        ro = r;
    }

    if jo == 1 && i == "hello" && qo == "hello" && ro == "hello" {
        return;
    }

    json[] jarr = [jo, i, qo, ro];
    panic error(ASSERTION_ERROR_REASON, message = "expected '[1, \"hello\", \"hello\", \"hello\"]', found '" +
                                                        jarr.toJsonString() + "'");
}

type TargetType typedesc<string|xml|json>;

function testType(TargetType targetType) returns string {
    if (targetType is typedesc<json>) {
        return "json";
    }
    if (targetType is typedesc<string>) {
        return "http:Response";
    }
    return "null";
}

function testTypeDescTypeTest1() returns boolean {
    string result = testType(json);
    if (result == "json") {
        return true;
    }

    return false;
}

type XmlType xml;

function testTypeDescTypeTest2() returns boolean {
    string result = testType(XmlType);
    if (result == "null") {
        return true;
    }

    return false;
}

function testTypeNarrowingForIntersectingUnionWithRecords() returns boolean {
    AnydataOrObjectOpenRecord|int val = 11;
    if val is OpenRecordWithObjectField {
        return false;
    } else if <int> val != 11 {
        return false;
    }

    AnydataOrObjectOpenRecord|int val2 = {};
    if val2 is OpenRecordWithObjectField {
        return false;
    } else if val2 is int {
        return false;
    }

    AnydataOrObjectOpenRecord|int val3 = <OpenRecordWithObjectField> {code: new (10)};
    if val3 is OpenRecordWithObjectField {
        Class cl = val3.code;
        if cl.val != 10 {
            return false;
        }
    } else {
        return false;
    }

    OpenRecordWithObjectField val4 = {code: new (20)};
    if val4 is ClosedRecordWithObjectAndIntFields {
        return false;
    }

    OpenRecordWithObjectField val5 = <ClosedRecordWithObjectAndIntFields> {code: new (30), index: 0};
    if val5 is ClosedRecordWithObjectAndIntFields {
        if val5.code.val != 30 || val5.index != 0 {
            return false;
        }
    } else {
        return false;
    }

    OpenRecordWithIntField val6 = {i: 1, "s": "hello"};
    if val6 is record {| int i; string s; |} {
        return false;
    } else if val6.i != 1 || val6["s"] != "hello" {
       return false;
    }

    record {| int i; string s; |} v = {i: 2, s: "world"};
    OpenRecordWithIntField val7 = v;
    if val7 is record {| int i; string s; |} {
        if val7.i != 2 || val7.s != "world" {
            return false;
        }
    } else {
        return false;
    }

    ClosedRecordWithIntField val8 = {i: 10};
    if val8 is record {| byte i; |} {
        return false;
    }

    int|ClosedRecordWithIntField val9 = <record {| byte i; |}> {i: 10};
    if val9 is record {| byte i; |} {
        if val9.i != 10 {
            return false;
        }
    } else {
        return false;
    }

    byte b = 10;
    if val9 is record {} {
        if val9["i"] != b {
            return false;
        }
    } else {
        return false;
    }

    if val9 is record {| int...; |} {
        if val9["i"] != b {
            return false;
        }
    } else {
        return false;
    }
    return true;
}

type AnydataOrObjectOpenRecord record {|
    anydata|object {}...;
|};

type OpenRecordWithObjectField record {
    readonly Class code = new (0);
};

readonly class Class {
    int val;

    isolated function init(int val) {
        self.val = val;
    }
}

type ClosedRecordWithObjectAndIntFields record {|
    readonly Class code;
    int index;
|};

type OpenRecordWithIntField record {
    int i;
};

type ClosedRecordWithIntField record {|
    int i;
|};

type ClosedRec record {|
    int i?;
    boolean b;
|};

public function testRecordIntersectionWithClosedRecordAndRecordWithOptionalField() {
    record {| boolean b; |} x = {b: true};
    record {| byte i?; boolean b?; |} y = x;

    assertEquality(true, y is ClosedRec);

    if y is ClosedRec {
        record {| byte i?; boolean b; |} rec = y;
        assertEquality(true, rec.b);
        assertEquality((), rec?.i);

        record {| byte i?; boolean...; |} rec2 = y;
        assertEquality(true, rec2["b"]);
        assertEquality((), rec2?.i);
    }
}

type ClosedRecTwo record {|
    int i;
    string s?;
|};

type ClosedRecThree record {|
    int i?;
    boolean b = true;
|};

function testRecordIntersectionWithClosedRecordAndRecordWithOptionalField2() {
    record {| int i; |} x = {i: 1};
    record {| int i; boolean b?; |} y = x;

    assertEquality(true, y is ClosedRecTwo);

    if y is ClosedRecTwo {
        record {| int i; |} z = y;
        assertEquality(1, z.i);
    }

    ClosedRec cr1 = {i: 1, b: true};
    assertEquality(false, cr1 is record {| byte i?; boolean b?; |});
    assertEquality(false, cr1 is record {| byte i; boolean b?; |});

    record {| byte i?; boolean b = false; |} rec2 = {i: 100};
    ClosedRec cr2 = rec2;
    assertEquality(true, cr2 is record {| byte i?; boolean b?; |});
    assertEquality(false, cr2 is record {| byte i; boolean b?; |});

    if cr2 is record {| byte i?; boolean b?; |} {
        record {| byte i?; boolean b; |} rec = cr2;
        assertEquality(100, rec?.i);
        assertEquality(false, rec.b);
    }

    ClosedRec cr3 = <record {| byte i; boolean b; |}> {i: 45, b: true};
    assertEquality(true, cr3 is record {| byte i?; boolean b?; |});
    assertEquality(true, cr3 is record {| byte i; boolean b?; |});

    if cr3 is record {| byte i; boolean b?; |} {
        record {| byte i; boolean b; |} rec = cr3;
        assertEquality(45, rec?.i);
        assertEquality(true, rec.b);
    }

    ClosedRecThree cr4 = {i: 1, b: true};
    assertEquality(false, cr4 is record {| byte i?; boolean b?; |});
    assertEquality(false, cr4 is record {| byte i; boolean b?; |});

    record {| byte i?; boolean b = false; |} rec3 = {i: 100};
    ClosedRecThree cr5 = rec3;
    assertEquality(true, cr5 is record {| byte i?; boolean b?; |});
    assertEquality(false, cr5 is record {| byte i; boolean b?; |});

    if cr5 is record {| byte i?; boolean b?; |} {
        record {| byte i?; boolean b; |} rec = cr5;
        assertEquality(100, rec?.i);
        assertEquality(false, rec.b);
    }

    ClosedRecThree cr6 = <record {| byte i; boolean b = false; |}> {i: 45, b: true};
    assertEquality(true, cr6 is record {| byte i?; boolean b?; |});
    assertEquality(true, cr6 is record {| byte i; boolean b?; |});

    if cr6 is record {| byte i; boolean b?; |} {
        record {| byte i; boolean b; |} rec = cr6;
        assertEquality(45, rec?.i);
        assertEquality(true, rec.b);
    }
}

type RecordWithDefaultValue record {|
    int i = 10;
    boolean b?;
|};

type RecordWithNoDefaultValue record {|
    byte i;
    boolean|string b?;
|};

function testRecordIntersectionWithDefaultValues() {
    RecordWithDefaultValue e = {};
    assertEquality(false, e is RecordWithNoDefaultValue);

    record {| byte i = 101; |} rec = {};
    RecordWithDefaultValue f = rec;
    assertEquality(true, f is RecordWithNoDefaultValue);

    if f is RecordWithNoDefaultValue {
        record {| byte i; boolean b?; |} rec2 = f;
        assertEquality(101, rec2?.i);
        assertEquality((), rec2?.b);
    }

    record {| byte i = 101; boolean b; |} rec3 = {b: true};
    RecordWithDefaultValue g = rec3;
    assertEquality(true, g is RecordWithNoDefaultValue);

    if g is RecordWithNoDefaultValue {
        record {| byte i; boolean b?; |} rec2 = g;
        assertEquality(101, rec2?.i);
        assertEquality(true, rec2?.b);
    }
}

function testClosedRecordAndMapIntersection() {
    record {| byte i; |} x = {i: 123};
    map<int|string> m = x;

    assertEquality(true, m is record {| int i; float f?; |});

    if m is record {| int i; float f?; |} {
        record {| int i; |} rec = m;
        assertEquality(123, rec.i);
    }
}

type RecordWithNonReadOnlyField record {|
    int i;
    string s?;
|};

type RecordWithReadOnlyFieldAndOptionalNonReadOnlyField record {|
    readonly int i;
    boolean b?;
|};

type RecordWithReadOnlyFieldAndNonReadOnlyField record {|
    readonly int i;
    string|boolean s;
|};

function testIntersectionReadOnlyness() {
    record {| int i; |} & readonly r1 = {i: 1};
    RecordWithNonReadOnlyField r2 = r1;

    assertEquality(true, r2 is RecordWithReadOnlyFieldAndOptionalNonReadOnlyField);
    assertEquality(false, r2 is RecordWithReadOnlyFieldAndNonReadOnlyField);

    if r2 is RecordWithReadOnlyFieldAndOptionalNonReadOnlyField {
        readonly x = r2;
        record {| readonly int i; |} y = r2;
        assertEquality(1, y.i);
    }

    record {| readonly int i; string s; |} r3 = {i: 123, s: "hello"};
    RecordWithNonReadOnlyField r4 = r3;

    assertEquality(true, r4 is RecordWithReadOnlyFieldAndNonReadOnlyField);
    if r2 is RecordWithReadOnlyFieldAndNonReadOnlyField {
        record {| readonly int i; string s; |} x = r2;
        assertEquality(123, x.i);
        assertEquality("hello", x.s);
    }
}

function testMapIntersection() {
    map<int|string> m = {a: 1, b: 2};
    assertEquality(false, m is map<int|boolean>);

    map<int> m2 = {a: 1, b: 2};
    map<int|string> m3 = m2;
    assertEquality(true, m3 is map<int|boolean>);

    if m3 is map<int|boolean> {
        map<int> m4 = m3;
        assertEquality(2, m4.length());
        assertEquality(1, m4["a"]);
        assertEquality(2, m4["b"]);
        assertEquality((), m4["c"]);
    }
}

function jsonIntersection(json j) returns int {
    if j is map<int|stream<int>> {
        map<int> m = j;
        return m.length();
    }

    if j is (int|stream<int>|boolean)[] {
        (int|boolean)[] a = j;
        return a.length();
    }

    return -1;
}

function testJsonIntersection() {
    assertEquality(-1, jsonIntersection({a: 1, b: 2}));
    assertEquality(2, jsonIntersection(<map<byte>> {a: 1, b: 2}));
    assertEquality(-1, jsonIntersection(1));
    assertEquality(-1, jsonIntersection([1, 2, 3]));
    assertEquality(4, jsonIntersection(<int[]> [1, 2, 3, 4]));
}

public type Qux record {|
    record {|
        string s;
    |} baz?;
|};

function intersectionWithIntersectionType(Qux? & readonly bar) returns string {
    if bar is Qux {
        record {| string s; |}? y = bar?.baz;
        return y?.s ?: "Qux without baz";
    }
    return "nil";
}

function testIntersectionWithIntersectionType() {
    assertEquality("nil", intersectionWithIntersectionType(()));
    assertEquality("Qux without baz", intersectionWithIntersectionType({}));
    assertEquality("hello world", intersectionWithIntersectionType({baz: {s: "hello world"}}));
}

type Colour "r"|"g"|"b";
type Ints 1|2;
type Kinds Colour|Ints;

function testIntersectionOfBuiltInSubTypeWithFiniteType() {
    string:Char a = "r";
    assertEquality(true, a is Colour);

    if a is Colour {
        Colour a2 = a;
        assertEquality("r", a2);
    }

    int:Unsigned16 b = 1;
    assertEquality(true, b is Ints);

    if b is Ints {
        Ints b2 = b;
        assertEquality(1, b2);
    }

    Kinds c = 1;
    assertEquality(true, c is Ints);

    if c is Ints {
        Ints c2 = c;
        assertEquality(1, c2);
    }

    Kinds d = "g";
    assertEquality(true, d is Colour);
    assertEquality(false, d is Ints);

    if d is Colour {
        Colour d2 = d;
        assertEquality("g", d2);
    }

    string:Char e = "r";
    assertEquality(true, e is Colour);

    if e is Kinds {
        Colour e2 = e;
        assertEquality("r", e2);
    }
}

type Quux record {|
    int i;
    boolean b?;
|};

type Corge record {|
    byte i;
    boolean|string b?;
|};

type Grault record {|
    byte i = 101;
    boolean b?;
|};

function testTypeDefinitionForNewTypeCreatedInTypeGuard() {
    Grault rec = {};
    Quux f = rec;
    assertEquality(true, f is Corge);
    if f is Corge {
        var x = [f];
        x[0] = {i: 1};
        any v = x[0];
        assertEquality(true, v is Grault);
        assertEquality(false, v is record {| byte i = 101; boolean s; |});
    }
}

public type SomeRecord record {
    int intField;
};

public function testIfElseWithTypeTest() {
    SomeRecord? c = {intField: 10};
    SomeRecord? f = ();
    any x = true;

    if c is () {

    } else if x is int {
        c = f;
    } else if (x is boolean) {
        foo(c);
    }
}

public function testIfElseWithTypeTestMultipleVariables() {
    SomeRecord? c = {intField: 10};
    SomeRecord? f = ();
    SomeRecord|int|() g = 4;
    any x = true;

    if c is () {

    } else if g is () {

    } else if x is int {
        c = f;
        g = ();
        g = 4;
    } else if (x is boolean) {
        foo(c);
    } else if (g is SomeRecord) {
        foo(g);
    } else {
        SomeRecord|int w = 4;
        g = w;
    }
}

public function testIfElseWithTypeTestMultipleVariablesInMultipleBlocks() {
    SomeRecord? c = {intField: 10};
    SomeRecord? f = ();
    SomeRecord|int|() g = 4;
    any x = true;

    if c is () {
        assertFalse(c is ());
    } else if g is () {
        assertFalse(g is ());
    } else if x is int {
        c = f;
    } else if (x is float) {
        assertFalse(x is float);
         SomeRecord|int|() s = 5;
         g = s;
    } else if (x is boolean) {
        assertTrue(x is boolean);
        foo(c);
    } else if (x is string) {
        assertFalse(x is string);
        goo(g);
    } else {
        SomeRecord|int w = 4;
        g = w;
    }
}

public function testIfElseWithTypeTestMultipleVariablesInNestedBlocks() {
    SomeRecord? c = {intField: 10};
    SomeRecord? f = ();
    SomeRecord|int|() g = 4;
    any x = true;

    if c is () {
        assertFalse(c is ());
    } else if g is () {
        assertFalse(g is ());
    } else if x is int {
        assertFalse(x is int);
        c = f;
        if (f is SomeRecord) {
            hoo(c);
            if (g is int) {
                g = ();
            } else {
                SomeRecord s = g;
                g = 4;
                yoo(g);
            }
        }
    } else if (x is float) {
        assertFalse(x is float);
         SomeRecord|int|() s = 5;
         g = s;
    } else if (x is boolean) {
        assertTrue(x is boolean);
        foo(c);
    } else if (x is string) {
        goo(g);
    } else {
        SomeRecord|int w = 4;
        g = w;
    }
}

function foo(SomeRecord aa) {
    assertEquality(10, aa.intField);
}

function hoo(SomeRecord? aa) {

}

function goo(SomeRecord|int aa) {

}

function yoo(SomeRecord|int|() aa) {

}

type R1 readonly & record {|int i;|}?;
type R2 (readonly & record {|int i;|})?;
type R record {|int i;|};

function testR1(R1 r) {
    if r is R {
        R x = r; // no error
        assertEquality(x.i, 2);
    } else {
        panic error("Type of 'r' should be 'R'");
    }
}

function testR2(R2 r) {
    if r is R {
        R x = r; // no error
        assertEquality(x.i, 3);
    } else {
        panic error("Type of 'r' should be 'R'");
    }
}

function testTypeTestingInReadonlyRecord() {
    testR1({i: 2});
    testR2({i: 3});
}

type Type C|Tuple|List;
type C "C";
type Tuple ["tuple", Type, Type...];
type List ["list", int?, Type];

function testCustomCircularTupleTypeWithIsCheck() {
    Type a = ["list", 1, "C"];
    Type b = ["tuple", "C", "C", "C"];
    Type c = "C";

    assertEquality(checkIsExpressionWithCircularTuple(a), "List Type");
    assertEquality(checkIsExpressionWithCircularTuple(b), "Not a List Type");
    assertEquality(checkIsExpressionWithCircularTuple(c), "Not a List Type");

    assertEquality(checkIsExpressionWithCircularTuple2(a), "List Type");
    assertEquality(checkIsExpressionWithCircularTuple2(b), "Tuple Type");
    assertEquality(checkIsExpressionWithCircularTuple2(c), "Type C");
}

function checkIsExpressionWithCircularTuple(Type t) returns string {
    if t is List {
        List _ = t;
        return "List Type";
    }
    return "Not a List Type";
}

function checkIsExpressionWithCircularTuple2(Type t) returns string? {
    if t is List {
        List _ = t;
        return "List Type";
    } else if t is Tuple {
        Tuple _ = t;
        return "Tuple Type";
    } else if t is C {
        C _ = t;
        return "Type C";
    }
}

function testSingletonTypeNarrowedTypeDesc() {
    assertEquality(testSingletonTypeNarrowedTypeDesc1(1,1), 1);
    assertEquality(testSingletonTypeNarrowedTypeDesc2(3,1), 3);
    assertEquality(testSingletonTypeNarrowedTypeDesc3("","abc"), "abc");
}

function testSingletonTypeNarrowedTypeDesc1(int foo, int bar) returns int {
    if foo != 1 {
        return foo;
    } else if bar != 1 && foo == 1 {
        return bar;
    } else if foo == 1 && bar == 1 {
        return 1;
    }
    return -1;
}

function testSingletonTypeNarrowedTypeDesc2(1|2|3 foo, int bar) returns int {
    if foo == 1 {
        return foo;
    } else if bar != 1 && foo == 2 {
        return bar;
    } else if foo == 3 && bar == 1 {
        return 3;
    }
    return -1;
}

function testSingletonTypeNarrowedTypeDesc3(string foo, string bar) returns string {
    if foo != "" {
        return foo;
    }
    if bar != "" && foo == "" {
        return bar;
    }
    if foo == "" && bar == "" {
        return "";
    }
    return "";
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
