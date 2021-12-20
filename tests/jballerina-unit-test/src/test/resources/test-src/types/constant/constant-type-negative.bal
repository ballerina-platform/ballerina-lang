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
    CS2 cs2 = "4"; // expected '"12"', found 'string'

    TYPE1 t1 = CI6; // expected 'TYPE1', found '3'
    TYPE2 t2 = CF1; // expected 'TYPE2', found '3.0f'
    TYPE3 t3 = CD1; // expected 'TYPE3', found '3.0d'
//  TYPE4 t4 = CBT3; // expected 'TYPE4', found '3' // Uncomment after fixing #33889
    TYPE5 t5 = CB2; // expected 'TYPE5', found 'false'
    TYPE6 t6 = CS2; // expected 'TYPE5', found '12'
}
