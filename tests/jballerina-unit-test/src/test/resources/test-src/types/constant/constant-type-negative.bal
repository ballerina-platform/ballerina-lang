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

const int CI6 = 1 + 2;
const float CF1 = 1.0 + 2.0;
const decimal CD1 = 1.0 + 2.0;
// const byte CBT1 = 1; // Uncomment after fixing #33889
// const byte CBT2 = 2;
// const byte CBT3 = CBT1 + CBT2;
const boolean CB2 = !true;
const string CS2 = "1" + "2";

type TYPE1  4;
type TYPE2 5.0|4.0f;
type TYPE3 1d|2.0d;
type TYPE4 4;
type TYPE5 true;
type TYPE6 "a";

function userDefinedTypeTests() {
    CI6 ci6 = 4; // expected '3', found 'int'
    CF1 cf1 = 4.0; // expected '3.0f', found 'float'
    CD1 cd1 = 4.0; // expected '3.0d', found 'float'
//  CBT3 cbt3 = 4; // expected '3', found 'int' // Uncomment after fixing #33889
    CB2 cb2 = true; // expected 'false', found 'boolean'
    CS2 cs2 = "4"; // expected '12', found 'string'

    TYPE1 t1 = CI6; // expected 'TYPE1', found '3'
    TYPE2 t2 = CF1; // expected 'TYPE2', found '3.0f'
    TYPE3 t3 = CD1; // expected 'TYPE3', found '3.0d'
//  TYPE4 t4 = CBT3; // expected 'TYPE4', found '3' // Uncomment after fixing #33889
    TYPE5 t5 = CB2; // expected 'TYPE5', found 'false'
    TYPE6 t6 = CS2; // expected 'TYPE5', found '12'
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

const map<string> CMS1_CLONE = {...CMS1};
const map<string> CMS12_CLONE = {...CMS1, ...CMS2};

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
        |} b;
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

function testTypesOfConstantMaps() {
    TYPE12 t1 = CMI4;
    TYPE11 t2 = CMF1;
    TYPE10 t3 = CMD1;
//  TYPE9 t4 = CMBT; // Uncomment after fixing #33889
    TYPE8 t5 = CMB1;
    TYPE7 t6 = CMS1;
    TYPE7 t7 = CMS1_CLONE;
    TYPE7 t8 = CMS12_CLONE;
    TYPE7 t9 = CMI4;
    CMI1 cmi4 = CMI2;
    CMI4 cmi4 = {a : "C", b : "S"};
    CMF1 cmf1 = {a : 0.11, c : 2.12d};
    CMD1 cmd1 = {a : 0.11f, b : 2.12d};
//  CMBT cmbt = {a : "C", b : "S"};
    CMB1 cmb1 = {a : true};
    CMS1 cms1 = {a : "S", b : "C"};
    CMS1_CLONE cms1_clone = {};
    CMI5 cmi5 = {};
    CN2 cn2 = {a : ()};
}
