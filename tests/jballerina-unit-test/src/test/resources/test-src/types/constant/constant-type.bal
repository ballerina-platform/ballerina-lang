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

const int CI1 = 2 + 1;
const int CI2 = 2 * 1;
const int CI3 = CI2;
const float CF3 = 10.0 + 2.0;
const float CF4 = 10.0 * 2.0;
// const byte CBT = 10 * 2; // Uncomment after fixing #33889
const decimal CD = 10.0 + 4;
const boolean CB = !(true);
const string CS = "C" + "S";

type TYPE1  2;
type TYPE2 5.0|20.0f;
type TYPE3 1d|14.0d;
type TYPE4 20;
type TYPE5 false;
type TYPE6 "CS";

function testTypesOfSimpleConstants() {
    CI1 ci1 = 3;
    CI2 ci2 = 2;
    CF3 cf3 = 12.0;
    CF4 cf4 = 20.0;
    CD cd = 14;
    CB cb = false;

    TYPE1 t1 = CI3;
    TYPE2 t2 = CF4;
    TYPE3 t3 = CD;
//  TYPE4 t4 = CBT; // Uncomment after fixing #33889
    TYPE5 t5 = CB;
    TYPE6 t6 = CS;

    assertEqual(CI1 is 3, true);
    assertEqual(4 is CI1, false);
    assertEqual(CI2 is 2, true);
    assertEqual(3 is CI2, false);
    assertEqual(CI3 is 3, false);
    assertEqual(2 is CI3, true);
    assertEqual(CF3 is 12.0, true);
    assertEqual(13.0 is CF3, false);
    assertEqual(CF4 is 20.0, true);
    assertEqual(CF4 is 15.0, false);
    assertEqual(14.0d is CD, true);
    assertEqual(13.0d is CD, false);
    assertEqual(CB is true, false);
    assertEqual(false is CB, true);

    assertEqual(t1, 2);
    assertEqual(t2, 20.0);
    assertEqual(t3, 14.0d);
//  assertEqual(t4, 20); // Uncomment after fixing #33889
    assertEqual(t5, false);
    assertEqual(t6, "CS");
}

const map<int> CMI1 = {a : 1};
const map<int> CMI2 = {b : 2, c : 3};
const map<map<int>> CMI3 = {a : CMI1, CMI2, c : {d : 1}};
const map<map<map<int>>> CMI4 = {a : {b : {a : 1}}, b : CMI3};
const map<float> CMF1 = {a : 0.11, b : 2.12};
const map<map<float>> CMF2 = {c : {d : 0.11, e : 2.12}, f : CMF1};
const map<decimal> CMD1 = {a : 0.11, b : 2.12};
const map<map<decimal>> CMD2 = {c : {d : 0.11, e : 2.12}, f : CMD1};
const map<byte> CMBT = {a : 127, b : 255}; // Uncomment after fixing #33889
const map<boolean> CMB1 = {a : true, b : false};
const map<string> CMS1 = {a : "C", b : "S"};
const map<map<string>> CMS3 = {a : {e : "C", f : "S"}, b : {g : "C", h : "S"}};
const map<map<string>> CMS4 = {d : {i : "C", j : "S"}, e : {k : "C", l : "S"}};

const map<map<string>> CMS34_CLONE = {...CMS3, ...CMS4};

const int a = 1;
const map<int> CMI5 = {a};

const map<()> CN1 = {a : ()};
const map<map<()>> CN2 = {CN1};

const map<int> empty = {};

type TYPE7 readonly & record {|
    readonly & record {|
        readonly & record {|
            1 a;
        |} b;
    |} a;
    readonly & record {|
        readonly & record {|
            1 a;
        |} a;
        readonly & record {|
            2 b;
            3 c;
        |} CMI2;
        readonly & record {|
            1 d;
        |} c;
    |} b;
|};

type TYPE8 readonly & record {|
    0.11 a;
    2.12 b;
|};

type TYPE9 readonly & record {|
    0.11d a;
    2.12d b;
|};

type TYPE10 readonly & record {|
    127 a;
    255 b;
|};

type TYPE11 readonly & record {|
    true a;
    false b;
|};

type TYPE12 readonly & record {|
    "C" a;
    "S" b;
|};

type TYPE13 readonly & record {|
    readonly & record {|
        () a;
    |} CN1;
|};

type TYPE14 readonly & record {|
    readonly & record {|
        "C" e;
        "S" f;
    |} a;
    readonly & record {|
        "C" g;
        "S" h;
    |} b;
    readonly & record {|
        "C" i;
        "S" j;
    |} d;
    readonly & record {|
        "C" k;
        "S" l;
    |} e;
|};

type TYPE15 readonly & record {||};

function testTypesOfConstantMaps() {
    TYPE7 t7 = CMI4;
    TYPE8 t8 = CMF1;
    TYPE9 t9 = CMD1;
//  TYPE10 t10 = CMBT; // Uncomment after fixing #33889
    TYPE11 t11 = CMB1;
    TYPE12 t12 = CMS1;
    TYPE13 t13 = CN2;
    TYPE14 t14 = CMS34_CLONE;
    TYPE15 t15 = empty;

    assertEqual(CMI4 is TYPE7, true);
    assertEqual(CMF1 is TYPE8, true);
    assertEqual(CMD1 is TYPE9, true);
    // assertEqual(CMBT is TYPE10, true); // Uncomment after fixing #33889
    assertEqual(CMB1 is TYPE11, true);
    assertEqual(CMS1 is TYPE12, true);
    assertEqual(empty is TYPE15, true);

    assertEqual(t7, {a : {b : {a : 1}}, b : {a : {a : 1}, CMI2 : {b : 2, c : 3}, c : {d : 1}}});
    assertEqual(t8, {a : 0.11, b : 2.12});
    assertEqual(t9, {a : 0.11d, b : 2.12d});
    // assertEqual(t10, {a : 127, b : 255}); // Uncomment after fixing #33889
    assertEqual(t11, {a : true, b : false});
    assertEqual(t12, {a : "C", b : "S"});
    assertEqual(t13, {CN1 : {a : ()}});
    assertEqual(t14, {a : {e : "C", f : "S"}, b : {g : "C", h : "S"}, d : {i : "C", j : "S"}, e : {k : "C", l : "S"}});
    assertEqual(t15, {});
}

const A = 1;

const map<int> B = {
    a: A,
    b: 2
};

const map<map<int>> C = {
    a: B,
    b: {
        a: 3
    }
};

function testConstTypesInline() {
    1 _ = A; // OK
    anydata a = A;
    assertTrue(a is 1);
    assertEqual(1, a);

    readonly & record {| 1 a; 2 b; |} _ = B; // OK
    anydata b = B;
    assertTrue(b is readonly & record {| 1 a; 2 b; |});
    assertEqual({a: 1, b: 2}, b);

    readonly & record {| record {| 1 a; 2 b; |} a; record {| 3 a; |} b; |} _ = C; // OK
    anydata c = C;
    assertTrue(c is readonly & record {| record {| 1 a; 2 b; |} a; record {| 3 a; |} b; |});
    assertEqual({a: {a: 1, b: 2}, b: {a: 3}}, c);
}

function testInvalidRuntimeUpdateOfConstMaps() {
    map<int> a = B;

    function () fn = function () {
        a["a"] = 1;
    };
    error? res = trap fn();
    assertInvalidUpdateError(res, "cannot update 'readonly' field 'a' in record of type 'record {| readonly 1 a; readonly 2 b; |} & readonly'");

    record {| 1 a; 2 b; |} b = C.a;
    fn = function () {
        b.b = 2;
    };
    res = trap fn();
    assertInvalidUpdateError(res, "cannot update 'readonly' field 'b' in record of type 'record {| readonly 1 a; readonly 2 b; |} & readonly'");

    map<map<int>> c = C;
    fn = function () {
        c["a"]["a"] = 2;
    };
    res = trap fn();
    assertInvalidUpdateError(res, "cannot update 'readonly' field 'a' in record of type 'record {| readonly 1 a; readonly 2 b; |} & readonly'");

    fn = function () {
        c["c"] = {};
    };
    res = trap fn();
    // https://github.com/ballerina-platform/ballerina-lang/issues/34798
    assertInvalidUpdateError(res, "invalid value for record field 'c': expected value of type 'never', found 'map<int>'");

    fn = function () {
        c["a"] = {a: 1, b: 2};
    };
    res = trap fn();
    assertInvalidUpdateError(res, "cannot update 'readonly' field 'a' in record of type " +
                                    "'record {| readonly (record {| 1 a; 2 b; |} & readonly & readonly) a; " +
                                    "readonly (record {| 3 a; |} & readonly & readonly) b; |} & readonly'");
}

type AA boolean;

type DECIMAL decimal;

type FLOAT float;

type INT int;

const TWO = 2;
const AA aa = !false;

const DECIMAL b = 60 + 2;
const DECIMAL c = 60 - 2;
const DECIMAL d = 60 * 2;
const DECIMAL e = 60 / 2;

const FLOAT b1 = 60 + 2;
const FLOAT c1 = 60 - 2;
const FLOAT d1 = 60 * 2;
const FLOAT e1 = 60 / 2;

const INT f = 60 & 2;
const INT g = 60 | 2;
const INT h = 60 ^ 2;
const INT j = 60 >> 2;
const INT k = 60 >>> 2;
const INT m = 60 << 2;
const INT n = ~60;
const INT p = +60;
const INT q = -60;

function testResolvingConstValForConstantsOfUserDefinedTypes() {
    assertTrue(aa);
    assertEqual(b, 62d);
    assertEqual(c, 58d);
    assertEqual(d, 120d);
    assertEqual(e, 30d);

    assertEqual(b1, 62f);
    assertEqual(c1, 58f);
    assertEqual(d1, 120f);
    assertEqual(e1, 30f);

    assertEqual(f, 0);
    assertEqual(g, 62);
    assertEqual(h, 62);
    assertEqual(j, 15);
    assertEqual(k, 15);
    assertEqual(m, 240);
    assertEqual(n, -61);
    assertEqual(p, 60);
    assertEqual(q, -60);
}

function assertInvalidUpdateError(error? res, string expectedDetailMessage) {
    assertTrue(res is error);
    error err = <error> res;
    assertEqual("{ballerina/lang.map}InherentTypeViolation", err.message());
    assertEqual(expectedDetailMessage, <string> checkpanic err.detail()["message"]);
}

function assertTrue(anydata actual) {
    assertEqual(true, actual);
}

function assertEqual(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(string `expected '${expected.toBalString()}', found '${actual.toBalString()}'`);
}

