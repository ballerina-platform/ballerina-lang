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

import ballerina/lang.value;

type A int|A[];

public function testCycleTypeArray() {
    A a = 1;
    A b = [1];
    A c = [a];

    assert(a is int, true);
    assert(<int> a, 1);

    int m = 0;
    int n = 0;

    if b is A[] {
        m = <int> b[0];
    }

    if c is A[] {
       n = <int> c[0];
    }

    assert(m, 1);
    assert(n, 1);
}

type B int|map<B>;

public function testCycleTypeMap() {
    B number = 1;
    B mapping = {
        one: number,
        two: 2
    };

    int numberResult = 0;
    if(mapping is map<B>) {
        numberResult = <int> mapping.get("one");
    }
    assert(numberResult, 1);
}

type Person record {
    int id;
    string name;
};

type C Person|table<map<C>>;

public function testCycleTypeTable() {
    table<map<C>> tb = table [
            {
                one: { id: 1, name: "Jane"}
            },
            {
                one: { id: 1, name: "Anne"}
            }
    ];

    string names = "";
    foreach var x in tb {
        var  a = x.get("one");
        if(a is Person) {
            names = names + a.name;
        }
     }
    assert(names, "JaneAnne");
}

type D map<D>|int;

public function testCyclicAsFunctionParams() {
    D d = {
        elem: 2
    };

    map<D> c = <map<D>> takeCyclicTyDef(d);
    assert(<int> c.get("elem"), 5);
}

function takeCyclicTyDef(D d) returns D {
   if(d is map<D>) {
       d["elem"] = 5;
   }
   return d;
}

type E int|record{E a = 1;};

public function testCyclicTypeDefInRecord() {
    E a = 3;
    E rec = { a : a};
    int result = 0;
    if (rec is record{}) {
        result = <int> rec["a"];
    }
    assert(result, 3);
}

type F int|string|F[]|map<F>;

public function testCyclicTypeDefInTuple() {
    [F, F, F] values = [1, "hello", [2, "hi"]];
    assert(<int> values[0], 1);
    assert(<string> values[1], "hello");
    var arr = values[2];

    int x = 0;
    string y = "";
    if (arr is F[]) {
        x = <int> arr[0];
        y = <string> arr[1];
    }
    assert(x, 2);
    assert(y, "hi");
}

type H int|string|H[]|map<H>|table<map<H>>|record { H a = "rec"; float x;};

public function testComplexCyclicUnion() {
     H num = 1;
     H str = "hello";
     H mapping = {
         first: num,
         second: str
     };
     H rec = { a : "rec2", x: 2.0};
     H array = [
        1, "hello", mapping, rec
     ];

     int x = 0;
     string y = "";
     string mapVal = "";
     string objVal = "";
     record { H a; float x; } re = { a : "r", x: 0};

     if (array is H[]) {
        x = <int> array[0];
        y = <string> array[1];

        var m = array[2];
        if (m is map<H>) {
            mapVal = <string> m.get("second");
        }

        var n = array[3];
        if (n is record{}) {
            re = <record { H a; float x; }> array[3];
        }
     }
     assert(x, 1);
     assert(y, "hello");
     assert(mapVal, "hello");
     assert(re.a, "rec2");
}

type I H;

function testCyclicUserDefinedType() {
    I i = 1;
    assert(<int> i, 1);
}

function testCyclicUnionAgainstSubSetNegative() {
    record {} x = {};
    assert(false, x is record {| int|boolean|decimal|float|string|xml?...; |});
}

function testImmutableImportedCyclicUnionVariable() {
    value:Cloneable & readonly x = 1;
    assert(1, <anydata> checkpanic x);
}

type MyCyclicUnion int|MyCyclicUnion[];

function testCastingToImmutableCyclicUnion() {
    MyCyclicUnion a = [1, 2];
    (MyCyclicUnion & readonly)|error b = trap <MyCyclicUnion & readonly> a;
    assert(true, b is error);
    error err = <error> b;
    assert("{ballerina}TypeCastError", err.message());
    assert("incompatible types: 'MyCyclicUnion[]' cannot be cast to '(MyCyclicUnion & readonly)'",
           <string> checkpanic err.detail()["message"]);

    MyCyclicUnion c = <int[] & readonly> [1, 2];
    MyCyclicUnion & readonly d = <MyCyclicUnion & readonly> c;
    assert(true, d is int[] & readonly);
    assert(<int[]> [1, 2], d);

    value:Cloneable e = <value:Cloneable> [1, 2];
    (value:Cloneable & readonly)|error f = trap <value:Cloneable & readonly> e;
    assert(true, f is error);
    err = <error> f;
    assert("{ballerina}TypeCastError", err.message());
    assert("incompatible types: 'ballerina/lang.value:1:Cloneable[]' cannot be cast to " +
        "'(ballerina/lang.value:1:Cloneable & readonly)'", <string> checkpanic err.detail()["message"]);

    value:Cloneable g = <int[] & readonly> [1, 2];
    value:Cloneable & readonly h = <value:Cloneable & readonly> g;
    assert(true, h is int[] & readonly);
    assert(<int[]> [1, 2], <anydata> checkpanic h);
}

type P XNil|XBoolean|XInt|XString|XUnion4|XIntersection4|XNever|XAny|
    	   XListRef|XFunctionRef;

const XNil = "nil";
const XBoolean = "boolean";
const XInt = "int";
const XString = "string";
const XNever = "never";
const XAny = "any";

type XUnion4 ["|", P...];
type XIntersection4 ["&", P...];

type ListDef P[];
type FunctionDef P[2];

type Defs record {|
       ListDef[] listDefs;
       FunctionDef[] functionDefs;
|};

type XListRef ["listRef", int];
type XFunctionRef ["functionRef", int];
type mapDef map<P>;
type tupleDef [P...];

function testIndirectRecursion() {
    P test1 = ["|", "int", "nil"];
    assert(test1 is XUnion4, true);

    FunctionDef test2 = ["nil","string"];
    assert(test2[0] is XNil, true);

    ListDef test3 = [["|"],["&"],"any"];

    if(test3[1] is XIntersection4) {
       test2[0]= ["listRef", 1];
    }
    assert(test2[0].toString(), "[\"listRef\",1]");

    Defs test4 = {
       listDefs: [[["&"]]],
       functionDefs: []
    };
    assert(test4.functionDefs is FunctionDef[], true);

    mapDef test5 = {one: "never"};
    tupleDef test6;
    if (test5["one"] is XBoolean) {
        test6 = [["|"],["functionRef", 1]];
    } else {
        test6 = [["functionRef", 1]];
    }
    assert((<XFunctionRef>test6[0])[0] is string, true);
}

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}
