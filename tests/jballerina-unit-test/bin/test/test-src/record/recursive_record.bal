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

type RecursiveRecordWithUnionOne record {
    int i;
    RecursiveRecordWithUnionOne|int r;
};

type RecursiveRecordWithUnionTwo record {
    string i;
    RecursiveRecordWithUnionTwo|RecursiveRecordWithUnionOne r;
};

function testRecursiveRecordWithUnion() {
    RecursiveRecordWithUnionOne a = {i:4, r:{i:4, r:5}};
    assertEquality(4, a["i"]);
    RecursiveRecordWithUnionTwo b = {i: "a", r:{i:4, r:5}};
    assertEquality("a", b["i"]);
    RecursiveRecordWithUnionTwo c = {i: "a", r:b};
    assertEquality("a", c["i"]);
}

type RecursiveRecordWithTupleArray record {
    int i;
    [RecursiveRecordWithTupleArray[]] r;
};

function testRecursiveRecordWithTuple() {
    RecursiveRecordWithTupleArray a = {i:4, r:[]};
    assertEquality(4, a["i"]);
    RecursiveRecordWithTupleArray b = {i:5, r:[[a]]};
    assertEquality(5, b["i"]);
}

type RecusriveRecordTypeWithMapOne record {
    int i;
    map<RecusriveRecordTypeWithMapOne> r;
};

function testRecursiveRecordWithMap() {
    RecusriveRecordTypeWithMapOne a = {i:3, r:{}};
    assertEquality(3, a["i"]);
    RecusriveRecordTypeWithMapOne b = {i:5, r:{"b":a}};
    assertEquality(5, b["i"]);
}

type RecursiveRecordWithArray record {
    int i;
    RecursiveRecordWithArray[] r;
};

function testRecursiveRecordWithArray() {
    RecursiveRecordWithArray a = {i:4, r:[]};
    assertEquality(4, a["i"]);
    RecursiveRecordWithArray b = {i:5, r:[a]};
    assertEquality(5, b["i"]);
}

type RecursiveRecordWithRestType record {|
    int i;
    RecursiveRecordWithRestType...;
|};

function testRecursiveRecordWithRestType() {
    RecursiveRecordWithRestType a = {i:4};
    assertEquality(4, a["i"]);
    RecursiveRecordWithRestType b = {i:5, "a":a};
    assertEquality(5, b["i"]);
    RecursiveRecordWithRestType c = {i:5, "a":a, "b":b};
    assertEquality(5, b["i"]);
}

type RecursiveRecordWithOptionalType record {|
    int i;
    RecursiveRecordWithOptionalType r?;
|};

function testRecursiveRecordWithOptionalType() {
    RecursiveRecordWithOptionalType a = {i:4};
    assertEquality(4, a["i"]);
    RecursiveRecordWithOptionalType b = {i:5, r:a};
    assertEquality(5, b["i"]);
}

type RecursiveRecordWithReadOnlyType record {|
    int i;
    readonly RecursiveRecordWithReadOnlyType|int r;
|};

function testRecursiveRecordWithReadOnlyType() {
    RecursiveRecordWithReadOnlyType a = {i:4, r:5};
    assertEquality(4, a["i"]);
    RecursiveRecordWithReadOnlyType b = {i:5, r:{i:6, r:6}};
    assertEquality(5, b["i"]);
}

function testRecursiveRecordEquality() {
    RecursiveRecordWithOptionalTypeA a1 = {i:4};
    RecursiveRecordWithOptionalTypeB b1 = {i:4};
    assertTrue(checkValueEquality(a1,b1));
    assertFalse(checkRefEquality(a1,b1));

    RecursiveRecordWithOptionalTypeA a2 = {i:5, f:a1};
    RecursiveRecordWithOptionalTypeB b2 = {i:5};
    assertFalse(checkRefEquality(a2, b2));
    assertFalse(checkValueEquality(a2, b2));

    RecursiveRecordWithUnionOne a3 = {i:4, r:{i:4, r:5}};
    RecursiveRecordWithUnionOne b3 = {i:4, r:{i:4, r:5}};
    assertTrue(checkValueEquality(a3,b3));
    assertFalse(checkRefEquality(a3,b3));

    RecursiveRecordWithUnionTwo a4 = {i: "a", r:{i:4, r:5}};
    RecursiveRecordWithUnionTwo b4 = {i: "a", r:{i:4, r:5}};
    assertTrue(checkValueEquality(a4,b4));
    assertFalse(checkRefEquality(a4,b4));

    RecursiveRecordWithUnionTwo a5 = {i: "a", r:b3};
    RecursiveRecordWithUnionTwo b5 = {i: "a", r:b3};
    assertTrue(checkValueEquality(a5,b5));
    assertFalse(checkRefEquality(a5,b5));
}

type RecursiveRecordWithOptionalTypeA record {|
    int i;
    RecursiveRecordWithOptionalTypeA f?;
|};

type RecursiveRecordWithOptionalTypeB record {|
    int i;
    RecursiveRecordWithOptionalTypeB f?;
|};

function checkRefEquality(anydata a, anydata b) returns boolean {
    return a === b;
}

function checkValueEquality(anydata a, anydata b) returns boolean {
    return a == b;
}

const ASSERTION_ERR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}
