// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

type Foo record {|
    Bar x?;
    int y;
|};

type Bar record {|
    *Foo;
    int y;
|};

function testCyclicRecordTypeDefinition() {
    Foo f1 = {y: 1};
    assertEquals(f1.length(), 1);

    f1.x = f1;
    assertEquals(f1.length(), 2);
    assertEquals(f1.x?.y, 1);
    assertTrue(f1.x is Bar);

    Foo f2 = {x : f1, y : 2};
    assertEquals(f2.length(), 2);
    assertEquals(f2.x?.y, 1);
    assertTrue(f2.x is Bar);
}

type Foo2 record {|
    Bar2 b?;
    int y;
|};

type Bar2 record {|
    *Car2;
    int y;
|};

type Car2 record {|
    *Foo2;
    int z;
|};

function testComplexCyclicRecordTypeDefinition() {
    Foo2 f2 = { b: {z: 2, y: 3}, y: 1};
    assertEquals(f2.length(), 2);
    assertEquals(f2.b?.y, 3);
    assertTrue(f2.b is Bar2);
}

type Foo3 record {|
    Bar3 b?;
    int y;
    *Far3;
|};

type Bar3 record {|
    *Foo3;
    int y;
|};

type Far3 record {|
    int x;
|};

function testComplexCyclicRecordTypeDefinition2() {
    Foo3 f3 = { b: {y: 3, x: 4}, y: 1, x: 2};
    assertEquals(f3.length(), 3);
    assertEquals(f3.b?.y, 3);
    assertEquals(f3.b?.x, 4);
    assertEquals(f3.y, 1);
    assertEquals(f3.x, 2);
    assertTrue(f3.b is Bar3);
}

type MyFunc1 function (MyFunc1? a) returns int;

function testCyclicFunctionTypeDefinition() {
    MyFunc1 f1 = function (MyFunc1? a) returns int {
        return 1;
    };
    assertEquals(f1(()), 1);
    assertEquals(f1(f1), 1);
}

type MyIntersection1 MyFunc2 & readonly;
type MyFunc2 function (MyIntersection1? a) returns int;

function testCyclicReadonlyFunctionTypeDefinition() {
    MyFunc2 f2 = function (MyFunc2? a) returns int {
        return 1;
    };

    assertEquals(f2(()), 1);
    assertEquals(f2(f2), 1);
}

type A1 stream<A1?>;

class Generator {
    public isolated function next() returns record {| A1? value; |}? {
        return {value: ()};
    }
}

function testCyclicStreamTypeDefinition() {
    Generator gen = new ();
    A1 a1 = new (gen);
    record {| A1? value; |}? element = a1.next();
    assertTrue(element !is ());
    if (element !is ()) {
        assertTrue(element.value is ());
    }
}

type ErrA error<Details>;

type Details record {|
    ErrA? errA;
|};

function testCyclicErrorTypeDefinition() {
    ErrA error1 = error("Whoops", errA = ());
    assertEquals(error1.toString(), "error ErrA (\"Whoops\",errA=null)");

    ErrA error2 = error("Whoops", errA = error1);
    assertEquals(error2.detail().toString(), "{\"errA\":error ErrA (\"Whoops\",errA=null)}");
}

type ErrB error<DetailsB> & readonly;

type DetailsB record {|
    ErrB? errB;
|};

function testCyclicReadonlyErrorTypeDefinition() {
    ErrB error1 = error("Whoops", errB = ());
    assertTrue(error1 is readonly);
    assertEquals(error1.toString(), "error ErrB (\"Whoops\",errB=null)");

    ErrB error2 = error("Whoops", errB = error1);
    assertTrue(error2 is readonly);
    assertEquals(error2.detail().toString(), "{\"errB\":error ErrB (\"Whoops\",errB=null)}");
}

type T1 [T1?] & readonly;

type T2 T3 & readonly;
type T3 [T2?] & readonly;

public function testCyclicReadonlyTupleTypeDefinition() {
    T1 t1 = [];
    assertTrue(t1 is readonly);
    assertEquals(t1.toString(), "[null]");

    T2 t2 = [];
    assertTrue(t2 is readonly);
    assertEquals(t2.toString(), "[null]");

    T3 t3 = [];
    assertTrue(t3 is readonly);
    assertEquals(t3.toString(), "[null]");
}

function assertTrue(anydata actual) {
    assertEquals(true, actual);
}

function assertEquals(anydata actual, anydata expected) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}
