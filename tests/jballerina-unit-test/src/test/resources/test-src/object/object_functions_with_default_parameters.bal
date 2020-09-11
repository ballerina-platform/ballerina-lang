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

int GLB = 0;
string GLB_STRING = "global";

function getInt() returns int {
    return 100;
}

function getFloat() returns float {
    return 1.1;
}

function getString() returns string {
    return "default";
}

function getBoolean() returns boolean {
    return true;
}

function getRecord() returns FooRecord {
    return { a: "default2", b: 150, c: true, d: 33.3 };
}

type FooRecord record {
    string a;
    int b;
    boolean c;
    float d;
};

class FooObject {
    string a;
    int b;

    function init(string a, int b) {
        self.a = a;
        self.b = b;
    }

    function getValues() returns [string, int] {
        return [self.a, self.b];
    }
}

function getIntOrError(int a) returns int|error {
    if (a == 0) {
        error e = error("Generated Error");
        return e;
    } else {
        return 100;
    }
}

function getStringOrNil() returns string? {
    if (GLB == 0) {
        return "string";
    }
}

function getIntOrPanic() returns int {
    if (GLB == 0) {
        return GLB;
    } else {
        panic error("Panic!");
    }
}

// Test 1
function testObjectInitWithDefaultValues() returns [[string, boolean, int, float, FooRecord, FooObject], [string, boolean, int, float, FooRecord, FooObject]] {
    ObjectOne o1 = new();
    ObjectOne o2 = new(a = "given", b = false, c = 99, e = { a: "given2", b: 49, c: true, d: 10.9 }, f = new("def2", 199));

    return [o1.getValues(), o2.getValues()];
}

class ObjectOne {
    string a;
    boolean b;
    int c;
    float d;
    FooRecord e;
    FooObject f;

    function init(string a = getString(), boolean b = true, int c = getInt(), float d = 1.1, FooRecord e = { a: "default", b: 50, c: false, d: 11.1 }, FooObject f = new("def", 200)) {
        self.a = a;
        self.b = b;
        self.c = c;
        self.d = d;
        self.e = e;
        self.f = f;
    }

    function getValues() returns [string, boolean, int, float, FooRecord, FooObject] {
        return [self.a, self.b, self.c, self.d, self.e, self.f];
    }
}

// Test 2
function testObjectInitWithDefaultValues2() returns [[int, string, float, FooRecord], [int, string, float, FooRecord]]{
    ObjectTwo o1 = new();
    ObjectTwo o2 = new(a = 10, d = { a: "given", b: 49, c: false, d: 10.9 });

    return [o1.getValues(), o2.getValues()];
}

class ObjectTwo {
    int a;
    string b;
    float c;
    FooRecord d;

    function init(int a = getInt() + 5 + getInt(), string b = "def" + getString(), float c = getFloat() + getInt(), FooRecord d = getRecord()) {
        self.a = a;
        self.b = b;
        self.c = c;
        self.d = d;
    }

    function getValues() returns [int, string, float, FooRecord] {
        return [self.a, self.b, self.c, self.d];
    }
}

// Test 3
function testObjectAttachedFunction1() returns [[string, int, FooRecord], [string, int, FooRecord]] {
    ObjectThree o1 = new();
    [string, int, FooRecord] a = o1.attachedFunction1();
    [string, int, FooRecord] b = o1.attachedFunction1(a = "given", c = { a: "given2", b: 140, c: true, d: 22.2 });
    return [a, b];
}

// Test 4
function testObjectAttachedFunction2() returns [[int, string, float, FooRecord], [int, string, float, FooRecord]] {
    ObjectThree o1 = new();
    [int, string, float, FooRecord] a = o1.attachedFunction2();
    [int, string, float, FooRecord] b = o1.attachedFunction2(b = "given", d = { a: "given2", b: 140, c: true, d: 22.2});
    return [a, b];
}

class ObjectThree {

    function attachedFunction1(string a = GLB_STRING, int b = getInt(), FooRecord c = { a: "default", b: 50, c: false, d: 11.1 }) returns [string, int, FooRecord] {
        return [a, b * 2, c];
    }

    function attachedFunction2(int a = getInt() + 10 + getInt(), string b = "def" + getString() + GLB_STRING, float c = getFloat() + getInt(), FooRecord d = getRecord()) returns [int, string, float, FooRecord] {
        return [a, b, c, d];
    }
}

// Test 5
function testObjectAttachedFunction3() returns [[string, int, FooRecord], [string, int, FooRecord]] {
    ObjectFour o1 = new();
    [string, int, FooRecord] a = o1.attachedFunction1();
    [string, int, FooRecord] b = o1.attachedFunction1(a = "given", c = { a: "given2", b: 140, c: true, d: 22.2 });
    return [a, b];
}

// Test 6
function testObjectAttachedFunction4() returns [[int, string, float, FooRecord], [int, string, float, FooRecord]] {
    ObjectFour o1 = new();
    [int, string, float, FooRecord] a = o1.attachedFunction2();
    [int, string, float, FooRecord] b = o1.attachedFunction2(b = "given", d = { a: "given2", b: 140, c: true, d: 22.2 });
    return [a, b];
}

class ObjectFour {

    function attachedFunction1(string a = GLB_STRING, int b = getInt(), FooRecord c = { a: "default", b: 50, c: false, d: 11.1 }) returns [string, int, FooRecord] {
        return [a, b * 2, c];
    }

    function attachedFunction2(int a = getInt() + 10 + getInt(), string b = "def" + getString() + GLB_STRING, float c = getFloat() + getInt(), FooRecord d = getRecord()) returns [int, string, float, FooRecord] {
        return [a, b, c, d];
    }
}

class ObjectFive {
    function foo(int x = getInt(), float y = getFloat()) returns [int, float] {
        return [x * 2, y * 2];
    }
}

class ObjectSix {
    function foo(int x = getInt() * 2, float y = getFloat() * 2) returns [int, float] {
        return [x * 2, y * 2];
    }
}

function testObjectCasting1() returns [[int, float], [int, float], [int, float], [int, float]] {
    ObjectFive o1 = new();
    ObjectSix o2 = o1;

    [int, float] a = o2.foo();
    [int, float] b = o2.foo(x = 20);
    [int, float] c = o2.foo(x = 20, y = 11.1);
    [int, float] d = o2.foo(y = 11.1);

    return [a, b, c, d];
}

function testObjectCasting2() returns [[int, float], [int, float], [int, float], [int, float]] {
    ObjectSix o1 = new();
    ObjectFive o2 = o1;

    [int, float] a = o2.foo();
    [int, float] b = o2.foo(x = 40);
    [int, float] c = o2.foo(x = 40, y = 22.2);
    [int, float] d = o2.foo(y = 22.2);

    return [a, b, c, d];
}
