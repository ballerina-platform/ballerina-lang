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

function assertEqual(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(string `expected '${expected.toBalString()}', found '${actual.toBalString()}'`);
}
