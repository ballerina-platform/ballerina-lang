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

type B_TTDC [int, map<B_TTDC>];

public function testCycleTypeMap() {
    B_TTDC d = [1, {one: [1]}];

    int numberResult = 0;
    if (d[1] is map<B_TTDC>) {
        numberResult = d[1].get("one")[0];
    }
    assert(numberResult is int, true);
    assert(numberResult, 1);
}

type PersonTTDC record {
    int id;
    string name;
};

type CTTDC [PersonTTDC, table<map<CTTDC>>];

public function testCycleTypeTable() {
    table<map<CTTDC>> tb = table [
        {
            one: [{id: 1, name: "Jane"}]
        },
        {
            one: [{id: 1, name: "Anne"}]
        }
    ];

    string names = "";
    foreach var x in tb {
        var a = x.get("one");
        if (a[0] is PersonTTDC) {
            names = names + a[0].name;
        }
    }
    assert(names, "JaneAnne");
}

type DTTDC [int, map<DTTDC>];

public function testCyclicAsFunctionParams() {
    DTTDC d = [1, {elem: [2]}];
    map<DTTDC> c = takeCyclicTyDef(d);
    assert(c.get("elem")[0], 5);
}

function takeCyclicTyDef(DTTDC d) returns map<DTTDC> {
    if (d is [int, map<DTTDC>]) {
        d[1]["elem"][0] = 5;
    }
    return d[1];
}

type ETTDC [int, record {ETTDC a?;}];

public function testCyclicTypeDefInRecord() {
    ETTDC rec = [1, {}];
    int result = 0;
    if (rec[1] is record {}) {
        result = rec[0];
    }
    assert(result, 1);
}

type FTTDC [int, string, FTTDC[]|int|(), map<FTTDC>];

public function testCyclicTypeDefInUnion() {
    int|FTTDC values = [1, "hello"];
    assert((<FTTDC>values)[0], 1);
    assert((<FTTDC>values)[1], "hello");
    assert((<FTTDC>values)[2], ());
}

type XType1TTDC XNil|XBoolean|XInt|XString|XTuple1TTDC|XUnion1TTDC|XIntersection1TTDC|XNever|XAny|();

type XType2TTDC XNil|XBoolean|XInt|XString|XTuple2TTDC|XUnion2TTDC|XIntersection2TTDC|XNever|XAny|();

type XType3TTDC XNil|XTuple3TTDC[]|XNever|XAny|();

type P [XNil, XUnion4TTDC, XIntersection4TTDC, XAny, XListRefTTDC, XFunctionRefTTDC];

const XNil = "nil";
const XBoolean = "boolean";
const XInt = "int";
const XString = "string";
const XNever = "never";
const XAny = "any";

type XTuple1TTDC ["tuple", XType1TTDC, XType1TTDC];

type XUnion1TTDC ["union", XType1TTDC, XType1TTDC];

type XIntersection1TTDC ["intersection", XType1TTDC, XType1TTDC];

type ApproxTypeTTDC string|ApproxTypeTTDC[];

type XTuple2TTDC ["tuple", ApproxTypeTTDC, ApproxTypeTTDC];

type XUnion2TTDC ["union", ApproxTypeTTDC, ApproxTypeTTDC];

type XIntersection2TTDC ["intersection", ApproxTypeTTDC, ApproxTypeTTDC];

type XTuple3TTDC ["tuple", XType3TTDC, XType3TTDC];

type XUnion4TTDC ["|", P...];

type XIntersection4TTDC "&"|P;

type ListDefTTDC P[];

type FunctionDefTTDC P[2];

type DefsTTDC record {|
    ListDefTTDC[] listDefs;
    FunctionDefTTDC[] functionDefs;
|};

type XListRefTTDC ["listRef", int];

type XFunctionRefTTDC ["functionRef", int];

type mapDefTTDC map<P>;

type tupleDefTTDC [P...];

public function testCyclicUserDefinedTypes() {
    XType1TTDC a = ["tuple"];
    assert(a is XTuple1TTDC, true);
    assert(a is string, false);

    ApproxTypeTTDC c = ["text1"];
    assert(c is ApproxTypeTTDC[], true);

    XUnion2TTDC b = ["union", "text1", "text2"];
    assert(b[1] is ApproxTypeTTDC, true);

    XType3TTDC d = [["tuple"], ["tuple"]];
    assert(d is XTuple3TTDC[], true);
}

type JTTDC [int, JTTDC[2]];

type KTTDC [int, JTTDC[]];

function testIndirectRecursion() {
    P test1 = ["nil", ["|"], "&", "any", ["listRef"], ["functionRef", 2]];
    assert(test1[2] is XIntersection4TTDC, true);

    FunctionDefTTDC test2 = [["nil", [], "&"], ["nil", [], "&"]];
    assert(test2[0][1] is XUnion4TTDC, true);

    ListDefTTDC test3 = [["nil", [], ["nil", [], "&"]]];

    if (test3[0][2] is XIntersection4TTDC) {
        test2[1][2] = ["nil", [], "&"];
    }
    assert((<P>test2[1][2])[2].toString(), "&");

    DefsTTDC test4 = {
        listDefs: [[], [], []],
        functionDefs: []
    };
    assert(test4.listDefs[0] is P[], true);

    mapDefTTDC test5 = {one: ["nil", [], "&"]};
    tupleDefTTDC test6;
    if (test5.get("one")[0] is XNil) { // always true
        test6 = [["nil", ["|"], "&"]];
    }
    assert(test6[0][1][0] is string, true);

    KTTDC tempTuple = [1, []];
    assert(tempTuple[1] is JTTDC[], true);
}

type GTTDC [int, string, GTTDC...];

type HTTDC [int, HTTDC[], string, HTTDC...];

type TTTDC [int, (int|TTTDC)...];

public function testCyclicRestType() {
    GTTDC a = [1, "text"];
    GTTDC b = [1, "text1", [2, "text2"], [3, "text3"]];
    assert(b[2] is GTTDC, true);

    HTTDC c = [1, [[1]], "text2"];
    HTTDC d = [1, [[2]], "text2", [3]];
    assert(d[1] is HTTDC[], true);
    assert(d[3] is HTTDC, true);

    TTTDC x = [0];
    x[1] = 1;
    assert(x[1] is int|TTTDC, true);
}

type ITTDC [int, string, ITTDC[], map<ITTDC>, table<map<ITTDC>>, record {ITTDC a?; float x?;}, ITTDC...];

public function testComplexCyclicTuple() {
    string sampleString = "text";
    ITTDC test1 = [1];
    ITTDC test2 = [1, sampleString];
    ITTDC test3 = [1, "hello", [[1, sampleString]]];
    ITTDC test4 = [
        1,
        "hello",
        [[1, sampleString]],
        {
            first: test1,
            second: test3
        }
    ];

    table<map<ITTDC>> tb = table [
        {
            one: test3
        },
        {
            one: test4
        }
    ];

    ITTDC test5 = [
        1,
        "hello",
        [[1, sampleString]],
        {
            first: test4,
            second: test3
        },
        tb
    ];

    ITTDC test6 = [
        1,
        sampleString,
        [[1, "hello"]],
        {
            first: test1,
            second: test5
        },
        tb,
        {},
        test1,
        test2,
        test3,
        test4,
        test5
    ];

    int mapVal = 0;
    string tableVal = "";
    if (test6[1] is string) {
        test4[2][0][0] = 2;

        var m = test5[3];
        if (m is map<ITTDC>) {
            mapVal = m.get("second")[0];
        }

        var n = test6[5];
        if (n is record {}) {
            foreach var x in test5[4] {
                var temp = x.get("one");
                if (temp[2] is ITTDC[]) {
                    tableVal = tableVal + temp[1];
                }
            }
        }

    }
    assert(test2[1] is string, true);
    assert(test6[7][1], "text");
    assert(test4[2][0][0], 2);
    assert(mapVal, 1);
    assert(tableVal, "hellohello");
}

type MyCyclicTupleTTDC [int, MyCyclicTupleTTDC[]];

type MyRecTypeTTDC int|[MyRecTypeTTDC...];

function testCastingToImmutableCyclicTuple() {
    MyCyclicTupleTTDC a = [1, [[2]]];
    (MyCyclicTupleTTDC & readonly)|error b = trap <MyCyclicTupleTTDC & readonly>a;
    assert(b is error, true);
    error err = <error>b;
    assert(err.message(), "{ballerina}TypeCastError");
    assert(<string>checkpanic err.detail()["message"], "incompatible types: 'MyCyclicTupleTTDC' cannot " +
        "be cast to '(MyCyclicTupleTTDC & readonly)'");
    MyCyclicTupleTTDC c = <[int, MyCyclicTupleTTDC[]] & readonly>[1, []];
    MyCyclicTupleTTDC & readonly d = <MyCyclicTupleTTDC & readonly>c;
    assert(d is [int, MyCyclicTupleTTDC[]] & readonly, true);
    anydata e = [1, 2, 3];
    MyRecTypeTTDC f = <MyRecTypeTTDC>(e.cloneReadOnly());
    assert(f is MyRecTypeTTDC[], true);
}

type RTupleTTDC [int, RTupleTTDC...];

public function recursiveTupleArrayCloneTest() {
    RTupleTTDC[] v = [];
    any x = v.cloneReadOnly();
    assertTrue(x is readonly & RTupleTTDC[]);
}

type RestTypeTupleTTDC [int, RestTypeTupleTTDC...];

function testRecursiveTupleWithRestType() {
    RestTypeTupleTTDC a = [1];
    RestTypeTupleTTDC b = [2, a, a, a];
    RestTypeTupleTTDC c = [3, a, b];

    assertTrue(a[0] is int);
    assertTrue(b[1] is RestTypeTupleTTDC);
    assertTrue(c[2] is RestTypeTupleTTDC);
}

public type TypeTTDC string|UnionTTDC|TupleTTDC;

public type UnionTTDC ["|", TypeTTDC...];

public type TupleTTDC ["tuple", TypeTTDC, TypeTTDC...];

type SubtypeRelationTTDC record {|
    TypeTTDC subtype;
    TypeTTDC superType;
|};

function testUnionWithCyclicTuplesHashCode() {
    TupleTTDC tup1 = ["tuple", "never", "int"];
    TupleTTDC tup2 = ["tuple", "never", ["|", "int", "string"]];

    TypeTTDC subtype = ["|", "int", tup1];
    TypeTTDC superType = ["|", ["|", "int", "float"], tup2];
    SubtypeRelationTTDC p = {subtype: subtype, superType: superType};
    assert(p.toJsonString(), "{\"subtype\":[\"|\", \"int\", [\"tuple\", \"never\", \"int\"]], " +
            "\"superType\":[\"|\", [\"|\", \"int\", \"float\"], [\"tuple\", \"never\", [\"|\", \"int\", \"string\"]]]}");
}

type ListTTDC [ListTTDC?];

function testCloneOnRecursiveTuples() {
    ListTTDC list = [()];
    list[0] = list;
    ListTTDC list_cloned = list.clone();
    test:assertTrue(list_cloned == list);
    test:assertFalse(list_cloned === list);

    ListTTDC list_readonly = list.cloneReadOnly();
    test:assertTrue(list_readonly == list);
    test:assertFalse(list_readonly === list);
}

type Q1TTDC [Q1TTDC];

type Q2TTDC [Q2TTDC, Q2TTDC];

type Q3TTDC [Q3TTDC, Q3TTDC...];

type Q4TTDC [Q4TTDC, Q4TTDC, Q4TTDC...];

type Q5TTDC [Q5TTDC]|[Q5TTDC, Q5TTDC]|[Q5TTDC...]|[Q5TTDC, Q5TTDC...]|[Q5TTDC, Q5TTDC, Q5TTDC...];

type Q6TTDC [Q1TTDC];

type Q7TTDC [Q1TTDC, Q2TTDC, Q3TTDC, Q4TTDC, Q5TTDC, Q6TTDC, Q7TTDC];

type Q8TTDC [Q1TTDC?];

function testCyclicTuples() {
    Q1TTDC? q1 = ();
    Q2TTDC? q2 = ();
    Q3TTDC? q3 = ();
    Q4TTDC? q4 = ();
    Q5TTDC? q5 = ();
    Q6TTDC? q6 = ();
    Q7TTDC? q7 = ();
    Q8TTDC q8 = [];
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
