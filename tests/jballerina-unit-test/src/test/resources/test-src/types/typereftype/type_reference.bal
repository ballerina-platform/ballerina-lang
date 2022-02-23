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
//   assertEqual(a1.cloneReadOnly() is FloatBooleanTuple, true); // Need to fix ballerina-lang/#34954
    a1 = [1.2d, true];
//   assertEqual(a1.cloneReadOnly() is FloatBooleanTuple, false); // Need to fix ballerina-lang/#34954

    FunctionType1 func1 = function (int x) returns decimal {
            return <Decimal> x;
        };
    Decimal a2 = func1(3);
    assertEqual(a2, 3d);

    var func2 = function ([float, boolean] x) returns Record {
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
}

function getImmutable(ImmutableIntArrayOrStringArray x) returns ImmutableIntArray {
    if x is ImmutableIntArray {
        return x;
    }
    return [];
}

function assertEqual(anydata actual, anydata expected) {
    if expected == actual {
        return;
    }
    panic error(string `expected '${expected.toBalString()}', found '${actual.toBalString()}'`);
}
