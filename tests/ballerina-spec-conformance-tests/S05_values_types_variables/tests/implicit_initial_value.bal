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

// Most types, including all simple basic types, have an implicit initial value, which is used to
// initialize structure members.
@test:Config {}
function testImplicitInitialValueOfNilType() {
    ()[] nilArray = [];
    nilArray[1] = ();
    test:assertEquals(nilArray[0], (), msg = "expected implicit initial value of nil to be ()");
}

@test:Config {}
function testImplicitInitialValueOfBooleanType() {
    boolean[] booleanArray = [];
    booleanArray[1] = true;
    test:assertEquals(booleanArray[0], false, msg = "expected implicit initial value of boolean to be false");
}

@test:Config {}
function testImplicitInitialValueOfIntType() {
    int[] intArray = [];
    intArray[1] = 100;
    test:assertEquals(intArray[0], 0, msg = "expected implicit initial value of int to be 0");
}

@test:Config {}
function testImplicitInitialValueOfFloatType() {
    float[] floatArray = [];
    floatArray[1] = 50.9;
    test:assertEquals(floatArray[0], 0.0, msg = "expected implicit initial value of float to be 0.0");
}

@test:Config {}
function testImplicitInitialValueOfDecimalType() {
    decimal[] decimalArray = [];
    decimalArray[1] = 50.9;
    test:assertEquals(decimalArray[0], 0.0d, msg = "expected implicit initial value of decimal to be 0.0");
}

@test:Config {}
function testImplicitInitialValueOfStringType() {
    string[] stringArray = [];
    stringArray[1] = "test string";
    test:assertEquals(stringArray[0], "", msg = "expected implicit initial value of string to be an empty string");
}

@test:Config {}
function testImplicitInitialValueOfIntArrayType() {
    int[][] twoDIntArray = [];
    twoDIntArray[1] = [1, 2, 3, 4];
    int[] expectedArray = [];
    test:assertEquals(twoDIntArray[0], expectedArray, msg = "expected implicit initial value of int[] to be []");
}

@test:Config {}
function testImplicitInitialValueOfFloatArrayType() {
    float[][4] twoDFloatArrayWithLength = [];
    twoDFloatArrayWithLength[1] = [1.1, 2.2, 3.3, 4.4];
    float[] expectedArray = [0.0, 0.0, 0.0, 0.0];
    test:assertEquals(twoDFloatArrayWithLength[0], expectedArray,
        msg = "expected implicit initial value of float[4] to be [0.0, 0.0, 0.0, 0.0]");
}

@test:Config {}
function testImplicitInitialValueOfXMLType() {
    xml[] xmlArray = [];
    xmlArray[1] = xml`<t></t>`;
    test:assertTrue(xmlArray[0].isEmpty(), msg = "expected implicit initial value of xml to be an empty sequence");
}

@test:Config {}
function testImplicitInitialValueOfUnionType() {
    (int|string|())[] unionArray1 = [];
    unionArray1[1] = 500;
    test:assertEquals(unionArray1[0], (), msg = "expected implicit initial value of a union with () to be ()");
}

@test:Config {}
function testImplicitInitialValueOfAnyType() {
    any[] anyArray = [];
    anyArray[1] = 500;
    test:assertEquals(anyArray[0], (), msg = "expected implicit initial value of any type to be ()");
}

@test:Config {}
function testImplicitInitialValueOfAnydataType() {
    anydata[] anydataArray = [];
    anydataArray[1] = 500;
    test:assertEquals(anydataArray[0], (), msg = "expected implicit initial value of anydata type to be ()");
}

@test:Config {}
function testImplicitInitialValueOfByteType() {
    byte[] byteArray = [];
    byteArray[1] = 3;
    test:assertEquals(byteArray[0], 0, msg = "expected implicit initial value of byte type to be 0");
}

@test:Config {}
function testImplicitInitialValueOfJSONType() {
    json[] jsonArray = [];
    jsonArray[1] = 3;
    test:assertEquals(jsonArray[0], (), msg = "expected implicit initial value of json type to be ()");
}

@test:Config {}
function testImplicitInitialValueOfTuples() {
    (int, boolean, string)[] tupleArray = [];
    tupleArray[1] = (200, true, "test string");
    (int, boolean, string) expectedTuple = (0, false, "");
    test:assertEquals(tupleArray[0], expectedTuple,
        msg = "expected implicit initial value of (int, boolean, string) to be (0, false, \"\")");
}

@test:Config {}
function testImplicitInitialValueOfMaps() {
    map<any>[] mapArray = [];
    mapArray[1] = { fieldOne: "valueOne" };
    any implicitInitVal = mapArray[0];
    test:assertTrue(implicitInitVal is map<any>, msg = "expected implicit initial value to be of type map<any>");
    test:assertEquals(mapArray[0].length(), 0, msg = "expected an empty map");
}

@test:Config {}
function testImplicitInitialValueOfRecords() {
    QuuxRecord[] quuxRecordArray = [];
    quuxRecordArray[1] = { quuxFieldOne: "valueOne" };
    QuuxRecord expectedRecord = {quuxFieldOne: ""};
    test:assertEquals(quuxRecordArray[0], expectedRecord,
        msg = "expected implicit initial value of QuuxRecord to be { quuxFieldOne: \"\" }");
}

@test:Config {}
function testImplicitInitialValueOfTables() {
    table<QuuzRecord>[] tableArray = [];
    tableArray[1] = table{};
    any implicitInitVal = tableArray[0];
    test:assertTrue(implicitInitVal is table<QuuzRecord>,
        msg = "expected implicit initial value to be of type table<QuuzRecord>");
    test:assertEquals(tableArray[0].length(), 0, msg = "expected table to be empty");
}

@test:Config {}
function testImplicitInitialValueOfSingletonTypes() {
    One[] singletonArray = [];
    singletonArray[1] = 1;
    One expectedVal = 1;
    test:assertEquals(singletonArray[0], expectedVal,
        msg = "expected implicit initial value of a singleton to be the singleton value");
}

type Union 0|1|2;

@test:Config {}
function testImplicitInitialValueOfUnions() {
    Union[] unionArray2 = [];
    unionArray2[1] = 2;
    Union expectedVal = 0;
    test:assertEquals(unionArray2[0], expectedVal, msg = "expected implicit initial value of this union should be 0");
}

public type QuuzRecord record {|
    int quuzFieldOne;
|};

public type QuuxRecord record {|
    string quuxFieldOne;
|};
