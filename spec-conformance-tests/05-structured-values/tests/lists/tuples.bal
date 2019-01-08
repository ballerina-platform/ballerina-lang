// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;

// A list value is a container that keeps its members in an ordered list.
@test:Config {}
function testTupleMemberOrder() {
    int a1 = 30;
    string a2 = "10";
    float a3 = 20.2;
    int a4 = 11;

    (int, string, float) tuple = (a1, a2, a3);

    test:assertEquals(tuple[0], a1, msg = "expected value not found at index 0");
    test:assertEquals(tuple[1], a2, msg = "expected value not found at index 1");
    test:assertEquals(tuple[2], a3, msg = "expected value not found at index 2");

    tuple[0] = a4;
    test:assertEquals(tuple[0], a4, msg = "expected value not found at index 0");
    test:assertEquals(tuple[1], a2, msg = "expected value not found at index 1");
    test:assertEquals(tuple[2], a3, msg = "expected value not found at index 2");

    FooRecord a5 = { fooFieldOne: "valueOne" };
    BarRecord a6 = { barFieldOne: 100 };
    FooRecord a7 = { fooFieldOne: "valueTwo" };

    (FooRecord, BarRecord) tuple2 = (a5, a6);

    test:assertEquals(tuple2[0], a5, msg = "expected value not found at index 0");
    test:assertEquals(tuple2[1], a6, msg = "expected value not found at index 1");

    tuple2[0] = a7;
    test:assertEquals(tuple2[0], a7, msg = "expected value not found at index 0");

    FooObject a8 = new("valueOne");
    BarObject a9 = new(200);
    FooObject a10 = new("valueTwo");

    (FooObject, BarObject) tuple3 = (a8, a9);

    test:assertEquals(tuple3[0], a8, msg = "expected value not found at index 0");
    test:assertEquals(tuple3[1], a9, msg = "expected value not found at index 1");

    tuple3[0] = a10;
    test:assertEquals(tuple3[0], a10, msg = "expected value not found at index 0");
}

// A member of a list can be referenced by an integer index representing its position in the list.
// For a list of length n, the indices of the members of the list, from first to last, are 0,1,...,n - 1.
@test:Config {}
function testTupleMemberReferenceByValidIntegerIndex() {
    string a1 = "test string 1";
    string a2 = "test string 2";
    int a3 = 100;
    int a4 = 200;
    boolean a5 = true;
    int a6 = 300;
    (string, string, int, int, boolean) tuple = (a1, a2, a3, a4, a5);

    string b1 = tuple[0];
    string b2 = tuple[1];
    int b3 = tuple[2];
    int b4 = tuple[3];
    boolean b5 = tuple[4];

    test:assertEquals(b1, a1, msg = "expected value not found at index 0");
    test:assertEquals(b2, a2, msg = "expected value not found at index 1");
    test:assertEquals(b3, a3, msg = "expected value not found at index 2");
    test:assertEquals(b4, a4, msg = "expected value not found at index 3");
    test:assertEquals(b5, a5, msg = "expected value not found at index 4");

    tuple[2] = a6;
    int b6 = tuple[2];
    test:assertEquals(b6, a6, msg = "expected value not found at index 2");

    FooRecord a7 = { fooFieldOne: "valueOne" };
    BarRecord a8 = { barFieldOne: 100 };
    FooRecord a9 = { fooFieldOne: "valueTwo" };
    BarRecord a10 = { barFieldOne: 50 };
    (FooRecord, BarRecord, FooRecord, BarRecord) tuple2 = (a7, a8, a9, a10);

    FooRecord b7 = tuple2[0];
    BarRecord b8 = tuple2[1];
    FooRecord b9 = tuple2[2];
    BarRecord b10 = tuple2[3];

    test:assertEquals(b7, a7, msg = "expected value not found at index 0");
    test:assertEquals(b8, a8, msg = "expected value not found at index 1");
    test:assertEquals(b9, a9, msg = "expected value not found at index 2");
    test:assertEquals(b10, a10, msg = "expected value not found at index 3");

    FooObject a11 = new("valueOne");
    BarObject a12 = new(200);
    FooObject a13 = new("valueTwo");
    BarObject a14 = new(180);

    (FooObject, BarObject, FooObject, BarObject) tuple3 = (a11, a12, a13, a14);

    FooObject b11 = tuple3[0];
    BarObject b12 = tuple3[1];
    FooObject b13 = tuple3[2];
    BarObject b14 = tuple3[3];

    test:assertEquals(b11, a11, msg = "expected value not found at index 0");
    test:assertEquals(b12, a12, msg = "expected value not found at index 1");
    test:assertEquals(b13, a13, msg = "expected value not found at index 2");
    test:assertEquals(b14, a14, msg = "expected value not found at index 3");
}

// The shape of a list value is an ordered list of the shapes of its members.
@test:Config {}
function testTupleShape() {
    // TODO:
}

// The inherent type of a list value determines a type Ti for a member with index i.
// The runtime system will enforce a constraint that a value written to index i will
// belong to type Ti. Note that the constraint is not merely that the value looks
// like Ti.
@test:Config {}
function testTupleInherentTypeViolation() {
    (int, int) tuple = (1, 2);
    (any, any) tupleWithAnyTypedMembers = tuple;
    assertErrorReason(trap insertElementToTuple(tupleWithAnyTypedMembers, "not an int"),
        "{ballerina}InherentTypeViolation",
        "invalid reason on inherent type violating tuple insertion");

    (map<string>, map<string>) stringMapTuple = (
        {
            one: "test string 1",
            two: "test string 2"
        },
        {
            three: "test string 3"
        }
    );
    tupleWithAnyTypedMembers = stringMapTuple;

    // `map<string|int>` looks like `map<string>`
    map<string|int> stringOrIntMap = {
        one: "test string 1",
        two: "test string 2"
    };
    assertErrorReason(trap insertElementToTuple(tupleWithAnyTypedMembers, stringOrIntMap),
        "{ballerina}InherentTypeViolation",
        "invalid reason on inherent type violating tuple insertion");

    FooRecord a1 = { fooFieldOne: "valueOne" };
    FooRecord a2 = { fooFieldOne: "valueTwo" };
    (FooRecord, FooRecord) tuple2 = (a1, a2);
    tupleWithAnyTypedMembers = tuple2;
    assertErrorReason(trap insertElementToTuple(tupleWithAnyTypedMembers, "not a FooRecord"),
        "{ballerina}InherentTypeViolation",
        "invalid reason on inherent type violating tuple insertion");

    (map<FooRecord>, map<FooRecord>) fooRecordMapTuple = (
    {
        one: { fooFieldOne: "valueOne" },
        two: { fooFieldOne: "valueTwo" }
    },
    {
        three: { fooFieldOne: "valueThree" }
    }
    );
    tupleWithAnyTypedMembers = fooRecordMapTuple;

    // `map<FooRecord|BarRecord>` looks like `map<FooRecord>`
    map<FooRecord|BarRecord> FooRecordOrBarRecordMap = {
        one: { fooFieldOne: "valueOne" },
        two: { fooFieldOne: "valueTwo" }
    };
    assertErrorReason(trap insertElementToTuple(tupleWithAnyTypedMembers, FooRecordOrBarRecordMap),
        "{ballerina}InherentTypeViolation",
        "invalid reason on inherent type violating tuple insertion");

    FooObject a3 = new("valueOne");
    FooObject a4 = new("valueTwo");
    (FooObject, FooObject) tuple3 = (a3, a4);
    tupleWithAnyTypedMembers = tuple3;
    assertErrorReason(trap insertElementToTuple(tupleWithAnyTypedMembers, "not a FooRecord"),
        "{ballerina}InherentTypeViolation",
        "invalid reason on inherent type violating tuple insertion");

    (map<FooObject>, map<FooObject>) fooObjectMapTuple = (
        {
            one: new("valueOne"),
            two: new("valueTwo")
        },
        {
            three: new("valueThree")
        }
    );
    tupleWithAnyTypedMembers = fooObjectMapTuple;

    FooObject f1 = new("valueOne");
    FooObject f2 = new("valueTwo");

    // `map<FooObject|BarObject>` looks like `map<FooObject>`
    map<FooObject|BarObject> FooObjectOrBarObjectMap = {
        one: f1,
        two: f2
    };
    assertErrorReason(trap insertElementToTuple(tupleWithAnyTypedMembers, FooObjectOrBarObjectMap),
        "{ballerina}InherentTypeViolation",
        "invalid reason on inherent type violating tuple insertion");
}

function insertElementToTuple((any, any) tuple, any element) {
    tuple[0] = element;
}
