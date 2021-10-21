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

import ballerina/lang.'string as s;

function testValueAssignment() {
    s:Char a = "a";
    s:Char b = "ab"; // Error

    string c = a;
    s:Char d = c; // Error
    s:Char e = <s:Char> c;
}

function testConcat() {
    s:Char a = "a";
    s:Char b = "b";
    string cc = "c";

    s:Char t1 = a + b;
    s:Char t2 = a + cc;
    s:Char t3 = cc + b;
}

function testListAndMap(){
    s:Char[] chars = ["a", "bb", "c"];

    record {
        s:Char i;
    } rec = { i : "aa"};
}

function testLangLib() {
    string h = "abc";
    int y = h.toCodePointInt(); // Error
}

type X "ab"|"b";
type Y -1|"e"|"f";

function testFiniteTypeAsStringSubType() {
    X a = "ab";
    string:Char b = a;

    X[] c = ["ab", "b"];
    string:Char[] d = c;

    Y[] e = ["e", "e", -1];
    string:Char[] f = e;
    (int:Unsigned8|string:Char)[] g = e;
}

type StringFiniteType "ABC" | "D" | 3.0f;
type StringType s:Char|string|int;

function testStringSubtypesWithLangLibFunctions() {
    StringFiniteType str1 = "D";
    StringType str2 = "D";
    s:Char|string str3 = "d";

    _ = string:toLowerAscii(str1);
    _ = string:toLowerAscii(str2);

    _ = str1.toLowerAscii();
    _ = str2.toLowerAscii();

    _ = string:toLowerAscii(str3);
    _ = str3.toLowerAscii();
}
