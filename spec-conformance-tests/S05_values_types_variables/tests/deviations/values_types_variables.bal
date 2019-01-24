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
import utils;

// However, for other types of value, what is stored in the variable or member is a 
// reference to the value; the value itself has its own separate storage.
@test:Config {
    groups: ["deviation"]
}
function testNonSimpleValuesStoredInStructuresBroken() {
    table<utils:BarRecord> t1 = table{};
    utils:BarRecord b4 = { barFieldOne: 100 };
    _ = t1.add(b4);
    b4.barFieldOne = I;
    
    utils:BarRecord b5;
    foreach utils:BarRecord barRecord in t1 {
        b5 = barRecord;
    }
    // test:assertEquals(b5.barFieldOne, I, msg = "expected table member to have been updated");
    test:assertEquals(b5.barFieldOne, 100, msg = "expected table member to not have been updated");
}

// References make it possible for distinct members of a structure to refer to values that are
// identical, in the sense that they are stored in the same location.
@test:Config {
    groups: ["deviation"]
}
function testDistinctStructureMembersReferringToSameValueBroken() {
    table<utils:BarRecord> t1 = table{};
    utils:BarRecord b4 = { barFieldOne: 100 };
    utils:BarRecord b5 = { barFieldOne: 200 };
    _ = t1.add(b4);
    _ = t1.add(b5);
    _ = t1.add(b4);
    
    utils:BarRecord? b6 = ();
    utils:BarRecord? b7 = ();
    int count = 0;
    foreach utils:BarRecord barRecord in t1 {
        if (count == 0) {
            b6 = barRecord;
        }
        if (count == 1) {
            b7 = barRecord;
        }
        count += 1;
    }
    test:assertTrue(b6 is utils:BarRecord, msg = "expected values to be at the same location");
    // test:assertTrue(b6 === b7, msg = "expected values to be at the same location");
    test:assertFalse(b6 === b7, msg = "expected values to not be at the same location");
}

// Most basic types of structured values (along with one basic type of simple value) are
// iterable, meaning that a value of the type can be accessed as a sequence of simpler values.
// TODO: Support iterable for Tuple type and String type
// https://github.com/ballerina-platform/ballerina-lang/issues/13172
// https://github.com/ballerina-platform/ballerina-lang/issues/13165
@test:Config {
    groups: ["deviation"]
}
function testIterableTypesBroken() {
    //    (int, string, boolean) iterableTuple = (100, "test string", true);
    //    int count = 0;
    //    foreach var variable in iterableTuple {
    //        count += 1;
    //    }
    //    test:assertEquals(count, 3, msg = "expected int array to iterate 3 loops");
    //
    //    string iterableString = "Hello Ballerina";
    //    string result = "";
    //    foreach var char in iterableTuple {
    //        // TODO
    //    }
}

type Union 0|1|2;

// Most types, including all simple basic types, have an implicit initial value, which is used to
// initialize structure members.
// TODO: Fix tuple, map, record, table, typedesc, singleton and union type implicit initial values
// https://github.com/ballerina-platform/ballerina-lang/issues/13166
@test:Config {
    groups: ["deviation"]
}
function testImplicitInitialValuesBroken() {
    (int, boolean, string)[] tupleArray = [];
    tupleArray[1] = (200, true, "test string");
    test:assertEquals(tupleArray[0], (),
        msg = "expected implicit initial value of (int, boolean, string) to be (0, false, \"\")");

    map<any>[] mapArray = [];
    mapArray[1] = { fieldOne: "valueOne" };
    map<any> expectedMap = {};
    test:assertEquals(mapArray[0], (), msg = "expected implicit initial value of map to be {}");

    utils:FooRecord[] fooRecordArray = [];
    fooRecordArray[1] = { fooFieldOne: "valueOne" };
    test:assertEquals(fooRecordArray[0], (),
        msg = "expected implicit initial value of FooRecord to { fooFieldOne: \"\" }");

    table<utils:BarRecord>[] tableArray = [];
    tableArray[1] = table{};
    test:assertEquals(tableArray[0], (), msg = "expected implicit initial value of string to be an empty string");

    //typedesc[] typedescArray = [];
    //typedescArray[1] = int;
    //test:assertTrue(typedescArray[0], (), msg = "expected implicit initial value of typedesc to be ()");

    One[] singletonArray = [];
    singletonArray[1] = 1;
    test:assertEquals(singletonArray[0], (),
        msg = "expected implicit initial value of a singleton to be the singleton value");

    Union[] unionArray2 = [];
    unionArray2[1] = 2;
    test:assertEquals(unionArray2[0], (), msg = "expected implicit initial value of this union should be 0");
}
