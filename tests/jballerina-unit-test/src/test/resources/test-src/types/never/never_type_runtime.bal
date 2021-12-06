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

function func1() returns never {
    panic error("error!");
}

function func2() returns record {| never x; |} {
    panic error("error!");
}

function func3() returns int {
    panic error("error!");
}

function testNeverRuntime1() {
    boolean b = func1 is function () returns record {| never x; |};
    assertEquality(true, b);
}

function testNeverRuntime2() {
    boolean b = func1 is function () returns never;
    assertEquality(true, b);
}

function testNeverRuntime3() {
    boolean b = func1 is function () returns int;
    assertEquality(true, b);
}

function testNeverRuntime4() {
    boolean b = func2 is function () returns never;
    assertEquality(true, b);
}

function testNeverRuntime5() {
    boolean b = func2 is function () returns [never];
    assertEquality(true, b);
}

function testNeverRuntime6() {
    boolean b = func2 is function () returns string;
    assertEquality(true, b);
}

function testNeverRuntime7() {
    boolean b = func3 is function () returns never;
    assertEquality(false, b);
}

function testNeverRuntime8() {
    boolean b = func3 is function () returns record {| never x; |};
    assertEquality(false, b);
}

function testNeverRuntime9() {
    map<never> x = {};
    boolean b = x is map<string>;
    assertEquality(true, b);
}

function testNeverRuntime10() {
    int x = 100;
    boolean b = x is never;
    assertEquality(false, b);
}

type Record record {|
    int i;
    never[] j;
|};

type Record2 record {|
    int i;
    never[] j = [];
|};

function testNeverRuntime11() {
    Record x = { i: 1, j: [] };
    boolean b = x is never;
    assertEquality(false, b);
}

function testNeverRuntime12() {
    Record x = {i: 1, j: []};
    boolean b = x is Record2;
    assertEquality(true, b);
}

function testNeverWithAnyAndAnydataRuntime() {
    map<never> a = {};
    never[] b = [];

    anydata m = a;
    assertEquality(true, m is map<any>);
    assertEquality(true, m is any);

    any y1 = a;
    assertEquality(true, y1 is map<anydata>);
    assertEquality(true, y1 is anydata);

    anydata n = b;
    assertEquality(true, n is any[]);
    assertEquality(true, n is any);

    any y2 = b;
    assertEquality(true, y2 is anydata[]);
    assertEquality(true, y2 is anydata);

    boolean c1 = baz1 is function () returns map<anydata>;
    assertEquality(true, c1);

    boolean c2 = baz2 is function () returns anydata[];
    assertEquality(true, c2);

    boolean c3 = baz1 is function () returns map<any>;
    assertEquality(true, c3);

    boolean c4 = baz2 is function () returns any[];
    assertEquality(true, c4);
}

type NeverUnion (never|string);

function testNeverFieldTypeCheck() {

    // Check checkIsType type-checker method
    record {} r1 = {"x": 2, "color": "blue"};
    assertEquality(false, r1 is record {never x?;});

    record {int x;} r2 = {x: 2, "color": "blue"};
    assertEquality(false, r2 is record {never x?;});

    record {never? x;} r3 = {x: (), "color": "blue"};
    assertEquality(false, r3 is record {never x?;});

    record {int? x;} r4 = {x: 2, "color": "blue"};
    assertEquality(false, r4 is record {never x?;});

    record {} r5 = {};
    assertEquality(false, r5 is record {never x?;});

    record {} r6 = {"color": "blue"};
    assertEquality(false, r6 is record {never x?;});

    record {|never...; |} r7 = {};
    assertEquality(true, r7 is record {never x?;});

    record {|never?...; |} r8 = {};
    assertEquality(true, r8 is record {never x?;});

    record {|int?...; |} r9 = {};
    assertEquality(false, r9 is record {never x?;});

    record {||} r10 = {};
    assertEquality(true, r10 is record {never x?;}); 

    record {|int|(never|string)...; |} r11 = {};
    assertEquality(false, r11 is record {never x?;});

    record {|int|NeverUnion...; |} r12 = {};
    assertEquality(false, r12 is record {never x?;});

    record {never x?;} r13 = {};
    assertEquality(false, r13 is record {|int|(never|string)...; |});

    record {|never x?;|} r14 = {};
    assertEquality(true, r14 is record {|int|(never|string)...; |});

    record {never x?;} r15 = {};
    assertEquality(false, r15 is record {|int|NeverUnion...; |});

    record {|never x?;|} r16 = {};
    assertEquality(true, r16 is record {|int|NeverUnion...; |});

    // Check compilation of never field binding
    record {|never...; |} x1 = {};
    record {never i?;} y1 = x1;
    assertEquality(true, y1 is record {|never...; |});

    record {|never?...; |} x2 = {};
    record {never i?;} y2 = x2;
    assertEquality(true, y2 is record {|never?...; |});

    record {||} x3 = {};
    record {never i?;} y3 = x3;
    assertEquality(true, y3 is record {||});

    record {|int j;|} x4 = {j: 1};
    record {never i?; int j;} y4 = x4;
    assertEquality(true, y4 is record {|int j;|});

    // Check checkIsLikeType type-checker method
    record {} & readonly v1 = {"x": 2, "color": "blue"};
    assertEquality(false, v1 is record {never x?;});

    record {} & readonly v2 = {"x": 2};
    assertEquality(false, v2 is record {never x?;});

    record {int x;} & readonly v3 = {x: 2, "color": "blue"};
    assertEquality(false, v3 is record {never x?;});

    record {never? x;} & readonly v4 = {x: (), "color": "blue"};
    assertEquality(false, v4 is record {never x?;});

    record {never? x;} & readonly v5 = {x: (), "color": "blue"};
    assertEquality(true, v5 is record {never? x;});

    record {never? x;} & readonly v6 = {x: (), "color": "blue"};
    assertEquality(true, v6 is record {never? x;});

    record {} & readonly v7 = {};
    assertEquality(true, v7 is record {never x?;});

    record {} & readonly v8 = {"color": "blue"};
    assertEquality(true, v8 is record {never x?;});

    record {never x?;} v9 = {};
    anydata result = (<anydata>v9).cloneReadOnly();
    assertEquality(true, result is record {|int|(never|string)...; |});

    record {never x?;} v10 = {};
    result = (<anydata>v10).cloneReadOnly();
    assertEquality(true, result is record {|int|NeverUnion...; |});
}

function baz1() returns map<never> {
    return {};
}

function baz2() returns never[] {
    return [];
}

function testNeverRestFieldType() {
    record {|never...; |} a = {};
    record {||} copy = a;
    assertEquality(true, copy == {});
    assertEquality(true, copy is record {||});
    assertEquality(true, copy is record {|never...; |});

    record {||} a1 = {};
    record {|never...; |} copy2 = a1;
    assertEquality(true, copy2 == {});
    assertEquality(true, copy2 is record {||});
    assertEquality(true, copy2 is record {|never...; |});

    record {|int x; never...; |} a2 = {x: 12};
    record {|int x;|} copy3 = a2;
    assertEquality(true, copy3 == {x: 12});
    assertEquality(true, copy3 is record {|int x;|});
    assertEquality(true, copy3 is record {|int x; never...; |});

    record {|int x;|} a3 = {x: 12};
    record {|int x; never...; |} copy4 = a3;
    assertEquality(true, copy4 == {x: 12});
    assertEquality(true, copy4 is record {|int x;|});
    assertEquality(true, copy4 is record {|int x; never...; |});

    record {|int x?; never...; |} a4 = {};
    record {|int x?;|} copy5 = a4;
    assertEquality(true, copy5 == {});
    assertEquality(true, copy5 is record {|int x?;|});
    assertEquality(true, copy5 is record {|int x?; never...; |});

    record {|int x?;|} a5 = {};
    record {|int x?; never...; |} copy6 = a5;
    assertEquality(true, copy6 == {});
    assertEquality(true, copy6 is record {|int x?;|});
    assertEquality(true, copy6 is record {|int x?; never...; |});

    record {|int? x; never...; |} a6 = {x: ()};
    record {|int? x;|} copy7 = a6;
    assertEquality(true, copy7 == {x: ()});
    assertEquality(true, copy7 is record {|int? x;|});
    assertEquality(true, copy7 is record {|int? x; never...; |});

    record {|int? x; never...; |} a7 = {x: ()};
    record {|int? x;|} copy8 = a7;
    assertEquality(true, copy8 == {x: ()});
    assertEquality(true, copy8 is record {|int? x;|});
    assertEquality(true, copy8 is record {|int? x; never...; |});

    function () returns record {|never...; |} c = () => {};
    assertEquality(true, c is function () returns record {||});

    function () returns record {||} d = () => {};
    assertEquality(true, d is function () returns record {|never...; |});

    function () returns record {|int i; never...; |} e = () => {i: 2};
    assertEquality(false, <any>e is function () returns record {|int i; string s;|});

    function () returns record {|int i; string s;|} f = () => {i: 2, s: "s"};
    assertEquality(false, <any>f is function () returns record {|int i; never...; |});

    map<record {|never...; |}> g = {};
    assertEquality(true, g is map<record {||}>);

    map<record {||}> h = {};
    assertEquality(true, h is map<record {|never...; |}>);

    record {|never...; |}[] i = [];
    assertEquality(true, i is record {||}[]);

    record {||}[] j = [];
    assertEquality(true, j is record {|never...; |}[]);

    record {|never...; |} k = {};
    assertEquality(true, k is record {||});

    record {||} l = {};
    assertEquality(true, l is record {|never...; |});
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
