// Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import tuples/tuple_type_project as tuples;

const INDEX_OUT_OF_RANGE = "{ballerina/lang.array}IndexOutOfRange";

function testTupleWithMemberAndRestDesc() {
    any anyVal = tuples:getTupleWithMemberAndRestDesc();
    assertTrue(anyVal is [int, string...]);

    [int, string...] val = tuples:getTupleWithMemberAndRestDesc();
    assertEquality(3, val.length());
    int i = tuples:getTupleWithMemberAndRestDesc()[0];
    assertEquality(1, i);
    string s1 = tuples:getTupleWithMemberAndRestDesc()[1];
    assertEquality("hello", s1);
    string s2 = tuples:getTupleWithMemberAndRestDesc()[2];
    assertEquality("world", s2);

    string|error res = trap tuples:getTupleWithMemberAndRestDesc()[3];
    assertTrue(res is error);
    error err = <error> res;
    assertEquality(INDEX_OUT_OF_RANGE, err.message());
    assertEquality("tuple index out of range: index: 3, size: 3", err.detail()["message"]);
}

function testTupleWithRestDescOnly() {
    any anyVal = tuples:getTupleWithRestDescOnly();
    assertFalse(anyVal is [int, string...]);
    assertFalse(anyVal is string[2]);
    assertTrue(anyVal is [string...]);
    assertTrue(anyVal is string[]);

    [string...] val = tuples:getTupleWithRestDescOnly();
    assertEquality(2, val.length());
    string s1 = tuples:getTupleWithRestDescOnly()[0];
    assertEquality("hello", s1);
    string s2 = tuples:getTupleWithRestDescOnly()[1];
    assertEquality("world", s2);

    string|error res = trap tuples:getTupleWithRestDescOnly()[3];
    assertTrue(res is error);
    error err = <error> res;
    assertEquality(INDEX_OUT_OF_RANGE, err.message());
    assertEquality("tuple index out of range: index: 3, size: 2", err.detail()["message"]);

    val[2] = "from Ballerina";
    assertEquality(3, val.length());
    assertEquality(2, tuples:getTupleWithRestDescOnly().length());
    string s3 = val[2];
    assertEquality("from Ballerina", s3);
}

function testEmptyTupleWithRestDescOnly() {
    any anyVal = tuples:getEmptyTupleWithRestDescOnly();
    assertFalse(anyVal is [int, string...]);
    assertFalse(anyVal is string[2]);
    assertTrue(anyVal is [string...]);
    assertTrue(anyVal is string[]);

    [string...] val = tuples:getEmptyTupleWithRestDescOnly();
    assertEquality(0, val.length());

    string|error res = trap tuples:getEmptyTupleWithRestDescOnly()[1];
    assertTrue(res is error);
    error err = <error> res;
    assertEquality(INDEX_OUT_OF_RANGE, err.message());
    assertEquality("tuple index out of range: index: 1, size: 0", err.detail()["message"]);

    val[2] = "from Ballerina";
    assertEquality(3, val.length());
    assertEquality("", val[0]);
    assertEquality("", val[1]);
    assertEquality("from Ballerina", val[2]);
}

function testTupleWithUnionRestDesc() {
    any anyVal = tuples:getTupleWithUnionRestDesc();
    assertFalse(anyVal is [int, string...]);
    assertFalse(anyVal is (int|string|boolean)[4]);
    assertTrue(anyVal is [int, (string|boolean)...]);
    assertTrue(anyVal is (int|string|boolean)[]);

    [int, (string|boolean)...] val = tuples:getTupleWithUnionRestDesc();
    assertEquality(4, val.length());
    int i = tuples:getTupleWithUnionRestDesc()[0];
    assertEquality(2, i);
    string|boolean sb1 = tuples:getTupleWithUnionRestDesc()[1];
    assertEquality("hello", sb1);
    string|boolean? sb2 = tuples:getTupleWithUnionRestDesc()[2];
    assertEquality(true, sb2);
    string|boolean sb3 = tuples:getTupleWithUnionRestDesc()[3];
    assertEquality(false, sb3);

    string|boolean|error res = trap tuples:getTupleWithUnionRestDesc()[5];
    assertTrue(res is error);
    error err = <error> res;
    assertEquality(INDEX_OUT_OF_RANGE, err.message());
    assertEquality("tuple index out of range: index: 5, size: 4", err.detail()["message"]);
}

function testTupleWithVar() {
    var a = tuples:getTupleWithMemberAndRestDesc();
    [int, string...] b = a;
    assertEquality(<[int, string...]> [1, "hello", "world"], b);

    var [c, ...d] = tuples:getTupleWithMemberAndRestDesc();
    assertEquality(1, c);
    assertEquality(<string[]> ["hello", "world"], d);

    var e = tuples:getTupleWithRestDescOnly();
    string[] f = e;
    assertEquality(<[string...]> ["hello", "world"], e);
    assertEquality(<[string...]> ["hello", "world"], f);

    var [...g] = tuples:getTupleWithRestDescOnly();
    assertEquality(<[string...]> ["hello", "world"], g);

    // https://github.com/ballerina-platform/ballerina-lang/issues/28326
    //var [...h] = tuples:getTupleWithMemberAndRestDesc();
    //(int|string)[] i = h;
    //[int, string...] j = h;
    //assertEquality(<(int|string)[]> [1, "hello", "world"], i);
    //assertEquality(<(int|string)[]> [1, "hello", "world"], j);
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("expected '" + (expected is error ? expected.toString() : expected.toString()) + "', found '" +
                    (actual is error ? actual.toString() : actual.toString()) + "'");
}
