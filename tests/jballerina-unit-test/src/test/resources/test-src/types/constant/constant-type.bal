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

function testTypesOfConstants() {
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
const map<string> CMS2 = {b : "C", c : "S"};
const map<map<string>> CMS3 = {a : {e : "C", f : "S"}, b : {g : "C", h : "S"}};
const map<map<string>> CMS4 = {d : {i : "C", j : "S"}, e : {k : "C", l : "S"}};

// const map<string> CMS12_CLONE = {...CMS1, ...CMS2}; // need to fix
const map<map<string>> CMS34_CLONE = {...CMS3, ...CMS4};

const int a = 1;
const map<int> CMI5 = {a};

const map<()> CN1 = {a : ()};
const map<map<()>> CN2 = {CN1};

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

function testTypesOfConstantMaps() {
    TYPE7 t7 = CMI4;
    TYPE8 t8 = CMF1;
    TYPE9 t9 = CMD1;
//  TYPE10 t10 = CMBT; // Uncomment after fixing #33889
    TYPE11 t11 = CMB1;
    TYPE12 t12 = CMS1;
    TYPE13 t13 = CN2;
    TYPE14 t14 = CMS34_CLONE;

    assertEqual(CMI4 is TYPE7, true);
    assertEqual(CMF1 is TYPE8, true);
    assertEqual(CMD1 is TYPE9, true);
    // assertEqual(CMBT is TYPE10, true); // Uncomment after fixing #33889
    assertEqual(CMB1 is TYPE11, true);
    assertEqual(CMS1 is TYPE12, true);

    assertEqual(t7, {a : {b : {a : 1}}, b : {a : {a : 1}, CMI2 : {b : 2, c : 3}, c : {d : 1}}});
    assertEqual(t8, {a : 0.11, b : 2.12});
    assertEqual(t9, {a : 0.11d, b : 2.12d});
    // assertEqual(t10, {a : 127, b : 255}); // Uncomment after fixing #33889
    assertEqual(t11, {a : true, b : false});
    assertEqual(t12, {a : "C", b : "S"});
    assertEqual(t13, {CN1 : {a : ()}});
    assertEqual(t14, {a : {e : "C", f : "S"}, b : {g : "C", h : "S"}, d : {i : "C", j : "S"}, e : {k : "C", l : "S"}});
}

function assertEqual(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(string `expected '${expected.toBalString()}', found '${actual.toBalString()}'`);
}

