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

import ballerina/test;

type A_TTDC [int, A_TTDC[]];
public function testCycleTypeArray() {
    A_TTDC a = [1];
    A_TTDC b = [1, [a]];

    assert(a[0] is int, true);
    assert(a[0], 1);
    assert(b[1] is [int,A_TTDC[]][], true);
}

type B [int, map<B>];
public function testCycleTypeMap() {
    B d = [1, {one: [1]}];

    int numberResult = 0;
    if(d[1] is map<B>) {
        numberResult = d[1].get("one")[0];
    }
    assert(numberResult is int, true);
    assert(numberResult, 1);
}

type Person record {
    int id;
    string name;
};

type C [Person, table<map<C>>];

public function testCycleTypeTable() {
    table<map<C>> tb = table [
            {
                one: [{ id: 1, name: "Jane"}]
            },
            {
                one: [{ id: 1, name: "Anne"}]
            }
    ];

    string names = "";
    foreach var x in tb {
        var  a = x.get("one");
        if(a[0] is Person) {
            names = names + a[0].name;
        }
    }
    assert(names, "JaneAnne");
}

type D [int, map<D>];

public function testCyclicAsFunctionParams() {
   D d = [1, {elem: [2]}];
   map<D> c = takeCyclicTyDef(d);
   assert(c.get("elem")[0], 5);
}

function takeCyclicTyDef(D d) returns map<D> {
   if(d is [int, map<D>]) {
       d[1]["elem"][0] = 5;
   }
   return d[1];
}

type E [int, record{E a?;}];

public function testCyclicTypeDefInRecord() {
    E rec = [1, {}];
    int result = 0;
    if (rec[1] is record{}) {
        result = rec[0];
    }
    assert(result, 1);
}

type F [int, string, F[]|int|(), map<F>];

public function testCyclicTypeDefInUnion() {
   int|F values = [1, "hello"];
   assert((<F> values)[0], 1);
   assert((<F> values)[1], "hello");
   assert((<F> values)[2], ());
}

type XType1 XNil|XBoolean|XInt|XString|XTuple1|XUnion1|XIntersection1|XNever|XAny|();
type XType2 XNil|XBoolean|XInt|XString|XTuple2|XUnion2|XIntersection2|XNever|XAny|();
type XType3 XNil|XTuple3[]|XNever|XAny|();

type P [XNil, XUnion4, XIntersection4, XAny, XListRef, XFunctionRef];

const XNil = "nil";
const XBoolean = "boolean";
const XInt = "int";
const XString = "string";
const XNever = "never";
const XAny = "any";

type XTuple1 ["tuple", XType1, XType1];
type XUnion1 ["union", XType1, XType1];
type XIntersection1 ["intersection", XType1, XType1];

type ApproxType string|ApproxType[];

type XTuple2 ["tuple", ApproxType, ApproxType];
type XUnion2 ["union", ApproxType, ApproxType];
type XIntersection2 ["intersection", ApproxType, ApproxType];

type XTuple3 ["tuple", XType3, XType3];

type XUnion4 ["|", P...];
type XIntersection4 "&"|P;

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

public function testCyclicUserDefinedTypes() {
    XType1 a = ["tuple"];
    assert(a is XTuple1, true);
    assert(a is string, false);

    ApproxType c = ["text1"];
    assert(c is ApproxType[], true);

    XUnion2 b = ["union", "text1", "text2"];
    assert(b[1] is ApproxType, true);

    XType3 d = [["tuple"], ["tuple"]];
    assert(d is XTuple3[], true);
}

type J [int, J[2]];
type K [int, J[]];

function testIndirectRecursion() {
    P test1 = ["nil", ["|"], "&", "any", ["listRef"], ["functionRef", 2]];
    assert(test1[2] is XIntersection4, true);

    FunctionDef test2 = [["nil", [], "&"], ["nil", [], "&"]];
    assert(test2[0][1] is XUnion4, true);

    ListDef test3 = [["nil", [], ["nil", [], "&"]]];

    if(test3[0][2] is XIntersection4) {
       test2[1][2]= ["nil", [], "&"];
    }
    assert((<P>test2[1][2])[2].toString(), "&");

    Defs test4 = {
       listDefs: [[],[],[]],
       functionDefs: []
    };
    assert(test4.listDefs[0] is P[], true);

    mapDef test5 = {one: ["nil", [], "&"]};
    tupleDef test6;
    if (test5.get("one")[0] is XNil) { // always true
        test6 = [["nil", ["|"], "&"]];
    }
    assert(test6[0][1][0] is string, true);

    K tempTuple = [1, []];
    assert(tempTuple[1] is J[], true);
}

type G [int, string, G...];
type H [int, H[], string, H...];
type T [int, (int|T)...];

public function testCyclicRestType() {
    G a = [1, "text"];
    G b = [1, "text1", [2, "text2"], [3, "text3"]];
    assert(b[2] is G, true);

    H c = [1, [[1]], "text2"];
    H d = [1, [[2]], "text2", [3]];
    assert(d[1] is H[], true);
    assert(d[3] is H, true);

    T x = [0];
    x[1] = 1;
    assert(x[1] is int|T, true);
}

type I [int, string, I[], map<I>, table<map<I>>, record { I a?; float x?;}, I...];

public function testComplexCyclicTuple() {
   string sampleString = "text";
    I test1 = [1];
    I test2 = [1, sampleString];
    I test3 = [1, "hello", [[1, sampleString]]];
    I test4 = [1, "hello", [[1, sampleString]], {
         first: test1,
         second: test3
    }];

    table<map<I>> tb = table [
            {
                one: test3
            },
            {
                one: test4
            }
    ];

    I test5 = [1, "hello", [[1, sampleString]], {
         first: test4,
         second: test3
    }, tb];

    I test6 = [1, sampleString, [[1, "hello"]], {
         first: test1,
         second: test5
    }, tb, {}, test1, test2, test3, test4, test5];

     int mapVal = 0;
     string tableVal = "";
     if (test6[1] is string) {
        test4[2][0][0] = 2;

        var m = test5[3];
        if (m is map<I>) {
            mapVal =  m.get("second")[0];
        }

        var n = test6[5];
        if (n is record{}) {
            foreach var x in test5[4] {
                var temp = x.get("one");
                if(temp[2] is I[]) {
                    tableVal = tableVal + temp[1];
                }
            }
        }

     }
     assert(test2[1] is string, true);
     assert(test6[7][1] , "text");
     assert(test4[2][0][0], 2);
     assert(mapVal, 1);
     assert(tableVal, "hellohello");
}

type MyCyclicTuple [int, MyCyclicTuple[]];
type MyRecType int|[MyRecType...];

function testCastingToImmutableCyclicTuple() {
    MyCyclicTuple a = [1, [[2]]];
    (MyCyclicTuple & readonly)|error b = trap <MyCyclicTuple & readonly> a;
    assert(b is error, true);
    error err = <error> b;
    assert(err.message(), "{ballerina}TypeCastError");
    assert(<string> checkpanic err.detail()["message"], "incompatible types: 'MyCyclicTuple' cannot be " +
    "cast to '[int,(MyCyclicTuple[] & readonly)] & readonly'");
    MyCyclicTuple c = <[int, MyCyclicTuple[]] & readonly> [1, []];
    MyCyclicTuple & readonly d = <MyCyclicTuple & readonly> c;
    assert(d is [int, MyCyclicTuple[]] & readonly, true);
    anydata e = [1, 2, 3];
    MyRecType f = <MyRecType>(e.cloneReadOnly());
    assert(f is MyRecType[], true);
}

type RTuple [int, RTuple...];

public function recursiveTupleArrayCloneTest() {
   RTuple[] v = [];
   any x = v.cloneReadOnly();
   assertTrue(x is readonly & RTuple[]);
}

type RestTypeTuple [int, RestTypeTuple...];

function testRecursiveTupleWithRestType() {
   RestTypeTuple a = [1];
   RestTypeTuple b = [2, a, a, a];
   RestTypeTuple c = [3, a, b];

   assertTrue(a[0] is int);
   assertTrue(b[1] is RestTypeTuple);
   assertTrue(c[2] is RestTypeTuple);
}

public type Type string|Union|Tuple;

public type Union ["|", Type...];

public type Tuple ["tuple", Type, Type...]; 

type SubtypeRelation record {|
    Type subtype;
    Type superType;
|};

function testUnionWithCyclicTuplesHashCode() {
    Tuple tup1 = ["tuple", "never", "int"];
    Tuple tup2 = ["tuple", "never", ["|", "int", "string"]];

    Type subtype = ["|", "int", tup1];
    Type superType = ["|", ["|", "int", "float"], tup2];
    SubtypeRelation p = {subtype: subtype, superType: superType};
    assert(p.toJsonString(), "{\"subtype\":[\"|\", \"int\", [\"tuple\", \"never\", \"int\"]], " +
    "\"superType\":[\"|\", [\"|\", \"int\", \"float\"], [\"tuple\", \"never\", [\"|\", \"int\", \"string\"]]]}"); 
}

type List [List?];

function testCloneOnRecursiveTuples() {
    List list = [()];
    list[0] = list;
    List list_cloned = list.clone();
    test:assertTrue(list_cloned == list);
    test:assertFalse(list_cloned === list);

    List list_readonly = list.cloneReadOnly();
    test:assertTrue(list_readonly == list);
    test:assertFalse(list_readonly === list);
}

type Q1 [Q1];
type Q2 [Q2, Q2];
type Q3 [Q3, Q3...];
type Q4 [Q4, Q4, Q4...];
type Q5 [Q5]|[Q5, Q5]|[Q5...]|[Q5, Q5...]|[Q5, Q5, Q5...];
type Q6 [Q1];
type Q7 [Q1, Q2, Q3, Q4, Q5, Q6, Q7];
type Q8 [Q1?];

function testCyclicTuples() {
    Q1? q1 = ();
    Q2? q2 = ();
    Q3? q3 = ();
    Q4? q4 = ();
    Q5? q5 = ();
    Q6? q6 = ();
    Q7? q7 = ();
    Q8 q8 = [];
}

function assertTrue(anydata actual) {
    assert(true, actual);
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}
