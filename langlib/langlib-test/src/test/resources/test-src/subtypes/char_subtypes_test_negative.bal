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