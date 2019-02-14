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
    table<QuuzRecord> t1 = table{};
    QuuzRecord b4 = { quuzFieldOne: 100 };
    _ = t1.add(b4);
    b4.quuzFieldOne = I;
    
    QuuzRecord b5;
    foreach QuuzRecord barRecord in t1 {
        b5 = barRecord;
    }
    // test:assertEquals(b5.quuzFieldOne, I, msg = "expected table member to have been updated");
    test:assertEquals(b5.quuzFieldOne, 100, msg = "expected table member to not have been updated");
}

// References make it possible for distinct members of a structure to refer to values that are
// identical, in the sense that they are stored in the same location.
@test:Config {
    groups: ["deviation"]
}
function testDistinctStructureMembersReferringToSameValueBroken() {
    table<QuuzRecord> t1 = table{};
    QuuzRecord b4 = { quuzFieldOne: 100 };
    QuuzRecord b5 = { quuzFieldOne: 200 };
    _ = t1.add(b4);
    _ = t1.add(b5);
    _ = t1.add(b4);
    
    QuuzRecord? b6 = ();
    QuuzRecord? b7 = ();
    int count = 0;
    foreach QuuzRecord barRecord in t1 {
        if (count == 0) {
            b6 = barRecord;
        }
        if (count == 1) {
            b7 = barRecord;
        }
        count += 1;
    }
    test:assertTrue(b6 is QuuzRecord, msg = "expected values to be at the same location");
    // test:assertTrue(b6 === b7, msg = "expected values to be at the same location");
    test:assertFalse(b6 === b7, msg = "expected values to not be at the same location");
}

// Most basic types of structured values (along with one basic type of simple value) are
// iterable, meaning that a value of the type can be accessed as a sequence of simpler values.
// TODO: Support iterable for Tuple type and String type
// https://github.com/ballerina-platform/ballerina-lang/issues/13172
@test:Config {
    groups: ["deviation"]
}
function testIterableTypesBroken() {
    //    string iterableString = "Hello Ballerina";
    //    string result = "";
    //    foreach var char in iterableString {
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

    QuuxRecord[] quuxRecordArray = [];
    quuxRecordArray[1] = { quuxFieldOne: "valueOne" };
    test:assertEquals(quuxRecordArray[0], (),
        msg = "expected implicit initial value of QuuxRecord to be { quuxFieldOne: \"\" }");

    table<QuuzRecord>[] tableArray = [];
    tableArray[1] = table{};
    test:assertEquals(tableArray[0], (), msg = "expected implicit initial value of string to be an empty string");

    //typedesc[] typedescArray = [];
    //typedescArray[1] = int;
    //test:assertTrue(typedescArray[0], (), msg = "expected implicit initial value of typedesc to be ()");

    One?[] singletonArray = [];
    singletonArray[1] = 1;
    test:assertEquals(singletonArray[0], (),
        msg = "expected implicit initial value of a singleton to be the singleton value");

    Union[] unionArray2 = [];
    unionArray2[1] = 2;
    test:assertEquals(unionArray2[0], (), msg = "expected implicit initial value of this union should be 0");

    QuxObject[] objArray = [];
    objArray[1] = new QuxObject();
    test:assertEquals(objArray[0], (), msg = "expected implicit initial value of QuxObject " +
        "should be '{fooFieldOne:\"init value\"}'");
}

public type QuuzRecord record {
    int quuzFieldOne;
    !...;
};

public type QuuxRecord record {
    string quuxFieldOne;
    !...;
};

public type QuxObject object {
    public string fooFieldOne;

    public function __init() {
        self.fooFieldOne = "init value";
    }

    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};
