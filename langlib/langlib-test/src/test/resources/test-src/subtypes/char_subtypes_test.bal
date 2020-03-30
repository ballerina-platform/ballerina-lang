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
    s:Char a1 = "a";
    string t1 = a1;
    assertValueEqual("a", t1);
    assertNotError(trap <s:Char> t1);

    s:Char a2 = "µ";
    string t2 = a2;
    assertValueEqual("µ", t2);
    assertNotError(trap <s:Char> t2);

    string b = "ab";
    assertError(trap <s:Char> b);
}

type Char s:Char;

function testTypeAlias() {
    Char value = "a";
    string a = value;
    s:Char b = value;
    assertValueEqual("a", a);
    assertValueEqual("a", b);
}

function testConcat() {
    Char a = "a";
    Char b = "b";
    string c = "c";
    xml x = xml `<x/>`;
    assertTrue("a" == a);
    assertTrue("b" != a);
    assertTrue(a.length() == 1);

    string t1 = a + b;
    string t2 = a + c;
    string t3 = c + b;
    xml t4 = a + x;
    xml t5 = x + a;
    assertValueEqual("ab", t1);
    assertValueEqual("ac", t2);
    assertValueEqual("cb", t3);
    string sa = "a";
    xml xa = sa + x;
    xml ax = x + sa;
    assertTrue(xa == t4);
    assertTrue(ax == t5);
}

function testCharLangLib() {
    string s = "hello";
    int x = s.getCodePoint(0);
    s:Char|error result = s:fromCodePointInt(x);
    if(result is s:Char){
        assertValueEqual("h", result);
    } else {
        assertNotError(result); // Should fail.
    }

    s:Char h = <s:Char> result;
    assertValueEqual("h", h);

    int y = h.toCodePointInt();
    assertValueEqual(x, y);
}

function testList(){
    s:Char[] chars = ["a", "b", "c"];
    string[] strings = chars;

    assertNotError(trap <s:Char> chars[0]);
    assertValueEqual("a", chars[0]);
    assertNotError(trap <s:Char[]> strings);
    assertError(trap insertListValue(chars, 5, "b")); // Error due to no filler value;
    assertNotError(trap insertListValue(chars, 3, "b"));
    assertError(trap insertListValue(chars, 3, "ab"));

    string[] notChars = ["a", "ab", "c"];
    assertError(trap <s:Char[]> notChars);
    string[] someChars = ["a", "p", "q"];
    assertError(trap <s:Char[]> someChars);
    assertTrue(!(someChars is s:Char[]));

    [string, s:Char] x = ["ab", "c"];
    s:Char t1 = x[1];
    string t2 = x[1];
    assertValueEqual("c", t1);
    assertValueEqual("c", t2);
}

function testMapping() {

    map<s:Char> m1 = {};
    m1["k1"] = "a";
    string? t0 = m1["k1"];
    assertValueEqual("a", t0);
    map<string> t1 = m1;

    assertError(trap insertMapValue(t1, "k2", "abc"));

    record {
        s:Char i;
    } rec = { i : "a"};
    rec.i = "b";
    assertValueEqual("b", rec.i);

    record { string i;} t3 = rec;
    assertError(trap updateRecord(t3, "abc"));
}
function insertListValue(string[] list, int pos, string value) {
    list[pos] = value;
}

function insertMapValue(map<string> m, string key, string value) {
    m[key] = value;
}

function updateRecord(record { string i;} rec, string value) {
    rec.i = value;
}

// Test Functions

function assertValueEqual(anydata expected, anydata actual) {
    if(expected != actual) {
        error e = error("Not Equal", message = "expected: " + expected.toString() + ", found: " + actual.toString());
        panic e;
    }
}

function assertError(anydata|error value) {
    if !(value is error) {
        error e = error("Not Error", message = "expected: Error, found: " + value.toString());
        panic e;
    }
}

function assertNotError(anydata|error value) {
    if (value is error) {
        error e = error("Not Error", message = "expected: Error, found: " + value.toString());
        panic e;
    }
}

function assertTrue(boolean value) {
    if !(value) {
        error e = error("Not True", message = "expected: true, found: " + value.toString());
        panic e;
    }
}

function assertFalse(boolean value) {
    if (value) {
        error e = error("Not False", message = "expected: false, found: " + value.toString());
        panic e;
    }
}
