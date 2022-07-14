// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type CustomTupleType [int, string, boolean];
type CustomTupleType2 [int...];
type CustomTupleType3 [int, string, boolean...];

int index = 0;

function testTupleAccessNegative() {
    [int, string, boolean] t = [1, "a", true];

    string _ = t[0];
    int _ = t[1];

    string _ = t[index];
    int _ = t[index + 1];

    var x1 = t[0];
    string _ = x1;

    var x2 = t[1];
    int _ = x2;

    var x3 = t[index];
    string _ = x3;

    var x4 = t[index + 1];
    int _ = x4;
}

function testTupleWithRestTypesAccessNegative() {
    [int...] t1 = [1, 2, 3, 5];
    [int, string, boolean...] t2 = [1, "a", true, true];

    string _ = t1[0];
    boolean _ = t2[0];
    int _ = t2[3];

    string _ = t1[index];
    boolean _ = t2[index];
    int _ = t2[index + 3];

    var x1 = t1[0];
    string _ = x1;

    var x2 = t2[0];
    boolean _ = x2;

    var x3 = t2[3];
    int _ = x3;

    var x4 = t1[index];
    string _ = x4;

    var x5 = t2[index];
    boolean _ = x5;

    var x6 = t2[index + 3];
    int _ = x6;
}

function testCustomTupleTypesAccessNegative() {
    CustomTupleType t1 = [1, "a", true];
    CustomTupleType2 t2 = [1, 2, 3, 5];
    CustomTupleType3 t3 = [1, "a", true, true];

    string _ = t1[0];
    int _ = t1[1];
    string _ = t2[0];
    boolean _ = t3[0];
    int _ = t3[3];

    string _ = t1[index];
    int _ = t1[index + 1];
    string _ = t2[index];
    boolean _ = t3[index];
    int _ = t3[index + 3];

    var x1 = t1[0];
    string _ = x1;

    var x2 = t1[1];
    int _ = x2;

    var x3 = t2[0];
    string _ = x3;

    var x4 = t3[0];
    boolean _ = x4;

    var x5 = t3[3];
    int _ = x5;

    var x6 = t1[index];
    string _ = x6;

    var x7 = t1[index + 1];
    int _ = x7;

    var x8 = t2[index];
    string _ = x8;

    var x9 = t3[index];
    boolean _ = x9;

    var x10 = t3[index + 3];
    int _ = x10;
}
