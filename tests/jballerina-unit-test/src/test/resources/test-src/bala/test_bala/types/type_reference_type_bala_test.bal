// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import testorg/typereftypes as tr;

type ImmutableIntArrayOrStringArray tr:Strings[]|tr:ImmutableIntArray;

type FunctionType1 function (tr:Integer i) returns tr:Decimal;

type TypeConstInt tr:ConstInt;

type MapFooBar map<tr:FooBar>;

record {|
    tr:ImmutableIntArray a;
    tr:FooBar b;
|} X = {a : [1], b : 1};

record {|
    *tr:Bar;
|} Y = {b : {}};

function testFn() {
    tr:Baz baz = {};
    tr:Bar bar = {b: baz};
    tr:Foo foo = {a: bar};
    assertEquality(<tr:Foo> {a: {b: baz}}, foo);

    tr:Quux d = ();
    assertEquality((), d);

    anydata a1 = int:MAX_VALUE;
    assertEquality(true, a1 is tr:Integer);
    a1 = int:MIN_VALUE;
    assertEquality(true, a1 is tr:Integer);
    a1 = 1.0;
    assertEquality(false, a1 is tr:Integer);
    a1 = "a";
    assertEquality(false, a1 is tr:Integer);

    a1 = [1, 2].cloneReadOnly();
    assertEquality(true, a1 is int[2]);
    a1 = [1, 2, 3].cloneReadOnly();
    assertEquality(false, a1 is tr:IntArray);
    a1 = [1].cloneReadOnly();
    assertEquality(false, a1 is tr:IntArray);

    a1 = [1.2, true].cloneReadOnly();
    assertEquality(true, a1 is tr:FloatBooleanTuple);
    a1 = [1.2d, true].cloneReadOnly();
    assertEquality(false, a1 is tr:FloatBooleanTuple);

    FunctionType1 func1 = function (int x) returns decimal {
        return <tr:Decimal> x;
    };
    tr:Decimal a2 = func1(3);
    assertEquality(3d, a2);

    var func2 = function ([float, boolean] x) returns tr:Record {
        return {a : x, b : ()};
    };
    tr:FloatBooleanTuple a3 = [1.2, true];
    tr:Record a4 = func2(a3);
    assertEquality({a : [1.2, true], b : ()}, a4);
    assertEquality(true, func2 is function (tr:FloatBooleanTuple i) returns tr:Record);

    a1 = [1, 2];
    tr:ImmutableIntArray a5 = [1, 2];
    assertEquality(true, a5 is int[]);
    assertEquality(true, a5 is int[] & readonly);
    assertEquality(false, a1 is tr:ImmutableIntArray);
    assertEquality(true, a5 is ImmutableIntArrayOrStringArray);
    assertEquality(false, a1 is ImmutableIntArrayOrStringArray);

    tr:FooBar a6 = "foo";
    assertEquality(true, "foo" is tr:FooBar);
    assertEquality(true, 1 is tr:FooBar);
    assertEquality(false, "1" is tr:FooBar);
    assertEquality(true, a6 is 1|"foo");

    assertEquality(true, X is record {|ImmutableIntArrayOrStringArray a; tr:FooBar b;|});

    MapFooBar a8 = {a : 1};
    assertEquality(true, a8 is map<tr:FooBar>);

    boolean b1 = true;
    "foo"|1 b2 = 1;
    if b1 {
        tr:FooBar b = "foo";
        b2 = b;
    } else {
        tr:FooBar b = 1;
        b2 = b;
    }
    assertEquality("foo", b2);

    tr:IntArray array = [1, 2];
    int b3 = 0;
    foreach tr:Integer i in array {
        tr:Integer b = i;
        b3 = b;
    }
    assertEquality(2, b3);

    assertEquality([1, 2], getImmutable([1, 2]));
    assertEquality([], getImmutable(["1", "2"]));

    TypeConstInt b4 = 5;
    assertEquality(true, b4 is tr:ConstInt);
    assertEquality(true, tr:ConstInt is TypeConstInt);

    assertEquality({b : {}}, Y);

    tr:Seconds? sec1 = 10;
    assertEquality(sec1 is decimal, true);

    tr:SecondsOrNil sec2 = 11;
    assertEquality(sec2 is decimal, true);
}

function getImmutable(ImmutableIntArrayOrStringArray x) returns tr:ImmutableIntArray {
    if x is tr:ImmutableIntArray {
        return x;
    }
    return [];
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
